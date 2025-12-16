package io.netty.example.nio;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NIO Selector 测试
 *
 * <p>测试 Selector 的核心功能：Channel 注册、事件监听、多路复用。
 */
@DisplayName("NIO Selector 测试")
class NioSelectorTest {

    private Selector selector;
    private ServerSocketChannel serverChannel;

    @BeforeEach
    void setUp() throws IOException {
        selector = Selector.open();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (serverChannel != null && serverChannel.isOpen()) {
            serverChannel.close();
        }
        if (selector != null && selector.isOpen()) {
            selector.close();
        }
    }

    @Test
    @DisplayName("可以创建和关闭 Selector")
    void canCreateAndCloseSelector() throws IOException {
        // When: 创建 Selector
        Selector newSelector = Selector.open();

        // Then: Selector 是打开的
        assertThat(newSelector.isOpen()).isTrue();

        // When: 关闭 Selector
        newSelector.close();

        // Then: Selector 已关闭
        assertThat(newSelector.isOpen()).isFalse();
    }

    @Test
    @DisplayName("Channel 必须是非阻塞模式才能注册到 Selector")
    void channelMustBeNonBlockingForRegistration() throws IOException {
        // Given: 创建非阻塞 ServerSocketChannel
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(0));

        // When: 注册到 Selector
        SelectionKey key = serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        // Then: 注册成功
        assertThat(key).isNotNull();
        assertThat(key.isValid()).isTrue();
        assertThat(key.channel()).isEqualTo(serverChannel);
    }

    @Test
    @DisplayName("SelectionKey 包含正确的事件类型")
    void selectionKeyContainsCorrectInterestOps() throws IOException {
        // Given: 创建并注册 Channel
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(0));

        // When: 注册 OP_ACCEPT 事件
        SelectionKey key = serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        // Then: interestOps 是 OP_ACCEPT
        assertThat(key.interestOps()).isEqualTo(SelectionKey.OP_ACCEPT);
        assertThat(key.isAcceptable()).isFalse(); // 还没有事件发生
    }

    @Test
    @DisplayName("可以在 SelectionKey 上附加数据")
    void canAttachDataToSelectionKey() throws IOException {
        // Given: 创建并注册 Channel
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(0));
        SelectionKey key = serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        // When: 附加数据
        String attachment = "my-server";
        key.attach(attachment);

        // Then: 可以获取附加的数据
        assertThat(key.attachment()).isEqualTo(attachment);
    }

    @Test
    @DisplayName("多个 Channel 可以注册到同一个 Selector")
    void multipleChannelsCanRegisterToSameSelector() throws IOException {
        // Given: 创建多个 Channel
        ServerSocketChannel channel1 = ServerSocketChannel.open();
        channel1.configureBlocking(false);
        channel1.bind(new InetSocketAddress(0));

        ServerSocketChannel channel2 = ServerSocketChannel.open();
        channel2.configureBlocking(false);
        channel2.bind(new InetSocketAddress(0));

        // When: 注册到同一个 Selector
        channel1.register(selector, SelectionKey.OP_ACCEPT);
        channel2.register(selector, SelectionKey.OP_ACCEPT);

        // Then: Selector 包含两个 key
        assertThat(selector.keys()).hasSize(2);

        // 清理
        channel1.close();
        channel2.close();
    }

    @Test
    @DisplayName("select() 返回就绪的 Channel 数量")
    void selectReturnsReadyChannelCount() throws Exception {
        // Given: 创建服务端
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(0));
        int port = serverChannel.socket().getLocalPort();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        // When: 在后台创建客户端连接
        Thread clientThread = new Thread(() -> {
            try {
                Thread.sleep(50);
                SocketChannel client = SocketChannel.open();
                client.connect(new InetSocketAddress("localhost", port));
                Thread.sleep(100);
                client.close();
            } catch (Exception e) {
                // ignore
            }
        });
        clientThread.start();

        // Then: select() 返回就绪的 Channel 数量
        int readyCount = selector.select(1000);
        assertThat(readyCount).isGreaterThan(0);

        clientThread.join(2000);
    }

    @Test
    @DisplayName("selectNow() 不阻塞立即返回")
    void selectNowReturnsImmediately() throws IOException {
        // Given: 创建服务端但没有客户端连接
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(0));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        // When: 调用 selectNow()
        long startTime = System.currentTimeMillis();
        int readyCount = selector.selectNow();
        long elapsed = System.currentTimeMillis() - startTime;

        // Then: 立即返回，没有就绪的 Channel
        assertThat(elapsed).isLessThan(100);
        assertThat(readyCount).isEqualTo(0);
    }

    @Test
    @DisplayName("处理 ACCEPT 事件后可以注册 READ 事件")
    void canRegisterReadEventAfterAccept() throws Exception {
        // Given: 创建服务端
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(0));
        int port = serverChannel.socket().getLocalPort();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        // 在后台创建客户端连接并发送数据
        Thread clientThread = new Thread(() -> {
            try {
                Thread.sleep(50);
                SocketChannel client = SocketChannel.open();
                client.connect(new InetSocketAddress("localhost", port));
                ByteBuffer buffer = ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8));
                client.write(buffer);
                Thread.sleep(200);
                client.close();
            } catch (Exception e) {
                // ignore
            }
        });
        clientThread.start();

        // When: 处理 ACCEPT 事件
        int readyCount = selector.select(1000);
        assertThat(readyCount).isGreaterThan(0);

        Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
        SocketChannel clientChannel = null;
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            if (key.isAcceptable()) {
                ServerSocketChannel server = (ServerSocketChannel) key.channel();
                clientChannel = server.accept();
                clientChannel.configureBlocking(false);
                clientChannel.register(selector, SelectionKey.OP_READ);
            }
            keyIterator.remove();
        }

        // Then: 客户端 Channel 已注册 READ 事件
        assertThat(clientChannel).isNotNull();
        assertThat(selector.keys()).hasSize(2);

        // 等待 READ 事件
        readyCount = selector.select(1000);
        if (readyCount > 0) {
            for (SelectionKey key : selector.selectedKeys()) {
                if (key.isReadable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(256);
                    int bytesRead = channel.read(buffer);
                    assertThat(bytesRead).isGreaterThan(0);
                }
            }
        }

        clientThread.join(2000);
        if (clientChannel != null) {
            clientChannel.close();
        }
    }

    @Test
    @DisplayName("wakeup() 可以唤醒阻塞的 select()")
    void wakeupCanUnblockSelect() throws Exception {
        // Given: 创建服务端
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(0));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        // When: 在另一个线程中调用 wakeup()
        Thread wakeupThread = new Thread(() -> {
            try {
                Thread.sleep(100);
                selector.wakeup();
            } catch (Exception e) {
                // ignore
            }
        });
        wakeupThread.start();

        // Then: select() 被唤醒，即使没有事件
        long startTime = System.currentTimeMillis();
        int readyCount = selector.select(5000);
        long elapsed = System.currentTimeMillis() - startTime;

        assertThat(elapsed).isLessThan(1000);
        assertThat(readyCount).isEqualTo(0);

        wakeupThread.join(2000);
    }
}
