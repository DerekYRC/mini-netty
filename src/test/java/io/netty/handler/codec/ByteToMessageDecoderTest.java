package io.netty.handler.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.HeapByteBuf;
import io.netty.channel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ByteToMessageDecoder 测试
 */
@DisplayName("ByteToMessageDecoder 测试")
class ByteToMessageDecoderTest {

    /**
     * 整数解码器 - 每次读取 4 字节
     */
    private static class IntegerDecoder extends ByteToMessageDecoder {
        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
            if (in.readableBytes() >= 4) {
                out.add(in.readInt());
            }
        }
    }

    /**
     * 行解码器 - 以换行符分割
     */
    private static class LineDecoder extends ByteToMessageDecoder {
        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
            int newlineIndex = findNewline(in);
            if (newlineIndex >= 0) {
                byte[] line = new byte[newlineIndex - in.readerIndex()];
                in.readBytes(line);
                in.skipBytes(1); // 跳过换行符
                out.add(new String(line));
            }
        }

        private int findNewline(ByteBuf in) {
            for (int i = in.readerIndex(); i < in.writerIndex(); i++) {
                if (in.getByte(i) == '\n') {
                    return i;
                }
            }
            return -1;
        }
    }

    /**
     * 记录接收消息的 Handler
     */
    private static class RecordingHandler extends ChannelInboundHandlerAdapter {
        final List<Object> messages = new ArrayList<>();

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            messages.add(msg);
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
    private RecordingHandler recorder;

    @BeforeEach
    void setUp() {
        channel = new MockChannel();
        pipeline = channel.pipeline();
        recorder = new RecordingHandler();
    }

    @Nested
    @DisplayName("基础解码测试")
    class BasicDecodingTests {

        @Test
        @DisplayName("应解码完整的整数消息")
        void shouldDecodeCompleteIntegerMessage() {
            pipeline.addLast("decoder", new IntegerDecoder());
            pipeline.addLast("recorder", recorder);

            ByteBuf buf = new HeapByteBuf(16, 64);
            buf.writeInt(42);

            pipeline.fireChannelRead(buf);

            assertThat(recorder.messages).containsExactly(42);
        }

        @Test
        @DisplayName("应解码多个整数消息")
        void shouldDecodeMultipleIntegerMessages() {
            pipeline.addLast("decoder", new IntegerDecoder());
            pipeline.addLast("recorder", recorder);

            ByteBuf buf = new HeapByteBuf(32, 64);
            buf.writeInt(1);
            buf.writeInt(2);
            buf.writeInt(3);

            pipeline.fireChannelRead(buf);

            assertThat(recorder.messages).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("数据不足时应等待更多数据")
        void shouldWaitForMoreDataIfIncomplete() {
            pipeline.addLast("decoder", new IntegerDecoder());
            pipeline.addLast("recorder", recorder);

            // 只发送 2 字节，不足 4 字节
            ByteBuf buf = new HeapByteBuf(8, 64);
            buf.writeShort(0x1234);

            pipeline.fireChannelRead(buf);

            // 不应有消息输出
            assertThat(recorder.messages).isEmpty();
        }
    }

    @Nested
    @DisplayName("粘包拆包测试")
    class PacketSplitMergeTests {

        @Test
        @DisplayName("应处理粘包（多条消息合并）")
        void shouldHandlePacketMerging() {
            pipeline.addLast("decoder", new IntegerDecoder());
            pipeline.addLast("recorder", recorder);

            // 一次发送包含两条消息的数据
            ByteBuf buf = new HeapByteBuf(16, 64);
            buf.writeInt(100);
            buf.writeInt(200);

            pipeline.fireChannelRead(buf);

            assertThat(recorder.messages).containsExactly(100, 200);
        }

        @Test
        @DisplayName("应处理拆包（一条消息分多次发送）")
        void shouldHandlePacketSplitting() {
            pipeline.addLast("decoder", new IntegerDecoder());
            pipeline.addLast("recorder", recorder);

            // 第一次发送 2 字节
            ByteBuf part1 = new HeapByteBuf(8, 64);
            part1.writeShort(0x0000);
            pipeline.fireChannelRead(part1);

            // 没有完整消息
            assertThat(recorder.messages).isEmpty();

            // 第二次发送剩余 2 字节，组成完整的 int
            ByteBuf part2 = new HeapByteBuf(8, 64);
            part2.writeShort(0x002A); // 42 的低 16 位
            pipeline.fireChannelRead(part2);

            // 现在应该有一条消息
            assertThat(recorder.messages).hasSize(1);
            assertThat(recorder.messages.get(0)).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("行解码器测试")
    class LineDecoderTests {

        @Test
        @DisplayName("应解码单行消息")
        void shouldDecodeSingleLine() {
            pipeline.addLast("decoder", new LineDecoder());
            pipeline.addLast("recorder", recorder);

            ByteBuf buf = new HeapByteBuf(32, 128);
            buf.writeBytes("Hello\n".getBytes());

            pipeline.fireChannelRead(buf);

            assertThat(recorder.messages).containsExactly("Hello");
        }

        @Test
        @DisplayName("应解码多行消息")
        void shouldDecodeMultipleLines() {
            pipeline.addLast("decoder", new LineDecoder());
            pipeline.addLast("recorder", recorder);

            ByteBuf buf = new HeapByteBuf(64, 128);
            buf.writeBytes("Line1\nLine2\nLine3\n".getBytes());

            pipeline.fireChannelRead(buf);

            assertThat(recorder.messages).containsExactly("Line1", "Line2", "Line3");
        }

        @Test
        @DisplayName("不完整行应等待换行符")
        void shouldWaitForNewline() {
            pipeline.addLast("decoder", new LineDecoder());
            pipeline.addLast("recorder", recorder);

            ByteBuf buf = new HeapByteBuf(32, 128);
            buf.writeBytes("Incomplete".getBytes());

            pipeline.fireChannelRead(buf);

            assertThat(recorder.messages).isEmpty();
        }
    }

    @Nested
    @DisplayName("验收场景测试")
    class AcceptanceScenarioTests {

        @Test
        @DisplayName("典型的网络消息解码场景")
        void typicalNetworkMessageDecodingScenario() {
            // 长度前缀解码器
            ByteToMessageDecoder lengthDecoder = new ByteToMessageDecoder() {
                @Override
                protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
                    if (in.readableBytes() < 4) {
                        return; // 等待长度字段
                    }
                    
                    in.markReaderIndex();
                    int length = in.readInt();
                    
                    if (in.readableBytes() < length) {
                        in.resetReaderIndex(); // 数据不足，重置
                        return;
                    }
                    
                    byte[] data = new byte[length];
                    in.readBytes(data);
                    out.add(new String(data));
                }
            };

            pipeline.addLast("decoder", lengthDecoder);
            pipeline.addLast("recorder", recorder);

            // 发送 "Hello" 消息（长度 5 + 内容）
            ByteBuf buf = new HeapByteBuf(32, 128);
            buf.writeInt(5);
            buf.writeBytes("Hello".getBytes());

            pipeline.fireChannelRead(buf);

            assertThat(recorder.messages).containsExactly("Hello");
        }

        @Test
        @DisplayName("混合粘包拆包场景")
        void mixedPacketScenario() {
            pipeline.addLast("decoder", new IntegerDecoder());
            pipeline.addLast("recorder", recorder);

            // 第一批：1.5 条消息
            ByteBuf batch1 = new HeapByteBuf(16, 64);
            batch1.writeInt(1);
            batch1.writeShort(0); // 第二条消息的前半部分
            pipeline.fireChannelRead(batch1);

            assertThat(recorder.messages).containsExactly(1);

            // 第二批：0.5 + 2 条消息
            ByteBuf batch2 = new HeapByteBuf(16, 64);
            batch2.writeShort(2); // 第二条消息的后半部分 (值为2)
            batch2.writeInt(3);
            batch2.writeInt(4);
            pipeline.fireChannelRead(batch2);

            assertThat(recorder.messages).containsExactly(1, 2, 3, 4);
        }
    }
}
