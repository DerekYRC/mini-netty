package io.netty.integration;

import io.netty.example.nio.NioClient;
import io.netty.example.nio.NioServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NIO 客户端/服务端集成测试
 *
 * <p>测试 NioClient 和 NioServer 的协作：
 * <ul>
 *   <li>客户端连接到服务端</li>
 *   <li>消息发送和接收</li>
 *   <li>多客户端并发访问</li>
 * </ul>
 */
@DisplayName("NIO 客户端/服务端集成测试")
class NioClientServerTest {

    private static final int TEST_PORT = 9995;
    private NioServer server;

    @BeforeEach
    void setUp() throws InterruptedException {
        server = new NioServer(TEST_PORT);
        server.startInBackground();
        Thread.sleep(100); // 等待服务端启动
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    @DisplayName("NIO客户端可以连接到NIO服务端")
    void clientCanConnectToServer() throws IOException {
        // Given: 服务端已启动
        assertThat(server.isRunning()).isTrue();

        // When: 客户端连接
        try (NioClient client = new NioClient("localhost", TEST_PORT)) {
            client.connect();

            // Then: 客户端已连接
            assertThat(client.isConnected()).isTrue();
        }
    }

    @Test
    @DisplayName("客户端发送hello，收到hello, mini-netty响应")
    void clientReceivesCorrectResponse() throws IOException {
        // Given: 客户端已连接
        try (NioClient client = new NioClient("localhost", TEST_PORT)) {
            client.connect();

            // When: 发送消息
            String response = client.sendAndReceive("hello");

            // Then: 收到预期响应
            assertThat(response).isEqualTo("hello, mini-netty");
        }
    }

    @Test
    @DisplayName("客户端可以发送多条消息")
    void clientCanSendMultipleMessages() throws IOException {
        // Given: 客户端已连接
        try (NioClient client = new NioClient("localhost", TEST_PORT)) {
            client.connect();

            // When: 发送多条消息
            String response1 = client.sendAndReceive("message 1");
            String response2 = client.sendAndReceive("message 2");
            String response3 = client.sendAndReceive("message 3");

            // Then: 每条消息都收到正确响应
            assertThat(response1).isEqualTo("hello, mini-netty");
            assertThat(response2).isEqualTo("hello, mini-netty");
            assertThat(response3).isEqualTo("hello, mini-netty");
        }
    }

    @Test
    @DisplayName("NIO服务端可以同时处理多个客户端")
    void serverHandlesMultipleClients() throws Exception {
        // Given: 服务端已启动
        assertThat(server.isRunning()).isTrue();

        int clientCount = 5;
        CountDownLatch latch = new CountDownLatch(clientCount);
        AtomicInteger successCount = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(clientCount);

        // When: 多个客户端同时连接并发送消息
        for (int i = 0; i < clientCount; i++) {
            executor.submit(() -> {
                try (NioClient client = new NioClient("localhost", TEST_PORT)) {
                    client.connect();
                    String response = client.sendAndReceive("test");
                    if ("hello, mini-netty".equals(response)) {
                        successCount.incrementAndGet();
                    }
                } catch (IOException e) {
                    System.err.println("Client failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有客户端完成
        boolean completed = latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Then: 所有客户端都成功
        assertThat(completed).isTrue();
        assertThat(successCount.get()).isEqualTo(clientCount);
    }

    @Test
    @DisplayName("验收场景：服务端已启动，客户端连接发送hello，收到hello, mini-netty")
    void acceptanceScenario1() throws IOException {
        // 验收场景1: 服务端已启动并监听指定端口
        assertThat(server.isRunning()).isTrue();
        assertThat(server.getPort()).isEqualTo(TEST_PORT);

        // When: 客户端连接并发送"hello"
        try (NioClient client = new NioClient("localhost", TEST_PORT)) {
            client.connect();
            String response = client.sendAndReceive("hello");

            // Then: 服务端接收到消息并返回"hello, mini-netty"
            assertThat(response).isEqualTo("hello, mini-netty");
        }
    }

    @Test
    @DisplayName("验收场景：多个客户端同时连接，每个都能正常收发消息")
    void acceptanceScenario2() throws Exception {
        // 验收场景2: 多个客户端同时连接
        List<NioClient> clients = new ArrayList<>();
        List<String> responses = new ArrayList<>();

        try {
            // 创建并连接多个客户端
            for (int i = 0; i < 3; i++) {
                NioClient client = new NioClient("localhost", TEST_PORT);
                client.connect();
                clients.add(client);
            }

            // 每个客户端发送消息
            for (int i = 0; i < clients.size(); i++) {
                String response = clients.get(i).sendAndReceive("client " + i);
                responses.add(response);
            }

            // Then: 每个客户端都能正常收发消息
            assertThat(responses).hasSize(3);
            assertThat(responses).allMatch(r -> "hello, mini-netty".equals(r));
        } finally {
            for (NioClient client : clients) {
                client.close();
            }
        }
    }

    @Test
    @DisplayName("验收场景：EventLoop运行时，客户端发送数据能触发READ事件")
    void acceptanceScenarioReadEvent() throws Exception {
        // 验收场景2: EventLoop正在运行，客户端发送数据
        assertThat(server.isRunning()).isTrue();

        try (NioClient client = new NioClient("localhost", TEST_PORT)) {
            client.connect();
            
            // When: 客户端发送数据
            String response = client.sendAndReceive("trigger read event");
            
            // Then: EventLoop触发READ事件并处理（通过收到响应验证）
            assertThat(response).isEqualTo("hello, mini-netty");
        }
    }
}
