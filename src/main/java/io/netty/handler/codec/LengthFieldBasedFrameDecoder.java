package io.netty.handler.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.HeapByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * 基于长度字段的帧解码器
 *
 * <p>这是最通用的解决 TCP 粘包/拆包问题的解码器。
 * 消息格式：[长度字段][数据]，长度字段指示后续数据的字节数。
 *
 * <p>参数说明：
 * <ul>
 *   <li>lengthFieldOffset - 长度字段偏移量（跳过消息头）</li>
 *   <li>lengthFieldLength - 长度字段本身的字节数（1, 2, 3, 4, 8）</li>
 *   <li>lengthAdjustment - 长度值调整量（长度不含头部时需要调整）</li>
 *   <li>initialBytesToStrip - 解码后跳过的字节数（去掉头部）</li>
 * </ul>
 *
 * <p>示例1：长度字段表示整个消息长度
 * <pre>
 * 消息格式: [2字节长度=10][8字节数据]
 * lengthFieldOffset = 0
 * lengthFieldLength = 2
 * lengthAdjustment = -2 (长度包含自身，需要减去)
 * initialBytesToStrip = 2 (去掉长度字段)
 * </pre>
 *
 * <p>示例2：长度字段表示数据长度
 * <pre>
 * 消息格式: [2字节长度=8][8字节数据]
 * lengthFieldOffset = 0
 * lengthFieldLength = 2
 * lengthAdjustment = 0
 * initialBytesToStrip = 2
 * </pre>
 *
 * <p>示例3：带消息头
 * <pre>
 * 消息格式: [2字节魔数][2字节长度=8][8字节数据]
 * lengthFieldOffset = 2 (跳过魔数)
 * lengthFieldLength = 2
 * lengthAdjustment = 0
 * initialBytesToStrip = 4 (去掉魔数和长度)
 * </pre>
 *
 * @see ByteToMessageDecoder
 */
public class LengthFieldBasedFrameDecoder extends ByteToMessageDecoder {

    /**
     * 长度字段偏移量
     */
    private final int lengthFieldOffset;

    /**
     * 长度字段字节数
     */
    private final int lengthFieldLength;

    /**
     * 长度值调整量
     */
    private final int lengthAdjustment;

    /**
     * 解码后跳过的字节数
     */
    private final int initialBytesToStrip;

    /**
     * 最大帧长度
     */
    private final int maxFrameLength;

    /**
     * 长度字段结束位置偏移量 = lengthFieldOffset + lengthFieldLength
     */
    private final int lengthFieldEndOffset;

    /**
     * 创建 LengthFieldBasedFrameDecoder
     *
     * @param maxFrameLength       最大帧长度（防止内存溢出）
     * @param lengthFieldOffset    长度字段偏移量
     * @param lengthFieldLength    长度字段字节数（1, 2, 3, 4, 8）
     * @param lengthAdjustment     长度值调整量
     * @param initialBytesToStrip  解码后跳过的字节数
     */
    public LengthFieldBasedFrameDecoder(int maxFrameLength, int lengthFieldOffset,
                                        int lengthFieldLength, int lengthAdjustment,
                                        int initialBytesToStrip) {
        if (maxFrameLength <= 0) {
            throw new IllegalArgumentException("maxFrameLength must be positive: " + maxFrameLength);
        }
        if (lengthFieldOffset < 0) {
            throw new IllegalArgumentException("lengthFieldOffset must be non-negative: " + lengthFieldOffset);
        }
        if (lengthFieldLength != 1 && lengthFieldLength != 2 &&
            lengthFieldLength != 3 && lengthFieldLength != 4 &&
            lengthFieldLength != 8) {
            throw new IllegalArgumentException("lengthFieldLength must be 1, 2, 3, 4, or 8: " + lengthFieldLength);
        }
        if (initialBytesToStrip < 0) {
            throw new IllegalArgumentException("initialBytesToStrip must be non-negative: " + initialBytesToStrip);
        }

        this.maxFrameLength = maxFrameLength;
        this.lengthFieldOffset = lengthFieldOffset;
        this.lengthFieldLength = lengthFieldLength;
        this.lengthAdjustment = lengthAdjustment;
        this.initialBytesToStrip = initialBytesToStrip;
        this.lengthFieldEndOffset = lengthFieldOffset + lengthFieldLength;
    }

    /**
     * 简化构造函数：使用默认最大帧长度 1MB
     */
    public LengthFieldBasedFrameDecoder(int lengthFieldOffset, int lengthFieldLength,
                                        int lengthAdjustment, int initialBytesToStrip) {
        this(1024 * 1024, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        Object decoded = decodeFrame(ctx, in);
        if (decoded != null) {
            out.add(decoded);
        }
    }

    /**
     * 解码单个帧
     *
     * @param ctx 上下文
     * @param in  输入缓冲区
     * @return 解码的帧，数据不足返回 null
     * @throws Exception 解码异常
     */
    protected Object decodeFrame(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        // 检查是否有足够的字节读取长度字段
        if (in.readableBytes() < lengthFieldEndOffset) {
            return null;
        }

        // 读取长度值（不移动读指针）
        int actualLengthFieldOffset = in.readerIndex() + lengthFieldOffset;
        long frameLength = getUnadjustedFrameLength(in, actualLengthFieldOffset, lengthFieldLength);

        // 应用长度调整
        frameLength += lengthAdjustment + lengthFieldEndOffset;

        // 检查帧长度是否有效
        if (frameLength < lengthFieldEndOffset) {
            throw new DecoderException("Adjusted frame length (" + frameLength + ") is less than lengthFieldEndOffset: " + lengthFieldEndOffset);
        }
        if (frameLength > maxFrameLength) {
            throw new DecoderException("Frame length exceeds maximum: " + frameLength + " > " + maxFrameLength);
        }

        int frameLengthInt = (int) frameLength;

        // 检查是否有足够的数据
        if (in.readableBytes() < frameLengthInt) {
            return null;
        }

        // 检查跳过的字节是否超过帧长度
        if (initialBytesToStrip > frameLengthInt) {
            throw new DecoderException("initialBytesToStrip (" + initialBytesToStrip + 
                    ") exceeds frame length: " + frameLengthInt);
        }

        // 跳过头部
        in.skipBytes(initialBytesToStrip);

        // 读取帧数据
        int actualFrameLength = frameLengthInt - initialBytesToStrip;
        ByteBuf frame = extractFrame(in, actualFrameLength);

        return frame;
    }

    /**
     * 读取无符号长度值
     */
    private long getUnadjustedFrameLength(ByteBuf buf, int offset, int length) {
        // 保存当前读指针
        int savedReaderIndex = buf.readerIndex();
        buf.readerIndex(offset);

        long frameLength;
        switch (length) {
            case 1:
                frameLength = buf.readByte() & 0xFF;
                break;
            case 2:
                frameLength = buf.readShort() & 0xFFFF;
                break;
            case 3:
                frameLength = (buf.readByte() & 0xFF) << 16 |
                             (buf.readByte() & 0xFF) << 8 |
                             (buf.readByte() & 0xFF);
                break;
            case 4:
                frameLength = buf.readInt() & 0xFFFFFFFFL;
                break;
            case 8:
                frameLength = buf.readLong();
                break;
            default:
                throw new DecoderException("Unsupported length field length: " + length);
        }

        // 恢复读指针
        buf.readerIndex(savedReaderIndex);
        return frameLength;
    }

    /**
     * 提取帧数据
     */
    private ByteBuf extractFrame(ByteBuf buf, int length) {
        ByteBuf frame = new HeapByteBuf(length, length);
        byte[] data = new byte[length];
        buf.readBytes(data);
        frame.writeBytes(data);
        return frame;
    }

    // Getter 方法

    public int getLengthFieldOffset() {
        return lengthFieldOffset;
    }

    public int getLengthFieldLength() {
        return lengthFieldLength;
    }

    public int getLengthAdjustment() {
        return lengthAdjustment;
    }

    public int getInitialBytesToStrip() {
        return initialBytesToStrip;
    }

    public int getMaxFrameLength() {
        return maxFrameLength;
    }
}
