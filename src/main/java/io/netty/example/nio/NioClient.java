package io.netty.example.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * NIO 客户端
 *
 * <p>本类演示了使用 NIO SocketChannel 实现客户端。
 * 与 BIO 客户端相比，NIO 客户端使用 Channel 和 Buffer 进行数据传输。
 *
 * <h2>学习要点</h2>
 * <ul>
 *   <li>SocketChannel 用于建立 TCP 连接</li>
 *   <li>可配置为阻塞或非阻塞模式</li>
 *   <li>使用 ByteBuffer 读写数据</li>
 *   <li>connect() 在非阻塞模式下可能不会立即完成</li>
 * </ul>
 *
 * @see java.nio.channels.SocketChannel
 * @see NioServer
 */
public class NioClient implements AutoCloseable {

    private final String host;
    private final int port;
    private SocketChannel channel;

    /**
     * 创建一个 NIO 客户端
     *
     * @param host 服务端主机地址
     * @param port 服务端端口号
     */
    public NioClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 连接到服务端（阻塞模式）
     *
     * @throws IOException 如果连接失败
     */
    public void connect() throws IOException {
        channel = SocketChannel.open();
        channel.configureBlocking(true); // 使用阻塞模式简化客户端
        channel.connect(new InetSocketAddress(host, port));
        System.out.println("[NioClient] 已连接到 " + host + ":" + port);
    }

    /**
     * 发送消息并接收响应
     *
     * @param message 要发送的消息
     * @return 服务端响应
     * @throws IOException 如果发送或接收失败
     */
    public String sendAndReceive(String message) throws IOException {
        ensureConnected();
        
        // 发送消息
        ByteBuffer writeBuffer = ByteBuffer.wrap((message + "\n").getBytes(StandardCharsets.UTF_8));
        channel.write(writeBuffer);
        System.out.println("[NioClient] 发送消息: " + message);
        
        // 接收响应
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        int bytesRead = channel.read(readBuffer);
        
        if (bytesRead > 0) {
            readBuffer.flip();
            byte[] data = new byte[readBuffer.remaining()];
            readBuffer.get(data);
            String response = new String(data, StandardCharsets.UTF_8).trim();
            System.out.println("[NioClient] 收到响应: " + response);
            return response;
        }
        
        return null;
    }

    /**
     * 只发送消息，不等待响应
     *
     * @param message 要发送的消息
     * @throws IOException 如果发送失败
     */
    public void send(String message) throws IOException {
        ensureConnected();
        ByteBuffer buffer = ByteBuffer.wrap((message + "\n").getBytes(StandardCharsets.UTF_8));
        channel.write(buffer);
        System.out.println("[NioClient] 发送消息: " + message);
    }

    /**
     * 接收响应
     *
     * @return 服务端响应
     * @throws IOException 如果接收失败
     */
    public String receive() throws IOException {
        ensureConnected();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = channel.read(buffer);
        
        if (bytesRead > 0) {
            buffer.flip();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            String response = new String(data, StandardCharsets.UTF_8).trim();
            System.out.println("[NioClient] 收到响应: " + response);
            return response;
        }
        
        return null;
    }

    /**
     * 确保已连接到服务端
     *
     * @throws IOException 如果未连接
     */
    private void ensureConnected() throws IOException {
        if (channel == null || !channel.isConnected()) {
            throw new IOException("客户端未连接");
        }
    }

    /**
     * 检查是否已连接
     *
     * @return 如果已连接返回 true
     */
    public boolean isConnected() {
        return channel != null && channel.isConnected();
    }

    /**
     * 关闭客户端连接
     */
    @Override
    public void close() {
        if (channel != null && channel.isOpen()) {
            try {
                channel.close();
                System.out.println("[NioClient] 连接已关闭");
            } catch (IOException e) {
                System.err.println("[NioClient] 关闭连接失败: " + e.getMessage());
            }
        }
    }

    /**
     * 主方法 - 启动客户端进行交互
     *
     * @param args 命令行参数（可选：host port，默认 localhost 8080）
     * @throws IOException 如果连接或通信失败
     */
    public static void main(String[] args) throws IOException {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 8080;

        try (NioClient client = new NioClient(host, port);
             java.io.BufferedReader console = new java.io.BufferedReader(
                     new java.io.InputStreamReader(System.in))) {
            
            client.connect();
            System.out.println("输入消息发送到服务端（输入 'quit' 退出）：");

            String input;
            while ((input = console.readLine()) != null) {
                if ("quit".equalsIgnoreCase(input)) {
                    break;
                }
                String response = client.sendAndReceive(input);
                System.out.println("服务端响应: " + response);
            }
        }
    }
}
