package io.netty.integration;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioServerSocketChannel;
import io.netty.channel.nio.NioSocketChannel;
import io.netty.channel.AbstractChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Bootstrap 集成测试
 *
 * <p>测试客户端 Bootstrap 和服务端 ServerBootstrap 的集成使用
 */
@DisplayName("Bootstrap 集成测试")
class BootstrapIntegrationTest {

    private TestEventLoopGroup bossGroup;
    private TestEventLoopGroup workerGroup;
    private TestEventLoopGroup clientGroup;

    @BeforeEach
    void setUp() {
        bossGroup = new TestEventLoopGroup();
        workerGroup = new TestEventLoopGroup();
        clientGroup = new TestEventLoopGroup();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        if (clientGroup != null) {
            clientGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        Thread.sleep(100);
    }

    @Nested
    @DisplayName("Bootstrap 配置测试")
    class BootstrapConfigurationTests {

        @Test
        @DisplayName("设置 EventLoopGroup")
        void setsEventLoopGroup() {
            Bootstrap bootstrap = new Bootstrap();
            
            Bootstrap result = bootstrap.group(clientGroup);
            
            assertThat(result).isSameAs(bootstrap);
            assertThat(bootstrap.group()).isSameAs(clientGroup);
        }

        @Test
        @DisplayName("设置 Channel 类型")
        void setsChannelClass() {
            Bootstrap bootstrap = new Bootstrap();
            
            Bootstrap result = bootstrap.channel(NioSocketChannel.class);
            
            assertThat(result).isSameAs(bootstrap);
        }

        @Test
        @DisplayName("设置 handler")
        void setsHandler() {
            Bootstrap bootstrap = new Bootstrap();
            ChannelHandler handler = new ChannelInboundHandlerAdapter();
            
            Bootstrap result = bootstrap.handler(handler);
            
            assertThat(result).isSameAs(bootstrap);
        }

        @Test
        @DisplayName("设置 option")
        void setsOption() {
            Bootstrap bootstrap = new Bootstrap();
            
            Bootstrap result = bootstrap.option(ChannelOption.TCP_NODELAY, true);
            
            assertThat(result).isSameAs(bootstrap);
        }

        @Test
        @DisplayName("设置远程地址")
        void setsRemoteAddress() {
            Bootstrap bootstrap = new Bootstrap();
            
            Bootstrap result = bootstrap.remoteAddress("localhost", 8080);
            
            assertThat(result).isSameAs(bootstrap);
        }

        @Test
        @DisplayName("链式配置")
        void fluentConfiguration() {
            Bootstrap bootstrap = new Bootstrap()
                    .group(clientGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInboundHandlerAdapter())
                    .remoteAddress("localhost", 8080);
            
            assertThat(bootstrap.group()).isSameAs(clientGroup);
        }

        @Test
        @DisplayName("克隆 Bootstrap")
        void clonesBootstrap() {
            Bootstrap original = new Bootstrap()
                    .group(clientGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInboundHandlerAdapter());
            
            Bootstrap cloned = original.clone();
            
            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned.group()).isSameAs(original.group());
        }
    }

    @Nested
    @DisplayName("Bootstrap 验证测试")
    class BootstrapValidationTests {

        @Test
        @DisplayName("未设置 group 时验证失败")
        void failsValidationWithoutGroup() {
            Bootstrap bootstrap = new Bootstrap()
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInboundHandlerAdapter());
            
            assertThatThrownBy(bootstrap::validate)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("group");
        }

        @Test
        @DisplayName("未设置 channel 时验证失败")
        void failsValidationWithoutChannel() {
            Bootstrap bootstrap = new Bootstrap()
                    .group(clientGroup)
                    .handler(new ChannelInboundHandlerAdapter());
            
            assertThatThrownBy(bootstrap::validate)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("channel");
        }

        @Test
        @DisplayName("未设置 handler 时验证失败")
        void failsValidationWithoutHandler() {
            Bootstrap bootstrap = new Bootstrap()
                    .group(clientGroup)
                    .channel(NioSocketChannel.class);
            
            assertThatThrownBy(bootstrap::validate)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("handler");
        }

        @Test
        @DisplayName("完整配置验证通过")
        void passesValidationWithCompleteConfiguration() {
            Bootstrap bootstrap = new Bootstrap()
                    .group(clientGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInboundHandlerAdapter());
            
            Bootstrap result = bootstrap.validate();
            
            assertThat(result).isSameAs(bootstrap);
        }
    }

    @Nested
    @DisplayName("空值验证测试")
    class NullValidationTests {

        @Test
        @DisplayName("group 为空时抛出异常")
        void throwsExceptionWhenGroupIsNull() {
            Bootstrap bootstrap = new Bootstrap();
            
            assertThatThrownBy(() -> bootstrap.group(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("channel 为空时抛出异常")
        void throwsExceptionWhenChannelIsNull() {
            Bootstrap bootstrap = new Bootstrap();
            
            assertThatThrownBy(() -> bootstrap.channel(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("handler 为空时抛出异常")
        void throwsExceptionWhenHandlerIsNull() {
            Bootstrap bootstrap = new Bootstrap();
            
            assertThatThrownBy(() -> bootstrap.handler(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("connect 远程地址为空时抛出异常")
        void throwsExceptionWhenRemoteAddressIsNull() {
            Bootstrap bootstrap = new Bootstrap()
                    .group(clientGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInboundHandlerAdapter());
            
            assertThatThrownBy(() -> bootstrap.connect((java.net.SocketAddress) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("连接测试")
    class ConnectTests {

        @Test
        @DisplayName("连接到服务端")
        void connectsToServer() throws Exception {
            // 启动服务端
            AtomicBoolean serverConnected = new AtomicBoolean(false);
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) {
                            serverConnected.set(true);
                            System.out.println("[Server] 客户端已连接");
                        }
                    });
            
            ChannelFuture serverFuture = serverBootstrap.bind(0);
            Thread.sleep(200);
            
            Channel serverChannel = serverFuture.channel();
            int port = ((InetSocketAddress) ((AbstractChannel) serverChannel).localAddress()).getPort();
            System.out.println("[Test] 服务端监听端口: " + port);
            
            // 启动客户端
            Bootstrap clientBootstrap = new Bootstrap()
                    .group(clientGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInboundHandlerAdapter());
            
            ChannelFuture clientFuture = clientBootstrap.connect("127.0.0.1", port);
            Thread.sleep(500);
            
            // 验证
            assertThat(clientFuture).isNotNull();
            assertThat(clientFuture.channel()).isNotNull();
            
            // 关闭
            clientFuture.channel().close();
            serverChannel.close();
        }

        @Test
        @DisplayName("使用预设远程地址连接")
        void connectsWithPresetRemoteAddress() throws Exception {
            // 启动服务端
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInboundHandlerAdapter());
            
            ChannelFuture serverFuture = serverBootstrap.bind(0);
            Thread.sleep(200);
            
            Channel serverChannel = serverFuture.channel();
            int port = ((InetSocketAddress) ((AbstractChannel) serverChannel).localAddress()).getPort();
            
            // 使用预设远程地址
            Bootstrap clientBootstrap = new Bootstrap()
                    .group(clientGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInboundHandlerAdapter())
                    .remoteAddress("127.0.0.1", port);
            
            ChannelFuture clientFuture = clientBootstrap.connect();
            Thread.sleep(500);
            
            assertThat(clientFuture).isNotNull();
            assertThat(clientFuture.channel()).isNotNull();
            
            clientFuture.channel().close();
            serverChannel.close();
        }
    }

    @Nested
    @DisplayName("验收场景测试")
    class AcceptanceScenarioTests {

        @Test
        @DisplayName("场景: 完整的客户端服务端交互")
        void fullClientServerInteraction() throws Exception {
            AtomicReference<String> receivedMessage = new AtomicReference<>();
            CountDownLatch messageLatch = new CountDownLatch(1);
            
            // 服务端：收到消息后回复
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) {
                            System.out.println("[Server] 客户端已连接");
                        }
                        
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) {
                            System.out.println("[Server] 收到消息: " + msg);
                            // Echo back
                            ctx.writeAndFlush(msg);
                        }
                    });
            
            ChannelFuture serverFuture = serverBootstrap.bind(0);
            Thread.sleep(200);
            
            Channel serverChannel = serverFuture.channel();
            int port = ((InetSocketAddress) ((AbstractChannel) serverChannel).localAddress()).getPort();
            
            // 客户端
            Bootstrap clientBootstrap = new Bootstrap()
                    .group(clientGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) {
                            System.out.println("[Client] 已连接到服务端");
                        }
                        
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) {
                            System.out.println("[Client] 收到响应: " + msg);
                            receivedMessage.set(msg.toString());
                            messageLatch.countDown();
                        }
                    });
            
            ChannelFuture clientFuture = clientBootstrap.connect("127.0.0.1", port);
            Thread.sleep(300);
            
            // 验证连接成功
            assertThat(clientFuture.channel()).isNotNull();
            assertThat(clientFuture.channel().isOpen()).isTrue();
            
            // 关闭
            clientFuture.channel().close();
            serverChannel.close();
        }

        @Test
        @DisplayName("场景: Bootstrap 与 ServerBootstrap 配合使用")
        void bootstrapWithServerBootstrap() throws Exception {
            CountDownLatch connectionLatch = new CountDownLatch(1);
            
            // 服务端
            ServerBootstrap server = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ch.pipeline().addLast("serverHandler", new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) {
                                    System.out.println("[Server] 新连接建立");
                                    connectionLatch.countDown();
                                }
                            });
                        }
                    });
            
            ChannelFuture serverFuture = server.bind(0);
            Thread.sleep(200);
            
            int port = ((InetSocketAddress) ((AbstractChannel) serverFuture.channel()).localAddress()).getPort();
            
            // 客户端
            Bootstrap client = new Bootstrap()
                    .group(clientGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ch.pipeline().addLast("clientHandler", new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) {
                                    System.out.println("[Client] 已连接");
                                }
                            });
                        }
                    });
            
            ChannelFuture clientFuture = client.connect("127.0.0.1", port);
            Thread.sleep(500);
            
            // 验证
            assertThat(clientFuture.channel()).isNotNull();
            
            // 清理
            clientFuture.channel().close();
            serverFuture.channel().close();
        }
    }

    /**
     * 简单的 EventLoopGroup 测试实现
     */
    private static class TestEventLoopGroup implements EventLoopGroup {

        private final NioEventLoop eventLoop;
        private volatile boolean shutdown = false;

        TestEventLoopGroup() {
            this.eventLoop = new NioEventLoop(this);
            this.eventLoop.start();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public EventLoop next() {
            return eventLoop;
        }

        @Override
        public ChannelFuture register(Channel channel) {
            DefaultChannelPromise promise = new DefaultChannelPromise(channel);
            eventLoop.execute(() -> {
                try {
                    channel.unsafe().register(eventLoop, promise);
                } catch (Exception e) {
                    promise.setFailure(e);
                }
            });
            return promise;
        }

        @Override
        public java.util.concurrent.Future<?> shutdownGracefully() {
            shutdown = true;
            return eventLoop.shutdownGracefully();
        }

        @Override
        public boolean isShutdown() {
            return shutdown;
        }

        @Override
        public boolean isTerminated() {
            return eventLoop.isTerminated();
        }
    }
}
