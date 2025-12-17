package io.netty.channel;

/**
 * Channel 异步操作的结果
 *
 * <p>ChannelFuture 代表一个尚未完成的 I/O 操作的结果。
 * 由于 Netty 中所有 I/O 操作都是异步的，调用 write(), bind(), connect() 等方法
 * 会立即返回一个 ChannelFuture，可以通过它来获取操作结果或添加监听器。
 *
 * <p>使用示例：
 * <pre>
 * ChannelFuture future = channel.write(msg);
 *
 * // 方式1: 阻塞等待完成
 * future.sync();
 *
 * // 方式2: 添加监听器（推荐）
 * future.addListener(f -&gt; {
 *     if (f.isSuccess()) {
 *         System.out.println("写入成功");
 *     } else {
 *         System.out.println("写入失败: " + f.cause());
 *     }
 * });
 * </pre>
 *
 * <p>学习要点：
 * <ul>
 *   <li>ChannelFuture 是异步编程的核心</li>
 *   <li>避免在 EventLoop 线程中调用 sync()，可能导致死锁</li>
 *   <li>推荐使用 addListener() 方式处理结果</li>
 * </ul>
 *
 * @see Channel
 */
public interface ChannelFuture {

    /**
     * 返回关联的 Channel
     *
     * @return 关联的 Channel
     */
    Channel channel();

    /**
     * 判断操作是否成功完成
     *
     * @return 如果操作成功完成返回 true
     */
    boolean isSuccess();

    /**
     * 返回操作失败的原因
     *
     * @return 失败原因，如果成功或未完成返回 null
     */
    Throwable cause();

    /**
     * 添加操作完成后的监听器
     *
     * <p>当操作完成时（无论成功或失败），监听器会被调用。
     * 如果操作已经完成，监听器会立即被调用。
     *
     * @param listener 监听器
     * @return this，便于链式调用
     */
    ChannelFuture addListener(ChannelFutureListener listener);

    /**
     * 同步等待操作完成
     *
     * <p>阻塞当前线程直到操作完成。
     * <b>警告</b>: 不要在 EventLoop 线程中调用此方法。
     *
     * @return this，便于链式调用
     * @throws InterruptedException 如果等待被中断
     */
    ChannelFuture sync() throws InterruptedException;

    /**
     * 等待操作完成
     *
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return 如果在超时前完成返回 true
     * @throws InterruptedException 如果等待被中断
     */
    boolean await(long timeout, java.util.concurrent.TimeUnit unit) throws InterruptedException;

    /**
     * 判断操作是否已完成
     *
     * @return 如果操作已完成返回 true
     */
    boolean isDone();
}
