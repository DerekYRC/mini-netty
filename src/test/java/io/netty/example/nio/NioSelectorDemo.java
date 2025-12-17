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
 * NIO Selector 多路复用演示
 *
 * <p>本类演示了 Java NIO 的核心组件 Selector，它可以让单个线程处理多个 Channel。
 * Selector 是实现非阻塞 I/O 的关键。
 *
 * <h2>学习要点</h2>
 * <ul>
 *   <li>Selector 可以监控多个 Channel 的 I/O 事件</li>
 *   <li>Channel 必须是非阻塞模式才能注册到 Selector</li>
 *   <li>四种事件类型：OP_ACCEPT, OP_CONNECT, OP_READ, OP_WRITE</li>
 *   <li>select() 方法阻塞直到有事件发生</li>
 *   <li>SelectionKey 代表 Channel 在 Selector 上的注册</li>
 * </ul>
 *
 * <h2>Selector 工作流程</h2>
 * <pre>
 *     +-----------+
 *     |  Selector |
 *     +-----+-----+
 *           |
 *     +-----+-----+-----+
 *     |     |     |     |
 *   +---+ +---+ +---+ +---+
 *   |Ch1| |Ch2| |Ch3| |Ch4|
 *   +---+ +---+ +---+ +---+
 *
 * 1. 多个 Channel 注册到一个 Selector
 * 2. 调用 select() 等待事件
 * 3. 遍历 selectedKeys 处理就绪的 Channel
 * 4. 处理完成后从 selectedKeys 中移除
 * </pre>
 *
 * <h2>SelectionKey 事件类型</h2>
 * <ul>
 *   <li>OP_ACCEPT (16): ServerSocketChannel 准备接受新连接</li>
 *   <li>OP_CONNECT (8): SocketChannel 连接完成</li>
 *   <li>OP_READ (1): Channel 有数据可读</li>
 *   <li>OP_WRITE (4): Channel 可以写入数据</li>
 * </ul>
 *
 * @see java.nio.channels.Selector
 * @see java.nio.channels.SelectionKey
 */
public class NioSelectorDemo {

    /**
     * 演示 Selector 的基本用法
     */
    public static void demonstrateSelector() throws IOException {
        System.out.println("=== Selector 基本用法演示 ===\n");

        // 1. 创建 Selector
        Selector selector = Selector.open();
        System.out.println("创建 Selector: " + selector);

        // 2. 创建 ServerSocketChannel
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false); // 必须设置为非阻塞
        serverChannel.bind(new InetSocketAddress(0)); // 绑定随机端口
        int port = serverChannel.socket().getLocalPort();
        System.out.println("ServerSocketChannel 绑定端口: " + port);

        // 3. 注册 Channel 到 Selector
        SelectionKey key = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("注册 ServerSocketChannel，关注 OP_ACCEPT 事件");
        System.out.println("SelectionKey: " + key);
        System.out.println("  interestOps: " + key.interestOps() + " (OP_ACCEPT=" + SelectionKey.OP_ACCEPT + ")");
        System.out.println("  isValid: " + key.isValid());

        // 4. 演示 SelectionKey 操作
        System.out.println("\n--- SelectionKey 事件类型 ---");
        System.out.println("OP_ACCEPT  = " + SelectionKey.OP_ACCEPT + " (0b" + Integer.toBinaryString(SelectionKey.OP_ACCEPT) + ")");
        System.out.println("OP_CONNECT = " + SelectionKey.OP_CONNECT + " (0b" + Integer.toBinaryString(SelectionKey.OP_CONNECT) + ")");
        System.out.println("OP_READ    = " + SelectionKey.OP_READ + " (0b" + Integer.toBinaryString(SelectionKey.OP_READ) + ")");
        System.out.println("OP_WRITE   = " + SelectionKey.OP_WRITE + " (0b" + Integer.toBinaryString(SelectionKey.OP_WRITE) + ")");

        // 5. 清理
        serverChannel.close();
        selector.close();
        System.out.println("\n资源已关闭");
    }

    /**
     * 演示多 Channel 注册到同一个 Selector
     */
    public static void demonstrateMultipleChannels() throws IOException {
        System.out.println("\n=== 多 Channel 注册演示 ===\n");

        Selector selector = Selector.open();

        // 创建多个 ServerSocketChannel
        ServerSocketChannel[] channels = new ServerSocketChannel[3];
        for (int i = 0; i < channels.length; i++) {
            channels[i] = ServerSocketChannel.open();
            channels[i].configureBlocking(false);
            channels[i].bind(new InetSocketAddress(0));
            
            // 注册到 Selector，并附加数据
            SelectionKey key = channels[i].register(selector, SelectionKey.OP_ACCEPT);
            key.attach("Server-" + i); // 附加自定义数据
            
            System.out.println("注册 " + key.attachment() + "，端口: " + channels[i].socket().getLocalPort());
        }

        System.out.println("\n已注册 Channel 数量: " + selector.keys().size());

        // 清理
        for (ServerSocketChannel channel : channels) {
            channel.close();
        }
        selector.close();
    }

    /**
     * 演示完整的 Selector 事件循环（简化版）
     */
    public static void demonstrateEventLoop() throws IOException {
        System.out.println("\n=== Selector 事件循环演示 ===\n");

        Selector selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(0));
        int port = serverChannel.socket().getLocalPort();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("服务端启动，端口: " + port);
        System.out.println("等待连接...");

        // 模拟客户端连接
        Thread clientThread = new Thread(() -> {
            try {
                Thread.sleep(100);
                SocketChannel clientChannel = SocketChannel.open();
                clientChannel.connect(new InetSocketAddress("localhost", port));
                
                // 发送数据
                ByteBuffer buffer = ByteBuffer.wrap("Hello, Selector!".getBytes(StandardCharsets.UTF_8));
                clientChannel.write(buffer);
                
                Thread.sleep(100);
                clientChannel.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        clientThread.start();

        // 事件循环
        int eventCount = 0;
        long startTime = System.currentTimeMillis();
        
        while (eventCount < 2 && (System.currentTimeMillis() - startTime) < 2000) {
            // select() 阻塞直到有事件或超时
            int readyChannels = selector.select(500);
            
            if (readyChannels == 0) {
                continue;
            }

            System.out.println("\n就绪 Channel 数量: " + readyChannels);

            // 获取就绪的 SelectionKey 集合
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                eventCount++;

                if (key.isAcceptable()) {
                    // 处理 ACCEPT 事件
                    System.out.println("事件: OP_ACCEPT");
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel clientChannel = server.accept();
                    clientChannel.configureBlocking(false);
                    
                    // 注册客户端 Channel，关注 READ 事件
                    clientChannel.register(selector, SelectionKey.OP_READ);
                    System.out.println("接受连接: " + clientChannel.getRemoteAddress());
                }

                if (key.isReadable()) {
                    // 处理 READ 事件
                    System.out.println("事件: OP_READ");
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(256);
                    int bytesRead = clientChannel.read(buffer);
                    
                    if (bytesRead > 0) {
                        buffer.flip();
                        byte[] data = new byte[buffer.remaining()];
                        buffer.get(data);
                        System.out.println("读取数据: " + new String(data, StandardCharsets.UTF_8));
                    } else if (bytesRead == -1) {
                        System.out.println("客户端关闭连接");
                        clientChannel.close();
                    }
                }

                // 必须手动移除已处理的 key
                keyIterator.remove();
            }
        }

        // 清理
        serverChannel.close();
        selector.close();
        System.out.println("\n演示完成");
    }

    /**
     * 主方法 - 运行所有演示
     */
    public static void main(String[] args) throws IOException {
        demonstrateSelector();
        demonstrateMultipleChannels();
        demonstrateEventLoop();
    }
}
