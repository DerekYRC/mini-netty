package io.netty.channel;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 单线程事件循环的抽象基类
 *
 * <p>SingleThreadEventLoop 是 EventLoop 的核心实现，它保证：
 * <ul>
 *   <li>所有 I/O 操作在同一个线程中执行</li>
 *   <li>任务按提交顺序执行</li>
 *   <li>线程安全地提交任务</li>
 * </ul>
 *
 * <p>核心组件：
 * <ul>
 *   <li><b>thread</b>: 执行事件循环的线程</li>
 *   <li><b>taskQueue</b>: 普通任务队列</li>
 *   <li><b>running</b>: 运行状态标志</li>
 * </ul>
 *
 * <p>学习要点：
 * <ul>
 *   <li>使用 ConcurrentLinkedQueue 保证任务提交的线程安全</li>
 *   <li>inEventLoop() 通过比较线程引用实现</li>
 *   <li>execute() 将任务添加到队列，由事件循环线程处理</li>
 * </ul>
 *
 * @see EventLoop
 * @see NioEventLoop
 */
public abstract class SingleThreadEventLoop implements EventLoop {

    /**
     * 父 EventLoopGroup
     */
    protected final EventLoopGroup parent;

    /**
     * 事件循环线程
     */
    protected volatile Thread thread;

    /**
     * 任务队列
     */
    protected final Queue<Runnable> taskQueue;

    /**
     * 定时任务队列（按执行时间排序）
     */
    protected final PriorityQueue<ScheduledTask> scheduledTaskQueue;

    /**
     * 运行状态
     */
    protected final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * 关闭状态
     */
    protected final AtomicBoolean shutdown = new AtomicBoolean(false);

    /**
     * 终止状态
     */
    protected final AtomicBoolean terminated = new AtomicBoolean(false);

    /**
     * 构造函数
     *
     * @param parent 父 EventLoopGroup
     */
    protected SingleThreadEventLoop(EventLoopGroup parent) {
        this.parent = parent;
        this.taskQueue = new ConcurrentLinkedQueue<>();
        this.scheduledTaskQueue = new PriorityQueue<>();
    }

    @Override
    public EventLoopGroup parent() {
        return parent;
    }

    @Override
    public EventLoop next() {
        return this;
    }

    @Override
    public boolean inEventLoop() {
        return inEventLoop(Thread.currentThread());
    }

    @Override
    public boolean inEventLoop(Thread thread) {
        return thread == this.thread;
    }

    @Override
    public void execute(Runnable task) {
        if (task == null) {
            throw new NullPointerException("task");
        }

        // 添加任务到队列
        taskQueue.offer(task);

        // 确保事件循环已启动
        startIfNeeded();

        // 如果不在 EventLoop 线程，唤醒选择器
        if (!inEventLoop()) {
            wakeup();
        }
    }

    /**
     * 如果事件循环未启动，则启动它
     */
    private void startIfNeeded() {
        if (!running.get()) {
            start();
        }
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        if (task == null) {
            throw new NullPointerException("task");
        }
        if (unit == null) {
            throw new NullPointerException("unit");
        }

        ScheduledTask scheduledTask = new ScheduledTask(this, task, delay, unit);
        
        if (inEventLoop()) {
            scheduledTaskQueue.offer(scheduledTask);
        } else {
            execute(() -> scheduledTaskQueue.offer(scheduledTask));
        }
        
        return scheduledTask;
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        if (task == null) {
            throw new NullPointerException("task");
        }
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        if (period <= 0) {
            throw new IllegalArgumentException("period 必须大于 0");
        }

        ScheduledTask scheduledTask = new ScheduledTask(this, task, initialDelay, period, unit);
        
        if (inEventLoop()) {
            scheduledTaskQueue.offer(scheduledTask);
        } else {
            execute(() -> scheduledTaskQueue.offer(scheduledTask));
        }
        
        return scheduledTask;
    }

    /**
     * 从 EventLoop 线程内重新调度任务（用于周期性任务）
     *
     * @param task 要重新调度的任务
     */
    void scheduleFromEventLoop(ScheduledTask task) {
        scheduledTaskQueue.offer(task);
    }

    @Override
    public ChannelFuture register(Channel channel) {
        // 简化实现：在后续迭代中完善
        throw new UnsupportedOperationException("将在后续迭代实现 Channel 注册");
    }

    @Override
    public Future<?> shutdownGracefully() {
        if (shutdown.compareAndSet(false, true)) {
            wakeup();
        }
        return null; // 简化实现
    }

    @Override
    public boolean isShutdown() {
        return shutdown.get();
    }

    @Override
    public boolean isTerminated() {
        return terminated.get();
    }

    /**
     * 判断事件循环是否正在运行
     *
     * @return 如果正在运行返回 true
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * 启动事件循环
     *
     * <p>在新线程中启动事件循环。
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            thread = new Thread(this::run, getThreadName());
            thread.start();
        }
    }

    /**
     * 获取线程名称
     *
     * @return 线程名称
     */
    protected String getThreadName() {
        return "eventloop-" + Integer.toHexString(hashCode());
    }

    /**
     * 事件循环主逻辑
     *
     * <p>子类实现具体的事件处理逻辑。
     */
    protected abstract void run();

    /**
     * 唤醒事件循环
     *
     * <p>用于在提交任务后唤醒可能阻塞的选择器。
     */
    protected abstract void wakeup();

    /**
     * 运行所有待处理的任务
     *
     * @return 运行的任务数量
     */
    protected int runAllTasks() {
        int count = 0;
        
        // 先运行到期的定时任务
        count += runScheduledTasks();
        
        // 再运行普通任务
        Runnable task;
        while ((task = taskQueue.poll()) != null) {
            try {
                task.run();
                count++;
            } catch (Throwable t) {
                System.err.println("[EventLoop] 任务执行失败: " + t.getMessage());
            }
        }
        return count;
    }

    /**
     * 运行所有到期的定时任务
     *
     * @return 运行的定时任务数量
     */
    protected int runScheduledTasks() {
        int count = 0;
        while (true) {
            ScheduledTask task = scheduledTaskQueue.peek();
            if (task == null || !task.isExpired()) {
                break;
            }
            scheduledTaskQueue.poll();
            if (!task.isCancelled()) {
                try {
                    task.run();
                    count++;
                } catch (Throwable t) {
                    System.err.println("[EventLoop] 定时任务执行失败: " + t.getMessage());
                }
            }
        }
        return count;
    }

    /**
     * 检查是否有待处理的任务
     *
     * @return 如果有任务返回 true
     */
    protected boolean hasTasks() {
        return !taskQueue.isEmpty() || hasScheduledTasks();
    }

    /**
     * 检查是否有待处理的定时任务
     *
     * @return 如果有定时任务返回 true
     */
    protected boolean hasScheduledTasks() {
        ScheduledTask task = scheduledTaskQueue.peek();
        return task != null && task.isExpired();
    }

    /**
     * 获取下一个定时任务的延迟时间（纳秒）
     *
     * @return 延迟时间（纳秒），如果没有定时任务返回 -1
     */
    protected long nextScheduledTaskDelayNanos() {
        ScheduledTask task = scheduledTaskQueue.peek();
        if (task == null) {
            return -1;
        }
        return Math.max(0, task.delayNanos());
    }
}
