package io.netty.channel.nio;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * 客户端/连接 NIO Channel 实现
 *
 * <p>NioSocketChannel 封装了 Java NIO 的 SocketChannel，
 * 用于 TCP 连接的读写操作。可以是：
 * <ul>
 *   <li>客户端主动创建的连接</li>
 *   <li>服务端接受的客户端连接</li>
 * </ul>
 *
 * <p>主要功能：
 * <ul>
 *   <li>连接到远程服务器（客户端）</li>
 *   <li>读取和写入数据（OP_READ, OP_WRITE 事件）</li>
 *   <li>管理 TCP 连接的生命周期</li>
 * </ul>
 *
 * <p>学习要点：
 * <ul>
 *   <li>客户端通道关注 OP_CONNECT 和 OP_READ 事件</li>
 *   <li>使用 ByteBuffer 进行数据读写</li>
 *   <li>非阻塞 connect() 可能需要等待 finishConnect()</li>
 * </ul>
 *
 * @see SocketChannel
 * @see NioServerSocketChannel
 */
public class NioSocketChannel extends AbstractNioChannel {

    /**
     * 读取缓冲区大小
     */
    private static final int READ_BUFFER_SIZE = 1024;

    /**
     * 构造函数 - 用于客户端创建新连接
     */
    public NioSocketChannel() {
        super(null, newSocket(), SelectionKey.OP_READ);
    }

    /**
     * 构造函数 - 用于服务端接受的连接
     *
     * @param parent 父 Channel（NioServerSocketChannel）
     * @param socket 已接受的 SocketChannel
     */
    public NioSocketChannel(Channel parent, SocketChannel socket) {
        super(parent, socket, SelectionKey.OP_READ);
    }

    /**
     * 创建新的 SocketChannel
     *
     * @return 新的 SocketChannel
     */
    private static SocketChannel newSocket() {
        try {
            return SocketChannel.open();
        } catch (IOException e) {
            throw new RuntimeException("无法创建 SocketChannel", e);
        }
    }

    /**
     * 获取底层的 SocketChannel
     *
     * @return SocketChannel
     */
    @Override
    protected SocketChannel javaChannel() {
        return (SocketChannel) super.javaChannel();
    }

    @Override
    public boolean isActive() {
        SocketChannel ch = javaChannel();
        return isOpen() && ch.isConnected();
    }

    @Override
    protected SocketAddress localAddress0() throws Exception {
        return javaChannel().getLocalAddress();
    }

    @Override
    protected SocketAddress remoteAddress0() throws Exception {
        return javaChannel().getRemoteAddress();
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        javaChannel().bind(localAddress);
    }

    @Override
    protected Unsafe newUnsafe() {
        return new NioSocketChannelUnsafe();
    }

    /**
     * 连接到远程地址
     *
     * @param remoteAddress 远程地址
     * @return 连接操作的 Future
     */
    public ChannelFuture connect(SocketAddress remoteAddress) {
        try {
            boolean connected = javaChannel().connect(remoteAddress);
            if (connected) {
                System.out.println("[NioSocketChannel] 已连接到 " + remoteAddress);
                // 连接成功，触发 channelActive
                if (isRegistered()) {
                    pipeline().fireChannelActive();
                }
            } else {
                // 连接进行中，需要等待 OP_CONNECT 事件
                System.out.println("[NioSocketChannel] 正在连接 " + remoteAddress);
                // 设置关注 OP_CONNECT 事件
                SelectionKey key = selectionKey();
                if (key != null) {
                    key.interestOps(key.interestOps() | SelectionKey.OP_CONNECT);
                }
            }
            return newSucceededFuture();
        } catch (IOException e) {
            System.err.println("[NioSocketChannel] 连接失败: " + e.getMessage());
            return newFailedFuture(e);
        }
    }

    /**
     * 连接到远程地址
     *
     * @param host 主机名
     * @param port 端口号
     * @return 连接操作的 Future
     */
    public ChannelFuture connect(String host, int port) {
        return connect(new InetSocketAddress(host, port));
    }

    /**
     * 完成连接（用于非阻塞连接）
     *
     * @return 如果连接完成返回 true
     */
    public boolean finishConnect() {
        try {
            boolean finished = javaChannel().finishConnect();
            if (finished) {
                System.out.println("[NioSocketChannel] 连接完成");
                // 取消 OP_CONNECT，设置 OP_READ
                SelectionKey key = selectionKey();
                if (key != null) {
                    key.interestOps((key.interestOps() & ~SelectionKey.OP_CONNECT) | SelectionKey.OP_READ);
                }
                // 触发 channelActive
                pipeline().fireChannelActive();
            }
            return finished;
        } catch (IOException e) {
            System.err.println("[NioSocketChannel] 完成连接失败: " + e.getMessage());
            pipeline().fireExceptionCaught(e);
            return false;
        }
    }

    @Override
    protected void doRead() {
        ByteBuffer buffer = ByteBuffer.allocate(READ_BUFFER_SIZE);
        
        try {
            int bytesRead = javaChannel().read(buffer);
            
            if (bytesRead > 0) {
                buffer.flip();
                // 触发 channelRead 事件，传递 ByteBuffer
                pipeline().fireChannelRead(buffer);
                pipeline().fireChannelReadComplete();
            } else if (bytesRead < 0) {
                // 连接关闭
                System.out.println("[NioSocketChannel] 对端关闭连接");
                close();
            }
        } catch (IOException e) {
            System.err.println("[NioSocketChannel] 读取失败: " + e.getMessage());
            pipeline().fireExceptionCaught(e);
            close();
        }
    }

    @Override
    protected void doWrite(Object msg) throws Exception {
        if (msg instanceof ByteBuffer) {
            ByteBuffer buffer = (ByteBuffer) msg;
            while (buffer.hasRemaining()) {
                javaChannel().write(buffer);
            }
        } else if (msg instanceof byte[]) {
            ByteBuffer buffer = ByteBuffer.wrap((byte[]) msg);
            while (buffer.hasRemaining()) {
                javaChannel().write(buffer);
            }
        } else if (msg instanceof String) {
            ByteBuffer buffer = ByteBuffer.wrap(((String) msg).getBytes());
            while (buffer.hasRemaining()) {
                javaChannel().write(buffer);
            }
        } else {
            throw new IllegalArgumentException("不支持的消息类型: " + msg.getClass());
        }
    }

    /**
     * 写入消息
     *
     * @param msg 要写入的消息
     * @return 写入操作的 Future
     */
    public ChannelFuture writeAndFlush(Object msg) {
        try {
            doWrite(msg);
            return newSucceededFuture();
        } catch (Exception e) {
            return newFailedFuture(e);
        }
    }

    @Override
    protected void doClose() throws Exception {
        System.out.println("[NioSocketChannel] 关闭连接");
        super.doClose();
    }

    /**
     * SocketChannel 的 Unsafe 实现
     */
    private class NioSocketChannelUnsafe extends AbstractNioUnsafe {

        @Override
        protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
            if (localAddress != null) {
                javaChannel().bind(localAddress);
            }

            boolean connected = javaChannel().connect(remoteAddress);
            if (!connected) {
                // 连接进行中，需要等待 OP_CONNECT 事件
                SelectionKey key = selectionKey();
                if (key != null) {
                    key.interestOps(key.interestOps() | SelectionKey.OP_CONNECT);
                }
            }
        }
    }
}
