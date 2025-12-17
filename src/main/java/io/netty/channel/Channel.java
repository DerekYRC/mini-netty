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

    /**
     * 返回底层 I/O 操作接口
     *
     * <p><b>警告</b>: 这是内部 API，不应该被用户代码直接调用。
     * Unsafe 接口封装了底层的 I/O 操作，如 register、bind、connect 等。
     *
     * @return Unsafe 实例
     */
    Unsafe unsafe();

    /**
     * 底层 I/O 操作接口（内部使用）
     *
     * <p>Unsafe 封装了不应该直接暴露给用户的底层操作。
     * 这些操作会被 Pipeline 中的 HeadContext 调用。
     *
     * <p>学习要点：
     * <ul>
     *   <li>封装底层 I/O 操作，隐藏实现细节</li>
     *   <li>确保操作在正确的线程中执行</li>
     *   <li>管理 Channel 的生命周期</li>
     * </ul>
     */
    interface Unsafe {

        /**
         * 注册 Channel 到 EventLoop
         *
         * @param eventLoop 要注册的 EventLoop
         * @param promise 操作结果通知
         */
        void register(EventLoop eventLoop, ChannelPromise promise);

        /**
         * 绑定到本地地址
         *
         * @param localAddress 本地地址
         * @param promise 操作结果通知
         */
        void bind(java.net.SocketAddress localAddress, ChannelPromise promise);

        /**
         * 连接到远程地址
         *
         * @param remoteAddress 远程地址
         * @param localAddress 本地地址，可以为 null
         * @param promise 操作结果通知
         */
        void connect(java.net.SocketAddress remoteAddress, 
                     java.net.SocketAddress localAddress, 
                     ChannelPromise promise);

        /**
         * 断开连接
         *
         * @param promise 操作结果通知
         */
        void disconnect(ChannelPromise promise);

        /**
         * 关闭 Channel
         *
         * @param promise 操作结果通知
         */
        void close(ChannelPromise promise);

        /**
         * 读取数据
         */
        void beginRead();

        /**
         * 写入消息
         *
         * @param msg 要写入的消息
         * @param promise 操作结果通知
         */
        void write(Object msg, ChannelPromise promise);

        /**
         * 刷新所有待写入的消息
         */
        void flush();
    }
}
