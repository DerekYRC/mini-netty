package io.netty.channel;

/**
 * 入站事件处理器接口
 *
 * <p>处理从网络读取的数据和连接状态变化事件。
 *
 * <p>入站事件流向（从网络到应用）：
 * <pre>
 * Network → channelActive → channelRead → channelReadComplete → channelInactive
 * </pre>
 *
 * <p>典型实现示例：
 * <pre>
 * public class EchoHandler extends ChannelInboundHandlerAdapter {
 *     &#64;Override
 *     public void channelRead(ChannelHandlerContext ctx, Object msg) {
 *         ctx.writeAndFlush(msg); // Echo back
 *     }
 * }
 * </pre>
 *
 * @see ChannelHandler
 * @see ChannelOutboundHandler
 */
public interface ChannelInboundHandler extends ChannelHandler {

    /**
     * Channel 注册到 EventLoop 时调用
     *
     * @param ctx 上下文
     * @throws Exception 如果处理过程中发生异常
     */
    void channelRegistered(ChannelHandlerContext ctx) throws Exception;

    /**
     * Channel 从 EventLoop 注销时调用
     *
     * @param ctx 上下文
     * @throws Exception 如果处理过程中发生异常
     */
    void channelUnregistered(ChannelHandlerContext ctx) throws Exception;

    /**
     * Channel 变为活动状态时调用（连接建立）
     *
     * @param ctx 上下文
     * @throws Exception 如果处理过程中发生异常
     */
    void channelActive(ChannelHandlerContext ctx) throws Exception;

    /**
     * Channel 变为非活动状态时调用（连接断开）
     *
     * @param ctx 上下文
     * @throws Exception 如果处理过程中发生异常
     */
    void channelInactive(ChannelHandlerContext ctx) throws Exception;

    /**
     * 读取到数据时调用
     *
     * @param ctx 上下文
     * @param msg 读取到的消息
     * @throws Exception 如果处理过程中发生异常
     */
    void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception;

    /**
     * 一次读取操作完成时调用
     *
     * @param ctx 上下文
     * @throws Exception 如果处理过程中发生异常
     */
    void channelReadComplete(ChannelHandlerContext ctx) throws Exception;

    /**
     * 发生异常时调用
     *
     * @param ctx   上下文
     * @param cause 异常原因
     * @throws Exception 如果处理过程中发生异常
     */
    void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception;
}
