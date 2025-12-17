package io.netty.handler.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.HeapByteBuf;
import io.netty.channel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * FixedLengthFrameDecoder 测试
 */
@DisplayName("FixedLengthFrameDecoder 测试")
class FixedLengthFrameDecoderTest {

    /**
     * 记录接收帧的 Handler
     */
    private static class FrameRecordingHandler extends ChannelInboundHandlerAdapter {
        final List<ByteBuf> frames = new ArrayList<>();
        final List<String> frameStrings = new ArrayList<>();

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof ByteBuf) {
                ByteBuf frame = (ByteBuf) msg;
                frames.add(frame);
                frameStrings.add(frame.toString(StandardCharsets.UTF_8));
            }
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
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("应接受正数帧长度")
        void shouldAcceptPositiveFrameLength() {
            FixedLengthFrameDecoder decoder = new FixedLengthFrameDecoder(8);
            
            assertThat(decoder.getFrameLength()).isEqualTo(8);
        }

        @Test
        @DisplayName("应拒绝零或负数帧长度")
        void shouldRejectZeroOrNegativeFrameLength() {
            assertThatThrownBy(() -> new FixedLengthFrameDecoder(0))
                    .isInstanceOf(IllegalArgumentException.class);
            
            assertThatThrownBy(() -> new FixedLengthFrameDecoder(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("基本解码测试")
    class BasicDecodingTests {

        @Test
        @DisplayName("应解码单个完整帧")
        void shouldDecodeSingleCompleteFrame() {
            pipeline.addLast("decoder", new FixedLengthFrameDecoder(4));
            pipeline.addLast("recorder", recorder);

            ByteBuf buf = new HeapByteBuf(16, 64);
            buf.writeBytes("ABCD".getBytes(StandardCharsets.UTF_8));

            pipeline.fireChannelRead(buf);

            try {
                assertThat(recorder.frameStrings).containsExactly("ABCD");
            } finally {
                recorder.releaseAll();
            }
        }

        @Test
        @DisplayName("应解码多个完整帧")
        void shouldDecodeMultipleCompleteFrames() {
            pipeline.addLast("decoder", new FixedLengthFrameDecoder(4));
            pipeline.addLast("recorder", recorder);

            ByteBuf buf = new HeapByteBuf(32, 64);
            buf.writeBytes("ABCDWXYZ1234".getBytes(StandardCharsets.UTF_8));

            pipeline.fireChannelRead(buf);

            try {
                assertThat(recorder.frameStrings).containsExactly("ABCD", "WXYZ", "1234");
            } finally {
                recorder.releaseAll();
            }
        }

        @Test
        @DisplayName("不完整帧应等待更多数据")
        void shouldWaitForMoreDataIfIncomplete() {
            pipeline.addLast("decoder", new FixedLengthFrameDecoder(8));
            pipeline.addLast("recorder", recorder);

            ByteBuf buf = new HeapByteBuf(8, 64);
            buf.writeBytes("ABCD".getBytes(StandardCharsets.UTF_8)); // 只有 4 字节

            pipeline.fireChannelRead(buf);

            assertThat(recorder.frames).isEmpty();
        }
    }

    @Nested
    @DisplayName("粘包拆包测试")
    class PacketSplitMergeTests {

        @Test
        @DisplayName("应处理跨包的帧")
        void shouldHandleFrameAcrossPackets() {
            pipeline.addLast("decoder", new FixedLengthFrameDecoder(8));
            pipeline.addLast("recorder", recorder);

            // 第一批：4 字节
            ByteBuf part1 = new HeapByteBuf(8, 64);
            part1.writeBytes("ABCD".getBytes(StandardCharsets.UTF_8));
            pipeline.fireChannelRead(part1);

            assertThat(recorder.frames).isEmpty();

            // 第二批：4 字节，凑成完整帧
            ByteBuf part2 = new HeapByteBuf(8, 64);
            part2.writeBytes("EFGH".getBytes(StandardCharsets.UTF_8));
            pipeline.fireChannelRead(part2);

            try {
                assertThat(recorder.frameStrings).containsExactly("ABCDEFGH");
            } finally {
                recorder.releaseAll();
            }
        }

        @Test
        @DisplayName("应处理一批数据包含多个帧和部分帧")
        void shouldHandleMultipleFramesAndPartial() {
            pipeline.addLast("decoder", new FixedLengthFrameDecoder(4));
            pipeline.addLast("recorder", recorder);

            // 发送 10 字节 = 2 个完整帧 + 2 字节部分帧
            ByteBuf buf = new HeapByteBuf(16, 64);
            buf.writeBytes("ABCDWXYZ12".getBytes(StandardCharsets.UTF_8));
            pipeline.fireChannelRead(buf);

            try {
                assertThat(recorder.frameStrings).containsExactly("ABCD", "WXYZ");
            } finally {
                recorder.releaseAll();
            }

            // 发送剩余 2 字节
            ByteBuf buf2 = new HeapByteBuf(8, 64);
            buf2.writeBytes("34".getBytes(StandardCharsets.UTF_8));
            pipeline.fireChannelRead(buf2);

            try {
                assertThat(recorder.frameStrings).containsExactly("ABCD", "WXYZ", "1234");
            } finally {
                recorder.releaseAll();
            }
        }
    }

    @Nested
    @DisplayName("验收场景测试")
    class AcceptanceScenarioTests {

        @Test
        @DisplayName("传感器数据解码场景")
        void sensorDataDecodingScenario() {
            // 假设每个传感器读数是 8 字节
            pipeline.addLast("decoder", new FixedLengthFrameDecoder(8));
            pipeline.addLast("recorder", recorder);

            // 模拟传感器数据流
            ByteBuf data = new HeapByteBuf(32, 128);
            // 温度读数 1
            data.writeInt(25); // 温度值
            data.writeInt(1000); // 时间戳
            // 温度读数 2
            data.writeInt(26);
            data.writeInt(1001);

            pipeline.fireChannelRead(data);

            try {
                assertThat(recorder.frames).hasSize(2);
                
                // 验证第一个读数
                ByteBuf frame1 = recorder.frames.get(0);
                assertThat(frame1.readInt()).isEqualTo(25);
                assertThat(frame1.readInt()).isEqualTo(1000);
                
                // 验证第二个读数
                ByteBuf frame2 = recorder.frames.get(1);
                assertThat(frame2.readInt()).isEqualTo(26);
                assertThat(frame2.readInt()).isEqualTo(1001);
            } finally {
                recorder.releaseAll();
            }
        }

        @Test
        @DisplayName("固定长度命令协议场景")
        void fixedLengthCommandProtocolScenario() {
            // 命令格式：4 字节命令码
            pipeline.addLast("decoder", new FixedLengthFrameDecoder(4));
            pipeline.addLast("recorder", recorder);

            ByteBuf commands = new HeapByteBuf(20, 64);
            commands.writeBytes("PING".getBytes(StandardCharsets.UTF_8));
            commands.writeBytes("QUIT".getBytes(StandardCharsets.UTF_8));
            commands.writeBytes("STAT".getBytes(StandardCharsets.UTF_8));

            pipeline.fireChannelRead(commands);

            try {
                assertThat(recorder.frameStrings)
                        .containsExactly("PING", "QUIT", "STAT");
            } finally {
                recorder.releaseAll();
            }
        }
    }
}
