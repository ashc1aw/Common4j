package cc.ashclaw.common4j.core.id;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ID generation utilities.
 *
 * <p>Provides a configurable default {@link Snowflake} instance for convenience.
 * The worker ID is resolved in order:
 * <ol>
 *   <li>Explicit call to {@link #initialize(long)}</li>
 *   <li>System property {@code common4j.snowflake.workerId}</li>
 *   <li>Environment variable {@code COMMON4J_SNOWFLAKE_WORKER_ID}</li>
 *   <li>Default: {@code 1}</li>
 * </ol>
 *
 * <p>Multi-node deployments must ensure each node uses a unique worker ID.
 */
public final class IdUtils {

    private static final AtomicReference<Snowflake> DEFAULT = new AtomicReference<>();

    private IdUtils() {
    }

    /**
     * Initializes the default Snowflake instance with the given worker ID.
     *
     * <p>Must be called at most once, before any ID generation. Subsequent calls
     * have no effect.
     *
     * @param workerId worker ID, 0–1023
     * @throws IllegalArgumentException if workerId is out of range
     */
    public static void initialize(long workerId) {
        DEFAULT.compareAndSet(null, new Snowflake(workerId));
    }

    /**
     * Returns the default Snowflake instance, creating it lazily if needed.
     */
    public static Snowflake getSnowflake() {
        Snowflake sf = DEFAULT.get();
        if (sf == null) {
            long workerId = resolveWorkerId();
            DEFAULT.compareAndSet(null, new Snowflake(workerId));
            sf = DEFAULT.get();
        }
        return sf;
    }

    /**
     * Generates the next unique snowflake ID using the default instance.
     *
     * @return a 64-bit unique ID
     */
    public static long nextId() {
        return getSnowflake().nextId();
    }

    /**
     * Generates the next unique snowflake ID as a decimal string.
     *
     * @return a string representation of the next unique ID
     */
    public static String nextString() {
        return getSnowflake().nextString();
    }

    // ==================== UUID ====================

    /**
     * Generates a random UUID (type 4) as a 36-character string with dashes.
     *
     * @return e.g. {@code "550e8400-e29b-41d4-a716-446655440000"}
     */
    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generates a random UUID (type 4) as a 32-character string without dashes.
     *
     * @return e.g. {@code "550e8400e29b41d4a716446655440000"}
     */
    public static String randomUUIDSimple() {
        UUID uuid = UUID.randomUUID();
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        return String.format("%016x%016x", msb, lsb);
    }

    /**
     * Parses a UUID string, accepting both standard (with dashes) and simple
     * (without dashes) formats.
     *
     * @param text the UUID string
     * @return the parsed UUID
     * @throws IllegalArgumentException if the string is not a valid UUID
     */
    public static UUID uuidFromString(String text) {
        if (text.length() == 36) {
            return UUID.fromString(text);
        }
        if (text.length() == 32) {
            long msb = Long.parseUnsignedLong(text.substring(0, 16), 16);
            long lsb = Long.parseUnsignedLong(text.substring(16), 16);
            return new UUID(msb, lsb);
        }
        throw new IllegalArgumentException("Invalid UUID string (expected 32 or 36 chars): " + text);
    }

    private static long resolveWorkerId() {
        String prop = System.getProperty("common4j.snowflake.workerId");
        if (prop != null && !prop.isEmpty()) {
            return Long.parseLong(prop);
        }
        String env = System.getenv("COMMON4J_SNOWFLAKE_WORKER_ID");
        if (env != null && !env.isEmpty()) {
            return Long.parseLong(env);
        }
        return 1;
    }
}
