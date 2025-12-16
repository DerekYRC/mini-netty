package io.netty.channel;

/**
 * ChannelHandler 容器，负责事件的分发
 *
 * <p>ChannelPipeline 是一个双向链表，包含了处理入站和出站事件的 Handler。
 * 每个 Channel 都有一个 Pipeline，事件会按顺序通过 Pipeline 中的 Handler。
 *
 * <p>事件流向：
 * <pre>
 *                                                 I/O 请求
 *                                                (write, flush)
 *                                                      |
 *                                                      ▼
 *                                 ┌───────────────────────────────────────┐
 *                                 │              Pipeline                  │
 *                                 │                                        │
 *  Inbound                        │  ┌─────┐  ┌─────┐  ┌─────┐  ┌─────┐   │
 *  (read) ─────────────────────▶  │  │Head │──│  H1 │──│  H2 │──│Tail │   │
 *                                 │  └─────┘  └─────┘  └─────┘  └─────┘   │
 *                                 │                                        │
 *  Outbound                       │                                        │
 *  (write) ◀──────────────────────│                                        │
 *                                 └───────────────────────────────────────┘
 * </pre>
 *
 * <p><b>注意</b>: 这是简化版接口，完整版将在后续迭代中添加更多方法。
 *
 * @see Channel
 * @see ChannelHandler
 */
public interface ChannelPipeline {

    /**
     * 在链尾添加 Handler
     *
     * @param name    Handler 名称
     * @param handler Handler 实例
     * @return this，便于链式调用
     */
    ChannelPipeline addLast(String name, ChannelHandler handler);

    /**
     * 在链头添加 Handler
     *
     * @param name    Handler 名称
     * @param handler Handler 实例
     * @return this，便于链式调用
     */
    ChannelPipeline addFirst(String name, ChannelHandler handler);

    /**
     * 移除指定 Handler
     *
     * @param handler 要移除的 Handler
     * @return this，便于链式调用
     */
    ChannelPipeline remove(ChannelHandler handler);

    /**
     * 获取关联的 Channel
     *
     * @return 关联的 Channel
     */
    Channel channel();

    /**
     * 触发 channelRead 事件
     *
     * @param msg 读取到的消息
     * @return this，便于链式调用
     */
    ChannelPipeline fireChannelRead(Object msg);

    /**
     * 触发 channelActive 事件
     *
     * @return this，便于链式调用
     */
    ChannelPipeline fireChannelActive();

    /**
     * 触发 channelInactive 事件
     *
     * @return this，便于链式调用
     */
    ChannelPipeline fireChannelInactive();

    /**
     * 触发异常事件
     *
     * @param cause 异常原因
     * @return this，便于链式调用
     */
    ChannelPipeline fireExceptionCaught(Throwable cause);
}
