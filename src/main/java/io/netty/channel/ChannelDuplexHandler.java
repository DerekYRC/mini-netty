package io.netty.channel;

import java.net.SocketAddress;

/**
 * 双向 Handler 适配器类
 *
 * <p>同时实现 ChannelInboundHandler 和 ChannelOutboundHandler，
 * 可以同时处理入站和出站事件。
 *
 * <p>使用示例：
 * <pre>{@code
 * public class LoggingHandler extends ChannelDuplexHandler {
 *     @Override
 *     public void channelRead(ChannelHandlerContext ctx, Object msg) {
 *         System.out.println("Inbound: " + msg);
 *         ctx.fireChannelRead(msg);
 *     }
 *
 *     @Override
 *     public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
 *         System.out.println("Outbound: " + msg);
 *         ctx.write(msg, promise);
 *     }
 * }
 * }</pre>
 *
 * <p>典型用途：
 * <ul>
 *   <li>日志记录（记录双向流量）</li>
 *   <li>监控和统计（请求/响应计数）</li>
 *   <li>超时处理（读写超时控制）</li>
 * </ul>
 *
 * @see ChannelInboundHandler
 * @see ChannelOutboundHandler
 * @see ChannelInboundHandlerAdapter
 * @see ChannelOutboundHandlerAdapter
 */
public class ChannelDuplexHandler implements ChannelInboundHandler, ChannelOutboundHandler {

    // =====================
    // ChannelHandler 生命周期
    // =====================

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

    // =====================
    // 入站事件处理
    // =====================

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

    // =====================
    // 出站事件处理
    // =====================

    /**
     * 默认实现：设置 Promise 成功（bind 由 HeadContext 处理）
     */
    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, 
                     ChannelPromise promise) throws Exception {
        if (promise != null) {
            promise.setSuccess();
        }
    }

    /**
     * 默认实现：设置 Promise 成功（connect 由 HeadContext 处理）
     */
    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,
                        SocketAddress localAddress, ChannelPromise promise) throws Exception {
        if (promise != null) {
            promise.setSuccess();
        }
    }

    /**
     * 默认实现：设置 Promise 成功（disconnect 由 HeadContext 处理）
     */
    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        if (promise != null) {
            promise.setSuccess();
        }
    }

    /**
     * 默认实现：传递给下一个 Handler
     */
    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.close(promise);
    }

    /**
     * 默认实现：传递给下一个 Handler
     */
    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        ctx.read();
    }

    /**
     * 默认实现：传递给下一个 Handler
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ctx.write(msg, promise);
    }

    /**
     * 默认实现：传递给下一个 Handler
     */
    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
