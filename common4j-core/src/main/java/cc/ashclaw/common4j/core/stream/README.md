# stream

Stream utilities bridging gaps the JDK's rich Stream API still leaves open. Most streaming needs are covered by the JDK itself — `Gatherers`, `takeWhile`/`dropWhile`, `mapMulti`, `Stream.toList()`. Use StreamUtils for the few remaining gaps.

## Classes

### `StreamUtils`

Iterator bridges and stream zipping.

```java
// Iterator → Stream — instead of the verbose Spliterators boilerplate
var stream = StreamUtils.stream(legacyLib.iterator());

// Enumeration → Stream — bridge legacy APIs (JDBC, servlets, etc.)
var stream = StreamUtils.stream(request.getHeaderNames());

// Zip two streams
StreamUtils.zip(ids, names, (id, name) -> id + "=" + name).toList();
```

| Method | Returns | Notes |
|---|---|---|
| `stream(iterator)` | `Stream<T>` | Sequential, non-parallel |
| `stream(enumeration)` | `Stream<T>` | Wraps as Iterator first, then streams |
| `zip(first, second, zipper)` | `Stream<R>` | Stops when the shorter stream ends |

## Design notes

- All returned streams are sequential and non-parallel — parallelism is a caller decision.
- `zip` is sized when both input spliterators report a size; otherwise unknown-sized.
- `zip` is lazy — it consumes elements from both inputs on demand, not eagerly.
- `StreamUtils` constructor throws — pure utility class.
