package io.netty.handler.timeout;

/**
 * 空闲状态类型枚举
 *
 * <p>定义三种空闲状态：
 * <ul>
 *   <li>{@link #READER_IDLE} - 读空闲，一段时间内没有读取到数据</li>
 *   <li>{@link #WRITER_IDLE} - 写空闲，一段时间内没有写入数据</li>
 *   <li>{@link #ALL_IDLE} - 全部空闲，一段时间内既没有读也没有写</li>
 * </ul>
 *
 * @see IdleStateEvent
 * @see IdleStateHandler
 */
public enum IdleState {

    /**
     * 读空闲
     *
     * <p>在指定的时间内没有接收到任何数据。
     * 通常用于检测对端是否存活。
     */
    READER_IDLE,

    /**
     * 写空闲
     *
     * <p>在指定的时间内没有发送任何数据。
     * 可用于触发心跳发送。
     */
    WRITER_IDLE,

    /**
     * 全部空闲
     *
     * <p>在指定的时间内既没有读也没有写。
     * 可用于检测连接是否完全静默。
     */
    ALL_IDLE
}
