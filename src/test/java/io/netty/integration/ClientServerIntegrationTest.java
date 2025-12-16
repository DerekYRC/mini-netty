package io.netty.integration;

import io.netty.example.bio.SimpleBioClient;
import io.netty.example.bio.SimpleBioServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BIO 客户端/服务端集成测试
 *
 * <p>测试 SimpleBioClient 和 SimpleBioServer 的协作：
 * <ul>
 *   <li>客户端连接到服务端</li>
 *   <li>消息发送和接收</li>
 *   <li>多次消息交互</li>
 * </ul>
 */
@DisplayName("BIO 客户端/服务端集成测试")
class ClientServerIntegrationTest {

    private static final int TEST_PORT = 9998;
    private SimpleBioServer server;
    private SimpleBioClient client;

    @BeforeEach
    void setUp() throws InterruptedException {
        server = new SimpleBioServer(TEST_PORT);
        server.startInBackground();
        Thread.sleep(100); // 等待服务端启动
    }

    @AfterEach
    void tearDown() {
        if (client != null) {
            client.close();
        }
        if (server != null) {
            server.stop();
        }
    }

    @Test
    @DisplayName("客户端可以连接到服务端")
    void clientCanConnectToServer() throws IOException {
        // Given: 服务端已启动
        assertThat(server.isRunning()).isTrue();

        // When: 客户端连接
        client = new SimpleBioClient("localhost", TEST_PORT);
        client.connect();

        // Then: 客户端已连接
        assertThat(client.isConnected()).isTrue();
    }

    @Test
    @DisplayName("客户端发送hello，收到hello, mini-netty响应")
    void clientReceivesCorrectResponse() throws IOException {
        // Given: 客户端已连接
        client = new SimpleBioClient("localhost", TEST_PORT);
        client.connect();

        // When: 发送消息
        String response = client.sendAndReceive("hello");

        // Then: 收到预期响应
        assertThat(response).isEqualTo("hello, mini-netty");
    }

    @Test
    @DisplayName("客户端可以发送多条消息")
    void clientCanSendMultipleMessages() throws IOException {
        // Given: 客户端已连接
        client = new SimpleBioClient("localhost", TEST_PORT);
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

    @Test
    @DisplayName("客户端关闭后无法发送消息")
    void clientCannotSendAfterClose() throws IOException {
        // Given: 客户端已连接
        client = new SimpleBioClient("localhost", TEST_PORT);
        client.connect();
        assertThat(client.isConnected()).isTrue();

        // When: 关闭客户端
        client.close();

        // Then: 客户端已断开
        assertThat(client.isConnected()).isFalse();
    }

    @Test
    @DisplayName("完整的客户端-服务端通信流程")
    void fullCommunicationFlow() throws IOException {
        // 验收场景1: 服务端已启动并监听指定端口
        assertThat(server.isRunning()).isTrue();
        assertThat(server.getPort()).isEqualTo(TEST_PORT);

        // 客户端连接并发送"hello"
        client = new SimpleBioClient("localhost", TEST_PORT);
        client.connect();
        String response = client.sendAndReceive("hello");

        // 服务端接收到消息并返回"hello, mini-netty"
        assertThat(response).isEqualTo("hello, mini-netty");
    }
}
