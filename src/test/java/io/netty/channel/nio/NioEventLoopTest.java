package io.netty.channel.nio;

import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SingleThreadEventLoop;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NioEventLoop 单元测试
 *
 * <p>测试事件循环的核心功能：
 * <ul>
 *   <li>启动和停止</li>
 *   <li>任务执行</li>
 *   <li>线程判断</li>
 *   <li>Selector 事件处理</li>
 * </ul>
 */
@DisplayName("NioEventLoop 测试")
class NioEventLoopTest {

    private NioEventLoop eventLoop;

    @BeforeEach
    void setUp() {
        eventLoop = new NioEventLoop(null);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        if (eventLoop != null && eventLoop.isRunning()) {
            eventLoop.shutdownGracefully();
            // 等待事件循环停止
            Thread.sleep(200);
        }
    }

    @Nested
    @DisplayName("基本功能")
    class BasicFunctionalityTests {

        @Test
        @DisplayName("NioEventLoop 实现了 EventLoop 接口")
        void implementsEventLoop() {
            assertThat(eventLoop).isInstanceOf(EventLoop.class);
        }

        @Test
        @DisplayName("NioEventLoop 继承自 SingleThreadEventLoop")
        void extendsSingleThreadEventLoop() {
            assertThat(eventLoop).isInstanceOf(SingleThreadEventLoop.class);
        }

        @Test
        @DisplayName("NioEventLoop 有 Selector")
        void hasSelector() {
            Selector selector = eventLoop.selector();
            assertThat(selector).isNotNull();
            assertThat(selector.isOpen()).isTrue();
        }

        @Test
        @DisplayName("next() 返回自身")
        void nextReturnsSelf() {
            assertThat(eventLoop.next()).isSameAs(eventLoop);
        }

        @Test
        @DisplayName("parent() 返回构造时传入的 EventLoopGroup")
        void parentReturnsConstructorArg() {
            assertThat(eventLoop.parent()).isNull();

            // 创建带 parent 的 eventLoop
            EventLoopGroup mockParent = new EventLoopGroup() {
                @Override
                public EventLoop next() {
                    return null;
                }

                @Override
                public io.netty.channel.ChannelFuture register(io.netty.channel.Channel channel) {
                    return null;
                }

                @Override
                public java.util.concurrent.Future<?> shutdownGracefully() {
                    return null;
                }

                @Override
                public boolean isShutdown() {
                    return false;
                }

                @Override
                public boolean isTerminated() {
                    return false;
                }
            };
            NioEventLoop eventLoopWithParent = new NioEventLoop(mockParent);
            assertThat(eventLoopWithParent.parent()).isSameAs(mockParent);
            eventLoopWithParent.shutdownGracefully();
        }
    }

    @Nested
    @DisplayName("启动和停止")
    class StartStopTests {

        @Test
        @DisplayName("初始状态：未运行、未关闭、未终止")
        void initialState() {
            assertThat(eventLoop.isRunning()).isFalse();
            assertThat(eventLoop.isShutdown()).isFalse();
            assertThat(eventLoop.isTerminated()).isFalse();
        }

        @Test
        @DisplayName("start() 启动事件循环")
        void startStartsEventLoop() throws InterruptedException {
            eventLoop.start();
            Thread.sleep(100);
            assertThat(eventLoop.isRunning()).isTrue();
        }

        @Test
        @DisplayName("shutdownGracefully() 停止事件循环")
        void shutdownGracefullyStopsEventLoop() throws InterruptedException {
            eventLoop.start();
            Thread.sleep(100);
            assertThat(eventLoop.isRunning()).isTrue();

            eventLoop.shutdownGracefully();
            Thread.sleep(300);
            assertThat(eventLoop.isShutdown()).isTrue();
        }

        @Test
        @DisplayName("重复调用 start() 只启动一次")
        void multipleStartsOnlyStartOnce() throws InterruptedException {
            eventLoop.start();
            Thread thread1 = getEventLoopThread();
            
            eventLoop.start();
            Thread thread2 = getEventLoopThread();
            
            assertThat(thread1).isSameAs(thread2);
        }

        private Thread getEventLoopThread() throws InterruptedException {
            Thread.sleep(50);
            CountDownLatch latch = new CountDownLatch(1);
            Thread[] holder = new Thread[1];
            eventLoop.execute(() -> {
                holder[0] = Thread.currentThread();
                latch.countDown();
            });
            latch.await(1, TimeUnit.SECONDS);
            return holder[0];
        }
    }

    @Nested
    @DisplayName("线程判断")
    class InEventLoopTests {

        @Test
        @DisplayName("在非 EventLoop 线程调用 inEventLoop() 返回 false")
        void inEventLoopReturnsFalseFromOtherThread() {
            eventLoop.start();
            assertThat(eventLoop.inEventLoop()).isFalse();
        }

        @Test
        @DisplayName("在 EventLoop 线程内调用 inEventLoop() 返回 true")
        void inEventLoopReturnsTrueFromEventLoopThread() throws InterruptedException {
            eventLoop.start();
            Thread.sleep(100);

            CountDownLatch latch = new CountDownLatch(1);
            AtomicBoolean result = new AtomicBoolean(false);

            eventLoop.execute(() -> {
                result.set(eventLoop.inEventLoop());
                latch.countDown();
            });

            boolean completed = latch.await(2, TimeUnit.SECONDS);
            assertThat(completed).isTrue();
            assertThat(result.get()).isTrue();
        }

        @Test
        @DisplayName("inEventLoop(Thread) 正确判断线程")
        void inEventLoopWithThreadParameter() throws InterruptedException {
            eventLoop.start();
            Thread.sleep(100);

            CountDownLatch latch = new CountDownLatch(1);
            Thread[] eventLoopThread = new Thread[1];

            eventLoop.execute(() -> {
                eventLoopThread[0] = Thread.currentThread();
                latch.countDown();
            });

            latch.await(2, TimeUnit.SECONDS);
            assertThat(eventLoop.inEventLoop(eventLoopThread[0])).isTrue();
            assertThat(eventLoop.inEventLoop(Thread.currentThread())).isFalse();
        }
    }

    @Nested
    @DisplayName("任务执行")
    class TaskExecutionTests {

        @Test
        @DisplayName("execute() 执行任务")
        void executeRunsTask() throws InterruptedException {
            eventLoop.start();
            Thread.sleep(100);

            CountDownLatch latch = new CountDownLatch(1);
            AtomicBoolean executed = new AtomicBoolean(false);

            eventLoop.execute(() -> {
                executed.set(true);
                latch.countDown();
            });

            boolean completed = latch.await(2, TimeUnit.SECONDS);
            assertThat(completed).isTrue();
            assertThat(executed.get()).isTrue();
        }

        @Test
        @DisplayName("execute() 按顺序执行多个任务")
        void executeRunsTasksInOrder() throws InterruptedException {
            eventLoop.start();
            Thread.sleep(100);

            CountDownLatch latch = new CountDownLatch(3);
            AtomicInteger counter = new AtomicInteger(0);
            int[] order = new int[3];

            for (int i = 0; i < 3; i++) {
                final int index = i;
                eventLoop.execute(() -> {
                    order[index] = counter.getAndIncrement();
                    latch.countDown();
                });
            }

            boolean completed = latch.await(2, TimeUnit.SECONDS);
            assertThat(completed).isTrue();
            assertThat(order).containsExactly(0, 1, 2);
        }

        @Test
        @DisplayName("任务在 EventLoop 线程执行")
        void tasksRunInEventLoopThread() throws InterruptedException {
            eventLoop.start();
            Thread.sleep(100);

            CountDownLatch latch = new CountDownLatch(1);
            Thread[] taskThread = new Thread[1];

            eventLoop.execute(() -> {
                taskThread[0] = Thread.currentThread();
                latch.countDown();
            });

            latch.await(2, TimeUnit.SECONDS);
            assertThat(eventLoop.inEventLoop(taskThread[0])).isTrue();
        }

        @Test
        @DisplayName("从非 EventLoop 线程 execute() 会唤醒选择器")
        void executeFromOtherThreadWakesUpSelector() throws InterruptedException {
            eventLoop.start();
            Thread.sleep(100);

            long startTime = System.currentTimeMillis();
            CountDownLatch latch = new CountDownLatch(1);

            // 从主线程提交任务
            eventLoop.execute(latch::countDown);

            boolean completed = latch.await(500, TimeUnit.MILLISECONDS);
            long elapsed = System.currentTimeMillis() - startTime;

            assertThat(completed).isTrue();
            // 应该很快执行，不需要等待 select 超时
            assertThat(elapsed).isLessThan(500);
        }
    }

    @Nested
    @DisplayName("Selector 事件处理")
    class SelectorEventTests {

        @Test
        @DisplayName("可以注册 Channel 到 Selector")
        void canRegisterChannelToSelector() throws IOException, InterruptedException {
            eventLoop.start();
            Thread.sleep(100);

            CountDownLatch latch = new CountDownLatch(1);
            SelectionKey[] keyHolder = new SelectionKey[1];

            eventLoop.execute(() -> {
                try {
                    ServerSocketChannel serverChannel = ServerSocketChannel.open();
                    serverChannel.configureBlocking(false);
                    serverChannel.bind(new InetSocketAddress(0));
                    
                    keyHolder[0] = serverChannel.register(eventLoop.selector(), SelectionKey.OP_ACCEPT);
                    latch.countDown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            boolean completed = latch.await(2, TimeUnit.SECONDS);
            assertThat(completed).isTrue();
            assertThat(keyHolder[0]).isNotNull();
            assertThat(keyHolder[0].isValid()).isTrue();

            // 清理
            keyHolder[0].channel().close();
        }
    }

    @Nested
    @DisplayName("验收场景")
    class AcceptanceScenarioTests {

        @Test
        @DisplayName("验收场景1: EventLoop 启动后可以执行任务")
        void acceptanceScenario1() throws InterruptedException {
            // Given: 创建并启动 EventLoop
            eventLoop.start();
            Thread.sleep(100);
            assertThat(eventLoop.isRunning()).isTrue();

            // When: 提交任务
            CountDownLatch latch = new CountDownLatch(1);
            AtomicBoolean taskExecuted = new AtomicBoolean(false);
            eventLoop.execute(() -> {
                taskExecuted.set(true);
                latch.countDown();
            });

            // Then: 任务被执行
            boolean completed = latch.await(2, TimeUnit.SECONDS);
            assertThat(completed).isTrue();
            assertThat(taskExecuted.get()).isTrue();
        }

        @Test
        @DisplayName("验收场景2: inEventLoop() 正确判断线程")
        void acceptanceScenario2() throws InterruptedException {
            // Given: 创建并启动 EventLoop
            eventLoop.start();
            Thread.sleep(100);

            // When & Then: 在不同线程检查 inEventLoop()
            // 主线程不是 EventLoop 线程
            assertThat(eventLoop.inEventLoop()).isFalse();

            // EventLoop 内部是 EventLoop 线程
            CountDownLatch latch = new CountDownLatch(1);
            AtomicBoolean isInEventLoop = new AtomicBoolean(false);
            eventLoop.execute(() -> {
                isInEventLoop.set(eventLoop.inEventLoop());
                latch.countDown();
            });
            latch.await(2, TimeUnit.SECONDS);
            assertThat(isInEventLoop.get()).isTrue();
        }
    }
}
