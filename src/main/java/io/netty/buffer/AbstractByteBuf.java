package io.netty.buffer;

import java.nio.charset.Charset;

/**
 * ByteBuf 的抽象基类
 *
 * <p>提供读写索引管理和边界检查的通用实现。
 * 子类需要实现具体的字节读写方法。
 */
public abstract class AbstractByteBuf extends ByteBuf {

    protected int readerIndex;
    protected int writerIndex;
    private int markedReaderIndex;
    private int markedWriterIndex;
    private final int maxCapacity;

    protected AbstractByteBuf(int maxCapacity) {
        if (maxCapacity < 0) {
            throw new IllegalArgumentException("maxCapacity: " + maxCapacity + " (expected: >= 0)");
        }
        this.maxCapacity = maxCapacity;
    }

    @Override
    public int maxCapacity() {
        return maxCapacity;
    }

    @Override
    public int readerIndex() {
        return readerIndex;
    }

    @Override
    public ByteBuf readerIndex(int readerIndex) {
        if (readerIndex < 0 || readerIndex > writerIndex) {
            throw new IndexOutOfBoundsException(String.format(
                    "readerIndex: %d (expected: 0 <= readerIndex <= writerIndex(%d))",
                    readerIndex, writerIndex));
        }
        this.readerIndex = readerIndex;
        return this;
    }

    @Override
    public int writerIndex() {
        return writerIndex;
    }

    @Override
    public ByteBuf writerIndex(int writerIndex) {
        if (writerIndex < readerIndex || writerIndex > capacity()) {
            throw new IndexOutOfBoundsException(String.format(
                    "writerIndex: %d (expected: readerIndex(%d) <= writerIndex <= capacity(%d))",
                    writerIndex, readerIndex, capacity()));
        }
        this.writerIndex = writerIndex;
        return this;
    }

    @Override
    public ByteBuf setIndex(int readerIndex, int writerIndex) {
        if (readerIndex < 0 || readerIndex > writerIndex || writerIndex > capacity()) {
            throw new IndexOutOfBoundsException(String.format(
                    "readerIndex: %d, writerIndex: %d (expected: 0 <= readerIndex <= writerIndex <= capacity(%d))",
                    readerIndex, writerIndex, capacity()));
        }
        this.readerIndex = readerIndex;
        this.writerIndex = writerIndex;
        return this;
    }

    @Override
    public int readableBytes() {
        return writerIndex - readerIndex;
    }

    @Override
    public int writableBytes() {
        return capacity() - writerIndex;
    }

    @Override
    public boolean isReadable() {
        return writerIndex > readerIndex;
    }

    @Override
    public boolean isReadable(int size) {
        return writerIndex - readerIndex >= size;
    }

    @Override
    public boolean isWritable() {
        return capacity() > writerIndex;
    }

    @Override
    public boolean isWritable(int size) {
        return capacity() - writerIndex >= size;
    }

    @Override
    public ByteBuf clear() {
        readerIndex = writerIndex = 0;
        return this;
    }

    @Override
    public ByteBuf markReaderIndex() {
        markedReaderIndex = readerIndex;
        return this;
    }

    @Override
    public ByteBuf resetReaderIndex() {
        readerIndex(markedReaderIndex);
        return this;
    }

    @Override
    public ByteBuf markWriterIndex() {
        markedWriterIndex = writerIndex;
        return this;
    }

    @Override
    public ByteBuf resetWriterIndex() {
        writerIndex(markedWriterIndex);
        return this;
    }

    // =====================
    // 顺序读取实现
    // =====================

    @Override
    public byte readByte() {
        checkReadableBytes(1);
        int i = readerIndex;
        byte b = getByte(i);
        readerIndex = i + 1;
        return b;
    }

    @Override
    public short readShort() {
        checkReadableBytes(2);
        short v = getShort(readerIndex);
        readerIndex += 2;
        return v;
    }

    @Override
    public int readInt() {
        checkReadableBytes(4);
        int v = getInt(readerIndex);
        readerIndex += 4;
        return v;
    }

    @Override
    public long readLong() {
        checkReadableBytes(8);
        long v = getLong(readerIndex);
        readerIndex += 8;
        return v;
    }

    @Override
    public ByteBuf readBytes(byte[] dst) {
        return readBytes(dst, 0, dst.length);
    }

    @Override
    public ByteBuf readBytes(byte[] dst, int dstIndex, int length) {
        checkReadableBytes(length);
        getBytes(readerIndex, dst, dstIndex, length);
        readerIndex += length;
        return this;
    }

    @Override
    public ByteBuf skipBytes(int length) {
        checkReadableBytes(length);
        readerIndex += length;
        return this;
    }

    // =====================
    // 顺序写入实现
    // =====================

    @Override
    public ByteBuf writeByte(int value) {
        ensureWritable(1);
        setByte(writerIndex++, value);
        return this;
    }

    @Override
    public ByteBuf writeShort(int value) {
        ensureWritable(2);
        setShort(writerIndex, value);
        writerIndex += 2;
        return this;
    }

    @Override
    public ByteBuf writeInt(int value) {
        ensureWritable(4);
        setInt(writerIndex, value);
        writerIndex += 4;
        return this;
    }

    @Override
    public ByteBuf writeLong(long value) {
        ensureWritable(8);
        setLong(writerIndex, value);
        writerIndex += 8;
        return this;
    }

    @Override
    public ByteBuf writeBytes(byte[] src) {
        return writeBytes(src, 0, src.length);
    }

    @Override
    public ByteBuf writeBytes(byte[] src, int srcIndex, int length) {
        ensureWritable(length);
        setBytes(writerIndex, src, srcIndex, length);
        writerIndex += length;
        return this;
    }

    // =====================
    // 字符串方法
    // =====================

    @Override
    public String toString(Charset charset) {
        return toString(readerIndex, readableBytes(), charset);
    }

    @Override
    public String toString(int index, int length, Charset charset) {
        if (length == 0) {
            return "";
        }
        byte[] bytes = new byte[length];
        getBytes(index, bytes, 0, length);
        return new String(bytes, charset);
    }

    // =====================
    // 辅助方法
    // =====================

    protected void checkReadableBytes(int minimumReadableBytes) {
        if (readerIndex > writerIndex - minimumReadableBytes) {
            throw new IndexOutOfBoundsException(String.format(
                    "readerIndex(%d) + length(%d) exceeds writerIndex(%d)",
                    readerIndex, minimumReadableBytes, writerIndex));
        }
    }

    protected void ensureWritable(int minWritableBytes) {
        if (minWritableBytes < 0) {
            throw new IllegalArgumentException(String.format(
                    "minWritableBytes: %d (expected: >= 0)", minWritableBytes));
        }
        if (minWritableBytes <= writableBytes()) {
            return;
        }

        if (minWritableBytes > maxCapacity - writerIndex) {
            throw new IndexOutOfBoundsException(String.format(
                    "writerIndex(%d) + minWritableBytes(%d) exceeds maxCapacity(%d)",
                    writerIndex, minWritableBytes, maxCapacity));
        }

        // 扩容
        int newCapacity = calculateNewCapacity(writerIndex + minWritableBytes);
        capacity(newCapacity);
    }

    private int calculateNewCapacity(int minNewCapacity) {
        final int THRESHOLD = 4 * 1024 * 1024; // 4MB
        
        if (minNewCapacity == THRESHOLD) {
            return THRESHOLD;
        }

        if (minNewCapacity > THRESHOLD) {
            int newCapacity = (minNewCapacity / THRESHOLD) * THRESHOLD;
            if (newCapacity < minNewCapacity) {
                newCapacity += THRESHOLD;
            }
            return Math.min(newCapacity, maxCapacity);
        }

        // 小于阈值时，以64为起点翻倍增长
        int newCapacity = 64;
        while (newCapacity < minNewCapacity) {
            newCapacity <<= 1;
        }
        return Math.min(newCapacity, maxCapacity);
    }

    protected void checkIndex(int index, int length) {
        if (index < 0 || index > capacity() - length) {
            throw new IndexOutOfBoundsException(String.format(
                    "index: %d, length: %d (expected: index >= 0 && index + length <= capacity(%d))",
                    index, length, capacity()));
        }
    }

    @Override
    public ByteBuf discardReadBytes() {
        if (readerIndex == 0) {
            return this;
        }

        if (readerIndex != writerIndex) {
            // 将未读数据移动到开头
            setBytes(0, array(), arrayOffset() + readerIndex, writerIndex - readerIndex);
            writerIndex -= readerIndex;
            adjustMarkers(readerIndex);
            readerIndex = 0;
        } else {
            // 没有可读数据，直接清空
            adjustMarkers(readerIndex);
            writerIndex = readerIndex = 0;
        }
        return this;
    }

    private void adjustMarkers(int decrement) {
        markedReaderIndex = Math.max(markedReaderIndex - decrement, 0);
        markedWriterIndex = Math.max(markedWriterIndex - decrement, 0);
    }
}
