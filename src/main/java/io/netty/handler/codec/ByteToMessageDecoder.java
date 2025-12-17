package io.netty.handler.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.HeapByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 字节到消息解码器
 *
 * <p>将接收到的字节累积到缓冲区，然后解码成消息对象。
 * 这是解决 TCP 粘包/拆包问题的核心类。
 *
 * <p>粘包问题：多条消息被合并成一个 TCP 包发送
 * <p>拆包问题：一条消息被拆分成多个 TCP 包发送
 *
 * <p>使用示例：
 * <pre>{@code
 * public class IntegerDecoder extends ByteToMessageDecoder {
 *     @Override
 *     protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
 *         if (in.readableBytes() >= 4) {
 *             out.add(in.readInt());
 *         }
 *     }
 * }
 * }</pre>
 *
 * <p>decode 方法的实现原则：
 * <ul>
 *   <li>检查可读字节是否足够</li>
 *   <li>如果足够，读取并添加到 out</li>
 *   <li>如果不够，直接返回等待更多数据</li>
 * </ul>
 *
 * @see ChannelInboundHandlerAdapter
 */
public abstract class ByteToMessageDecoder extends ChannelInboundHandlerAdapter {

    /**
     * 累积缓冲区
     */
    private ByteBuf cumulation;

    /**
     * 默认初始容量
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 256;

    /**
     * 默认最大容量
     */
    private static final int DEFAULT_MAX_CAPACITY = 65536;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf data = (ByteBuf) msg;
            boolean wasNull = cumulation == null;
            
            if (wasNull) {
                cumulation = data;
            } else {
                cumulation = cumulate(cumulation, data);
            }
            
            try {
                callDecode(ctx, cumulation);
            } finally {
                if (cumulation != null && !cumulation.isReadable()) {
                    cumulation.release();
                    cumulation = null;
                }
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    /**
     * 累积数据到缓冲区
     *
     * @param cumulation 现有累积缓冲区
     * @param in         新到达的数据
     * @return 累积后的缓冲区
     */
    protected ByteBuf cumulate(ByteBuf cumulation, ByteBuf in) {
        try {
            int required = in.readableBytes();
            if (required > cumulation.writableBytes()) {
                // 需要扩容或创建新 buffer
                ByteBuf newCumulation = new HeapByteBuf(
                        cumulation.readableBytes() + required,
                        DEFAULT_MAX_CAPACITY);
                
                // 复制现有数据
                byte[] existing = new byte[cumulation.readableBytes()];
                cumulation.readBytes(existing);
                newCumulation.writeBytes(existing);
                
                cumulation.release();
                cumulation = newCumulation;
            }
            
            // 写入新数据
            byte[] newData = new byte[in.readableBytes()];
            in.readBytes(newData);
            cumulation.writeBytes(newData);
            
            return cumulation;
        } finally {
            in.release();
        }
    }

    /**
     * 调用解码方法
     *
     * @param ctx        上下文
     * @param cumulation 累积缓冲区
     */
    private void callDecode(ChannelHandlerContext ctx, ByteBuf cumulation) throws Exception {
        List<Object> out = new ArrayList<>();
        
        while (cumulation.isReadable()) {
            int oldReaderIndex = cumulation.readerIndex();
            
            decode(ctx, cumulation, out);
            
            if (out.isEmpty()) {
                // 没有解码出消息，等待更多数据
                if (oldReaderIndex == cumulation.readerIndex()) {
                    break;
                }
            } else {
                // 已解码出消息
                if (oldReaderIndex == cumulation.readerIndex()) {
                    throw new DecoderException(
                            getClass() + ".decode() did not read anything but decoded a message.");
                }
                
                // 将解码的消息传递给下一个 handler
                for (Object decoded : out) {
                    ctx.fireChannelRead(decoded);
                }
                out.clear();
            }
        }
    }

    /**
     * 解码方法 - 子类必须实现
     *
     * <p>从输入缓冲区读取数据并解码成消息，添加到 out 列表。
     * 如果数据不足以解码一条完整消息，应该直接返回不添加任何内容。
     *
     * @param ctx 上下文
     * @param in  输入缓冲区
     * @param out 解码后的消息列表
     * @throws Exception 如果解码过程中发生异常
     */
    protected abstract void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (cumulation != null) {
            cumulation.release();
            cumulation = null;
        }
        ctx.fireChannelInactive();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        if (cumulation != null) {
            cumulation.release();
            cumulation = null;
        }
    }

    /**
     * 获取累积缓冲区（测试用）
     */
    protected ByteBuf internalBuffer() {
        return cumulation;
    }
}
