package io.netty.channel;

/**
 * 默认 EventLoop 选择器工厂
 *
 * <p>根据 EventLoop 数量自动选择最优的选择策略：
 * <ul>
 *   <li>如果数量是 2 的幂，使用 {@link PowerOfTwoEventLoopChooser}（位运算优化）</li>
 *   <li>否则使用 {@link RoundRobinEventLoopChooser}（普通取模运算）</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * EventLoopChooserFactory factory = DefaultEventLoopChooserFactory.INSTANCE;
 * 
 * // 8 是 2 的幂，使用 PowerOfTwoEventLoopChooser
 * EventLoopChooser chooser1 = factory.newChooser(new EventLoop[8]);
 * 
 * // 6 不是 2 的幂，使用 RoundRobinEventLoopChooser
 * EventLoopChooser chooser2 = factory.newChooser(new EventLoop[6]);
 * }</pre>
 *
 * <p>学习要点：
 * <ul>
 *   <li>工厂模式：封装对象创建逻辑</li>
 *   <li>策略模式：根据条件选择不同的算法</li>
 *   <li>单例模式：使用 INSTANCE 避免重复创建工厂</li>
 * </ul>
 *
 * @see EventLoopChooserFactory
 * @see PowerOfTwoEventLoopChooser
 * @see RoundRobinEventLoopChooser
 */
public final class DefaultEventLoopChooserFactory implements EventLoopChooserFactory {

    /**
     * 单例实例
     */
    public static final DefaultEventLoopChooserFactory INSTANCE = new DefaultEventLoopChooserFactory();

    /**
     * 私有构造函数，防止外部实例化
     */
    private DefaultEventLoopChooserFactory() {
    }

    @Override
    public EventLoopChooser newChooser(EventLoop[] eventLoops) {
        if (eventLoops == null || eventLoops.length == 0) {
            throw new IllegalArgumentException("eventLoops must not be null or empty");
        }

        if (PowerOfTwoEventLoopChooser.isPowerOfTwo(eventLoops.length)) {
            System.out.println("[EventLoopChooserFactory] 使用 PowerOfTwoEventLoopChooser (length=" + eventLoops.length + ")");
            return new PowerOfTwoEventLoopChooser(eventLoops);
        } else {
            System.out.println("[EventLoopChooserFactory] 使用 RoundRobinEventLoopChooser (length=" + eventLoops.length + ")");
            return new RoundRobinEventLoopChooser(eventLoops);
        }
    }
}
