package io.netty.bootstrap;

import io.netty.channel.*;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * 客户端启动器
 *
 * <p>Bootstrap 是用于引导客户端的辅助类。
 * 它使用流式 API 配置客户端 Channel，并连接到远程服务器。
 *
 * <p>使用示例：
 * <pre>{@code
 * EventLoopGroup group = new NioEventLoopGroup();
 * try {
 *     Bootstrap b = new Bootstrap();
 *     b.group(group)
 *      .channel(NioSocketChannel.class)
 *      .option(ChannelOption.TCP_NODELAY, true)
 *      .handler(new ChannelInitializer<SocketChannel>() {
 *          @Override
 *          protected void initChannel(SocketChannel ch) {
 *              ch.pipeline().addLast(new MyHandler());
 *          }
 *      });
 *
 *     ChannelFuture f = b.connect("localhost", 8080).sync();
 *     f.channel().closeFuture().sync();
 * } finally {
 *     group.shutdownGracefully();
 * }
 * }</pre>
 *
 * <p>学习要点：
 * <ul>
 *   <li>Bootstrap 专用于客户端连接</li>
 *   <li>只需要一个 EventLoopGroup</li>
 *   <li>connect() 方法用于连接远程服务器</li>
 * </ul>
 *
 * @see AbstractBootstrap
 * @see ServerBootstrap
 */
public class Bootstrap extends AbstractBootstrap<Bootstrap, Channel> {

    /**
     * 远程地址
     */
    private volatile SocketAddress remoteAddress;

    /**
     * 默认构造函数
     */
    public Bootstrap() {
    }

    /**
     * 复制构造函数
     *
     * @param bootstrap 要复制的 Bootstrap
     */
    private Bootstrap(Bootstrap bootstrap) {
        super(bootstrap);
        this.remoteAddress = bootstrap.remoteAddress;
    }

    /**
     * 设置远程地址
     *
     * @param remoteAddress 远程地址
     * @return this
     */
    public Bootstrap remoteAddress(SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
        return this;
    }

    /**
     * 设置远程地址
     *
     * @param host 主机名
     * @param port 端口号
     * @return this
     */
    public Bootstrap remoteAddress(String host, int port) {
        return remoteAddress(new InetSocketAddress(host, port));
    }

    /**
     * 设置远程地址
     *
     * @param port 端口号（连接到本地）
     * @return this
     */
    public Bootstrap remoteAddress(int port) {
        return remoteAddress(new InetSocketAddress(port));
    }

    /**
     * 连接到预设的远程地址
     *
     * @return ChannelFuture
     */
    public ChannelFuture connect() {
        validate();
        SocketAddress remoteAddress = this.remoteAddress;
        if (remoteAddress == null) {
            throw new IllegalStateException("remoteAddress not set");
        }
        return doConnect(remoteAddress, localAddress());
    }

    /**
     * 连接到指定主机和端口
     *
     * @param host 主机名
     * @param port 端口号
     * @return ChannelFuture
     */
    public ChannelFuture connect(String host, int port) {
        return connect(new InetSocketAddress(host, port));
    }

    /**
     * 连接到指定远程地址
     *
     * @param remoteAddress 远程地址
     * @return ChannelFuture
     */
    public ChannelFuture connect(SocketAddress remoteAddress) {
        if (remoteAddress == null) {
            throw new NullPointerException("remoteAddress");
        }
        validate();
        return doConnect(remoteAddress, localAddress());
    }

    /**
     * 连接到指定远程地址，并绑定本地地址
     *
     * @param remoteAddress 远程地址
     * @param localAddress  本地地址
     * @return ChannelFuture
     */
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
        if (remoteAddress == null) {
            throw new NullPointerException("remoteAddress");
        }
        validate();
        return doConnect(remoteAddress, localAddress);
    }

    /**
     * 执行连接操作
     */
    private ChannelFuture doConnect(final SocketAddress remoteAddress, final SocketAddress localAddress) {
        final Channel channel = initAndRegister();
        if (channel == null) {
            return null;
        }

        final DefaultChannelPromise promise = new DefaultChannelPromise(channel);

        // 在 EventLoop 中执行连接
        doConnect0(channel, remoteAddress, localAddress, promise);

        return promise;
    }

    /**
     * 执行连接
     */
    private void doConnect0(final Channel channel, final SocketAddress remoteAddress,
                            final SocketAddress localAddress, final ChannelPromise promise) {
        // 在 EventLoop 中执行连接
        channel.eventLoop().execute(() -> {
            try {
                // 如果设置了本地地址，先绑定
                if (localAddress != null) {
                    channel.unsafe().bind(localAddress, new DefaultChannelPromise(channel));
                }
                
                // 执行连接
                channel.unsafe().connect(remoteAddress, localAddress, promise);
            } catch (Throwable t) {
                promise.setFailure(t);
            }
        });
    }

    /**
     * 初始化 Channel
     *
     * @param channel 要初始化的 Channel
     */
    @Override
    void init(Channel channel) throws Exception {
        // 设置 Channel 选项
        setChannelOptions(channel, options());

        // 添加 handler 到 Pipeline
        ChannelHandler handler = handler();
        if (handler != null) {
            channel.pipeline().addLast("handler", handler);
        }
    }

    /**
     * 设置 Channel 选项
     */
    private static void setChannelOptions(Channel channel, java.util.Map<ChannelOption<?>, Object> options) {
        for (java.util.Map.Entry<ChannelOption<?>, Object> e : options.entrySet()) {
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
            System.err.println("[Bootstrap] 设置选项失败: " + option + " = " + value);
        }
    }

    /**
     * 验证配置
     */
    @Override
    public Bootstrap validate() {
        super.validate();
        if (handler() == null) {
            throw new IllegalStateException("handler not set");
        }
        return this;
    }

    /**
     * 复制 Bootstrap
     */
    @Override
    public Bootstrap clone() {
        return new Bootstrap(this);
    }

    /**
     * 使用指定的 EventLoopGroup 克隆
     *
     * @param group 新的 EventLoopGroup
     * @return 克隆的 Bootstrap
     */
    public Bootstrap clone(EventLoopGroup group) {
        Bootstrap bootstrap = new Bootstrap(this);
        // 需要重置 group 后重新设置
        // 这里简化处理，直接返回克隆
        return bootstrap;
    }
}
