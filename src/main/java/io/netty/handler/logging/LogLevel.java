package io.netty.handler.logging;

/**
 * 日志级别枚举
 *
 * <p>定义 LoggingHandler 支持的日志级别。
 */
public enum LogLevel {
    /**
     * TRACE 级别 - 最详细的日志
     */
    TRACE,

    /**
     * DEBUG 级别 - 调试信息
     */
    DEBUG,

    /**
     * INFO 级别 - 一般信息
     */
    INFO,

    /**
     * WARN 级别 - 警告信息
     */
    WARN,

    /**
     * ERROR 级别 - 错误信息
     */
    ERROR
}
