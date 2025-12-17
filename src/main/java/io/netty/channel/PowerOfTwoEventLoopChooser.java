package io.netty.channel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 优化的 2 的幂 EventLoop 选择器
 *
 * <p>当 EventLoop 数量是 2 的幂时，使用位运算代替取模运算，
 * 可以获得更好的性能。
 *
 * <p>优化原理：
 * <pre>
 * // 普通取模运算
 * index % length
 * 
 * // 当 length 是 2 的幂时，可以用位运算代替
 * index & (length - 1)
 * </pre>
 *
 * <p>例如，当 length = 8 时：
 * <ul>
 *   <li>8 的二进制是 1000</li>
 *   <li>8 - 1 = 7 的二进制是 0111</li>
 *   <li>任何数 & 0111 的结果范围是 0-7</li>
 * </ul>
 *
 * <p>学习要点：
 * <ul>
 *   <li>位运算比取模运算快（CPU 指令级别）</li>
 *   <li>这种优化在高频调用场景下效果明显</li>
 *   <li>需要保证 EventLoop 数量是 2 的幂</li>
 * </ul>
 *
 * @see EventLoopChooser
 * @see RoundRobinEventLoopChooser
 */
public class PowerOfTwoEventLoopChooser implements EventLoopChooser {

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
     * @param eventLoops EventLoop 数组，长度必须是 2 的幂
     */
    public PowerOfTwoEventLoopChooser(EventLoop[] eventLoops) {
        if (eventLoops == null || eventLoops.length == 0) {
            throw new IllegalArgumentException("eventLoops must not be null or empty");
        }
        if (!isPowerOfTwo(eventLoops.length)) {
            throw new IllegalArgumentException("eventLoops length must be a power of two");
        }
        this.eventLoops = eventLoops;
    }

    @Override
    public EventLoop next() {
        // 使用位运算代替取模，更高效
        return eventLoops[idx.getAndIncrement() & (eventLoops.length - 1)];
    }

    /**
     * 判断一个数是否是 2 的幂
     *
     * <p>原理：2 的幂的二进制只有一个 1，例如：
     * <ul>
     *   <li>2 = 10</li>
     *   <li>4 = 100</li>
     *   <li>8 = 1000</li>
     * </ul>
     *
     * <p>n & (n - 1) 会将最低位的 1 变成 0：
     * <ul>
     *   <li>8 & 7 = 1000 & 0111 = 0</li>
     *   <li>6 & 5 = 0110 & 0101 = 0100 ≠ 0</li>
     * </ul>
     *
     * @param n 要判断的数
     * @return 如果是 2 的幂返回 true
     */
    public static boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }
}
