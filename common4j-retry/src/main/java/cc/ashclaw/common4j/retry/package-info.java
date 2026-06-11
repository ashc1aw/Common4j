/**
 * Lightweight retry with exponential backoff and jitter — fills a gap
 * the JDK leaves completely open.
 *
 * Quick start:
 * <pre>{@code
 * var retryer = new Retryer(RetryConfig.DEFAULTS);
 * String result = retryer.execute(() -> httpClient.get(url));
 * }</pre>
 *
 * Custom configuration:
 * <pre>{@code
 * var config = RetryConfig.builder()
 *     .maxAttempts(3)
 *     .initialDelay(200)
 *     .maxDelay(Duration.ofSeconds(5))
 *     .jitter(0.3)
 *     .retryOn(IOException.class, TimeoutException.class)
 *     .build();
 * var retryer = new Retryer(config);
 * }</pre>
 *
 * <p>Zero runtime dependencies. Thread-safe — a single {@code Retryer} instance
 * can be shared across threads (config is immutable).
 */
package cc.ashclaw.common4j.retry;
