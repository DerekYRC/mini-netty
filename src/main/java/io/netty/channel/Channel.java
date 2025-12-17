package io.netty.channel;

/**
 * 网络通道接口，代表一个可进行 I/O 操作的通道
 *
 * <p>Channel 是 Netty 网络抽象的核心接口，它代表了一个打开的连接，
 * 可以是：
 * <ul>
 *   <li>TCP 连接（客户端或服务端）</li>
 *   <li>UDP 数据报</li>
 *   <li>文件 I/O</li>
 * </ul>
 *
 * <p>Channel 提供了以下核心功能：
 * <ul>
 *   <li>状态查询（isOpen, isActive, isRegistered）</li>
 *   <li>配置（config）</li>
 *   <li>I/O 操作（read, write, flush）</li>
 *   <li>获取关联的 EventLoop 和 Pipeline</li>
 * </ul>
 *
 * <p><b>注意</b>: 这是简化版接口，完整版将在后续迭代中添加更多方法。
 *
 * @see EventLoop
 * @see ChannelPipeline
 */
public interface Channel {

    /**
     * 返回 Channel 的唯一标识
     *
     * @return Channel ID
     */
    ChannelId id();

    /**
     * 返回关联的 EventLoop
     *
     * <p>Channel 的所有 I/O 操作都在此 EventLoop 的线程中执行。
     *
     * @return 关联的 EventLoop，如果未注册返回 null
     */
    EventLoop eventLoop();

    /**
     * 返回父 Channel
     *
     * <p>对于服务端接受的连接，返回 ServerSocketChannel；
     * 对于客户端连接或 ServerSocketChannel 本身，返回 null。
     *
     * @return 父 Channel，如果没有返回 null
     */
    Channel parent();

    /**
     * 返回 Channel 的配置
     *
     * @return Channel 配置
     */
    ChannelConfig config();

    /**
     * 返回 ChannelPipeline
     *
     * <p>Pipeline 包含了处理入站和出站事件的 Handler 链。
     *
     * @return Channel 的 Pipeline
     */
    ChannelPipeline pipeline();

    /**
     * 判断 Channel 是否打开
     *
     * @return 如果 Channel 打开返回 true
     */
    boolean isOpen();

    /**
     * 判断 Channel 是否已注册到 EventLoop
     *
     * @return 如果已注册返回 true
     */
    boolean isRegistered();

    /**
     * 判断 Channel 是否处于活动状态
     *
     * <p>对于 TCP 连接，活动状态意味着连接已建立。
     *
     * @return 如果处于活动状态返回 true
     */
    boolean isActive();

    /**
     * 关闭 Channel
     *
     * @return 关闭操作的 Future
     */
    ChannelFuture close();

    /**
     * 请求从 Channel 读取数据
     *
     * <p>此方法触发一次读取操作，读取到的数据会通过 Pipeline 中的
     * ChannelInboundHandler.channelRead() 方法传递。
     *
     * @return this
     */
    Channel read();
}
