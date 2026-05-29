package cc.ashclaw.common4j.core.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Practical collection utilities bridging gaps in the JDK.
 *
 * <h3>Partition — batch processing</h3>
 * <pre>{@code
 * var items = List.of("a", "b", "c", "d", "e");
 *
 * for (var batch : CollectionUtils.partition(items, 2)) {
 *     service.sendBatch(batch);   // [a,b], [c,d], [e]
 * }
 * }</pre>
 *
 * <h3>Set operations</h3>
 * <pre>{@code
 * var a = Set.of(1, 2, 3);
 * var b = Set.of(2, 3, 4);
 * var c = Set.of(2, 5);
 *
 * CollectionUtils.intersection(a, b);     // {2, 3}
 * CollectionUtils.intersection(a, b, c);  // {2}
 * CollectionUtils.union(a, b);            // {1, 2, 3, 4}
 * CollectionUtils.union(a, b, c);         // {1, 2, 3, 4, 5}
 * CollectionUtils.difference(a, b);       // {1}
 * }</pre>
 */
public final class CollectionUtils {

    private CollectionUtils() {
        throw new UnsupportedOperationException("utility class");
    }

    // ==================== Partition ====================

    /**
     * Splits a list into consecutive sublists of the given size.
     * Each sublist is an unmodifiable copy of the original.
     * The last sublist may be smaller if the size is not evenly divisible.
     *
     * @param list the list to partition
     * @param size the maximum size of each partition, must be positive
     * @return a list of partitions, each an unmodifiable {@link List}
     * @throws NullPointerException     if {@code list} is null
     * @throws IllegalArgumentException if {@code size} is not positive
     */
    public static <T> List<List<T>> partition(List<T> list, int size) {
        Objects.requireNonNull(list, "list must not be null");
        if (size <= 0) {
            throw new IllegalArgumentException("size must be positive: " + size);
        }
        int n = list.size();
        var result = new ArrayList<List<T>>((n + size - 1) / size);
        for (int i = 0; i < n; i += size) {
            int end = Math.min(i + size, n);
            result.add(List.copyOf(list.subList(i, end)));
        }
        return Collections.unmodifiableList(result);
    }

    // ==================== Set operations ====================

    /**
     * Returns the elements present in all given collections as an unmodifiable set.
     * Requires at least 2 collections.
     */
    @SafeVarargs
    public static <T> Set<T> intersection(Collection<T> first, Collection<T> second, Collection<T>... rest) {
        Objects.requireNonNull(first, "first must not be null");
        Objects.requireNonNull(second, "second must not be null");
        var set = new HashSet<>(first);
        set.retainAll(second);
        for (var c : rest) {
            Objects.requireNonNull(c);
            set.retainAll(c);
        }
        return Collections.unmodifiableSet(set);
    }

    /**
     * Returns the elements present in any of the given collections as an unmodifiable set.
     * Requires at least 2 collections.
     */
    @SafeVarargs
    public static <T> Set<T> union(Collection<T> first, Collection<T> second, Collection<T>... rest) {
        Objects.requireNonNull(first, "first must not be null");
        Objects.requireNonNull(second, "second must not be null");
        var set = new HashSet<>(first);
        set.addAll(second);
        for (var c : rest) {
            Objects.requireNonNull(c);
            set.addAll(c);
        }
        return Collections.unmodifiableSet(set);
    }

    /**
     * Returns the elements in {@code source} that are not in {@code remove} as an unmodifiable set.
     */
    public static <T> Set<T> difference(Collection<T> source, Collection<T> remove) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(remove, "remove must not be null");
        var set = new HashSet<>(source);
        set.removeAll(remove);
        return Collections.unmodifiableSet(set);
    }
}
