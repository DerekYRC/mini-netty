package io.netty.channel;

import java.util.concurrent.TimeUnit;

/**
 * ChannelPromise 的默认实现
 *
 * <p>Promise 是可写的 Future，允许设置操作结果。
 * 用于出站操作（如 write、close）的结果通知。
 *
 * @see ChannelPromise
 * @see DefaultChannelFuture
 */
public class DefaultChannelPromise extends DefaultChannelFuture implements ChannelPromise {

    /**
     * 构造函数
     *
     * @param channel 关联的 Channel
     */
    public DefaultChannelPromise(Channel channel) {
        super(channel);
    }

    @Override
    public ChannelPromise setSuccess() {
        super.markSuccess();
        return this;
    }

    @Override
    public boolean trySuccess() {
        return super.tryMarkSuccess();
    }

    @Override
    public ChannelPromise setFailure(Throwable cause) {
        super.markFailure(cause);
        return this;
    }

    @Override
    public boolean tryFailure(Throwable cause) {
        return super.tryMarkFailure(cause);
    }

    @Override
    public ChannelPromise sync() throws InterruptedException {
        super.sync();
        return this;
    }

    @Override
    public ChannelPromise addListener(ChannelFutureListener listener) {
        super.addListener(listener);
        return this;
    }
}
