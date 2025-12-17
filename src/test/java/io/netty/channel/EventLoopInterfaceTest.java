package io.netty.channel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EventLoop 和 EventLoopGroup 接口定义测试
 *
 * <p>验证接口是否正确定义了所有必需的方法。
 */
@DisplayName("EventLoop 接口定义测试")
class EventLoopInterfaceTest {

    @Nested
    @DisplayName("EventLoopGroup 接口")
    class EventLoopGroupTests {

        @Test
        @DisplayName("EventLoopGroup 是一个接口")
        void eventLoopGroupIsInterface() {
            assertThat(EventLoopGroup.class.isInterface()).isTrue();
        }

        @Test
        @DisplayName("EventLoopGroup 定义了 next() 方法")
        void hasNextMethod() throws NoSuchMethodException {
            Method method = EventLoopGroup.class.getMethod("next");
            assertThat(method.getReturnType()).isEqualTo(EventLoop.class);
        }

        @Test
        @DisplayName("EventLoopGroup 定义了 register(Channel) 方法")
        void hasRegisterMethod() throws NoSuchMethodException {
            Method method = EventLoopGroup.class.getMethod("register", Channel.class);
            assertThat(method.getReturnType()).isEqualTo(ChannelFuture.class);
        }

        @Test
        @DisplayName("EventLoopGroup 定义了 shutdownGracefully() 方法")
        void hasShutdownGracefullyMethod() throws NoSuchMethodException {
            Method method = EventLoopGroup.class.getMethod("shutdownGracefully");
            assertThat(Future.class).isAssignableFrom(method.getReturnType());
        }

        @Test
        @DisplayName("EventLoopGroup 定义了 isShutdown() 方法")
        void hasIsShutdownMethod() throws NoSuchMethodException {
            Method method = EventLoopGroup.class.getMethod("isShutdown");
            assertThat(method.getReturnType()).isEqualTo(boolean.class);
        }

        @Test
        @DisplayName("EventLoopGroup 定义了 isTerminated() 方法")
        void hasIsTerminatedMethod() throws NoSuchMethodException {
            Method method = EventLoopGroup.class.getMethod("isTerminated");
            assertThat(method.getReturnType()).isEqualTo(boolean.class);
        }

        @Test
        @DisplayName("EventLoopGroup 所有方法都是抽象的（接口方法）")
        void allMethodsAreAbstract() {
            Set<String> declaredMethodNames = Arrays.stream(EventLoopGroup.class.getDeclaredMethods())
                    .filter(m -> !m.isDefault())
                    .map(Method::getName)
                    .collect(Collectors.toSet());

            assertThat(declaredMethodNames)
                    .containsExactlyInAnyOrder("next", "register", "shutdownGracefully", "isShutdown", "isTerminated");
        }
    }

    @Nested
    @DisplayName("EventLoop 接口")
    class EventLoopTests {

        @Test
        @DisplayName("EventLoop 是一个接口")
        void eventLoopIsInterface() {
            assertThat(EventLoop.class.isInterface()).isTrue();
        }

        @Test
        @DisplayName("EventLoop 继承自 EventLoopGroup")
        void extendsEventLoopGroup() {
            assertThat(EventLoopGroup.class).isAssignableFrom(EventLoop.class);
        }

        @Test
        @DisplayName("EventLoop 定义了 parent() 方法")
        void hasParentMethod() throws NoSuchMethodException {
            Method method = EventLoop.class.getMethod("parent");
            assertThat(method.getReturnType()).isEqualTo(EventLoopGroup.class);
            assertThat(method.getDeclaringClass()).isEqualTo(EventLoop.class);
        }

        @Test
        @DisplayName("EventLoop 定义了 inEventLoop() 方法")
        void hasInEventLoopMethod() throws NoSuchMethodException {
            Method method = EventLoop.class.getMethod("inEventLoop");
            assertThat(method.getReturnType()).isEqualTo(boolean.class);
        }

        @Test
        @DisplayName("EventLoop 定义了 inEventLoop(Thread) 方法")
        void hasInEventLoopWithThreadMethod() throws NoSuchMethodException {
            Method method = EventLoop.class.getMethod("inEventLoop", Thread.class);
            assertThat(method.getReturnType()).isEqualTo(boolean.class);
        }

        @Test
        @DisplayName("EventLoop 定义了 execute(Runnable) 方法")
        void hasExecuteMethod() throws NoSuchMethodException {
            Method method = EventLoop.class.getMethod("execute", Runnable.class);
            assertThat(method.getReturnType()).isEqualTo(void.class);
        }

        @Test
        @DisplayName("EventLoop 定义了 schedule() 方法")
        void hasScheduleMethod() throws NoSuchMethodException {
            Method method = EventLoop.class.getMethod("schedule", Runnable.class, long.class, TimeUnit.class);
            assertThat(ScheduledFuture.class).isAssignableFrom(method.getReturnType());
        }

        @Test
        @DisplayName("EventLoop 定义了 scheduleAtFixedRate() 方法")
        void hasScheduleAtFixedRateMethod() throws NoSuchMethodException {
            Method method = EventLoop.class.getMethod("scheduleAtFixedRate", 
                    Runnable.class, long.class, long.class, TimeUnit.class);
            assertThat(ScheduledFuture.class).isAssignableFrom(method.getReturnType());
        }

        @Test
        @DisplayName("EventLoop.next() 返回 EventLoop 类型")
        void nextReturnsEventLoop() throws NoSuchMethodException {
            // EventLoop.next() 重写了 EventLoopGroup.next()，返回更具体的类型
            Method method = EventLoop.class.getDeclaredMethod("next");
            assertThat(method.getReturnType()).isEqualTo(EventLoop.class);
        }
    }

    @Nested
    @DisplayName("相关接口")
    class RelatedInterfacesTests {

        @Test
        @DisplayName("Channel 接口存在")
        void channelInterfaceExists() {
            assertThat(Channel.class.isInterface()).isTrue();
        }

        @Test
        @DisplayName("ChannelFuture 接口存在")
        void channelFutureInterfaceExists() {
            assertThat(ChannelFuture.class.isInterface()).isTrue();
        }

        @Test
        @DisplayName("ChannelFuture 具有必要的异步操作方法")
        void channelFutureHasAsyncMethods() throws NoSuchMethodException {
            // 验证 isDone() 方法存在
            assertThat(ChannelFuture.class.getMethod("isDone")).isNotNull();
            // 验证 isSuccess() 方法存在
            assertThat(ChannelFuture.class.getMethod("isSuccess")).isNotNull();
            // 验证 sync() 方法存在
            assertThat(ChannelFuture.class.getMethod("sync")).isNotNull();
        }

        @Test
        @DisplayName("ChannelHandler 接口存在")
        void channelHandlerInterfaceExists() {
            assertThat(ChannelHandler.class.isInterface()).isTrue();
        }

        @Test
        @DisplayName("ChannelInboundHandler 继承自 ChannelHandler")
        void channelInboundHandlerExtendsChannelHandler() {
            assertThat(ChannelHandler.class).isAssignableFrom(ChannelInboundHandler.class);
        }

        @Test
        @DisplayName("ChannelOutboundHandler 继承自 ChannelHandler")
        void channelOutboundHandlerExtendsChannelHandler() {
            assertThat(ChannelHandler.class).isAssignableFrom(ChannelOutboundHandler.class);
        }

        @Test
        @DisplayName("ChannelPipeline 接口存在")
        void channelPipelineInterfaceExists() {
            assertThat(ChannelPipeline.class.isInterface()).isTrue();
        }

        @Test
        @DisplayName("ChannelPromise 继承自 ChannelFuture")
        void channelPromiseExtendsChannelFuture() {
            assertThat(ChannelFuture.class).isAssignableFrom(ChannelPromise.class);
        }
    }

    @Nested
    @DisplayName("设计验证")
    class DesignVerificationTests {

        @Test
        @DisplayName("EventLoop 是 EventLoopGroup 的子接口（设计模式验证）")
        void eventLoopIsSpecializedEventLoopGroup() {
            // EventLoop 继承 EventLoopGroup 是 Netty 的设计特点
            // 一个 EventLoop 也是一个只有自己的 EventLoopGroup
            assertThat(EventLoopGroup.class.isAssignableFrom(EventLoop.class)).isTrue();
        }

        @Test
        @DisplayName("所有核心接口都在 io.netty.channel 包中")
        void allCoreInterfacesInChannelPackage() {
            String expectedPackage = "io.netty.channel";
            assertThat(EventLoop.class.getPackageName()).isEqualTo(expectedPackage);
            assertThat(EventLoopGroup.class.getPackageName()).isEqualTo(expectedPackage);
            assertThat(Channel.class.getPackageName()).isEqualTo(expectedPackage);
            assertThat(ChannelFuture.class.getPackageName()).isEqualTo(expectedPackage);
            assertThat(ChannelHandler.class.getPackageName()).isEqualTo(expectedPackage);
            assertThat(ChannelPipeline.class.getPackageName()).isEqualTo(expectedPackage);
        }
    }
}
