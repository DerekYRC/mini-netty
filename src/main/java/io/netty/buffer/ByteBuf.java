package io.netty.buffer;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * 字节缓冲区抽象类
 *
 * <p>ByteBuf 是 Netty 的核心数据容器，提供比 JDK ByteBuffer 更强大的功能：
 * <ul>
 *   <li>读写索引分离（readerIndex 和 writerIndex）</li>
 *   <li>引用计数内存管理</li>
 *   <li>支持池化和非池化分配</li>
 *   <li>支持堆内和直接内存</li>
 * </ul>
 *
 * <p>索引布局：
 * <pre>
 * +-------------------+------------------+------------------+
 * | discardable bytes |  readable bytes  |  writable bytes  |
 * |                   |     (CONTENT)    |                  |
 * +-------------------+------------------+------------------+
 * |                   |                  |                  |
 * 0      <=      readerIndex   <=   writerIndex    <=    capacity
 * </pre>
 *
 * <p>使用示例：
 * <pre>{@code
 * ByteBuf buf = allocator.buffer(256);
 * buf.writeInt(42);
 * buf.writeBytes("Hello".getBytes());
 * int value = buf.readInt();
 * buf.release();
 * }</pre>
 *
 * @see ReferenceCounted
 */
public abstract class ByteBuf implements ReferenceCounted {

    /**
     * 获取缓冲区容量
     *
     * @return 容量（字节数）
     */
    public abstract int capacity();

    /**
     * 调整缓冲区容量
     *
     * @param newCapacity 新容量
     * @return this
     */
    public abstract ByteBuf capacity(int newCapacity);

    /**
     * 获取最大容量
     *
     * @return 最大容量
     */
    public abstract int maxCapacity();

    // =====================
    // 读写索引
    // =====================

    /**
     * 获取读索引
     *
     * @return 读索引位置
     */
    public abstract int readerIndex();

    /**
     * 设置读索引
     *
     * @param readerIndex 新的读索引
     * @return this
     */
    public abstract ByteBuf readerIndex(int readerIndex);

    /**
     * 获取写索引
     *
     * @return 写索引位置
     */
    public abstract int writerIndex();

    /**
     * 设置写索引
     *
     * @param writerIndex 新的写索引
     * @return this
     */
    public abstract ByteBuf writerIndex(int writerIndex);

    /**
     * 同时设置读写索引
     *
     * @param readerIndex 读索引
     * @param writerIndex 写索引
     * @return this
     */
    public abstract ByteBuf setIndex(int readerIndex, int writerIndex);

    /**
     * 获取可读字节数
     *
     * @return writerIndex - readerIndex
     */
    public abstract int readableBytes();

    /**
     * 获取可写字节数
     *
     * @return capacity - writerIndex
     */
    public abstract int writableBytes();

    /**
     * 是否有可读字节
     *
     * @return readableBytes() > 0
     */
    public abstract boolean isReadable();

    /**
     * 是否有至少指定数量的可读字节
     *
     * @param size 字节数
     * @return readableBytes() >= size
     */
    public abstract boolean isReadable(int size);

    /**
     * 是否可写
     *
     * @return writableBytes() > 0
     */
    public abstract boolean isWritable();

    /**
     * 是否有至少指定数量的可写空间
     *
     * @param size 字节数
     * @return writableBytes() >= size
     */
    public abstract boolean isWritable(int size);

    /**
     * 清除索引（readerIndex = writerIndex = 0）
     *
     * @return this
     */
    public abstract ByteBuf clear();

    // =====================
    // 标记和重置
    // =====================

    /**
     * 标记当前读索引
     *
     * @return this
     */
    public abstract ByteBuf markReaderIndex();

    /**
     * 重置读索引到标记位置
     *
     * @return this
     */
    public abstract ByteBuf resetReaderIndex();

    /**
     * 标记当前写索引
     *
     * @return this
     */
    public abstract ByteBuf markWriterIndex();

    /**
     * 重置写索引到标记位置
     *
     * @return this
     */
    public abstract ByteBuf resetWriterIndex();

    // =====================
    // 随机访问（不改变索引）
    // =====================

    /**
     * 获取指定位置的字节
     *
     * @param index 位置
     * @return 字节值
     */
    public abstract byte getByte(int index);

    /**
     * 获取指定位置的短整型（2字节，大端）
     *
     * @param index 位置
     * @return 短整型值
     */
    public abstract short getShort(int index);

    /**
     * 获取指定位置的整型（4字节，大端）
     *
     * @param index 位置
     * @return 整型值
     */
    public abstract int getInt(int index);

    /**
     * 获取指定位置的长整型（8字节，大端）
     *
     * @param index 位置
     * @return 长整型值
     */
    public abstract long getLong(int index);

    /**
     * 设置指定位置的字节
     *
     * @param index 位置
     * @param value 字节值
     * @return this
     */
    public abstract ByteBuf setByte(int index, int value);

    /**
     * 设置指定位置的短整型
     *
     * @param index 位置
     * @param value 短整型值
     * @return this
     */
    public abstract ByteBuf setShort(int index, int value);

    /**
     * 设置指定位置的整型
     *
     * @param index 位置
     * @param value 整型值
     * @return this
     */
    public abstract ByteBuf setInt(int index, int value);

    /**
     * 设置指定位置的长整型
     *
     * @param index 位置
     * @param value 长整型值
     * @return this
     */
    public abstract ByteBuf setLong(int index, long value);

    /**
     * 设置指定位置的字节数组
     *
     * @param index 起始位置
     * @param src   源字节数组
     * @return this
     */
    public abstract ByteBuf setBytes(int index, byte[] src);

    /**
     * 设置指定位置的字节数组（指定范围）
     *
     * @param index  起始位置
     * @param src    源字节数组
     * @param srcIndex 源数组起始位置
     * @param length 长度
     * @return this
     */
    public abstract ByteBuf setBytes(int index, byte[] src, int srcIndex, int length);

    /**
     * 获取指定位置的字节到目标数组
     *
     * @param index 起始位置
     * @param dst   目标字节数组
     * @return this
     */
    public abstract ByteBuf getBytes(int index, byte[] dst);

    /**
     * 获取指定位置的字节到目标数组（指定范围）
     *
     * @param index    起始位置
     * @param dst      目标字节数组
     * @param dstIndex 目标数组起始位置
     * @param length   长度
     * @return this
     */
    public abstract ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length);

    // =====================
    // 顺序读取（改变 readerIndex）
    // =====================

    /**
     * 读取一个字节并增加 readerIndex
     *
     * @return 字节值
     */
    public abstract byte readByte();

    /**
     * 读取一个短整型并增加 readerIndex
     *
     * @return 短整型值
     */
    public abstract short readShort();

    /**
     * 读取一个整型并增加 readerIndex
     *
     * @return 整型值
     */
    public abstract int readInt();

    /**
     * 读取一个长整型并增加 readerIndex
     *
     * @return 长整型值
     */
    public abstract long readLong();

    /**
     * 读取字节到目标数组
     *
     * @param dst 目标数组
     * @return this
     */
    public abstract ByteBuf readBytes(byte[] dst);

    /**
     * 读取指定长度的字节到目标数组
     *
     * @param dst      目标数组
     * @param dstIndex 目标数组起始位置
     * @param length   长度
     * @return this
     */
    public abstract ByteBuf readBytes(byte[] dst, int dstIndex, int length);

    /**
     * 跳过指定字节数
     *
     * @param length 跳过的字节数
     * @return this
     */
    public abstract ByteBuf skipBytes(int length);

    // =====================
    // 顺序写入（改变 writerIndex）
    // =====================

    /**
     * 写入一个字节并增加 writerIndex
     *
     * @param value 字节值
     * @return this
     */
    public abstract ByteBuf writeByte(int value);

    /**
     * 写入一个短整型并增加 writerIndex
     *
     * @param value 短整型值
     * @return this
     */
    public abstract ByteBuf writeShort(int value);

    /**
     * 写入一个整型并增加 writerIndex
     *
     * @param value 整型值
     * @return this
     */
    public abstract ByteBuf writeInt(int value);

    /**
     * 写入一个长整型并增加 writerIndex
     *
     * @param value 长整型值
     * @return this
     */
    public abstract ByteBuf writeLong(long value);

    /**
     * 写入字节数组
     *
     * @param src 源数组
     * @return this
     */
    public abstract ByteBuf writeBytes(byte[] src);

    /**
     * 写入字节数组（指定范围）
     *
     * @param src      源数组
     * @param srcIndex 源数组起始位置
     * @param length   长度
     * @return this
     */
    public abstract ByteBuf writeBytes(byte[] src, int srcIndex, int length);

    // =====================
    // 转换方法
    // =====================

    /**
     * 转换为 NIO ByteBuffer
     *
     * @return ByteBuffer 视图
     */
    public abstract ByteBuffer nioBuffer();

    /**
     * 转换为 NIO ByteBuffer（指定范围）
     *
     * @param index  起始位置
     * @param length 长度
     * @return ByteBuffer 视图
     */
    public abstract ByteBuffer nioBuffer(int index, int length);

    /**
     * 是否有底层字节数组
     *
     * @return 如果是堆缓冲区返回 true
     */
    public abstract boolean hasArray();

    /**
     * 获取底层字节数组
     *
     * @return 字节数组
     */
    public abstract byte[] array();

    /**
     * 获取底层数组的偏移量
     *
     * @return 偏移量
     */
    public abstract int arrayOffset();

    /**
     * 丢弃已读字节，压缩缓冲区
     *
     * @return this
     */
    public abstract ByteBuf discardReadBytes();

    // =====================
    // 字符串方法
    // =====================

    /**
     * 将可读字节转换为字符串
     *
     * @param charset 字符集
     * @return 字符串
     */
    public abstract String toString(Charset charset);

    /**
     * 将指定范围转换为字符串
     *
     * @param index   起始位置
     * @param length  长度
     * @param charset 字符集
     * @return 字符串
     */
    public abstract String toString(int index, int length, Charset charset);

    // =====================
    // 引用计数
    // =====================

    @Override
    public abstract int refCnt();

    @Override
    public abstract ByteBuf retain();

    @Override
    public abstract ByteBuf retain(int increment);

    @Override
    public abstract boolean release();

    @Override
    public abstract boolean release(int decrement);
}
