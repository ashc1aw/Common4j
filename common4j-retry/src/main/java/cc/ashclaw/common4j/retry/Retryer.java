package cc.ashclaw.common4j.retry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Executes a task with retry logic — exponential backoff, optional jitter,
 * and configurable exception filtering.
 *
 * Basic usage:
 * <pre>{@code
 * var retryer = new Retryer(RetryConfig.DEFAULTS);
 * String result = retryer.execute(() -> unstableService.fetch());
 * }</pre>
 *
 * Custom config:
 * <pre>{@code
 * var config = RetryConfig.builder()
 *     .maxAttempts(3)
 *     .initialDelay(500)
 *     .jitter(0.3)
 *     .retryOn(IOException.class)
 *     .build();
 * var retryer = new Retryer(config);
 * }</pre>
 *
 * <p>Thread-safe: a single instance can be shared. Virtual-thread friendly.
 * The sleeping thread is interrupted on JVM shutdown via the standard
 * interruption mechanism.
 *
 * <p>Errors ({@link Error}, {@link VirtualMachineError}, etc.) are never retried —
 * they are rethrown immediately regardless of {@link RetryConfig#retryOn()}.
 */
public final class Retryer {

    private final RetryConfig config;
    private final ThreadLocalRandom rng = ThreadLocalRandom.current();

    /** Creates a retryer with the given configuration.
     * @param config the retry configuration, must not be null */
    public Retryer(RetryConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    // ── Execute ──────────────────────────────────────────────────────

    /**
     * Executes the task, retrying on failure according to the configuration.
     * Returns the result on the first successful attempt.
     *
     * @param <T>  the return type of the task
     * @param task the task to execute, must not be null
     * @return the task's return value
     * @throws RetryExhaustedException if all attempts fail
     * @throws InterruptedException    if the sleeping thread is interrupted
     */
    public <T> T execute(Callable<T> task) throws RetryExhaustedException, InterruptedException {
        Objects.requireNonNull(task, "task must not be null");

        Exception lastException;
        try {
            return task.call();
        } catch (Error e) {
            throw e;   // Errors are never retried
        } catch (Exception e) {
            if (!shouldRetry(e)) throw new RetryExhaustedException("non-retryable exception", 1, e);
            lastException = e;
        }

        long delayMs = config.initialDelay().toMillis();
        long maxMs = config.maxDelay().toMillis();

        for (int attempt = 2; attempt <= config.maxAttempts(); attempt++) {
            sleep(applyJitter(delayMs));

            try {
                return task.call();
            } catch (Error e) {
                throw e;
            } catch (Exception e) {
                if (!shouldRetry(e)) {
                    throw new RetryExhaustedException(
                            "non-retryable exception after %d attempts".formatted(attempt),
                            attempt, e);
                }
                lastException = e;
            }

            // Increase delay for next attempt (cap at maxDelay)
            delayMs = Math.min(
                    (long) (delayMs * config.backoffMultiplier()),
                    maxMs);
        }

        throw new RetryExhaustedException(
                "all %d attempts failed".formatted(config.maxAttempts()),
                config.maxAttempts(), lastException);
    }

    /**
     * Executes a runnable-style task with retry. Convenience overload for
     * tasks that return nothing.
     *
     * @param task the runnable task to execute, must not be null
     * @throws RetryExhaustedException if all attempts fail
     * @throws InterruptedException    if the sleeping thread is interrupted
     */
    public void executeRunnable(Runnable task)
            throws RetryExhaustedException, InterruptedException {
        Objects.requireNonNull(task, "task must not be null");
        execute(() -> { task.run(); return null; });
    }

    // ── Interface proxy ────────────────────────────────────────────────

    /**
     * Wraps an interface implementation in a JDK dynamic proxy that retries
     * every method call according to the given config.
     *
     * <pre>{@code
     * var client = Retryer.proxy(ApiClient.class, realClient, config);
     * client.fetch();  // auto-retries on failure
     * }</pre>
     *
     * <p>No external dependencies — pure JDK {@link Proxy}. The proxy
     * preserves the original exception type so {@link RetryConfig#retryOn()}
     * filtering works correctly.
     *
     * @param <T>    the interface type
     * @param iface  the interface class
     * @param target the real implementation
     * @param config the retry configuration
     * @return a dynamic proxy that retries every method call
     * @throws IllegalArgumentException if {@code iface} is not an interface
     */
    @SuppressWarnings("unchecked")
    public static <T> T proxy(Class<T> iface, T target, RetryConfig config) {
        Objects.requireNonNull(iface, "iface must not be null");
        Objects.requireNonNull(target, "target must not be null");
        Objects.requireNonNull(config, "config must not be null");
        if (!iface.isInterface()) {
            throw new IllegalArgumentException(
                    "iface must be an interface: " + iface.getName());
        }
        var retryer = new Retryer(config);
        return (T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class<?>[] { iface },
                (proxy, method, args) -> retryer.execute(() -> {
                    try {
                        return method.invoke(target, args);
                    } catch (InvocationTargetException e) {
                        var cause = e.getCause();
                        if (cause instanceof Exception ex) throw ex;
                        if (cause instanceof Error err) throw err;
                        throw e; // unreachable — ITE always wraps Throwable
                    }
                }));
    }

    // ── Internal ──────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private boolean shouldRetry(Throwable t) {
        for (var cls : config.retryOn()) {
            if (cls.isInstance(t)) return true;
        }
        return false;
    }

    private long applyJitter(long delayMs) {
        double j = config.jitter();
        if (j <= 0.0) return delayMs;
        double factor = 1.0 + (rng.nextDouble() * 2.0 - 1.0) * j;
        return Math.max(1, (long) (delayMs * factor));
    }

    @SuppressWarnings("java:S2273") // sleep in loop is intentional — retry with backoff
    private static void sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    @Override
    public String toString() {
        return "Retryer[maxAttempts=%d, initial=%s, max=%s]".formatted(
                config.maxAttempts(), config.initialDelay(), config.maxDelay());
    }
}
