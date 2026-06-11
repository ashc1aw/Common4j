package cc.ashclaw.common4j.web.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * A page of records with metadata — the standard paginated response shape.
 *
 * Usage example:
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

    /**
     * Validates that records is non-null.
     *
     * @throws NullPointerException if records is null
     */
    public PageResult {
        Objects.requireNonNull(records, "records must not be null");
    }

    /**
     * Creates a page result from a list, total count, and the original query.
     *
     * @param <T>     the record type
     * @param records the page contents
     * @param total   total item count
     * @param query   the originating page query
     * @return a new {@link PageResult} with unmodifiable records list
     */
    public static <T> PageResult<T> of(List<T> records, long total, PageQuery query) {
        return new PageResult<>(
                Collections.unmodifiableList(records),
                total,
                query.page(),
                query.size());
    }

    // ==================== Derived properties ====================

    /**
     * Returns the total number of pages.
     *
     * @return total pages (0 if no records)
     */
    public int totalPages() {
        if (total == 0) {
            return 0;
        }
        return (int) ((total + size - 1) / size);
    }

    /**
     * Returns whether there is a next page.
     *
     * @return true if the current page is not the last page
     */
    public boolean hasNext() {
        return page < totalPages();
    }

    /**
     * Returns whether there is a previous page.
     *
     * @return true if the current page is not the first page
     */
    public boolean hasPrev() {
        return page > 1;
    }

    /**
     * Returns whether this page contains no records.
     *
     * @return true if the records list is empty
     */
    public boolean isEmpty() {
        return records.isEmpty();
    }

    // ==================== Transformation ====================

    /**
     * Transforms the records in this page, keeping metadata intact.
     *
     * @param <U>    the transformed record type
     * @param mapper the transformation function applied to each record
     * @return a new {@link PageResult} with mapped records and unchanged metadata
     */
    @SuppressWarnings("unchecked")
    public <U> PageResult<U> map(Function<? super T, ? extends U> mapper) {
        List<U> mapped = (List<U>) records.stream().map(mapper).toList();
        return new PageResult<>(mapped, total, page, size);
    }
}
