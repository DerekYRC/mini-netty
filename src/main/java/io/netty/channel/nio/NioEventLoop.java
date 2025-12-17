package io.netty.channel.nio;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SingleThreadEventLoop;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;

/**
 * 基于 NIO Selector 的事件循环实现
 *
 * <p>NioEventLoop 是 Netty NIO 传输的核心组件，它：
 * <ul>
 *   <li>使用 Selector 监控多个 Channel 的 I/O 事件</li>
 *   <li>在单线程中处理所有注册 Channel 的事件</li>
 *   <li>支持执行普通任务和定时任务</li>
 * </ul>
 *
 * <p>事件循环流程：
 * <pre>
 * while (running) {
 *     1. select() - 等待 I/O 事件
 *     2. processSelectedKeys() - 处理就绪的 I/O 事件
 *     3. runAllTasks() - 执行队列中的任务
 * }
 * </pre>
 *
 * <p>学习要点：
 * <ul>
 *   <li>Selector.select() 可能阻塞，需要 wakeup() 唤醒</li>
 *   <li>处理完 SelectionKey 后必须从 selectedKeys 中移除</li>
 *   <li>I/O 事件处理和任务执行在同一线程，避免同步开销</li>
 * </ul>
 *
 * @see SingleThreadEventLoop
 * @see Selector
 */
public class NioEventLoop extends SingleThreadEventLoop {

    /**
     * NIO Selector
     */
    private final Selector selector;

    /**
     * 构造函数
     *
     * @param parent 父 EventLoopGroup
     */
    public NioEventLoop(EventLoopGroup parent) {
        super(parent);
        try {
            this.selector = SelectorProvider.provider().openSelector();
        } catch (IOException e) {
            throw new RuntimeException("无法创建 Selector", e);
        }
    }

    /**
     * 获取 Selector
     *
     * @return NIO Selector
     */
    public Selector selector() {
        return selector;
    }

    @Override
    public ChannelFuture register(Channel channel) {
        // TODO: 在后续迭代中实现 Channel 注册
        throw new UnsupportedOperationException("将在后续迭代实现 Channel 注册");
    }

    @Override
    protected void run() {
        System.out.println("[NioEventLoop] 事件循环启动");
        
        while (!isShutdown()) {
            try {
                // 1. 选择就绪的 Channel
                int readyChannels = select();

                // 2. 处理就绪的 I/O 事件
                if (readyChannels > 0) {
                    processSelectedKeys();
                }

                // 3. 执行任务队列中的任务
                runAllTasks();

            } catch (Throwable t) {
                System.err.println("[NioEventLoop] 事件循环异常: " + t.getMessage());
            }
        }

        // 关闭 Selector
        try {
            selector.close();
        } catch (IOException e) {
            System.err.println("[NioEventLoop] 关闭 Selector 失败: " + e.getMessage());
        }

        terminated.set(true);
        System.out.println("[NioEventLoop] 事件循环已停止");
    }

    /**
     * 选择就绪的 Channel
     *
     * @return 就绪的 Channel 数量
     * @throws IOException 如果选择操作失败
     */
    private int select() throws IOException {
        // 如果有任务，使用 selectNow() 不阻塞
        if (hasTasks()) {
            return selector.selectNow();
        }
        
        // 否则使用超时选择，最多等待 1 秒
        return selector.select(1000);
    }

    /**
     * 处理就绪的 SelectionKey
     */
    private void processSelectedKeys() {
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = selectedKeys.iterator();

        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            iterator.remove();

            if (!key.isValid()) {
                continue;
            }

            try {
                processSelectedKey(key);
            } catch (Throwable t) {
                System.err.println("[NioEventLoop] 处理事件失败: " + t.getMessage());
                // 关闭出错的 Channel
                try {
                    key.channel().close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * 处理单个 SelectionKey
     *
     * <p>子类或通过注册的 Channel 处理具体事件。
     *
     * @param key 就绪的 SelectionKey
     */
    protected void processSelectedKey(SelectionKey key) {
        // 默认实现：打印事件信息
        // 实际的事件处理将由注册的 Channel 完成
        if (key.isAcceptable()) {
            System.out.println("[NioEventLoop] ACCEPT 事件");
        }
        if (key.isConnectable()) {
            System.out.println("[NioEventLoop] CONNECT 事件");
        }
        if (key.isReadable()) {
            System.out.println("[NioEventLoop] READ 事件");
        }
        if (key.isWritable()) {
            System.out.println("[NioEventLoop] WRITE 事件");
        }
    }

    @Override
    protected void wakeup() {
        // 唤醒可能阻塞的 select()
        if (!inEventLoop()) {
            selector.wakeup();
        }
    }

    @Override
    protected String getThreadName() {
        return "nio-eventloop-" + Integer.toHexString(hashCode());
    }
}
