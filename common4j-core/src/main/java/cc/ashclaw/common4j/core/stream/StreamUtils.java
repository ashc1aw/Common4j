package cc.ashclaw.common4j.core.stream;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Stream utilities bridging gaps the JDK's rich Stream API still leaves open.
 *
 * <h3>Iterator / Enumeration to Stream</h3>
 * <pre>{@code
 * var stream = StreamUtils.stream(legacyLib.iterator());
 * }</pre>
 * Instead of the verbose {@code StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, 0), false)}.
 *
 * <h3>Zip two streams</h3>
 * <pre>{@code
 * var ids   = Stream.of(1, 2, 3);
 * var names = Stream.of("a", "b", "c");
 *
 * StreamUtils.zip(ids, names, (id, name) -> id + "=" + name)
 *     .toList();  // ["1=a", "2=b", "3=c"]
 * }</pre>
 */
public final class StreamUtils {

    private StreamUtils() {
        throw new UnsupportedOperationException("utility class");
    }

    /**
     * Wraps an {@link Iterator} as a sequential, non-parallel {@link Stream}.
     *
     * @param iterator the source iterator, must not be null
     * @return a new sequential stream consuming the iterator
     */
    public static <T> Stream<T> stream(Iterator<? extends T> iterator) {
        Objects.requireNonNull(iterator, "iterator must not be null");
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false);
    }

    /**
     * Wraps an {@link Enumeration} as a sequential, non-parallel {@link Stream}.
     * Useful for bridging legacy APIs (JDBC, servlets, etc.).
     *
     * @param enumeration the source enumeration, must not be null
     * @return a new sequential stream consuming the enumeration
     */
    @SuppressWarnings("JdkObsolete")
    public static <T> Stream<T> stream(Enumeration<? extends T> enumeration) {
        Objects.requireNonNull(enumeration, "enumeration must not be null");
        var iter = new Iterator<T>() {
            @Override public boolean hasNext() { return enumeration.hasMoreElements(); }
            @Override public T next()          { return enumeration.nextElement(); }
        };
        return stream(iter);
    }

    /**
     * Zips two streams together using a combiner function.
     * The resulting stream stops when the shorter stream ends.
     *
     * <pre>{@code
     * zip(Stream.of(1, 2), Stream.of("a", "b", "c"), (a, b) -> a + b)
     *     .toList();  // ["1a", "2b"]
     * }</pre>
     *
     * @param first   the first stream, must not be null
     * @param second  the second stream, must not be null
     * @param zipper  combines elements from both streams
     * @return a new sequential stream of zipped elements
     */
    public static <A, B, R> Stream<R> zip(
            Stream<A> first,
            Stream<B> second,
            BiFunction<? super A, ? super B, ? extends R> zipper) {

        Objects.requireNonNull(first, "first must not be null");
        Objects.requireNonNull(second, "second must not be null");
        Objects.requireNonNull(zipper, "zipper must not be null");

        var splitA = first.spliterator();
        var splitB = second.spliterator();

        long estA = splitA.estimateSize();
        long estB = splitB.estimateSize();
        long size = (estA != Long.MAX_VALUE && estB != Long.MAX_VALUE)
                ? Math.min(estA, estB) : -1;

        var itA = Spliterators.iterator(splitA);
        var itB = Spliterators.iterator(splitB);

        var iter = new Iterator<R>() {
            @Override public boolean hasNext() { return itA.hasNext() && itB.hasNext(); }
            @Override public R next()          { return zipper.apply(itA.next(), itB.next()); }
        };

        var split = (size >= 0)
                ? Spliterators.spliterator(iter, size, Spliterator.ORDERED | Spliterator.SIZED)
                : Spliterators.spliteratorUnknownSize(iter, Spliterator.ORDERED);

        return StreamSupport.stream(split, false);
    }
}
