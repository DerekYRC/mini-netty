package io.netty.integration;

import io.netty.channel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Pipeline 集成测试
 *
 * <p>测试完整的入站出站事件处理流程和异常传播机制。
 */
@DisplayName("Pipeline 集成测试")
class PipelineIntegrationTest {

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
     * 记录事件的双向 Handler
     */
    private static class RecordingDuplexHandler extends ChannelDuplexHandler {
        final List<String> events = new ArrayList<>();
        final String name;

        RecordingDuplexHandler(String name) {
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
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            events.add(name + ":channelActive");
            super.channelActive(ctx);
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
    @DisplayName("ChannelDuplexHandler 测试")
    class DuplexHandlerTests {

        @Test
        @DisplayName("DuplexHandler 应同时实现入站和出站接口")
        void duplexHandlerShouldImplementBothInterfaces() {
            ChannelDuplexHandler handler = new ChannelDuplexHandler();

            assertThat(handler).isInstanceOf(ChannelInboundHandler.class);
            assertThat(handler).isInstanceOf(ChannelOutboundHandler.class);
        }

        @Test
        @DisplayName("DuplexHandler 应记录入站和出站事件")
        void duplexHandlerShouldRecordBothEvents() {
            RecordingDuplexHandler handler = new RecordingDuplexHandler("Duplex");
            pipeline.addLast("duplex", handler);
            handler.events.clear();

            // 触发入站事件
            pipeline.fireChannelActive();
            pipeline.fireChannelRead("InboundMessage");

            assertThat(handler.events)
                    .contains("Duplex:channelActive", "Duplex:channelRead:InboundMessage");
        }

        @Test
        @DisplayName("DuplexHandler 可以在 Pipeline 中正常工作")
        void duplexHandlerShouldWorkInPipeline() {
            RecordingDuplexHandler handler = new RecordingDuplexHandler("H");
            pipeline.addLast("handler", handler);

            assertThat(pipeline.get("handler")).isSameAs(handler);
            assertThat(handler.events).contains("H:handlerAdded");
        }
    }

    @Nested
    @DisplayName("异常传播测试")
    class ExceptionPropagationTests {

        @Test
        @DisplayName("异常应通过 exceptionCaught 传播")
        void exceptionShouldPropagateThroughExceptionCaught() {
            RecordingDuplexHandler handler1 = new RecordingDuplexHandler("H1");
            RecordingDuplexHandler handler2 = new RecordingDuplexHandler("H2");
            
            pipeline.addLast("h1", handler1);
            pipeline.addLast("h2", handler2);
            handler1.events.clear();
            handler2.events.clear();

            // 触发异常
            Exception testException = new RuntimeException("Test Error");
            pipeline.fireExceptionCaught(testException);

            // 异常应该按入站顺序传播
            assertThat(handler1.events).contains("H1:exceptionCaught:Test Error");
            assertThat(handler2.events).contains("H2:exceptionCaught:Test Error");
        }

        @Test
        @DisplayName("异常可以在处理器中被拦截")
        void exceptionCanBeIntercepted() {
            List<String> interceptedErrors = new ArrayList<>();
            
            ChannelInboundHandlerAdapter interceptor = new ChannelInboundHandlerAdapter() {
                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                    interceptedErrors.add(cause.getMessage());
                    // 不调用 super，停止传播
                }
            };

            RecordingDuplexHandler afterHandler = new RecordingDuplexHandler("After");
            
            pipeline.addLast("interceptor", interceptor);
            pipeline.addLast("after", afterHandler);
            afterHandler.events.clear();

            pipeline.fireExceptionCaught(new RuntimeException("Intercepted"));

            assertThat(interceptedErrors).contains("Intercepted");
            // After handler 不应该收到异常
            assertThat(afterHandler.events)
                    .filteredOn(e -> e.contains("exceptionCaught"))
                    .isEmpty();
        }
    }

    @Nested
    @DisplayName("完整事件流测试")
    class FullEventFlowTests {

        @Test
        @DisplayName("典型的请求响应场景")
        void typicalRequestResponseScenario() {
            // 设置日志 Handler
            RecordingDuplexHandler logger = new RecordingDuplexHandler("Logger");
            
            // 设置业务 Handler
            ChannelInboundHandlerAdapter businessHandler = new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) {
                    // 处理请求并发送响应
                    String request = (String) msg;
                    String response = "Response to: " + request;
                    ctx.write(response);
                }
            };

            pipeline.addLast("logger", logger);
            pipeline.addLast("business", businessHandler);
            logger.events.clear();

            // 模拟收到请求
            pipeline.fireChannelRead("Hello");

            // 验证入站事件被记录
            assertThat(logger.events).contains("Logger:channelRead:Hello");
        }

        @Test
        @DisplayName("多个双向 Handler 的事件传递顺序")
        void multipleHandlersEventOrder() {
            RecordingDuplexHandler h1 = new RecordingDuplexHandler("H1");
            RecordingDuplexHandler h2 = new RecordingDuplexHandler("H2");
            RecordingDuplexHandler h3 = new RecordingDuplexHandler("H3");

            pipeline.addLast("h1", h1);
            pipeline.addLast("h2", h2);
            pipeline.addLast("h3", h3);
            
            h1.events.clear();
            h2.events.clear();
            h3.events.clear();

            // 触发入站事件
            pipeline.fireChannelRead("Message");

            // 入站事件顺序: H1 -> H2 -> H3
            assertThat(h1.events).contains("H1:channelRead:Message");
            assertThat(h2.events).contains("H2:channelRead:Message");
            assertThat(h3.events).contains("H3:channelRead:Message");
        }
    }

    @Nested
    @DisplayName("验收场景测试")
    class AcceptanceScenarioTests {

        @Test
        @DisplayName("完整的 Echo 服务器 Handler 链")
        void completeEchoServerHandlerChain() {
            List<String> processedMessages = new ArrayList<>();
            
            // 日志 Handler (双向)
            ChannelDuplexHandler loggingHandler = new ChannelDuplexHandler() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    processedMessages.add("LOG_IN:" + msg);
                    super.channelRead(ctx, msg);
                }

                @Override
                public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                    processedMessages.add("LOG_OUT:" + msg);
                    super.write(ctx, msg, promise);
                }
            };

            // 业务 Handler (入站)
            ChannelInboundHandlerAdapter echoHandler = new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) {
                    processedMessages.add("ECHO:" + msg);
                    // Echo back
                    ctx.write("Echo: " + msg);
                }
            };

            pipeline.addLast("logger", loggingHandler);
            pipeline.addLast("echo", echoHandler);

            // 模拟收到消息
            pipeline.fireChannelRead("Hello World");

            // 验证处理顺序
            assertThat(processedMessages)
                    .containsExactly(
                            "LOG_IN:Hello World",
                            "ECHO:Hello World",
                            "LOG_OUT:Echo: Hello World"
                    );
        }

        @Test
        @DisplayName("异常处理链场景")
        void exceptionHandlingChainScenario() {
            List<String> errorLog = new ArrayList<>();
            
            // 可能抛出异常的 Handler
            ChannelInboundHandlerAdapter riskyHandler = new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    if ("error".equals(msg)) {
                        throw new RuntimeException("Simulated error");
                    }
                    ctx.fireChannelRead(msg);
                }

                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                    errorLog.add("Risky caught: " + cause.getMessage());
                    ctx.fireExceptionCaught(cause);
                }
            };

            // 最终异常处理器
            ChannelInboundHandlerAdapter finalHandler = new ChannelInboundHandlerAdapter() {
                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                    errorLog.add("Final caught: " + cause.getMessage());
                    // 不再传播
                }
            };

            pipeline.addLast("risky", riskyHandler);
            pipeline.addLast("final", finalHandler);

            // 触发异常
            pipeline.fireExceptionCaught(new RuntimeException("Test exception"));

            // 验证异常被正确处理
            assertThat(errorLog)
                    .contains("Risky caught: Test exception", "Final caught: Test exception");
        }
    }
}
