package io.netty.buffer;

/**
 * ByteBuf 分配器接口
 *
 * <p>负责创建 ByteBuf 实例。不同的分配器实现可以提供：
 * <ul>
 *   <li>非池化分配（每次创建新实例）</li>
 *   <li>池化分配（复用已释放的 ByteBuf）</li>
 *   <li>堆内存或直接内存分配</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * ByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
 * ByteBuf buf = allocator.buffer(256);
 * try {
 *     buf.writeBytes(data);
 * } finally {
 *     buf.release();
 * }
 * }</pre>
 *
 * @see UnpooledByteBufAllocator
 */
public interface ByteBufAllocator {

    /**
     * 分配一个默认大小的 ByteBuf
     *
     * @return 新分配的 ByteBuf
     */
    ByteBuf buffer();

    /**
     * 分配指定初始容量的 ByteBuf
     *
     * @param initialCapacity 初始容量
     * @return 新分配的 ByteBuf
     */
    ByteBuf buffer(int initialCapacity);

    /**
     * 分配指定初始容量和最大容量的 ByteBuf
     *
     * @param initialCapacity 初始容量
     * @param maxCapacity     最大容量
     * @return 新分配的 ByteBuf
     */
    ByteBuf buffer(int initialCapacity, int maxCapacity);

    /**
     * 分配一个堆内存 ByteBuf
     *
     * @return 新分配的堆内存 ByteBuf
     */
    ByteBuf heapBuffer();

    /**
     * 分配指定初始容量的堆内存 ByteBuf
     *
     * @param initialCapacity 初始容量
     * @return 新分配的堆内存 ByteBuf
     */
    ByteBuf heapBuffer(int initialCapacity);

    /**
     * 分配指定初始容量和最大容量的堆内存 ByteBuf
     *
     * @param initialCapacity 初始容量
     * @param maxCapacity     最大容量
     * @return 新分配的堆内存 ByteBuf
     */
    ByteBuf heapBuffer(int initialCapacity, int maxCapacity);

    /**
     * 分配一个直接内存 ByteBuf
     *
     * @return 新分配的直接内存 ByteBuf
     */
    ByteBuf directBuffer();

    /**
     * 分配指定初始容量的直接内存 ByteBuf
     *
     * @param initialCapacity 初始容量
     * @return 新分配的直接内存 ByteBuf
     */
    ByteBuf directBuffer(int initialCapacity);

    /**
     * 分配指定初始容量和最大容量的直接内存 ByteBuf
     *
     * @param initialCapacity 初始容量
     * @param maxCapacity     最大容量
     * @return 新分配的直接内存 ByteBuf
     */
    ByteBuf directBuffer(int initialCapacity, int maxCapacity);

    /**
     * 是否使用直接内存作为默认
     *
     * @return 如果 buffer() 方法返回直接内存 ByteBuf 则返回 true
     */
    boolean isDirectBufferPooled();
}
