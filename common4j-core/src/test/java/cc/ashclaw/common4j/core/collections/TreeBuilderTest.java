package cc.ashclaw.common4j.core.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TreeBuilder")
class TreeBuilderTest {

    record Cat(int id, Integer parentId, String name) {}

    // helper for testing null-id validation (Cat record can't have null id)
    static class NullIdCat {
        final int id;
        final Integer parentId;
        final String name;

        NullIdCat(int id, Integer parentId, String name) {
            this.id = id;
            this.parentId = parentId;
            this.name = name;
        }

        Integer getId() { return id == -1 ? null : id; }
        Integer getParentId() { return parentId; }
    }

    @Nested
    @DisplayName("build() with null root sentinel")
    class BuildNullRoot {

        @Test
        @DisplayName("should build a simple two-level tree")
        void simpleTwoLevelTree() {
            var nodes = List.of(
                    new Cat(1, null, "Root"),
                    new Cat(2, 1, "Child1"),
                    new Cat(3, 1, "Child2"));

            var roots = TreeBuilder.build(nodes, Cat::id, Cat::parentId);

            assertEquals(1, roots.size());
            assertEquals("Root", roots.getFirst().data().name());
            assertEquals(2, roots.getFirst().children().size());
            assertEquals(Set.of("Child1", "Child2"),
                    Set.of(roots.getFirst().children().get(0).data().name(),
                           roots.getFirst().children().get(1).data().name()));
        }

        @Test
        @DisplayName("should build multiple roots (forest)")
        void multipleRoots() {
            var nodes = List.of(
                    new Cat(1, null, "A"),
                    new Cat(2, null, "B"),
                    new Cat(3, 1, "A1"));

            var roots = TreeBuilder.build(nodes, Cat::id, Cat::parentId);

            assertEquals(2, roots.size());
            assertEquals("A", roots.get(0).data().name());
            assertEquals("B", roots.get(1).data().name());
            assertEquals(1, roots.get(0).children().size());
            assertTrue(roots.get(1).children().isEmpty());
        }

        @Test
        @DisplayName("should handle deep nesting (3+ levels)")
        void deepNesting() {
            var nodes = List.of(
                    new Cat(1, null, "L1"),
                    new Cat(2, 1, "L2"),
                    new Cat(3, 2, "L3"));

            var roots = TreeBuilder.build(nodes, Cat::id, Cat::parentId);

            assertEquals(1, roots.size());
            var l2 = roots.getFirst().children().getFirst();
            assertEquals("L2", l2.data().name());
            var l3 = l2.children().getFirst();
            assertEquals("L3", l3.data().name());
            assertTrue(l3.children().isEmpty());
        }

        @Test
        @DisplayName("empty collection should return empty list")
        void emptyCollection() {
            var roots = TreeBuilder.build(List.of(), Cat::id, Cat::parentId);
            assertTrue(roots.isEmpty());
        }

        @Test
        @DisplayName("single root node should have no children")
        void singleRootNode() {
            var nodes = List.of(new Cat(1, null, "Only"));
            var roots = TreeBuilder.build(nodes, Cat::id, Cat::parentId);

            assertEquals(1, roots.size());
            assertEquals("Only", roots.getFirst().data().name());
            assertTrue(roots.getFirst().children().isEmpty());
        }

        @Test
        @DisplayName("orphan nodes (parent not found) should become roots")
        void orphanNodesBecomeRoots() {
            var nodes = List.of(
                    new Cat(1, 999, "Orphan"),
                    new Cat(2, null, "RealRoot"));

            var roots = TreeBuilder.build(nodes, Cat::id, Cat::parentId);

            assertEquals(2, roots.size());
        }

        @Test
        @DisplayName("result should be unmodifiable")
        void resultShouldBeUnmodifiable() {
            var nodes = List.of(new Cat(1, null, "Root"));
            var roots = TreeBuilder.build(nodes, Cat::id, Cat::parentId);
            var extra = new TreeBuilder.TreeNode<>(new Cat(99, null, "X"), List.of());
            assertThrows(UnsupportedOperationException.class, () -> roots.add(extra));
        }

        @Test
        @DisplayName("children should be unmodifiable")
        void childrenShouldBeUnmodifiable() {
            var nodes = List.of(
                    new Cat(1, null, "Root"),
                    new Cat(2, 1, "Child"));
            var roots = TreeBuilder.build(nodes, Cat::id, Cat::parentId);
            var children = roots.getFirst().children();
            var extra = new TreeBuilder.TreeNode<>(new Cat(99, null, "X"), List.of());
            assertThrows(UnsupportedOperationException.class, () -> children.add(extra));
        }

        @Test
        @DisplayName("should preserve insertion order for roots")
        void preservesInsertionOrder() {
            var nodes = List.of(
                    new Cat(3, null, "Third"),
                    new Cat(1, null, "First"),
                    new Cat(2, null, "Second"));

            var roots = TreeBuilder.build(nodes, Cat::id, Cat::parentId);

            assertEquals(List.of("Third", "First", "Second"),
                    roots.stream().map(r -> r.data().name()).toList());
        }

        @Test
        @DisplayName("should preserve insertion order for children")
        void preservesChildrenOrder() {
            var nodes = List.of(
                    new Cat(1, null, "Root"),
                    new Cat(3, 1, "Third"),
                    new Cat(2, 1, "Second"),
                    new Cat(4, 1, "Fourth"));

            var roots = TreeBuilder.build(nodes, Cat::id, Cat::parentId);
            var children = roots.getFirst().children();
            assertEquals(List.of("Third", "Second", "Fourth"),
                    children.stream().map(c -> c.data().name()).toList());
        }
    }

    @Nested
    @DisplayName("build() with explicit root sentinel")
    class BuildExplicitRoot {

        @Test
        @DisplayName("should treat 0 as root marker")
        void zeroAsRootMarker() {
            var nodes = List.of(
                    new Cat(1, 0, "Root"),
                    new Cat(2, 1, "Child"));

            var roots = TreeBuilder.build(nodes, Cat::id, Cat::parentId, 0);

            assertEquals(1, roots.size());
            assertEquals("Root", roots.getFirst().data().name());
            assertEquals(1, roots.getFirst().children().size());
        }

        @Test
        @DisplayName("should distinguish root marker from real parent")
        void distinguishesRootFromParent() {
            record Menu(int id, int parentId, String label) {}
            var menus = List.of(
                    new Menu(1, 0, "Dashboard"),
                    new Menu(2, 0, "Settings"),
                    new Menu(3, 1, "Overview"));

            var roots = TreeBuilder.build(menus, Menu::id, Menu::parentId, 0);

            assertEquals(2, roots.size());
            assertEquals(1, roots.get(0).children().size());
            assertEquals("Overview", roots.get(0).children().getFirst().data().label());
            assertTrue(roots.get(1).children().isEmpty());
        }
    }

    @Nested
    @DisplayName("null / invalid input")
    class InvalidInput {

        @Test
        @DisplayName("should throw NPE when nodes is null")
        void npeWhenNodesIsNull() {
            assertThrows(NullPointerException.class,
                    () -> TreeBuilder.build(null, Cat::id, Cat::parentId));
        }

        @Test
        @DisplayName("should throw NPE when idGetter is null")
        void npeWhenIdGetterIsNull() {
            assertThrows(NullPointerException.class,
                    () -> TreeBuilder.build(List.of(), null, Cat::parentId));
        }

        @Test
        @DisplayName("should throw NPE when parentIdGetter is null")
        void npeWhenParentIdGetterIsNull() {
            assertThrows(NullPointerException.class,
                    () -> TreeBuilder.build(List.of(), Cat::id, null));
        }

        @Test
        @DisplayName("should throw IAE when id is null")
        void iaeWhenIdIsNull() {
            var nodes = List.of(
                    new NullIdCat(-1, null, "Bad"),
                    new NullIdCat(1, null, "Good"));
            assertThrows(IllegalArgumentException.class,
                    () -> TreeBuilder.build(nodes, NullIdCat::getId, NullIdCat::getParentId));
        }

        @Test
        @DisplayName("should throw IAE when duplicate ids exist")
        void iaeWhenDuplicateIds() {
            var nodes = List.of(
                    new Cat(1, null, "First"),
                    new Cat(1, null, "Duplicate"));
            assertThrows(IllegalArgumentException.class,
                    () -> TreeBuilder.build(nodes, Cat::id, Cat::parentId));
        }
    }

    @Nested
    @DisplayName("TreeNode")
    class TreeNodeTests {

        @Test
        @DisplayName("should throw NPE when data is null")
        void npeWhenDataIsNull() {
            assertThrows(NullPointerException.class,
                    () -> new TreeBuilder.TreeNode<>(null, List.of()));
        }

        @Test
        @DisplayName("should accept empty children list")
        void acceptsEmptyChildren() {
            var node = new TreeBuilder.TreeNode<>("root", List.of());
            assertEquals("root", node.data());
            assertTrue(node.children().isEmpty());
        }

        @Test
        @DisplayName("children should be an independent copy")
        void childrenIsIndependentCopy() {
            var mutable = new java.util.ArrayList<TreeBuilder.TreeNode<String>>();
            mutable.add(new TreeBuilder.TreeNode<>("child", List.of()));
            var node = new TreeBuilder.TreeNode<>("root", mutable);
            mutable.clear();
            assertEquals(1, node.children().size(),
                    "children() should be a snapshot");
        }
    }

    @Nested
    @DisplayName("constructor")
    class Constructor {

        @Test
        @DisplayName("should throw UnsupportedOperationException")
        void shouldThrow() throws Exception {
            var ctor = TreeBuilder.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            var e = assertThrows(UnsupportedOperationException.class, () -> {
                try {
                    ctor.newInstance();
                } catch (java.lang.reflect.InvocationTargetException ite) {
                    throw ite.getCause();
                }
            });
            assertNotNull(e);
        }
    }
}
