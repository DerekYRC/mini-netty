package io.netty.handler.codec.string;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 字符串解码器
 *
 * <p>将 ByteBuf 解码为字符串。通常与 LengthFieldBasedFrameDecoder 
 * 或其他帧解码器配合使用。
 *
 * <p>使用示例：
 * <pre>{@code
 * pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 0, 2, 0, 2));
 * pipeline.addLast(new StringDecoder(StandardCharsets.UTF_8));
 * pipeline.addLast(new YourHandler());
 * }</pre>
 *
 * @see io.netty.handler.codec.LengthFieldBasedFrameDecoder
 */
public class StringDecoder extends ChannelInboundHandlerAdapter {

    /**
     * 字符编码
     */
    private final Charset charset;

    /**
     * 使用 UTF-8 编码创建解码器
     */
    public StringDecoder() {
        this(StandardCharsets.UTF_8);
    }

    /**
     * 使用指定编码创建解码器
     *
     * @param charset 字符编码
     */
    public StringDecoder(Charset charset) {
        if (charset == null) {
            throw new NullPointerException("charset");
        }
        this.charset = charset;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            try {
                String decoded = buf.toString(charset);
                ctx.fireChannelRead(decoded);
            } finally {
                buf.release();
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    /**
     * 获取使用的字符编码
     *
     * @return 字符编码
     */
    public Charset getCharset() {
        return charset;
    }
}
