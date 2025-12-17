package io.netty.handler.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.HeapByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * 消息到字节的编码器基类
 *
 * <p>将出站消息对象编码为 ByteBuf。这是编写协议编码器的基类。
 *
 * <p>使用示例：
 * <pre>{@code
 * public class IntegerEncoder extends MessageToByteEncoder<Integer> {
 *     @Override
 *     protected void encode(ChannelHandlerContext ctx, Integer msg, ByteBuf out) {
 *         out.writeInt(msg);
 *     }
 * }
 * }</pre>
 *
 * @param <I> 输入消息类型
 * @see ChannelOutboundHandlerAdapter
 */
public abstract class MessageToByteEncoder<I> extends ChannelOutboundHandlerAdapter {

    /**
     * 支持的消息类型
     */
    private final Class<? extends I> outboundMessageType;

    /**
     * 默认初始容量
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 256;

    /**
     * 默认最大容量
     */
    private static final int DEFAULT_MAX_CAPACITY = 65536;

    /**
     * 使用 Object 作为默认匹配类型
     */
    @SuppressWarnings("unchecked")
    protected MessageToByteEncoder() {
        this.outboundMessageType = (Class<? extends I>) Object.class;
    }

    /**
     * 指定支持的消息类型
     *
     * @param outboundMessageType 支持的消息类型
     */
    protected MessageToByteEncoder(Class<? extends I> outboundMessageType) {
        this.outboundMessageType = outboundMessageType;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ByteBuf buf = null;
        try {
            if (acceptOutboundMessage(msg)) {
                @SuppressWarnings("unchecked")
                I cast = (I) msg;
                buf = allocateBuffer(ctx);
                try {
                    encode(ctx, cast, buf);
                } finally {
                    // 如果原始消息是 ByteBuf，需要释放
                    if (msg instanceof ByteBuf) {
                        ((ByteBuf) msg).release();
                    }
                }
                if (buf.isReadable()) {
                    ctx.write(buf, promise);
                } else {
                    buf.release();
                    ctx.write(new HeapByteBuf(0, 0), promise);
                }
                buf = null;
            } else {
                ctx.write(msg, promise);
            }
        } catch (EncoderException e) {
            throw e;
        } catch (Throwable e) {
            throw new EncoderException(e);
        } finally {
            if (buf != null) {
                buf.release();
            }
        }
    }

    /**
     * 检查消息是否可以被编码
     *
     * @param msg 消息
     * @return 如果可以编码返回 true
     */
    protected boolean acceptOutboundMessage(Object msg) {
        return outboundMessageType.isInstance(msg);
    }

    /**
     * 分配输出缓冲区
     *
     * @param ctx 上下文
     * @return 新的 ByteBuf
     */
    protected ByteBuf allocateBuffer(ChannelHandlerContext ctx) {
        return new HeapByteBuf(DEFAULT_INITIAL_CAPACITY, DEFAULT_MAX_CAPACITY);
    }

    /**
     * 编码消息到 ByteBuf
     *
     * @param ctx 上下文
     * @param msg 要编码的消息
     * @param out 输出缓冲区
     * @throws Exception 编码异常
     */
    protected abstract void encode(ChannelHandlerContext ctx, I msg, ByteBuf out) throws Exception;
}
