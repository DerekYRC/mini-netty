package io.netty.channel;

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 定时任务的 Future 实现
 *
 * <p>封装一个定时任务，包含：
 * <ul>
 *   <li>要执行的任务</li>
 *   <li>执行时间（纳秒）</li>
 *   <li>周期（用于周期性任务）</li>
 *   <li>取消和完成状态</li>
 * </ul>
 *
 * <p>学习要点：
 * <ul>
 *   <li>使用 System.nanoTime() 计算绝对时间</li>
 *   <li>Delayed 接口用于优先级队列排序</li>
 *   <li>period > 0 表示周期性任务</li>
 * </ul>
 */
public class ScheduledTask implements ScheduledFuture<Void>, Runnable {

    /**
     * 要执行的任务
     */
    private final Runnable task;

    /**
     * 执行时间（纳秒，基于 System.nanoTime()）
     */
    private long deadlineNanos;

    /**
     * 执行周期（纳秒）。0 表示一次性任务，>0 表示固定频率任务
     */
    private final long periodNanos;

    /**
     * 所属的 EventLoop
     */
    private final SingleThreadEventLoop eventLoop;

    /**
     * 是否已取消
     */
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    /**
     * 是否已完成
     */
    private final AtomicBoolean done = new AtomicBoolean(false);

    /**
     * 创建一次性定时任务
     *
     * @param eventLoop 所属的 EventLoop
     * @param task      要执行的任务
     * @param delay     延迟时间
     * @param unit      时间单位
     */
    public ScheduledTask(SingleThreadEventLoop eventLoop, Runnable task, long delay, TimeUnit unit) {
        this(eventLoop, task, delay, 0, unit);
    }

    /**
     * 创建定时任务
     *
     * @param eventLoop 所属的 EventLoop
     * @param task      要执行的任务
     * @param delay     初始延迟
     * @param period    执行周期（0 表示一次性任务）
     * @param unit      时间单位
     */
    public ScheduledTask(SingleThreadEventLoop eventLoop, Runnable task, long delay, long period, TimeUnit unit) {
        this.eventLoop = eventLoop;
        this.task = task;
        this.deadlineNanos = System.nanoTime() + unit.toNanos(delay);
        this.periodNanos = unit.toNanos(period);
    }

    /**
     * 返回距离执行时间的剩余时间（纳秒）
     *
     * @return 剩余纳秒数，如果已过期返回负数
     */
    public long delayNanos() {
        return deadlineNanos - System.nanoTime();
    }

    /**
     * 判断任务是否已到期
     *
     * @return 如果已到期返回 true
     */
    public boolean isExpired() {
        return delayNanos() <= 0;
    }

    /**
     * 判断是否是周期性任务
     *
     * @return 如果是周期性任务返回 true
     */
    public boolean isPeriodic() {
        return periodNanos > 0;
    }

    @Override
    public void run() {
        if (cancelled.get()) {
            return;
        }

        try {
            task.run();
        } catch (Throwable t) {
            System.err.println("[ScheduledTask] 任务执行失败: " + t.getMessage());
        }

        if (isPeriodic() && !cancelled.get()) {
            // 计算下次执行时间
            deadlineNanos = System.nanoTime() + periodNanos;
            // 重新加入调度队列
            eventLoop.scheduleFromEventLoop(this);
        } else {
            done.set(true);
        }
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(delayNanos(), TimeUnit.NANOSECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
        if (other == this) {
            return 0;
        }
        if (other instanceof ScheduledTask) {
            ScheduledTask otherTask = (ScheduledTask) other;
            long diff = deadlineNanos - otherTask.deadlineNanos;
            if (diff < 0) {
                return -1;
            } else if (diff > 0) {
                return 1;
            } else {
                return 0;
            }
        }
        long diff = getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS);
        return (diff < 0) ? -1 : (diff > 0) ? 1 : 0;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return cancelled.compareAndSet(false, true);
    }

    @Override
    public boolean isCancelled() {
        return cancelled.get();
    }

    @Override
    public boolean isDone() {
        return done.get() || cancelled.get();
    }

    @Override
    public Void get() throws InterruptedException, ExecutionException {
        throw new UnsupportedOperationException("不支持阻塞获取结果");
    }

    @Override
    public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException("不支持阻塞获取结果");
    }
}
