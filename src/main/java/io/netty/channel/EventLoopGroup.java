package io.netty.channel;

import java.util.concurrent.Future;

/**
 * 事件循环组接口，管理一组 EventLoop
 *
 * <p>EventLoopGroup 是 Netty 的核心组件之一，负责：
 * <ul>
 *   <li>管理一组 EventLoop 实例</li>
 *   <li>提供 Channel 注册功能</li>
 *   <li>负载均衡地选择 EventLoop</li>
 * </ul>
 *
 * <p>典型用法：
 * <pre>
 * EventLoopGroup bossGroup = new NioEventLoopGroup(1);   // 接受连接
 * EventLoopGroup workerGroup = new NioEventLoopGroup();  // 处理I/O
 * try {
 *     // 使用 bossGroup 和 workerGroup
 * } finally {
 *     bossGroup.shutdownGracefully();
 *     workerGroup.shutdownGracefully();
 * }
 * </pre>
 *
 * <p>学习要点：
 * <ul>
 *   <li>EventLoopGroup 是 EventLoop 的管理者</li>
 *   <li>通过 next() 方法选择下一个 EventLoop</li>
 *   <li>支持优雅关闭以释放资源</li>
 * </ul>
 *
 * @see EventLoop
 */
public interface EventLoopGroup {

    /**
     * 返回下一个要使用的 EventLoop
     *
     * <p>使用轮询或其他策略选择一个 EventLoop。
     * 用于为新连接分配 EventLoop。
     *
     * @return 下一个 EventLoop
     */
    EventLoop next();

    /**
     * 注册一个 Channel 到 EventLoopGroup
     *
     * <p>Channel 会被分配到一个 EventLoop，
     * 之后该 Channel 的所有 I/O 操作都在该 EventLoop 线程中执行。
     *
     * @param channel 要注册的 Channel
     * @return 注册结果的 Future
     */
    ChannelFuture register(Channel channel);

    /**
     * 优雅关闭所有 EventLoop
     *
     * <p>优雅关闭意味着：
     * <ul>
     *   <li>拒绝接受新任务</li>
     *   <li>等待已提交的任务完成</li>
     *   <li>释放所有资源</li>
     * </ul>
     *
     * @return 关闭结果的 Future
     */
    Future<?> shutdownGracefully();

    /**
     * 判断 EventLoopGroup 是否已关闭
     *
     * @return 如果已关闭返回 true
     */
    boolean isShutdown();

    /**
     * 判断所有 EventLoop 是否已终止
     *
     * @return 如果所有 EventLoop 都已终止返回 true
     */
    boolean isTerminated();
}
