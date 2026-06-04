package cc.ashclaw.common4j.retry;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable configuration for a {@link Retryer}.
 *
 * <p>Use {@link #DEFAULTS} for reasonable defaults, or build a custom config:
 *
 * <pre>{@code
 * var config = RetryConfig.builder()
 *     .maxAttempts(5)
 *     .initialDelay(Duration.ofMillis(100))
 *     .maxDelay(Duration.ofSeconds(10))
 *     .jitter(0.2)
 *     .retryOn(IOException.class, TimeoutException.class)
 *     .build();
 * }</pre>
 *
 * @param maxAttempts        maximum number of attempts (1 = no retry), default 3
 * @param initialDelay       delay before the first retry, default 200ms
 * @param maxDelay           maximum delay between retries, default 30s
 * @param backoffMultiplier  factor applied to each subsequent delay, default 2.0
 * @param jitter             random factor applied to delay (0 = none, 0.5 = ±50%), default 0.2
 * @param retryOn            exception types that trigger a retry, default any
 */
public record RetryConfig(
        int maxAttempts,
        Duration initialDelay,
        Duration maxDelay,
        double backoffMultiplier,
        double jitter,
        List<Class<? extends Throwable>> retryOn) {

    public RetryConfig {
        if (maxAttempts < 1)
            throw new IllegalArgumentException("maxAttempts must be >= 1: " + maxAttempts);
        Objects.requireNonNull(initialDelay, "initialDelay must not be null");
        Objects.requireNonNull(maxDelay, "maxDelay must not be null");
        if (initialDelay.toMillis() <= 0)
            throw new IllegalArgumentException("initialDelay must be > 0: " + initialDelay);
        if (maxDelay.toMillis() < initialDelay.toMillis())
            throw new IllegalArgumentException(
                    "maxDelay must be >= initialDelay: " + maxDelay + " < " + initialDelay);
        if (backoffMultiplier < 1.0)
            throw new IllegalArgumentException(
                    "backoffMultiplier must be >= 1.0: " + backoffMultiplier);
        if (jitter < 0.0 || jitter > 1.0)
            throw new IllegalArgumentException(
                    "jitter must be in [0.0, 1.0]: " + jitter);
        retryOn = List.copyOf(retryOn);
    }

    /** Default configuration: 3 attempts, 200ms initial, 2x backoff, 20% jitter, 30s cap. */
    public static final RetryConfig DEFAULTS = builder().build();

    /** Creates a new {@link Builder} with defaults. */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link RetryConfig}.
     *
     * <p>This is a traditional mutable builder (not a record) so users can set
     * individual properties without specifying everything at once.
     */
    public static final class Builder {
        private int maxAttempts = 3;
        private Duration initialDelay = Duration.ofMillis(200);
        private Duration maxDelay = Duration.ofSeconds(30);
        private double backoffMultiplier = 2.0;
        private double jitter = 0.2;
        @SuppressWarnings("rawtypes")
        private List<Class<? extends Throwable>> retryOn = List.of(Exception.class);

        /** Maximum number of attempts (1 = no retry). Default 3. */
        public Builder maxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        /** Delay before the first retry. Default 200ms. */
        public Builder initialDelay(Duration initialDelay) {
            this.initialDelay = Objects.requireNonNull(initialDelay);
            return this;
        }

        /** Delay before the first retry in milliseconds. */
        public Builder initialDelay(long millis) {
            return initialDelay(Duration.ofMillis(millis));
        }

        /** Maximum delay between retries. Default 30s. */
        public Builder maxDelay(Duration maxDelay) {
            this.maxDelay = Objects.requireNonNull(maxDelay);
            return this;
        }

        /** Maximum delay between retries in milliseconds. */
        public Builder maxDelay(long millis) {
            return maxDelay(Duration.ofMillis(millis));
        }

        /** Factor applied to each subsequent delay. Default 2.0. */
        public Builder backoffMultiplier(double multiplier) {
            this.backoffMultiplier = multiplier;
            return this;
        }

        /**
         * Random jitter factor applied to each delay.
         * 0.0 = no jitter, 0.5 = delay varies by ±50%.
         * Default 0.2.
         */
        public Builder jitter(double jitter) {
            this.jitter = jitter;
            return this;
        }

        /**
         * Exception types that trigger a retry. Default: any {@link Exception}.
         * <p>Errors (OutOfMemoryError, etc.) are never retried.
         */
        @SafeVarargs
        public final Builder retryOn(Class<? extends Throwable>... exceptions) {
            this.retryOn = List.copyOf(Arrays.asList(exceptions));
            return this;
        }

        /** Builds the immutable config. */
        public RetryConfig build() {
            return new RetryConfig(
                    maxAttempts, initialDelay, maxDelay,
                    backoffMultiplier, jitter, retryOn);
        }
    }
}
