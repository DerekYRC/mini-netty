package io.netty.channel.nio;

import io.netty.channel.EventLoop;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * NioEventLoopGroup 测试
 *
 * <p>测试 NioEventLoopGroup 的创建、轮询和生命周期功能
 */
@DisplayName("NioEventLoopGroup 测试")
class NioEventLoopGroupTest {

    @Test
    @DisplayName("使用默认线程数创建")
    void createsWithDefaultThreadCount() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            int expectedThreads = Math.max(1, Runtime.getRuntime().availableProcessors() * 2);
            assertThat(group.executorCount()).isEqualTo(expectedThreads);
        } finally {
            group.shutdownGracefully();
        }
    }

    @Test
    @DisplayName("使用指定线程数创建")
    void createsWithSpecifiedThreadCount() {
        NioEventLoopGroup group = new NioEventLoopGroup(4);
        try {
            assertThat(group.executorCount()).isEqualTo(4);
        } finally {
            group.shutdownGracefully();
        }
    }

    @Test
    @DisplayName("使用负数线程数创建时抛出异常")
    void throwsExceptionForNegativeThreadCount() {
        assertThatThrownBy(() -> new NioEventLoopGroup(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("next() 返回 EventLoop")
    void nextReturnsEventLoop() {
        NioEventLoopGroup group = new NioEventLoopGroup(2);
        try {
            EventLoop loop = group.next();
            assertThat(loop).isNotNull();
        } finally {
            group.shutdownGracefully();
        }
    }

    @Test
    @DisplayName("轮询循环回到第一个")
    void roundRobinCyclesBack() {
        NioEventLoopGroup group = new NioEventLoopGroup(2);
        try {
            EventLoop loop1 = group.next(); // index 0
            EventLoop loop2 = group.next(); // index 1
            EventLoop loop3 = group.next(); // index 0 again
            
            assertThat(loop3).isSameAs(loop1);
        } finally {
            group.shutdownGracefully();
        }
    }

    @Test
    @DisplayName("通过索引获取 EventLoop")
    void getsEventLoopByIndex() {
        NioEventLoopGroup group = new NioEventLoopGroup(3);
        try {
            NioEventLoop loop0 = group.eventLoop(0);
            NioEventLoop loop1 = group.eventLoop(1);
            NioEventLoop loop2 = group.eventLoop(2);
            
            assertThat(loop0).isNotNull();
            assertThat(loop1).isNotNull();
            assertThat(loop2).isNotNull();
            assertThat(loop0).isNotSameAs(loop1);
        } finally {
            group.shutdownGracefully();
        }
    }

    @Test
    @DisplayName("索引越界时抛出异常")
    void throwsExceptionForInvalidIndex() {
        NioEventLoopGroup group = new NioEventLoopGroup(2);
        try {
            assertThatThrownBy(() -> group.eventLoop(-1))
                    .isInstanceOf(IndexOutOfBoundsException.class);
            
            assertThatThrownBy(() -> group.eventLoop(2))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        } finally {
            group.shutdownGracefully();
        }
    }

    @Test
    @DisplayName("未关闭时 isShutdown 返回 false")
    void isNotShutdownInitially() {
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        try {
            assertThat(group.isShutdown()).isFalse();
        } finally {
            group.shutdownGracefully();
        }
    }

    @Test
    @DisplayName("关闭后 isShutdown 返回 true")
    void isShutdownAfterShutdown() {
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        group.shutdownGracefully();
        
        assertThat(group.isShutdown()).isTrue();
    }

    @Test
    @DisplayName("场景: 创建主从 Reactor 线程组")
    void scenarioBossWorkerReactorGroups() {
        // Given: 创建 Boss 和 Worker 线程组
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(4);
        
        try {
            // Then: 验证线程数正确
            assertThat(bossGroup.executorCount()).isEqualTo(1);
            assertThat(workerGroup.executorCount()).isEqualTo(4);
            
            // 验证 Boss 总是返回同一个 EventLoop
            EventLoop bossLoop1 = bossGroup.next();
            EventLoop bossLoop2 = bossGroup.next();
            assertThat(bossLoop1).isSameAs(bossLoop2);
            
            // 验证 Worker 轮询返回不同 EventLoop
            Set<EventLoop> workerLoops = new HashSet<>();
            for (int i = 0; i < 4; i++) {
                workerLoops.add(workerGroup.next());
            }
            assertThat(workerLoops).hasSize(4);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
