package io.netty.channel;

/**
 * ChannelHandler 的上下文，提供 Handler 与 Pipeline 交互的方法
 *
 * <p>ChannelHandlerContext 是 Handler 与 Pipeline 之间的桥梁，它提供：
 * <ul>
 *   <li>获取关联的 Channel、Pipeline、Handler</li>
 *   <li>事件传播方法（fireChannelRead, write 等）</li>
 *   <li>获取 EventLoop</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>
 * public void channelRead(ChannelHandlerContext ctx, Object msg) {
 *     // 处理消息
 *     String processed = process(msg);
 *
 *     // 传递给下一个 Handler
 *     ctx.fireChannelRead(processed);
 *
 *     // 或者写回响应
 *     ctx.writeAndFlush(response);
 * }
 * </pre>
 *
 * <p><b>注意</b>: 这是简化版接口，完整版将在后续迭代中添加更多方法。
 *
 * @see ChannelHandler
 * @see ChannelPipeline
 */
public interface ChannelHandlerContext {

    /**
     * 返回关联的 Channel
     *
     * @return 关联的 Channel
     */
    Channel channel();

    /**
     * 返回关联的 EventLoop
     *
     * @return 关联的 EventLoop
     */
    EventLoop executor();

    /**
     * 返回 Handler 名称
     *
     * @return Handler 名称
     */
    String name();

    /**
     * 返回关联的 Handler
     *
     * @return 关联的 Handler
     */
    ChannelHandler handler();

    /**
     * 返回关联的 Pipeline
     *
     * @return 关联的 Pipeline
     */
    ChannelPipeline pipeline();

    /**
     * 触发下一个 Handler 的 channelRead 事件
     *
     * @param msg 消息
     * @return this，便于链式调用
     */
    ChannelHandlerContext fireChannelRead(Object msg);

    /**
     * 触发下一个 Handler 的 channelActive 事件
     *
     * @return this，便于链式调用
     */
    ChannelHandlerContext fireChannelActive();

    /**
     * 触发下一个 Handler 的 channelInactive 事件
     *
     * @return this，便于链式调用
     */
    ChannelHandlerContext fireChannelInactive();

    /**
     * 触发下一个 Handler 的异常处理
     *
     * @param cause 异常原因
     * @return this，便于链式调用
     */
    ChannelHandlerContext fireExceptionCaught(Throwable cause);

    /**
     * 写入消息到 Channel
     *
     * @param msg 要写入的消息
     * @return 写入操作的 Future
     */
    ChannelFuture write(Object msg);

    /**
     * 刷新所有待写入的消息
     *
     * @return this，便于链式调用
     */
    ChannelHandlerContext flush();

    /**
     * 写入并刷新消息
     *
     * @param msg 要写入的消息
     * @return 写入操作的 Future
     */
    ChannelFuture writeAndFlush(Object msg);

    /**
     * 关闭 Channel
     *
     * @return 关闭操作的 Future
     */
    ChannelFuture close();
}
