package cc.ashclaw.common4j.web.model;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.Collections;
import java.util.List;

/**
 * 分页响应结果。Controller 直接返回（不包 {@code R&lt;&gt;}），由全局序列化器渲染为
 * 形如 {@code { records: [...], total: 100, page: 1, size: 10 }} 的 JSON。
 *
 * <p>设计要点：</p>
 * <ul>
 *   <li>不可变 record —— 线程安全，天然具备 {@code equals}/{@code hashCode}/{@code toString}</li>
 *   <li>{@link #records} 在紧凑构造器中通过 {@link List#copyOf} 复制，调用方无法再修改原始列表</li>
 *   <li>{@code null} 记录列表会被规范化为 {@link Collections#emptyList()}，避免空指针</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * // 手工构造
 * PageResult<UserDto> pr = PageResult.of(dtos, totalCount, 1, 20);
 *
 * // 从 MyBatis-Plus IPage 转换（Entity→VO 转换在 Service 层通过 page.convert() 完成）
 * PageResult<UserDto> pr = PageResult.from(userMapper.selectPage(page, wrapper));
 * }</pre>
 *
 * @param <T>     列表元素类型
 * @param records 当前页数据
 * @param total   总记录数
 * @param page    当前页码（1-based）
 * @param size    每页大小
 */
public record PageResult<T>(List<T> records, long total, long page, long size) {

    /**
     * 紧凑构造器：规范化 {@code records}。
     * <ul>
     *   <li>{@code null} 会被替换为不可变空列表</li>
     *   <li>非 {@code null} 会被 {@link List#copyOf} 复制为不可变列表</li>
     * </ul>
     *
     * @param records 待写入的记录列表
     */
    public PageResult {
        records = records == null ? Collections.emptyList() : List.copyOf(records);
    }

    /**
     * 手工构造一个分页结果。
     *
     * @param <T>     元素类型
     * @param records 当前页数据，可为 {@code null}（会被规范化为空列表）
     * @param total   总记录数
     * @param page    当前页码（1-based）
     * @param size    每页大小
     * @return 不可变的 {@link PageResult}
     */
    public static <T> PageResult<T> of(List<T> records, long total, long page, long size) {
        return new PageResult<>(records, total, page, size);
    }

    /**
     * 从 MyBatis-Plus 的 {@link IPage} 提取分页元数据。
     * Entity→VO 的转换由 {@code IPage.convert()} 在 Service 层完成，
     * 本方法仅复制 records、total、current、size 四个字段。
     *
     * @param <T>   元素类型（通常为 VO）
     * @param ipage MyBatis-Plus 分页对象
     * @return 与 {@code ipage} 元数据一致的 {@link PageResult}
     */
    public static <T> PageResult<T> from(IPage<T> ipage) {
        return new PageResult<>(ipage.getRecords(), ipage.getTotal(),
                ipage.getCurrent(), ipage.getSize());
    }
}
