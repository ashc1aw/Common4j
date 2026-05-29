package cc.ashclaw.common4j.web.model;

/**
 * Pagination request parameters, 1-based page numbering.
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * // GET /users?page=1&size=20
 * PageQuery pq = new PageQuery(1, 20);
 * int offset = pq.offset();  // 0 — use directly in LIMIT clause
 * }</pre>
 *
 * @param page current page number, starting at 1
 * @param size items per page, clamped to [{@value #MIN_SIZE}, {@value #MAX_SIZE}]
 */
public record PageQuery(int page, int size) {

    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_SIZE = 20;
    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 100;

    public PageQuery {
        if (page < 1) {
            page = DEFAULT_PAGE;
        }
        if (size < MIN_SIZE) {
            size = DEFAULT_SIZE;
        } else if (size > MAX_SIZE) {
            size = MAX_SIZE;
        }
    }

    /** Default query: page 1, size 20. */
    public PageQuery() {
        this(DEFAULT_PAGE, DEFAULT_SIZE);
    }

    /** Creates a query with the given page, default size. */
    public static PageQuery of(int page) {
        return new PageQuery(page, DEFAULT_SIZE);
    }

    /**
     * Returns the zero-based offset for SQL {@code LIMIT offset, size}.
     *
     * <pre>{@code
     * var pq = new PageQuery(3, 20);
     * pq.offset();  // 40
     * }</pre>
     */
    public int offset() {
        return (page - 1) * size;
    }
}
