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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 定时任务测试
 *
 * <p>验证 EventLoop 定时任务功能：
 * <ul>
 *   <li>schedule() - 延迟执行</li>
 *   <li>scheduleAtFixedRate() - 周期性执行</li>
 *   <li>任务取消</li>
 *   <li>执行顺序</li>
 * </ul>
 */
@DisplayName("定时任务测试")
class ScheduledTaskTest {

    private NioEventLoop eventLoop;

    @BeforeEach
    void setUp() {
        eventLoop = new NioEventLoop(null);
        eventLoop.start();
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
    @DisplayName("schedule() 延迟任务")
    class ScheduleDelayedTaskTests {

        @Test
        @DisplayName("schedule() 在指定延迟后执行任务")
        void schedulesTaskWithDelay() throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicBoolean executed = new AtomicBoolean(false);
            long startTime = System.currentTimeMillis();

            eventLoop.schedule(() -> {
                executed.set(true);
                latch.countDown();
            }, 200, TimeUnit.MILLISECONDS);

            boolean completed = latch.await(2, TimeUnit.SECONDS);
            long elapsed = System.currentTimeMillis() - startTime;

            assertThat(completed).isTrue();
            assertThat(executed.get()).isTrue();
            // 应该至少延迟 200ms
            assertThat(elapsed).isGreaterThanOrEqualTo(180); // 允许一些误差
        }

        @Test
        @DisplayName("schedule() 返回 ScheduledFuture")
        void returnsScheduledFuture() {
            ScheduledFuture<?> future = eventLoop.schedule(() -> {}, 1, TimeUnit.SECONDS);
            assertThat((Object) future).isNotNull();
            assertThat((Object) future).isInstanceOf(ScheduledFuture.class);
            future.cancel(false);
        }

        @Test
        @DisplayName("schedule() 不接受 null 任务")
        void rejectsNullTask() {
            assertThatThrownBy(() -> eventLoop.schedule(null, 1, TimeUnit.SECONDS))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("schedule() 不接受 null TimeUnit")
        void rejectsNullTimeUnit() {
            assertThatThrownBy(() -> eventLoop.schedule(() -> {}, 1, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("多个定时任务按时间顺序执行")
        void multipleTasksExecuteInOrder() throws InterruptedException {
            List<Integer> order = Collections.synchronizedList(new ArrayList<>());
            CountDownLatch latch = new CountDownLatch(3);

            // 提交顺序：300ms, 100ms, 200ms
            eventLoop.schedule(() -> {
                order.add(3);
                latch.countDown();
            }, 300, TimeUnit.MILLISECONDS);

            eventLoop.schedule(() -> {
                order.add(1);
                latch.countDown();
            }, 100, TimeUnit.MILLISECONDS);

            eventLoop.schedule(() -> {
                order.add(2);
                latch.countDown();
            }, 200, TimeUnit.MILLISECONDS);

            boolean completed = latch.await(2, TimeUnit.SECONDS);
            assertThat(completed).isTrue();
            // 执行顺序应该是 1, 2, 3
            assertThat(order).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("scheduleAtFixedRate() 周期任务")
    class ScheduleAtFixedRateTests {

        @Test
        @DisplayName("scheduleAtFixedRate() 周期性执行任务")
        void schedulesPeriodicTask() throws InterruptedException {
            AtomicInteger counter = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(3);

            ScheduledFuture<?> future = eventLoop.scheduleAtFixedRate(() -> {
                counter.incrementAndGet();
                latch.countDown();
            }, 50, 100, TimeUnit.MILLISECONDS);

            boolean completed = latch.await(2, TimeUnit.SECONDS);
            future.cancel(false);

            assertThat(completed).isTrue();
            assertThat(counter.get()).isGreaterThanOrEqualTo(3);
        }

        @Test
        @DisplayName("scheduleAtFixedRate() 不接受 period <= 0")
        void rejectsNonPositivePeriod() {
            assertThatThrownBy(() -> eventLoop.scheduleAtFixedRate(() -> {}, 0, 0, TimeUnit.SECONDS))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> eventLoop.scheduleAtFixedRate(() -> {}, 0, -1, TimeUnit.SECONDS))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("周期任务可以被取消")
        void periodicTaskCanBeCancelled() throws InterruptedException {
            AtomicInteger counter = new AtomicInteger(0);

            ScheduledFuture<?> future = eventLoop.scheduleAtFixedRate(() -> {
                counter.incrementAndGet();
            }, 50, 50, TimeUnit.MILLISECONDS);

            // 等待执行几次
            Thread.sleep(200);
            int countBeforeCancel = counter.get();
            
            // 取消任务
            future.cancel(false);
            assertThat(future.isCancelled()).isTrue();

            // 等待一段时间，确认不再执行
            Thread.sleep(200);
            int countAfterCancel = counter.get();

            // 取消后不应再增加（允许取消时正在执行的一次）
            assertThat(countAfterCancel).isLessThanOrEqualTo(countBeforeCancel + 1);
        }
    }

    @Nested
    @DisplayName("任务取消")
    class TaskCancellationTests {

        @Test
        @DisplayName("已取消的任务不会执行")
        void cancelledTaskDoesNotExecute() throws InterruptedException {
            AtomicBoolean executed = new AtomicBoolean(false);

            ScheduledFuture<?> future = eventLoop.schedule(() -> {
                executed.set(true);
            }, 500, TimeUnit.MILLISECONDS);

            // 立即取消
            boolean cancelled = future.cancel(false);
            assertThat(cancelled).isTrue();
            assertThat(future.isCancelled()).isTrue();

            // 等待超过延迟时间
            Thread.sleep(700);

            // 任务不应该被执行
            assertThat(executed.get()).isFalse();
        }

        @Test
        @DisplayName("cancel() 返回 true 表示成功取消")
        void cancelReturnsTrueOnSuccess() {
            ScheduledFuture<?> future = eventLoop.schedule(() -> {}, 1, TimeUnit.SECONDS);
            boolean result = future.cancel(false);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("重复取消返回 false")
        void doubleCancelReturnsFalse() {
            ScheduledFuture<?> future = eventLoop.schedule(() -> {}, 1, TimeUnit.SECONDS);
            future.cancel(false);
            boolean result = future.cancel(false);
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("ScheduledFuture 状态")
    class ScheduledFutureStateTests {

        @Test
        @DisplayName("新创建的 Future 未完成也未取消")
        void newFutureNotDoneNotCancelled() {
            ScheduledFuture<?> future = eventLoop.schedule(() -> {}, 1, TimeUnit.SECONDS);
            assertThat(future.isDone()).isFalse();
            assertThat(future.isCancelled()).isFalse();
            future.cancel(false);
        }

        @Test
        @DisplayName("执行完成后 isDone() 返回 true")
        void isDoneReturnsTrueAfterExecution() throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(1);
            ScheduledFuture<?> future = eventLoop.schedule(latch::countDown, 50, TimeUnit.MILLISECONDS);

            latch.await(2, TimeUnit.SECONDS);
            Thread.sleep(50); // 额外等待确保状态更新

            assertThat(future.isDone()).isTrue();
            assertThat(future.isCancelled()).isFalse();
        }

        @Test
        @DisplayName("getDelay() 返回剩余延迟时间")
        void getDelayReturnsRemainingTime() throws InterruptedException {
            ScheduledFuture<?> future = eventLoop.schedule(() -> {}, 1, TimeUnit.SECONDS);

            long delay = future.getDelay(TimeUnit.MILLISECONDS);
            assertThat(delay).isGreaterThan(900).isLessThanOrEqualTo(1000);

            Thread.sleep(100);
            long delayAfter = future.getDelay(TimeUnit.MILLISECONDS);
            assertThat(delayAfter).isLessThan(delay);

            future.cancel(false);
        }
    }

    @Nested
    @DisplayName("验收场景")
    class AcceptanceScenarioTests {

        @Test
        @DisplayName("验收场景1: schedule() 提交的任务在指定延迟后执行")
        void acceptanceScenario1() throws InterruptedException {
            // Given: EventLoop 已启动
            assertThat(eventLoop.isRunning()).isTrue();

            // When: 提交延迟 100ms 的任务
            CountDownLatch latch = new CountDownLatch(1);
            long startTime = System.currentTimeMillis();
            
            eventLoop.schedule(latch::countDown, 100, TimeUnit.MILLISECONDS);

            // Then: 任务在约 100ms 后执行
            boolean completed = latch.await(2, TimeUnit.SECONDS);
            long elapsed = System.currentTimeMillis() - startTime;

            assertThat(completed).isTrue();
            assertThat(elapsed).isGreaterThanOrEqualTo(90); // 允许误差
        }

        @Test
        @DisplayName("验收场景2: scheduleAtFixedRate() 周期性执行任务")
        void acceptanceScenario2() throws InterruptedException {
            // Given: EventLoop 已启动
            assertThat(eventLoop.isRunning()).isTrue();

            // When: 提交周期 50ms 的任务
            AtomicInteger counter = new AtomicInteger(0);
            ScheduledFuture<?> future = eventLoop.scheduleAtFixedRate(
                    counter::incrementAndGet, 0, 50, TimeUnit.MILLISECONDS);

            // 等待一段时间
            Thread.sleep(300);
            future.cancel(false);

            // Then: 任务应该执行了多次
            assertThat(counter.get()).isGreaterThanOrEqualTo(5);
        }

        @Test
        @DisplayName("验收场景3: 任务可以被取消")
        void acceptanceScenario3() throws InterruptedException {
            // Given: 提交一个延迟任务
            AtomicBoolean executed = new AtomicBoolean(false);
            ScheduledFuture<?> future = eventLoop.schedule(() -> {
                executed.set(true);
            }, 500, TimeUnit.MILLISECONDS);

            // When: 取消任务
            future.cancel(false);

            // Then: 任务不会执行
            Thread.sleep(700);
            assertThat(executed.get()).isFalse();
        }
    }
}
