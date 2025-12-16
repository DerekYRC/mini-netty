# 数据模型: Mini-Netty

**创建日期**: 2025-12-16
**目的**: 定义 Mini-Netty 框架的核心实体、接口和关系

## 核心实体

### 1. EventLoop 事件循环

**描述**: 事件循环是 Netty 的核心调度器，负责处理 Channel 的 I/O 事件和异步任务。

```
┌─────────────────────────────────────────────────┐
│                   EventLoop                      │
├─────────────────────────────────────────────────┤
│ - selector: Selector                            │
│ - thread: Thread                                │
│ - taskQueue: Queue<Runnable>                    │
│ - scheduledTaskQueue: PriorityQueue<ScheduledTask>│
├─────────────────────────────────────────────────┤
│ + register(channel: Channel): ChannelFuture     │
│ + execute(task: Runnable): void                 │
│ + schedule(task: Runnable, delay: long): Future │
│ + inEventLoop(): boolean                        │
│ + inEventLoop(thread: Thread): boolean          │
└─────────────────────────────────────────────────┘
```

**属性**:
- `selector`: NIO Selector，监控注册的 Channel
- `thread`: 绑定的线程，所有操作在此线程执行
- `taskQueue`: 普通任务队列
- `scheduledTaskQueue`: 定时任务队列

**状态转换**: 无（生命周期与 EventLoopGroup 一致）

---

### 2. EventLoopGroup 事件循环组

**描述**: 管理一组 EventLoop，提供负载均衡。

```
┌─────────────────────────────────────────────────┐
│                EventLoopGroup                    │
├─────────────────────────────────────────────────┤
│ - eventLoops: EventLoop[]                       │
│ - chooser: EventLoopChooser                     │
├─────────────────────────────────────────────────┤
│ + next(): EventLoop                             │
│ + register(channel: Channel): ChannelFuture     │
│ + shutdownGracefully(): Future                  │
└─────────────────────────────────────────────────┘
```

**关系**: 1:N 与 EventLoop

---

### 3. Channel 网络通道

**描述**: 网络连接的抽象，代表一个可进行 I/O 操作的通道。

```
┌─────────────────────────────────────────────────┐
│                    Channel                       │
├─────────────────────────────────────────────────┤
│ - id: ChannelId                                 │
│ - parent: Channel (服务端Channel为null)          │
│ - eventLoop: EventLoop                          │
│ - pipeline: ChannelPipeline                     │
│ - unsafe: Unsafe                                │
│ - config: ChannelConfig                         │
├─────────────────────────────────────────────────┤
│ + eventLoop(): EventLoop                        │
│ + pipeline(): ChannelPipeline                   │
│ + config(): ChannelConfig                       │
│ + isOpen(): boolean                             │
│ + isActive(): boolean                           │
│ + read(): Channel                               │
│ + write(msg: Object): ChannelFuture             │
│ + flush(): Channel                              │
│ + close(): ChannelFuture                        │
└─────────────────────────────────────────────────┘
```

**子类**:
- `NioServerSocketChannel`: 服务端监听 Channel
- `NioSocketChannel`: 客户端/连接 Channel

**状态转换**:
```
UNREGISTERED → REGISTERED → ACTIVE → INACTIVE → UNREGISTERED
```

---

### 4. ChannelPipeline 处理链

**描述**: Handler 的容器，使用双向链表组织，负责事件的分发。

```
┌─────────────────────────────────────────────────┐
│               ChannelPipeline                    │
├─────────────────────────────────────────────────┤
│ - channel: Channel                              │
│ - head: HeadContext                             │
│ - tail: TailContext                             │
├─────────────────────────────────────────────────┤
│ + addFirst(handler: ChannelHandler): Pipeline   │
│ + addLast(handler: ChannelHandler): Pipeline    │
│ + remove(handler: ChannelHandler): Pipeline     │
│ + fireChannelRegistered(): Pipeline             │
│ + fireChannelActive(): Pipeline                 │
│ + fireChannelRead(msg: Object): Pipeline        │
│ + fireExceptionCaught(cause: Throwable): Pipeline│
└─────────────────────────────────────────────────┘
```

**内部结构**:
```
head ⇄ handler1 ⇄ handler2 ⇄ ... ⇄ tail
```

---

### 5. ChannelHandler 事件处理器

**描述**: 处理 I/O 事件的接口，分为入站和出站两类。

```
┌─────────────────────────────────────────────────┐
│             ChannelHandler (接口)                │
├─────────────────────────────────────────────────┤
│ + handlerAdded(ctx: ChannelHandlerContext): void│
│ + handlerRemoved(ctx: ChannelHandlerContext): void│
└─────────────────────────────────────────────────┘
         ▲                         ▲
         │                         │
┌────────┴────────┐     ┌─────────┴─────────┐
│ChannelInbound   │     │ChannelOutbound    │
│Handler          │     │ Handler           │
├─────────────────┤     ├───────────────────┤
│+channelRegistered│     │+bind()            │
│+channelActive   │     │+connect()         │
│+channelRead     │     │+write()           │
│+channelReadComplete│  │+flush()           │
│+exceptionCaught │     │+close()           │
└─────────────────┘     └───────────────────┘
```

---

### 6. ChannelHandlerContext 上下文

**描述**: 连接 Handler 和 Pipeline，提供事件传递能力。

```
┌─────────────────────────────────────────────────┐
│           ChannelHandlerContext                  │
├─────────────────────────────────────────────────┤
│ - prev: ChannelHandlerContext                   │
│ - next: ChannelHandlerContext                   │
│ - handler: ChannelHandler                       │
│ - pipeline: ChannelPipeline                     │
│ - eventLoop: EventLoop                          │
├─────────────────────────────────────────────────┤
│ + channel(): Channel                            │
│ + handler(): ChannelHandler                     │
│ + fireChannelRead(msg: Object): Context         │
│ + write(msg: Object): ChannelFuture             │
└─────────────────────────────────────────────────┘
```

---

### 7. ByteBuf 缓冲区

**描述**: 高效的字节缓冲区，读写索引分离。

```
┌─────────────────────────────────────────────────┐
│                   ByteBuf                        │
├─────────────────────────────────────────────────┤
│ - data: byte[]                                  │
│ - readerIndex: int                              │
│ - writerIndex: int                              │
│ - capacity: int                                 │
│ - refCnt: int (引用计数)                         │
├─────────────────────────────────────────────────┤
│ + readerIndex(): int                            │
│ + writerIndex(): int                            │
│ + readableBytes(): int                          │
│ + writableBytes(): int                          │
│ + readByte(): byte                              │
│ + writeByte(value: int): ByteBuf                │
│ + readBytes(dst: byte[]): ByteBuf               │
│ + writeBytes(src: byte[]): ByteBuf              │
│ + retain(): ByteBuf                             │
│ + release(): boolean                            │
└─────────────────────────────────────────────────┘
```

**索引示意**:
```
+-------------------+------------------+------------------+
| discardable bytes |  readable bytes  |  writable bytes  |
+-------------------+------------------+------------------+
|                   |                  |                  |
0      <=      readerIndex   <=   writerIndex    <=    capacity
```

---

### 8. Bootstrap 启动器

**描述**: 简化服务端/客户端的配置与启动。

```
┌─────────────────────────────────────────────────┐
│              AbstractBootstrap<B,C>              │
├─────────────────────────────────────────────────┤
│ - group: EventLoopGroup                         │
│ - channelFactory: ChannelFactory                │
│ - handler: ChannelHandler                       │
│ - options: Map<ChannelOption, Object>           │
├─────────────────────────────────────────────────┤
│ + group(group: EventLoopGroup): B               │
│ + channel(channelClass: Class<C>): B            │
│ + handler(handler: ChannelHandler): B           │
│ + option(option: ChannelOption, value: Object): B│
└─────────────────────────────────────────────────┘
         ▲                         ▲
         │                         │
┌────────┴────────┐     ┌─────────┴─────────┐
│ ServerBootstrap │     │    Bootstrap       │
├─────────────────┤     ├───────────────────┤
│ - childGroup    │     │                   │
│ - childHandler  │     │                   │
├─────────────────┤     ├───────────────────┤
│ + bind(port): F │     │ + connect(): F    │
└─────────────────┘     └───────────────────┘
```

---

## 实体关系图

```
┌──────────────┐
│EventLoopGroup│
└──────┬───────┘
       │ 1:N
       ▼
┌──────────────┐         ┌──────────────┐
│  EventLoop   │◄────────│   Channel    │
└──────────────┘   N:1   └──────┬───────┘
                                │ 1:1
                                ▼
                         ┌──────────────┐
                         │ChannelPipeline│
                         └──────┬───────┘
                                │ 1:N
                                ▼
                    ┌───────────────────────┐
                    │ChannelHandlerContext  │
                    └───────────┬───────────┘
                                │ 1:1
                                ▼
                         ┌──────────────┐
                         │ChannelHandler│
                         └──────────────┘
```

---

## 事件流转

### 入站事件 (Inbound)

```
网络数据到达
    ↓
NIO Selector 触发 READ 事件
    ↓
EventLoop.processSelectedKeys()
    ↓
Channel.Unsafe.read()
    ↓
Pipeline.fireChannelRead(byteBuf)
    ↓
Head → Handler1 → Handler2 → ... → Tail
       (解码器)    (业务Handler)
```

### 出站事件 (Outbound)

```
业务代码调用 channel.write(msg)
    ↓
Pipeline.write(msg)
    ↓
Tail → ... → Handler2 → Handler1 → Head
              (业务)      (编码器)
    ↓
Channel.Unsafe.write()
    ↓
数据写入网络
```

---

## 验证规则

| 实体 | 规则 | 说明 |
|------|------|------|
| EventLoop | 必须绑定唯一线程 | 保证线程安全 |
| Channel | 必须关联唯一 EventLoop | 整个生命周期不变 |
| Pipeline | 必须包含 Head 和 Tail | 边界处理 |
| ByteBuf | refCnt >= 0 | 为0时释放 |
| Handler | 可共享或独占 | 通过 @Sharable 标注 |
