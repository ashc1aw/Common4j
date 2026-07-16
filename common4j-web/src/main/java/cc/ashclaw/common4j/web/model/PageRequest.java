package cc.ashclaw.common4j.web.model;

/**
 * 通用分页请求。封装 1-based 页码、每页大小以及可选的查询条件对象，
 * 与 MyBatis-Plus 等持久层框架无直接耦合，可单独使用。
 *
 * <p>字段约束（在紧凑构造器中静默修正，非抛异常）：</p>
 * <ul>
 *   <li>{@code page <= 0} 会被强制修正为 {@code 1}</li>
 *   <li>{@code size <= 0} 会被强制修正为 {@code 10}</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * // 仅分页，无条件
 * PageRequest<?> req = PageRequest.of(2, 20);
 *
 * // 分页 + 业务条件对象
 * PageRequest<UserQuery> req = PageRequest.of(2, 20, userQuery);
 *
 * // 配合 MyBatis-Plus
 * IPage<User> page = userMapper.selectPage(
 *         Page.of(req.page(), req.size()),
 *         Wrappers.lambdaQuery());
 * }</pre>
 *
 * @param <T>        查询条件对象的类型，无条件时为 {@link Void} 或任意引用类型
 * @param page       1-based 页码，非法值会被修正为 1
 * @param size       每页大小，非法值会被修正为 10
 * @param condition  业务查询条件，可为 {@code null}
 */
public record PageRequest<T>(long page, long size, T condition) {

    /**
     * 紧凑构造器：对非法分页参数进行静默修正，避免 Controller 层重复校验。
     *
     * @param page      待写入的页码
     * @param size      待写入的每页大小
     * @param condition 待写入的查询条件
     */
    public PageRequest {
        if (page <= 0) page = 1;
        if (size <= 0) size = 10;
    }

    /**
     * 创建无业务条件的分页请求（条件为 {@code null}）。
     *
     * @param page 1-based 页码
     * @param size 每页大小
     * @return 条件为 {@code null} 的 {@link PageRequest}
     */
    public static PageRequest<?> of(long page, long size) {
        return new PageRequest<>(page, size, null);
    }

    /**
     * 创建带业务条件的分页请求。
     *
     * @param <T>       条件对象类型
     * @param page      1-based 页码
     * @param size      每页大小
     * @param condition 业务查询条件，可为 {@code null}
     * @return 携带指定条件的 {@link PageRequest}
     */
    public static <T> PageRequest<T> of(long page, long size, T condition) {
        return new PageRequest<>(page, size, condition);
    }
}
