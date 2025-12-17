package io.netty.channel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * 入站事件处理测试
 *
 * <p>测试 ChannelInboundHandlerAdapter 和入站事件传递机制。
 */
@DisplayName("InboundHandler 测试")
class InboundHandlerTest {

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
     * 记录事件的 Handler
     */
    private static class RecordingHandler extends ChannelInboundHandlerAdapter {
        final List<String> events = new ArrayList<>();
        final String name;

        RecordingHandler(String name) {
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
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            events.add(name + ":channelRegistered");
            super.channelRegistered(ctx);
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            events.add(name + ":channelUnregistered");
            super.channelUnregistered(ctx);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            events.add(name + ":channelActive");
            super.channelActive(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            events.add(name + ":channelInactive");
            super.channelInactive(ctx);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            events.add(name + ":channelRead:" + msg);
            super.channelRead(ctx, msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            events.add(name + ":channelReadComplete");
            super.channelReadComplete(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            events.add(name + ":exceptionCaught:" + cause.getMessage());
            super.exceptionCaught(ctx, cause);
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
    @DisplayName("ChannelInboundHandlerAdapter 测试")
    class AdapterTests {

        @Test
        @DisplayName("Adapter 应提供所有入站方法的默认实现")
        void adapterShouldProvideDefaultImplementations() {
            // 使用默认 Adapter，只验证它实现了所有方法
            ChannelInboundHandlerAdapter adapter = new ChannelInboundHandlerAdapter();
            
            // 验证是 ChannelInboundHandler 的实例
            assertThat(adapter).isInstanceOf(ChannelInboundHandler.class);
        }

        @Test
        @DisplayName("Adapter 默认实现应传递事件")
        void defaultImplementationShouldPropagateEvents() {
            RecordingHandler handler1 = new RecordingHandler("H1");
            RecordingHandler handler2 = new RecordingHandler("H2");
            
            pipeline.addLast("h1", handler1);
            pipeline.addLast("h2", handler2);
            handler1.events.clear();
            handler2.events.clear();

            // 触发事件
            pipeline.fireChannelActive();

            // 验证事件传递到两个 Handler
            assertThat(handler1.events).contains("H1:channelActive");
            assertThat(handler2.events).contains("H2:channelActive");
        }
    }

    @Nested
    @DisplayName("入站事件传递测试")
    class InboundEventPropagationTests {

        @Test
        @DisplayName("channelRegistered 应按顺序传递")
        void channelRegisteredShouldPropagate() {
            RecordingHandler h1 = new RecordingHandler("H1");
            RecordingHandler h2 = new RecordingHandler("H2");
            pipeline.addLast("h1", h1);
            pipeline.addLast("h2", h2);
            h1.events.clear();
            h2.events.clear();

            pipeline.fireChannelRegistered();

            assertThat(h1.events).contains("H1:channelRegistered");
            assertThat(h2.events).contains("H2:channelRegistered");
        }

        @Test
        @DisplayName("channelActive 应按顺序传递")
        void channelActiveShouldPropagate() {
            RecordingHandler h1 = new RecordingHandler("H1");
            RecordingHandler h2 = new RecordingHandler("H2");
            pipeline.addLast("h1", h1);
            pipeline.addLast("h2", h2);
            h1.events.clear();
            h2.events.clear();

            pipeline.fireChannelActive();

            assertThat(h1.events).contains("H1:channelActive");
            assertThat(h2.events).contains("H2:channelActive");
        }

        @Test
        @DisplayName("channelRead 应传递消息")
        void channelReadShouldPropagateMessage() {
            RecordingHandler h1 = new RecordingHandler("H1");
            RecordingHandler h2 = new RecordingHandler("H2");
            pipeline.addLast("h1", h1);
            pipeline.addLast("h2", h2);
            h1.events.clear();
            h2.events.clear();

            pipeline.fireChannelRead("TestMessage");

            assertThat(h1.events).contains("H1:channelRead:TestMessage");
            assertThat(h2.events).contains("H2:channelRead:TestMessage");
        }

        @Test
        @DisplayName("channelReadComplete 应按顺序传递")
        void channelReadCompleteShouldPropagate() {
            RecordingHandler h1 = new RecordingHandler("H1");
            RecordingHandler h2 = new RecordingHandler("H2");
            pipeline.addLast("h1", h1);
            pipeline.addLast("h2", h2);
            h1.events.clear();
            h2.events.clear();

            pipeline.fireChannelReadComplete();

            assertThat(h1.events).contains("H1:channelReadComplete");
            assertThat(h2.events).contains("H2:channelReadComplete");
        }

        @Test
        @DisplayName("channelInactive 应按顺序传递")
        void channelInactiveShouldPropagate() {
            RecordingHandler h1 = new RecordingHandler("H1");
            RecordingHandler h2 = new RecordingHandler("H2");
            pipeline.addLast("h1", h1);
            pipeline.addLast("h2", h2);
            h1.events.clear();
            h2.events.clear();

            pipeline.fireChannelInactive();

            assertThat(h1.events).contains("H1:channelInactive");
            assertThat(h2.events).contains("H2:channelInactive");
        }

        @Test
        @DisplayName("exceptionCaught 应传递异常")
        void exceptionCaughtShouldPropagate() {
            RecordingHandler h1 = new RecordingHandler("H1");
            RecordingHandler h2 = new RecordingHandler("H2");
            pipeline.addLast("h1", h1);
            pipeline.addLast("h2", h2);
            h1.events.clear();
            h2.events.clear();

            pipeline.fireExceptionCaught(new RuntimeException("TestError"));

            assertThat(h1.events).contains("H1:exceptionCaught:TestError");
            assertThat(h2.events).contains("H2:exceptionCaught:TestError");
        }
    }

    @Nested
    @DisplayName("事件拦截测试")
    class EventInterceptionTests {

        @Test
        @DisplayName("不调用 super 方法应阻止事件传递")
        void notCallingSupergShouldStopPropagation() {
            RecordingHandler first = new RecordingHandler("First") {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) {
                    events.add(name + ":channelRead:" + msg);
                    // 不调用 super，阻止传递
                }
            };
            RecordingHandler second = new RecordingHandler("Second");
            
            pipeline.addLast("first", first);
            pipeline.addLast("second", second);
            first.events.clear();
            second.events.clear();

            pipeline.fireChannelRead("Message");

            assertThat(first.events).contains("First:channelRead:Message");
            assertThat(second.events).doesNotContain("Second:channelRead:Message");
        }

        @Test
        @DisplayName("修改消息后传递")
        void modifyMessageBeforePropagation() {
            RecordingHandler transformer = new RecordingHandler("Transformer") {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    events.add(name + ":channelRead:" + msg);
                    // 修改消息后传递
                    ctx.fireChannelRead("Transformed-" + msg);
                }
            };
            RecordingHandler receiver = new RecordingHandler("Receiver");
            
            pipeline.addLast("transformer", transformer);
            pipeline.addLast("receiver", receiver);
            transformer.events.clear();
            receiver.events.clear();

            pipeline.fireChannelRead("Original");

            assertThat(transformer.events).contains("Transformer:channelRead:Original");
            assertThat(receiver.events).contains("Receiver:channelRead:Transformed-Original");
        }
    }

    @Nested
    @DisplayName("验收场景测试")
    class AcceptanceScenarioTests {

        @Test
        @DisplayName("典型的消息处理 Pipeline 场景")
        void typicalMessageProcessingScenario() {
            // 模拟 Decoder -> Business Handler -> Logger
            RecordingHandler decoder = new RecordingHandler("Decoder") {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    events.add(name + ":decode:" + msg);
                    // 模拟解码：将消息转换为大写
                    ctx.fireChannelRead(msg.toString().toUpperCase());
                }
            };
            
            RecordingHandler businessHandler = new RecordingHandler("Business") {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    events.add(name + ":process:" + msg);
                    // 业务处理
                    ctx.fireChannelRead(msg);
                }
            };
            
            RecordingHandler logger = new RecordingHandler("Logger") {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    events.add(name + ":log:" + msg);
                    super.channelRead(ctx, msg);
                }
            };
            
            pipeline.addLast("decoder", decoder);
            pipeline.addLast("business", businessHandler);
            pipeline.addLast("logger", logger);
            
            decoder.events.clear();
            businessHandler.events.clear();
            logger.events.clear();

            // 触发入站消息
            pipeline.fireChannelRead("hello");

            // 验证消息处理流程
            assertThat(decoder.events).contains("Decoder:decode:hello");
            assertThat(businessHandler.events).contains("Business:process:HELLO");
            assertThat(logger.events).contains("Logger:log:HELLO");
        }

        @Test
        @DisplayName("完整的 Channel 生命周期事件")
        void completeChannelLifecycleEvents() {
            RecordingHandler handler = new RecordingHandler("Lifecycle");
            pipeline.addLast("handler", handler);
            handler.events.clear();

            // 模拟 Channel 生命周期
            pipeline.fireChannelRegistered();
            pipeline.fireChannelActive();
            pipeline.fireChannelRead("Data1");
            pipeline.fireChannelRead("Data2");
            pipeline.fireChannelReadComplete();
            pipeline.fireChannelInactive();
            pipeline.fireChannelUnregistered();

            // 验证所有事件按顺序接收
            assertThat(handler.events).containsExactly(
                    "Lifecycle:channelRegistered",
                    "Lifecycle:channelActive",
                    "Lifecycle:channelRead:Data1",
                    "Lifecycle:channelRead:Data2",
                    "Lifecycle:channelReadComplete",
                    "Lifecycle:channelInactive",
                    "Lifecycle:channelUnregistered"
            );
        }
    }
}
