package io.netty.channel;

/**
 * 事件处理器基接口
 *
 * <p>ChannelHandler 是处理 I/O 事件的核心接口。它有两个主要子接口：
 * <ul>
 *   <li>{@link ChannelInboundHandler} - 处理入站事件（数据读取）</li>
 *   <li>{@link ChannelOutboundHandler} - 处理出站事件（数据写入）</li>
 * </ul>
 *
 * <p>生命周期方法：
 * <ul>
 *   <li>handlerAdded - Handler 被添加到 Pipeline 时调用</li>
 *   <li>handlerRemoved - Handler 从 Pipeline 移除时调用</li>
 * </ul>
 *
 * @see ChannelInboundHandler
 * @see ChannelOutboundHandler
 * @see ChannelPipeline
 */
public interface ChannelHandler {

    /**
     * Handler 被添加到 Pipeline 时调用
     *
     * @param ctx Handler 上下文
     * @throws Exception 如果处理过程中发生异常
     */
    void handlerAdded(ChannelHandlerContext ctx) throws Exception;

    /**
     * Handler 从 Pipeline 移除时调用
     *
     * @param ctx Handler 上下文
     * @throws Exception 如果处理过程中发生异常
     */
    void handlerRemoved(ChannelHandlerContext ctx) throws Exception;
}
