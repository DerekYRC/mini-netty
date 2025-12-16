package io.netty.example.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 多线程 BIO (Blocking I/O) 服务端
 *
 * <p>本类演示了使用线程池处理多个客户端连接的 BIO 服务端实现。
 * 相比 {@link SimpleBioServer}，可以同时处理多个客户端连接。
 *
 * <h2>学习要点</h2>
 * <ul>
 *   <li>使用线程池管理工作线程</li>
 *   <li>每个客户端连接由独立线程处理</li>
 *   <li>线程池可以限制并发连接数，防止资源耗尽</li>
 *   <li>BIO + 多线程模型的优缺点</li>
 * </ul>
 *
 * <h2>优点</h2>
 * <ul>
 *   <li>可以同时处理多个客户端连接</li>
 *   <li>编程模型简单直观</li>
 * </ul>
 *
 * <h2>缺点</h2>
 * <ul>
 *   <li>每个连接需要一个线程，资源消耗大</li>
 *   <li>线程切换开销大</li>
 *   <li>连接数受限于线程池大小</li>
 * </ul>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * MultiThreadBioServer server = new MultiThreadBioServer(8080, 10);
 * server.start();
 * }</pre>
 *
 * @see SimpleBioServer
 * @see java.util.concurrent.ExecutorService
 */
public class MultiThreadBioServer {

    private final int port;
    private final int threadPoolSize;
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private volatile boolean running;
    private final AtomicInteger activeConnections = new AtomicInteger(0);

    /**
     * 创建一个多线程 BIO 服务端
     *
     * @param port 监听端口号
     * @param threadPoolSize 线程池大小（最大并发连接数）
     */
    public MultiThreadBioServer(int port, int threadPoolSize) {
        this.port = port;
        this.threadPoolSize = threadPoolSize;
    }

    /**
     * 创建一个多线程 BIO 服务端（使用默认线程池大小）
     *
     * @param port 监听端口号
     */
    public MultiThreadBioServer(int port) {
        this(port, 10);
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
        executorService = Executors.newFixedThreadPool(threadPoolSize);
        running = true;
        System.out.println("[MultiThreadBioServer] 服务端启动，监听端口: " + port + 
                           "，线程池大小: " + threadPoolSize);

        while (running) {
            try {
                // accept() 阻塞，直到有客户端连接
                Socket clientSocket = serverSocket.accept();
                System.out.println("[MultiThreadBioServer] 客户端连接: " + 
                                   clientSocket.getRemoteSocketAddress());

                // 将客户端处理任务提交到线程池
                executorService.submit(() -> handleClient(clientSocket));
            } catch (IOException e) {
                if (running) {
                    System.err.println("[MultiThreadBioServer] 接受连接失败: " + e.getMessage());
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
                    System.err.println("[MultiThreadBioServer] 服务端异常: " + e.getMessage());
                }
            }
        }, "multi-thread-bio-server");
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
     * <p>此方法在独立线程中运行，读取客户端发送的消息，并返回响应。
     *
     * @param clientSocket 客户端 Socket
     */
    private void handleClient(Socket clientSocket) {
        activeConnections.incrementAndGet();
        String clientAddress = clientSocket.getRemoteSocketAddress().toString();
        
        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[MultiThreadBioServer] [" + Thread.currentThread().getName() + 
                                   "] 收到消息: " + line);

                // 发送响应
                String response = "hello, mini-netty";
                writer.println(response);
                System.out.println("[MultiThreadBioServer] [" + Thread.currentThread().getName() + 
                                   "] 发送响应: " + response);
            }
        } catch (IOException e) {
            System.err.println("[MultiThreadBioServer] 处理客户端失败: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("[MultiThreadBioServer] 客户端断开连接: " + clientAddress);
            } catch (IOException e) {
                // ignore
            }
            activeConnections.decrementAndGet();
        }
    }

    /**
     * 停止服务端
     */
    public void stop() {
        running = false;
        
        // 关闭线程池
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        // 关闭服务端 Socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                System.out.println("[MultiThreadBioServer] 服务端已停止");
            } catch (IOException e) {
                System.err.println("[MultiThreadBioServer] 关闭服务端失败: " + e.getMessage());
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
     * 获取当前活跃连接数
     *
     * @return 活跃连接数
     */
    public int getActiveConnections() {
        return activeConnections.get();
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
     * @param args 命令行参数（可选：端口号 线程池大小，默认 8080 10）
     * @throws IOException 如果启动失败
     */
    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        int threadPoolSize = args.length > 1 ? Integer.parseInt(args[1]) : 10;
        MultiThreadBioServer server = new MultiThreadBioServer(port, threadPoolSize);
        server.start();
    }
}
