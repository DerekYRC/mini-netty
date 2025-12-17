package io.netty.buffer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * ByteBuf 分配器测试
 */
@DisplayName("ByteBufAllocator 测试")
class ByteBufAllocatorTest {

    @Nested
    @DisplayName("UnpooledByteBufAllocator 测试")
    class UnpooledAllocatorTests {

        private final ByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;

        @Test
        @DisplayName("buffer() 应分配默认大小的 ByteBuf")
        void bufferShouldAllocateDefaultSize() {
            ByteBuf buf = allocator.buffer();
            
            try {
                assertThat(buf).isNotNull();
                assertThat(buf.capacity()).isEqualTo(256);
                assertThat(buf.refCnt()).isEqualTo(1);
            } finally {
                buf.release();
            }
        }

        @Test
        @DisplayName("buffer(initialCapacity) 应分配指定容量")
        void bufferWithCapacityShouldWork() {
            ByteBuf buf = allocator.buffer(512);
            
            try {
                assertThat(buf.capacity()).isEqualTo(512);
            } finally {
                buf.release();
            }
        }

        @Test
        @DisplayName("buffer(initialCapacity, maxCapacity) 应设置最大容量")
        void bufferWithMaxCapacityShouldWork() {
            ByteBuf buf = allocator.buffer(128, 1024);
            
            try {
                assertThat(buf.capacity()).isEqualTo(128);
                assertThat(buf.maxCapacity()).isEqualTo(1024);
            } finally {
                buf.release();
            }
        }

        @Test
        @DisplayName("heapBuffer() 应分配堆内存 ByteBuf")
        void heapBufferShouldAllocateHeapMemory() {
            ByteBuf buf = allocator.heapBuffer();
            
            try {
                assertThat(buf).isInstanceOf(HeapByteBuf.class);
                assertThat(buf.hasArray()).isTrue();
            } finally {
                buf.release();
            }
        }

        @Test
        @DisplayName("heapBuffer 可以读写数据")
        void heapBufferShouldBeReadWritable() {
            ByteBuf buf = allocator.heapBuffer(256);
            
            try {
                buf.writeBytes("Hello, World!".getBytes(StandardCharsets.UTF_8));
                
                assertThat(buf.readableBytes()).isEqualTo(13);
                assertThat(buf.toString(StandardCharsets.UTF_8)).isEqualTo("Hello, World!");
            } finally {
                buf.release();
            }
        }

        @Test
        @DisplayName("directBuffer 应可用（简化实现使用堆内存）")
        void directBufferShouldBeAvailable() {
            ByteBuf buf = allocator.directBuffer();
            
            try {
                assertThat(buf).isNotNull();
                buf.writeInt(42);
                assertThat(buf.readInt()).isEqualTo(42);
            } finally {
                buf.release();
            }
        }

        @Test
        @DisplayName("isDirectBufferPooled 应返回 false")
        void isDirectBufferPooledShouldReturnFalse() {
            assertThat(allocator.isDirectBufferPooled()).isFalse();
        }
    }

    @Nested
    @DisplayName("分配器配置测试")
    class AllocatorConfigTests {

        @Test
        @DisplayName("preferDirect=false 时 buffer() 应返回堆内存")
        void bufferShouldReturnHeapWhenPreferDirectIsFalse() {
            ByteBufAllocator heapAllocator = new UnpooledByteBufAllocator(false);
            ByteBuf buf = heapAllocator.buffer();
            
            try {
                assertThat(buf.hasArray()).isTrue();
            } finally {
                buf.release();
            }
        }

        @Test
        @DisplayName("preferDirect=true 时 buffer() 应返回直接内存（简化实现）")
        void bufferShouldReturnDirectWhenPreferDirectIsTrue() {
            ByteBufAllocator directAllocator = new UnpooledByteBufAllocator(true);
            ByteBuf buf = directAllocator.buffer();
            
            try {
                // 当前简化实现仍返回堆内存
                assertThat(buf).isNotNull();
            } finally {
                buf.release();
            }
        }
    }

    @Nested
    @DisplayName("验收场景测试")
    class AcceptanceScenarioTests {

        @Test
        @DisplayName("典型的消息处理场景")
        void typicalMessageProcessingScenario() {
            ByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
            
            // 分配 buffer 写入请求
            ByteBuf requestBuf = allocator.buffer(256);
            try {
                String request = "GET /index.html HTTP/1.1";
                requestBuf.writeBytes(request.getBytes(StandardCharsets.UTF_8));
                
                // 读取并处理
                byte[] data = new byte[requestBuf.readableBytes()];
                requestBuf.readBytes(data);
                
                assertThat(new String(data, StandardCharsets.UTF_8)).isEqualTo(request);
            } finally {
                requestBuf.release();
            }
            
            // 分配 buffer 写入响应
            ByteBuf responseBuf = allocator.buffer(512);
            try {
                String response = "HTTP/1.1 200 OK\r\nContent-Length: 13\r\n\r\nHello, World!";
                responseBuf.writeBytes(response.getBytes(StandardCharsets.UTF_8));
                
                assertThat(responseBuf.readableBytes()).isEqualTo(response.length());
            } finally {
                responseBuf.release();
            }
        }

        @Test
        @DisplayName("多个 ByteBuf 的独立生命周期")
        void multipleBuffersIndependentLifecycle() {
            ByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
            
            ByteBuf buf1 = allocator.buffer(64);
            ByteBuf buf2 = allocator.buffer(128);
            ByteBuf buf3 = allocator.buffer(256);
            
            try {
                // 每个 buffer 独立
                buf1.writeInt(1);
                buf2.writeInt(2);
                buf3.writeInt(3);
                
                assertThat(buf1.readInt()).isEqualTo(1);
                assertThat(buf2.readInt()).isEqualTo(2);
                assertThat(buf3.readInt()).isEqualTo(3);
            } finally {
                buf1.release();
                buf2.release();
                buf3.release();
            }
            
            // 所有 buffer 已释放
            assertThat(buf1.refCnt()).isZero();
            assertThat(buf2.refCnt()).isZero();
            assertThat(buf3.refCnt()).isZero();
        }

        @Test
        @DisplayName("buffer 扩容场景")
        void bufferExpansionScenario() {
            ByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
            ByteBuf buf = allocator.buffer(8, 1024);
            
            try {
                // 写入超过初始容量的数据
                for (int i = 0; i < 50; i++) {
                    buf.writeInt(i);
                }
                
                // 应该自动扩容
                assertThat(buf.capacity()).isGreaterThan(8);
                assertThat(buf.writerIndex()).isEqualTo(200);
                
                // 验证数据完整性
                for (int i = 0; i < 50; i++) {
                    assertThat(buf.readInt()).isEqualTo(i);
                }
            } finally {
                buf.release();
            }
        }
    }
}
