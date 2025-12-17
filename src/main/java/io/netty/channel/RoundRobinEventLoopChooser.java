package io.netty.channel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询 EventLoop 选择器
 *
 * <p>使用简单的轮询（Round-Robin）算法选择 EventLoop。
 * 每次调用 next() 返回下一个 EventLoop，循环遍历所有 EventLoop。
 *
 * <p>实现细节：
 * <ul>
 *   <li>使用 AtomicInteger 作为计数器，保证线程安全</li>
 *   <li>使用取模运算实现循环</li>
 *   <li>适用于任意数量的 EventLoop</li>
 * </ul>
 *
 * <p>学习要点：
 * <ul>
 *   <li>轮询算法简单高效，保证均匀分配</li>
 *   <li>AtomicInteger 的 getAndIncrement() 是原子操作</li>
 *   <li>使用 Math.abs() 处理整数溢出</li>
 * </ul>
 *
 * @see EventLoopChooser
 * @see PowerOfTwoEventLoopChooser
 */
public class RoundRobinEventLoopChooser implements EventLoopChooser {

    /**
     * EventLoop 数组
     */
    private final EventLoop[] eventLoops;

    /**
     * 轮询索引
     */
    private final AtomicInteger idx = new AtomicInteger();

    /**
     * 构造函数
     *
     * @param eventLoops EventLoop 数组
     */
    public RoundRobinEventLoopChooser(EventLoop[] eventLoops) {
        if (eventLoops == null || eventLoops.length == 0) {
            throw new IllegalArgumentException("eventLoops must not be null or empty");
        }
        this.eventLoops = eventLoops;
    }

    @Override
    public EventLoop next() {
        return eventLoops[Math.abs(idx.getAndIncrement() % eventLoops.length)];
    }
}
