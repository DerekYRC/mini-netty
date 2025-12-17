package io.netty.channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ChannelPipeline 的默认实现
 *
 * <p>Pipeline 是一个双向链表，包含入站和出站处理器。
 * 事件按照以下顺序处理：
 * <ul>
 *   <li>入站事件：从头到尾（Head -> ... -> Tail）</li>
 *   <li>出站事件：从尾到头（Tail -> ... -> Head）</li>
 * </ul>
 *
 * <p>学习要点：
 * <ul>
 *   <li>责任链模式：每个 Handler 处理后可以传递给下一个</li>
 *   <li>双向链表实现高效的添加/删除操作</li>
 *   <li>头尾节点封装 I/O 操作</li>
 * </ul>
 *
 * @see ChannelPipeline
 * @see ChannelHandler
 */
public class DefaultChannelPipeline implements ChannelPipeline {

    /**
     * 关联的 Channel
     */
    private final Channel channel;

    /**
     * 链表头节点
     */
    private final AbstractChannelHandlerContext head;

    /**
     * 链表尾节点
     */
    private final AbstractChannelHandlerContext tail;

    /**
     * Handler 名称到 Context 的映射
     */
    private final Map<String, AbstractChannelHandlerContext> name2ctx = new ConcurrentHashMap<>();

    /**
     * 构造函数
     *
     * @param channel 关联的 Channel
     */
    public DefaultChannelPipeline(Channel channel) {
        this.channel = channel;
        
        // 创建头尾节点
        this.head = new HeadContext(this);
        this.tail = new TailContext(this);
        
        // 链接头尾节点
        head.next = tail;
        tail.prev = head;
    }

    @Override
    public Channel channel() {
        return channel;
    }

    @Override
    public ChannelPipeline addFirst(String name, ChannelHandler handler) {
        final AbstractChannelHandlerContext newCtx = newContext(name, handler);
        
        synchronized (this) {
            checkDuplicateName(name);
            
            AbstractChannelHandlerContext nextCtx = head.next;
            newCtx.prev = head;
            newCtx.next = nextCtx;
            head.next = newCtx;
            nextCtx.prev = newCtx;
            
            name2ctx.put(name, newCtx);
        }
        
        callHandlerAdded(newCtx);
        return this;
    }

    @Override
    public ChannelPipeline addLast(String name, ChannelHandler handler) {
        final AbstractChannelHandlerContext newCtx = newContext(name, handler);
        
        synchronized (this) {
            checkDuplicateName(name);
            
            AbstractChannelHandlerContext prevCtx = tail.prev;
            newCtx.prev = prevCtx;
            newCtx.next = tail;
            prevCtx.next = newCtx;
            tail.prev = newCtx;
            
            name2ctx.put(name, newCtx);
        }
        
        callHandlerAdded(newCtx);
        return this;
    }

    @Override
    public ChannelPipeline remove(ChannelHandler handler) {
        AbstractChannelHandlerContext ctx = getContext(handler);
        if (ctx == null) {
            throw new NoSuchElementException("Handler 不在 Pipeline 中");
        }
        remove(ctx);
        return this;
    }

    @Override
    public ChannelHandler remove(String name) {
        AbstractChannelHandlerContext ctx = name2ctx.get(name);
        if (ctx == null) {
            throw new NoSuchElementException("没有名为 '" + name + "' 的 Handler");
        }
        remove(ctx);
        return ctx.handler();
    }

    private void remove(AbstractChannelHandlerContext ctx) {
        synchronized (this) {
            AbstractChannelHandlerContext prev = ctx.prev;
            AbstractChannelHandlerContext next = ctx.next;
            prev.next = next;
            next.prev = prev;
            name2ctx.remove(ctx.name());
        }
        callHandlerRemoved(ctx);
    }

    @Override
    public ChannelHandler get(String name) {
        AbstractChannelHandlerContext ctx = name2ctx.get(name);
        return ctx == null ? null : ctx.handler();
    }

    @Override
    public ChannelHandlerContext context(String name) {
        return name2ctx.get(name);
    }

    @Override
    public ChannelHandlerContext context(ChannelHandler handler) {
        return getContext(handler);
    }

    @Override
    public List<String> names() {
        List<String> names = new ArrayList<>();
        AbstractChannelHandlerContext ctx = head.next;
        while (ctx != tail) {
            names.add(ctx.name());
            ctx = ctx.next;
        }
        return names;
    }

    // ========== 入站事件触发方法 ==========

    @Override
    public ChannelPipeline fireChannelRegistered() {
        head.invokeChannelRegistered();
        return this;
    }

    @Override
    public ChannelPipeline fireChannelUnregistered() {
        head.invokeChannelUnregistered();
        return this;
    }

    @Override
    public ChannelPipeline fireChannelActive() {
        head.invokeChannelActive();
        return this;
    }

    @Override
    public ChannelPipeline fireChannelInactive() {
        head.invokeChannelInactive();
        return this;
    }

    @Override
    public ChannelPipeline fireChannelRead(Object msg) {
        head.invokeChannelRead(msg);
        return this;
    }

    @Override
    public ChannelPipeline fireChannelReadComplete() {
        head.invokeChannelReadComplete();
        return this;
    }

    @Override
    public ChannelPipeline fireExceptionCaught(Throwable cause) {
        head.invokeExceptionCaught(cause);
        return this;
    }

    @Override
    public ChannelPipeline read() {
        tail.read();
        return this;
    }

    // ========== 辅助方法 ==========

    private AbstractChannelHandlerContext newContext(String name, ChannelHandler handler) {
        return new DefaultChannelHandlerContext(this, name, handler);
    }

    private void checkDuplicateName(String name) {
        if (name2ctx.containsKey(name)) {
            throw new IllegalArgumentException("名称 '" + name + "' 已存在");
        }
    }

    private AbstractChannelHandlerContext getContext(ChannelHandler handler) {
        AbstractChannelHandlerContext ctx = head.next;
        while (ctx != tail) {
            if (ctx.handler() == handler) {
                return ctx;
            }
            ctx = ctx.next;
        }
        return null;
    }

    private void callHandlerAdded(AbstractChannelHandlerContext ctx) {
        try {
            ctx.handler().handlerAdded(ctx);
        } catch (Exception e) {
            System.err.println("[DefaultChannelPipeline] handlerAdded 失败: " + e.getMessage());
        }
    }

    private void callHandlerRemoved(AbstractChannelHandlerContext ctx) {
        try {
            ctx.handler().handlerRemoved(ctx);
        } catch (Exception e) {
            System.err.println("[DefaultChannelPipeline] handlerRemoved 失败: " + e.getMessage());
        }
    }

    /**
     * 头节点 Context - 处理出站操作的最终执行
     */
    private static class HeadContext extends AbstractChannelHandlerContext implements ChannelOutboundHandler {

        HeadContext(DefaultChannelPipeline pipeline) {
            super(pipeline, "head", null);
        }

        @Override
        public ChannelHandler handler() {
            return this;
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) {}

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) {}

        @Override
        public void bind(ChannelHandlerContext ctx, java.net.SocketAddress localAddress, ChannelPromise promise) {
            // 实际绑定操作由子类实现
        }

        @Override
        public void connect(ChannelHandlerContext ctx, java.net.SocketAddress remoteAddress,
                            java.net.SocketAddress localAddress, ChannelPromise promise) {
            // 实际连接操作由子类实现
        }

        @Override
        public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) {
            // 实际断开操作由子类实现
        }

        @Override
        public void close(ChannelHandlerContext ctx, ChannelPromise promise) {
            // 实际关闭操作由 Channel 执行
            ctx.channel().close();
        }

        @Override
        public void read(ChannelHandlerContext ctx) {
            // 请求读取操作
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
            // 实际写操作由子类实现
        }

        @Override
        public void flush(ChannelHandlerContext ctx) {
            // 实际 flush 操作由子类实现
        }
    }

    /**
     * 尾节点 Context - 入站事件的终点
     */
    private static class TailContext extends AbstractChannelHandlerContext implements ChannelInboundHandler {

        TailContext(DefaultChannelPipeline pipeline) {
            super(pipeline, "tail", null);
        }

        @Override
        public ChannelHandler handler() {
            return this;
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) {}

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) {}

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) {
            // 入站事件终点，不再传递
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) {}

        @Override
        public void channelActive(ChannelHandlerContext ctx) {}

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {}

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            // 未被消费的消息到达这里
            System.out.println("[TailContext] 未处理的消息: " + msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {}

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            // 未处理的异常到达这里
            System.err.println("[TailContext] 未处理的异常: " + cause.getMessage());
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            // 未处理的用户事件到达这里
        }
    }
}
