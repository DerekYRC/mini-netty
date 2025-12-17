package io.netty.example.echo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Echo 客户端处理器
 *
 * <p>发送消息到服务端并接收回显的响应。
 *
 * <p>客户端工作流程：
 * <ol>
 *   <li>连接建立时发送初始消息</li>
 *   <li>接收服务端回显的消息</li>
 *   <li>可选：继续发送更多消息</li>
 * </ol>
 *
 * <p>学习要点：
 * <ul>
 *   <li>channelActive() 在连接建立后触发</li>
 *   <li>如何在客户端主动发送消息</li>
 *   <li>ByteBuf 的创建和使用</li>
 * </ul>
 */
public class EchoClientHandler extends ChannelInboundHandlerAdapter {

    private final String message;

    /**
     * 创建客户端处理器
     *
     * @param message 要发送的消息
     */
    public EchoClientHandler(String message) {
        this.message = message;
    }

    /**
     * 连接建立时发送初始消息
     *
     * @param ctx 上下文
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[EchoClient] 已连接到服务器");

        // 创建 ByteBuf 并写入消息
        ByteBuf buf = UnpooledByteBufAllocator.DEFAULT.buffer();
        buf.writeBytes(message.getBytes());

        // 发送消息
        ctx.writeAndFlush(buf);
        System.out.println("[EchoClient] 发送消息: " + message);
    }

    /**
     * 接收服务端回显的消息
     *
     * @param ctx 上下文
     * @param msg 接收到的消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            String response = new String(bytes);
            System.out.println("[EchoClient] 收到回显: " + response);
        }
    }

    /**
     * 读取完成时调用
     *
     * @param ctx 上下文
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // 可以在这里决定是否继续发送消息或关闭连接
        System.out.println("[EchoClient] 接收完成");
    }

    /**
     * 连接断开时调用
     *
     * @param ctx 上下文
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[EchoClient] 与服务器断开连接");
    }

    /**
     * 异常处理
     *
     * @param ctx   上下文
     * @param cause 异常原因
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("[EchoClient] 发生异常: " + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}
