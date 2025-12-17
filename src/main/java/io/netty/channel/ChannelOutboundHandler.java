package io.netty.channel;

import java.net.SocketAddress;

/**
 * 出站事件处理器接口
 *
 * <p>处理向网络写入数据和连接管理操作。
 *
 * <p>出站事件流向（从应用到网络）：
 * <pre>
 * Application → write → flush → Network
 * Application → bind/connect/disconnect/close → Network
 * </pre>
 *
 * <p>典型实现示例：
 * <pre>
 * public class LoggingOutboundHandler extends ChannelOutboundHandlerAdapter {
 *     &#64;Override
 *     public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
 *         System.out.println("Writing: " + msg);
 *         ctx.write(msg, promise);
 *     }
 * }
 * </pre>
 *
 * @see ChannelHandler
 * @see ChannelInboundHandler
 */
public interface ChannelOutboundHandler extends ChannelHandler {

    /**
     * 绑定到本地地址时调用
     *
     * @param ctx          上下文
     * @param localAddress 本地地址
     * @param promise      操作结果 Promise
     * @throws Exception 如果处理过程中发生异常
     */
    void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception;

    /**
     * 连接到远程地址时调用
     *
     * @param ctx           上下文
     * @param remoteAddress 远程地址
     * @param localAddress  本地地址（可为 null）
     * @param promise       操作结果 Promise
     * @throws Exception 如果处理过程中发生异常
     */
    void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,
                 SocketAddress localAddress, ChannelPromise promise) throws Exception;

    /**
     * 断开连接时调用
     *
     * @param ctx     上下文
     * @param promise 操作结果 Promise
     * @throws Exception 如果处理过程中发生异常
     */
    void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception;

    /**
     * 关闭 Channel 时调用
     *
     * @param ctx     上下文
     * @param promise 操作结果 Promise
     * @throws Exception 如果处理过程中发生异常
     */
    void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception;

    /**
     * 写入数据时调用
     *
     * @param ctx     上下文
     * @param msg     要写入的消息
     * @param promise 操作结果 Promise
     * @throws Exception 如果处理过程中发生异常
     */
    void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception;

    /**
     * 刷新数据时调用
     *
     * @param ctx 上下文
     * @throws Exception 如果处理过程中发生异常
     */
    void flush(ChannelHandlerContext ctx) throws Exception;

    /**
     * 请求读取数据时调用
     *
     * @param ctx 上下文
     * @throws Exception 如果处理过程中发生异常
     */
    void read(ChannelHandlerContext ctx) throws Exception;
}
