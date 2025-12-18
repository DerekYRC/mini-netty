package io.netty.example.echo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Echo 服务端处理器
 *
 * <p>接收客户端消息并原样返回（Echo）。
 *
 * <p>这是最简单的 Netty 应用示例，演示了基本的消息处理流程：
 * <ol>
 *   <li>接收客户端数据</li>
 *   <li>处理数据（本例中直接回显）</li>
 *   <li>发送响应给客户端</li>
 * </ol>
 *
 * <p>学习要点：
 * <ul>
 *   <li>ChannelInboundHandlerAdapter 的使用</li>
 *   <li>channelRead() 处理读取事件</li>
 *   <li>ctx.write() 和 ctx.flush() 的区别</li>
 *   <li>异常处理的最佳实践</li>
 * </ul>
 */
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 读取客户端消息
     *
     * <p>将接收到的消息原样写回客户端。
     *
     * @param ctx 上下文
     * @param msg 接收到的消息（通常是 ByteBuf）
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 打印接收到的消息（用于调试）
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            System.out.println("[EchoServer] 收到消息: " + buf.readableBytes() + " 字节");
        }

        // 将消息写入缓冲区（不立即发送）
        ctx.write(msg);
    }

    /**
     * 读取完成时调用
     *
     * <p>当一批消息读取完成后，刷新缓冲区将数据发送给客户端。
     *
     * @param ctx 上下文
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // 刷新缓冲区，发送所有待发送的数据
        ctx.flush();
    }

    /**
     * 客户端连接建立时调用
     *
     * @param ctx 上下文
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[EchoServer] 客户端已连接: " + ctx.channel().id().asShortText());
        ctx.fireChannelActive();
    }

    /**
     * 客户端断开连接时调用
     *
     * @param ctx 上下文
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[EchoServer] 客户端已断开: " + ctx.channel().id().asShortText());
        ctx.fireChannelInactive();
    }

    /**
     * 异常处理
     *
     * <p>当处理过程中发生异常时，打印错误信息并关闭连接。
     *
     * @param ctx   上下文
     * @param cause 异常原因
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("[EchoServer] 发生异常: " + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}
