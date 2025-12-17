package io.netty.channel;

/**
 * EventLoop 选择器策略接口
 *
 * <p>EventLoopChooser 定义了如何从 EventLoopGroup 中选择下一个 EventLoop 的策略。
 * 不同的选择策略可以实现不同的负载均衡算法。
 *
 * <p>内置策略：
 * <ul>
 *   <li>{@link RoundRobinEventLoopChooser} - 轮询策略，依次选择每个 EventLoop</li>
 *   <li>{@link PowerOfTwoEventLoopChooser} - 优化的轮询策略，当 EventLoop 数量是 2 的幂时使用位运算</li>
 * </ul>
 *
 * <p>学习要点：
 * <ul>
 *   <li>策略模式：将选择算法封装为独立的类</li>
 *   <li>负载均衡：保证每个 EventLoop 被均匀地分配到新 Channel</li>
 *   <li>性能优化：针对特定场景（如 2 的幂）进行优化</li>
 * </ul>
 *
 * @see EventLoopGroup
 * @see EventLoopChooserFactory
 */
public interface EventLoopChooser {

    /**
     * 选择下一个 EventLoop
     *
     * @return 下一个要使用的 EventLoop
     */
    EventLoop next();
}
