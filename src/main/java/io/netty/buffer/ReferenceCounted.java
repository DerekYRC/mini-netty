package io.netty.buffer;

/**
 * 引用计数接口
 *
 * <p>用于管理资源的生命周期。当引用计数降为 0 时，资源可以被释放。
 * 这是一种手动内存管理机制，用于避免 GC 开销。
 *
 * <p>使用规则：
 * <ul>
 *   <li>创建时引用计数为 1</li>
 *   <li>调用 {@link #retain()} 增加引用计数</li>
 *   <li>调用 {@link #release()} 减少引用计数</li>
 *   <li>引用计数为 0 时释放资源</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * ByteBuf buf = allocator.buffer();
 * try {
 *     // 使用 buf
 * } finally {
 *     buf.release();
 * }
 * }</pre>
 *
 * @see ByteBuf
 */
public interface ReferenceCounted {

    /**
     * 获取当前引用计数
     *
     * @return 当前引用计数值
     */
    int refCnt();

    /**
     * 增加引用计数 1
     *
     * @return this
     */
    ReferenceCounted retain();

    /**
     * 增加指定的引用计数
     *
     * @param increment 增加的数量
     * @return this
     */
    ReferenceCounted retain(int increment);

    /**
     * 减少引用计数 1，当引用计数为 0 时释放资源
     *
     * @return 如果引用计数变为 0 且资源被释放，返回 true
     */
    boolean release();

    /**
     * 减少指定的引用计数
     *
     * @param decrement 减少的数量
     * @return 如果引用计数变为 0 且资源被释放，返回 true
     */
    boolean release(int decrement);
}
