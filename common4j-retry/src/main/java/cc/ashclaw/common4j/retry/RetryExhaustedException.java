package cc.ashclaw.common4j.retry;

/**
 * Thrown when all retry attempts have been exhausted or when a non-retryable
 * exception occurs during execution.
 *
 * <p>Use {@link #getCause()} to access the last exception from the task.
 */
public class RetryExhaustedException extends Exception {

    private final int attempts;

    /**
     * @param message  human-readable description
     * @param attempts the number of attempts that were made
     * @param cause    the last exception from the task
     */
    public RetryExhaustedException(String message, int attempts, Throwable cause) {
        super(message, cause);
        this.attempts = attempts;
    }

    /** Returns the number of attempts that were made before giving up. */
    public int attempts() {
        return attempts;
    }
}
