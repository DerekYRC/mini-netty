package io.netty.handler.codec;

/**
 * 编码器异常
 *
 * <p>在消息编码过程中发生错误时抛出此异常。
 *
 * @see MessageToByteEncoder
 */
public class EncoderException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 创建一个新的 EncoderException
     */
    public EncoderException() {
    }

    /**
     * 创建一个带消息的 EncoderException
     *
     * @param message 错误消息
     */
    public EncoderException(String message) {
        super(message);
    }

    /**
     * 创建一个带消息和原因的 EncoderException
     *
     * @param message 错误消息
     * @param cause   原因
     */
    public EncoderException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 创建一个带原因的 EncoderException
     *
     * @param cause 原因
     */
    public EncoderException(Throwable cause) {
        super(cause);
    }
}
