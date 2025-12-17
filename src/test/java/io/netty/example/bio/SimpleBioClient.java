package io.netty.example.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * 最简单的 BIO (Blocking I/O) 客户端
 *
 * <p>本类演示了传统的阻塞式 I/O 客户端实现。连接服务端后，可以发送消息并接收响应。
 *
 * <h2>学习要点</h2>
 * <ul>
 *   <li>Socket 用于连接服务端</li>
 *   <li>使用 InputStream/OutputStream 进行数据读写</li>
 *   <li>阻塞 I/O 的特点：connect() 和 read() 会阻塞线程</li>
 *   <li>资源管理：使用 try-with-resources 确保 Socket 正确关闭</li>
 * </ul>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * SimpleBioClient client = new SimpleBioClient("localhost", 8080);
 * String response = client.sendAndReceive("hello");
 * System.out.println(response); // "hello, mini-netty"
 * client.close();
 * }</pre>
 *
 * @see java.net.Socket
 * @see SimpleBioServer
 */
public class SimpleBioClient implements AutoCloseable {

    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    /**
     * 创建一个 BIO 客户端
     *
     * @param host 服务端主机地址
     * @param port 服务端端口号
     */
    public SimpleBioClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 连接到服务端
     *
     * @throws IOException 如果连接失败
     */
    public void connect() throws IOException {
        socket = new Socket(host, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);
        System.out.println("[SimpleBioClient] 已连接到 " + host + ":" + port);
    }

    /**
     * 发送消息并接收响应
     *
     * <p>此方法会阻塞，直到收到服务端响应。
     *
     * @param message 要发送的消息
     * @return 服务端响应
     * @throws IOException 如果发送或接收失败
     */
    public String sendAndReceive(String message) throws IOException {
        ensureConnected();
        
        System.out.println("[SimpleBioClient] 发送消息: " + message);
        writer.println(message);
        
        String response = reader.readLine();
        System.out.println("[SimpleBioClient] 收到响应: " + response);
        
        return response;
    }

    /**
     * 只发送消息，不等待响应
     *
     * @param message 要发送的消息
     * @throws IOException 如果发送失败
     */
    public void send(String message) throws IOException {
        ensureConnected();
        writer.println(message);
        System.out.println("[SimpleBioClient] 发送消息: " + message);
    }

    /**
     * 接收响应
     *
     * @return 服务端响应
     * @throws IOException 如果接收失败
     */
    public String receive() throws IOException {
        ensureConnected();
        String response = reader.readLine();
        System.out.println("[SimpleBioClient] 收到响应: " + response);
        return response;
    }

    /**
     * 确保已连接到服务端
     *
     * @throws IOException 如果未连接
     */
    private void ensureConnected() throws IOException {
        if (socket == null || socket.isClosed()) {
            throw new IOException("客户端未连接");
        }
    }

    /**
     * 检查是否已连接
     *
     * @return 如果已连接返回 true
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    /**
     * 关闭客户端连接
     */
    @Override
    public void close() {
        try {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("[SimpleBioClient] 连接已关闭");
            }
        } catch (IOException e) {
            System.err.println("[SimpleBioClient] 关闭连接失败: " + e.getMessage());
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

        try (SimpleBioClient client = new SimpleBioClient(host, port);
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {
            
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
