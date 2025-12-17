package io.netty.channel;

/**
 * EventLoop 选择器工厂接口
 *
 * <p>EventLoopChooserFactory 负责创建 EventLoopChooser 实例。
 * 工厂可以根据 EventLoop 数量选择最优的选择策略。
 *
 * <p>使用示例：
 * <pre>{@code
 * EventLoop[] eventLoops = ...;
 * EventLoopChooserFactory factory = DefaultEventLoopChooserFactory.INSTANCE;
 * EventLoopChooser chooser = factory.newChooser(eventLoops);
 * 
 * EventLoop loop = chooser.next();
 * }</pre>
 *
 * <p>学习要点：
 * <ul>
 *   <li>工厂模式：将对象创建逻辑封装到工厂类</li>
 *   <li>策略选择：工厂根据条件选择最优策略</li>
 *   <li>解耦：使用者不需要知道具体的选择器实现</li>
 * </ul>
 *
 * @see EventLoopChooser
 * @see DefaultEventLoopChooserFactory
 */
public interface EventLoopChooserFactory {

    /**
     * 创建新的 EventLoopChooser
     *
     * @param eventLoops EventLoop 数组
     * @return 新创建的 EventLoopChooser
     */
    EventLoopChooser newChooser(EventLoop[] eventLoops);
}
