package io.netty.channel;

/**
 * ChannelFuture 的监听器接口
 *
 * <p>当 ChannelFuture 完成时（无论成功或失败），监听器的 operationComplete 方法会被调用。
 *
 * <p>使用示例：
 * <pre>
 * channel.writeAndFlush(msg).addListener(future -&gt; {
 *     if (future.isSuccess()) {
 *         System.out.println("发送成功");
 *     } else {
 *         System.out.println("发送失败: " + future.cause());
 *     }
 * });
 * </pre>
 *
 * <p>常用预定义监听器：
 * <ul>
 *   <li>CLOSE: 操作完成后关闭 Channel</li>
 *   <li>CLOSE_ON_FAILURE: 操作失败时关闭 Channel</li>
 * </ul>
 *
 * @see ChannelFuture
 */
@FunctionalInterface
public interface ChannelFutureListener {

    /**
     * 操作完成后关闭 Channel 的监听器
     */
    ChannelFutureListener CLOSE = future -> future.channel().close();

    /**
     * 操作失败时关闭 Channel 的监听器
     */
    ChannelFutureListener CLOSE_ON_FAILURE = future -> {
        if (!future.isSuccess()) {
            future.channel().close();
        }
    };

    /**
     * 当操作完成时调用
     *
     * @param future 已完成的 ChannelFuture
     * @throws Exception 如果处理过程中发生异常
     */
    void operationComplete(ChannelFuture future) throws Exception;
}
