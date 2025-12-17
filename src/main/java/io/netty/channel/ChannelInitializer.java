package io.netty.channel;

/**
 * Channel 初始化器
 *
 * <p>用于在 Channel 注册到 EventLoop 后初始化其 ChannelPipeline。
 * 通常与 Bootstrap 配合使用，配置新连接的处理器链。
 *
 * <p>使用示例：
 * <pre>{@code
 * ServerBootstrap b = new ServerBootstrap();
 * b.childHandler(new ChannelInitializer<SocketChannel>() {
 *     @Override
 *     protected void initChannel(SocketChannel ch) {
 *         ch.pipeline().addLast(new StringDecoder());
 *         ch.pipeline().addLast(new StringEncoder());
 *         ch.pipeline().addLast(new MyHandler());
 *     }
 * });
 * }</pre>
 *
 * <p>ChannelInitializer 自身会在 initChannel 完成后自动从 Pipeline 中移除，
 * 因此它不会占用 Pipeline 的位置。
 *
 * @param <C> Channel 类型
 * @see ChannelHandler
 */
public abstract class ChannelInitializer<C extends Channel> extends ChannelInboundHandlerAdapter {

    /**
     * 当 Channel 注册到 EventLoop 后调用此方法
     *
     * @param ctx 上下文
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        // 初始化 Channel
        if (initChannel(ctx)) {
            // 从 Pipeline 中移除自己
            ctx.pipeline().remove(this);
            // 重新触发 channelRegistered 事件给新添加的 handler
            ctx.fireChannelRegistered();
        } else {
            // 如果初始化失败，关闭 Channel
            ctx.close();
        }
    }

    /**
     * 初始化 Channel 的 Pipeline
     *
     * @param ctx 上下文
     * @return 如果初始化成功返回 true
     */
    private boolean initChannel(ChannelHandlerContext ctx) {
        try {
            @SuppressWarnings("unchecked")
            C channel = (C) ctx.channel();
            initChannel(channel);
            return true;
        } catch (Throwable cause) {
            exceptionCaught(ctx, cause);
            return false;
        }
    }

    /**
     * 初始化 Channel
     *
     * <p>子类需要实现此方法，在 Channel 的 Pipeline 上添加所需的 Handler。
     *
     * @param ch 要初始化的 Channel
     * @throws Exception 初始化异常
     */
    protected abstract void initChannel(C ch) throws Exception;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("[ChannelInitializer] 初始化失败: " + cause.getMessage());
        ctx.close();
    }
}
