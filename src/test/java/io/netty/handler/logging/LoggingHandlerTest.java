package io.netty.handler.logging;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * LoggingHandler 测试
 *
 * <p>测试日志处理器的功能
 */
@DisplayName("LoggingHandler 测试")
class LoggingHandlerTest {

    @Nested
    @DisplayName("LogLevel 枚举测试")
    class LogLevelTests {

        @Test
        @DisplayName("包含五种日志级别")
        void containsFiveLogLevels() {
            LogLevel[] levels = LogLevel.values();
            
            assertThat(levels).hasSize(5);
            assertThat(levels).containsExactly(
                    LogLevel.TRACE, LogLevel.DEBUG, LogLevel.INFO, 
                    LogLevel.WARN, LogLevel.ERROR
            );
        }

        @Test
        @DisplayName("valueOf 正确解析")
        void valueOfParsesCorrectly() {
            assertThat(LogLevel.valueOf("TRACE")).isEqualTo(LogLevel.TRACE);
            assertThat(LogLevel.valueOf("DEBUG")).isEqualTo(LogLevel.DEBUG);
            assertThat(LogLevel.valueOf("INFO")).isEqualTo(LogLevel.INFO);
            assertThat(LogLevel.valueOf("WARN")).isEqualTo(LogLevel.WARN);
            assertThat(LogLevel.valueOf("ERROR")).isEqualTo(LogLevel.ERROR);
        }
    }

    @Nested
    @DisplayName("LoggingHandler 构造测试")
    class ConstructorTests {

        @Test
        @DisplayName("默认构造器使用 DEBUG 级别")
        void defaultConstructorUsesDebugLevel() {
            LoggingHandler handler = new LoggingHandler();
            
            assertThat(handler.level()).isEqualTo(LogLevel.DEBUG);
            assertThat(handler.name()).isEqualTo("LoggingHandler");
        }

        @Test
        @DisplayName("使用指定级别构造")
        void constructorWithLevel() {
            LoggingHandler handler = new LoggingHandler(LogLevel.INFO);
            
            assertThat(handler.level()).isEqualTo(LogLevel.INFO);
            assertThat(handler.name()).isEqualTo("LoggingHandler");
        }

        @Test
        @DisplayName("使用指定名称构造")
        void constructorWithName() {
            LoggingHandler handler = new LoggingHandler("SERVER");
            
            assertThat(handler.level()).isEqualTo(LogLevel.DEBUG);
            assertThat(handler.name()).isEqualTo("SERVER");
        }

        @Test
        @DisplayName("使用名称和级别构造")
        void constructorWithNameAndLevel() {
            LoggingHandler handler = new LoggingHandler("CLIENT", LogLevel.TRACE);
            
            assertThat(handler.level()).isEqualTo(LogLevel.TRACE);
            assertThat(handler.name()).isEqualTo("CLIENT");
        }

        @Test
        @DisplayName("null 名称抛出异常")
        void throwsExceptionForNullName() {
            assertThatThrownBy(() -> new LoggingHandler(null, LogLevel.DEBUG))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null 级别抛出异常")
        void throwsExceptionForNullLevel() {
            assertThatThrownBy(() -> new LoggingHandler("test", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("事件传递测试")
    class EventPropagationTests {

        private MockChannel channel;
        private RecordingHandler downstream;

        @BeforeEach
        void setUp() {
            channel = new MockChannel();
            downstream = new RecordingHandler();
            channel.pipeline().addLast("logger", new LoggingHandler());
            channel.pipeline().addLast("recorder", downstream);
        }

        @Test
        @DisplayName("channelRegistered 传递")
        void channelRegisteredPropagates() {
            channel.pipeline().fireChannelRegistered();
            
            assertThat(downstream.events).contains("channelRegistered");
        }

        @Test
        @DisplayName("channelActive 传递")
        void channelActivePropagates() {
            channel.pipeline().fireChannelActive();
            
            assertThat(downstream.events).contains("channelActive");
        }

        @Test
        @DisplayName("channelRead 传递")
        void channelReadPropagates() {
            channel.pipeline().fireChannelRead("test message");
            
            assertThat(downstream.events).contains("channelRead:test message");
        }

        @Test
        @DisplayName("channelInactive 传递")
        void channelInactivePropagates() {
            channel.pipeline().fireChannelInactive();
            
            assertThat(downstream.events).contains("channelInactive");
        }

        @Test
        @DisplayName("exceptionCaught 传递")
        void exceptionCaughtPropagates() {
            channel.pipeline().fireExceptionCaught(new RuntimeException("test error"));
            
            assertThat(downstream.events).anyMatch(e -> e.contains("exceptionCaught"));
        }

        @Test
        @DisplayName("userEventTriggered 传递")
        void userEventTriggeredPropagates() {
            channel.pipeline().fireUserEventTriggered("custom event");
            
            assertThat(downstream.events).contains("userEventTriggered:custom event");
        }
    }

    @Nested
    @DisplayName("ByteBuf 格式化测试")
    class ByteBufFormatTests {

        @Test
        @DisplayName("格式化空 ByteBuf")
        void formatsEmptyByteBuf() {
            LoggingHandler handler = new LoggingHandler();
            ByteBuf buf = UnpooledByteBufAllocator.DEFAULT.buffer(0);
            
            String result = handler.formatData(buf);
            
            assertThat(result).isEqualTo("ByteBuf(0B)");
            buf.release();
        }

        @Test
        @DisplayName("格式化带内容的 ByteBuf")
        void formatsByteBufWithContent() {
            LoggingHandler handler = new LoggingHandler();
            ByteBuf buf = UnpooledByteBufAllocator.DEFAULT.buffer();
            buf.writeBytes("Hello".getBytes(StandardCharsets.US_ASCII));
            
            String result = handler.formatData(buf);
            
            assertThat(result).contains("ByteBuf(5B");
            assertThat(result).contains("hex=");
            assertThat(result).contains("str=\"Hello\"");
            buf.release();
        }

        @Test
        @DisplayName("格式化二进制 ByteBuf 不显示字符串")
        void formatsBinaryByteBufWithoutString() {
            LoggingHandler handler = new LoggingHandler();
            ByteBuf buf = UnpooledByteBufAllocator.DEFAULT.buffer();
            buf.writeByte(0x00);
            buf.writeByte(0x01);
            buf.writeByte(0xFF);
            
            String result = handler.formatData(buf);
            
            assertThat(result).contains("ByteBuf(3B");
            assertThat(result).contains("hex=");
            assertThat(result).doesNotContain("str=");
            buf.release();
        }

        @Test
        @DisplayName("超过 64 字节截断显示")
        void truncatesLargeByteBuf() {
            LoggingHandler handler = new LoggingHandler();
            ByteBuf buf = UnpooledByteBufAllocator.DEFAULT.buffer();
            byte[] data = new byte[100];
            for (int i = 0; i < 100; i++) {
                data[i] = 0x41; // 'A'
            }
            buf.writeBytes(data);
            
            String result = handler.formatData(buf);
            
            assertThat(result).contains("ByteBuf(100B");
            assertThat(result).contains("...");
            buf.release();
        }
    }

    @Nested
    @DisplayName("异常格式化测试")
    class ExceptionFormatTests {

        @Test
        @DisplayName("格式化异常信息")
        void formatsException() {
            LoggingHandler handler = new LoggingHandler();
            RuntimeException ex = new RuntimeException("test error");
            
            String result = handler.formatData(ex);
            
            assertThat(result).isEqualTo("RuntimeException: test error");
        }

        @Test
        @DisplayName("格式化嵌套异常")
        void formatsNestedException() {
            LoggingHandler handler = new LoggingHandler();
            Exception cause = new IllegalArgumentException("inner");
            RuntimeException ex = new RuntimeException("outer", cause);
            
            String result = handler.formatData(ex);
            
            assertThat(result).isEqualTo("RuntimeException: outer");
        }
    }

    @Nested
    @DisplayName("日志输出测试")
    class LogOutputTests {

        private PrintStream originalOut;
        private PrintStream originalErr;
        private ByteArrayOutputStream outContent;
        private ByteArrayOutputStream errContent;

        @BeforeEach
        void setUp() {
            originalOut = System.out;
            originalErr = System.err;
            outContent = new ByteArrayOutputStream();
            errContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));
            System.setErr(new PrintStream(errContent));
        }

        @org.junit.jupiter.api.AfterEach
        void tearDown() {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }

        @Test
        @DisplayName("DEBUG 输出到 stdout")
        void debugLogsToStdout() {
            MockChannel channel = new MockChannel();
            channel.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
            
            channel.pipeline().fireChannelActive();
            
            String output = outContent.toString();
            assertThat(output).contains("[DEBUG]");
            assertThat(output).contains("ACTIVE");
        }

        @Test
        @DisplayName("INFO 输出到 stdout")
        void infoLogsToStdout() {
            MockChannel channel = new MockChannel();
            channel.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
            
            channel.pipeline().fireChannelActive();
            
            String output = outContent.toString();
            assertThat(output).contains("[INFO]");
        }

        @Test
        @DisplayName("WARN 输出到 stderr")
        void warnLogsToStderr() {
            MockChannel channel = new MockChannel();
            channel.pipeline().addLast(new LoggingHandler(LogLevel.WARN));
            
            channel.pipeline().fireChannelActive();
            
            String output = errContent.toString();
            assertThat(output).contains("[WARN]");
        }

        @Test
        @DisplayName("ERROR 输出到 stderr")
        void errorLogsToStderr() {
            MockChannel channel = new MockChannel();
            channel.pipeline().addLast(new LoggingHandler(LogLevel.ERROR));
            
            channel.pipeline().fireChannelActive();
            
            String output = errContent.toString();
            assertThat(output).contains("[ERROR]");
        }
    }

    @Nested
    @DisplayName("toString 测试")
    class ToStringTests {

        @Test
        @DisplayName("toString 包含名称和级别")
        void toStringContainsNameAndLevel() {
            LoggingHandler handler = new LoggingHandler("SERVER", LogLevel.INFO);
            
            String result = handler.toString();
            
            assertThat(result).contains("SERVER");
            assertThat(result).contains("INFO");
        }
    }

    @Nested
    @DisplayName("验收场景测试")
    class AcceptanceScenarioTests {

        @Test
        @DisplayName("场景: 服务端日志记录")
        void scenarioServerLogging() {
            // Given: 服务端 Pipeline 添加日志处理器
            LoggingHandler handler = new LoggingHandler("SERVER", LogLevel.DEBUG);
            
            // Then: 名称和级别正确
            assertThat(handler.name()).isEqualTo("SERVER");
            assertThat(handler.level()).isEqualTo(LogLevel.DEBUG);
        }

        @Test
        @DisplayName("场景: 客户端日志记录")
        void scenarioClientLogging() {
            // Given: 客户端 Pipeline 添加日志处理器
            LoggingHandler handler = new LoggingHandler("CLIENT", LogLevel.TRACE);
            
            // Then: 名称和级别正确
            assertThat(handler.name()).isEqualTo("CLIENT");
            assertThat(handler.level()).isEqualTo(LogLevel.TRACE);
        }

        @Test
        @DisplayName("场景: 调试网络数据")
        void scenarioDebugNetworkData() {
            // Given: 使用默认日志处理器
            LoggingHandler handler = new LoggingHandler();
            ByteBuf buf = UnpooledByteBufAllocator.DEFAULT.buffer();
            buf.writeBytes("Hello, World!".getBytes(StandardCharsets.US_ASCII));
            
            // When: 格式化 ByteBuf
            String result = handler.formatData(buf);
            
            // Then: 显示字节数和内容
            assertThat(result).contains("13B");
            assertThat(result).contains("Hello, World!");
            buf.release();
        }
    }

    // ========== 辅助类 ==========

    /**
     * 简单的 ChannelId 实现
     */
    private static class SimpleChannelId implements ChannelId {
        private static int counter = 0;
        private final String id = "test-" + (++counter);

        @Override
        public String asShortText() {
            return id;
        }

        @Override
        public String asLongText() {
            return "test-channel-" + id;
        }

        @Override
        public int compareTo(ChannelId o) {
            return asLongText().compareTo(o.asLongText());
        }
    }

    /**
     * 模拟 Channel
     */
    private static class MockChannel implements Channel {
        private final ChannelPipeline pipeline;
        private final ChannelId id = new SimpleChannelId();

        MockChannel() {
            this.pipeline = new DefaultChannelPipeline(this);
        }

        @Override
        public ChannelPipeline pipeline() {
            return pipeline;
        }

        @Override
        public ChannelConfig config() {
            return null;
        }

        @Override
        public boolean isOpen() {
            return true;
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public ChannelId id() {
            return id;
        }

        @Override
        public EventLoop eventLoop() {
            return null;
        }

        @Override
        public Channel parent() {
            return null;
        }

        @Override
        public boolean isRegistered() {
            return true;
        }

        @Override
        public ChannelFuture close() {
            return null;
        }

        @Override
        public Channel.Unsafe unsafe() {
            return null;
        }

        @Override
        public Channel read() {
            return this;
        }
    }

    /**
     * 记录事件的 Handler
     */
    private static class RecordingHandler extends ChannelInboundHandlerAdapter {
        final List<String> events = new ArrayList<>();

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) {
            events.add("channelRegistered");
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) {
            events.add("channelUnregistered");
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            events.add("channelActive");
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            events.add("channelInactive");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            events.add("channelRead:" + msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            events.add("channelReadComplete");
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            events.add("exceptionCaught:" + cause.getMessage());
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            events.add("userEventTriggered:" + evt);
        }
    }
}
