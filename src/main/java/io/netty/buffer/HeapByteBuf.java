package io.netty.buffer;

import java.nio.ByteBuffer;

/**
 * 堆内存 ByteBuf 实现
 *
 * <p>使用 Java 堆内存（byte[]）存储数据。
 * 适合需要频繁访问数据的场景，GC 可以自动管理内存。
 *
 * <p>特点：
 * <ul>
 *   <li>分配速度快</li>
 *   <li>数据存储在 JVM 堆中</li>
 *   <li>可直接访问底层数组</li>
 *   <li>网络 I/O 时需要额外拷贝到直接内存</li>
 * </ul>
 */
public class HeapByteBuf extends AbstractByteBuf {

    private byte[] array;
    private int refCnt = 1;

    /**
     * 创建指定初始容量的堆 ByteBuf
     *
     * @param initialCapacity 初始容量
     * @param maxCapacity     最大容量
     */
    public HeapByteBuf(int initialCapacity, int maxCapacity) {
        super(maxCapacity);
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("initialCapacity: " + initialCapacity + " (expected: >= 0)");
        }
        if (initialCapacity > maxCapacity) {
            throw new IllegalArgumentException(String.format(
                    "initialCapacity: %d (expected: <= maxCapacity(%d))",
                    initialCapacity, maxCapacity));
        }
        this.array = new byte[initialCapacity];
    }

    /**
     * 使用给定数组创建堆 ByteBuf
     *
     * @param initialArray 初始数组
     * @param maxCapacity  最大容量
     */
    public HeapByteBuf(byte[] initialArray, int maxCapacity) {
        super(maxCapacity);
        if (initialArray == null) {
            throw new NullPointerException("initialArray");
        }
        this.array = initialArray;
        this.writerIndex = initialArray.length;
    }

    @Override
    public int capacity() {
        return array.length;
    }

    @Override
    public ByteBuf capacity(int newCapacity) {
        if (newCapacity < 0 || newCapacity > maxCapacity()) {
            throw new IllegalArgumentException(String.format(
                    "newCapacity: %d (expected: 0 <= newCapacity <= maxCapacity(%d))",
                    newCapacity, maxCapacity()));
        }

        int oldCapacity = array.length;
        if (newCapacity == oldCapacity) {
            return this;
        }

        byte[] newArray = new byte[newCapacity];
        System.arraycopy(array, 0, newArray, 0, Math.min(oldCapacity, newCapacity));
        this.array = newArray;

        if (readerIndex > newCapacity) {
            readerIndex = newCapacity;
            writerIndex = newCapacity;
        } else if (writerIndex > newCapacity) {
            writerIndex = newCapacity;
        }

        return this;
    }

    @Override
    public boolean hasArray() {
        return true;
    }

    @Override
    public byte[] array() {
        return array;
    }

    @Override
    public int arrayOffset() {
        return 0;
    }

    // =====================
    // 随机访问实现
    // =====================

    @Override
    public byte getByte(int index) {
        checkIndex(index, 1);
        return array[index];
    }

    @Override
    public short getShort(int index) {
        checkIndex(index, 2);
        return (short) ((array[index] & 0xff) << 8 | (array[index + 1] & 0xff));
    }

    @Override
    public int getInt(int index) {
        checkIndex(index, 4);
        return (array[index] & 0xff) << 24 |
               (array[index + 1] & 0xff) << 16 |
               (array[index + 2] & 0xff) << 8 |
               (array[index + 3] & 0xff);
    }

    @Override
    public long getLong(int index) {
        checkIndex(index, 8);
        return ((long) array[index] & 0xff) << 56 |
               ((long) array[index + 1] & 0xff) << 48 |
               ((long) array[index + 2] & 0xff) << 40 |
               ((long) array[index + 3] & 0xff) << 32 |
               ((long) array[index + 4] & 0xff) << 24 |
               ((long) array[index + 5] & 0xff) << 16 |
               ((long) array[index + 6] & 0xff) << 8 |
               ((long) array[index + 7] & 0xff);
    }

    @Override
    public ByteBuf setByte(int index, int value) {
        checkIndex(index, 1);
        array[index] = (byte) value;
        return this;
    }

    @Override
    public ByteBuf setShort(int index, int value) {
        checkIndex(index, 2);
        array[index] = (byte) (value >>> 8);
        array[index + 1] = (byte) value;
        return this;
    }

    @Override
    public ByteBuf setInt(int index, int value) {
        checkIndex(index, 4);
        array[index] = (byte) (value >>> 24);
        array[index + 1] = (byte) (value >>> 16);
        array[index + 2] = (byte) (value >>> 8);
        array[index + 3] = (byte) value;
        return this;
    }

    @Override
    public ByteBuf setLong(int index, long value) {
        checkIndex(index, 8);
        array[index] = (byte) (value >>> 56);
        array[index + 1] = (byte) (value >>> 48);
        array[index + 2] = (byte) (value >>> 40);
        array[index + 3] = (byte) (value >>> 32);
        array[index + 4] = (byte) (value >>> 24);
        array[index + 5] = (byte) (value >>> 16);
        array[index + 6] = (byte) (value >>> 8);
        array[index + 7] = (byte) value;
        return this;
    }

    @Override
    public ByteBuf setBytes(int index, byte[] src) {
        return setBytes(index, src, 0, src.length);
    }

    @Override
    public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
        checkIndex(index, length);
        System.arraycopy(src, srcIndex, array, index, length);
        return this;
    }

    @Override
    public ByteBuf getBytes(int index, byte[] dst) {
        return getBytes(index, dst, 0, dst.length);
    }

    @Override
    public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
        checkIndex(index, length);
        System.arraycopy(array, index, dst, dstIndex, length);
        return this;
    }

    // =====================
    // NIO 转换
    // =====================

    @Override
    public ByteBuffer nioBuffer() {
        return nioBuffer(readerIndex, readableBytes());
    }

    @Override
    public ByteBuffer nioBuffer(int index, int length) {
        return ByteBuffer.wrap(array, index, length).slice();
    }

    // =====================
    // 引用计数
    // =====================

    @Override
    public int refCnt() {
        return refCnt;
    }

    @Override
    public ByteBuf retain() {
        return retain(1);
    }

    @Override
    public ByteBuf retain(int increment) {
        if (increment <= 0) {
            throw new IllegalArgumentException("increment: " + increment + " (expected: > 0)");
        }
        refCnt += increment;
        return this;
    }

    @Override
    public boolean release() {
        return release(1);
    }

    @Override
    public boolean release(int decrement) {
        if (decrement <= 0) {
            throw new IllegalArgumentException("decrement: " + decrement + " (expected: > 0)");
        }
        refCnt -= decrement;
        if (refCnt <= 0) {
            deallocate();
            return true;
        }
        return false;
    }

    /**
     * 释放资源
     */
    protected void deallocate() {
        // 堆内存由 GC 管理，这里只是标记
        array = null;
    }
}
