package io.netty.example.nio;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NIO Server ACCEPT 事件测试
 *
 * <p>测试 NioServer 处理客户端连接的能力：
 * <ul>
 *   <li>服务端启动和停止</li>
 *   <li>接受客户端连接</li>
 *   <li>客户端 Channel 注册到 Selector</li>
 * </ul>
 */
@DisplayName("NIO Server ACCEPT 测试")
class NioServerAcceptTest {

    private static final int TEST_PORT = 9996;
    private NioServer server;

    @BeforeEach
    void setUp() {
        server = new NioServer(TEST_PORT);
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
        Thread.sleep(100);

        // Then: 服务端正在运行
        assertThat(server.isRunning()).isTrue();
        assertThat(server.getPort()).isEqualTo(TEST_PORT);
        assertThat(server.getSelector()).isNotNull();
        assertThat(server.getSelector().isOpen()).isTrue();

        // When: 停止服务端
        server.stop();
        Thread.sleep(100);

        // Then: 服务端已停止
        assertThat(server.isRunning()).isFalse();
    }

    @Test
    @DisplayName("服务端可以接受客户端连接")
    void serverCanAcceptClientConnection() throws Exception {
        // Given: 服务端已启动
        server.startInBackground();
        Thread.sleep(100);
        assertThat(server.isRunning()).isTrue();

        // When: 客户端连接
        SocketChannel clientChannel = SocketChannel.open();
        clientChannel.connect(new InetSocketAddress("localhost", TEST_PORT));

        // Then: 连接成功
        assertThat(clientChannel.isConnected()).isTrue();

        // 等待服务端处理连接
        Thread.sleep(100);

        // Selector 应该有2个 key：ServerSocketChannel 和 SocketChannel
        assertThat(server.getSelector().keys().size()).isEqualTo(2);

        clientChannel.close();
    }

    @Test
    @DisplayName("服务端可以接受多个客户端连接")
    void serverCanAcceptMultipleConnections() throws Exception {
        // Given: 服务端已启动
        server.startInBackground();
        Thread.sleep(100);

        // When: 多个客户端连接
        SocketChannel client1 = SocketChannel.open();
        client1.connect(new InetSocketAddress("localhost", TEST_PORT));

        SocketChannel client2 = SocketChannel.open();
        client2.connect(new InetSocketAddress("localhost", TEST_PORT));

        SocketChannel client3 = SocketChannel.open();
        client3.connect(new InetSocketAddress("localhost", TEST_PORT));

        // 等待服务端处理连接
        Thread.sleep(200);

        // Then: 所有连接都被接受
        assertThat(client1.isConnected()).isTrue();
        assertThat(client2.isConnected()).isTrue();
        assertThat(client3.isConnected()).isTrue();

        // Selector 应该有4个 key：1个 ServerSocketChannel + 3个 SocketChannel
        assertThat(server.getSelector().keys().size()).isEqualTo(4);

        client1.close();
        client2.close();
        client3.close();
    }

    @Test
    @DisplayName("客户端 Channel 注册了 READ 事件")
    void clientChannelRegisteredWithReadEvent() throws Exception {
        // Given: 服务端已启动
        server.startInBackground();
        Thread.sleep(100);

        // When: 客户端连接
        SocketChannel clientChannel = SocketChannel.open();
        clientChannel.connect(new InetSocketAddress("localhost", TEST_PORT));

        // 等待服务端处理连接
        Thread.sleep(100);

        // Then: 客户端 Channel 注册了 READ 事件
        boolean hasReadInterest = false;
        for (SelectionKey key : server.getSelector().keys()) {
            if (key.channel() instanceof SocketChannel) {
                if ((key.interestOps() & SelectionKey.OP_READ) != 0) {
                    hasReadInterest = true;
                    break;
                }
            }
        }
        assertThat(hasReadInterest).isTrue();

        clientChannel.close();
    }

    @Test
    @DisplayName("验收场景：EventLoop运行时，注册ServerSocketChannel能接收到ACCEPT事件")
    void acceptanceScenario() throws Exception {
        // 验收场景1: EventLoop已创建并运行
        server.startInBackground();
        Thread.sleep(100);
        assertThat(server.isRunning()).isTrue();

        // 注册一个ServerSocketChannel
        // （已在 start() 中完成）

        // When: 客户端连接触发 ACCEPT 事件
        SocketChannel clientChannel = SocketChannel.open();
        clientChannel.connect(new InetSocketAddress("localhost", TEST_PORT));

        // Then: 能够接收到ACCEPT事件（通过检查客户端已注册到 Selector）
        Thread.sleep(100);
        
        // 验证：客户端 Channel 已被服务端接受并注册
        int socketChannelCount = 0;
        for (SelectionKey key : server.getSelector().keys()) {
            if (key.channel() instanceof SocketChannel) {
                socketChannelCount++;
            }
        }
        assertThat(socketChannelCount).isEqualTo(1);

        clientChannel.close();
    }
}
