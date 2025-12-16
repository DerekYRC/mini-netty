package io.netty.example.nio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NIO Channel 和 Buffer 测试
 *
 * <p>测试 ByteBuffer 的基本操作和 FileChannel 的读写功能。
 */
@DisplayName("NIO Channel 和 Buffer 测试")
class NioChannelBufferTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("ByteBuffer 可以正确分配指定容量")
    void byteBufferAllocation() {
        // When: 分配指定容量的 Buffer
        ByteBuffer buffer = ByteBuffer.allocate(10);

        // Then: 容量正确，position 为 0，limit 等于 capacity
        assertThat(buffer.capacity()).isEqualTo(10);
        assertThat(buffer.position()).isEqualTo(0);
        assertThat(buffer.limit()).isEqualTo(10);
    }

    @Test
    @DisplayName("写入数据后 position 增加")
    void positionIncreasesAfterWrite() {
        // Given: 创建 Buffer
        ByteBuffer buffer = ByteBuffer.allocate(10);

        // When: 写入数据
        buffer.put((byte) 'A');
        buffer.put((byte) 'B');
        buffer.put((byte) 'C');

        // Then: position 增加到 3
        assertThat(buffer.position()).isEqualTo(3);
        assertThat(buffer.remaining()).isEqualTo(7);
    }

    @Test
    @DisplayName("flip() 切换到读模式")
    void flipSwitchesToReadMode() {
        // Given: 写入数据的 Buffer
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put("Hello".getBytes(StandardCharsets.UTF_8));
        assertThat(buffer.position()).isEqualTo(5);
        assertThat(buffer.limit()).isEqualTo(10);

        // When: 调用 flip()
        buffer.flip();

        // Then: position 归零，limit 变为之前的 position
        assertThat(buffer.position()).isEqualTo(0);
        assertThat(buffer.limit()).isEqualTo(5);
        assertThat(buffer.remaining()).isEqualTo(5);
    }

    @Test
    @DisplayName("可以从 Buffer 读取写入的数据")
    void canReadWrittenData() {
        // Given: 写入数据的 Buffer
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put("Hello".getBytes(StandardCharsets.UTF_8));
        buffer.flip();

        // When: 读取数据
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        // Then: 读取到正确的数据
        assertThat(new String(data, StandardCharsets.UTF_8)).isEqualTo("Hello");
    }

    @Test
    @DisplayName("clear() 重置 Buffer 到写模式")
    void clearResetsBufferToWriteMode() {
        // Given: 已读取的 Buffer
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put("Hello".getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        buffer.get(); // 读取一个字节

        // When: 调用 clear()
        buffer.clear();

        // Then: position 归零，limit 恢复为 capacity
        assertThat(buffer.position()).isEqualTo(0);
        assertThat(buffer.limit()).isEqualTo(10);
    }

    @Test
    @DisplayName("compact() 保留未读数据并切换到写模式")
    void compactPreservesUnreadData() {
        // Given: 部分读取的 Buffer
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put("Hello".getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        buffer.get(); // 读取 'H'
        buffer.get(); // 读取 'e'

        // When: 调用 compact()
        buffer.compact();

        // Then: 未读数据移到开头，position 在未读数据后
        assertThat(buffer.position()).isEqualTo(3); // "llo" 的长度
        assertThat(buffer.limit()).isEqualTo(10);

        // 可以继续写入
        buffer.put((byte) '!');

        // 读取验证
        buffer.flip();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        assertThat(new String(data, StandardCharsets.UTF_8)).isEqualTo("llo!");
    }

    @Test
    @DisplayName("直接缓冲区和堆缓冲区的区别")
    void directVsHeapBuffer() {
        // 堆缓冲区
        ByteBuffer heapBuffer = ByteBuffer.allocate(1024);
        assertThat(heapBuffer.isDirect()).isFalse();

        // 直接缓冲区
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024);
        assertThat(directBuffer.isDirect()).isTrue();
    }

    @Test
    @DisplayName("FileChannel 可以写入和读取数据")
    void fileChannelReadWrite() throws Exception {
        // Given: 创建临时文件
        Path filePath = tempDir.resolve("test.txt");
        String content = "Hello, NIO!";

        // When: 写入数据
        try (RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "rw");
             FileChannel channel = file.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate(48);
            buffer.put(content.getBytes(StandardCharsets.UTF_8));
            buffer.flip();
            channel.write(buffer);
        }

        // Then: 可以读取写入的数据
        try (RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "r");
             FileChannel channel = file.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate(48);
            int bytesRead = channel.read(buffer);
            assertThat(bytesRead).isEqualTo(content.length());

            buffer.flip();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            assertThat(new String(data, StandardCharsets.UTF_8)).isEqualTo(content);
        }
    }

    @Test
    @DisplayName("wrap() 可以将字节数组包装为 Buffer")
    void wrapCreatesBufferFromArray() {
        // Given: 字节数组
        byte[] array = "Hello".getBytes(StandardCharsets.UTF_8);

        // When: 包装为 Buffer
        ByteBuffer buffer = ByteBuffer.wrap(array);

        // Then: Buffer 包含数组数据，可直接读取
        assertThat(buffer.capacity()).isEqualTo(array.length);
        assertThat(buffer.position()).isEqualTo(0);
        assertThat(buffer.limit()).isEqualTo(array.length);

        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        assertThat(new String(data, StandardCharsets.UTF_8)).isEqualTo("Hello");
    }

    @Test
    @DisplayName("slice() 创建 Buffer 的视图")
    void sliceCreatesBufferView() {
        // Given: 原始 Buffer
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put("HelloWorld".getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        buffer.position(5); // 跳过 "Hello"

        // When: 创建 slice
        ByteBuffer slice = buffer.slice();

        // Then: slice 包含从当前 position 开始的数据
        assertThat(slice.capacity()).isEqualTo(5);
        assertThat(slice.position()).isEqualTo(0);

        byte[] data = new byte[slice.remaining()];
        slice.get(data);
        assertThat(new String(data, StandardCharsets.UTF_8)).isEqualTo("World");
    }
}
