package io.netty.handler.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.HeapByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * 定长帧解码器
 *
 * <p>将接收到的字节按固定长度切分成帧。
 * 这是最简单的帧解码策略之一。
 *
 * <p>使用场景：
 * <ul>
 *   <li>消息长度固定的协议</li>
 *   <li>二进制传感器数据（每条数据固定大小）</li>
 *   <li>定长记录的批处理</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * pipeline.addLast(new FixedLengthFrameDecoder(8)); // 每帧 8 字节
 * }</pre>
 *
 * @see ByteToMessageDecoder
 */
public class FixedLengthFrameDecoder extends ByteToMessageDecoder {

    private final int frameLength;

    /**
     * 创建定长帧解码器
     *
     * @param frameLength 每帧的固定长度（必须大于 0）
     */
    public FixedLengthFrameDecoder(int frameLength) {
        if (frameLength <= 0) {
            throw new IllegalArgumentException(
                    "frameLength must be a positive integer: " + frameLength);
        }
        this.frameLength = frameLength;
    }

    /**
     * 获取帧长度
     *
     * @return 帧长度
     */
    public int getFrameLength() {
        return frameLength;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        while (in.readableBytes() >= frameLength) {
            // 创建新的 ByteBuf 存放帧数据
            ByteBuf frame = new HeapByteBuf(frameLength, frameLength);
            byte[] data = new byte[frameLength];
            in.readBytes(data);
            frame.writeBytes(data);
            out.add(frame);
        }
    }
}
