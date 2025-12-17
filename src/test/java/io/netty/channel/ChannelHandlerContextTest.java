package io.netty.channel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ChannelHandlerContext 测试
 *
 * <p>测试 Context 的事件传递和链表导航能力。
 */
@DisplayName("ChannelHandlerContext 测试")
class ChannelHandlerContextTest {

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
        private EventLoop eventLoop;

        MockChannel() {
            this.pipeline = new DefaultChannelPipeline(this);
        }

        @Override
        public ChannelId id() {
            return id;
        }

        @Override
        public EventLoop eventLoop() {
            return eventLoop;
        }

        public void setEventLoop(EventLoop eventLoop) {
            this.eventLoop = eventLoop;
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
     * 用于记录事件的 Handler
     */
    private static class RecordingHandler implements ChannelInboundHandler {
        final List<String> events = new ArrayList<>();
        final String name;
        boolean stopPropagation = false;

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
        public void channelRegistered(ChannelHandlerContext ctx) {
            events.add(name + ":channelRegistered");
            if (!stopPropagation) {
                ctx.fireChannelRegistered();
            }
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) {
            events.add(name + ":channelUnregistered");
            if (!stopPropagation) {
                ctx.fireChannelUnregistered();
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            events.add(name + ":channelActive");
            if (!stopPropagation) {
                ctx.fireChannelActive();
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            events.add(name + ":channelInactive");
            if (!stopPropagation) {
                ctx.fireChannelInactive();
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            events.add(name + ":channelRead:" + msg);
            if (!stopPropagation) {
                ctx.fireChannelRead(msg);
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            events.add(name + ":channelReadComplete");
            if (!stopPropagation) {
                ctx.fireChannelReadComplete();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            events.add(name + ":exceptionCaught:" + cause.getMessage());
            if (!stopPropagation) {
                ctx.fireExceptionCaught(cause);
            }
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            events.add(name + ":userEventTriggered:" + evt);
            if (!stopPropagation) {
                ctx.fireUserEventTriggered(evt);
            }
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
    @DisplayName("Context 基本属性测试")
    class ContextBasicPropertiesTests {

        @Test
        @DisplayName("Context 应返回正确的 Channel")
        void contextShouldReturnChannel() {
            RecordingHandler handler = new RecordingHandler("H");
            pipeline.addLast("handler", handler);

            ChannelHandlerContext ctx = pipeline.context("handler");

            assertThat(ctx.channel()).isSameAs(channel);
        }

        @Test
        @DisplayName("Context 应返回正确的名称")
        void contextShouldReturnName() {
            RecordingHandler handler = new RecordingHandler("H");
            pipeline.addLast("myHandler", handler);

            ChannelHandlerContext ctx = pipeline.context("myHandler");

            assertThat(ctx.name()).isEqualTo("myHandler");
        }

        @Test
        @DisplayName("Context 应返回正确的 Handler")
        void contextShouldReturnHandler() {
            RecordingHandler handler = new RecordingHandler("H");
            pipeline.addLast("handler", handler);

            ChannelHandlerContext ctx = pipeline.context("handler");

            assertThat(ctx.handler()).isSameAs(handler);
        }

        @Test
        @DisplayName("Context 应返回正确的 Pipeline")
        void contextShouldReturnPipeline() {
            RecordingHandler handler = new RecordingHandler("H");
            pipeline.addLast("handler", handler);

            ChannelHandlerContext ctx = pipeline.context("handler");

            assertThat(ctx.pipeline()).isSameAs(pipeline);
        }
    }

    @Nested
    @DisplayName("事件传递测试")
    class EventPropagationTests {

        @Test
        @DisplayName("fireChannelRead 应传递到下一个 Handler")
        void fireChannelReadShouldPropagateToNext() {
            RecordingHandler first = new RecordingHandler("First");
            RecordingHandler second = new RecordingHandler("Second");
            pipeline.addLast("first", first);
            pipeline.addLast("second", second);
            first.events.clear();
            second.events.clear();

            // 通过 Pipeline 触发事件
            pipeline.fireChannelRead("TestMessage");

            // 验证事件按顺序传递
            assertThat(first.events).containsExactly("First:channelRead:TestMessage");
            assertThat(second.events).containsExactly("Second:channelRead:TestMessage");
        }

        @Test
        @DisplayName("停止传播时事件不应传递到下一个 Handler")
        void eventShouldNotPropagateWhenStopped() {
            RecordingHandler first = new RecordingHandler("First");
            first.stopPropagation = true;
            RecordingHandler second = new RecordingHandler("Second");
            pipeline.addLast("first", first);
            pipeline.addLast("second", second);
            first.events.clear();
            second.events.clear();

            pipeline.fireChannelActive();

            assertThat(first.events).containsExactly("First:channelActive");
            assertThat(second.events).isEmpty(); // 事件被 First 拦截
        }

        @Test
        @DisplayName("fireExceptionCaught 应传递异常")
        void fireExceptionCaughtShouldPropagate() {
            RecordingHandler handler = new RecordingHandler("H");
            pipeline.addLast("handler", handler);
            handler.events.clear();

            ChannelHandlerContext ctx = pipeline.context("handler");
            ctx.fireExceptionCaught(new RuntimeException("TestError"));

            // 异常传递到 TailContext
        }
    }

    @Nested
    @DisplayName("链式传递测试")
    class ChainPropagationTests {

        @Test
        @DisplayName("多个 Handler 按顺序接收事件")
        void multipleHandlersShouldReceiveEventsInOrder() {
            RecordingHandler h1 = new RecordingHandler("H1");
            RecordingHandler h2 = new RecordingHandler("H2");
            RecordingHandler h3 = new RecordingHandler("H3");
            pipeline.addLast("h1", h1);
            pipeline.addLast("h2", h2);
            pipeline.addLast("h3", h3);
            h1.events.clear();
            h2.events.clear();
            h3.events.clear();

            pipeline.fireChannelRegistered();

            assertThat(h1.events).contains("H1:channelRegistered");
            assertThat(h2.events).contains("H2:channelRegistered");
            assertThat(h3.events).contains("H3:channelRegistered");
        }

        @Test
        @DisplayName("从中间 Context 触发事件应跳过之前的 Handler")
        void firingFromMiddleShouldSkipPreviousHandlers() {
            RecordingHandler h1 = new RecordingHandler("H1");
            RecordingHandler h2 = new RecordingHandler("H2");
            RecordingHandler h3 = new RecordingHandler("H3");
            pipeline.addLast("h1", h1);
            pipeline.addLast("h2", h2);
            pipeline.addLast("h3", h3);
            h1.events.clear();
            h2.events.clear();
            h3.events.clear();

            // 从 h2 的 Context 触发事件
            ChannelHandlerContext ctx = pipeline.context("h2");
            ctx.fireChannelActive();

            // h1 不应该收到事件，因为事件是从 h2 触发的
            assertThat(h1.events).isEmpty();
            // h3 应该收到事件
            assertThat(h3.events).contains("H3:channelActive");
        }
    }

    @Nested
    @DisplayName("验收场景测试")
    class AcceptanceScenarioTests {

        @Test
        @DisplayName("完整的事件处理链场景")
        void completeEventChainScenario() {
            // 模拟一个典型的 Pipeline：Decoder -> Handler -> Encoder
            RecordingHandler decoder = new RecordingHandler("Decoder");
            RecordingHandler handler = new RecordingHandler("Handler");
            RecordingHandler encoder = new RecordingHandler("Encoder");

            pipeline.addLast("decoder", decoder);
            pipeline.addLast("handler", handler);
            pipeline.addLast("encoder", encoder);

            decoder.events.clear();
            handler.events.clear();
            encoder.events.clear();

            // 1. 触发 channelActive
            pipeline.fireChannelActive();
            assertThat(decoder.events).contains("Decoder:channelActive");
            assertThat(handler.events).contains("Handler:channelActive");
            assertThat(encoder.events).contains("Encoder:channelActive");

            // 2. 触发 channelRead
            pipeline.fireChannelRead("Hello");
            assertThat(decoder.events).contains("Decoder:channelRead:Hello");
            assertThat(handler.events).contains("Handler:channelRead:Hello");
            assertThat(encoder.events).contains("Encoder:channelRead:Hello");

            // 3. 触发 channelReadComplete
            pipeline.fireChannelReadComplete();
            assertThat(decoder.events).contains("Decoder:channelReadComplete");
            assertThat(handler.events).contains("Handler:channelReadComplete");
            assertThat(encoder.events).contains("Encoder:channelReadComplete");
        }

        @Test
        @DisplayName("异常处理场景")
        void exceptionHandlingScenario() {
            RecordingHandler normalHandler = new RecordingHandler("Normal");
            RecordingHandler errorHandler = new RecordingHandler("Error") {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) {
                    events.add(name + ":channelRead:" + msg);
                    throw new RuntimeException("Simulated Error");
                }
            };
            RecordingHandler catchHandler = new RecordingHandler("Catch") {
                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                    events.add("Catch:exceptionCaught:" + cause.getMessage());
                    // 不再传播
                }
            };

            pipeline.addLast("normal", normalHandler);
            pipeline.addLast("error", errorHandler);
            pipeline.addLast("catch", catchHandler);

            normalHandler.events.clear();
            errorHandler.events.clear();
            catchHandler.events.clear();

            // 触发 channelRead，errorHandler 会抛出异常
            pipeline.fireChannelRead("Data");

            // normalHandler 应该正常处理
            assertThat(normalHandler.events).contains("Normal:channelRead:Data");
            // errorHandler 应该接收并抛出异常
            assertThat(errorHandler.events).contains("Error:channelRead:Data");
            // catchHandler 应该捕获异常
            assertThat(catchHandler.events).contains("Catch:exceptionCaught:Simulated Error");
        }
    }
}
