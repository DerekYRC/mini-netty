package io.netty.bootstrap;

import io.netty.channel.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 服务端启动器
 *
 * <p>ServerBootstrap 是用于引导服务端的辅助类。
 * 它使用流式 API 配置服务端 Channel，并启动服务监听。
 *
 * <p>ServerBootstrap 支持主从 Reactor 模型：
 * <ul>
 *   <li>parentGroup (bossGroup) - 负责接受新连接</li>
 *   <li>childGroup (workerGroup) - 负责处理已建立连接的 I/O 操作</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * EventLoopGroup bossGroup = new NioEventLoopGroup(1);
 * EventLoopGroup workerGroup = new NioEventLoopGroup();
 * try {
 *     ServerBootstrap b = new ServerBootstrap();
 *     b.group(bossGroup, workerGroup)
 *      .channel(NioServerSocketChannel.class)
 *      .option(ChannelOption.SO_BACKLOG, 128)
 *      .childOption(ChannelOption.SO_KEEPALIVE, true)
 *      .childHandler(new ChannelInitializer<SocketChannel>() {
 *          @Override
 *          protected void initChannel(SocketChannel ch) {
 *              ch.pipeline().addLast(new MyHandler());
 *          }
 *      });
 *
 *     ChannelFuture f = b.bind(8080).sync();
 *     f.channel().closeFuture().sync();
 * } finally {
 *     workerGroup.shutdownGracefully();
 *     bossGroup.shutdownGracefully();
 * }
 * }</pre>
 *
 * <p>学习要点：
 * <ul>
 *   <li>主从 Reactor 模型的实现</li>
 *   <li>Boss 负责 accept，Worker 负责 read/write</li>
 *   <li>childHandler 用于配置子 Channel 的处理器链</li>
 * </ul>
 *
 * @see AbstractBootstrap
 * @see Bootstrap
 */
public class ServerBootstrap extends AbstractBootstrap<ServerBootstrap, Channel> {

    /**
     * 子 EventLoopGroup（用于处理已接受的连接）
     */
    private volatile EventLoopGroup childGroup;

    /**
     * 子 Channel 选项
     */
    private final Map<ChannelOption<?>, Object> childOptions = new LinkedHashMap<>();

    /**
     * 子 Channel 属性
     */
    private final Map<Object, Object> childAttrs = new LinkedHashMap<>();

    /**
     * 子 Channel 处理器
     */
    private volatile ChannelHandler childHandler;

    /**
     * 默认构造函数
     */
    public ServerBootstrap() {
    }

    /**
     * 复制构造函数
     *
     * @param bootstrap 要复制的 ServerBootstrap
     */
    private ServerBootstrap(ServerBootstrap bootstrap) {
        super(bootstrap);
        this.childGroup = bootstrap.childGroup;
        this.childHandler = bootstrap.childHandler;
        synchronized (bootstrap.childOptions) {
            this.childOptions.putAll(bootstrap.childOptions);
        }
        synchronized (bootstrap.childAttrs) {
            this.childAttrs.putAll(bootstrap.childAttrs);
        }
    }

    /**
     * 设置 Boss 和 Worker EventLoopGroup
     *
     * <p>这是 ServerBootstrap 的核心配置方法，实现主从 Reactor 模型：
     * <ul>
     *   <li>parentGroup - Boss 线程组，负责接受新连接（通常1个线程）</li>
     *   <li>childGroup - Worker 线程组，负责处理 I/O 操作（通常多个线程）</li>
     * </ul>
     *
     * @param parentGroup Boss EventLoopGroup
     * @param childGroup  Worker EventLoopGroup
     * @return this
     */
    public ServerBootstrap group(EventLoopGroup parentGroup, EventLoopGroup childGroup) {
        super.group(parentGroup);
        if (childGroup == null) {
            throw new NullPointerException("childGroup");
        }
        if (this.childGroup != null) {
            throw new IllegalStateException("childGroup set already");
        }
        this.childGroup = childGroup;
        return this;
    }

    /**
     * 设置子 Channel 的选项
     *
     * <p>子 Channel 选项会应用到每个接受的客户端连接。
     * 常用选项包括：
     * <ul>
     *   <li>{@link ChannelOption#SO_KEEPALIVE} - TCP 保活</li>
     *   <li>{@link ChannelOption#TCP_NODELAY} - 禁用 Nagle 算法</li>
     *   <li>{@link ChannelOption#SO_RCVBUF} - 接收缓冲区大小</li>
     * </ul>
     *
     * @param option 选项
     * @param value  值
     * @param <T>    值类型
     * @return this
     */
    public <T> ServerBootstrap childOption(ChannelOption<T> option, T value) {
        if (option == null) {
            throw new NullPointerException("childOption");
        }
        synchronized (childOptions) {
            if (value == null) {
                childOptions.remove(option);
            } else {
                childOptions.put(option, value);
            }
        }
        return this;
    }

    /**
     * 设置子 Channel 的属性
     *
     * @param key   属性键
     * @param value 属性值
     * @return this
     */
    public ServerBootstrap childAttr(Object key, Object value) {
        if (key == null) {
            throw new NullPointerException("childAttr key");
        }
        synchronized (childAttrs) {
            if (value == null) {
                childAttrs.remove(key);
            } else {
                childAttrs.put(key, value);
            }
        }
        return this;
    }

    /**
     * 设置子 Channel 的处理器
     *
     * <p>通常使用 {@link ChannelInitializer} 来配置子 Channel 的处理器链。
     *
     * <p>示例：
     * <pre>{@code
     * b.childHandler(new ChannelInitializer<SocketChannel>() {
     *     @Override
     *     protected void initChannel(SocketChannel ch) {
     *         ch.pipeline().addLast(new StringDecoder());
     *         ch.pipeline().addLast(new MyHandler());
     *     }
     * });
     * }</pre>
     *
     * @param childHandler 子 Channel 处理器
     * @return this
     */
    public ServerBootstrap childHandler(ChannelHandler childHandler) {
        if (childHandler == null) {
            throw new NullPointerException("childHandler");
        }
        this.childHandler = childHandler;
        return this;
    }

    /**
     * 初始化服务端 Channel
     *
     * <p>为服务端 Channel 配置处理器，该处理器会在接受新连接时：
     * <ul>
     *   <li>为子 Channel 设置选项和属性</li>
     *   <li>将子 Channel 注册到 childGroup</li>
     *   <li>为子 Channel 添加 childHandler</li>
     * </ul>
     *
     * @param channel 服务端 Channel
     */
    @Override
    void init(Channel channel) throws Exception {
        // 设置服务端 Channel 的选项
        setChannelOptions(channel, options());

        // 获取 Pipeline
        ChannelPipeline p = channel.pipeline();

        // 保存子 Channel 配置到局部变量
        final EventLoopGroup currentChildGroup = childGroup;
        final ChannelHandler currentChildHandler = childHandler;
        final Map<ChannelOption<?>, Object> currentChildOptions;
        final Map<Object, Object> currentChildAttrs;
        synchronized (childOptions) {
            currentChildOptions = new LinkedHashMap<>(childOptions);
        }
        synchronized (childAttrs) {
            currentChildAttrs = new LinkedHashMap<>(childAttrs);
        }

        // 添加 ServerBootstrapAcceptor 处理器
        // 该处理器负责接受新连接并配置子 Channel
        p.addLast("ServerBootstrapAcceptor", new ServerBootstrapAcceptor(
                currentChildGroup, currentChildHandler, currentChildOptions, currentChildAttrs));

        // 如果设置了 handler，也添加到服务端 Pipeline
        ChannelHandler handler = handler();
        if (handler != null) {
            p.addLast("handler", handler);
        }
    }

    /**
     * 设置 Channel 选项
     */
    private static void setChannelOptions(Channel channel, Map<ChannelOption<?>, Object> options) {
        for (Map.Entry<ChannelOption<?>, Object> e : options.entrySet()) {
            setChannelOption(channel, e.getKey(), e.getValue());
        }
    }

    /**
     * 设置单个 Channel 选项
     */
    @SuppressWarnings("unchecked")
    private static void setChannelOption(Channel channel, ChannelOption<?> option, Object value) {
        try {
            ChannelConfig config = channel.config();
            if (config != null) {
                config.setOption((ChannelOption<Object>) option, value);
            }
        } catch (Exception e) {
            System.err.println("[ServerBootstrap] 设置选项失败: " + option + " = " + value);
        }
    }

    /**
     * 验证配置
     */
    @Override
    public ServerBootstrap validate() {
        super.validate();
        if (childHandler == null) {
            throw new IllegalStateException("childHandler not set");
        }
        if (childGroup == null) {
            // 如果没有设置 childGroup，使用 parentGroup
            System.out.println("[ServerBootstrap] childGroup 未设置，使用 parentGroup");
            childGroup = group();
        }
        return this;
    }

    /**
     * 复制 ServerBootstrap
     */
    @Override
    public ServerBootstrap clone() {
        return new ServerBootstrap(this);
    }

    /**
     * 获取子 EventLoopGroup
     */
    public final EventLoopGroup childGroup() {
        return childGroup;
    }

    /**
     * 服务端接受器
     *
     * <p>这是一个内部 Handler，负责处理服务端接受的新连接。
     * 当服务端 Channel 接受到新连接时，它会：
     * <ol>
     *   <li>接收 channelRead 事件中的子 Channel</li>
     *   <li>为子 Channel 配置选项和属性</li>
     *   <li>添加 childHandler 到子 Channel 的 Pipeline</li>
     *   <li>将子 Channel 注册到 childGroup</li>
     * </ol>
     */
    private static class ServerBootstrapAcceptor extends ChannelInboundHandlerAdapter {

        private final EventLoopGroup childGroup;
        private final ChannelHandler childHandler;
        private final Map<ChannelOption<?>, Object> childOptions;
        private final Map<Object, Object> childAttrs;

        ServerBootstrapAcceptor(
                EventLoopGroup childGroup,
                ChannelHandler childHandler,
                Map<ChannelOption<?>, Object> childOptions,
                Map<Object, Object> childAttrs) {
            this.childGroup = childGroup;
            this.childHandler = childHandler;
            this.childOptions = childOptions;
            this.childAttrs = childAttrs;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            // msg 是新接受的子 Channel
            final Channel child = (Channel) msg;

            // 添加 childHandler 到子 Channel 的 Pipeline
            child.pipeline().addLast("childHandler", childHandler);

            // 设置子 Channel 的选项
            setChannelOptions(child, childOptions);

            // 设置子 Channel 的属性 (如果需要，可以扩展 Channel 接口支持属性)
            // 这里暂时不实现属性设置

            System.out.println("[ServerBootstrapAcceptor] 新连接已接受: " + child);

            try {
                // 将子 Channel 注册到 childGroup
                childGroup.register(child).addListener(future -> {
                    if (!future.isSuccess()) {
                        System.err.println("[ServerBootstrapAcceptor] 注册子 Channel 失败: " + 
                                future.cause().getMessage());
                        child.close();
                    }
                });
            } catch (Exception e) {
                System.err.println("[ServerBootstrapAcceptor] 注册子 Channel 异常: " + e.getMessage());
                child.close();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            System.err.println("[ServerBootstrapAcceptor] 异常: " + cause.getMessage());
            ctx.fireExceptionCaught(cause);
        }
    }
}
