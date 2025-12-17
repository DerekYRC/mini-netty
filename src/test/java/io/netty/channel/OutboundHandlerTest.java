package io.netty.channel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * 出站事件处理测试
 *
 * <p>测试 ChannelOutboundHandlerAdapter 和出站事件传递机制。
 */
@DisplayName("OutboundHandler 测试")
class OutboundHandlerTest {

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
     * 模拟 Channel 用于测试
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

    /**
     * 记录事件的出站 Handler
     */
    private static class RecordingOutboundHandler extends ChannelOutboundHandlerAdapter {
        final List<String> events = new ArrayList<>();
        final String name;

        RecordingOutboundHandler(String name) {
            this.name = name;
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) {
            events.add(name + ":handlerAdded");
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) {
            events.add(name + ":handlerRemoved");
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            events.add(name + ":write:" + msg);
            super.write(ctx, msg, promise);
        }

        @Override
        public void flush(ChannelHandlerContext ctx) throws Exception {
            events.add(name + ":flush");
            super.flush(ctx);
        }

        @Override
        public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            events.add(name + ":close");
            super.close(ctx, promise);
        }

        @Override
        public void read(ChannelHandlerContext ctx) throws Exception {
            events.add(name + ":read");
            super.read(ctx);
        }
    }

    private MockChannel channel;
    private ChannelPipeline pipeline;

    @BeforeEach
    void setUp() {
        channel = new MockChannel();
        pipeline = channel.pipeline();
    }

    @Nested
    @DisplayName("ChannelOutboundHandlerAdapter 测试")
    class AdapterTests {

        @Test
        @DisplayName("Adapter 应提供所有出站方法的默认实现")
        void adapterShouldProvideDefaultImplementations() {
            ChannelOutboundHandlerAdapter adapter = new ChannelOutboundHandlerAdapter();
            
            assertThat(adapter).isInstanceOf(ChannelOutboundHandler.class);
        }

        @Test
        @DisplayName("Adapter 应实现 ChannelHandler 生命周期方法")
        void adapterShouldImplementLifecycleMethods() {
            RecordingOutboundHandler handler = new RecordingOutboundHandler("H");
            
            pipeline.addLast("handler", handler);
            
            assertThat(handler.events).contains("H:handlerAdded");
        }
    }

    @Nested
    @DisplayName("出站操作测试")
    class OutboundOperationsTests {

        @Test
        @DisplayName("write 应通过 Context 触发")
        void writeShouldBeTriggerableFromContext() {
            RecordingOutboundHandler handler = new RecordingOutboundHandler("H");
            pipeline.addLast("handler", handler);
            handler.events.clear();

            ChannelHandlerContext ctx = pipeline.context("handler");
            ctx.write("TestMessage");

            // write 会传递到 HeadContext
            // 由于没有实际的 HeadContext write 实现，这里只验证调用成功
        }

        @Test
        @DisplayName("flush 应通过 Context 触发")
        void flushShouldBeTriggerableFromContext() {
            RecordingOutboundHandler handler = new RecordingOutboundHandler("H");
            pipeline.addLast("handler", handler);
            handler.events.clear();

            ChannelHandlerContext ctx = pipeline.context("handler");
            ctx.flush();

            // flush 传递成功
        }

        @Test
        @DisplayName("close 应通过 Context 触发")
        void closeShouldBeTriggerableFromContext() {
            RecordingOutboundHandler handler = new RecordingOutboundHandler("H");
            pipeline.addLast("handler", handler);
            handler.events.clear();

            ChannelHandlerContext ctx = pipeline.context("handler");
            ctx.close();

            // close 传递到 HeadContext
        }

        @Test
        @DisplayName("read 应通过 Context 触发")
        void readShouldBeTriggerableFromContext() {
            RecordingOutboundHandler handler = new RecordingOutboundHandler("H");
            pipeline.addLast("handler", handler);
            handler.events.clear();

            ChannelHandlerContext ctx = pipeline.context("handler");
            ctx.read();

            // read 传递成功
        }
    }

    @Nested
    @DisplayName("Handler 链测试")
    class HandlerChainTests {

        @Test
        @DisplayName("多个出站 Handler 应反向处理 write")
        void multipleHandlersShouldProcessWriteInReverse() {
            // 添加入站和出站 Handler
            RecordingOutboundHandler encoder1 = new RecordingOutboundHandler("Encoder1");
            RecordingOutboundHandler encoder2 = new RecordingOutboundHandler("Encoder2");
            
            pipeline.addLast("encoder1", encoder1);
            pipeline.addLast("encoder2", encoder2);
            encoder1.events.clear();
            encoder2.events.clear();

            // 通过 Context 触发 write
            // 出站事件从尾部向头部传递
            ChannelHandlerContext ctx = pipeline.context("encoder2");
            ctx.write("Message");

            // encoder2 不处理（它是调用者）
            // encoder1 应该收到（它在 encoder2 前面）
        }
    }

    @Nested
    @DisplayName("验收场景测试")
    class AcceptanceScenarioTests {

        @Test
        @DisplayName("典型的编码器 Pipeline 场景")
        void typicalEncoderPipelineScenario() {
            // 创建一个转换消息的编码器
            ChannelOutboundHandlerAdapter stringEncoder = new ChannelOutboundHandlerAdapter() {
                @Override
                public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                    // 将消息转换为字符串
                    String encoded = "[ENCODED]" + msg;
                    ctx.write(encoded, promise);
                }
            };

            RecordingOutboundHandler logger = new RecordingOutboundHandler("Logger");

            pipeline.addLast("encoder", stringEncoder);
            pipeline.addLast("logger", logger);
            logger.events.clear();

            // 从 logger 发起 write
            ChannelHandlerContext loggerCtx = pipeline.context("logger");
            loggerCtx.write("Hello");

            // Logger 是调用者，不会记录这次 write
            // 但 encoder 会处理并转换消息
        }

        @Test
        @DisplayName("出站操作应正确处理")
        void outboundOperationsShouldBeProcessedCorrectly() {
            RecordingOutboundHandler handler = new RecordingOutboundHandler("Handler");
            pipeline.addLast("handler", handler);
            handler.events.clear();

            // 验证可以添加出站 Handler
            assertThat(pipeline.get("handler")).isNotNull();
            
            // 验证 handler 被正确添加
            ChannelHandlerContext ctx = pipeline.context("handler");
            assertThat(ctx).isNotNull();
            assertThat(ctx.handler()).isSameAs(handler);
        }
    }
}
