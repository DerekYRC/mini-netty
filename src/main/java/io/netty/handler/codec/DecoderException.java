package io.netty.handler.codec;

/**
 * 解码器异常
 *
 * <p>在解码过程中发生的异常。
 */
public class DecoderException extends RuntimeException {

    public DecoderException() {
        super();
    }

    public DecoderException(String message) {
        super(message);
    }

    public DecoderException(String message, Throwable cause) {
        super(message, cause);
    }

    public DecoderException(Throwable cause) {
        super(cause);
    }
}
