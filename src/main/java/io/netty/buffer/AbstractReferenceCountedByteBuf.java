package io.netty.buffer;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * 带原子引用计数的 ByteBuf 抽象基类
 *
 * <p>使用原子操作实现线程安全的引用计数管理。
 * 这是 Netty 引用计数机制的核心实现。
 *
 * <p>引用计数规则：
 * <ul>
 *   <li>创建时引用计数为 1</li>
 *   <li>每次 retain() 增加 1（或指定增量）</li>
 *   <li>每次 release() 减少 1（或指定减量）</li>
 *   <li>引用计数变为 0 时调用 deallocate() 释放资源</li>
 * </ul>
 *
 * <p>线程安全：使用 AtomicIntegerFieldUpdater 保证并发安全。
 */
public abstract class AbstractReferenceCountedByteBuf extends AbstractByteBuf {

    private static final AtomicIntegerFieldUpdater<AbstractReferenceCountedByteBuf> REF_CNT_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(AbstractReferenceCountedByteBuf.class, "refCnt");

    @SuppressWarnings("FieldMayBeFinal")
    private volatile int refCnt = 1;

    protected AbstractReferenceCountedByteBuf(int maxCapacity) {
        super(maxCapacity);
    }

    @Override
    public int refCnt() {
        return refCnt;
    }

    /**
     * 设置引用计数（仅供内部使用）
     *
     * @param newRefCnt 新的引用计数值
     */
    protected void setRefCnt(int newRefCnt) {
        REF_CNT_UPDATER.set(this, newRefCnt);
    }

    @Override
    public ByteBuf retain() {
        return retain(1);
    }

    @Override
    public ByteBuf retain(int increment) {
        if (increment <= 0) {
            throw new IllegalArgumentException("increment: " + increment + " (expected: > 0)");
        }
        
        int oldRef = refCnt;
        int nextRef = oldRef + increment;
        
        // 检查是否已释放或溢出
        if (oldRef <= 0 || nextRef < oldRef) {
            throw new IllegalReferenceCountException(oldRef, increment);
        }
        
        // CAS 操作
        while (!REF_CNT_UPDATER.compareAndSet(this, oldRef, nextRef)) {
            oldRef = refCnt;
            nextRef = oldRef + increment;
            if (oldRef <= 0 || nextRef < oldRef) {
                throw new IllegalReferenceCountException(oldRef, increment);
            }
        }
        
        return this;
    }

    @Override
    public boolean release() {
        return release(1);
    }

    @Override
    public boolean release(int decrement) {
        if (decrement <= 0) {
            throw new IllegalArgumentException("decrement: " + decrement + " (expected: > 0)");
        }
        
        int oldRef = refCnt;
        
        if (oldRef < decrement) {
            throw new IllegalReferenceCountException(oldRef, -decrement);
        }
        
        // CAS 操作
        while (!REF_CNT_UPDATER.compareAndSet(this, oldRef, oldRef - decrement)) {
            oldRef = refCnt;
            if (oldRef < decrement) {
                throw new IllegalReferenceCountException(oldRef, -decrement);
            }
        }
        
        if (oldRef == decrement) {
            deallocate();
            return true;
        }
        return false;
    }

    /**
     * 释放资源的模板方法
     *
     * <p>当引用计数变为 0 时调用，子类需要实现具体的资源释放逻辑。
     */
    protected abstract void deallocate();

    /**
     * 非法引用计数异常
     */
    public static class IllegalReferenceCountException extends IllegalStateException {
        
        public IllegalReferenceCountException(int refCnt, int increment) {
            super("refCnt: " + refCnt + ", " + (increment > 0 ? "increment: " : "decrement: ") + Math.abs(increment));
        }
    }
}
