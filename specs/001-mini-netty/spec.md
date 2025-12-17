# 功能规格说明: Mini-Netty 网络编程框架

**功能分支**: `001-mini-netty`  
**创建日期**: 2025-12-16  
**状态**: 草稿  
**输入**: 参照mini-spring项目从零开始实现一个简化版的netty网络编程框架mini-netty

## 用户场景与测试 *(必填)*

### 用户故事 1 - 最简单的网络通信 (优先级: P1)

作为一名学习者，我希望能够创建一个最基础的服务端和客户端，实现简单的消息收发，从而理解网络编程的基本原理。

**优先级理由**: 这是所有网络编程的基础，没有这个基础，后续的所有功能都无法实现。类似于mini-spring中的"最简单的bean容器"。

**独立测试**: 可以通过启动服务端监听端口，客户端连接并发送消息，服务端接收并返回响应来完整测试。

**验收场景**:

1. **Given** 服务端已启动并监听指定端口, **When** 客户端连接并发送"hello", **Then** 服务端接收到消息并返回"hello, mini-netty"
2. **Given** 服务端已启动, **When** 多个客户端同时连接, **Then** 每个客户端都能正常收发消息

---

### 用户故事 2 - 基于NIO的事件循环 (优先级: P1)

作为一名学习者，我希望理解Netty的EventLoop机制，学习如何用单线程处理多个连接的I/O事件。

**优先级理由**: EventLoop是Netty的核心，理解事件循环机制对于理解Netty的高性能原理至关重要。

**独立测试**: 可以通过创建EventLoop实例，注册多个Channel，验证事件能够被正确分发和处理。

**验收场景**:

1. **Given** EventLoop已创建并运行, **When** 注册一个ServerSocketChannel, **Then** 能够接收到ACCEPT事件
2. **Given** EventLoop正在运行, **When** 客户端发送数据, **Then** EventLoop能够触发READ事件并处理

---

### 用户故事 3 - Channel和ChannelHandler处理链 (优先级: P1)

作为一名学习者，我希望理解Netty的Channel抽象和Handler链式处理模式，学习如何优雅地组织网络事件处理逻辑。

**优先级理由**: ChannelPipeline是Netty设计的精髓，理解责任链模式的应用对于编写可维护的网络程序非常重要。

**独立测试**: 可以通过配置多个Handler，验证消息按顺序经过每个Handler处理。

**验收场景**:

1. **Given** Pipeline中配置了3个Handler, **When** 收到入站消息, **Then** 消息依次经过Handler1→Handler2→Handler3处理
2. **Given** Pipeline中有解码器和业务Handler, **When** 收到原始字节, **Then** 解码器将字节转为对象后传递给业务Handler

---

### 用户故事 4 - ByteBuf缓冲区 (优先级: P2)

作为一名学习者，我希望理解Netty的ByteBuf设计，学习它相比Java NIO ByteBuffer的优势。

**优先级理由**: ByteBuf是Netty高效数据处理的基础，但可以在理解核心流程后再深入。

**独立测试**: 可以通过ByteBuf的读写操作测试，验证读写索引分离和自动扩容等特性。

**验收场景**:

1. **Given** 创建一个ByteBuf实例, **When** 写入数据后读取, **Then** 读写索引分别维护，可独立操作
2. **Given** ByteBuf容量为10字节, **When** 写入15字节, **Then** 自动扩容并成功写入

---

### 用户故事 5 - 编解码器 (优先级: P2)

作为一名学习者，我希望理解如何解决TCP粘包/拆包问题，学习常用的编解码器设计。

**优先级理由**: 编解码是实际网络应用必须解决的问题，但属于应用层面的扩展。

**独立测试**: 可以通过发送多条消息，验证解码器能正确拆分并解码每条消息。

**验收场景**:

1. **Given** 配置了LengthFieldBasedFrameDecoder, **When** 发送带长度前缀的消息, **Then** 能正确解析出完整消息
2. **Given** 配置了StringDecoder, **When** 收到字节数据, **Then** 能转换为String对象

---

### 用户故事 6 - Bootstrap启动器 (优先级: P2)

作为一名学习者，我希望使用类似Netty的流式API配置和启动服务端/客户端，体验Netty的优雅设计。

**优先级理由**: Bootstrap简化了Netty的使用，但核心能力应该先于易用性封装。

**独立测试**: 可以通过使用Bootstrap API配置并启动服务端，验证流式配置和启动流程。

**验收场景**:

1. **Given** 使用ServerBootstrap配置服务端, **When** 调用bind方法, **Then** 服务端启动并监听指定端口
2. **Given** 使用Bootstrap配置客户端, **When** 调用connect方法, **Then** 成功连接到服务端

---

### 边界情况

- 客户端异常断开连接时，服务端如何处理？
- 服务端关闭时，如何优雅地释放资源？
- EventLoop线程阻塞时，如何保证其他Channel的事件处理？
- ByteBuf内存泄漏如何检测和预防？

## 需求 *(必填)*

### 功能需求

**基础篇 - 核心机制**

- **FR-001**: 框架必须提供基于Java NIO的非阻塞网络通信能力
- **FR-002**: 框架必须实现EventLoop事件循环机制，支持单线程处理多连接
- **FR-003**: 框架必须提供Channel抽象，封装底层SocketChannel操作
- **FR-004**: 框架必须实现ChannelPipeline责任链模式，支持Handler链式处理
- **FR-005**: 框架必须提供ChannelHandler接口，支持入站(Inbound)和出站(Outbound)事件处理
- **FR-006**: 框架必须实现ChannelHandlerContext，支持Handler间的事件传递

**扩展篇 - 增强能力**

- **FR-007**: 框架必须提供ByteBuf缓冲区，支持读写索引分离
- **FR-008**: 框架必须实现ByteBuf引用计数（retain/release），帮助理解内存管理（不实现池化）
- **FR-009**: 框架必须提供常用编解码器（长度字段解码器、字符串编解码器）
- **FR-010**: 框架必须提供Bootstrap启动器，支持流式API配置
- **FR-011**: 框架必须支持EventLoopGroup，实现多EventLoop负载均衡

**非功能需求**

- **FR-012**: 每次迭代必须记录到changelog.md文件中
- **FR-013**: 每次功能改动必须补充完整的单元测试
- **FR-014**: 每次改动必须启动服务端和客户端进行实际验证（单元测试 + 集成测试）
- **FR-015**: Java版本必须为17，参考Netty 4.x版本设计
- **FR-016**: 包结构必须对齐Netty（io.netty.channel、io.netty.buffer、io.netty.bootstrap等）
- **FR-017**: 异常必须通过Pipeline传播，由Handler的exceptionCaught方法处理
- **FR-018**: 每个迭代必须新建独立的git分支，分支名与迭代名称一致

### 关键实体

- **EventLoop**: 事件循环，负责处理Channel的I/O事件，一个EventLoop可管理多个Channel
- **Channel**: 网络连接的抽象，代表一个可进行I/O操作的通道
- **ChannelPipeline**: 处理链，包含一系列ChannelHandler，负责处理入站和出站事件
- **ChannelHandler**: 事件处理器，处理具体的I/O事件（如读取、写入、连接、断开）
- **ChannelHandlerContext**: Handler上下文，连接Handler和Pipeline，提供事件传递能力
- **ByteBuf**: 缓冲区，用于高效读写二进制数据
- **Bootstrap/ServerBootstrap**: 启动器，简化服务端和客户端的配置与启动

## 成功标准 *(必填)*

### 可衡量的结果

- **SC-001**: 学习者能够在30分钟内理解每个迭代分支的核心概念
- **SC-002**: 每个迭代分支都能独立编译运行，测试覆盖率达到80%以上
- **SC-003**: 服务端能够稳定处理100个并发客户端连接
- **SC-004**: Echo服务端响应延迟小于10ms（本地环回）
- **SC-005**: changelog.md文档清晰记录每次迭代的改动点和设计思路
- **SC-006**: 代码风格与mini-spring保持一致，便于对照学习

## 假设与约束

### 假设

- 学习者具备基本的Java编程能力和网络编程概念
- 学习者可以参考mini-spring的学习路径理解迭代式开发
- 开发环境支持Java 17及以上版本

### 约束

- 代码必须极度简化，只保留核心功能，便于理解
- 不实现Netty的所有特性，聚焦于核心机制
- 每个迭代分支必须可独立运行和验证

## 迭代计划 *(参照mini-spring)*

### 基础篇：网络通信基础

1. **simple-bio-server** - 最简单的BIO服务端，使用ServerSocket阻塞接收连接
2. **simple-bio-client** - 配套的BIO客户端，实现基本的消息收发
3. **multi-thread-bio-server** - 多线程BIO服务端，每个连接一个线程处理
4. **nio-channel-buffer** - 引入NIO的Channel和Buffer概念
5. **nio-selector** - 引入Selector，实现单线程多路复用
6. **nio-server-accept** - NIO服务端接受连接，处理ACCEPT事件
7. **nio-server-read-write** - NIO服务端读写数据，处理READ/WRITE事件

### 基础篇：EventLoop事件循环

8. **event-loop-interface** - 定义EventLoop接口和基本抽象
9. **single-thread-event-loop** - 实现单线程事件循环，封装Selector操作
10. **event-loop-task-queue** - 为EventLoop添加任务队列，支持异步任务提交
11. **event-loop-scheduled-task** - 支持定时任务和延迟任务

### 基础篇：Channel抽象

12. **channel-interface** - 定义Channel接口，抽象网络连接
13. **nio-channel-impl** - 实现NioServerSocketChannel和NioSocketChannel
14. **channel-config** - 实现ChannelConfig，支持Channel配置
15. **channel-unsafe** - 实现Channel.Unsafe，封装底层I/O操作

### 基础篇：ChannelHandler处理链

16. **channel-handler-interface** - 定义ChannelHandler接口
17. **channel-pipeline-basic** - 实现ChannelPipeline基本结构（双向链表）
18. **channel-handler-context** - 实现ChannelHandlerContext，连接Handler和Pipeline
19. **inbound-handler** - 实现ChannelInboundHandler，处理入站事件
20. **outbound-handler** - 实现ChannelOutboundHandler，处理出站事件
21. **handler-adapter** - 提供ChannelHandlerAdapter简化Handler实现

### 扩展篇：ByteBuf缓冲区

22. **byte-buf-interface** - 定义ByteBuf接口，读写索引分离
23. **heap-byte-buf** - 实现HeapByteBuf，基于字节数组
24. **byte-buf-reference-count** - 实现引用计数（retain/release），理解内存管理
25. **byte-buf-allocator** - 实现ByteBufAllocator，统一缓冲区分配

### 扩展篇：编解码器

26. **byte-to-message-decoder** - 实现ByteToMessageDecoder抽象类
27. **fixed-length-decoder** - 实现固定长度解码器
28. **length-field-decoder** - 实现长度字段解码器，解决粘包拆包
29. **string-codec** - 实现StringEncoder和StringDecoder

### 扩展篇：Bootstrap启动器

30. **abstract-bootstrap** - 实现AbstractBootstrap基类
31. **server-bootstrap** - 实现ServerBootstrap，流式API配置服务端
32. **client-bootstrap** - 实现Bootstrap，流式API配置客户端

### 扩展篇：多线程模型

33. **event-loop-group** - 实现EventLoopGroup，管理多个EventLoop
34. **boss-worker-model** - 实现主从Reactor模型，Boss接受连接，Worker处理I/O
35. **channel-chooser** - 实现负载均衡策略，将Channel分配给EventLoop

### 扩展篇：实用Handler

36. **idle-state-handler** - 实现空闲状态检测Handler
37. **logging-handler** - 实现日志Handler，用于调试

## Clarifications

### Session 2025-12-16

- Q: 迭代计划的拆分粒度？ → A: 细粒度拆分 (20-25个分支)，每个分支只引入1个核心概念
- Q: 每个分支的验证方式？ → A: 单元测试 + 集成测试，组件逻辑验证 + 服务端/客户端实际通信验证
- Q: 包结构和命名规范？ → A: 对齐Netty包结构，使用 io.netty.channel、io.netty.buffer、io.netty.bootstrap 等包名
- Q: 异常处理策略？ → A: Pipeline异常传播，由Handler的exceptionCaught方法处理，未处理则关闭Channel
- Q: 内存管理策略？ → A: 实现引用计数（retain/release）帮助理解内存管理，但跳过池化实现
- Q: 分支管理策略？ → A: 每个迭代都新建独立的git分支，分支名与迭代名称一致（如 simple-bio-server, nio-selector 等）
- Q: 项目目录结构？ → A: 使用仓库根目录作为项目目录，无需创建mini-netty子目录


