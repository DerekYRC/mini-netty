# Mini-Netty

简化版 Netty 网络编程框架，用于学习 Netty 核心原理。

## 项目介绍

Mini-Netty 参照 [mini-spring](./mini-spring) 的渐进式学习方法，从零开始实现一个简化版的 Netty 网络编程框架。通过 37 个细粒度的迭代分支，逐步引入网络编程的核心概念。

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
git clone <repository-url>
cd mini-netty

# 编译项目
mvn compile

# 运行测试
mvn test
```

### 学习建议

1. **按顺序学习**: 每个迭代都基于前一个迭代，建议按 IT01 → IT37 顺序学习
2. **切换分支**: 使用 `git checkout <分支名>` 切换到对应迭代
3. **阅读 changelog**: 每个分支的改动都记录在 changelog.md 中
4. **运行测试**: 每个迭代都有对应的单元测试和集成测试
5. **动手实践**: 尝试修改代码，加深理解

## 技术栈

- **语言**: Java 17
- **构建工具**: Maven 3.6+
- **测试框架**: JUnit 5 + AssertJ
- **依赖**: 无外部依赖（纯 JDK 实现）

## 参考资料

- [Netty 官方文档](https://netty.io/wiki/)
- [Netty in Action](https://www.manning.com/books/netty-in-action)
- [mini-spring](./mini-spring) - 本项目的参考模板

## 许可证

本项目仅用于学习目的。
