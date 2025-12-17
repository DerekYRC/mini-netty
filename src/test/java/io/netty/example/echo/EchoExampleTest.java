package io.netty.example.echo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Echo 示例测试
 *
 * <p>测试 Echo 服务端和客户端处理器
 */
@DisplayName("Echo 示例测试")
class EchoExampleTest {

    @Nested
    @DisplayName("EchoServerHandler 测试")
    class EchoServerHandlerTests {

        private MockChannel channel;
        private EchoServerHandler handler;
        private RecordingOutboundHandler recorder;

        @BeforeEach
        void setUp() {
            channel = new MockChannel();
            handler = new EchoServerHandler();
            recorder = new RecordingOutboundHandler(channel);
            // 先添加记录器，再添加业务handler
            channel.pipeline().addLast("recorder", recorder);
            channel.pipeline().addLast("echo", handler);
        }

        @Test
        @DisplayName("接收消息后回写")
        void writesBackReceivedMessage() throws Exception {
            ByteBuf buf = UnpooledByteBufAllocator.DEFAULT.buffer();
            buf.writeBytes("Hello".getBytes(StandardCharsets.UTF_8));

            ChannelHandlerContext ctx = channel.pipeline().context("echo");
            handler.channelRead(ctx, buf);

            // 消息应该被写入
            assertThat(recorder.writtenMessages).hasSize(1);
        }

        @Test
        @DisplayName("channelReadComplete 触发 flush")
        void flushesOnChannelReadComplete() throws Exception {
            ChannelHandlerContext ctx = channel.pipeline().context("echo");
            handler.channelReadComplete(ctx);

            assertThat(recorder.flushCount).isGreaterThan(0);
        }

        @Test
        @DisplayName("异常时关闭连接")
        void closesOnException() throws Exception {
            ChannelHandlerContext ctx = channel.pipeline().context("echo");
            handler.exceptionCaught(ctx, new RuntimeException("test error"));

            assertThat(recorder.closeCalled).isTrue();
        }

        @Test
        @DisplayName("连接建立时打印日志")
        void logsOnChannelActive() throws Exception {
            ChannelHandlerContext ctx = channel.pipeline().context("echo");
            handler.channelActive(ctx);
            // 只验证不抛异常，日志输出到控制台
        }

        @Test
        @DisplayName("连接断开时打印日志")
        void logsOnChannelInactive() throws Exception {
            ChannelHandlerContext ctx = channel.pipeline().context("echo");
            handler.channelInactive(ctx);
            // 只验证不抛异常，日志输出到控制台
        }
    }

    @Nested
    @DisplayName("EchoClientHandler 测试")
    class EchoClientHandlerTests {

        private MockChannel channel;
        private EchoClientHandler handler;
        private RecordingOutboundHandler recorder;

        @BeforeEach
        void setUp() {
            channel = new MockChannel();
            handler = new EchoClientHandler("Hello, World!");
            recorder = new RecordingOutboundHandler(channel);
            channel.pipeline().addLast("recorder", recorder);
            channel.pipeline().addLast("client", handler);
        }

        @Test
        @DisplayName("连接建立时发送消息")
        void sendsMessageOnActive() throws Exception {
            ChannelHandlerContext ctx = channel.pipeline().context("client");
            handler.channelActive(ctx);

            assertThat(recorder.writtenMessages).hasSize(1);
            assertThat(recorder.flushCount).isGreaterThan(0);
        }

        @Test
        @DisplayName("接收回显消息")
        void receivesEchoMessage() throws Exception {
            ByteBuf buf = UnpooledByteBufAllocator.DEFAULT.buffer();
            buf.writeBytes("Response".getBytes(StandardCharsets.UTF_8));

            ChannelHandlerContext ctx = channel.pipeline().context("client");
            handler.channelRead(ctx, buf);
            // 只验证不抛异常
        }

        @Test
        @DisplayName("异常时关闭连接")
        void closesOnException() throws Exception {
            ChannelHandlerContext ctx = channel.pipeline().context("client");
            handler.exceptionCaught(ctx, new RuntimeException("test error"));

            assertThat(recorder.closeCalled).isTrue();
        }
    }

    @Nested
    @DisplayName("验收场景测试")
    class AcceptanceScenarioTests {

        @Test
        @DisplayName("场景: 完整的 Echo 流程")
        void scenarioCompleteEchoFlow() throws Exception {
            // Given: 服务端 Handler
            EchoServerHandler serverHandler = new EchoServerHandler();
            MockChannel serverChannel = new MockChannel();
            RecordingOutboundHandler recorder = new RecordingOutboundHandler(serverChannel);
            serverChannel.pipeline().addLast("recorder", recorder);
            serverChannel.pipeline().addLast("echo", serverHandler);

            // When: 模拟客户端发送消息
            ByteBuf request = UnpooledByteBufAllocator.DEFAULT.buffer();
            request.writeBytes("Hello, Netty!".getBytes(StandardCharsets.UTF_8));

            ChannelHandlerContext ctx = serverChannel.pipeline().context("echo");
            serverHandler.channelRead(ctx, request);
            serverHandler.channelReadComplete(ctx);

            // Then: 消息被回写并刷新
            assertThat(recorder.writtenMessages).hasSize(1);
            assertThat(recorder.flushCount).isGreaterThan(0);
        }

        @Test
        @DisplayName("场景: 客户端发送自定义消息")
        void scenarioClientSendsCustomMessage() throws Exception {
            // Given: 带自定义消息的客户端 Handler
            String customMessage = "Custom Echo Message";
            EchoClientHandler clientHandler = new EchoClientHandler(customMessage);
            MockChannel clientChannel = new MockChannel();
            RecordingOutboundHandler recorder = new RecordingOutboundHandler(clientChannel);
            clientChannel.pipeline().addLast("recorder", recorder);
            clientChannel.pipeline().addLast("client", clientHandler);

            // When: 连接建立
            ChannelHandlerContext ctx = clientChannel.pipeline().context("client");
            clientHandler.channelActive(ctx);

            // Then: 自定义消息被发送
            assertThat(recorder.writtenMessages).hasSize(1);
        }
    }

    // ========== 辅助类 ==========

    /**
     * 简单的 ChannelId 实现
     */
    private static class SimpleChannelId implements ChannelId {
        private static int counter = 0;
        private final String id = "echo-test-" + (++counter);

        @Override
        public String asShortText() {
            return id;
        }

        @Override
        public String asLongText() {
            return "echo-test-channel-" + id;
        }

        @Override
        public int compareTo(ChannelId o) {
            return asLongText().compareTo(o.asLongText());
        }
    }

    /**
     * 记录出站操作的 Handler
     * 用于捕获 write、flush、close 调用
     */
    private static class RecordingOutboundHandler extends ChannelOutboundHandlerAdapter {
        final List<Object> writtenMessages = new ArrayList<>();
        int flushCount = 0;
        boolean closeCalled = false;
        private final MockChannel channel;

        RecordingOutboundHandler(MockChannel channel) {
            this.channel = channel;
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            writtenMessages.add(msg);
            // 不继续传播，终止在这里
        }

        @Override
        public void flush(ChannelHandlerContext ctx) throws Exception {
            flushCount++;
            // 不继续传播
        }

        @Override
        public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            closeCalled = true;
            channel.markClosed();
            // 不继续传播
        }
    }

    /**
     * 模拟 Channel
     */
    private static class MockChannel implements Channel {
        private final ChannelPipeline pipeline;
        private final ChannelId id = new SimpleChannelId();
        private boolean closed = false;

        MockChannel() {
            this.pipeline = new DefaultChannelPipeline(this);
        }

        void markClosed() {
            closed = true;
        }

        @Override
        public ChannelPipeline pipeline() {
            return pipeline;
        }

        @Override
        public ChannelConfig config() {
            return null;
        }

        @Override
        public boolean isOpen() {
            return !closed;
        }

        @Override
        public boolean isActive() {
            return !closed;
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
        public boolean isRegistered() {
            return true;
        }

        @Override
        public ChannelFuture close() {
            closed = true;
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
}
