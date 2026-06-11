package cc.ashclaw.common4j.retry;

/**
 * Thrown when all retry attempts have been exhausted or when a non-retryable
 * exception occurs during execution.
 *
 * <p>Use {@link #getCause()} to access the last exception from the task,
 * and {@link #attempts()} to see how many attempts were made.
 */
public class RetryExhaustedException extends Exception {

    /** The number of attempts that were made before giving up. */
    private final int attempts;

    /**
     * Constructs a new instance with the given details.
     *
     * @param message  human-readable description
     * @param attempts the number of attempts that were made
     * @param cause    the last exception from the task
     */
    public RetryExhaustedException(String message, int attempts, Throwable cause) {
        super(message, cause);
        this.attempts = attempts;
    }

    /** Returns the number of attempts that were made before giving up.
     * @return the attempt count (>= 1) */
    public int attempts() {
        return attempts;
    }
}
