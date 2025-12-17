package io.netty.buffer;

/**
 * 非池化 ByteBuf 分配器
 *
 * <p>每次调用都创建新的 ByteBuf 实例，不复用已释放的缓冲区。
 * 适合小型应用或测试场景，实现简单但可能有更高的 GC 压力。
 *
 * <p>特点：
 * <ul>
 *   <li>实现简单，易于调试</li>
 *   <li>每次分配都是新对象</li>
 *   <li>释放后由 GC 回收内存</li>
 * </ul>
 *
 * <p>使用方式：
 * <pre>{@code
 * ByteBuf buf = UnpooledByteBufAllocator.DEFAULT.buffer(256);
 * try {
 *     // 使用 buffer
 * } finally {
 *     buf.release();
 * }
 * }</pre>
 *
 * @see ByteBufAllocator
 */
public class UnpooledByteBufAllocator implements ByteBufAllocator {

    /**
     * 默认实例（优先使用堆内存）
     */
    public static final UnpooledByteBufAllocator DEFAULT = new UnpooledByteBufAllocator(false);

    /**
     * 默认初始容量
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 256;

    /**
     * 默认最大容量
     */
    private static final int DEFAULT_MAX_CAPACITY = Integer.MAX_VALUE;

    private final boolean preferDirect;

    /**
     * 创建非池化分配器
     *
     * @param preferDirect 是否优先使用直接内存
     */
    public UnpooledByteBufAllocator(boolean preferDirect) {
        this.preferDirect = preferDirect;
    }

    @Override
    public ByteBuf buffer() {
        return buffer(DEFAULT_INITIAL_CAPACITY);
    }

    @Override
    public ByteBuf buffer(int initialCapacity) {
        return buffer(initialCapacity, DEFAULT_MAX_CAPACITY);
    }

    @Override
    public ByteBuf buffer(int initialCapacity, int maxCapacity) {
        if (preferDirect) {
            return directBuffer(initialCapacity, maxCapacity);
        }
        return heapBuffer(initialCapacity, maxCapacity);
    }

    @Override
    public ByteBuf heapBuffer() {
        return heapBuffer(DEFAULT_INITIAL_CAPACITY);
    }

    @Override
    public ByteBuf heapBuffer(int initialCapacity) {
        return heapBuffer(initialCapacity, DEFAULT_MAX_CAPACITY);
    }

    @Override
    public ByteBuf heapBuffer(int initialCapacity, int maxCapacity) {
        return new HeapByteBuf(initialCapacity, maxCapacity);
    }

    @Override
    public ByteBuf directBuffer() {
        return directBuffer(DEFAULT_INITIAL_CAPACITY);
    }

    @Override
    public ByteBuf directBuffer(int initialCapacity) {
        return directBuffer(initialCapacity, DEFAULT_MAX_CAPACITY);
    }

    @Override
    public ByteBuf directBuffer(int initialCapacity, int maxCapacity) {
        // 简化实现：暂时使用堆内存代替直接内存
        // 完整实现应返回 DirectByteBuf
        return new HeapByteBuf(initialCapacity, maxCapacity);
    }

    @Override
    public boolean isDirectBufferPooled() {
        return false;
    }
}
