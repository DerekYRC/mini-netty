package io.netty.channel;

/**
 * ChannelHandlerContext 的抽象基类
 *
 * <p>Context 是 Pipeline 中的节点，封装了 Handler 和链表导航能力。
 * 每个 Context 知道：
 * <ul>
 *   <li>它关联的 Handler</li>
 *   <li>它所在的 Pipeline</li>
 *   <li>链表中的前后节点</li>
 * </ul>
 *
 * <p>学习要点：
 * <ul>
 *   <li>事件通过 Context 在链表中传递</li>
 *   <li>fireXxx() 方法触发下一个入站 Handler</li>
 *   <li>ctx.xxx() 方法触发下一个出站 Handler</li>
 * </ul>
 */
public abstract class AbstractChannelHandlerContext implements ChannelHandlerContext {

    /**
     * 所属的 Pipeline
     */
    final DefaultChannelPipeline pipeline;

    /**
     * Handler 名称
     */
    private final String name;

    /**
     * 关联的 Handler（可能为 null，对于 Head/Tail 节点）
     */
    private final ChannelHandler handler;

    /**
     * 链表前一个节点
     */
    volatile AbstractChannelHandlerContext prev;

    /**
     * 链表后一个节点
     */
    volatile AbstractChannelHandlerContext next;

    /**
     * 构造函数
     *
     * @param pipeline 所属的 Pipeline
     * @param name     Handler 名称
     * @param handler  关联的 Handler
     */
    AbstractChannelHandlerContext(DefaultChannelPipeline pipeline, String name, ChannelHandler handler) {
        this.pipeline = pipeline;
        this.name = name;
        this.handler = handler;
    }

    @Override
    public Channel channel() {
        return pipeline.channel();
    }

    @Override
    public EventLoop eventLoop() {
        return channel().eventLoop();
    }

    @Override
    public ChannelHandler handler() {
        return handler;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public ChannelPipeline pipeline() {
        return pipeline;
    }

    // ========== 入站事件传递方法 ==========

    @Override
    public ChannelHandlerContext fireChannelRegistered() {
        invokeChannelRegistered(findContextInbound());
        return this;
    }

    void invokeChannelRegistered() {
        invokeChannelRegistered(this);
    }

    private void invokeChannelRegistered(AbstractChannelHandlerContext ctx) {
        if (ctx.handler() instanceof ChannelInboundHandler) {
            try {
                ((ChannelInboundHandler) ctx.handler()).channelRegistered(ctx);
            } catch (Exception e) {
                ctx.invokeExceptionCaught(e);
            }
        } else {
            ctx.fireChannelRegistered();
        }
    }

    @Override
    public ChannelHandlerContext fireChannelUnregistered() {
        invokeChannelUnregistered(findContextInbound());
        return this;
    }

    void invokeChannelUnregistered() {
        invokeChannelUnregistered(this);
    }

    private void invokeChannelUnregistered(AbstractChannelHandlerContext ctx) {
        if (ctx.handler() instanceof ChannelInboundHandler) {
            try {
                ((ChannelInboundHandler) ctx.handler()).channelUnregistered(ctx);
            } catch (Exception e) {
                ctx.invokeExceptionCaught(e);
            }
        } else {
            ctx.fireChannelUnregistered();
        }
    }

    @Override
    public ChannelHandlerContext fireChannelActive() {
        invokeChannelActive(findContextInbound());
        return this;
    }

    void invokeChannelActive() {
        invokeChannelActive(this);
    }

    private void invokeChannelActive(AbstractChannelHandlerContext ctx) {
        if (ctx.handler() instanceof ChannelInboundHandler) {
            try {
                ((ChannelInboundHandler) ctx.handler()).channelActive(ctx);
            } catch (Exception e) {
                ctx.invokeExceptionCaught(e);
            }
        } else {
            ctx.fireChannelActive();
        }
    }

    @Override
    public ChannelHandlerContext fireChannelInactive() {
        invokeChannelInactive(findContextInbound());
        return this;
    }

    void invokeChannelInactive() {
        invokeChannelInactive(this);
    }

    private void invokeChannelInactive(AbstractChannelHandlerContext ctx) {
        if (ctx.handler() instanceof ChannelInboundHandler) {
            try {
                ((ChannelInboundHandler) ctx.handler()).channelInactive(ctx);
            } catch (Exception e) {
                ctx.invokeExceptionCaught(e);
            }
        } else {
            ctx.fireChannelInactive();
        }
    }

    @Override
    public ChannelHandlerContext fireChannelRead(Object msg) {
        invokeChannelRead(findContextInbound(), msg);
        return this;
    }

    void invokeChannelRead(Object msg) {
        invokeChannelRead(this, msg);
    }

    private void invokeChannelRead(AbstractChannelHandlerContext ctx, Object msg) {
        if (ctx.handler() instanceof ChannelInboundHandler) {
            try {
                ((ChannelInboundHandler) ctx.handler()).channelRead(ctx, msg);
            } catch (Exception e) {
                ctx.invokeExceptionCaught(e);
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public ChannelHandlerContext fireChannelReadComplete() {
        invokeChannelReadComplete(findContextInbound());
        return this;
    }

    void invokeChannelReadComplete() {
        invokeChannelReadComplete(this);
    }

    private void invokeChannelReadComplete(AbstractChannelHandlerContext ctx) {
        if (ctx.handler() instanceof ChannelInboundHandler) {
            try {
                ((ChannelInboundHandler) ctx.handler()).channelReadComplete(ctx);
            } catch (Exception e) {
                ctx.invokeExceptionCaught(e);
            }
        } else {
            ctx.fireChannelReadComplete();
        }
    }

    @Override
    public ChannelHandlerContext fireExceptionCaught(Throwable cause) {
        invokeExceptionCaught(findContextInbound(), cause);
        return this;
    }

    void invokeExceptionCaught(Throwable cause) {
        invokeExceptionCaught(this, cause);
    }

    private void invokeExceptionCaught(AbstractChannelHandlerContext ctx, Throwable cause) {
        if (ctx.handler() instanceof ChannelInboundHandler) {
            try {
                ((ChannelInboundHandler) ctx.handler()).exceptionCaught(ctx, cause);
            } catch (Exception e) {
                System.err.println("[AbstractChannelHandlerContext] 异常处理器抛出异常: " + e.getMessage());
            }
        } else {
            ctx.fireExceptionCaught(cause);
        }
    }

    // ========== 出站操作方法 ==========

    @Override
    public ChannelFuture write(Object msg) {
        return write(msg, newPromise());
    }

    @Override
    public ChannelFuture write(Object msg, ChannelPromise promise) {
        AbstractChannelHandlerContext ctx = findContextOutbound();
        if (ctx.handler() instanceof ChannelOutboundHandler) {
            try {
                ((ChannelOutboundHandler) ctx.handler()).write(ctx, msg, promise);
            } catch (Exception e) {
                promise.setFailure(e);
            }
        }
        return promise;
    }

    @Override
    public ChannelHandlerContext flush() {
        AbstractChannelHandlerContext ctx = findContextOutbound();
        if (ctx.handler() instanceof ChannelOutboundHandler) {
            try {
                ((ChannelOutboundHandler) ctx.handler()).flush(ctx);
            } catch (Exception e) {
                // ignore
            }
        }
        return this;
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg) {
        return writeAndFlush(msg, newPromise());
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
        write(msg, promise);
        flush();
        return promise;
    }

    @Override
    public ChannelFuture close() {
        return close(newPromise());
    }

    @Override
    public ChannelFuture close(ChannelPromise promise) {
        AbstractChannelHandlerContext ctx = findContextOutbound();
        if (ctx.handler() instanceof ChannelOutboundHandler) {
            try {
                ((ChannelOutboundHandler) ctx.handler()).close(ctx, promise);
            } catch (Exception e) {
                promise.setFailure(e);
            }
        }
        return promise;
    }

    @Override
    public ChannelPromise newPromise() {
        return new DefaultChannelPromise(channel());
    }

    // ========== 链表导航方法 ==========

    /**
     * 查找下一个入站处理器
     *
     * @return 下一个入站 Context
     */
    private AbstractChannelHandlerContext findContextInbound() {
        AbstractChannelHandlerContext ctx = this.next;
        while (ctx != null) {
            if (ctx.handler() instanceof ChannelInboundHandler) {
                return ctx;
            }
            ctx = ctx.next;
        }
        return ctx;
    }

    /**
     * 查找下一个出站处理器（向前查找）
     *
     * @return 下一个出站 Context
     */
    private AbstractChannelHandlerContext findContextOutbound() {
        AbstractChannelHandlerContext ctx = this.prev;
        while (ctx != null) {
            if (ctx.handler() instanceof ChannelOutboundHandler) {
                return ctx;
            }
            ctx = ctx.prev;
        }
        return ctx;
    }
}
