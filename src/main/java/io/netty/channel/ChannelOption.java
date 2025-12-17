package io.netty.channel;

import java.util.Objects;

/**
 * Channel 配置选项的类型安全键。
 * 
 * <p>ChannelOption 用于在 Bootstrap 和 ChannelConfig 中设置 Channel 的配置参数。
 * 每个选项都是强类型的，确保配置值的类型安全性。
 * 
 * <p>常用选项:
 * <ul>
 *   <li>{@link #SO_RCVBUF} - 接收缓冲区大小</li>
 *   <li>{@link #SO_SNDBUF} - 发送缓冲区大小</li>
 *   <li>{@link #SO_KEEPALIVE} - TCP 保活</li>
 *   <li>{@link #SO_REUSEADDR} - 地址复用</li>
 *   <li>{@link #TCP_NODELAY} - 禁用 Nagle 算法</li>
 *   <li>{@link #CONNECT_TIMEOUT_MILLIS} - 连接超时</li>
 * </ul>
 * 
 * @param <T> 选项值的类型
 */
public class ChannelOption<T> {

    // ======================== Socket Options ========================

    /**
     * 接收缓冲区大小 (SO_RCVBUF)
     */
    public static final ChannelOption<Integer> SO_RCVBUF = new ChannelOption<>("SO_RCVBUF");

    /**
     * 发送缓冲区大小 (SO_SNDBUF)
     */
    public static final ChannelOption<Integer> SO_SNDBUF = new ChannelOption<>("SO_SNDBUF");

    /**
     * TCP 保活选项 (SO_KEEPALIVE)
     */
    public static final ChannelOption<Boolean> SO_KEEPALIVE = new ChannelOption<>("SO_KEEPALIVE");

    /**
     * 地址复用选项 (SO_REUSEADDR)
     */
    public static final ChannelOption<Boolean> SO_REUSEADDR = new ChannelOption<>("SO_REUSEADDR");

    /**
     * 关闭时的延迟时间 (SO_LINGER)
     */
    public static final ChannelOption<Integer> SO_LINGER = new ChannelOption<>("SO_LINGER");

    /**
     * 服务端积压队列大小 (SO_BACKLOG)
     */
    public static final ChannelOption<Integer> SO_BACKLOG = new ChannelOption<>("SO_BACKLOG");

    /**
     * 禁用 Nagle 算法 (TCP_NODELAY)
     */
    public static final ChannelOption<Boolean> TCP_NODELAY = new ChannelOption<>("TCP_NODELAY");

    // ======================== Application Options ========================

    /**
     * 连接超时时间（毫秒）
     */
    public static final ChannelOption<Integer> CONNECT_TIMEOUT_MILLIS = 
            new ChannelOption<>("CONNECT_TIMEOUT_MILLIS");

    /**
     * 写缓冲区高水位标记
     */
    public static final ChannelOption<Integer> WRITE_BUFFER_HIGH_WATER_MARK = 
            new ChannelOption<>("WRITE_BUFFER_HIGH_WATER_MARK");

    /**
     * 写缓冲区低水位标记
     */
    public static final ChannelOption<Integer> WRITE_BUFFER_LOW_WATER_MARK = 
            new ChannelOption<>("WRITE_BUFFER_LOW_WATER_MARK");

    /**
     * 自动读取
     */
    public static final ChannelOption<Boolean> AUTO_READ = new ChannelOption<>("AUTO_READ");

    /**
     * 自动关闭
     */
    public static final ChannelOption<Boolean> AUTO_CLOSE = new ChannelOption<>("AUTO_CLOSE");

    // ======================== Instance Fields ========================

    private final String name;

    /**
     * 创建一个新的 ChannelOption。
     * 
     * @param name 选项名称
     */
    public ChannelOption(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    /**
     * 返回选项名称。
     * 
     * @return 选项名称
     */
    public String name() {
        return name;
    }

    /**
     * 验证选项值。
     * 
     * @param value 要验证的值
     * @throws IllegalArgumentException 如果值无效
     */
    public void validate(T value) {
        Objects.requireNonNull(value, "value");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChannelOption<?> that = (ChannelOption<?>) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
