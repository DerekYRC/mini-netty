package io.netty.channel;

/**
 * Channel 的唯一标识
 *
 * <p>每个 Channel 都有一个全局唯一的 ID，用于识别和调试。
 *
 * <p>ChannelId 包含：
 * <ul>
 *   <li>长格式字符串表示（asLongText）</li>
 *   <li>短格式字符串表示（asShortText）</li>
 * </ul>
 */
public interface ChannelId extends Comparable<ChannelId> {

    /**
     * 返回 Channel ID 的短格式字符串
     *
     * <p>短格式适合日志输出，通常是 ID 的哈希值。
     *
     * @return 短格式字符串
     */
    String asShortText();

    /**
     * 返回 Channel ID 的长格式字符串
     *
     * <p>长格式包含完整的 ID 信息，适合精确识别。
     *
     * @return 长格式字符串
     */
    String asLongText();
}
