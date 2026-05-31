# collections

Collection utilities bridging gaps in the JDK.

## Classes

### `CollectionUtils`

Batch partitioning and set operations.

```java
// Partition a list into batches of 100
for (var batch : CollectionUtils.partition(items, 100)) {
    service.sendBatch(batch);
}

// Set operations — two or more collections
Set<String> common  = CollectionUtils.intersection(setA, setB);
Set<String> common  = CollectionUtils.intersection(setA, setB, setC);
Set<String> merged  = CollectionUtils.union(setA, setB);
Set<String> merged  = CollectionUtils.union(setA, setB, setC);
Set<String> onlyInA = CollectionUtils.difference(setA, setB);
```

| Method | Returns | Notes |
|---|---|---|
| `partition(list, size)` | `List<List<T>>` | Each sublist is an unmodifiable copy; last sublist may be smaller |
| `intersection(first, second, rest...)` | `Set<T>` | Elements present in all collections (2+ required) |
| `union(first, second, rest...)` | `Set<T>` | Elements present in any collection (2+ required) |
| `difference(source, remove)` | `Set<T>` | Elements in `source` not in `remove` |

### `TreeBuilder`

Builds a tree (or forest) from flat nodes that reference their parent by id.

```java
record Category(int id, Integer parentId, String name) {}

var flat = List.of(
    new Category(1, null, "Electronics"),
    new Category(2, 1,    "Phones"),
    new Category(3, 1,    "Laptops"),
    new Category(4, null, "Books"));

List<TreeBuilder.TreeNode<Category>> forest =
    TreeBuilder.build(flat, Category::id, Category::parentId);
```

| Method | Returns | Notes |
|---|---|---|
| `build(nodes, idGetter, parentIdGetter)` | `List<TreeNode<T>>` | `null` parentId = root |
| `build(nodes, idGetter, parentIdGetter, rootParentId)` | `List<TreeNode<T>>` | Custom root sentinel (e.g. 0, "") |

`TreeNode` is a record: `TreeNode<T>(T data, List<TreeNode<T>> children)` — both fields non-null, children is an unmodifiable snapshot.

## Design notes

- All returned collections are unmodifiable — safe to share across threads.
- `intersection` and `union` accept 2 or more collections via varargs — no nested calls needed for 3+ sets.
- `CollectionUtils` and `TreeBuilder` constructors throw — pure utility classes, not meant to be instantiated.
- `partition` copies elements per batch via `List.copyOf`, so the original list can be mutated or garbage-collected independently.
- `TreeBuilder.build` preserves insertion order for both roots and children. Orphan nodes (parent id not found in the collection) are promoted to roots.
- Input must not contain cycles; building a cyclic graph will result in a `StackOverflowError`.
