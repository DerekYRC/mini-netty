package io.netty.channel.nio;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * 服务端 NIO Channel 实现
 *
 * <p>NioServerSocketChannel 封装了 Java NIO 的 ServerSocketChannel，
 * 用于接受客户端连接。主要功能：
 * <ul>
 *   <li>绑定到指定端口</li>
 *   <li>接受客户端连接（OP_ACCEPT 事件）</li>
 *   <li>为每个新连接创建 NioSocketChannel</li>
 * </ul>
 *
 * <p>典型使用流程：
 * <pre>
 * NioServerSocketChannel serverChannel = new NioServerSocketChannel();
 * serverChannel.bind(new InetSocketAddress(8080));
 * // 注册到 EventLoop，开始接受连接
 * </pre>
 *
 * <p>学习要点：
 * <ul>
 *   <li>服务端通道关注 OP_ACCEPT 事件</li>
 *   <li>accept() 返回新的 SocketChannel</li>
 *   <li>每个客户端连接对应一个 NioSocketChannel</li>
 * </ul>
 *
 * @see ServerSocketChannel
 * @see NioSocketChannel
 */
public class NioServerSocketChannel extends AbstractNioChannel {

    /**
     * 构造函数
     */
    public NioServerSocketChannel() {
        super(null, newSocket(), SelectionKey.OP_ACCEPT);
    }

    /**
     * 创建新的 ServerSocketChannel
     *
     * @return 新的 ServerSocketChannel
     */
    private static ServerSocketChannel newSocket() {
        try {
            return ServerSocketChannel.open();
        } catch (IOException e) {
            throw new RuntimeException("无法创建 ServerSocketChannel", e);
        }
    }

    /**
     * 获取底层的 ServerSocketChannel
     *
     * @return ServerSocketChannel
     */
    @Override
    protected ServerSocketChannel javaChannel() {
        return (ServerSocketChannel) super.javaChannel();
    }

    @Override
    public boolean isActive() {
        return isOpen() && javaChannel().socket().isBound();
    }

    @Override
    protected SocketAddress localAddress0() throws Exception {
        return javaChannel().getLocalAddress();
    }

    @Override
    protected SocketAddress remoteAddress0() throws Exception {
        // ServerSocketChannel 没有远程地址
        return null;
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        javaChannel().bind(localAddress);
        System.out.println("[NioServerSocketChannel] 绑定到 " + localAddress);
    }

    @Override
    protected Unsafe newUnsafe() {
        return new NioServerSocketChannelUnsafe();
    }

    /**
     * 绑定到指定地址
     *
     * @param localAddress 本地地址
     * @return 绑定操作的 Future
     */
    public ChannelFuture bind(SocketAddress localAddress) {
        try {
            javaChannel().bind(localAddress);
            System.out.println("[NioServerSocketChannel] 绑定到 " + localAddress);
            
            // 触发 channelActive 事件
            if (isRegistered()) {
                pipeline().fireChannelActive();
            }
            
            return newSucceededFuture();
        } catch (IOException e) {
            System.err.println("[NioServerSocketChannel] 绑定失败: " + e.getMessage());
            return newFailedFuture(e);
        }
    }

    /**
     * 绑定到指定端口
     *
     * @param port 端口号
     * @return 绑定操作的 Future
     */
    public ChannelFuture bind(int port) {
        return bind(new InetSocketAddress(port));
    }

    @Override
    protected void doRead() {
        // 接受新的客户端连接
        try {
            SocketChannel socketChannel = javaChannel().accept();
            if (socketChannel != null) {
                System.out.println("[NioServerSocketChannel] 接受连接: " + 
                        socketChannel.getRemoteAddress());
                
                // 创建 NioSocketChannel
                NioSocketChannel childChannel = new NioSocketChannel(this, socketChannel);
                
                // 触发 channelRead 事件，传递新的子 Channel
                pipeline().fireChannelRead(childChannel);
            }
        } catch (IOException e) {
            System.err.println("[NioServerSocketChannel] 接受连接失败: " + e.getMessage());
            pipeline().fireExceptionCaught(e);
        }
    }

    @Override
    protected void doWrite(Object msg) throws Exception {
        // ServerSocketChannel 不支持写操作
        throw new UnsupportedOperationException("ServerSocketChannel 不支持写操作");
    }

    @Override
    protected void doClose() throws Exception {
        System.out.println("[NioServerSocketChannel] 关闭服务端通道");
        super.doClose();
    }

    /**
     * ServerSocketChannel 的 Unsafe 实现
     */
    private class NioServerSocketChannelUnsafe extends AbstractNioUnsafe {
        // ServerSocketChannel 使用默认实现
    }
}
