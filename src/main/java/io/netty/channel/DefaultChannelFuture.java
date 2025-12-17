package io.netty.channel;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * ChannelFuture 的默认实现
 *
 * <p>表示异步 Channel 操作的结果。简化实现，支持：
 * <ul>
 *   <li>同步等待操作完成</li>
 *   <li>检查操作是否成功</li>
 *   <li>获取失败原因</li>
 * </ul>
 */
public class DefaultChannelFuture implements ChannelFuture {

    private final Channel channel;
    private volatile boolean success;
    private volatile Throwable cause;
    private final CountDownLatch latch = new CountDownLatch(1);

    /**
     * 创建已完成的 Future
     *
     * @param channel 关联的 Channel
     * @param success 是否成功
     */
    public DefaultChannelFuture(Channel channel, boolean success) {
        this.channel = channel;
        this.success = success;
        if (success) {
            latch.countDown();
        }
    }

    /**
     * 创建已失败的 Future
     *
     * @param channel 关联的 Channel
     * @param cause   失败原因
     */
    public DefaultChannelFuture(Channel channel, Throwable cause) {
        this.channel = channel;
        this.success = false;
        this.cause = cause;
        latch.countDown();
    }

    /**
     * 创建未完成的 Future
     *
     * @param channel 关联的 Channel
     */
    public DefaultChannelFuture(Channel channel) {
        this.channel = channel;
        this.success = false;
    }

    @Override
    public Channel channel() {
        return channel;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public Throwable cause() {
        return cause;
    }

    @Override
    public ChannelFuture sync() throws InterruptedException {
        latch.await();
        return this;
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return latch.await(timeout, unit);
    }

    @Override
    public boolean isDone() {
        return latch.getCount() == 0;
    }

    @Override
    public ChannelFuture addListener(ChannelFutureListener listener) {
        if (isDone()) {
            notifyListener(listener);
        }
        // 简化实现：不支持异步监听器通知
        return this;
    }

    /**
     * 设置操作成功（内部方法）
     */
    protected void markSuccess() {
        this.success = true;
        latch.countDown();
    }

    /**
     * 尝试设置操作成功
     *
     * @return 如果成功设置返回 true
     */
    protected boolean tryMarkSuccess() {
        if (latch.getCount() > 0) {
            this.success = true;
            latch.countDown();
            return true;
        }
        return false;
    }

    /**
     * 设置操作失败（内部方法）
     *
     * @param cause 失败原因
     */
    protected void markFailure(Throwable cause) {
        this.success = false;
        this.cause = cause;
        latch.countDown();
    }

    /**
     * 尝试设置操作失败
     *
     * @param cause 失败原因
     * @return 如果成功设置返回 true
     */
    protected boolean tryMarkFailure(Throwable cause) {
        if (latch.getCount() > 0) {
            this.success = false;
            this.cause = cause;
            latch.countDown();
            return true;
        }
        return false;
    }

    private void notifyListener(ChannelFutureListener listener) {
        try {
            listener.operationComplete(this);
        } catch (Exception e) {
            System.err.println("[DefaultChannelFuture] 监听器通知失败: " + e.getMessage());
        }
    }
}
