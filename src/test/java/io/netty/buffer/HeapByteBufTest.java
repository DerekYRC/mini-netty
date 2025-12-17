package io.netty.buffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * HeapByteBuf 测试
 */
@DisplayName("HeapByteBuf 测试")
class HeapByteBufTest {

    private HeapByteBuf buf;

    @BeforeEach
    void setUp() {
        buf = new HeapByteBuf(256, 1024);
    }

    @Nested
    @DisplayName("基础属性测试")
    class BasicPropertyTests {

        @Test
        @DisplayName("初始状态应正确")
        void initialStateShouldBeCorrect() {
            assertThat(buf.capacity()).isEqualTo(256);
            assertThat(buf.maxCapacity()).isEqualTo(1024);
            assertThat(buf.readerIndex()).isZero();
            assertThat(buf.writerIndex()).isZero();
            assertThat(buf.readableBytes()).isZero();
            assertThat(buf.writableBytes()).isEqualTo(256);
        }

        @Test
        @DisplayName("hasArray 应返回 true")
        void hasArrayShouldReturnTrue() {
            assertThat(buf.hasArray()).isTrue();
            assertThat(buf.array()).isNotNull();
            assertThat(buf.arrayOffset()).isZero();
        }

        @Test
        @DisplayName("清除应重置索引")
        void clearShouldResetIndexes() {
            buf.writeInt(42);
            buf.readByte();
            
            buf.clear();
            
            assertThat(buf.readerIndex()).isZero();
            assertThat(buf.writerIndex()).isZero();
        }
    }

    @Nested
    @DisplayName("读写索引测试")
    class IndexTests {

        @Test
        @DisplayName("写入应增加 writerIndex")
        void writeShouldIncreaseWriterIndex() {
            buf.writeByte(1);
            assertThat(buf.writerIndex()).isEqualTo(1);
            
            buf.writeInt(42);
            assertThat(buf.writerIndex()).isEqualTo(5);
            
            buf.writeLong(100L);
            assertThat(buf.writerIndex()).isEqualTo(13);
        }

        @Test
        @DisplayName("读取应增加 readerIndex")
        void readShouldIncreaseReaderIndex() {
            buf.writeByte(1);
            buf.writeInt(42);
            buf.writeLong(100L);
            
            buf.readByte();
            assertThat(buf.readerIndex()).isEqualTo(1);
            
            buf.readInt();
            assertThat(buf.readerIndex()).isEqualTo(5);
            
            buf.readLong();
            assertThat(buf.readerIndex()).isEqualTo(13);
        }

        @Test
        @DisplayName("标记和重置应正确工作")
        void markAndResetShouldWork() {
            buf.writeBytes("Hello World".getBytes(StandardCharsets.UTF_8));
            
            buf.markReaderIndex();
            buf.readBytes(new byte[5]);
            assertThat(buf.readerIndex()).isEqualTo(5);
            
            buf.resetReaderIndex();
            assertThat(buf.readerIndex()).isZero();
        }

        @Test
        @DisplayName("设置索引应进行边界检查")
        void setIndexShouldCheckBounds() {
            buf.writeInt(42);
            
            assertThatThrownBy(() -> buf.readerIndex(-1))
                    .isInstanceOf(IndexOutOfBoundsException.class);
            
            assertThatThrownBy(() -> buf.readerIndex(10))
                    .isInstanceOf(IndexOutOfBoundsException.class);
            
            assertThatThrownBy(() -> buf.writerIndex(500))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }
    }

    @Nested
    @DisplayName("顺序读写测试")
    class SequentialReadWriteTests {

        @Test
        @DisplayName("writeByte 和 readByte 应正确工作")
        void writeAndReadByteShouldWork() {
            buf.writeByte(127);
            buf.writeByte(-1);
            
            assertThat(buf.readByte()).isEqualTo((byte) 127);
            assertThat(buf.readByte()).isEqualTo((byte) -1);
        }

        @Test
        @DisplayName("writeShort 和 readShort 应正确工作")
        void writeAndReadShortShouldWork() {
            buf.writeShort(32767);
            buf.writeShort(-1);
            
            assertThat(buf.readShort()).isEqualTo((short) 32767);
            assertThat(buf.readShort()).isEqualTo((short) -1);
        }

        @Test
        @DisplayName("writeInt 和 readInt 应正确工作")
        void writeAndReadIntShouldWork() {
            buf.writeInt(Integer.MAX_VALUE);
            buf.writeInt(Integer.MIN_VALUE);
            buf.writeInt(42);
            
            assertThat(buf.readInt()).isEqualTo(Integer.MAX_VALUE);
            assertThat(buf.readInt()).isEqualTo(Integer.MIN_VALUE);
            assertThat(buf.readInt()).isEqualTo(42);
        }

        @Test
        @DisplayName("writeLong 和 readLong 应正确工作")
        void writeAndReadLongShouldWork() {
            buf.writeLong(Long.MAX_VALUE);
            buf.writeLong(Long.MIN_VALUE);
            
            assertThat(buf.readLong()).isEqualTo(Long.MAX_VALUE);
            assertThat(buf.readLong()).isEqualTo(Long.MIN_VALUE);
        }

        @Test
        @DisplayName("writeBytes 和 readBytes 应正确工作")
        void writeAndReadBytesShouldWork() {
            byte[] data = "Hello, ByteBuf!".getBytes(StandardCharsets.UTF_8);
            buf.writeBytes(data);
            
            byte[] result = new byte[data.length];
            buf.readBytes(result);
            
            assertThat(result).isEqualTo(data);
        }

        @Test
        @DisplayName("skipBytes 应跳过指定字节数")
        void skipBytesShouldSkipBytes() {
            buf.writeBytes("HelloWorld".getBytes(StandardCharsets.UTF_8));
            
            buf.skipBytes(5);
            
            byte[] result = new byte[5];
            buf.readBytes(result);
            assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("World");
        }
    }

    @Nested
    @DisplayName("随机访问测试")
    class RandomAccessTests {

        @Test
        @DisplayName("getByte 和 setByte 不应改变索引")
        void getSetByteShouldNotChangeIndex() {
            buf.writerIndex(10);
            
            buf.setByte(5, 42);
            assertThat(buf.getByte(5)).isEqualTo((byte) 42);
            
            assertThat(buf.readerIndex()).isZero();
            assertThat(buf.writerIndex()).isEqualTo(10);
        }

        @Test
        @DisplayName("getInt 和 setInt 应正确工作")
        void getSetIntShouldWork() {
            buf.writerIndex(20);
            
            buf.setInt(4, 0x12345678);
            assertThat(buf.getInt(4)).isEqualTo(0x12345678);
        }

        @Test
        @DisplayName("getLong 和 setLong 应正确工作")
        void getSetLongShouldWork() {
            buf.writerIndex(20);
            
            buf.setLong(8, 0x123456789ABCDEF0L);
            assertThat(buf.getLong(8)).isEqualTo(0x123456789ABCDEF0L);
        }

        @Test
        @DisplayName("getBytes 和 setBytes 应正确工作")
        void getSetBytesShouldWork() {
            buf.writerIndex(20);
            byte[] data = {1, 2, 3, 4, 5};
            
            buf.setBytes(5, data);
            
            byte[] result = new byte[5];
            buf.getBytes(5, result);
            
            assertThat(result).isEqualTo(data);
        }
    }

    @Nested
    @DisplayName("容量测试")
    class CapacityTests {

        @Test
        @DisplayName("容量应可以增加")
        void capacityShouldBeIncreasable() {
            assertThat(buf.capacity()).isEqualTo(256);
            
            buf.capacity(512);
            
            assertThat(buf.capacity()).isEqualTo(512);
        }

        @Test
        @DisplayName("容量应可以减少")
        void capacityShouldBeDecreasable() {
            buf.capacity(128);
            
            assertThat(buf.capacity()).isEqualTo(128);
        }

        @Test
        @DisplayName("扩容应保留数据")
        void capacityIncreaseShouldPreserveData() {
            buf.writeBytes("Hello".getBytes(StandardCharsets.UTF_8));
            
            buf.capacity(512);
            
            byte[] result = new byte[5];
            buf.readBytes(result);
            assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("Hello");
        }

        @Test
        @DisplayName("自动扩容应在写入时触发")
        void autoExpandShouldTriggerOnWrite() {
            HeapByteBuf smallBuf = new HeapByteBuf(8, 256);
            
            // 写入超过初始容量的数据
            for (int i = 0; i < 20; i++) {
                smallBuf.writeInt(i);
            }
            
            assertThat(smallBuf.capacity()).isGreaterThan(8);
            assertThat(smallBuf.writerIndex()).isEqualTo(80);
        }
    }

    @Nested
    @DisplayName("NIO 转换测试")
    class NioConversionTests {

        @Test
        @DisplayName("nioBuffer 应返回正确的 ByteBuffer")
        void nioBufferShouldReturnCorrectByteBuffer() {
            buf.writeBytes("Hello".getBytes(StandardCharsets.UTF_8));
            
            ByteBuffer nioBuffer = buf.nioBuffer();
            
            assertThat(nioBuffer.remaining()).isEqualTo(5);
            
            byte[] result = new byte[5];
            nioBuffer.get(result);
            assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("Hello");
        }

        @Test
        @DisplayName("nioBuffer 带参数应返回指定范围")
        void nioBufferWithRangeShouldWork() {
            buf.writeBytes("HelloWorld".getBytes(StandardCharsets.UTF_8));
            
            ByteBuffer nioBuffer = buf.nioBuffer(5, 5);
            
            byte[] result = new byte[5];
            nioBuffer.get(result);
            assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("World");
        }
    }

    @Nested
    @DisplayName("字符串转换测试")
    class StringConversionTests {

        @Test
        @DisplayName("toString(Charset) 应返回可读内容")
        void toStringWithCharsetShouldReturnReadableContent() {
            buf.writeBytes("Hello, World!".getBytes(StandardCharsets.UTF_8));
            
            String result = buf.toString(StandardCharsets.UTF_8);
            
            assertThat(result).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("toString 带范围参数应返回指定内容")
        void toStringWithRangeShouldWork() {
            buf.writeBytes("Hello, World!".getBytes(StandardCharsets.UTF_8));
            
            String result = buf.toString(0, 5, StandardCharsets.UTF_8);
            
            assertThat(result).isEqualTo("Hello");
        }
    }

    @Nested
    @DisplayName("引用计数测试")
    class ReferenceCountTests {

        @Test
        @DisplayName("初始引用计数应为 1")
        void initialRefCntShouldBeOne() {
            assertThat(buf.refCnt()).isEqualTo(1);
        }

        @Test
        @DisplayName("retain 应增加引用计数")
        void retainShouldIncreaseRefCnt() {
            buf.retain();
            assertThat(buf.refCnt()).isEqualTo(2);
            
            buf.retain(3);
            assertThat(buf.refCnt()).isEqualTo(5);
        }

        @Test
        @DisplayName("release 应减少引用计数")
        void releaseShouldDecreaseRefCnt() {
            buf.retain();
            buf.retain();
            assertThat(buf.refCnt()).isEqualTo(3);
            
            boolean released = buf.release();
            assertThat(released).isFalse();
            assertThat(buf.refCnt()).isEqualTo(2);
        }

        @Test
        @DisplayName("引用计数为 0 时应释放资源")
        void shouldDeallocateWhenRefCntReachesZero() {
            boolean released = buf.release();
            
            assertThat(released).isTrue();
            assertThat(buf.refCnt()).isLessThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("discardReadBytes 测试")
    class DiscardReadBytesTests {

        @Test
        @DisplayName("discardReadBytes 应压缩缓冲区")
        void discardReadBytesShouldCompactBuffer() {
            buf.writeBytes("HelloWorld".getBytes(StandardCharsets.UTF_8));
            buf.skipBytes(5);
            
            assertThat(buf.readerIndex()).isEqualTo(5);
            
            buf.discardReadBytes();
            
            assertThat(buf.readerIndex()).isZero();
            assertThat(buf.readableBytes()).isEqualTo(5);
            
            byte[] result = new byte[5];
            buf.readBytes(result);
            assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("World");
        }
    }

    @Nested
    @DisplayName("验收场景测试")
    class AcceptanceScenarioTests {

        @Test
        @DisplayName("典型的网络消息处理场景")
        void typicalNetworkMessageScenario() {
            // 模拟写入网络消息：长度 + 消息体
            String message = "Hello, Netty!";
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            
            buf.writeInt(messageBytes.length);
            buf.writeBytes(messageBytes);
            
            // 模拟读取
            int length = buf.readInt();
            byte[] readBytes = new byte[length];
            buf.readBytes(readBytes);
            
            assertThat(new String(readBytes, StandardCharsets.UTF_8)).isEqualTo(message);
            assertThat(buf.readableBytes()).isZero();
        }

        @Test
        @DisplayName("大端字节序验证")
        void bigEndianByteOrderVerification() {
            buf.writeInt(0x12345678);
            
            assertThat(buf.getByte(0)).isEqualTo((byte) 0x12);
            assertThat(buf.getByte(1)).isEqualTo((byte) 0x34);
            assertThat(buf.getByte(2)).isEqualTo((byte) 0x56);
            assertThat(buf.getByte(3)).isEqualTo((byte) 0x78);
        }
    }
}
