package io.netty.channel;

import java.net.SocketAddress;

/**
 * ChannelOutboundHandler 的适配器类
 *
 * <p>提供所有出站方法的默认实现，用户可以只覆盖需要的方法。
 * 默认行为是将操作传递给下一个 Handler（通过 ctx 方法调用）。
 *
 * <p>使用示例：
 * <pre>{@code
 * public class LoggingOutboundHandler extends ChannelOutboundHandlerAdapter {
 *     @Override
 *     public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
 *         System.out.println("Writing: " + msg);
 *         ctx.write(msg, promise);
 *     }
 *
 *     @Override
 *     public void flush(ChannelHandlerContext ctx) {
 *         System.out.println("Flushing");
 *         ctx.flush();
 *     }
 * }
 * }</pre>
 *
 * <p><b>注意</b>: bind, connect, disconnect 操作会记录日志但不传递，
 * 因为 ChannelHandlerContext 当前不支持这些方法。
 * 完整实现将在后续迭代中添加。
 *
 * @see ChannelOutboundHandler
 * @see ChannelInboundHandlerAdapter
 */
public class ChannelOutboundHandlerAdapter implements ChannelOutboundHandler {

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
     * 默认实现：设置 Promise 成功（bind 由 HeadContext 处理）
     */
    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, 
                     ChannelPromise promise) throws Exception {
        // 默认行为：直接完成，实际绑定由 HeadContext 执行
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
        // 默认行为：直接完成，实际连接由 HeadContext 执行
        if (promise != null) {
            promise.setSuccess();
        }
    }

    /**
     * 默认实现：设置 Promise 成功（disconnect 由 HeadContext 处理）
     */
    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        // 默认行为：直接完成，实际断开由 HeadContext 执行
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
    public void write(ChannelHandlerContext ctx, Object msg, 
                      ChannelPromise promise) throws Exception {
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
