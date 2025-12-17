package io.netty.handler.timeout;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * IdleStateHandler 测试
 *
 * <p>测试空闲状态处理器的配置和事件
 */
@DisplayName("IdleStateHandler 测试")
class IdleStateHandlerTest {

    @Nested
    @DisplayName("IdleState 枚举测试")
    class IdleStateEnumTests {

        @Test
        @DisplayName("包含三种空闲状态")
        void containsThreeIdleStates() {
            IdleState[] states = IdleState.values();
            
            assertThat(states).hasSize(3);
            assertThat(states).containsExactly(IdleState.READER_IDLE, IdleState.WRITER_IDLE, IdleState.ALL_IDLE);
        }

        @Test
        @DisplayName("valueOf 正确解析")
        void valueOfParsesCorrectly() {
            assertThat(IdleState.valueOf("READER_IDLE")).isEqualTo(IdleState.READER_IDLE);
            assertThat(IdleState.valueOf("WRITER_IDLE")).isEqualTo(IdleState.WRITER_IDLE);
            assertThat(IdleState.valueOf("ALL_IDLE")).isEqualTo(IdleState.ALL_IDLE);
        }
    }

    @Nested
    @DisplayName("IdleStateEvent 测试")
    class IdleStateEventTests {

        @Test
        @DisplayName("预定义的读空闲事件")
        void predefinedReaderIdleEvents() {
            assertThat(IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT.state()).isEqualTo(IdleState.READER_IDLE);
            assertThat(IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT.isFirst()).isTrue();
            
            assertThat(IdleStateEvent.READER_IDLE_STATE_EVENT.state()).isEqualTo(IdleState.READER_IDLE);
            assertThat(IdleStateEvent.READER_IDLE_STATE_EVENT.isFirst()).isFalse();
        }

        @Test
        @DisplayName("预定义的写空闲事件")
        void predefinedWriterIdleEvents() {
            assertThat(IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT.state()).isEqualTo(IdleState.WRITER_IDLE);
            assertThat(IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT.isFirst()).isTrue();
            
            assertThat(IdleStateEvent.WRITER_IDLE_STATE_EVENT.state()).isEqualTo(IdleState.WRITER_IDLE);
            assertThat(IdleStateEvent.WRITER_IDLE_STATE_EVENT.isFirst()).isFalse();
        }

        @Test
        @DisplayName("预定义的全部空闲事件")
        void predefinedAllIdleEvents() {
            assertThat(IdleStateEvent.FIRST_ALL_IDLE_STATE_EVENT.state()).isEqualTo(IdleState.ALL_IDLE);
            assertThat(IdleStateEvent.FIRST_ALL_IDLE_STATE_EVENT.isFirst()).isTrue();
            
            assertThat(IdleStateEvent.ALL_IDLE_STATE_EVENT.state()).isEqualTo(IdleState.ALL_IDLE);
            assertThat(IdleStateEvent.ALL_IDLE_STATE_EVENT.isFirst()).isFalse();
        }

        @Test
        @DisplayName("toString 包含状态信息")
        void toStringContainsStateInfo() {
            assertThat(IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT.toString())
                    .contains("READER_IDLE")
                    .contains("first");
            
            assertThat(IdleStateEvent.WRITER_IDLE_STATE_EVENT.toString())
                    .contains("WRITER_IDLE")
                    .doesNotContain("first");
        }
    }

    @Nested
    @DisplayName("IdleStateHandler 构造测试")
    class IdleStateHandlerConstructorTests {

        @Test
        @DisplayName("使用秒构造")
        void constructsWithSeconds() {
            IdleStateHandler handler = new IdleStateHandler(30, 60, 90);
            
            assertThat(handler.getReaderIdleTime(TimeUnit.SECONDS)).isEqualTo(30);
            assertThat(handler.getWriterIdleTime(TimeUnit.SECONDS)).isEqualTo(60);
            assertThat(handler.getAllIdleTime(TimeUnit.SECONDS)).isEqualTo(90);
        }

        @Test
        @DisplayName("使用自定义时间单位构造")
        void constructsWithCustomTimeUnit() {
            IdleStateHandler handler = new IdleStateHandler(100, 200, 300, TimeUnit.MILLISECONDS);
            
            assertThat(handler.getReaderIdleTime(TimeUnit.MILLISECONDS)).isEqualTo(100);
            assertThat(handler.getWriterIdleTime(TimeUnit.MILLISECONDS)).isEqualTo(200);
            assertThat(handler.getAllIdleTime(TimeUnit.MILLISECONDS)).isEqualTo(300);
        }

        @Test
        @DisplayName("0 表示禁用")
        void zeroMeansDisabled() {
            IdleStateHandler handler = new IdleStateHandler(0, 0, 0);
            
            assertThat(handler.getReaderIdleTime(TimeUnit.SECONDS)).isEqualTo(0);
            assertThat(handler.getWriterIdleTime(TimeUnit.SECONDS)).isEqualTo(0);
            assertThat(handler.getAllIdleTime(TimeUnit.SECONDS)).isEqualTo(0);
        }

        @Test
        @DisplayName("null 时间单位抛出异常")
        void throwsExceptionForNullTimeUnit() {
            assertThatThrownBy(() -> new IdleStateHandler(10, 20, 30, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("负数转换为 0")
        void negativeValuesConvertedToZero() {
            IdleStateHandler handler = new IdleStateHandler(-10, -20, -30, TimeUnit.SECONDS);
            
            assertThat(handler.getReaderIdleTime(TimeUnit.SECONDS)).isEqualTo(0);
            assertThat(handler.getWriterIdleTime(TimeUnit.SECONDS)).isEqualTo(0);
            assertThat(handler.getAllIdleTime(TimeUnit.SECONDS)).isEqualTo(0);
        }

        @Test
        @DisplayName("只设置读空闲")
        void readerIdleOnly() {
            IdleStateHandler handler = new IdleStateHandler(30, 0, 0);
            
            assertThat(handler.getReaderIdleTime(TimeUnit.SECONDS)).isEqualTo(30);
            assertThat(handler.getWriterIdleTime(TimeUnit.SECONDS)).isEqualTo(0);
            assertThat(handler.getAllIdleTime(TimeUnit.SECONDS)).isEqualTo(0);
        }

        @Test
        @DisplayName("只设置写空闲")
        void writerIdleOnly() {
            IdleStateHandler handler = new IdleStateHandler(0, 60, 0);
            
            assertThat(handler.getReaderIdleTime(TimeUnit.SECONDS)).isEqualTo(0);
            assertThat(handler.getWriterIdleTime(TimeUnit.SECONDS)).isEqualTo(60);
            assertThat(handler.getAllIdleTime(TimeUnit.SECONDS)).isEqualTo(0);
        }

        @Test
        @DisplayName("只设置全部空闲")
        void allIdleOnly() {
            IdleStateHandler handler = new IdleStateHandler(0, 0, 90);
            
            assertThat(handler.getReaderIdleTime(TimeUnit.SECONDS)).isEqualTo(0);
            assertThat(handler.getWriterIdleTime(TimeUnit.SECONDS)).isEqualTo(0);
            assertThat(handler.getAllIdleTime(TimeUnit.SECONDS)).isEqualTo(90);
        }
    }

    @Nested
    @DisplayName("时间单位转换测试")
    class TimeUnitConversionTests {

        @Test
        @DisplayName("秒转毫秒")
        void convertsSecondsToMillis() {
            IdleStateHandler handler = new IdleStateHandler(1, 2, 3);
            
            assertThat(handler.getReaderIdleTime(TimeUnit.MILLISECONDS)).isEqualTo(1000);
            assertThat(handler.getWriterIdleTime(TimeUnit.MILLISECONDS)).isEqualTo(2000);
            assertThat(handler.getAllIdleTime(TimeUnit.MILLISECONDS)).isEqualTo(3000);
        }

        @Test
        @DisplayName("毫秒转秒")
        void convertsMillisToSeconds() {
            IdleStateHandler handler = new IdleStateHandler(1500, 2500, 3500, TimeUnit.MILLISECONDS);
            
            assertThat(handler.getReaderIdleTime(TimeUnit.SECONDS)).isEqualTo(1);
            assertThat(handler.getWriterIdleTime(TimeUnit.SECONDS)).isEqualTo(2);
            assertThat(handler.getAllIdleTime(TimeUnit.SECONDS)).isEqualTo(3);
        }

        @Test
        @DisplayName("分钟转秒")
        void convertsMinutesToSeconds() {
            IdleStateHandler handler = new IdleStateHandler(1, 2, 3, TimeUnit.MINUTES);
            
            assertThat(handler.getReaderIdleTime(TimeUnit.SECONDS)).isEqualTo(60);
            assertThat(handler.getWriterIdleTime(TimeUnit.SECONDS)).isEqualTo(120);
            assertThat(handler.getAllIdleTime(TimeUnit.SECONDS)).isEqualTo(180);
        }
    }

    @Nested
    @DisplayName("newIdleStateEvent 测试")
    class NewIdleStateEventTests {

        @Test
        @DisplayName("创建第一次读空闲事件")
        void createsFirstReaderIdleEvent() {
            IdleStateHandler handler = new IdleStateHandler(30, 0, 0);
            
            IdleStateEvent event = handler.newIdleStateEvent(IdleState.READER_IDLE, true);
            
            assertThat(event).isSameAs(IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT);
        }

        @Test
        @DisplayName("创建非第一次读空闲事件")
        void createsReaderIdleEvent() {
            IdleStateHandler handler = new IdleStateHandler(30, 0, 0);
            
            IdleStateEvent event = handler.newIdleStateEvent(IdleState.READER_IDLE, false);
            
            assertThat(event).isSameAs(IdleStateEvent.READER_IDLE_STATE_EVENT);
        }

        @Test
        @DisplayName("创建第一次写空闲事件")
        void createsFirstWriterIdleEvent() {
            IdleStateHandler handler = new IdleStateHandler(0, 60, 0);
            
            IdleStateEvent event = handler.newIdleStateEvent(IdleState.WRITER_IDLE, true);
            
            assertThat(event).isSameAs(IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT);
        }

        @Test
        @DisplayName("创建第一次全部空闲事件")
        void createsFirstAllIdleEvent() {
            IdleStateHandler handler = new IdleStateHandler(0, 0, 90);
            
            IdleStateEvent event = handler.newIdleStateEvent(IdleState.ALL_IDLE, true);
            
            assertThat(event).isSameAs(IdleStateEvent.FIRST_ALL_IDLE_STATE_EVENT);
        }
    }

    @Nested
    @DisplayName("验收场景测试")
    class AcceptanceScenarioTests {

        @Test
        @DisplayName("场景: 心跳检测配置")
        void scenarioHeartbeatConfiguration() {
            // Given: 配置 30 秒读空闲超时，用于心跳检测
            IdleStateHandler handler = new IdleStateHandler(30, 0, 0);
            
            // Then: 只有读空闲被配置
            assertThat(handler.getReaderIdleTime(TimeUnit.SECONDS)).isEqualTo(30);
            assertThat(handler.getWriterIdleTime(TimeUnit.SECONDS)).isEqualTo(0);
            assertThat(handler.getAllIdleTime(TimeUnit.SECONDS)).isEqualTo(0);
        }

        @Test
        @DisplayName("场景: 完整的空闲检测配置")
        void scenarioCompleteIdleConfiguration() {
            // Given: 配置完整的空闲检测
            // 30秒读空闲、60秒写空闲、90秒全部空闲
            IdleStateHandler handler = new IdleStateHandler(30, 60, 90);
            
            // Then: 所有超时都正确配置
            assertThat(handler.getReaderIdleTime(TimeUnit.SECONDS)).isEqualTo(30);
            assertThat(handler.getWriterIdleTime(TimeUnit.SECONDS)).isEqualTo(60);
            assertThat(handler.getAllIdleTime(TimeUnit.SECONDS)).isEqualTo(90);
        }

        @Test
        @DisplayName("场景: 使用毫秒精度配置")
        void scenarioMillisecondPrecision() {
            // Given: 需要更精确的超时控制（500ms）
            IdleStateHandler handler = new IdleStateHandler(500, 1000, 1500, TimeUnit.MILLISECONDS);
            
            // Then: 毫秒级精度正确
            assertThat(handler.getReaderIdleTime(TimeUnit.MILLISECONDS)).isEqualTo(500);
            assertThat(handler.getWriterIdleTime(TimeUnit.MILLISECONDS)).isEqualTo(1000);
            assertThat(handler.getAllIdleTime(TimeUnit.MILLISECONDS)).isEqualTo(1500);
        }
    }
}
