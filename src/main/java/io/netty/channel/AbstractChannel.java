package io.netty.channel;

import java.net.SocketAddress;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Channel 的抽象基类，提供所有 Channel 实现的公共功能
 *
 * <p>AbstractChannel 提供了：
 * <ul>
 *   <li>Channel 生命周期管理</li>
 *   <li>EventLoop 注册</li>
 *   <li>Pipeline 创建和管理</li>
 *   <li>异步操作的 ChannelFuture 支持</li>
 * </ul>
 *
 * <p>学习要点：
 * <ul>
 *   <li>模板方法模式：定义骨架，子类实现具体细节</li>
 *   <li>Unsafe 内部类：封装不应暴露给用户的底层操作</li>
 *   <li>每个 Channel 都有唯一的 Pipeline</li>
 * </ul>
 *
 * @see Channel
 * @see ChannelPipeline
 */
public abstract class AbstractChannel implements Channel {

    /**
     * 父 Channel（对于服务端接受的连接，指向 ServerSocketChannel）
     */
    private final Channel parent;

    /**
     * Channel 唯一标识
     */
    private final ChannelId id;

    /**
     * Channel 的处理管道
     */
    private final ChannelPipeline pipeline;

    /**
     * 关联的 EventLoop
     */
    private volatile EventLoop eventLoop;

    /**
     * 是否已注册到 EventLoop
     */
    private volatile boolean registered;

    /**
     * 是否已关闭
     */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * 本地地址
     */
    private volatile SocketAddress localAddress;

    /**
     * 远程地址
     */
    private volatile SocketAddress remoteAddress;

    /**
     * 构造函数
     *
     * @param parent 父 Channel，可以为 null
     */
    protected AbstractChannel(Channel parent) {
        this.parent = parent;
        this.id = newId();
        this.pipeline = newChannelPipeline();
    }

    /**
     * 创建新的 Channel ID
     *
     * @return 新的 ChannelId
     */
    protected ChannelId newId() {
        return new DefaultChannelId();
    }

    /**
     * 创建新的 ChannelPipeline
     *
     * @return 新的 ChannelPipeline
     */
    protected ChannelPipeline newChannelPipeline() {
        return new DefaultChannelPipeline(this);
    }

    @Override
    public ChannelId id() {
        return id;
    }

    @Override
    public Channel parent() {
        return parent;
    }

    @Override
    public ChannelPipeline pipeline() {
        return pipeline;
    }

    @Override
    public EventLoop eventLoop() {
        EventLoop loop = this.eventLoop;
        if (loop == null) {
            throw new IllegalStateException("Channel 未注册到 EventLoop");
        }
        return loop;
    }

    @Override
    public boolean isRegistered() {
        return registered;
    }

    @Override
    public boolean isOpen() {
        return !closed.get();
    }

    /**
     * 获取本地地址
     *
     * @return 本地 SocketAddress
     */
    public SocketAddress localAddress() {
        SocketAddress addr = this.localAddress;
        if (addr == null) {
            try {
                addr = localAddress0();
                this.localAddress = addr;
            } catch (Exception e) {
                // ignore
            }
        }
        return addr;
    }

    /**
     * 获取远程地址
     *
     * @return 远程 SocketAddress
     */
    public SocketAddress remoteAddress() {
        SocketAddress addr = this.remoteAddress;
        if (addr == null) {
            try {
                addr = remoteAddress0();
                this.remoteAddress = addr;
            } catch (Exception e) {
                // ignore
            }
        }
        return addr;
    }

    /**
     * 注册到 EventLoop
     *
     * @param eventLoop 要注册的 EventLoop
     * @return 注册操作的 Future
     */
    public ChannelFuture register(EventLoop eventLoop) {
        if (eventLoop == null) {
            throw new NullPointerException("eventLoop");
        }
        if (isRegistered()) {
            throw new IllegalStateException("已经注册到 EventLoop");
        }

        this.eventLoop = eventLoop;
        
        if (eventLoop.inEventLoop()) {
            register0();
        } else {
            eventLoop.execute(this::register0);
        }
        
        return newSucceededFuture();
    }

    /**
     * 实际的注册操作
     */
    private void register0() {
        try {
            doRegister();
            registered = true;
            // 触发 channelRegistered 事件
            pipeline.fireChannelRegistered();
            
            // 如果 Channel 已经是活动状态，触发 channelActive 事件
            if (isActive()) {
                pipeline.fireChannelActive();
            }
        } catch (Exception e) {
            System.err.println("[AbstractChannel] 注册失败: " + e.getMessage());
        }
    }

    @Override
    public ChannelFuture close() {
        if (closed.compareAndSet(false, true)) {
            if (eventLoop != null && eventLoop.inEventLoop()) {
                close0();
            } else if (eventLoop != null) {
                eventLoop.execute(this::close0);
            } else {
                close0();
            }
        }
        return newSucceededFuture();
    }

    /**
     * 实际的关闭操作
     */
    private void close0() {
        try {
            doClose();
            // 触发 channelInactive 事件
            if (registered) {
                pipeline.fireChannelInactive();
                pipeline.fireChannelUnregistered();
            }
        } catch (Exception e) {
            System.err.println("[AbstractChannel] 关闭失败: " + e.getMessage());
        }
    }

    /**
     * 创建成功的 ChannelFuture
     *
     * @return 已完成的 ChannelFuture
     */
    protected ChannelFuture newSucceededFuture() {
        return new DefaultChannelFuture(this, true);
    }

    /**
     * 创建失败的 ChannelFuture
     *
     * @param cause 失败原因
     * @return 已失败的 ChannelFuture
     */
    protected ChannelFuture newFailedFuture(Throwable cause) {
        return new DefaultChannelFuture(this, cause);
    }

    // ========== 子类需要实现的抽象方法 ==========

    /**
     * 获取本地地址的实际实现
     *
     * @return 本地地址
     * @throws Exception 如果获取失败
     */
    protected abstract SocketAddress localAddress0() throws Exception;

    /**
     * 获取远程地址的实际实现
     *
     * @return 远程地址
     * @throws Exception 如果获取失败
     */
    protected abstract SocketAddress remoteAddress0() throws Exception;

    /**
     * 注册到底层传输的实际实现
     *
     * @throws Exception 如果注册失败
     */
    protected abstract void doRegister() throws Exception;

    /**
     * 关闭 Channel 的实际实现
     *
     * @throws Exception 如果关闭失败
     */
    protected abstract void doClose() throws Exception;

    /**
     * 默认的 ChannelId 实现
     */
    private static class DefaultChannelId implements ChannelId {
        private final String id;

        DefaultChannelId() {
            this.id = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }

        @Override
        public String asShortText() {
            return id.substring(0, 8);
        }

        @Override
        public String asLongText() {
            return id;
        }

        @Override
        public int compareTo(ChannelId o) {
            return asLongText().compareTo(o.asLongText());
        }

        @Override
        public String toString() {
            return asShortText();
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ChannelId)) return false;
            return asLongText().equals(((ChannelId) obj).asLongText());
        }
    }
}
