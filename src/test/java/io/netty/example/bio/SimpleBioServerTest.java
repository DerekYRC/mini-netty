package io.netty.example.bio;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SimpleBioServer 单元测试
 *
 * <p>测试 BIO 服务端的基本功能：
 * <ul>
 *   <li>服务端启动和停止</li>
 *   <li>客户端连接和消息收发</li>
 *   <li>服务端正确响应 "hello, mini-netty"</li>
 * </ul>
 */
@DisplayName("SimpleBioServer 测试")
class SimpleBioServerTest {

    private static final int TEST_PORT = 9999;
    private SimpleBioServer server;

    @BeforeEach
    void setUp() {
        server = new SimpleBioServer(TEST_PORT);
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    @DisplayName("服务端可以正常启动和停止")
    void serverCanStartAndStop() throws InterruptedException {
        // Given: 创建服务端
        assertThat(server.isRunning()).isFalse();

        // When: 在后台启动服务端
        server.startInBackground();
        Thread.sleep(100); // 等待服务端启动

        // Then: 服务端正在运行
        assertThat(server.isRunning()).isTrue();
        assertThat(server.getPort()).isEqualTo(TEST_PORT);

        // When: 停止服务端
        server.stop();
        Thread.sleep(100); // 等待服务端停止

        // Then: 服务端已停止
        assertThat(server.isRunning()).isFalse();
    }

    @Test
    @DisplayName("客户端发送hello，服务端返回hello, mini-netty")
    void serverRespondsWithHelloMiniNetty() throws IOException, InterruptedException {
        // Given: 服务端已启动
        server.startInBackground();
        Thread.sleep(100);

        // When: 客户端连接并发送消息
        try (Socket clientSocket = new Socket("localhost", TEST_PORT);
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(clientSocket.getInputStream()))) {

            writer.println("hello");

            // Then: 收到预期响应
            String response = reader.readLine();
            assertThat(response).isEqualTo("hello, mini-netty");
        }
    }

    @Test
    @DisplayName("客户端发送任意消息，服务端都返回hello, mini-netty")
    void serverRespondsToAnyMessage() throws IOException, InterruptedException {
        // Given: 服务端已启动
        server.startInBackground();
        Thread.sleep(100);

        // When: 客户端发送不同的消息
        try (Socket clientSocket = new Socket("localhost", TEST_PORT);
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(clientSocket.getInputStream()))) {

            // 发送第一条消息
            writer.println("test message 1");
            String response1 = reader.readLine();
            assertThat(response1).isEqualTo("hello, mini-netty");

            // 发送第二条消息
            writer.println("another message");
            String response2 = reader.readLine();
            assertThat(response2).isEqualTo("hello, mini-netty");
        }
    }

    @Test
    @DisplayName("服务端可以处理客户端断开连接")
    void serverHandlesClientDisconnection() throws IOException, InterruptedException {
        // Given: 服务端已启动
        server.startInBackground();
        Thread.sleep(100);

        // When: 客户端连接后立即断开
        Socket clientSocket = new Socket("localhost", TEST_PORT);
        clientSocket.close();

        // Then: 服务端仍在运行
        Thread.sleep(100);
        assertThat(server.isRunning()).isTrue();
    }
}
