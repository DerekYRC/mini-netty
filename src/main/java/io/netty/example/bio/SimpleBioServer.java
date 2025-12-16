package io.netty.example.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 最简单的 BIO (Blocking I/O) 服务端
 *
 * <p>本类演示了传统的阻塞式 I/O 服务端实现。每次只能处理一个客户端连接，
 * 因为 accept() 和 read() 都是阻塞操作。
 *
 * <h2>学习要点</h2>
 * <ul>
 *   <li>ServerSocket 用于监听端口，接受客户端连接</li>
 *   <li>Socket 代表一个客户端连接</li>
 *   <li>使用 InputStream/OutputStream 进行数据读写</li>
 *   <li>阻塞 I/O 的特点：accept() 和 read() 会阻塞线程</li>
 * </ul>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * SimpleBioServer server = new SimpleBioServer(8080);
 * server.start();
 * }</pre>
 *
 * @see java.net.ServerSocket
 * @see java.net.Socket
 */
public class SimpleBioServer {

    private final int port;
    private ServerSocket serverSocket;
    private volatile boolean running;

    /**
     * 创建一个 BIO 服务端
     *
     * @param port 监听端口号
     */
    public SimpleBioServer(int port) {
        this.port = port;
    }

    /**
     * 启动服务端并开始处理连接
     *
     * <p>此方法会阻塞当前线程，直到服务端停止。
     *
     * @throws IOException 如果无法绑定端口或发生 I/O 错误
     */
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        System.out.println("[SimpleBioServer] 服务端启动，监听端口: " + port);

        while (running) {
            try {
                // accept() 阻塞，直到有客户端连接
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SimpleBioServer] 客户端连接: " + clientSocket.getRemoteSocketAddress());

                // 处理客户端请求（阻塞当前线程）
                handleClient(clientSocket);
            } catch (IOException e) {
                if (running) {
                    System.err.println("[SimpleBioServer] 接受连接失败: " + e.getMessage());
                }
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
                    System.err.println("[SimpleBioServer] 服务端异常: " + e.getMessage());
                }
            }
        }, "bio-server");
        serverThread.setDaemon(true);
        serverThread.start();

        // 等待服务端启动
        while (serverSocket == null && running) {
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
     * 处理单个客户端连接
     *
     * <p>读取客户端发送的消息，并返回 "hello, mini-netty" 响应。
     *
     * @param clientSocket 客户端 Socket
     */
    private void handleClient(Socket clientSocket) {
        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String line;
            // read() 阻塞，直到有数据可读或连接关闭
            while ((line = reader.readLine()) != null) {
                System.out.println("[SimpleBioServer] 收到消息: " + line);

                // 发送响应
                String response = "hello, mini-netty";
                writer.println(response);
                System.out.println("[SimpleBioServer] 发送响应: " + response);
            }
        } catch (IOException e) {
            System.err.println("[SimpleBioServer] 处理客户端失败: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("[SimpleBioServer] 客户端断开连接");
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * 停止服务端
     */
    public void stop() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                System.out.println("[SimpleBioServer] 服务端已停止");
            } catch (IOException e) {
                System.err.println("[SimpleBioServer] 关闭服务端失败: " + e.getMessage());
            }
        }
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
     * 检查服务端是否正在运行
     *
     * @return 如果服务端正在运行返回 true
     */
    public boolean isRunning() {
        return running && serverSocket != null && !serverSocket.isClosed();
    }

    /**
     * 主方法 - 启动服务端
     *
     * @param args 命令行参数（可选：端口号，默认 8080）
     * @throws IOException 如果启动失败
     */
    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        SimpleBioServer server = new SimpleBioServer(port);
        server.start();
    }
}
