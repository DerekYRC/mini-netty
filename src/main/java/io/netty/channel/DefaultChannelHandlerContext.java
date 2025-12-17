package io.netty.channel;

/**
 * ChannelHandlerContext 的默认实现
 *
 * <p>封装用户定义的 ChannelHandler，作为 Pipeline 链表中的节点。
 */
public class DefaultChannelHandlerContext extends AbstractChannelHandlerContext {

    /**
     * 关联的 Handler
     */
    private final ChannelHandler handler;

    /**
     * 构造函数
     *
     * @param pipeline 所属的 Pipeline
     * @param name     Handler 名称
     * @param handler  关联的 Handler
     */
    DefaultChannelHandlerContext(DefaultChannelPipeline pipeline, String name, ChannelHandler handler) {
        super(pipeline, name, handler);
        this.handler = handler;
    }

    @Override
    public ChannelHandler handler() {
        return handler;
    }
}
