package io.netty.handler.logging;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * 日志处理器
 *
 * <p>记录所有入站和出站事件的日志信息，用于调试和监控。
 *
 * <p>LoggingHandler 是一个双向处理器，可以同时记录：
 * <ul>
 *   <li>入站事件：channelRegistered, channelActive, channelRead 等</li>
 *   <li>出站操作：write, flush, close 等</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * // 添加到 Pipeline 的开头，记录所有事件
 * pipeline.addFirst("logger", new LoggingHandler(LogLevel.DEBUG));
 *
 * // 使用默认 INFO 级别
 * pipeline.addFirst("logger", new LoggingHandler());
 *
 * // 使用自定义名称
 * pipeline.addFirst(new LoggingHandler("SERVER"));
 * }</pre>
 *
 * <p><b>学习要点</b>:
 * <ul>
 *   <li>ChannelDuplexHandler 同时处理入站和出站</li>
 *   <li>日志格式化和 ByteBuf 内容的安全输出</li>
 *   <li>调试网络应用的常用技巧</li>
 * </ul>
 *
 * @see LogLevel
 * @see ChannelDuplexHandler
 */
public class LoggingHandler extends ChannelDuplexHandler {

    /**
     * 默认日志级别
     */
    private static final LogLevel DEFAULT_LEVEL = LogLevel.DEBUG;

    /**
     * 日志输出名称
     */
    private final String name;

    /**
     * 日志级别
     */
    private final LogLevel level;

    /**
     * 使用默认级别 (DEBUG) 和类名作为名称
     */
    public LoggingHandler() {
        this(DEFAULT_LEVEL);
    }

    /**
     * 使用指定级别和类名作为名称
     *
     * @param level 日志级别
     */
    public LoggingHandler(LogLevel level) {
        this(LoggingHandler.class.getSimpleName(), level);
    }

    /**
     * 使用指定名称和默认级别
     *
     * @param name 日志名称
     */
    public LoggingHandler(String name) {
        this(name, DEFAULT_LEVEL);
    }

    /**
     * 使用指定名称和级别
     *
     * @param name  日志名称
     * @param level 日志级别
     */
    public LoggingHandler(String name, LogLevel level) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (level == null) {
            throw new NullPointerException("level");
        }
        this.name = name;
        this.level = level;
    }

    /**
     * 返回日志级别
     *
     * @return 日志级别
     */
    public LogLevel level() {
        return level;
    }

    /**
     * 返回日志名称
     *
     * @return 日志名称
     */
    public String name() {
        return name;
    }

    // ========== 入站事件日志 ==========

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log(ctx, "REGISTERED");
        ctx.fireChannelRegistered();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log(ctx, "UNREGISTERED");
        ctx.fireChannelUnregistered();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log(ctx, "ACTIVE");
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log(ctx, "INACTIVE");
        ctx.fireChannelInactive();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log(ctx, "READ", msg);
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log(ctx, "READ_COMPLETE");
        ctx.fireChannelReadComplete();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log(ctx, "EXCEPTION", cause);
        ctx.fireExceptionCaught(cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        log(ctx, "USER_EVENT", evt);
        ctx.fireUserEventTriggered(evt);
    }

    // ========== 出站操作日志 ==========

    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress,
                     ChannelPromise promise) throws Exception {
        log(ctx, "BIND", localAddress);
        super.bind(ctx, localAddress, promise);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,
                        SocketAddress localAddress, ChannelPromise promise) throws Exception {
        log(ctx, "CONNECT", remoteAddress, localAddress);
        super.connect(ctx, remoteAddress, localAddress, promise);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        log(ctx, "DISCONNECT");
        super.disconnect(ctx, promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        log(ctx, "CLOSE");
        super.close(ctx, promise);
    }

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        log(ctx, "READ_REQUEST");
        super.read(ctx);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        log(ctx, "WRITE", msg);
        super.write(ctx, msg, promise);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        log(ctx, "FLUSH");
        super.flush(ctx);
    }

    // ========== 日志格式化 ==========

    /**
     * 格式化日志消息（无数据）
     *
     * @param ctx   上下文
     * @param event 事件名称
     */
    protected void log(ChannelHandlerContext ctx, String event) {
        log(ctx, event, null);
    }

    /**
     * 格式化日志消息（带数据）
     *
     * @param ctx   上下文
     * @param event 事件名称
     * @param data  数据对象
     */
    protected void log(ChannelHandlerContext ctx, String event, Object data) {
        String channelId = ctx.channel().id().asShortText();
        String message = formatMessage(event, channelId, data);
        doLog(message);
    }

    /**
     * 格式化日志消息（连接事件，有远程和本地地址）
     *
     * @param ctx          上下文
     * @param event        事件名称
     * @param remoteAddress 远程地址
     * @param localAddress  本地地址
     */
    protected void log(ChannelHandlerContext ctx, String event, 
                       SocketAddress remoteAddress, SocketAddress localAddress) {
        String channelId = ctx.channel().id().asShortText();
        String data = remoteAddress + (localAddress != null ? " -> " + localAddress : "");
        String message = formatMessage(event, channelId, data);
        doLog(message);
    }

    /**
     * 格式化消息字符串
     *
     * @param event     事件名称
     * @param channelId Channel ID
     * @param data      数据
     * @return 格式化后的字符串
     */
    private String formatMessage(String event, String channelId, Object data) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(name).append("]");
        sb.append(" [").append(channelId).append("]");
        sb.append(" ").append(event);
        
        if (data != null) {
            sb.append(": ");
            sb.append(formatData(data));
        }
        
        return sb.toString();
    }

    /**
     * 格式化数据对象
     *
     * @param data 数据对象
     * @return 格式化后的字符串
     */
    protected String formatData(Object data) {
        if (data instanceof ByteBuf) {
            return formatByteBuf((ByteBuf) data);
        } else if (data instanceof Throwable) {
            Throwable t = (Throwable) data;
            return t.getClass().getSimpleName() + ": " + t.getMessage();
        } else {
            return String.valueOf(data);
        }
    }

    /**
     * 格式化 ByteBuf 内容
     *
     * <p>以十六进制和可打印字符形式显示内容。
     *
     * @param buf ByteBuf
     * @return 格式化后的字符串
     */
    protected String formatByteBuf(ByteBuf buf) {
        int length = buf.readableBytes();
        if (length == 0) {
            return "ByteBuf(0B)";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("ByteBuf(").append(length).append("B");

        // 读取可读字节（不改变 readerIndex）
        int maxShow = Math.min(length, 64); // 最多显示 64 字节
        byte[] bytes = new byte[maxShow];
        buf.getBytes(buf.readerIndex(), bytes);

        // 显示十六进制
        sb.append(", hex=");
        for (int i = 0; i < maxShow; i++) {
            sb.append(String.format("%02x", bytes[i] & 0xFF));
            if (i < maxShow - 1) {
                sb.append(" ");
            }
        }
        if (length > maxShow) {
            sb.append("...");
        }

        // 尝试显示为字符串（如果是可打印字符）
        if (isPrintableAscii(bytes)) {
            sb.append(", str=\"");
            sb.append(new String(bytes, StandardCharsets.US_ASCII));
            if (length > maxShow) {
                sb.append("...");
            }
            sb.append("\"");
        }

        sb.append(")");
        return sb.toString();
    }

    /**
     * 判断字节数组是否是可打印 ASCII
     *
     * @param bytes 字节数组
     * @return 是否可打印
     */
    private boolean isPrintableAscii(byte[] bytes) {
        for (byte b : bytes) {
            if (b < 0x20 || b > 0x7E) {
                return false;
            }
        }
        return bytes.length > 0;
    }

    /**
     * 实际输出日志
     *
     * <p>子类可以覆盖此方法以使用自定义日志框架。
     *
     * @param message 日志消息
     */
    protected void doLog(String message) {
        switch (level) {
            case TRACE:
            case DEBUG:
            case INFO:
                System.out.println("[" + level + "] " + message);
                break;
            case WARN:
            case ERROR:
                System.err.println("[" + level + "] " + message);
                break;
        }
    }

    @Override
    public String toString() {
        return "LoggingHandler{name='" + name + "', level=" + level + "}";
    }
}
