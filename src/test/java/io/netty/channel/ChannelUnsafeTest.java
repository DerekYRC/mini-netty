package io.netty.channel;

import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioServerSocketChannel;
import io.netty.channel.nio.NioSocketChannel;
import org.junit.jupiter.api.*;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Channel.Unsafe 接口测试
 *
 * <p>测试 Unsafe 接口封装的底层 I/O 操作
 */
@DisplayName("Channel.Unsafe 测试")
class ChannelUnsafeTest {

    private NioEventLoop bossLoop;
    private NioEventLoop workerLoop;

    @BeforeEach
    void setUp() {
        bossLoop = new NioEventLoop(null);
        workerLoop = new NioEventLoop(null);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        if (bossLoop != null) {
            bossLoop.shutdownGracefully();
        }
        if (workerLoop != null) {
            workerLoop.shutdownGracefully();
        }
        Thread.sleep(100);
    }

    @Nested
    @DisplayName("Unsafe 接口存在性测试")
    class UnsafeInterfaceTests {

        @Test
        @DisplayName("Unsafe 接口定义在 Channel 内部")
        void unsafeInterfaceExistsInChannel() {
            // Unsafe 是 Channel 的内部接口
            Class<?>[] innerClasses = Channel.class.getDeclaredClasses();
            boolean found = false;
            for (Class<?> clazz : innerClasses) {
                if (clazz.getSimpleName().equals("Unsafe")) {
                    found = true;
                    break;
                }
            }
            assertThat(found).isTrue();
        }

        @Test
        @DisplayName("Channel 提供 unsafe() 方法")
        void channelProvidesUnsafeMethod() throws NoSuchMethodException {
            assertThat(Channel.class.getMethod("unsafe")).isNotNull();
        }

        @Test
        @DisplayName("Unsafe 接口包含必要的方法")
        void unsafeHasRequiredMethods() throws NoSuchMethodException {
            Class<?> unsafeClass = Channel.Unsafe.class;
            
            // 验证所有必要的方法
            assertThat(unsafeClass.getMethod("register", EventLoop.class, ChannelPromise.class)).isNotNull();
            assertThat(unsafeClass.getMethod("bind", java.net.SocketAddress.class, ChannelPromise.class)).isNotNull();
            assertThat(unsafeClass.getMethod("connect", java.net.SocketAddress.class, 
                    java.net.SocketAddress.class, ChannelPromise.class)).isNotNull();
            assertThat(unsafeClass.getMethod("disconnect", ChannelPromise.class)).isNotNull();
            assertThat(unsafeClass.getMethod("close", ChannelPromise.class)).isNotNull();
            assertThat(unsafeClass.getMethod("beginRead")).isNotNull();
            assertThat(unsafeClass.getMethod("write", Object.class, ChannelPromise.class)).isNotNull();
            assertThat(unsafeClass.getMethod("flush")).isNotNull();
        }
    }

    @Nested
    @DisplayName("ServerSocketChannel Unsafe 测试")
    class ServerChannelUnsafeTests {

        @Test
        @DisplayName("ServerSocketChannel 提供 Unsafe 实例")
        void serverChannelProvidesUnsafe() {
            NioServerSocketChannel serverChannel = new NioServerSocketChannel();
            try {
                assertThat(serverChannel.unsafe()).isNotNull();
                assertThat(serverChannel.unsafe()).isInstanceOf(Channel.Unsafe.class);
            } finally {
                serverChannel.close();
            }
        }

        @Test
        @DisplayName("通过 Unsafe 注册到 EventLoop")
        void registerViaUnsafe() throws InterruptedException {
            NioServerSocketChannel serverChannel = new NioServerSocketChannel();
            CountDownLatch latch = new CountDownLatch(1);
            
            try {
                DefaultChannelPromise promise = new DefaultChannelPromise(serverChannel);
                
                // 通过 Unsafe 注册
                serverChannel.unsafe().register(bossLoop, promise);
                
                // 等待足够时间让 EventLoop 启动并处理注册
                Thread.sleep(200);
                
                // 验证注册成功
                assertThat(serverChannel.isRegistered()).isTrue();
                assertThat(serverChannel.eventLoop()).isEqualTo(bossLoop);
            } finally {
                serverChannel.close();
            }
        }

        @Test
        @DisplayName("通过 Unsafe 绑定地址")
        void bindViaUnsafe() throws InterruptedException {
            NioServerSocketChannel serverChannel = new NioServerSocketChannel();
            
            try {
                DefaultChannelPromise registerPromise = new DefaultChannelPromise(serverChannel);
                
                // 先注册
                serverChannel.unsafe().register(bossLoop, registerPromise);
                Thread.sleep(200); // 等待注册完成
                
                // 绑定
                DefaultChannelPromise bindPromise = new DefaultChannelPromise(serverChannel);
                bossLoop.execute(() -> serverChannel.unsafe().bind(new InetSocketAddress(0), bindPromise));
                
                Thread.sleep(200); // 等待绑定完成
                
                // 验证绑定成功
                assertThat(serverChannel.isActive()).isTrue();
            } finally {
                serverChannel.close();
            }
        }

        @Test
        @DisplayName("通过 Unsafe 关闭 Channel")
        void closeViaUnsafe() throws InterruptedException {
            NioServerSocketChannel serverChannel = new NioServerSocketChannel();
            
            try {
                DefaultChannelPromise registerPromise = new DefaultChannelPromise(serverChannel);
                serverChannel.unsafe().register(bossLoop, registerPromise);
                Thread.sleep(200);
                
                // 关闭
                DefaultChannelPromise closePromise = new DefaultChannelPromise(serverChannel);
                bossLoop.execute(() -> serverChannel.unsafe().close(closePromise));
                Thread.sleep(200);
                
                // 验证关闭成功
                assertThat(serverChannel.isOpen()).isFalse();
            } finally {
                serverChannel.close();
            }
        }
    }

    @Nested
    @DisplayName("SocketChannel Unsafe 测试")
    class SocketChannelUnsafeTests {

        @Test
        @DisplayName("SocketChannel 提供 Unsafe 实例")
        void socketChannelProvidesUnsafe() {
            NioSocketChannel socketChannel = new NioSocketChannel();
            try {
                assertThat(socketChannel.unsafe()).isNotNull();
                assertThat(socketChannel.unsafe()).isInstanceOf(Channel.Unsafe.class);
            } finally {
                socketChannel.close();
            }
        }

        @Test
        @DisplayName("通过 Unsafe 注册 SocketChannel")
        void registerSocketChannelViaUnsafe() throws InterruptedException {
            NioSocketChannel socketChannel = new NioSocketChannel();
            
            try {
                DefaultChannelPromise promise = new DefaultChannelPromise(socketChannel);
                
                socketChannel.unsafe().register(workerLoop, promise);
                Thread.sleep(200);
                
                assertThat(socketChannel.isRegistered()).isTrue();
            } finally {
                socketChannel.close();
            }
        }
    }

    @Nested
    @DisplayName("Promise 回调测试")
    class PromiseCallbackTests {

        @Test
        @DisplayName("注册成功时 Promise 设置成功")
        void promiseSuccessOnRegister() throws InterruptedException {
            NioServerSocketChannel serverChannel = new NioServerSocketChannel();
            
            try {
                DefaultChannelPromise promise = new DefaultChannelPromise(serverChannel);
                
                serverChannel.unsafe().register(bossLoop, promise);
                
                // 等待 Promise 完成
                Thread.sleep(100);
                
                assertThat(promise.isDone()).isTrue();
                assertThat(promise.isSuccess()).isTrue();
            } finally {
                serverChannel.close();
            }
        }

        @Test
        @DisplayName("重复注册时 Promise 设置失败")
        void promiseFailureOnDuplicateRegister() throws InterruptedException {
            NioServerSocketChannel serverChannel = new NioServerSocketChannel();
            
            try {
                // 第一次注册
                DefaultChannelPromise promise1 = new DefaultChannelPromise(serverChannel);
                serverChannel.unsafe().register(bossLoop, promise1);
                Thread.sleep(100);
                
                // 第二次注册应该失败
                DefaultChannelPromise promise2 = new DefaultChannelPromise(serverChannel);
                serverChannel.unsafe().register(bossLoop, promise2);
                Thread.sleep(100);
                
                assertThat(promise2.isDone()).isTrue();
                assertThat(promise2.isSuccess()).isFalse();
                assertThat(promise2.cause()).isInstanceOf(IllegalStateException.class);
            } finally {
                serverChannel.close();
            }
        }
    }

    @Nested
    @DisplayName("验收场景测试")
    class AcceptanceScenarioTests {

        @Test
        @DisplayName("完整的服务端启动流程通过 Unsafe")
        void fullServerStartupViaUnsafe() throws InterruptedException {
            NioServerSocketChannel serverChannel = new NioServerSocketChannel();
            
            try {
                // 1. 注册
                DefaultChannelPromise registerPromise = new DefaultChannelPromise(serverChannel);
                serverChannel.unsafe().register(bossLoop, registerPromise);
                Thread.sleep(200);
                assertThat(serverChannel.isRegistered()).isTrue();
                
                // 2. 绑定
                DefaultChannelPromise bindPromise = new DefaultChannelPromise(serverChannel);
                bossLoop.execute(() -> serverChannel.unsafe().bind(new InetSocketAddress(0), bindPromise));
                Thread.sleep(200);
                assertThat(serverChannel.isActive()).isTrue();
                
                // 3. 关闭
                DefaultChannelPromise closePromise = new DefaultChannelPromise(serverChannel);
                bossLoop.execute(() -> serverChannel.unsafe().close(closePromise));
                Thread.sleep(200);
                assertThat(serverChannel.isOpen()).isFalse();
                
            } finally {
                serverChannel.close();
            }
        }

        @Test
        @DisplayName("客户端 Channel 通过 Unsafe 完成注册和关闭")
        void clientChannelViaUnsafe() throws InterruptedException {
            NioSocketChannel socketChannel = new NioSocketChannel();
            
            try {
                // 1. 注册
                DefaultChannelPromise registerPromise = new DefaultChannelPromise(socketChannel);
                socketChannel.unsafe().register(workerLoop, registerPromise);
                Thread.sleep(200);
                assertThat(socketChannel.isRegistered()).isTrue();
                
                // 2. 关闭
                DefaultChannelPromise closePromise = new DefaultChannelPromise(socketChannel);
                workerLoop.execute(() -> socketChannel.unsafe().close(closePromise));
                Thread.sleep(200);
                assertThat(socketChannel.isOpen()).isFalse();
                
            } finally {
                socketChannel.close();
            }
        }
    }
}
