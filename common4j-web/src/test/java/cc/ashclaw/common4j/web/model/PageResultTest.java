package cc.ashclaw.common4j.web.model;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PageResult<T>")
class PageResultTest {

    @Nested
    @DisplayName("of(records, total, page, size)")
    class Of {

        @Test
        @DisplayName("should store all four fields")
        void shouldStoreAllFields() {
            var records = List.of("a", "b", "c");
            var pr = PageResult.of(records, 10L, 2L, 3L);

            assertEquals(records, pr.records());
            assertEquals(10L, pr.total());
            assertEquals(2L, pr.page());
            assertEquals(3L, pr.size());
        }

        @Test
        @DisplayName("records list should be unmodifiable")
        void recordsShouldBeUnmodifiable() {
            var pr = PageResult.of(List.of("a"), 1L, 1L, 20L);
            assertThrows(UnsupportedOperationException.class,
                    () -> pr.records().add("b"));
        }

        @Test
        @DisplayName("null records should be coerced to empty list")
        void nullRecordsShouldBeCoercedToEmpty() {
            var pr = PageResult.of(null, 0L, 1L, 20L);
            assertNotNull(pr.records());
            assertTrue(pr.records().isEmpty());
        }

        @Test
        @DisplayName("empty records list should be accepted")
        void shouldAcceptEmptyRecords() {
            var pr = PageResult.of(List.of(), 0L, 1L, 20L);
            assertTrue(pr.records().isEmpty());
            assertEquals(0L, pr.total());
        }

        @Test
        @DisplayName("caller's list mutation should not affect result")
        void callerListMutationShouldNotAffectResult() {
            var mutable = new java.util.ArrayList<>(List.of("a", "b"));
            var pr = PageResult.of(mutable, 2L, 1L, 10L);

            // 修改原列表，PageResult.records() 不应受影响
            mutable.add("c");
            assertEquals(2, pr.records().size());
        }
    }

    @Nested
    @DisplayName("from(IPage)")
    class From {

        @Test
        @DisplayName("should copy records and metadata from IPage")
        void shouldCopyRecordsAndMetadata() {
            IPage<String> ipage = new Page<>(1, 2);
            ipage.setRecords(List.of("a", "bb"));
            ipage.setTotal(7L);

            PageResult<String> pr = PageResult.from(ipage);

            assertEquals(List.of("a", "bb"), pr.records());
            assertEquals(7L, pr.total());
            assertEquals(1L, pr.page());
            assertEquals(2L, pr.size());
        }

        @Test
        @DisplayName("should handle empty IPage")
        void shouldHandleEmptyIPage() {
            IPage<String> ipage = new Page<>(3, 20);
            ipage.setRecords(List.of());
            ipage.setTotal(0L);

            PageResult<String> pr = PageResult.from(ipage);

            assertTrue(pr.records().isEmpty());
            assertEquals(0L, pr.total());
            assertEquals(3L, pr.page());
            assertEquals(20L, pr.size());
        }

        @Test
        @DisplayName("result records should be unmodifiable")
        void resultRecordsShouldBeUnmodifiable() {
            IPage<String> ipage = new Page<>(1, 10);
            ipage.setRecords(List.of("a"));
            ipage.setTotal(1L);

            PageResult<String> pr = PageResult.from(ipage);
            assertThrows(UnsupportedOperationException.class,
                    () -> pr.records().add("b"));
        }

        @Test
        @DisplayName("Entity→VO conversion via IPage.convert() should be preserved")
        void shouldPreserveConvertedRecords() {
            // 模拟 Service 层用 page.convert() 完成 Entity→VO 的转换
            record UserVo(Long id, String name) {}
            record User(Long id, String name) {
                UserVo toVo() { return new UserVo(id, name); }
            }

            IPage<User> ipage = new Page<>(1, 10);
            ipage.setRecords(List.of(new User(1L, "alice"), new User(2L, "bob")));
            ipage.setTotal(2L);

            // convert() 返回转换后的 IPage<UserVo>，捕获后再传入 from()
            PageResult<UserVo> pr = PageResult.from(ipage.convert(User::toVo));
            assertEquals(2, pr.records().size());
            assertEquals("alice", pr.records().get(0).name());
        }
    }

    @Nested
    @DisplayName("Record semantics")
    class RecordSemantics {

        @Test
        @DisplayName("should have proper equals and hashCode")
        void shouldHaveProperEqualsAndHashCode() {
            var a = PageResult.of(List.of("x"), 1L, 1L, 10L);
            var b = PageResult.of(List.of("x"), 1L, 1L, 10L);
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("toString should include all fields")
        void toStringShouldIncludeAllFields() {
            var pr = PageResult.of(List.of("x"), 5L, 2L, 10L);
            var str = pr.toString();
            assertTrue(str.contains("5"), str);
            assertTrue(str.contains("2"), str);
            assertTrue(str.contains("10"), str);
        }
    }
}
