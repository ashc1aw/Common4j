package cc.ashclaw.common4j.retry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Retryer")
class RetryerTest {

    @Nested
    @DisplayName("success cases")
    class SuccessCases {

        @Test
        @DisplayName("succeeds on first attempt")
        void firstAttempt() throws Exception {
            var retryer = new Retryer(RetryConfig.DEFAULTS);
            var result = retryer.execute(() -> "ok");
            assertEquals("ok", result);
        }

        @Test
        @DisplayName("succeeds after one retry")
        void afterOneRetry() throws Exception {
            var config = RetryConfig.builder()
                    .maxAttempts(3)
                    .initialDelay(Duration.ofMillis(1))
                    .jitter(0)
                    .build();
            var retryer = new Retryer(config);
            var count = new AtomicInteger(0);

            String result = retryer.execute(() -> {
                if (count.incrementAndGet() < 2) throw new IOException("fail");
                return "recovered";
            });
            assertEquals("recovered", result);
            assertEquals(2, count.get());
        }
    }

    @Nested
    @DisplayName("exhaustion")
    class Exhaustion {

        @Test
        @DisplayName("throws after max attempts")
        void maxAttemptsExceeded() {
            var config = RetryConfig.builder()
                    .maxAttempts(3)
                    .initialDelay(Duration.ofMillis(1))
                    .jitter(0)
                    .build();
            var retryer = new Retryer(config);
            var count = new AtomicInteger(0);

            var ex = assertThrows(RetryExhaustedException.class, () ->
                    retryer.execute(() -> {
                        count.incrementAndGet();
                        throw new IOException("always fail");
                    }));
            assertEquals(3, ex.attempts());
            assertEquals(3, count.get());
        }
    }

    @Nested
    @DisplayName("exception filtering")
    class ExceptionFiltering {

        @Test
        @DisplayName("retries matching exception types")
        void retriesMatching() {
            var config = RetryConfig.builder()
                    .maxAttempts(3)
                    .initialDelay(Duration.ofMillis(1))
                    .jitter(0)
                    .retryOn(IOException.class)
                    .build();
            var retryer = new Retryer(config);
            var count = new AtomicInteger(0);

            var ex = assertThrows(RetryExhaustedException.class, () ->
                    retryer.execute(() -> {
                        count.incrementAndGet();
                        throw new IOException("io error");
                    }));
            assertEquals(3, count.get()); // all 3 attempts were made
        }

        @Test
        @DisplayName("non-matching exception thrown immediately")
        void nonMatchingThrowsImmediately() {
            var config = RetryConfig.builder()
                    .maxAttempts(3)
                    .initialDelay(Duration.ofMillis(10))
                    .jitter(0)
                    .retryOn(IOException.class)
                    .build();
            var retryer = new Retryer(config);
            var count = new AtomicInteger(0);

            var ex = assertThrows(RetryExhaustedException.class, () ->
                    retryer.execute(() -> {
                        count.incrementAndGet();
                        throw new IllegalStateException("not retryable");
                    }));
            assertEquals(1, count.get()); // only 1 attempt
            assertInstanceOf(IllegalStateException.class, ex.getCause());
        }
    }

    @Nested
    @DisplayName("errors never retried")
    class ErrorsNotRetried {

        @Test
        @DisplayName("Error is rethrown immediately")
        void errorRethrown() {
            var retryer = new Retryer(RetryConfig.DEFAULTS);
            var count = new AtomicInteger(0);

            assertThrows(OutOfMemoryError.class, () ->
                    retryer.execute(() -> {
                        count.incrementAndGet();
                        throw new OutOfMemoryError("oom");
                    }));
            assertEquals(1, count.get());
        }
    }

    @Nested
    @DisplayName("executeRunnable")
    class ExecuteRunnable {

        @Test
        @DisplayName("runnable succeeds")
        void runnableSuccess() throws Exception {
            var retryer = new Retryer(RetryConfig.DEFAULTS);
            var ran = new AtomicInteger(0);
            retryer.executeRunnable(ran::incrementAndGet);
            assertEquals(1, ran.get());
        }
    }

    @Nested
    @DisplayName("config validation")
    class ConfigValidation {

        @Test
        @DisplayName("maxAttempts must be >= 1")
        void maxAttempts() {
            assertThrows(IllegalArgumentException.class, () ->
                    RetryConfig.builder().maxAttempts(0).build());
        }

        @Test
        @DisplayName("initialDelay must be > 0")
        void initialDelay() {
            assertThrows(IllegalArgumentException.class, () ->
                    RetryConfig.builder().initialDelay(Duration.ZERO).build());
        }

        @Test
        @DisplayName("backoffMultiplier must be >= 1")
        void backoffMultiplier() {
            assertThrows(IllegalArgumentException.class, () ->
                    RetryConfig.builder().backoffMultiplier(0.5).build());
        }

        @Test
        @DisplayName("jitter must be in range")
        void jitter() {
            assertThrows(IllegalArgumentException.class, () ->
                    RetryConfig.builder().jitter(1.5).build());
            assertThrows(IllegalArgumentException.class, () ->
                    RetryConfig.builder().jitter(-0.1).build());
        }
    }

    @Nested
    @DisplayName("interface proxy")
    class InterfaceProxy {

        interface Greeter {
            String greet(String name) throws Exception;
        }

        @Test
        @DisplayName("proxy succeeds on first call")
        void proxySuccess() throws Exception {
            Greeter real = name -> "Hello, " + name;
            var proxy = Retryer.proxy(Greeter.class, real, RetryConfig.DEFAULTS);
            assertEquals("Hello, World", proxy.greet("World"));
        }

        @Test
        @DisplayName("proxy retries on failure")
        void proxyRetries() throws Exception {
            var count = new AtomicInteger(0);
            Greeter real = name -> {
                if (count.incrementAndGet() < 2) throw new IOException("fail");
                return "ok";
            };
            var config = RetryConfig.builder()
                    .maxAttempts(3)
                    .initialDelay(Duration.ofMillis(1))
                    .jitter(0)
                    .build();
            var proxy = Retryer.proxy(Greeter.class, real, config);
            assertEquals("ok", proxy.greet("test"));
            assertEquals(2, count.get());
        }

        @Test
        @DisplayName("proxy exhausts retries")
        void proxyExhausted() throws Exception {
            var count = new AtomicInteger(0);
            Greeter real = name -> {
                count.incrementAndGet();
                throw new IOException("fail");
            };
            var config = RetryConfig.builder()
                    .maxAttempts(3)
                    .initialDelay(Duration.ofMillis(1))
                    .jitter(0)
                    .build();
            var proxy = Retryer.proxy(Greeter.class, real, config);
            var ex = assertThrows(RetryExhaustedException.class,
                    () -> proxy.greet("test"));
            assertEquals(3, count.get());
            assertEquals(3, ex.attempts());
        }

        @Test
        @DisplayName("proxy preserves exception type for retryOn filtering")
        void proxyPreservesExceptionType() {
            var config = RetryConfig.builder()
                    .maxAttempts(3)
                    .initialDelay(Duration.ofMillis(1))
                    .jitter(0)
                    .retryOn(IOException.class)
                    .build();
            var count = new AtomicInteger(0);
            Greeter real = name -> {
                count.incrementAndGet();
                throw new IllegalStateException("not retryable");
            };
            var proxy = Retryer.proxy(Greeter.class, real, config);
            var ex = assertThrows(RetryExhaustedException.class,
                    () -> proxy.greet("test"));
            assertEquals(1, count.get());
            assertInstanceOf(IllegalStateException.class, ex.getCause());
        }

        @Test
        @DisplayName("non-interface throws")
        void nonInterfaceThrows() {
            assertThrows(IllegalArgumentException.class, () ->
                    Retryer.proxy(String.class, "hello", RetryConfig.DEFAULTS));
        }

        @Test
        @DisplayName("null args throw")
        void nullArgsThrow() {
            Greeter real = name -> "hi";
            assertThrows(NullPointerException.class, () ->
                    Retryer.proxy(null, real, RetryConfig.DEFAULTS));
            assertThrows(NullPointerException.class, () ->
                    Retryer.proxy(Greeter.class, null, RetryConfig.DEFAULTS));
            assertThrows(NullPointerException.class, () ->
                    Retryer.proxy(Greeter.class, real, null));
        }
    }

    @Nested
    @DisplayName("builder defaults")
    class BuilderDefaults {

        @Test
        @DisplayName("DEFAULTS has sensible values")
        void defaults() {
            assertEquals(3, RetryConfig.DEFAULTS.maxAttempts());
            assertEquals(Duration.ofMillis(200), RetryConfig.DEFAULTS.initialDelay());
            assertEquals(Duration.ofSeconds(30), RetryConfig.DEFAULTS.maxDelay());
            assertEquals(2.0, RetryConfig.DEFAULTS.backoffMultiplier());
            assertEquals(0.2, RetryConfig.DEFAULTS.jitter());
        }
    }
}
