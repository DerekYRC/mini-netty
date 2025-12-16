package io.netty.example.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * NIO 服务端 - ACCEPT 事件处理
 *
 * <p>本类演示了使用 NIO 实现服务端的第一步：处理客户端连接（ACCEPT 事件）。
 * 这是从 BIO 迁移到 NIO 的关键一步。
 *
 * <h2>学习要点</h2>
 * <ul>
 *   <li>ServerSocketChannel 配置为非阻塞模式</li>
 *   <li>注册 OP_ACCEPT 事件监听客户端连接</li>
 *   <li>使用 Selector 事件循环处理连接</li>
 *   <li>accept() 返回的 SocketChannel 代表一个客户端连接</li>
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

            if (readyChannels == 0) {
                continue;
            }

            // 获取就绪的 SelectionKey
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();

                try {
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    }
                    // READ 和 WRITE 事件将在下一个迭代中实现
                } catch (IOException e) {
                    System.err.println("[NioServer] 处理事件失败: " + e.getMessage());
                    key.cancel();
                }

                // 必须手动移除已处理的 key
                keyIterator.remove();
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
            
            // 注册 READ 事件（下一个迭代实现）
            clientChannel.register(selector, SelectionKey.OP_READ);
            
            System.out.println("[NioServer] 接受连接: " + clientChannel.getRemoteAddress());
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
