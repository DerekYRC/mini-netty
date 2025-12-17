package io.netty.channel;

import io.netty.channel.nio.NioEventLoop;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * EventLoop 任务队列测试
 *
 * <p>验证 EventLoop 任务执行的核心功能：
 * <ul>
 *   <li>任务提交和执行</li>
 *   <li>任务执行顺序（FIFO）</li>
 *   <li>多线程提交任务的安全性</li>
 *   <li>异常处理</li>
 * </ul>
 */
@DisplayName("EventLoop 任务队列测试")
class TaskQueueTest {

    private NioEventLoop eventLoop;

    @BeforeEach
    void setUp() {
        eventLoop = new NioEventLoop(null);
        eventLoop.start();
        // 等待事件循环启动
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        if (eventLoop != null) {
            eventLoop.shutdownGracefully();
            Thread.sleep(200);
        }
    }

    @Nested
    @DisplayName("execute() 方法")
    class ExecuteMethodTests {

        @Test
        @DisplayName("execute() 执行单个任务")
        void executesSingleTask() throws InterruptedException {
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
        @DisplayName("execute() 按 FIFO 顺序执行任务")
        void executesTasksInFifoOrder() throws InterruptedException {
            int taskCount = 10;
            CountDownLatch latch = new CountDownLatch(taskCount);
            List<Integer> executionOrder = Collections.synchronizedList(new ArrayList<>());

            for (int i = 0; i < taskCount; i++) {
                final int index = i;
                eventLoop.execute(() -> {
                    executionOrder.add(index);
                    latch.countDown();
                });
            }

            boolean completed = latch.await(2, TimeUnit.SECONDS);
            assertThat(completed).isTrue();
            assertThat(executionOrder).containsExactly(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        }

        @Test
        @DisplayName("execute() 不接受 null 任务")
        void rejectsNullTask() {
            assertThatThrownBy(() -> eventLoop.execute(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("任务在 EventLoop 线程中执行")
        void tasksExecuteInEventLoopThread() throws InterruptedException {
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

    @Nested
    @DisplayName("多线程提交")
    class ConcurrentSubmissionTests {

        @Test
        @DisplayName("多个线程同时提交任务")
        void multipleThreadsSubmitTasks() throws InterruptedException {
            int threadCount = 5;
            int tasksPerThread = 20;
            int totalTasks = threadCount * tasksPerThread;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch completeLatch = new CountDownLatch(totalTasks);
            AtomicInteger executedCount = new AtomicInteger(0);

            // 创建多个线程提交任务
            List<Thread> threads = new ArrayList<>();
            for (int t = 0; t < threadCount; t++) {
                Thread thread = new Thread(() -> {
                    try {
                        startLatch.await();
                        for (int i = 0; i < tasksPerThread; i++) {
                            eventLoop.execute(() -> {
                                executedCount.incrementAndGet();
                                completeLatch.countDown();
                            });
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                threads.add(thread);
                thread.start();
            }

            // 同时开始
            startLatch.countDown();

            // 等待所有任务完成
            boolean completed = completeLatch.await(5, TimeUnit.SECONDS);
            assertThat(completed).isTrue();
            assertThat(executedCount.get()).isEqualTo(totalTasks);

            // 等待线程结束
            for (Thread thread : threads) {
                thread.join(1000);
            }
        }

        @Test
        @DisplayName("从 EventLoop 线程内提交任务")
        void submitTaskFromEventLoopThread() throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(2);
            List<String> order = Collections.synchronizedList(new ArrayList<>());

            eventLoop.execute(() -> {
                order.add("first");
                latch.countDown();
                
                // 从 EventLoop 线程内提交另一个任务
                eventLoop.execute(() -> {
                    order.add("second");
                    latch.countDown();
                });
            });

            boolean completed = latch.await(2, TimeUnit.SECONDS);
            assertThat(completed).isTrue();
            assertThat(order).containsExactly("first", "second");
        }
    }

    @Nested
    @DisplayName("异常处理")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("任务异常不影响后续任务执行")
        void exceptionDoesNotAffectSubsequentTasks() throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(2);
            AtomicBoolean secondTaskExecuted = new AtomicBoolean(false);

            // 第一个任务抛出异常
            eventLoop.execute(() -> {
                latch.countDown();
                throw new RuntimeException("测试异常");
            });

            // 第二个任务应该正常执行
            eventLoop.execute(() -> {
                secondTaskExecuted.set(true);
                latch.countDown();
            });

            boolean completed = latch.await(2, TimeUnit.SECONDS);
            assertThat(completed).isTrue();
            assertThat(secondTaskExecuted.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("性能和行为")
    class PerformanceTests {

        @Test
        @DisplayName("执行大量任务")
        void executeManyTasks() throws InterruptedException {
            int taskCount = 1000;
            CountDownLatch latch = new CountDownLatch(taskCount);
            AtomicInteger counter = new AtomicInteger(0);

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < taskCount; i++) {
                eventLoop.execute(() -> {
                    counter.incrementAndGet();
                    latch.countDown();
                });
            }

            boolean completed = latch.await(10, TimeUnit.SECONDS);
            long elapsed = System.currentTimeMillis() - startTime;

            assertThat(completed).isTrue();
            assertThat(counter.get()).isEqualTo(taskCount);
            System.out.println("[TaskQueueTest] 执行 " + taskCount + " 个任务耗时: " + elapsed + "ms");
        }

        @Test
        @DisplayName("wakeup() 立即唤醒阻塞的选择器")
        void wakeupImmediatelyWakesSelector() throws InterruptedException {
            // 确保选择器正在阻塞
            Thread.sleep(50);

            long startTime = System.currentTimeMillis();
            CountDownLatch latch = new CountDownLatch(1);

            eventLoop.execute(latch::countDown);

            boolean completed = latch.await(200, TimeUnit.MILLISECONDS);
            long elapsed = System.currentTimeMillis() - startTime;

            assertThat(completed).isTrue();
            // 应该很快完成，不需要等待 select 超时
            assertThat(elapsed).isLessThan(200);
        }
    }

    @Nested
    @DisplayName("验收场景")
    class AcceptanceScenarioTests {

        @Test
        @DisplayName("验收场景1: execute() 提交的任务被执行")
        void acceptanceScenario1() throws InterruptedException {
            // Given: EventLoop 已启动
            assertThat(eventLoop.isRunning()).isTrue();

            // When: 提交任务
            CountDownLatch latch = new CountDownLatch(1);
            AtomicBoolean executed = new AtomicBoolean(false);
            eventLoop.execute(() -> {
                executed.set(true);
                latch.countDown();
            });

            // Then: 任务被执行
            boolean completed = latch.await(2, TimeUnit.SECONDS);
            assertThat(completed).isTrue();
            assertThat(executed.get()).isTrue();
        }

        @Test
        @DisplayName("验收场景2: 任务按提交顺序执行")
        void acceptanceScenario2() throws InterruptedException {
            // Given: EventLoop 已启动
            assertThat(eventLoop.isRunning()).isTrue();

            // When: 按顺序提交多个任务
            List<Integer> results = Collections.synchronizedList(new ArrayList<>());
            CountDownLatch latch = new CountDownLatch(5);

            for (int i = 1; i <= 5; i++) {
                final int value = i;
                eventLoop.execute(() -> {
                    results.add(value);
                    latch.countDown();
                });
            }

            // Then: 任务按顺序执行
            latch.await(2, TimeUnit.SECONDS);
            assertThat(results).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("验收场景3: hasTasks() 正确反映队列状态")
        void acceptanceScenario3() throws InterruptedException {
            // 在 EventLoop 线程内检查任务队列状态
            CountDownLatch latch = new CountDownLatch(1);
            AtomicBoolean[] hasTasksResults = new AtomicBoolean[2];
            hasTasksResults[0] = new AtomicBoolean();
            hasTasksResults[1] = new AtomicBoolean();

            eventLoop.execute(() -> {
                // 添加一个任务
                eventLoop.execute(() -> {});
                
                // 检查 hasTasks
                hasTasksResults[0].set(((SingleThreadEventLoop) eventLoop).hasTasks());
                latch.countDown();
            });

            latch.await(2, TimeUnit.SECONDS);
            // 在添加任务后，hasTasks 应该返回 true
            assertThat(hasTasksResults[0].get()).isTrue();
        }
    }
}
