package io.netty.handler.timeout;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 空闲状态处理器
 *
 * <p>IdleStateHandler 用于检测连接的空闲状态。当连接在指定时间内
 * 没有 I/O 活动时，会触发 {@link IdleStateEvent} 事件。
 *
 * <p>支持三种空闲检测：
 * <ul>
 *   <li><b>readerIdleTime</b> - 读空闲时间，超时未读取数据触发 READER_IDLE</li>
 *   <li><b>writerIdleTime</b> - 写空闲时间，超时未写入数据触发 WRITER_IDLE</li>
 *   <li><b>allIdleTime</b> - 全部空闲时间，超时无任何 I/O 触发 ALL_IDLE</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * // 30秒没有读取数据、60秒没有写入数据、90秒完全空闲时触发事件
 * pipeline.addLast("idleStateHandler", new IdleStateHandler(30, 60, 90, TimeUnit.SECONDS));
 * pipeline.addLast("myHandler", new MyHandler());
 *
 * public class MyHandler extends ChannelInboundHandlerAdapter {
 *     @Override
 *     public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
 *         if (evt instanceof IdleStateEvent) {
 *             IdleStateEvent e = (IdleStateEvent) evt;
 *             if (e.state() == IdleState.READER_IDLE) {
 *                 ctx.close(); // 读超时，关闭连接
 *             }
 *         }
 *     }
 * }
 * }</pre>
 *
 * <p>实现原理：
 * <ol>
 *   <li>使用定时任务检测空闲状态</li>
 *   <li>每次读写操作更新最后活动时间</li>
 *   <li>定时任务检查距离最后活动的时间是否超过阈值</li>
 *   <li>超时则触发相应的 IdleStateEvent</li>
 * </ol>
 *
 * <p>学习要点：
 * <ul>
 *   <li>使用 EventLoop 的调度功能实现定时检测</li>
 *   <li>重写 channelRead、write 等方法记录 I/O 活动时间</li>
 *   <li>通过 userEventTriggered 传递空闲事件</li>
 * </ul>
 *
 * @see IdleState
 * @see IdleStateEvent
 */
public class IdleStateHandler extends ChannelInboundHandlerAdapter {

    /**
     * 最小超时时间（纳秒）
     */
    private static final long MIN_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(1);

    /**
     * 读空闲超时时间（纳秒）
     */
    private final long readerIdleTimeNanos;

    /**
     * 写空闲超时时间（纳秒）
     */
    private final long writerIdleTimeNanos;

    /**
     * 全部空闲超时时间（纳秒）
     */
    private final long allIdleTimeNanos;

    /**
     * 读空闲定时任务
     */
    private ScheduledFuture<?> readerIdleTimeout;

    /**
     * 写空闲定时任务
     */
    private ScheduledFuture<?> writerIdleTimeout;

    /**
     * 全部空闲定时任务
     */
    private ScheduledFuture<?> allIdleTimeout;

    /**
     * 最后读取时间（纳秒）
     */
    private long lastReadTime;

    /**
     * 最后写入时间（纳秒）
     */
    private long lastWriteTime;

    /**
     * 是否是第一次读空闲
     */
    private boolean firstReaderIdleEvent = true;

    /**
     * 是否是第一次写空闲
     */
    private boolean firstWriterIdleEvent = true;

    /**
     * 是否是第一次全部空闲
     */
    private boolean firstAllIdleEvent = true;

    /**
     * 处理器状态：0-未初始化，1-已初始化，2-已销毁
     */
    private byte state;

    /**
     * 是否正在读取
     */
    private boolean reading;

    /**
     * 创建空闲状态处理器
     *
     * <p>使用秒作为时间单位。值为 0 表示禁用该检测。
     *
     * @param readerIdleTimeSeconds 读空闲超时（秒），0 表示禁用
     * @param writerIdleTimeSeconds 写空闲超时（秒），0 表示禁用
     * @param allIdleTimeSeconds    全部空闲超时（秒），0 表示禁用
     */
    public IdleStateHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds) {
        this(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds, TimeUnit.SECONDS);
    }

    /**
     * 创建空闲状态处理器
     *
     * @param readerIdleTime 读空闲超时，0 表示禁用
     * @param writerIdleTime 写空闲超时，0 表示禁用
     * @param allIdleTime    全部空闲超时，0 表示禁用
     * @param unit           时间单位
     */
    public IdleStateHandler(long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
        if (unit == null) {
            throw new NullPointerException("unit");
        }

        this.readerIdleTimeNanos = Math.max(unit.toNanos(readerIdleTime), 0);
        this.writerIdleTimeNanos = Math.max(unit.toNanos(writerIdleTime), 0);
        this.allIdleTimeNanos = Math.max(unit.toNanos(allIdleTime), 0);
    }

    /**
     * 获取读空闲超时时间
     *
     * @param unit 时间单位
     * @return 读空闲超时时间
     */
    public long getReaderIdleTime(TimeUnit unit) {
        return unit.convert(readerIdleTimeNanos, TimeUnit.NANOSECONDS);
    }

    /**
     * 获取写空闲超时时间
     *
     * @param unit 时间单位
     * @return 写空闲超时时间
     */
    public long getWriterIdleTime(TimeUnit unit) {
        return unit.convert(writerIdleTimeNanos, TimeUnit.NANOSECONDS);
    }

    /**
     * 获取全部空闲超时时间
     *
     * @param unit 时间单位
     * @return 全部空闲超时时间
     */
    public long getAllIdleTime(TimeUnit unit) {
        return unit.convert(allIdleTimeNanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isActive()) {
            // Channel 已经激活，初始化定时任务
            initialize(ctx);
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        destroy();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isActive()) {
            initialize(ctx);
        }
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        initialize(ctx);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        destroy();
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (readerIdleTimeNanos > 0 || allIdleTimeNanos > 0) {
            reading = true;
            firstReaderIdleEvent = true;
            firstAllIdleEvent = true;
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        if ((readerIdleTimeNanos > 0 || allIdleTimeNanos > 0) && reading) {
            lastReadTime = ticksInNanos();
            reading = false;
        }
        ctx.fireChannelReadComplete();
    }

    /**
     * 写操作完成时更新最后写入时间
     *
     * <p>注意：这是一个简化实现。在真实的 Netty 中，
     * 需要通过拦截 write 和 flush 操作来记录写入时间。
     */
    public void writeComplete() {
        if (writerIdleTimeNanos > 0 || allIdleTimeNanos > 0) {
            lastWriteTime = ticksInNanos();
            firstWriterIdleEvent = true;
            firstAllIdleEvent = true;
        }
    }

    /**
     * 初始化定时任务
     */
    private void initialize(ChannelHandlerContext ctx) {
        if (state == 1 || state == 2) {
            return;
        }
        state = 1;

        long currentTime = ticksInNanos();
        lastReadTime = currentTime;
        lastWriteTime = currentTime;

        if (readerIdleTimeNanos > 0) {
            readerIdleTimeout = schedule(ctx, new ReaderIdleTimeoutTask(ctx),
                    readerIdleTimeNanos, TimeUnit.NANOSECONDS);
        }
        if (writerIdleTimeNanos > 0) {
            writerIdleTimeout = schedule(ctx, new WriterIdleTimeoutTask(ctx),
                    writerIdleTimeNanos, TimeUnit.NANOSECONDS);
        }
        if (allIdleTimeNanos > 0) {
            allIdleTimeout = schedule(ctx, new AllIdleTimeoutTask(ctx),
                    allIdleTimeNanos, TimeUnit.NANOSECONDS);
        }
    }

    /**
     * 销毁定时任务
     */
    private void destroy() {
        state = 2;

        if (readerIdleTimeout != null) {
            readerIdleTimeout.cancel(false);
            readerIdleTimeout = null;
        }
        if (writerIdleTimeout != null) {
            writerIdleTimeout.cancel(false);
            writerIdleTimeout = null;
        }
        if (allIdleTimeout != null) {
            allIdleTimeout.cancel(false);
            allIdleTimeout = null;
        }
    }

    /**
     * 调度定时任务
     */
    private ScheduledFuture<?> schedule(ChannelHandlerContext ctx, Runnable task, long delay, TimeUnit unit) {
        return ctx.channel().eventLoop().schedule(task, delay, unit);
    }

    /**
     * 获取当前时间（纳秒）
     */
    long ticksInNanos() {
        return System.nanoTime();
    }

    /**
     * 触发空闲事件
     */
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        ctx.fireUserEventTriggered(evt);
    }

    /**
     * 创建空闲状态事件
     */
    protected IdleStateEvent newIdleStateEvent(IdleState state, boolean first) {
        switch (state) {
            case READER_IDLE:
                return first ? IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT : IdleStateEvent.READER_IDLE_STATE_EVENT;
            case WRITER_IDLE:
                return first ? IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT : IdleStateEvent.WRITER_IDLE_STATE_EVENT;
            case ALL_IDLE:
                return first ? IdleStateEvent.FIRST_ALL_IDLE_STATE_EVENT : IdleStateEvent.ALL_IDLE_STATE_EVENT;
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }
    }

    /**
     * 读空闲超时任务
     */
    private final class ReaderIdleTimeoutTask implements Runnable {

        private final ChannelHandlerContext ctx;

        ReaderIdleTimeoutTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            if (!ctx.channel().isOpen()) {
                return;
            }

            long nextDelay = readerIdleTimeNanos;
            if (!reading) {
                nextDelay -= ticksInNanos() - lastReadTime;
            }

            if (nextDelay <= 0) {
                // 读空闲超时
                readerIdleTimeout = schedule(ctx, this, readerIdleTimeNanos, TimeUnit.NANOSECONDS);

                boolean first = firstReaderIdleEvent;
                firstReaderIdleEvent = false;

                try {
                    IdleStateEvent event = newIdleStateEvent(IdleState.READER_IDLE, first);
                    channelIdle(ctx, event);
                } catch (Throwable t) {
                    ctx.fireExceptionCaught(t);
                }
            } else {
                // 还未超时，重新调度
                readerIdleTimeout = schedule(ctx, this, nextDelay, TimeUnit.NANOSECONDS);
            }
        }
    }

    /**
     * 写空闲超时任务
     */
    private final class WriterIdleTimeoutTask implements Runnable {

        private final ChannelHandlerContext ctx;

        WriterIdleTimeoutTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            if (!ctx.channel().isOpen()) {
                return;
            }

            long lastWriteTime = IdleStateHandler.this.lastWriteTime;
            long nextDelay = writerIdleTimeNanos - (ticksInNanos() - lastWriteTime);

            if (nextDelay <= 0) {
                // 写空闲超时
                writerIdleTimeout = schedule(ctx, this, writerIdleTimeNanos, TimeUnit.NANOSECONDS);

                boolean first = firstWriterIdleEvent;
                firstWriterIdleEvent = false;

                try {
                    IdleStateEvent event = newIdleStateEvent(IdleState.WRITER_IDLE, first);
                    channelIdle(ctx, event);
                } catch (Throwable t) {
                    ctx.fireExceptionCaught(t);
                }
            } else {
                // 还未超时，重新调度
                writerIdleTimeout = schedule(ctx, this, nextDelay, TimeUnit.NANOSECONDS);
            }
        }
    }

    /**
     * 全部空闲超时任务
     */
    private final class AllIdleTimeoutTask implements Runnable {

        private final ChannelHandlerContext ctx;

        AllIdleTimeoutTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            if (!ctx.channel().isOpen()) {
                return;
            }

            long nextDelay = allIdleTimeNanos;
            if (!reading) {
                long lastIoTime = Math.max(lastReadTime, lastWriteTime);
                nextDelay -= ticksInNanos() - lastIoTime;
            }

            if (nextDelay <= 0) {
                // 全部空闲超时
                allIdleTimeout = schedule(ctx, this, allIdleTimeNanos, TimeUnit.NANOSECONDS);

                boolean first = firstAllIdleEvent;
                firstAllIdleEvent = false;

                try {
                    IdleStateEvent event = newIdleStateEvent(IdleState.ALL_IDLE, first);
                    channelIdle(ctx, event);
                } catch (Throwable t) {
                    ctx.fireExceptionCaught(t);
                }
            } else {
                // 还未超时，重新调度
                allIdleTimeout = schedule(ctx, this, nextDelay, TimeUnit.NANOSECONDS);
            }
        }
    }
}
