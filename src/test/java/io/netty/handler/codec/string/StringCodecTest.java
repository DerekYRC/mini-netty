package io.netty.handler.codec.string;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.HeapByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.junit.jupiter.api.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * StringDecoder 和 StringEncoder 测试
 */
@DisplayName("StringCodec Tests")
class StringCodecTest {

    /**
     * 记录接收消息的 Handler
     */
    private static class MessageRecordingHandler extends ChannelInboundHandlerAdapter {
        final List<Object> messages = new ArrayList<>();
        Throwable lastException;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            messages.add(msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            lastException = cause;
        }
    }

    /**
     * 记录写出消息的 Handler
     */
    private static class OutboundRecordingHandler extends ChannelOutboundHandlerAdapter {
        final List<Object> messages = new ArrayList<>();

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            messages.add(msg);
            promise.setSuccess();
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
    private MessageRecordingHandler inboundRecorder;
    private OutboundRecordingHandler outboundRecorder;

    @BeforeEach
    void setUp() {
        channel = new MockChannel();
        pipeline = channel.pipeline();
        inboundRecorder = new MessageRecordingHandler();
        outboundRecorder = new OutboundRecordingHandler();
    }

    @Nested
    @DisplayName("StringDecoder Tests")
    class StringDecoderTests {

        @Test
        @DisplayName("Should decode ByteBuf to String with UTF-8")
        void shouldDecodeWithUtf8() throws Exception {
            StringDecoder decoder = new StringDecoder();
            pipeline.addLast("decoder", decoder);
            pipeline.addLast("recorder", inboundRecorder);

            ByteBuf input = new HeapByteBuf(32, 32);
            input.writeBytes("Hello, World!".getBytes(StandardCharsets.UTF_8));

            pipeline.fireChannelRead(input);

            assertThat(inboundRecorder.messages).hasSize(1);
            assertThat(inboundRecorder.messages.get(0)).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("Should decode ByteBuf to String with specified charset")
        void shouldDecodeWithSpecifiedCharset() throws Exception {
            StringDecoder decoder = new StringDecoder(StandardCharsets.ISO_8859_1);
            pipeline.addLast("decoder", decoder);
            pipeline.addLast("recorder", inboundRecorder);

            ByteBuf input = new HeapByteBuf(32, 32);
            input.writeBytes("Hello".getBytes(StandardCharsets.ISO_8859_1));

            pipeline.fireChannelRead(input);

            assertThat(inboundRecorder.messages).hasSize(1);
            assertThat(inboundRecorder.messages.get(0)).isEqualTo("Hello");
        }

        @Test
        @DisplayName("Should decode Chinese characters")
        void shouldDecodeChineseCharacters() throws Exception {
            StringDecoder decoder = new StringDecoder(StandardCharsets.UTF_8);
            pipeline.addLast("decoder", decoder);
            pipeline.addLast("recorder", inboundRecorder);

            ByteBuf input = new HeapByteBuf(32, 32);
            input.writeBytes("你好世界".getBytes(StandardCharsets.UTF_8));

            pipeline.fireChannelRead(input);

            assertThat(inboundRecorder.messages).hasSize(1);
            assertThat(inboundRecorder.messages.get(0)).isEqualTo("你好世界");
        }

        @Test
        @DisplayName("Should release ByteBuf after decoding")
        void shouldReleaseByteBufAfterDecoding() throws Exception {
            StringDecoder decoder = new StringDecoder();
            pipeline.addLast("decoder", decoder);
            pipeline.addLast("recorder", inboundRecorder);

            ByteBuf input = new HeapByteBuf(32, 32);
            input.writeBytes("Test".getBytes(StandardCharsets.UTF_8));

            pipeline.fireChannelRead(input);

            assertThat(input.refCnt()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should pass through non-ByteBuf messages")
        void shouldPassThroughNonByteBufMessages() throws Exception {
            StringDecoder decoder = new StringDecoder();
            pipeline.addLast("decoder", decoder);
            pipeline.addLast("recorder", inboundRecorder);

            Integer number = 42;
            pipeline.fireChannelRead(number);

            assertThat(inboundRecorder.messages).hasSize(1);
            assertThat(inboundRecorder.messages.get(0)).isEqualTo(42);
        }

        @Test
        @DisplayName("Should reject null charset")
        void shouldRejectNullCharset() {
            assertThatThrownBy(() -> new StringDecoder(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    /**
     * 测试用 StringEncoder - 暴露 protected 方法
     */
    private static class TestableStringEncoder extends StringEncoder {
        public TestableStringEncoder() {
            super();
        }

        public TestableStringEncoder(Charset charset) {
            super(charset);
        }

        public void testEncode(CharSequence msg, ByteBuf out) throws Exception {
            encode(null, msg, out);
        }
    }

    @Nested
    @DisplayName("StringEncoder Tests")
    class StringEncoderTests {

        @Test
        @DisplayName("Should encode String to ByteBuf with UTF-8")
        void shouldEncodeWithUtf8() throws Exception {
            TestableStringEncoder encoder = new TestableStringEncoder();
            ByteBuf out = new HeapByteBuf(64, 64);
            
            encoder.testEncode("Hello, World!", out);

            assertThat(out.toString(StandardCharsets.UTF_8)).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("Should encode Chinese characters")
        void shouldEncodeChineseCharacters() throws Exception {
            TestableStringEncoder encoder = new TestableStringEncoder(StandardCharsets.UTF_8);
            ByteBuf out = new HeapByteBuf(64, 64);

            encoder.testEncode("你好世界", out);

            assertThat(out.toString(StandardCharsets.UTF_8)).isEqualTo("你好世界");
        }

        @Test
        @DisplayName("Should handle empty string")
        void shouldHandleEmptyString() throws Exception {
            TestableStringEncoder encoder = new TestableStringEncoder();
            ByteBuf out = new HeapByteBuf(64, 64);

            encoder.testEncode("", out);

            assertThat(out.readableBytes()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should reject null charset")
        void shouldRejectNullCharset() {
            assertThatThrownBy(() -> new StringEncoder(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Combined Codec Tests")
    class CombinedCodecTests {

        @Test
        @DisplayName("Should work with LengthFieldBasedFrameDecoder")
        void shouldWorkWithFrameDecoder() throws Exception {
            // 模拟完整的解码流程
            LengthFieldBasedFrameDecoder frameDecoder = new LengthFieldBasedFrameDecoder(
                1024, 0, 2, 0, 2);
            StringDecoder stringDecoder = new StringDecoder();
            
            pipeline.addLast("frameDecoder", frameDecoder);
            pipeline.addLast("stringDecoder", stringDecoder);
            pipeline.addLast("recorder", inboundRecorder);

            // 发送带长度前缀的消息
            ByteBuf input = new HeapByteBuf(32, 32);
            input.writeShort(5);  // 长度
            input.writeBytes("Hello".getBytes(StandardCharsets.UTF_8));

            pipeline.fireChannelRead(input);

            assertThat(inboundRecorder.messages).hasSize(1);
            assertThat(inboundRecorder.messages.get(0)).isEqualTo("Hello");
        }

        @Test
        @DisplayName("Should handle multiple messages with frame decoder")
        void shouldHandleMultipleMessagesWithFrameDecoder() throws Exception {
            LengthFieldBasedFrameDecoder frameDecoder = new LengthFieldBasedFrameDecoder(
                1024, 0, 2, 0, 2);
            StringDecoder stringDecoder = new StringDecoder();
            
            pipeline.addLast("frameDecoder", frameDecoder);
            pipeline.addLast("stringDecoder", stringDecoder);
            pipeline.addLast("recorder", inboundRecorder);

            // 发送两个粘在一起的消息
            ByteBuf input = new HeapByteBuf(64, 64);
            input.writeShort(5);
            input.writeBytes("Hello".getBytes(StandardCharsets.UTF_8));
            input.writeShort(5);
            input.writeBytes("World".getBytes(StandardCharsets.UTF_8));

            pipeline.fireChannelRead(input);

            assertThat(inboundRecorder.messages).hasSize(2);
            assertThat(inboundRecorder.messages.get(0)).isEqualTo("Hello");
            assertThat(inboundRecorder.messages.get(1)).isEqualTo("World");
        }
    }

    @Nested
    @DisplayName("Acceptance Scenario Tests")
    class AcceptanceScenarioTests {

        @Test
        @DisplayName("Chat message decoding scenario")
        void chatMessageDecodingScenario() throws Exception {
            // 模拟聊天消息解码
            LengthFieldBasedFrameDecoder frameDecoder = new LengthFieldBasedFrameDecoder(
                1024, 0, 2, 0, 2);
            StringDecoder stringDecoder = new StringDecoder();
            
            pipeline.addLast("frameDecoder", frameDecoder);
            pipeline.addLast("stringDecoder", stringDecoder);
            pipeline.addLast("recorder", inboundRecorder);

            // 模拟收到的聊天消息
            String[] messages = {"Hello!", "How are you?", "I'm fine, thanks!"};
            ByteBuf input = new HeapByteBuf(256, 256);
            for (String msg : messages) {
                byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
                input.writeShort(bytes.length);
                input.writeBytes(bytes);
            }

            pipeline.fireChannelRead(input);

            assertThat(inboundRecorder.messages).hasSize(3);
            assertThat(inboundRecorder.messages.get(0)).isEqualTo("Hello!");
            assertThat(inboundRecorder.messages.get(1)).isEqualTo("How are you?");
            assertThat(inboundRecorder.messages.get(2)).isEqualTo("I'm fine, thanks!");
        }

        @Test
        @DisplayName("RPC response decoding scenario")
        void rpcResponseDecodingScenario() throws Exception {
            // 模拟 RPC 响应解码：[4字节请求ID][2字节长度][数据]
            // 只解码数据部分为字符串
            LengthFieldBasedFrameDecoder frameDecoder = new LengthFieldBasedFrameDecoder(
                1024, 4, 2, 0, 6);  // 跳过请求ID和长度
            StringDecoder stringDecoder = new StringDecoder();
            
            pipeline.addLast("frameDecoder", frameDecoder);
            pipeline.addLast("stringDecoder", stringDecoder);
            pipeline.addLast("recorder", inboundRecorder);

            // 发送RPC响应
            ByteBuf input = new HeapByteBuf(32, 32);
            input.writeInt(12345);  // 请求ID
            input.writeShort(7);    // 数据长度
            input.writeBytes("Success".getBytes(StandardCharsets.UTF_8));

            pipeline.fireChannelRead(input);

            assertThat(inboundRecorder.messages).hasSize(1);
            assertThat(inboundRecorder.messages.get(0)).isEqualTo("Success");
        }
    }
}
