package io.netty.channel;

import io.netty.channel.nio.NioEventLoopGroup;
import org.junit.jupiter.api.*;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * EventLoopChooser 和工厂测试
 *
 * <p>测试不同的 EventLoop 选择策略
 */
@DisplayName("EventLoopChooser 测试")
class ChannelChooserTest {

    @Nested
    @DisplayName("RoundRobinEventLoopChooser 测试")
    class RoundRobinEventLoopChooserTests {

        @Test
        @DisplayName("轮询选择 EventLoop")
        void selectsEventLoopsInRoundRobin() {
            NioEventLoopGroup group = new NioEventLoopGroup(3);
            try {
                EventLoop[] loops = new EventLoop[3];
                for (int i = 0; i < 3; i++) {
                    loops[i] = group.eventLoop(i);
                }

                RoundRobinEventLoopChooser chooser = new RoundRobinEventLoopChooser(loops);

                // 验证轮询顺序
                assertThat(chooser.next()).isSameAs(loops[0]);
                assertThat(chooser.next()).isSameAs(loops[1]);
                assertThat(chooser.next()).isSameAs(loops[2]);
                assertThat(chooser.next()).isSameAs(loops[0]); // 循环回到第一个
            } finally {
                group.shutdownGracefully();
            }
        }

        @Test
        @DisplayName("均匀分配 EventLoop")
        void distributesEvenly() {
            NioEventLoopGroup group = new NioEventLoopGroup(3);
            try {
                EventLoop[] loops = new EventLoop[3];
                for (int i = 0; i < 3; i++) {
                    loops[i] = group.eventLoop(i);
                }

                RoundRobinEventLoopChooser chooser = new RoundRobinEventLoopChooser(loops);

                // 调用 9 次，每个应该被选中 3 次
                int[] counts = new int[3];
                for (int i = 0; i < 9; i++) {
                    EventLoop selected = chooser.next();
                    for (int j = 0; j < 3; j++) {
                        if (selected == loops[j]) {
                            counts[j]++;
                            break;
                        }
                    }
                }

                assertThat(counts[0]).isEqualTo(3);
                assertThat(counts[1]).isEqualTo(3);
                assertThat(counts[2]).isEqualTo(3);
            } finally {
                group.shutdownGracefully();
            }
        }

        @Test
        @DisplayName("空数组抛出异常")
        void throwsExceptionForEmptyArray() {
            assertThatThrownBy(() -> new RoundRobinEventLoopChooser(new EventLoop[0]))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null 数组抛出异常")
        void throwsExceptionForNullArray() {
            assertThatThrownBy(() -> new RoundRobinEventLoopChooser(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("PowerOfTwoEventLoopChooser 测试")
    class PowerOfTwoEventLoopChooserTests {

        @Test
        @DisplayName("2 的幂数量时使用位运算选择")
        void selectsWithBitOperation() {
            NioEventLoopGroup group = new NioEventLoopGroup(4);
            try {
                EventLoop[] loops = new EventLoop[4];
                for (int i = 0; i < 4; i++) {
                    loops[i] = group.eventLoop(i);
                }

                PowerOfTwoEventLoopChooser chooser = new PowerOfTwoEventLoopChooser(loops);

                // 验证轮询顺序
                assertThat(chooser.next()).isSameAs(loops[0]);
                assertThat(chooser.next()).isSameAs(loops[1]);
                assertThat(chooser.next()).isSameAs(loops[2]);
                assertThat(chooser.next()).isSameAs(loops[3]);
                assertThat(chooser.next()).isSameAs(loops[0]); // 循环回到第一个
            } finally {
                group.shutdownGracefully();
            }
        }

        @Test
        @DisplayName("非 2 的幂数量时抛出异常")
        void throwsExceptionForNonPowerOfTwo() {
            NioEventLoopGroup group = new NioEventLoopGroup(3);
            try {
                EventLoop[] loops = new EventLoop[3];
                for (int i = 0; i < 3; i++) {
                    loops[i] = group.eventLoop(i);
                }

                assertThatThrownBy(() -> new PowerOfTwoEventLoopChooser(loops))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("power of two");
            } finally {
                group.shutdownGracefully();
            }
        }

        @Test
        @DisplayName("isPowerOfTwo 正确判断")
        void isPowerOfTwoWorksCorrectly() {
            // 2 的幂
            assertThat(PowerOfTwoEventLoopChooser.isPowerOfTwo(1)).isTrue();
            assertThat(PowerOfTwoEventLoopChooser.isPowerOfTwo(2)).isTrue();
            assertThat(PowerOfTwoEventLoopChooser.isPowerOfTwo(4)).isTrue();
            assertThat(PowerOfTwoEventLoopChooser.isPowerOfTwo(8)).isTrue();
            assertThat(PowerOfTwoEventLoopChooser.isPowerOfTwo(16)).isTrue();
            assertThat(PowerOfTwoEventLoopChooser.isPowerOfTwo(1024)).isTrue();

            // 非 2 的幂
            assertThat(PowerOfTwoEventLoopChooser.isPowerOfTwo(0)).isFalse();
            assertThat(PowerOfTwoEventLoopChooser.isPowerOfTwo(-1)).isFalse();
            assertThat(PowerOfTwoEventLoopChooser.isPowerOfTwo(3)).isFalse();
            assertThat(PowerOfTwoEventLoopChooser.isPowerOfTwo(5)).isFalse();
            assertThat(PowerOfTwoEventLoopChooser.isPowerOfTwo(6)).isFalse();
            assertThat(PowerOfTwoEventLoopChooser.isPowerOfTwo(7)).isFalse();
            assertThat(PowerOfTwoEventLoopChooser.isPowerOfTwo(100)).isFalse();
        }

        @Test
        @DisplayName("长度为 1 时正常工作")
        void worksWithSingleEventLoop() {
            NioEventLoopGroup group = new NioEventLoopGroup(1);
            try {
                EventLoop[] loops = new EventLoop[1];
                loops[0] = group.eventLoop(0);

                PowerOfTwoEventLoopChooser chooser = new PowerOfTwoEventLoopChooser(loops);

                // 总是返回同一个
                assertThat(chooser.next()).isSameAs(loops[0]);
                assertThat(chooser.next()).isSameAs(loops[0]);
                assertThat(chooser.next()).isSameAs(loops[0]);
            } finally {
                group.shutdownGracefully();
            }
        }
    }

    @Nested
    @DisplayName("DefaultEventLoopChooserFactory 测试")
    class DefaultEventLoopChooserFactoryTests {

        @Test
        @DisplayName("2 的幂数量时返回 PowerOfTwoEventLoopChooser")
        void createsPowerOfTwoChooserForPowerOfTwo() {
            NioEventLoopGroup group = new NioEventLoopGroup(4);
            try {
                EventLoop[] loops = new EventLoop[4];
                for (int i = 0; i < 4; i++) {
                    loops[i] = group.eventLoop(i);
                }

                EventLoopChooser chooser = DefaultEventLoopChooserFactory.INSTANCE.newChooser(loops);
                assertThat(chooser).isInstanceOf(PowerOfTwoEventLoopChooser.class);
            } finally {
                group.shutdownGracefully();
            }
        }

        @Test
        @DisplayName("非 2 的幂数量时返回 RoundRobinEventLoopChooser")
        void createsRoundRobinChooserForNonPowerOfTwo() {
            NioEventLoopGroup group = new NioEventLoopGroup(3);
            try {
                EventLoop[] loops = new EventLoop[3];
                for (int i = 0; i < 3; i++) {
                    loops[i] = group.eventLoop(i);
                }

                EventLoopChooser chooser = DefaultEventLoopChooserFactory.INSTANCE.newChooser(loops);
                assertThat(chooser).isInstanceOf(RoundRobinEventLoopChooser.class);
            } finally {
                group.shutdownGracefully();
            }
        }

        @Test
        @DisplayName("1 个 EventLoop 时返回 PowerOfTwoEventLoopChooser")
        void createsPowerOfTwoChooserForOne() {
            NioEventLoopGroup group = new NioEventLoopGroup(1);
            try {
                EventLoop[] loops = new EventLoop[1];
                loops[0] = group.eventLoop(0);

                EventLoopChooser chooser = DefaultEventLoopChooserFactory.INSTANCE.newChooser(loops);
                assertThat(chooser).isInstanceOf(PowerOfTwoEventLoopChooser.class);
            } finally {
                group.shutdownGracefully();
            }
        }

        @Test
        @DisplayName("空数组抛出异常")
        void throwsExceptionForEmptyArray() {
            assertThatThrownBy(() -> DefaultEventLoopChooserFactory.INSTANCE.newChooser(new EventLoop[0]))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("工厂是单例")
        void factoryIsSingleton() {
            EventLoopChooserFactory factory1 = DefaultEventLoopChooserFactory.INSTANCE;
            EventLoopChooserFactory factory2 = DefaultEventLoopChooserFactory.INSTANCE;
            
            assertThat(factory1).isSameAs(factory2);
        }
    }

    @Nested
    @DisplayName("验收场景测试")
    class AcceptanceScenarioTests {

        @Test
        @DisplayName("场景: 根据线程数自动选择最优策略")
        void scenarioAutoSelectsOptimalStrategy() {
            // Given: 不同数量的 EventLoop 组
            NioEventLoopGroup group4 = new NioEventLoopGroup(4);  // 2 的幂
            NioEventLoopGroup group6 = new NioEventLoopGroup(6);  // 非 2 的幂
            
            try {
                EventLoop[] loops4 = new EventLoop[4];
                EventLoop[] loops6 = new EventLoop[6];
                for (int i = 0; i < 4; i++) loops4[i] = group4.eventLoop(i);
                for (int i = 0; i < 6; i++) loops6[i] = group6.eventLoop(i);

                // When: 使用工厂创建选择器
                EventLoopChooser chooser4 = DefaultEventLoopChooserFactory.INSTANCE.newChooser(loops4);
                EventLoopChooser chooser6 = DefaultEventLoopChooserFactory.INSTANCE.newChooser(loops6);

                // Then: 自动选择最优策略
                assertThat(chooser4).isInstanceOf(PowerOfTwoEventLoopChooser.class);
                assertThat(chooser6).isInstanceOf(RoundRobinEventLoopChooser.class);

                // 两者都能正确轮询
                Set<EventLoop> selected4 = new HashSet<>();
                Set<EventLoop> selected6 = new HashSet<>();
                for (int i = 0; i < 4; i++) selected4.add(chooser4.next());
                for (int i = 0; i < 6; i++) selected6.add(chooser6.next());

                assertThat(selected4).hasSize(4);
                assertThat(selected6).hasSize(6);
            } finally {
                group4.shutdownGracefully();
                group6.shutdownGracefully();
            }
        }
    }
}
