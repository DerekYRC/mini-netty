package io.netty.channel.nio;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NIO Channel 实现测试
 *
 * <p>测试 NioServerSocketChannel 和 NioSocketChannel 的基本功能
 */
@DisplayName("NIO Channel 实现测试")
class NioChannelTest {

    private NioEventLoop serverEventLoop;
    private NioEventLoop clientEventLoop;

    @BeforeEach
    void setUp() {
        serverEventLoop = new NioEventLoop(null);
        clientEventLoop = new NioEventLoop(null);
        serverEventLoop.start();
        clientEventLoop.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        if (serverEventLoop != null) {
            serverEventLoop.shutdownGracefully();
        }
        if (clientEventLoop != null) {
            clientEventLoop.shutdownGracefully();
        }
        Thread.sleep(200);
    }

    @Nested
    @DisplayName("NioServerSocketChannel 测试")
    class ServerChannelTests {

        @Test
        @DisplayName("创建 NioServerSocketChannel")
        void createsServerChannel() {
            NioServerSocketChannel serverChannel = new NioServerSocketChannel();
            
            assertThat(serverChannel).isNotNull();
            assertThat(serverChannel.isOpen()).isTrue();
            assertThat(serverChannel.isActive()).isFalse(); // 未绑定
            assertThat(serverChannel.id()).isNotNull();
            assertThat(serverChannel.pipeline()).isNotNull();
            
            serverChannel.close();
        }

        @Test
        @DisplayName("绑定到端口")
        void bindsToPort() {
            NioServerSocketChannel serverChannel = new NioServerSocketChannel();
            
            ChannelFuture future = serverChannel.bind(0); // 随机端口
            assertThat(future.isSuccess()).isTrue();
            assertThat(serverChannel.isActive()).isTrue();
            assertThat(serverChannel.localAddress()).isNotNull();
            
            serverChannel.close();
        }

        @Test
        @DisplayName("关闭服务端 Channel")
        void closesServerChannel() {
            NioServerSocketChannel serverChannel = new NioServerSocketChannel();
            serverChannel.bind(0);
            
            assertThat(serverChannel.isOpen()).isTrue();
            
            serverChannel.close();
            
            assertThat(serverChannel.isOpen()).isFalse();
        }

        @Test
        @DisplayName("注册到 EventLoop")
        void registersToEventLoop() throws Exception {
            NioServerSocketChannel serverChannel = new NioServerSocketChannel();
            
            ChannelFuture future = serverChannel.register(serverEventLoop);
            
            Thread.sleep(100);
            assertThat(serverChannel.isRegistered()).isTrue();
            assertThat(serverChannel.eventLoop()).isEqualTo(serverEventLoop);
            
            serverChannel.close();
        }
    }

    @Nested
    @DisplayName("NioSocketChannel 测试")
    class SocketChannelTests {

        @Test
        @DisplayName("创建 NioSocketChannel")
        void createsSocketChannel() {
            NioSocketChannel socketChannel = new NioSocketChannel();
            
            assertThat(socketChannel).isNotNull();
            assertThat(socketChannel.isOpen()).isTrue();
            assertThat(socketChannel.isActive()).isFalse(); // 未连接
            assertThat(socketChannel.id()).isNotNull();
            assertThat(socketChannel.pipeline()).isNotNull();
            
            socketChannel.close();
        }

        @Test
        @DisplayName("关闭 Socket Channel")
        void closesSocketChannel() {
            NioSocketChannel socketChannel = new NioSocketChannel();
            
            assertThat(socketChannel.isOpen()).isTrue();
            
            socketChannel.close();
            
            assertThat(socketChannel.isOpen()).isFalse();
        }

        @Test
        @DisplayName("注册到 EventLoop")
        void registersToEventLoop() throws Exception {
            NioSocketChannel socketChannel = new NioSocketChannel();
            
            ChannelFuture future = socketChannel.register(clientEventLoop);
            
            Thread.sleep(100);
            assertThat(socketChannel.isRegistered()).isTrue();
            assertThat(socketChannel.eventLoop()).isEqualTo(clientEventLoop);
            
            socketChannel.close();
        }
    }

    @Nested
    @DisplayName("Channel ID 测试")
    class ChannelIdTests {

        @Test
        @DisplayName("每个 Channel 有唯一 ID")
        void channelHasUniqueId() {
            NioServerSocketChannel channel1 = new NioServerSocketChannel();
            NioServerSocketChannel channel2 = new NioServerSocketChannel();
            
            assertThat(channel1.id()).isNotNull();
            assertThat(channel2.id()).isNotNull();
            assertThat(channel1.id().asLongText()).isNotEqualTo(channel2.id().asLongText());
            
            channel1.close();
            channel2.close();
        }

        @Test
        @DisplayName("ChannelId 有短格式和长格式")
        void channelIdHasShortAndLongText() {
            NioSocketChannel channel = new NioSocketChannel();
            
            assertThat(channel.id().asShortText()).isNotEmpty();
            assertThat(channel.id().asLongText()).isNotEmpty();
            assertThat(channel.id().asShortText().length()).isLessThan(channel.id().asLongText().length());
            
            channel.close();
        }
    }

    @Nested
    @DisplayName("Pipeline 测试")
    class PipelineTests {

        @Test
        @DisplayName("Channel 创建时自动创建 Pipeline")
        void channelHasPipeline() {
            NioSocketChannel channel = new NioSocketChannel();
            
            assertThat(channel.pipeline()).isNotNull();
            assertThat(channel.pipeline().channel()).isEqualTo(channel);
            
            channel.close();
        }

        @Test
        @DisplayName("Pipeline 可以添加 Handler")
        void pipelineCanAddHandler() {
            NioSocketChannel channel = new NioSocketChannel();
            
            TestHandler handler = new TestHandler();
            channel.pipeline().addLast("test", handler);
            
            assertThat(channel.pipeline().get("test")).isEqualTo(handler);
            assertThat(channel.pipeline().names()).contains("test");
            
            channel.close();
        }
    }

    @Nested
    @DisplayName("验收场景")
    class AcceptanceScenarioTests {

        @Test
        @DisplayName("验收场景1: 创建服务端 Channel 并绑定端口")
        void acceptanceScenario1() {
            // Given: 创建服务端 Channel
            NioServerSocketChannel serverChannel = new NioServerSocketChannel();
            
            // When: 绑定到端口
            ChannelFuture future = serverChannel.bind(0);
            
            // Then: 绑定成功，Channel 变为活动状态
            assertThat(future.isSuccess()).isTrue();
            assertThat(serverChannel.isOpen()).isTrue();
            assertThat(serverChannel.isActive()).isTrue();
            assertThat(serverChannel.localAddress()).isInstanceOf(InetSocketAddress.class);
            
            serverChannel.close();
        }

        @Test
        @DisplayName("验收场景2: 创建客户端 Channel 并准备连接")
        void acceptanceScenario2() throws Exception {
            // Given: 创建客户端 Channel
            NioSocketChannel socketChannel = new NioSocketChannel();
            
            // When: 注册到 EventLoop
            socketChannel.register(clientEventLoop);
            Thread.sleep(100);
            
            // Then: Channel 已注册，准备连接
            assertThat(socketChannel.isOpen()).isTrue();
            assertThat(socketChannel.isRegistered()).isTrue();
            assertThat(socketChannel.isActive()).isFalse(); // 未连接
            
            socketChannel.close();
        }

        @Test
        @DisplayName("验收场景3: Channel Pipeline 添加多个 Handler")
        void acceptanceScenario3() {
            // Given: 创建 Channel
            NioSocketChannel channel = new NioSocketChannel();
            
            // When: 添加多个 Handler
            channel.pipeline().addLast("handler1", new TestHandler());
            channel.pipeline().addLast("handler2", new TestHandler());
            channel.pipeline().addFirst("handler0", new TestHandler());
            
            // Then: Handler 按正确顺序排列
            assertThat(channel.pipeline().names()).containsExactly("handler0", "handler1", "handler2");
            
            channel.close();
        }
    }

    /**
     * 测试用 Handler
     */
    private static class TestHandler implements ChannelInboundHandler {
        @Override
        public void handlerAdded(ChannelHandlerContext ctx) {}

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) {}

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) {}

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) {}

        @Override
        public void channelActive(ChannelHandlerContext ctx) {}

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {}

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {}

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {}

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {}

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {}
    }
}
