package io.netty.channel;

/**
 * 可写入的 ChannelFuture，用于设置操作结果
 *
 * <p>ChannelPromise 是 ChannelFuture 的可写版本，允许设置操作成功或失败的结果。
 * 通常在 Handler 内部使用，用于通知操作完成。
 *
 * <p>使用示例：
 * <pre>
 * public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
 *     try {
 *         // 执行写入操作
 *         doWrite(msg);
 *         promise.setSuccess();
 *     } catch (Exception e) {
 *         promise.setFailure(e);
 *     }
 * }
 * </pre>
 *
 * @see ChannelFuture
 */
public interface ChannelPromise extends ChannelFuture {

    /**
     * 标记操作成功完成
     *
     * @return this，便于链式调用
     */
    ChannelPromise setSuccess();

    /**
     * 尝试标记操作成功完成
     *
     * @return 如果成功标记返回 true，如果已经完成返回 false
     */
    boolean trySuccess();

    /**
     * 标记操作失败
     *
     * @param cause 失败原因
     * @return this，便于链式调用
     */
    ChannelPromise setFailure(Throwable cause);

    /**
     * 尝试标记操作失败
     *
     * @param cause 失败原因
     * @return 如果成功标记返回 true，如果已经完成返回 false
     */
    boolean tryFailure(Throwable cause);

    @Override
    ChannelPromise addListener(ChannelFutureListener listener);

    @Override
    ChannelPromise sync() throws InterruptedException;

    @Override
    ChannelPromise await() throws InterruptedException;
}
