package io.netty.channel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;

/**
 * ChannelPipeline 测试
 *
 * <p>测试 Pipeline 的双向链表结构和事件传递机制。
 */
@DisplayName("ChannelPipeline 测试")
class ChannelPipelineTest {

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
            ctx.fireChannelRegistered();
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) {
            events.add(name + ":channelUnregistered");
            ctx.fireChannelUnregistered();
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            events.add(name + ":channelActive");
            ctx.fireChannelActive();
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            events.add(name + ":channelInactive");
            ctx.fireChannelInactive();
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            events.add(name + ":channelRead:" + msg);
            ctx.fireChannelRead(msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            events.add(name + ":channelReadComplete");
            ctx.fireChannelReadComplete();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            events.add(name + ":exceptionCaught:" + cause.getMessage());
            ctx.fireExceptionCaught(cause);
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
    @DisplayName("Pipeline 结构测试")
    class PipelineStructureTests {

        @Test
        @DisplayName("Pipeline 应关联到 Channel")
        void pipelineShouldBeAssociatedWithChannel() {
            assertThat(pipeline.channel()).isSameAs(channel);
        }

        @Test
        @DisplayName("新创建的 Pipeline 应只有 Head 和 Tail")
        void newPipelineShouldOnlyHaveHeadAndTail() {
            assertThat(pipeline.names()).isEmpty();
        }

        @Test
        @DisplayName("addLast 应在尾部添加 Handler")
        void addLastShouldAddHandlerAtTail() {
            RecordingHandler handler1 = new RecordingHandler("H1");
            RecordingHandler handler2 = new RecordingHandler("H2");

            pipeline.addLast("handler1", handler1);
            pipeline.addLast("handler2", handler2);

            assertThat(pipeline.names()).containsExactly("handler1", "handler2");
        }

        @Test
        @DisplayName("addFirst 应在头部添加 Handler")
        void addFirstShouldAddHandlerAtHead() {
            RecordingHandler handler1 = new RecordingHandler("H1");
            RecordingHandler handler2 = new RecordingHandler("H2");

            pipeline.addFirst("handler1", handler1);
            pipeline.addFirst("handler2", handler2);

            assertThat(pipeline.names()).containsExactly("handler2", "handler1");
        }

        @Test
        @DisplayName("重复名称应抛出异常")
        void duplicateNameShouldThrowException() {
            RecordingHandler handler = new RecordingHandler("H");
            pipeline.addLast("handler", handler);

            assertThatThrownBy(() -> pipeline.addLast("handler", new RecordingHandler("H2")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("已存在");
        }
    }

    @Nested
    @DisplayName("Handler 管理测试")
    class HandlerManagementTests {

        @Test
        @DisplayName("get 应返回指定名称的 Handler")
        void getShouldReturnHandlerByName() {
            RecordingHandler handler = new RecordingHandler("H");
            pipeline.addLast("handler", handler);

            assertThat(pipeline.get("handler")).isSameAs(handler);
            assertThat(pipeline.get("nonexistent")).isNull();
        }

        @Test
        @DisplayName("context 应返回指定名称的 Context")
        void contextShouldReturnContextByName() {
            RecordingHandler handler = new RecordingHandler("H");
            pipeline.addLast("handler", handler);

            ChannelHandlerContext ctx = pipeline.context("handler");
            assertThat(ctx).isNotNull();
            assertThat(ctx.name()).isEqualTo("handler");
            assertThat(ctx.handler()).isSameAs(handler);
        }

        @Test
        @DisplayName("context(ChannelHandler) 应返回 Handler 的 Context")
        void contextShouldReturnContextByHandler() {
            RecordingHandler handler = new RecordingHandler("H");
            pipeline.addLast("handler", handler);

            ChannelHandlerContext ctx = pipeline.context(handler);
            assertThat(ctx).isNotNull();
            assertThat(ctx.handler()).isSameAs(handler);
        }

        @Test
        @DisplayName("remove(Handler) 应移除 Handler")
        void removeShouldRemoveHandler() {
            RecordingHandler handler = new RecordingHandler("H");
            pipeline.addLast("handler", handler);
            
            pipeline.remove(handler);
            
            assertThat(pipeline.names()).isEmpty();
            assertThat(pipeline.get("handler")).isNull();
        }

        @Test
        @DisplayName("remove(name) 应返回被移除的 Handler")
        void removeByNameShouldReturnRemovedHandler() {
            RecordingHandler handler = new RecordingHandler("H");
            pipeline.addLast("handler", handler);
            
            ChannelHandler removed = pipeline.remove("handler");
            
            assertThat(removed).isSameAs(handler);
            assertThat(pipeline.names()).isEmpty();
        }

        @Test
        @DisplayName("remove 不存在的 Handler 应抛出异常")
        void removeNonexistentHandlerShouldThrow() {
            RecordingHandler handler = new RecordingHandler("H");
            
            assertThatThrownBy(() -> pipeline.remove(handler))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("remove 不存在的名称应抛出异常")
        void removeNonexistentNameShouldThrow() {
            assertThatThrownBy(() -> pipeline.remove("nonexistent"))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("Handler 生命周期测试")
    class HandlerLifecycleTests {

        @Test
        @DisplayName("添加 Handler 时应调用 handlerAdded")
        void shouldCallHandlerAddedWhenAdding() {
            RecordingHandler handler = new RecordingHandler("H");
            
            pipeline.addLast("handler", handler);
            
            assertThat(handler.events).contains("H:handlerAdded");
        }

        @Test
        @DisplayName("移除 Handler 时应调用 handlerRemoved")
        void shouldCallHandlerRemovedWhenRemoving() {
            RecordingHandler handler = new RecordingHandler("H");
            pipeline.addLast("handler", handler);
            handler.events.clear();
            
            pipeline.remove(handler);
            
            assertThat(handler.events).contains("H:handlerRemoved");
        }
    }

    @Nested
    @DisplayName("入站事件传递测试")
    class InboundEventPropagationTests {

        @Test
        @DisplayName("fireChannelRegistered 应按顺序传递事件")
        void fireChannelRegisteredShouldPropagate() {
            RecordingHandler handler1 = new RecordingHandler("H1");
            RecordingHandler handler2 = new RecordingHandler("H2");
            pipeline.addLast("h1", handler1);
            pipeline.addLast("h2", handler2);
            handler1.events.clear();
            handler2.events.clear();

            pipeline.fireChannelRegistered();

            assertThat(handler1.events).contains("H1:channelRegistered");
            assertThat(handler2.events).contains("H2:channelRegistered");
        }

        @Test
        @DisplayName("fireChannelActive 应按顺序传递事件")
        void fireChannelActiveShouldPropagate() {
            RecordingHandler handler1 = new RecordingHandler("H1");
            RecordingHandler handler2 = new RecordingHandler("H2");
            pipeline.addLast("h1", handler1);
            pipeline.addLast("h2", handler2);
            handler1.events.clear();
            handler2.events.clear();

            pipeline.fireChannelActive();

            assertThat(handler1.events).contains("H1:channelActive");
            assertThat(handler2.events).contains("H2:channelActive");
        }

        @Test
        @DisplayName("fireChannelRead 应传递消息")
        void fireChannelReadShouldPropagateMessage() {
            RecordingHandler handler1 = new RecordingHandler("H1");
            RecordingHandler handler2 = new RecordingHandler("H2");
            pipeline.addLast("h1", handler1);
            pipeline.addLast("h2", handler2);
            handler1.events.clear();
            handler2.events.clear();

            pipeline.fireChannelRead("TestMessage");

            assertThat(handler1.events).contains("H1:channelRead:TestMessage");
            assertThat(handler2.events).contains("H2:channelRead:TestMessage");
        }

        @Test
        @DisplayName("fireExceptionCaught 应传递异常")
        void fireExceptionCaughtShouldPropagateException() {
            RecordingHandler handler = new RecordingHandler("H");
            pipeline.addLast("h", handler);
            handler.events.clear();

            pipeline.fireExceptionCaught(new RuntimeException("TestError"));

            assertThat(handler.events).contains("H:exceptionCaught:TestError");
        }
    }

    @Nested
    @DisplayName("验收场景测试")
    class AcceptanceScenarioTests {

        @Test
        @DisplayName("完整的 Pipeline 操作场景")
        void completePipelineScenario() {
            // 1. 添加多个 Handler
            RecordingHandler decoder = new RecordingHandler("Decoder");
            RecordingHandler handler = new RecordingHandler("Handler");
            RecordingHandler encoder = new RecordingHandler("Encoder");
            
            pipeline.addLast("decoder", decoder);
            pipeline.addLast("handler", handler);
            pipeline.addLast("encoder", encoder);
            
            assertThat(pipeline.names()).containsExactly("decoder", "handler", "encoder");
            
            // 2. 清空事件记录
            decoder.events.clear();
            handler.events.clear();
            encoder.events.clear();
            
            // 3. 触发事件并验证传递顺序
            pipeline.fireChannelActive();
            
            assertThat(decoder.events).containsExactly("Decoder:channelActive");
            assertThat(handler.events).containsExactly("Handler:channelActive");
            assertThat(encoder.events).containsExactly("Encoder:channelActive");
            
            // 4. 移除中间 Handler
            pipeline.remove(handler);
            
            assertThat(pipeline.names()).containsExactly("decoder", "encoder");
            
            // 5. 再次触发事件验证链表正确性
            decoder.events.clear();
            encoder.events.clear();
            
            pipeline.fireChannelRead("Data");
            
            assertThat(decoder.events).contains("Decoder:channelRead:Data");
            assertThat(encoder.events).contains("Encoder:channelRead:Data");
        }

        @Test
        @DisplayName("动态添加和移除 Handler 场景")
        void dynamicHandlerManagement() {
            // 初始添加
            RecordingHandler h1 = new RecordingHandler("H1");
            pipeline.addLast("h1", h1);
            assertThat(pipeline.names()).containsExactly("h1");
            
            // 在头部添加
            RecordingHandler h0 = new RecordingHandler("H0");
            pipeline.addFirst("h0", h0);
            assertThat(pipeline.names()).containsExactly("h0", "h1");
            
            // 在尾部添加
            RecordingHandler h2 = new RecordingHandler("H2");
            pipeline.addLast("h2", h2);
            assertThat(pipeline.names()).containsExactly("h0", "h1", "h2");
            
            // 按名称移除
            pipeline.remove("h1");
            assertThat(pipeline.names()).containsExactly("h0", "h2");
            
            // 按 Handler 移除
            pipeline.remove(h0);
            assertThat(pipeline.names()).containsExactly("h2");
            
            // 清空
            pipeline.remove(h2);
            assertThat(pipeline.names()).isEmpty();
        }
    }
}
