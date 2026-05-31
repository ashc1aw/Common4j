package cc.ashclaw.common4j.core.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Builds a tree (or forest) from a flat collection of nodes that reference
 * their parent by id.
 *
 * <pre>{@code
 * record Category(int id, Integer parentId, String name) {}
 *
 * var flat = List.of(
 *     new Category(1, null, "Electronics"),
 *     new Category(2, 1,    "Phones"),
 *     new Category(3, 1,    "Laptops"),
 *     new Category(4, null, "Books"));
 *
 * List<TreeBuilder.TreeNode<Category>> forest =
 *     TreeBuilder.build(flat, Category::id, Category::parentId);
 * // forest = [Electronics → [Phones, Laptops], Books]
 * }</pre>
 *
 * <h3>Custom root marker</h3>
 * When {@code null} is not the root sentinel (e.g. 0L or ""):
 * <pre>{@code
 * TreeBuilder.build(nodes, Menu::id, Menu::parentId, 0L);
 * }</pre>
 *
 * <h3>Edge cases</h3>
 * Nodes whose parent id does not match any id in the collection (orphans)
 * are treated as roots. Input must not contain cycles; the result of building
 * a cyclic graph is undefined (likely a {@link StackOverflowError}).
 */
public final class TreeBuilder {

    private TreeBuilder() {
        throw new UnsupportedOperationException("utility class");
    }

    /**
     * A node in the resulting tree, holding the original data and its children.
     * Both the data and the children list are never null.
     */
    public record TreeNode<T>(T data, List<TreeNode<T>> children) {
        public TreeNode {
            Objects.requireNonNull(data);
            children = List.copyOf(children);
        }
    }

    /**
     * Builds a forest from a flat collection.
     * Nodes whose {@code parentIdGetter} returns {@code null} become roots.
     *
     * @param nodes          the flat collection of nodes
     * @param idGetter       extracts the unique identifier from a node
     * @param parentIdGetter extracts the parent identifier from a node (null = root)
     * @return unmodifiable list of root tree nodes, preserving insertion order
     * @throws NullPointerException     if any argument is null
     * @throws IllegalArgumentException if an id is null or duplicate ids are found
     */
    public static <T, K> List<TreeNode<T>> build(
            Collection<T> nodes,
            Function<? super T, ? extends K> idGetter,
            Function<? super T, ? extends K> parentIdGetter) {
        return build(nodes, idGetter, parentIdGetter, null);
    }

    /**
     * Builds a forest from a flat collection, treating {@code rootParentId} as
     * the sentinel value that marks a root node.
     *
     * @param nodes          the flat collection of nodes
     * @param idGetter       extracts the unique identifier from a node
     * @param parentIdGetter extracts the parent identifier from a node
     * @param rootParentId   the parent-id value that marks a root node (may be null)
     * @return unmodifiable list of root tree nodes, preserving insertion order
     * @throws NullPointerException     if {@code nodes}, {@code idGetter}, or {@code parentIdGetter} is null
     * @throws IllegalArgumentException if an id is null or duplicate ids are found
     */
    public static <T, K> List<TreeNode<T>> build(
            Collection<T> nodes,
            Function<? super T, ? extends K> idGetter,
            Function<? super T, ? extends K> parentIdGetter,
            K rootParentId) {

        Objects.requireNonNull(nodes, "nodes must not be null");
        Objects.requireNonNull(idGetter, "idGetter must not be null");
        Objects.requireNonNull(parentIdGetter, "parentIdGetter must not be null");

        if (nodes.isEmpty()) return List.of();

        // First pass: create internal nodes, index by id
        var idToNode = new LinkedHashMap<K, InternalNode<T>>();
        for (var node : nodes) {
            K id = idGetter.apply(node);
            if (id == null) throw new IllegalArgumentException("id must not be null: " + node);
            if (idToNode.containsKey(id))
                throw new IllegalArgumentException("duplicate id: " + id);
            idToNode.put(id, new InternalNode<>(node));
        }

        // Second pass: wire parent-child relationships
        var roots = new ArrayList<InternalNode<T>>();
        for (var node : nodes) {
            K id = idGetter.apply(node);
            K parentId = parentIdGetter.apply(node);
            var self = idToNode.get(id);

            if (Objects.equals(parentId, rootParentId)) {
                roots.add(self);
            } else {
                var parent = idToNode.get(parentId);
                if (parent == null) {
                    roots.add(self);  // orphan → promote to root
                } else {
                    parent.children.add(self);
                }
            }
        }

        return roots.stream()
                .map(InternalNode::freeze)
                .toList();
    }

    // -- internal mutable holder, frozen after wiring is complete --

    private static class InternalNode<T> {
        final T data;
        final List<InternalNode<T>> children = new ArrayList<>();

        InternalNode(T data) { this.data = data; }

        TreeNode<T> freeze() {
            return new TreeNode<>(
                    data,
                    children.stream().map(InternalNode::freeze).toList());
        }
    }
}
