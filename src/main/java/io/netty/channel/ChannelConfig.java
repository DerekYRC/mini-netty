package io.netty.channel;

import java.util.Map;

/**
 * Channel 的配置接口。
 * 
 * <p>ChannelConfig 封装了 Channel 的所有配置选项，包括 Socket 选项
 * （如 SO_RCVBUF、SO_SNDBUF、TCP_NODELAY）和应用级别选项
 * （如连接超时、自动读取等）。
 * 
 * <p>配置选项可以通过 {@link ChannelOption} 以类型安全的方式设置和获取：
 * <pre>
 * channel.config().setOption(ChannelOption.TCP_NODELAY, true);
 * boolean noDelay = channel.config().getOption(ChannelOption.TCP_NODELAY);
 * </pre>
 * 
 * <p>也可以通过便捷方法直接设置常用选项：
 * <pre>
 * channel.config()
 *        .setConnectTimeoutMillis(5000)
 *        .setAutoRead(true);
 * </pre>
 */
public interface ChannelConfig {

    /**
     * 返回所有配置选项的映射。
     * 
     * @return 配置选项映射
     */
    Map<ChannelOption<?>, Object> getOptions();

    /**
     * 批量设置配置选项。
     * 
     * @param options 配置选项映射
     * @return 如果所有选项都成功设置返回 true
     */
    boolean setOptions(Map<ChannelOption<?>, ?> options);

    /**
     * 获取指定选项的值。
     * 
     * @param option 选项键
     * @param <T> 选项值类型
     * @return 选项值，如果未设置返回 null
     */
    <T> T getOption(ChannelOption<T> option);

    /**
     * 设置指定选项的值。
     * 
     * @param option 选项键
     * @param value 选项值
     * @param <T> 选项值类型
     * @return 如果成功设置返回 true
     */
    <T> boolean setOption(ChannelOption<T> option, T value);

    // ======================== 连接超时 ========================

    /**
     * 获取连接超时时间（毫秒）。
     * 
     * @return 连接超时时间，0 表示无超时
     */
    int getConnectTimeoutMillis();

    /**
     * 设置连接超时时间（毫秒）。
     * 
     * @param connectTimeoutMillis 连接超时时间，0 表示无超时
     * @return this
     * @throws IllegalArgumentException 如果值为负数
     */
    ChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis);

    // ======================== 写缓冲区水位线 ========================

    /**
     * 获取写缓冲区高水位标记。
     * 
     * <p>当写缓冲区中的数据量超过此阈值时，{@link Channel#isWritable()} 
     * 返回 false，用于流量控制。
     * 
     * @return 高水位标记（字节数）
     */
    int getWriteBufferHighWaterMark();

    /**
     * 设置写缓冲区高水位标记。
     * 
     * @param writeBufferHighWaterMark 高水位标记（字节数）
     * @return this
     * @throws IllegalArgumentException 如果值为负数或小于低水位标记
     */
    ChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark);

    /**
     * 获取写缓冲区低水位标记。
     * 
     * <p>当写缓冲区中的数据量低于此阈值时，{@link Channel#isWritable()} 
     * 返回 true。
     * 
     * @return 低水位标记（字节数）
     */
    int getWriteBufferLowWaterMark();

    /**
     * 设置写缓冲区低水位标记。
     * 
     * @param writeBufferLowWaterMark 低水位标记（字节数）
     * @return this
     * @throws IllegalArgumentException 如果值为负数或大于高水位标记
     */
    ChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark);

    // ======================== 自动读取 ========================

    /**
     * 检查是否启用自动读取。
     * 
     * <p>当启用自动读取时，Channel 会自动触发读取操作。
     * 默认值为 true。
     * 
     * @return 如果启用自动读取返回 true
     */
    boolean isAutoRead();

    /**
     * 设置是否启用自动读取。
     * 
     * @param autoRead 是否启用自动读取
     * @return this
     */
    ChannelConfig setAutoRead(boolean autoRead);

    // ======================== 自动关闭 ========================

    /**
     * 检查是否启用自动关闭。
     * 
     * <p>当启用自动关闭时，写入失败会自动关闭 Channel。
     * 默认值为 true。
     * 
     * @return 如果启用自动关闭返回 true
     */
    boolean isAutoClose();

    /**
     * 设置是否启用自动关闭。
     * 
     * @param autoClose 是否启用自动关闭
     * @return this
     */
    ChannelConfig setAutoClose(boolean autoClose);
}
