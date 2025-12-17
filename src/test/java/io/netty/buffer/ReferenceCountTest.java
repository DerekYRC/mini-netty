package io.netty.buffer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * 引用计数测试
 *
 * <p>测试原子引用计数实现的正确性和线程安全性。
 */
@DisplayName("引用计数测试")
class ReferenceCountTest {

    @Nested
    @DisplayName("基础引用计数测试")
    class BasicReferenceCountTests {

        @Test
        @DisplayName("初始引用计数应为 1")
        void initialRefCntShouldBeOne() {
            HeapByteBuf buf = new HeapByteBuf(16, 64);
            
            assertThat(buf.refCnt()).isEqualTo(1);
        }

        @Test
        @DisplayName("retain() 应增加引用计数")
        void retainShouldIncreaseRefCnt() {
            HeapByteBuf buf = new HeapByteBuf(16, 64);
            
            buf.retain();
            assertThat(buf.refCnt()).isEqualTo(2);
            
            buf.retain();
            assertThat(buf.refCnt()).isEqualTo(3);
        }

        @Test
        @DisplayName("retain(increment) 应增加指定数量")
        void retainWithIncrementShouldWork() {
            HeapByteBuf buf = new HeapByteBuf(16, 64);
            
            buf.retain(5);
            
            assertThat(buf.refCnt()).isEqualTo(6);
        }

        @Test
        @DisplayName("release() 应减少引用计数")
        void releaseShouldDecreaseRefCnt() {
            HeapByteBuf buf = new HeapByteBuf(16, 64);
            buf.retain();
            buf.retain();
            assertThat(buf.refCnt()).isEqualTo(3);
            
            boolean released = buf.release();
            
            assertThat(released).isFalse();
            assertThat(buf.refCnt()).isEqualTo(2);
        }

        @Test
        @DisplayName("release(decrement) 应减少指定数量")
        void releaseWithDecrementShouldWork() {
            HeapByteBuf buf = new HeapByteBuf(16, 64);
            buf.retain(4);
            assertThat(buf.refCnt()).isEqualTo(5);
            
            boolean released = buf.release(3);
            
            assertThat(released).isFalse();
            assertThat(buf.refCnt()).isEqualTo(2);
        }

        @Test
        @DisplayName("引用计数为 0 时应释放资源")
        void shouldDeallocateWhenRefCntReachesZero() {
            HeapByteBuf buf = new HeapByteBuf(16, 64);
            
            boolean released = buf.release();
            
            assertThat(released).isTrue();
            assertThat(buf.refCnt()).isZero();
        }

        @Test
        @DisplayName("释放后无法再 retain")
        void cannotRetainAfterRelease() {
            HeapByteBuf buf = new HeapByteBuf(16, 64);
            buf.release();
            
            assertThatThrownBy(buf::retain)
                    .isInstanceOf(AbstractReferenceCountedByteBuf.IllegalReferenceCountException.class);
        }

        @Test
        @DisplayName("不能 release 超过引用计数的数量")
        void cannotReleaseMoreThanRefCnt() {
            HeapByteBuf buf = new HeapByteBuf(16, 64);
            
            assertThatThrownBy(() -> buf.release(5))
                    .isInstanceOf(AbstractReferenceCountedByteBuf.IllegalReferenceCountException.class);
        }
    }

    @Nested
    @DisplayName("参数验证测试")
    class ValidationTests {

        @Test
        @DisplayName("retain 不接受非正数")
        void retainShouldRejectNonPositive() {
            HeapByteBuf buf = new HeapByteBuf(16, 64);
            
            assertThatThrownBy(() -> buf.retain(0))
                    .isInstanceOf(IllegalArgumentException.class);
            
            assertThatThrownBy(() -> buf.retain(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("release 不接受非正数")
        void releaseShouldRejectNonPositive() {
            HeapByteBuf buf = new HeapByteBuf(16, 64);
            
            assertThatThrownBy(() -> buf.release(0))
                    .isInstanceOf(IllegalArgumentException.class);
            
            assertThatThrownBy(() -> buf.release(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("并发安全测试")
    class ConcurrencySafetyTests {

        @Test
        @DisplayName("多线程 retain 应线程安全")
        void multiThreadedRetainShouldBeThreadSafe() throws InterruptedException {
            HeapByteBuf buf = new HeapByteBuf(16, 64);
            int threadCount = 10;
            int retainPerThread = 100;
            CountDownLatch latch = new CountDownLatch(threadCount);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < retainPerThread; j++) {
                            buf.retain();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            latch.await();
            executor.shutdown();
            
            // 初始 1 + 每个线程 100 次 retain
            assertThat(buf.refCnt()).isEqualTo(1 + threadCount * retainPerThread);
        }

        @Test
        @DisplayName("多线程 retain 和 release 应线程安全")
        void multiThreadedRetainAndReleaseShouldBeThreadSafe() throws InterruptedException {
            HeapByteBuf buf = new HeapByteBuf(16, 64);
            int threadCount = 10;
            int operationsPerThread = 100;
            CountDownLatch latch = new CountDownLatch(threadCount);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            AtomicInteger successfulOperations = new AtomicInteger(0);
            
            // 先增加足够的引用计数
            buf.retain(threadCount * operationsPerThread);
            
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < operationsPerThread; j++) {
                            buf.release();
                            successfulOperations.incrementAndGet();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            latch.await();
            executor.shutdown();
            
            // 操作应该全部成功
            assertThat(successfulOperations.get()).isEqualTo(threadCount * operationsPerThread);
            // 最终引用计数应为 1（初始值）
            assertThat(buf.refCnt()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("验收场景测试")
    class AcceptanceScenarioTests {

        @Test
        @DisplayName("典型的资源传递场景")
        void typicalResourcePassingScenario() {
            // 创建 ByteBuf
            HeapByteBuf buf = new HeapByteBuf(256, 1024);
            buf.writeBytes("Hello, World!".getBytes());
            
            // 传递给另一个组件（需要 retain）
            buf.retain();
            assertThat(buf.refCnt()).isEqualTo(2);
            
            // 第一个组件完成使用
            buf.release();
            assertThat(buf.refCnt()).isEqualTo(1);
            
            // 第二个组件完成使用
            boolean released = buf.release();
            assertThat(released).isTrue();
            assertThat(buf.refCnt()).isZero();
        }

        @Test
        @DisplayName("try-finally 模式的资源管理")
        void tryFinallyResourceManagement() {
            HeapByteBuf buf = new HeapByteBuf(256, 1024);
            
            try {
                buf.writeBytes("Test data".getBytes());
                // 正常处理
                assertThat(buf.readableBytes()).isGreaterThan(0);
            } finally {
                buf.release();
            }
            
            assertThat(buf.refCnt()).isZero();
        }

        @Test
        @DisplayName("池化场景的引用计数")
        void pooledBufferReferenceCount() {
            // 模拟从池中获取 buffer
            HeapByteBuf pooledBuf = new HeapByteBuf(256, 1024);
            
            // 写入数据
            pooledBuf.writeInt(42);
            
            // 使用完成后释放回池
            boolean released = pooledBuf.release();
            assertThat(released).isTrue();
            
            // 模拟池重置 buffer（实际池化实现会重置状态）
            // 这里只是验证释放成功
        }
    }
}
