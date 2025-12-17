package io.netty.channel;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 事件循环接口，负责处理 Channel 的 I/O 事件和异步任务
 *
 * <p>EventLoop 是 Netty 的核心组件，它是一个单线程执行器，负责：
 * <ul>
 *   <li>处理注册到其上的 Channel 的所有 I/O 事件</li>
 *   <li>执行提交的普通任务（Runnable）</li>
 *   <li>执行定时任务和周期性任务</li>
 * </ul>
 *
 * <p>关键设计原则：
 * <ul>
 *   <li><b>单线程执行</b>: 所有 I/O 操作和任务都在同一个线程中执行</li>
 *   <li><b>无锁设计</b>: 由于单线程，避免了大部分同步开销</li>
 *   <li><b>Channel 绑定</b>: 一个 Channel 只会绑定到一个 EventLoop</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>
 * EventLoop eventLoop = channel.eventLoop();
 *
 * // 在 EventLoop 线程中执行任务
 * if (eventLoop.inEventLoop()) {
 *     doSomething();
 * } else {
 *     eventLoop.execute(() -&gt; doSomething());
 * }
 *
 * // 提交定时任务
 * eventLoop.schedule(() -&gt; System.out.println("delayed"), 1, TimeUnit.SECONDS);
 * </pre>
 *
 * <p>学习要点：
 * <ul>
 *   <li>EventLoop 继承自 EventLoopGroup（一个 EventLoop 也是一个只有一个元素的 Group）</li>
 *   <li>inEventLoop() 用于判断当前线程是否是 EventLoop 线程</li>
 *   <li>execute() 用于提交任务到 EventLoop 执行</li>
 *   <li>schedule() 用于提交延迟任务</li>
 * </ul>
 *
 * @see EventLoopGroup
 */
public interface EventLoop extends EventLoopGroup {

    /**
     * 返回父 EventLoopGroup
     *
     * @return 父 EventLoopGroup，如果没有则返回 null
     */
    EventLoopGroup parent();

    /**
     * 返回自身（EventLoop 既是 EventLoop 又是只包含自己的 EventLoopGroup）
     *
     * @return 自身
     */
    @Override
    EventLoop next();

    /**
     * 判断当前线程是否是 EventLoop 线程
     *
     * <p>这是一个非常重要的方法，用于确保线程安全：
     * <ul>
     *   <li>如果返回 true，可以直接执行操作</li>
     *   <li>如果返回 false，应该通过 execute() 提交任务</li>
     * </ul>
     *
     * @return 如果当前线程是 EventLoop 线程返回 true
     */
    boolean inEventLoop();

    /**
     * 判断指定线程是否是 EventLoop 线程
     *
     * @param thread 要判断的线程
     * @return 如果指定线程是 EventLoop 线程返回 true
     */
    boolean inEventLoop(Thread thread);

    /**
     * 提交一个任务到 EventLoop 执行
     *
     * <p>如果当前线程是 EventLoop 线程，任务可能会立即执行；
     * 否则任务会被添加到任务队列，等待 EventLoop 线程执行。
     *
     * @param task 要执行的任务
     */
    void execute(Runnable task);

    /**
     * 提交一个定时任务
     *
     * <p>任务会在指定延迟后执行。
     *
     * @param task  要执行的任务
     * @param delay 延迟时间
     * @param unit  时间单位
     * @return 可用于取消任务的 ScheduledFuture
     */
    ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit);

    /**
     * 提交一个周期性任务
     *
     * <p>任务会在初始延迟后首次执行，然后按指定周期重复执行。
     *
     * @param task         要执行的任务
     * @param initialDelay 初始延迟
     * @param period       执行周期
     * @param unit         时间单位
     * @return 可用于取消任务的 ScheduledFuture
     */
    ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit);
}
