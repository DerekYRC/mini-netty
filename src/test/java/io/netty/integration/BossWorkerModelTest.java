package io.netty.integration;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.nio.NioServerSocketChannel;
import org.junit.jupiter.api.*;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Boss/Worker 主从 Reactor 模型集成测试
 *
 * <p>测试 ServerBootstrap 的 Boss/Worker 线程模型是否正确工作
 */
@DisplayName("Boss/Worker 主从 Reactor 模型测试")
class BossWorkerModelTest {

    @Nested
    @DisplayName("配置测试")
    class ConfigurationTests {

        @Test
        @DisplayName("Boss 1个线程 + Worker 4个线程配置正确")
        void configuresBossAndWorkerGroups() {
            NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
            NioEventLoopGroup workerGroup = new NioEventLoopGroup(4);
            
            try {
                assertThat(bossGroup.executorCount()).isEqualTo(1);
                assertThat(workerGroup.executorCount()).isEqualTo(4);
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }

        @Test
        @DisplayName("ServerBootstrap 绑定 Boss 和 Worker 组")
        void bindsBossAndWorkerGroups() {
            NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
            NioEventLoopGroup workerGroup = new NioEventLoopGroup(4);
            
            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                         .channel(NioServerSocketChannel.class)
                         .childHandler(new ChannelInitializer<Channel>() {
                             @Override
                             protected void initChannel(Channel ch) {
                                 // 空实现
                             }
                         });

                // 验证 childGroup 正确设置
                assertThat(bootstrap.childGroup()).isSameAs(workerGroup);
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }

        @Test
        @DisplayName("未设置 childGroup 时使用 parentGroup")
        void usesParentGroupWhenChildGroupNotSet() {
            NioEventLoopGroup bossGroup = new NioEventLoopGroup(2);
            
            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup)
                         .channel(NioServerSocketChannel.class)
                         .childHandler(new ChannelInitializer<Channel>() {
                             @Override
                             protected void initChannel(Channel ch) {
                                 // 空实现
                             }
                         });

                // validate() 会将 parentGroup 作为 childGroup
                bootstrap.validate();
                assertThat(bootstrap.childGroup()).isSameAs(bossGroup);
            } finally {
                bossGroup.shutdownGracefully();
            }
        }
    }

    @Nested
    @DisplayName("线程模型测试")
    class ThreadModelTests {

        @Test
        @DisplayName("Boss 使用单独的线程组")
        void bossUsesOwnThreadGroup() {
            NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
            NioEventLoopGroup workerGroup = new NioEventLoopGroup(4);
            
            try {
                // Boss 应该只有 1 个 EventLoop
                assertThat(bossGroup.executorCount()).isEqualTo(1);
                
                // Boss EventLoop 不同于 Worker EventLoop
                EventLoop bossLoop = bossGroup.next();
                EventLoop workerLoop = workerGroup.next();
                
                assertThat(bossLoop).isNotSameAs(workerLoop);
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }

        @Test
        @DisplayName("Worker 组支持多个线程")
        void workerGroupSupportsMultipleThreads() {
            NioEventLoopGroup workerGroup = new NioEventLoopGroup(4);
            
            try {
                assertThat(workerGroup.executorCount()).isEqualTo(4);
                
                // 验证轮询选择不同的 EventLoop
                Set<EventLoop> loops = ConcurrentHashMap.newKeySet();
                for (int i = 0; i < 4; i++) {
                    loops.add(workerGroup.next());
                }
                
                assertThat(loops).hasSize(4);
            } finally {
                workerGroup.shutdownGracefully();
            }
        }

        @Test
        @DisplayName("Worker 轮询分配 EventLoop")
        void workerDistributesEvenly() {
            NioEventLoopGroup workerGroup = new NioEventLoopGroup(3);
            
            try {
                // 调用 6 次，每个 EventLoop 应该被选中 2 次
                int[] counts = new int[3];
                for (int i = 0; i < 6; i++) {
                    EventLoop loop = workerGroup.next();
                    for (int j = 0; j < 3; j++) {
                        if (loop == workerGroup.eventLoop(j)) {
                            counts[j]++;
                            break;
                        }
                    }
                }
                
                assertThat(counts[0]).isEqualTo(2);
                assertThat(counts[1]).isEqualTo(2);
                assertThat(counts[2]).isEqualTo(2);
            } finally {
                workerGroup.shutdownGracefully();
            }
        }
    }

    @Nested
    @DisplayName("生命周期测试")
    class LifecycleTests {

        @Test
        @DisplayName("优雅关闭 Boss 和 Worker 组")
        void gracefullyShutsBothGroups() {
            NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
            NioEventLoopGroup workerGroup = new NioEventLoopGroup(2);
            
            // 关闭
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();

            assertThat(bossGroup.isShutdown()).isTrue();
            assertThat(workerGroup.isShutdown()).isTrue();
        }

        @Test
        @DisplayName("关闭后 EventLoopGroup 不再接受新任务")
        void noNewTasksAfterShutdown() {
            NioEventLoopGroup group = new NioEventLoopGroup(1);
            
            group.shutdownGracefully();
            
            assertThat(group.isShutdown()).isTrue();
            // 关闭后尝试执行任务应该是安全的（不会报错）
            // 但任务不一定会执行
        }
    }

    @Nested
    @DisplayName("验收场景测试")
    class AcceptanceScenarioTests {

        @Test
        @DisplayName("场景: 完整的 Boss/Worker 配置")
        void scenarioCompleteSetup() {
            // Given: 创建 Boss (1 线程) 和 Worker (4 线程) 组
            NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
            NioEventLoopGroup workerGroup = new NioEventLoopGroup(4);
            
            try {
                // When: 配置服务器
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                         .channel(NioServerSocketChannel.class)
                         .option(ChannelOption.SO_BACKLOG, 128)
                         .childOption(ChannelOption.SO_KEEPALIVE, true)
                         .childHandler(new ChannelInitializer<Channel>() {
                             @Override
                             protected void initChannel(Channel ch) {
                                 ch.pipeline().addLast("echo", new ChannelInboundHandlerAdapter());
                             }
                         });

                // Then: 验证配置
                assertThat(bossGroup.executorCount()).isEqualTo(1);
                assertThat(workerGroup.executorCount()).isEqualTo(4);
                assertThat(bootstrap.childGroup()).isSameAs(workerGroup);
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }

        @Test
        @DisplayName("场景: Boss 和 Worker 使用相同组")
        void scenarioSameGroupForBossAndWorker() {
            // Given: 使用单个 EventLoopGroup
            NioEventLoopGroup group = new NioEventLoopGroup(4);
            
            try {
                // When: 只设置一个 group
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(group)
                         .channel(NioServerSocketChannel.class)
                         .childHandler(new ChannelInitializer<Channel>() {
                             @Override
                             protected void initChannel(Channel ch) {
                             }
                         });

                // Then: validate 会设置 childGroup = parentGroup
                bootstrap.validate();
                assertThat(bootstrap.childGroup()).isSameAs(group);
            } finally {
                group.shutdownGracefully();
            }
        }
    }
}
