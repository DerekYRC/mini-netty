# mini-netty

[![Java](https://img.shields.io/badge/Java-17%2B-blue)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/Maven-3.6%2B-orange)](https://maven.apache.org/)
[![Tests](https://img.shields.io/badge/Tests-400+%20passing-brightgreen)]()
[![License](https://img.shields.io/badge/License-Educational-yellow)]()

**[English](./README_en.md) | 简体中文**

**姊妹版：**
- [**mini-spring**](https://github.com/DerekYRC/mini-spring) **(简化版的spring框架)**
- [**mini-spring-cloud**](https://github.com/DerekYRC/mini-spring-cloud) **(简化版的spring cloud框架)**
- [**mini-claude-code**](https://github.com/DerekYRC/mini-claude-code) **(简化版的Claude Code)**

## 项目介绍

**mini-netty** 是一个从零开始实现的简化版 **Netty** 网络编程框架。通过 37 个细粒度的迭代分支，逐步引入网络编程的核心概念。 该项目使用**AI**(**Claude Code** + **Spec-Kit**)进行辅助开发。

### ✨ 特性

- 📚 **渐进式学习**: 37 个迭代，每个都可独立运行
- 🔬 **完整测试**: 400+ 单元测试和集成测试
- 📖 **详细文档**: 每个迭代都有 [changelog](https://github.com/DerekYRC/mini-netty/blob/main/changelog.md) 记录
- 🎯 **零依赖**: 纯 JDK 实现，无第三方依赖
- 🏗️ **真实架构**: 与 Netty 保持相同的包结构和命名

### 🏛️ 架构概览

```
                           ┌─────────────────────────────────────┐
                           │           ServerBootstrap           │
                           └──────────────────┬──────────────────┘
                                              │
                    ┌─────────────────────────┼─────────────────────────┐
                    │                         │                         │
           ┌────────┴────────┐      ┌────────┴────────┐      ┌────────┴────────┐
           │  BossGroup (1)  │      │ WorkerGroup (N) │      │  ChannelOption  │
           └────────┬────────┘      └────────┬────────┘      └─────────────────┘
                    │                        │
           ┌────────┴────────┐      ┌────────┴────────┐
           │  NioEventLoop   │      │  NioEventLoop   │ × N
           │  (Selector)     │      │  (Selector)     │
           └────────┬────────┘      └────────┬────────┘
                    │                        │
           ┌────────┴────────┐      ┌────────┴────────┐
           │ ServerChannel   │─────▶│  SocketChannel  │ × Connections
           └────────┬────────┘      └────────┬────────┘
                    │                        │
           ┌────────┴────────────────────────┴────────┐
           │              ChannelPipeline             │
           │  ┌─────┐   ┌─────┐   ┌─────┐   ┌─────┐   │
           │  │HEAD │◀─▶│ H1  │◀─▶│ H2  │◀─▶│TAIL │   │
           │  └─────┘   └─────┘   └─────┘   └─────┘   │
           │    ▼ Inbound                 Outbound ▲  │
           └──────────────────────────────────────────┘
```

### 📁 项目结构

```
src/main/java/io/netty/
├── bootstrap/          # 启动器 (ServerBootstrap, Bootstrap)
├── buffer/             # 缓冲区 (ByteBuf, HeapByteBuf)
├── channel/            # 核心通道
│   ├── nio/            # NIO 实现 (NioEventLoop, NioChannel)
│   ├── Channel.java    # 通道接口
│   ├── ChannelPipeline.java
│   ├── ChannelHandler.java
│   └── EventLoop.java
└── handler/            # 内置处理器
    ├── codec/          # 编解码器
    ├── logging/        # 日志处理器
    └── timeout/        # 超时处理器
```

## 学习路径

### 第一阶段：网络通信基础 (IT01-IT07)

学习 BIO 和 NIO 编程模型，理解阻塞 I/O 与非阻塞 I/O 的区别。

| 迭代 | 分支名 | 学习目标 |
|------|--------|----------|
| IT01 | simple-bio-server | 最简单的 BIO 服务端 |
| IT02 | simple-bio-client | BIO 客户端实现 |
| IT03 | multi-thread-bio-server | 多线程处理并发连接 |
| IT04 | nio-channel-buffer | NIO Channel 和 Buffer |
| IT05 | nio-selector | NIO Selector 多路复用 |
| IT06 | nio-server-accept | NIO 服务端 ACCEPT 事件 |
| IT07 | nio-server-read-write | NIO 完整读写流程 |

### 第二阶段：EventLoop 事件循环 (IT08-IT11)

学习 Netty 的事件循环机制，理解单线程处理多连接的原理。

| 迭代 | 分支名 | 学习目标 |
|------|--------|----------|
| IT08 | event-loop-interface | EventLoop 接口定义 |
| IT09 | single-thread-event-loop | 单线程事件循环实现 |
| IT10 | event-loop-task-queue | 任务队列机制 |
| IT11 | event-loop-scheduled-task | 定时任务支持 |

### 第三阶段：Channel 和 Pipeline (IT12-IT21)

学习 Channel 抽象和 Pipeline 责任链模式。

| 迭代 | 分支名 | 学习目标 |
|------|--------|----------|
| IT12 | channel-interface | Channel 接口定义 |
| IT13 | nio-channel-impl | NioChannel 实现 |
| IT14 | channel-config | Channel 配置 |
| IT15 | channel-unsafe | Channel.Unsafe 底层操作 |
| IT16 | channel-handler-interface | Handler 接口定义 |
| IT17 | channel-pipeline-basic | Pipeline 双向链表 |
| IT18 | channel-handler-context | HandlerContext 上下文 |
| IT19 | inbound-handler | 入站事件处理 |
| IT20 | outbound-handler | 出站事件处理 |
| IT21 | handler-adapter | Handler 适配器和异常传播 |

### 第四阶段：ByteBuf 缓冲区 (IT22-IT25)

学习 Netty 的 ByteBuf 设计，理解读写索引分离和引用计数。

| 迭代 | 分支名 | 学习目标 |
|------|--------|----------|
| IT22 | byte-buf-interface | ByteBuf 接口定义 |
| IT23 | heap-byte-buf | 堆内存 ByteBuf |
| IT24 | byte-buf-reference-count | 引用计数机制 |
| IT25 | byte-buf-allocator | ByteBuf 分配器 |

### 第五阶段：编解码器 (IT26-IT29)

学习解决 TCP 粘包/拆包问题的编解码器设计。

| 迭代 | 分支名 | 学习目标 |
|------|--------|----------|
| IT26 | byte-to-message-decoder | 字节转消息解码器 |
| IT27 | fixed-length-decoder | 固定长度解码器 |
| IT28 | length-field-decoder | 长度字段解码器 |
| IT29 | string-codec | 字符串编解码器 |

### 第六阶段：Bootstrap 启动器 (IT30-IT32)

学习 Netty 流式 API 的设计和使用。

| 迭代 | 分支名 | 学习目标 |
|------|--------|----------|
| IT30 | abstract-bootstrap | Bootstrap 抽象基类 |
| IT31 | server-bootstrap | 服务端启动器 |
| IT32 | client-bootstrap | 客户端启动器 |

### 第七阶段：多线程模型 (IT33-IT35)

学习 Boss-Worker 主从 Reactor 模型。

| 迭代 | 分支名 | 学习目标 |
|------|--------|----------|
| IT33 | event-loop-group | EventLoopGroup 线程组 |
| IT34 | boss-worker-model | 主从 Reactor 模型 |
| IT35 | channel-chooser | 负载均衡策略 |

### 第八阶段：实用 Handler (IT36-IT37)

学习常用的 Handler 实现。

| 迭代 | 分支名 | 学习目标 |
|------|--------|----------|
| IT36 | idle-state-handler | 空闲状态检测 |
| IT37 | logging-handler | 日志处理器 |

## 快速开始

### 环境要求

- JDK 17 或更高版本
- Maven 3.6 或更高版本
- Git

### 构建项目

```bash
# 克隆仓库
git clone git@github.com:DerekYRC/mini-netty.git
cd mini-netty

# 编译项目
mvn compile

# 运行测试
mvn test
```

### 学习建议

1. **按顺序学习**: 每个迭代都基于前一个迭代，建议按 IT01 → IT37 顺序学习
2. **切换分支**: 使用 `git checkout <分支名>` 切换到对应迭代
3. **阅读 [changelog](https://github.com/DerekYRC/mini-netty/blob/main/changelog.md)**: 每个分支的改动都记录在 [**changelog.md**](https://github.com/DerekYRC/mini-netty/blob/main/changelog.md) 中
4. **运行测试**: 每个迭代都有对应的单元测试和集成测试
5. **动手实践**: 尝试修改代码，加深理解

## 🔧 核心组件

### EventLoop - 事件循环

```java
EventLoopGroup group = new NioEventLoopGroup(4);
EventLoop eventLoop = group.next();

// 提交任务
eventLoop.execute(() -> System.out.println("Hello from EventLoop"));

// 定时任务
eventLoop.schedule(() -> System.out.println("Delayed"), 1, TimeUnit.SECONDS);
```

### Channel & Pipeline - 通道和管道

```java
Channel channel = ...;
ChannelPipeline pipeline = channel.pipeline();

// 添加处理器
pipeline.addLast("decoder", new StringDecoder());
pipeline.addLast("encoder", new StringEncoder());
pipeline.addLast("handler", new MyHandler());
```

### ByteBuf - 缓冲区

```java
ByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
ByteBuf buf = allocator.buffer(256);

// 写入数据
buf.writeBytes("Hello".getBytes());
buf.writeInt(42);

// 读取数据
byte[] bytes = new byte[5];
buf.readBytes(bytes);
int value = buf.readInt();

// 释放资源
buf.release();
```

### ServerBootstrap - 服务端启动器

```java
EventLoopGroup bossGroup = new NioEventLoopGroup(1);
EventLoopGroup workerGroup = new NioEventLoopGroup(4);

ServerBootstrap b = new ServerBootstrap();
b.group(bossGroup, workerGroup)
 .channel(NioServerSocketChannel.class)
 .option(ChannelOption.SO_BACKLOG, 128)
 .childOption(ChannelOption.SO_KEEPALIVE, true)
 .childHandler(new ChannelInitializer<Channel>() {
     @Override
     protected void initChannel(Channel ch) {
         ch.pipeline().addLast(new LoggingHandler());
         ch.pipeline().addLast(new EchoServerHandler());
     }
 });

ChannelFuture f = b.bind(8080).sync();
```

## 技术栈

- **语言**: Java 17
- **构建工具**: Maven 3.6+
- **测试框架**: JUnit 5 + AssertJ
- **依赖**: 无外部依赖（纯 JDK 实现）
- **测试覆盖**: 400+ 测试用例

## 📚 延伸阅读

### 官方资源
- [Netty 官方文档](https://netty.io/wiki/)
- [Netty GitHub](https://github.com/netty/netty)

### 推荐书籍
- [Netty in Action](https://www.manning.com/books/netty-in-action)

## 版权说明

未取得本人书面许可，不得将该项目用于商业用途
