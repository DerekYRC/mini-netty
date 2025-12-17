package io.netty.handler.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.HeapByteBuf;
import io.netty.channel.*;
import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * LengthFieldBasedFrameDecoder 测试
 */
@DisplayName("LengthFieldBasedFrameDecoder Tests")
class LengthFieldBasedFrameDecoderTest {

    /**
     * 记录接收帧的 Handler
     */
    private static class FrameRecordingHandler extends ChannelInboundHandlerAdapter {
        final List<ByteBuf> frames = new ArrayList<>();
        final List<String> frameStrings = new ArrayList<>();
        Throwable lastException;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof ByteBuf) {
                ByteBuf frame = (ByteBuf) msg;
                frames.add(frame);
                frameStrings.add(frame.toString(StandardCharsets.UTF_8));
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            lastException = cause;
        }

        void releaseAll() {
            for (ByteBuf frame : frames) {
                if (frame.refCnt() > 0) {
                    frame.release();
                }
            }
        }
    }

    /**
     * 简单的 ChannelId 实现
     */
    private static class SimpleChannelId implements ChannelId {
        private static int counter = 0;
        private final int id = ++counter;

        @Override
        public String asShortText() {
            return "ch" + id;
        }

        @Override
        public String asLongText() {
            return "channel-" + id;
        }

        @Override
        public int compareTo(ChannelId o) {
            return asLongText().compareTo(o.asLongText());
        }
    }

    /**
     * 模拟 Channel
     */
    private static class MockChannel implements Channel {
        private final ChannelPipeline pipeline;
        private final ChannelId id = new SimpleChannelId();

        MockChannel() {
            this.pipeline = new DefaultChannelPipeline(this);
        }

        @Override
        public ChannelId id() {
            return id;
        }

        @Override
        public EventLoop eventLoop() {
            return null;
        }

        @Override
        public Channel parent() {
            return null;
        }

        @Override
        public ChannelConfig config() {
            return null;
        }

        @Override
        public boolean isOpen() {
            return true;
        }

        @Override
        public boolean isRegistered() {
            return false;
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public ChannelPipeline pipeline() {
            return pipeline;
        }

        @Override
        public ChannelFuture close() {
            return null;
        }

        @Override
        public Channel.Unsafe unsafe() {
            return null;
        }

        @Override
        public Channel read() {
            return this;
        }
    }

    private MockChannel channel;
    private ChannelPipeline pipeline;
    private FrameRecordingHandler recorder;

    @BeforeEach
    void setUp() {
        channel = new MockChannel();
        pipeline = channel.pipeline();
        recorder = new FrameRecordingHandler();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create decoder with valid parameters")
        void shouldCreateWithValidParameters() {
            LengthFieldBasedFrameDecoder decoder = new LengthFieldBasedFrameDecoder(
                1024, 0, 4, 0, 4);

            assertThat(decoder.getMaxFrameLength()).isEqualTo(1024);
            assertThat(decoder.getLengthFieldOffset()).isEqualTo(0);
            assertThat(decoder.getLengthFieldLength()).isEqualTo(4);
            assertThat(decoder.getLengthAdjustment()).isEqualTo(0);
            assertThat(decoder.getInitialBytesToStrip()).isEqualTo(4);
        }

        @Test
        @DisplayName("Should reject invalid maxFrameLength")
        void shouldRejectInvalidMaxFrameLength() {
            assertThatThrownBy(() -> new LengthFieldBasedFrameDecoder(0, 0, 4, 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxFrameLength");

            assertThatThrownBy(() -> new LengthFieldBasedFrameDecoder(-1, 0, 4, 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxFrameLength");
        }

        @Test
        @DisplayName("Should reject invalid lengthFieldLength")
        void shouldRejectInvalidLengthFieldLength() {
            assertThatThrownBy(() -> new LengthFieldBasedFrameDecoder(1024, 0, 5, 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lengthFieldLength");
        }

        @Test
        @DisplayName("Should accept valid lengthFieldLength values")
        void shouldAcceptValidLengthFieldLengths() {
            int[] validLengths = {1, 2, 3, 4, 8};
            for (int len : validLengths) {
                LengthFieldBasedFrameDecoder decoder = new LengthFieldBasedFrameDecoder(1024, 0, len, 0, 0);
                assertThat(decoder.getLengthFieldLength()).isEqualTo(len);
            }
        }
    }

    @Nested
    @DisplayName("Basic Decoding Tests")
    class BasicDecodingTests {

        @Test
        @DisplayName("Should decode frame with 2-byte length field")
        void shouldDecodeWith2ByteLength() throws Exception {
            // 配置: 长度字段在开头，2字节，表示数据长度，去掉长度字段
            LengthFieldBasedFrameDecoder decoder = new LengthFieldBasedFrameDecoder(
                1024, 0, 2, 0, 2);
            pipeline.addLast("decoder", decoder);
            pipeline.addLast("recorder", recorder);

            // 发送数据: [长度=5][Hello]
            ByteBuf input = new HeapByteBuf(16, 16);
            input.writeShort(5);  // 数据长度
            input.writeBytes("Hello".getBytes());

            pipeline.fireChannelRead(input);

            assertThat(recorder.frames).hasSize(1);
            ByteBuf frame = recorder.frames.get(0);
            byte[] data = new byte[frame.readableBytes()];
            frame.readBytes(data);
            assertThat(new String(data)).isEqualTo("Hello");
        }

        @Test
        @DisplayName("Should decode frame with 4-byte length field")
        void shouldDecodeWith4ByteLength() throws Exception {
            LengthFieldBasedFrameDecoder decoder = new LengthFieldBasedFrameDecoder(
                1024, 0, 4, 0, 4);
            pipeline.addLast("decoder", decoder);
            pipeline.addLast("recorder", recorder);

            // 发送数据: [长度=5][World]
            ByteBuf input = new HeapByteBuf(16, 16);
            input.writeInt(5);
            input.writeBytes("World".getBytes());

            pipeline.fireChannelRead(input);

            assertThat(recorder.frames).hasSize(1);
            ByteBuf frame = recorder.frames.get(0);
            byte[] data = new byte[frame.readableBytes()];
            frame.readBytes(data);
            assertThat(new String(data)).isEqualTo("World");
        }

        @Test
        @DisplayName("Should wait for more data when incomplete")
        void shouldWaitForMoreData() throws Exception {
            LengthFieldBasedFrameDecoder decoder = new LengthFieldBasedFrameDecoder(
                1024, 0, 2, 0, 2);
            pipeline.addLast("decoder", decoder);
            pipeline.addLast("recorder", recorder);

            // 只发送长度字段，数据不足
            ByteBuf input1 = new HeapByteBuf(8, 8);
            input1.writeShort(10);
            input1.writeBytes("Hello".getBytes());  // 只有5字节，需要10字节

            pipeline.fireChannelRead(input1);
            assertThat(recorder.frames).isEmpty();

            // 发送剩余数据
            ByteBuf input2 = new HeapByteBuf(8, 8);
            input2.writeBytes("World".getBytes());  // 剩余5字节

            pipeline.fireChannelRead(input2);
            assertThat(recorder.frames).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Length Adjustment Tests")
    class LengthAdjustmentTests {

        @Test
        @DisplayName("Should handle length including header")
        void shouldHandleLengthIncludingHeader() throws Exception {
            // 消息格式: [长度=7][5字节数据] 其中长度包含2字节长度字段
            LengthFieldBasedFrameDecoder decoder = new LengthFieldBasedFrameDecoder(
                1024, 0, 2, -2, 2);
            pipeline.addLast("decoder", decoder);
            pipeline.addLast("recorder", recorder);

            ByteBuf input = new HeapByteBuf(16, 16);
            input.writeShort(7);  // 总长度 = 2 + 5
            input.writeBytes("Hello".getBytes());

            pipeline.fireChannelRead(input);

            assertThat(recorder.frames).hasSize(1);
            ByteBuf frame = recorder.frames.get(0);
            byte[] data = new byte[frame.readableBytes()];
            frame.readBytes(data);
            assertThat(new String(data)).isEqualTo("Hello");
        }

        @Test
        @DisplayName("Should handle length with header offset")
        void shouldHandleHeaderOffset() throws Exception {
            // 消息格式: [2字节魔数][长度=5][5字节数据]
            LengthFieldBasedFrameDecoder decoder = new LengthFieldBasedFrameDecoder(
                1024, 2, 2, 0, 4);  // 跳过魔数和长度字段
            pipeline.addLast("decoder", decoder);
            pipeline.addLast("recorder", recorder);

            ByteBuf input = new HeapByteBuf(16, 16);
            input.writeShort(0xCAFE);  // 魔数
            input.writeShort(5);       // 数据长度
            input.writeBytes("Hello".getBytes());

            pipeline.fireChannelRead(input);

            assertThat(recorder.frames).hasSize(1);
            ByteBuf frame = recorder.frames.get(0);
            byte[] data = new byte[frame.readableBytes()];
            frame.readBytes(data);
            assertThat(new String(data)).isEqualTo("Hello");
        }
    }

    @Nested
    @DisplayName("Multiple Frames Tests")
    class MultipleFramesTests {

        @Test
        @DisplayName("Should decode multiple frames in one packet")
        void shouldDecodeMultipleFrames() throws Exception {
            LengthFieldBasedFrameDecoder decoder = new LengthFieldBasedFrameDecoder(
                1024, 0, 2, 0, 2);
            pipeline.addLast("decoder", decoder);
            pipeline.addLast("recorder", recorder);

            // 发送两个消息: [长度=2][Hi][长度=5][World]
            ByteBuf input = new HeapByteBuf(32, 32);
            input.writeShort(2);
            input.writeBytes("Hi".getBytes());
            input.writeShort(5);
            input.writeBytes("World".getBytes());

            pipeline.fireChannelRead(input);

            assertThat(recorder.frames).hasSize(2);

            ByteBuf frame1 = recorder.frames.get(0);
            byte[] data1 = new byte[frame1.readableBytes()];
            frame1.readBytes(data1);
            assertThat(new String(data1)).isEqualTo("Hi");

            ByteBuf frame2 = recorder.frames.get(1);
            byte[] data2 = new byte[frame2.readableBytes()];
            frame2.readBytes(data2);
            assertThat(new String(data2)).isEqualTo("World");
        }

        @Test
        @DisplayName("Should handle split packets")
        void shouldHandleSplitPackets() throws Exception {
            LengthFieldBasedFrameDecoder decoder = new LengthFieldBasedFrameDecoder(
                1024, 0, 2, 0, 2);
            pipeline.addLast("decoder", decoder);
            pipeline.addLast("recorder", recorder);

            // 第一个包: 只有长度的第一个字节
            ByteBuf input1 = new HeapByteBuf(8, 8);
            input1.writeByte(0);  // 高字节
            pipeline.fireChannelRead(input1);
            assertThat(recorder.frames).isEmpty();

            // 第二个包: 长度的第二个字节 + 部分数据
            ByteBuf input2 = new HeapByteBuf(8, 8);
            input2.writeByte(5);  // 低字节，组成长度 5
            input2.writeBytes("Hel".getBytes());
            pipeline.fireChannelRead(input2);
            assertThat(recorder.frames).isEmpty();

            // 第三个包: 剩余数据
            ByteBuf input3 = new HeapByteBuf(8, 8);
            input3.writeBytes("lo".getBytes());
            pipeline.fireChannelRead(input3);
            assertThat(recorder.frames).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw exception when frame exceeds maxFrameLength")
        void shouldThrowOnFrameExceedingMax() throws Exception {
            LengthFieldBasedFrameDecoder decoder = new LengthFieldBasedFrameDecoder(
                100, 0, 2, 0, 2);  // 最大100字节
            pipeline.addLast("decoder", decoder);
            pipeline.addLast("recorder", recorder);

            ByteBuf input = new HeapByteBuf(16, 16);
            input.writeShort(200);  // 请求200字节，超过限制
            input.writeBytes("Hello".getBytes());

            pipeline.fireChannelRead(input);

            assertThat(recorder.lastException)
                .isInstanceOf(DecoderException.class)
                .hasMessageContaining("exceeds maximum");
        }
    }

    @Nested
    @DisplayName("Acceptance Scenario Tests")
    class AcceptanceScenarioTests {

        @Test
        @DisplayName("Protocol message decoding - Type + Length + Data")
        void protocolMessageDecoding() throws Exception {
            // 协议格式: [1字节类型][2字节长度][N字节数据]
            LengthFieldBasedFrameDecoder decoder = new LengthFieldBasedFrameDecoder(
                1024, 1, 2, 0, 0);  // 保留整个帧
            pipeline.addLast("decoder", decoder);
            pipeline.addLast("recorder", recorder);

            // 消息类型=1 (登录), 数据长度=4, 数据="user"
            ByteBuf input = new HeapByteBuf(16, 16);
            input.writeByte(1);     // 类型
            input.writeShort(4);    // 数据长度
            input.writeBytes("user".getBytes());

            pipeline.fireChannelRead(input);

            assertThat(recorder.frames).hasSize(1);
            ByteBuf frame = recorder.frames.get(0);
            assertThat(frame.readableBytes()).isEqualTo(7);  // 1 + 2 + 4
            assertThat(frame.readByte()).isEqualTo((byte) 1);  // 类型
            assertThat(frame.readShort()).isEqualTo((short) 4);  // 长度
        }

        @Test
        @DisplayName("RPC message decoding with request ID")
        void rpcMessageDecoding() throws Exception {
            // RPC 协议: [4字节请求ID][4字节长度][N字节数据]
            LengthFieldBasedFrameDecoder decoder = new LengthFieldBasedFrameDecoder(
                1024, 4, 4, 0, 8);  // 跳过请求ID和长度
            pipeline.addLast("decoder", decoder);
            pipeline.addLast("recorder", recorder);

            // 请求ID=12345, 长度=3, 数据="RPC"
            ByteBuf input = new HeapByteBuf(32, 32);
            input.writeInt(12345);  // 请求ID
            input.writeInt(3);      // 数据长度
            input.writeBytes("RPC".getBytes());

            pipeline.fireChannelRead(input);

            assertThat(recorder.frames).hasSize(1);
            ByteBuf frame = recorder.frames.get(0);
            byte[] data = new byte[frame.readableBytes()];
            frame.readBytes(data);
            assertThat(new String(data)).isEqualTo("RPC");
        }
    }
}
