package io.netty.channel;

/**
 * ChannelInboundHandler 的适配器类
 *
 * <p>提供所有入站方法的默认实现，用户可以只覆盖需要的方法。
 * 默认行为是将事件传递给下一个 Handler。
 *
 * <p>使用示例：
 * <pre>{@code
 * public class EchoServerHandler extends ChannelInboundHandlerAdapter {
 *     @Override
 *     public void channelRead(ChannelHandlerContext ctx, Object msg) {
 *         // 处理消息
 *         ctx.write(msg);
 *     }
 *
 *     @Override
 *     public void channelReadComplete(ChannelHandlerContext ctx) {
 *         ctx.flush();
 *     }
 *
 *     @Override
 *     public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
 *         cause.printStackTrace();
 *         ctx.close();
 *     }
 * }
 * }</pre>
 *
 * @see ChannelInboundHandler
 * @see ChannelOutboundHandlerAdapter
 */
public class ChannelInboundHandlerAdapter implements ChannelInboundHandler {

    /**
     * 默认实现：什么都不做
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // NOOP
    }

    /**
     * 默认实现：什么都不做
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // NOOP
    }

    /**
     * 默认实现：传递给下一个 Handler
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelRegistered();
    }

    /**
     * 默认实现：传递给下一个 Handler
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelUnregistered();
    }

    /**
     * 默认实现：传递给下一个 Handler
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelActive();
    }

    /**
     * 默认实现：传递给下一个 Handler
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
    }

    /**
     * 默认实现：传递给下一个 Handler
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.fireChannelRead(msg);
    }

    /**
     * 默认实现：传递给下一个 Handler
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelReadComplete();
    }

    /**
     * 默认实现：传递给下一个 Handler
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
    }
}
