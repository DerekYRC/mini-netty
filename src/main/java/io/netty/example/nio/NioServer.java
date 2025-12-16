package io.netty.example.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * NIO 服务端 - 完整实现
 *
 * <p>本类演示了使用 NIO 实现完整的服务端，包括：
 * <ul>
 *   <li>ACCEPT：接受客户端连接</li>
 *   <li>READ：读取客户端数据</li>
 *   <li>WRITE：发送响应数据</li>
 * </ul>
 *
 * <h2>学习要点</h2>
 * <ul>
 *   <li>事件驱动模型：根据事件类型执行不同操作</li>
 *   <li>ByteBuffer 用于读写数据</li>
 *   <li>单线程处理多个连接</li>
 *   <li>非阻塞 I/O 的优势：高并发、低资源消耗</li>
 * </ul>
 *
 * <h2>与 BIO 的对比</h2>
 * <table border="1">
 *   <tr><th>BIO</th><th>NIO</th></tr>
 *   <tr><td>ServerSocket.accept() 阻塞</td><td>Selector.select() 阻塞，但可监控多个 Channel</td></tr>
 *   <tr><td>一个线程处理一个连接</td><td>一个线程处理多个连接</td></tr>
 *   <tr><td>简单直观</td><td>事件驱动，稍复杂</td></tr>
 * </table>
 *
 * @see java.nio.channels.ServerSocketChannel
 * @see java.nio.channels.Selector
 */
public class NioServer {

    private final int port;
    private Selector selector;
    private ServerSocketChannel serverChannel;
    private volatile boolean running;

    /**
     * 创建一个 NIO 服务端
     *
     * @param port 监听端口号
     */
    public NioServer(int port) {
        this.port = port;
    }

    /**
     * 启动服务端并开始处理连接
     *
     * @throws IOException 如果启动失败
     */
    public void start() throws IOException {
        // 1. 创建 Selector
        selector = Selector.open();

        // 2. 创建 ServerSocketChannel
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false); // 必须设置为非阻塞
        serverChannel.bind(new InetSocketAddress(port));

        // 3. 注册 ACCEPT 事件
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        running = true;
        System.out.println("[NioServer] 服务端启动，监听端口: " + port);

        // 4. 事件循环
        while (running) {
            // select() 阻塞直到有事件发生
            int readyChannels = selector.select(1000);

            // 检查服务端是否已关闭
            if (!running || !selector.isOpen()) {
                break;
            }

            if (readyChannels == 0) {
                continue;
            }

            // 获取就绪的 SelectionKey
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while (keyIterator.hasNext() && running) {
                SelectionKey key = keyIterator.next();
                // 先移除再处理，避免并发修改异常
                keyIterator.remove();

                if (!key.isValid()) {
                    continue;
                }

                try {
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    } else if (key.isWritable()) {
                        handleWrite(key);
                    }
                } catch (IOException e) {
                    System.err.println("[NioServer] 处理事件失败: " + e.getMessage());
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException ex) {
                        // ignore
                    }
                }
            }
        }
    }

    /**
     * 处理 ACCEPT 事件 - 接受客户端连接
     *
     * @param key SelectionKey
     * @throws IOException 如果接受连接失败
     */
    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        
        // 接受连接，返回客户端 Channel
        SocketChannel clientChannel = server.accept();
        
        if (clientChannel != null) {
            // 配置客户端 Channel 为非阻塞
            clientChannel.configureBlocking(false);
            
            // 注册 READ 事件
            clientChannel.register(selector, SelectionKey.OP_READ);
            
            System.out.println("[NioServer] 接受连接: " + clientChannel.getRemoteAddress());
        }
    }

    /**
     * 处理 READ 事件 - 读取客户端数据
     *
     * @param key SelectionKey
     * @throws IOException 如果读取失败
     */
    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        
        int bytesRead = clientChannel.read(buffer);
        
        if (bytesRead == -1) {
            // 客户端关闭连接
            System.out.println("[NioServer] 客户端关闭连接: " + clientChannel.getRemoteAddress());
            key.cancel();
            clientChannel.close();
            return;
        }
        
        if (bytesRead > 0) {
            buffer.flip();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            String message = new String(data, StandardCharsets.UTF_8).trim();
            System.out.println("[NioServer] 收到消息: " + message);
            
            // 准备响应数据，附加到 key
            String response = "hello, mini-netty\n";
            key.attach(ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8)));
            
            // 注册 WRITE 事件
            key.interestOps(SelectionKey.OP_WRITE);
        }
    }

    /**
     * 处理 WRITE 事件 - 发送响应数据
     *
     * @param key SelectionKey
     * @throws IOException 如果写入失败
     */
    private void handleWrite(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        
        if (buffer != null) {
            clientChannel.write(buffer);
            System.out.println("[NioServer] 发送响应: hello, mini-netty");
            
            if (!buffer.hasRemaining()) {
                // 写入完成，切换回 READ 事件
                key.attach(null);
                key.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    /**
     * 在后台线程启动服务端
     *
     * @return 启动服务端的线程
     */
    public Thread startInBackground() {
        Thread serverThread = new Thread(() -> {
            try {
                start();
            } catch (IOException e) {
                if (running) {
                    System.err.println("[NioServer] 服务端异常: " + e.getMessage());
                }
            }
        }, "nio-server");
        serverThread.setDaemon(true);
        serverThread.start();

        // 等待服务端启动
        while (selector == null && running) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return serverThread;
    }

    /**
     * 停止服务端
     */
    public void stop() {
        running = false;
        
        // 唤醒阻塞的 select()
        if (selector != null) {
            selector.wakeup();
        }

        // 关闭 ServerSocketChannel
        if (serverChannel != null && serverChannel.isOpen()) {
            try {
                serverChannel.close();
            } catch (IOException e) {
                System.err.println("[NioServer] 关闭 ServerSocketChannel 失败: " + e.getMessage());
            }
        }

        // 关闭 Selector
        if (selector != null && selector.isOpen()) {
            try {
                selector.close();
            } catch (IOException e) {
                System.err.println("[NioServer] 关闭 Selector 失败: " + e.getMessage());
            }
        }

        System.out.println("[NioServer] 服务端已停止");
    }

    /**
     * 获取服务端端口
     *
     * @return 监听端口号
     */
    public int getPort() {
        return port;
    }

    /**
     * 获取 Selector
     *
     * @return Selector 实例
     */
    public Selector getSelector() {
        return selector;
    }

    /**
     * 检查服务端是否正在运行
     *
     * @return 如果服务端正在运行返回 true
     */
    public boolean isRunning() {
        return running && serverChannel != null && serverChannel.isOpen();
    }

    /**
     * 主方法 - 启动服务端
     *
     * @param args 命令行参数（可选：端口号，默认 8080）
     * @throws IOException 如果启动失败
     */
    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        NioServer server = new NioServer(port);
        server.start();
    }
}
