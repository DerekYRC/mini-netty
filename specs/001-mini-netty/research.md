# 研究文档: Mini-Netty

**创建日期**: 2025-12-16
**目的**: 解决技术上下文中的未知项，为实现计划提供技术决策依据

## 1. Java NIO 核心组件

### 决策: 使用 Java NIO Selector 实现多路复用

**理由**:
- Selector 是 Java NIO 的核心，一个 Selector 可以监控多个 Channel 的 I/O 事件
- 事件驱动模型，避免阻塞等待
- 这是 Netty EventLoop 的底层实现基础

**考虑的替代方案**:
- BIO (阻塞 I/O): 每个连接需要一个线程，资源消耗大，不适合高并发场景
- AIO (异步 I/O): Java 7 引入，但在 Linux 上实现不如 NIO 成熟，Netty 也主要使用 NIO

**关键 API**:
```java
Selector selector = Selector.open();
ServerSocketChannel serverChannel = ServerSocketChannel.open();
serverChannel.configureBlocking(false);
serverChannel.register(selector, SelectionKey.OP_ACCEPT);

while (true) {
    selector.select();  // 阻塞直到有事件
    Set<SelectionKey> selectedKeys = selector.selectedKeys();
    // 处理事件...
}
```

## 2. EventLoop 设计模式

### 决策: 采用单线程 EventLoop 模型

**理由**:
- 每个 EventLoop 绑定一个线程，处理注册到它的所有 Channel 的 I/O 事件
- 避免多线程并发问题，简化编程模型
- 与 Netty 的设计保持一致

**考虑的替代方案**:
- 多线程共享 Channel: 需要复杂的同步机制，容易出错
- 每连接一线程: 资源消耗大，不适合高并发

**核心接口设计**:
```java
public interface EventLoop extends EventLoopGroup {
    EventLoopGroup parent();
    boolean inEventLoop();
    boolean inEventLoop(Thread thread);
    void execute(Runnable task);
    ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit);
}
```

## 3. Channel 抽象设计

### 决策: 定义统一的 Channel 接口，支持不同传输类型

**理由**:
- 抽象网络连接，屏蔽底层细节
- 支持 NIO SocketChannel 和 ServerSocketChannel
- 便于后续扩展其他传输类型

**关键抽象**:
- `Channel`: 网络连接的抽象
- `Channel.Unsafe`: 封装底层 I/O 操作（read, write, bind, connect）
- `ChannelConfig`: Channel 的配置选项

**继承层次**:
```
Channel
├── AbstractChannel
│   ├── AbstractNioChannel
│   │   ├── NioServerSocketChannel
│   │   └── NioSocketChannel
```

## 4. ChannelPipeline 责任链

### 决策: 使用双向链表实现 Pipeline

**理由**:
- 入站事件从头到尾传递 (Head → ... → Tail)
- 出站事件从尾到头传递 (Tail → ... → Head)
- 双向链表便于动态添加/删除 Handler

**核心结构**:
```java
class DefaultChannelPipeline {
    final HeadContext head;
    final TailContext tail;
    
    // head <-> handler1 <-> handler2 <-> ... <-> tail
}

class AbstractChannelHandlerContext {
    AbstractChannelHandlerContext next;
    AbstractChannelHandlerContext prev;
    ChannelHandler handler;
}
```

**事件传递方法**:
- 入站: `fireChannelRead()`, `fireChannelActive()`, `fireExceptionCaught()`
- 出站: `write()`, `flush()`, `close()`, `bind()`, `connect()`

## 5. ByteBuf 设计

### 决策: 实现读写索引分离的 ByteBuf，不实现池化

**理由**:
- 读写索引分离解决了 ByteBuffer 需要 flip() 的问题
- 引用计数帮助理解 Netty 的内存管理思想
- 跳过池化以降低实现复杂度

**核心 API**:
```java
public interface ByteBuf extends ReferenceCounted {
    int readerIndex();
    int writerIndex();
    int readableBytes();
    int writableBytes();
    
    byte readByte();
    ByteBuf writeByte(int value);
    
    ByteBuf retain();
    boolean release();
}
```

**与 ByteBuffer 对比**:

| 特性 | ByteBuffer | ByteBuf |
|------|------------|---------|
| 索引 | 单一 position | 独立的 readerIndex/writerIndex |
| 读写切换 | 需要 flip() | 不需要 |
| 容量扩展 | 不支持 | 自动扩容 |
| 引用计数 | 无 | 支持 |

## 6. 编解码器设计

### 决策: 实现基于长度字段的解码器解决粘包拆包

**理由**:
- TCP 是流式协议，不保证消息边界
- 长度字段解码器是最通用的解决方案
- 与 Netty 的 LengthFieldBasedFrameDecoder 设计一致

**粘包拆包解决方案对比**:

| 方案 | 优点 | 缺点 |
|------|------|------|
| 固定长度 | 简单 | 浪费带宽 |
| 分隔符 | 灵活 | 需要转义 |
| 长度字段 | 高效通用 | 稍复杂 |

**核心类**:
```java
public abstract class ByteToMessageDecoder extends ChannelInboundHandlerAdapter {
    protected abstract void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out);
}

public class LengthFieldBasedFrameDecoder extends ByteToMessageDecoder {
    private final int lengthFieldOffset;
    private final int lengthFieldLength;
    // ...
}
```

## 7. Bootstrap 启动器设计

### 决策: 使用流式 API (Builder 模式) 配置

**理由**:
- 链式调用使配置代码简洁易读
- 与 Netty API 保持一致
- 类型安全，编译期检查

**API 设计**:
```java
ServerBootstrap b = new ServerBootstrap();
b.group(bossGroup, workerGroup)
 .channel(NioServerSocketChannel.class)
 .childHandler(new ChannelInitializer<SocketChannel>() {
     @Override
     public void initChannel(SocketChannel ch) {
         ch.pipeline().addLast(new EchoServerHandler());
     }
 });
ChannelFuture f = b.bind(8080).sync();
```

## 8. 主从 Reactor 模型

### 决策: 实现 Boss-Worker 线程模型

**理由**:
- Boss 线程只负责接受连接，Worker 线程负责 I/O 处理
- 分工明确，提高吞吐量
- 这是 Netty 推荐的最佳实践

**线程模型**:
```
BossGroup (1个EventLoop)
    └── 监听 ServerSocketChannel
        └── 接收连接 → 将 SocketChannel 注册到 WorkerGroup

WorkerGroup (N个EventLoop, N通常为CPU核心数)
    └── 处理 SocketChannel 的读写事件
```

## 9. 异常处理策略

### 决策: 异常通过 Pipeline 传播到 exceptionCaught

**理由**:
- 集中处理异常，便于日志记录
- 给予 Handler 处理异常的机会
- 未处理的异常关闭 Channel，防止资源泄漏

**传播流程**:
```
Handler1.channelRead() 抛出异常
    → Handler1.exceptionCaught() [如果重写]
    → Handler2.exceptionCaught()
    → ... 
    → TailContext.exceptionCaught() [默认: 日志警告]
```

## 10. 分支管理策略

### 决策: 每个迭代独立分支，基于前一分支创建

**理由**:
- 每个分支代表一个独立的学习单元
- 学习者可以 checkout 任意分支查看该阶段的完整代码
- 与 mini-spring 的组织方式一致

**分支结构**:
```
main (基础README和pom.xml)
├── simple-bio-server (第1个迭代)
├── simple-bio-client (基于第1个，第2个迭代)
├── multi-thread-bio-server (基于第2个，第3个迭代)
└── ... (依次类推)
```

## 技术决策汇总

| 领域 | 决策 | 依据 |
|------|------|------|
| I/O 模型 | NIO Selector | 高效多路复用，Netty 基础 |
| 线程模型 | 单线程 EventLoop | 避免并发问题，简化编程 |
| Channel 抽象 | 统一接口 + Unsafe | 屏蔽底层，支持扩展 |
| Pipeline | 双向链表 | 支持入站/出站双向传递 |
| 缓冲区 | ByteBuf 无池化 | 读写分离，引用计数 |
| 编解码 | 长度字段解码器 | 通用解决粘包拆包 |
| 配置 | Bootstrap 流式 API | 简洁易读，类型安全 |
| 线程模型 | Boss-Worker | 分工明确，高吞吐 |
| 异常 | Pipeline 传播 | 集中处理，防泄漏 |
| 版本控制 | 迭代独立分支 | 便于学习，对照 mini-spring |
