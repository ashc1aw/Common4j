package cc.ashclaw.common4j.web.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * A page of records with metadata — the standard paginated response shape.
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * PageQuery pq = new PageQuery(1, 20);
 * List<User> users = userService.listByPage(pq);
 * long total = userService.count();
 * return PageResult.of(users, total, pq);
 *
 * // Transform records
 * PageResult<UserDto> dtos = result.map(User::toDto);
 * }</pre>
 *
 * @param <T>     the record type
 * @param records the items in this page
 * @param total   total number of items across all pages
 * @param page    current page number (1-based)
 * @param size    page size
 */
public record PageResult<T>(List<T> records, long total, int page, int size) {

    public PageResult {
        Objects.requireNonNull(records, "records must not be null");
    }

    /**
     * Creates a page result from a list, total count, and the original query.
     *
     * @param records the page contents
     * @param total   total item count
     * @param query   the originating page query
     */
    public static <T> PageResult<T> of(List<T> records, long total, PageQuery query) {
        return new PageResult<>(
                Collections.unmodifiableList(records),
                total,
                query.page(),
                query.size());
    }

    // ==================== Derived properties ====================

    /** Total number of pages. */
    public int totalPages() {
        if (total == 0) {
            return 0;
        }
        return (int) ((total + size - 1) / size);
    }

    /** Whether there is a next page. */
    public boolean hasNext() {
        return page < totalPages();
    }

    /** Whether there is a previous page. */
    public boolean hasPrev() {
        return page > 1;
    }

    /** Whether this page is empty. */
    public boolean isEmpty() {
        return records.isEmpty();
    }

    // ==================== Transformation ====================

    /**
     * Transforms the records in this page, keeping metadata intact.
     */
    @SuppressWarnings("unchecked")
    public <U> PageResult<U> map(Function<? super T, ? extends U> mapper) {
        List<U> mapped = (List<U>) records.stream().map(mapper).toList();
        return new PageResult<>(mapped, total, page, size);
    }
}
