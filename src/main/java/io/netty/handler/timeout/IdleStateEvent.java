package io.netty.handler.timeout;

/**
 * 空闲状态事件
 *
 * <p>当连接在指定时间内没有 I/O 活动时，{@link IdleStateHandler} 会触发此事件。
 * 该事件会作为用户事件（user event）传递给 Pipeline 中的下一个 Handler。
 *
 * <p>使用示例：
 * <pre>{@code
 * public class MyHandler extends ChannelInboundHandlerAdapter {
 *     @Override
 *     public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
 *         if (evt instanceof IdleStateEvent) {
 *             IdleStateEvent e = (IdleStateEvent) evt;
 *             if (e.state() == IdleState.READER_IDLE) {
 *                 // 读空闲，可能对端已断开
 *                 ctx.close();
 *             } else if (e.state() == IdleState.WRITER_IDLE) {
 *                 // 写空闲，发送心跳
 *                 ctx.writeAndFlush(new PingMessage());
 *             }
 *         }
 *     }
 * }
 * }</pre>
 *
 * <p>学习要点：
 * <ul>
 *   <li>使用枚举区分不同类型的空闲状态</li>
 *   <li>事件对象是不可变的，可以安全地重用</li>
 *   <li>通过 userEventTriggered 方法处理空闲事件</li>
 * </ul>
 *
 * @see IdleState
 * @see IdleStateHandler
 */
public class IdleStateEvent {

    /**
     * 读空闲事件（第一次）
     */
    public static final IdleStateEvent FIRST_READER_IDLE_STATE_EVENT = 
            new IdleStateEvent(IdleState.READER_IDLE, true);

    /**
     * 读空闲事件
     */
    public static final IdleStateEvent READER_IDLE_STATE_EVENT = 
            new IdleStateEvent(IdleState.READER_IDLE, false);

    /**
     * 写空闲事件（第一次）
     */
    public static final IdleStateEvent FIRST_WRITER_IDLE_STATE_EVENT = 
            new IdleStateEvent(IdleState.WRITER_IDLE, true);

    /**
     * 写空闲事件
     */
    public static final IdleStateEvent WRITER_IDLE_STATE_EVENT = 
            new IdleStateEvent(IdleState.WRITER_IDLE, false);

    /**
     * 全部空闲事件（第一次）
     */
    public static final IdleStateEvent FIRST_ALL_IDLE_STATE_EVENT = 
            new IdleStateEvent(IdleState.ALL_IDLE, true);

    /**
     * 全部空闲事件
     */
    public static final IdleStateEvent ALL_IDLE_STATE_EVENT = 
            new IdleStateEvent(IdleState.ALL_IDLE, false);

    /**
     * 空闲状态类型
     */
    private final IdleState state;

    /**
     * 是否是第一次空闲
     */
    private final boolean first;

    /**
     * 构造函数
     *
     * @param state 空闲状态类型
     * @param first 是否是第一次空闲
     */
    protected IdleStateEvent(IdleState state, boolean first) {
        this.state = state;
        this.first = first;
    }

    /**
     * 获取空闲状态类型
     *
     * @return 空闲状态类型
     */
    public IdleState state() {
        return state;
    }

    /**
     * 是否是第一次空闲
     *
     * <p>可用于区分是首次空闲还是持续空闲。
     * 首次空闲可能需要特殊处理，如发送第一个心跳。
     *
     * @return 如果是第一次空闲返回 true
     */
    public boolean isFirst() {
        return first;
    }

    @Override
    public String toString() {
        return "IdleStateEvent(" + state + (first ? ", first" : "") + ")";
    }
}
