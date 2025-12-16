# Changelog

本文件记录 Mini-Netty 项目的所有改动，按迭代分支组织。

## 格式说明

每个迭代记录包含：
- **分支名**: 对应的 Git 分支
- **日期**: 完成日期
- **改动内容**: 本次迭代的具体改动
- **学习要点**: 本次迭代的核心知识点

---

## [Unreleased]

### 项目初始化

**日期**: 2025-12-16

**改动内容**:
- 创建项目基础结构
- 配置 Maven 构建（Java 17, JUnit 5, AssertJ）
- 创建 README.md 说明项目目的和学习路径
- 创建 changelog.md 记录改动

**学习要点**:
- Maven 项目结构
- JUnit 5 测试框架配置

---

## [IT06] nio-server-accept

**分支**: `nio-server-accept`
**日期**: 2025-12-16

**改动内容**:
- 新增 `NioServer` 类 (`io.netty.example.nio.NioServer`)
  - 实现基于 NIO 的服务端
  - 处理 OP_ACCEPT 事件
  - 使用 Selector 事件循环
  - 支持后台启动和优雅停止
- 新增 `NioServerAcceptTest` 测试类
  - 测试服务端启动/停止
  - 测试接受单个和多个客户端连接
  - 测试客户端 Channel 注册 READ 事件

**学习要点**:
- ServerSocketChannel 必须配置为非阻塞模式
- 注册 OP_ACCEPT 事件监听新连接
- accept() 返回的 SocketChannel 也要配置为非阻塞
- 新连接的 Channel 注册 OP_READ 准备接收数据
- wakeup() 用于优雅停止服务端

---

## [IT05] nio-selector

**分支**: `nio-selector`
**日期**: 2025-12-16

**改动内容**:
- 新增 `NioSelectorDemo` 类 (`io.netty.example.nio.NioSelectorDemo`)
  - 演示 Selector 的基本用法
  - 演示多 Channel 注册
  - 演示完整的事件循环
- 新增 `NioSelectorTest` 测试类
  - 测试 Selector 创建和关闭
  - 测试 Channel 注册和事件监听
  - 测试 select(), selectNow(), wakeup() 方法

**学习要点**:
- Selector 是 NIO 多路复用的核心
- Channel 必须是非阻塞模式才能注册到 Selector
- 四种事件类型：OP_ACCEPT(16), OP_CONNECT(8), OP_READ(1), OP_WRITE(4)
- `select()` 阻塞等待事件，`selectNow()` 不阻塞
- `wakeup()` 可以唤醒阻塞的 select()
- 处理完 SelectionKey 后必须从 selectedKeys 中移除

---

## [IT04] nio-channel-buffer

**分支**: `nio-channel-buffer`
**日期**: 2025-12-16

**改动内容**:
- 新增 `NioChannelBufferDemo` 类 (`io.netty.example.nio.NioChannelBufferDemo`)
  - 演示 ByteBuffer 的基本操作
  - 演示 FileChannel 的读写
  - 演示 compact() 和直接缓冲区
- 新增 `NioChannelBufferTest` 测试类
  - 测试 ByteBuffer 的 allocate, flip, clear, compact 等操作
  - 测试 FileChannel 的读写
  - 测试 wrap() 和 slice() 方法

**学习要点**:
- Buffer 的三个关键属性：capacity、position、limit
- `flip()` 切换读模式：position→0, limit→原position
- `clear()` 切换写模式：position→0, limit→capacity
- `compact()` 保留未读数据并切换写模式
- 直接缓冲区 (`allocateDirect`) vs 堆缓冲区 (`allocate`)
- Channel 总是从 Buffer 读/写数据

---

## [IT03] multi-thread-bio-server

**分支**: `multi-thread-bio-server`
**日期**: 2025-12-16

**改动内容**:
- 新增 `MultiThreadBioServer` 类 (`io.netty.example.bio.MultiThreadBioServer`)
  - 使用 `ExecutorService` 线程池处理并发连接
  - 支持配置线程池大小
  - 使用 `AtomicInteger` 追踪活跃连接数
- 新增 `ConcurrentClientTest` 并发测试类
  - 测试多客户端同时连接
  - 测试并发消息收发
  - 验收场景：多个客户端同时连接，每个都能正常收发消息

**学习要点**:
- `ExecutorService` 线程池的使用
- `CountDownLatch` 用于同步多线程测试
- BIO + 线程池模型：解决单线程无法并发的问题
- 线程池大小决定最大并发连接数
- 每个连接占用一个线程，资源消耗较大

---

## [IT02] simple-bio-client

**分支**: `simple-bio-client`
**日期**: 2025-12-16

**改动内容**:
- 新增 `SimpleBioClient` 类 (`io.netty.example.bio.SimpleBioClient`)
  - 实现阻塞式 I/O 客户端
  - 支持 `connect()`, `sendAndReceive()`, `close()` 方法
  - 实现 `AutoCloseable` 接口，支持 try-with-resources
- 新增 `ClientServerIntegrationTest` 集成测试类
  - 测试客户端连接服务端
  - 测试消息发送和接收
  - 验收场景：客户端发送"hello"，收到"hello, mini-netty"

**学习要点**:
- `Socket` 构造函数会自动发起连接（阻塞操作）
- 使用 `AutoCloseable` 接口支持资源自动释放
- 集成测试验证客户端和服务端的协作
- BIO 模式下单线程服务端只能顺序处理客户端请求

---

## [IT01] simple-bio-server

**分支**: `simple-bio-server`
**日期**: 2025-12-16

**改动内容**:
- 新增 `SimpleBioServer` 类 (`io.netty.example.bio.SimpleBioServer`)
  - 实现最简单的阻塞式 I/O 服务端
  - 支持单客户端连接处理
  - 提供 `start()`, `stop()`, `startInBackground()` 方法
- 新增 `SimpleBioServerTest` 测试类
  - 测试服务端启动/停止
  - 测试客户端消息收发
  - 测试客户端断开连接处理

**学习要点**:
- `ServerSocket` 用于监听端口，`accept()` 方法阻塞等待连接
- `Socket` 代表一个 TCP 连接
- 阻塞 I/O 的特点：一个线程只能处理一个连接
- 使用 `BufferedReader` 和 `PrintWriter` 简化文本消息收发

---

*后续迭代记录将添加在此处*
