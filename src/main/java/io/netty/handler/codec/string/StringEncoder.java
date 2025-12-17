package io.netty.handler.codec.string;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 字符串编码器
 *
 * <p>将字符串编码为 ByteBuf。通常与 LengthFieldPrepender 
 * 或直接发送配合使用。
 *
 * <p>使用示例：
 * <pre>{@code
 * pipeline.addLast(new StringEncoder(StandardCharsets.UTF_8));
 * // 然后可以直接写入字符串
 * ctx.writeAndFlush("Hello, World!");
 * }</pre>
 *
 * @see StringDecoder
 * @see MessageToByteEncoder
 */
public class StringEncoder extends MessageToByteEncoder<CharSequence> {

    /**
     * 字符编码
     */
    private final Charset charset;

    /**
     * 使用 UTF-8 编码创建编码器
     */
    public StringEncoder() {
        this(StandardCharsets.UTF_8);
    }

    /**
     * 使用指定编码创建编码器
     *
     * @param charset 字符编码
     */
    public StringEncoder(Charset charset) {
        super(CharSequence.class);
        if (charset == null) {
            throw new NullPointerException("charset");
        }
        this.charset = charset;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, CharSequence msg, ByteBuf out) throws Exception {
        if (msg.length() == 0) {
            return;
        }
        byte[] bytes = msg.toString().getBytes(charset);
        out.writeBytes(bytes);
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
