package io.netty.channel;

import java.util.List;

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
     * 在链尾添加 Handler（使用默认生成的名称）
     *
     * @param handler Handler 实例
     * @return this，便于链式调用
     */
    ChannelPipeline addLast(ChannelHandler handler);

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
     * 移除指定名称的 Handler
     *
     * @param name Handler 名称
     * @return 被移除的 Handler
     */
    ChannelHandler remove(String name);

    /**
     * 获取指定名称的 Handler
     *
     * @param name Handler 名称
     * @return Handler 实例，如果不存在返回 null
     */
    ChannelHandler get(String name);

    /**
     * 获取指定名称的 Context
     *
     * @param name Handler 名称
     * @return ChannelHandlerContext，如果不存在返回 null
     */
    ChannelHandlerContext context(String name);

    /**
     * 获取指定 Handler 的 Context
     *
     * @param handler Handler 实例
     * @return ChannelHandlerContext，如果不存在返回 null
     */
    ChannelHandlerContext context(ChannelHandler handler);

    /**
     * 获取所有 Handler 的名称
     *
     * @return Handler 名称列表
     */
    List<String> names();

    /**
     * 获取关联的 Channel
     *
     * @return 关联的 Channel
     */
    Channel channel();

    /**
     * 触发 channelRegistered 事件
     *
     * @return this，便于链式调用
     */
    ChannelPipeline fireChannelRegistered();

    /**
     * 触发 channelUnregistered 事件
     *
     * @return this，便于链式调用
     */
    ChannelPipeline fireChannelUnregistered();

    /**
     * 触发 channelRead 事件
     *
     * @param msg 读取到的消息
     * @return this，便于链式调用
     */
    ChannelPipeline fireChannelRead(Object msg);

    /**
     * 触发 channelReadComplete 事件
     *
     * @return this，便于链式调用
     */
    ChannelPipeline fireChannelReadComplete();

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

    /**
     * 触发用户自定义事件
     *
     * @param event 用户事件
     * @return this，便于链式调用
     */
    ChannelPipeline fireUserEventTriggered(Object event);

    // ========== 出站操作 ==========

    /**
     * 请求从 Channel 读取数据
     *
     * <p>此方法会从 Pipeline 的尾部开始，向头部传递读取请求。
     *
     * @return this，便于链式调用
     */
    ChannelPipeline read();
}
