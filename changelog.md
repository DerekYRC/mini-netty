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

## [IT23] heap-byte-buf

**分支**: `heap-byte-buf`
**日期**: 2025-12-17

**改动内容**:
- 新增 `AbstractByteBuf` 抽象基类
  - 实现读写索引管理
  - 边界检查和自动扩容
  - 顺序读写方法的通用实现
  - 标记/重置和 discardReadBytes
- 新增 `HeapByteBuf` 堆内存实现
  - 基于 byte[] 的存储
  - 大端字节序编码
  - 基础引用计数实现
- 新增 `HeapByteBufTest` 共 28 个测试用例
  - BasicPropertyTests: 基础属性测试
  - IndexTests: 读写索引测试
  - SequentialReadWriteTests: 顺序读写测试
  - RandomAccessTests: 随机访问测试
  - CapacityTests: 容量和扩容测试
  - NioConversionTests: NIO ByteBuffer 转换测试
  - StringConversionTests: 字符串转换测试
  - ReferenceCountTests: 引用计数测试
  - DiscardReadBytesTests: 缓冲区压缩测试
  - AcceptanceScenarioTests: 网络消息和字节序验证

**学习要点**:
- 堆内存 vs 直接内存的权衡
- 大端字节序（网络字节序）编码
- 自动扩容策略（翻倍增长到 4MB，然后线性增长）

---

## [IT22] bytebuf-interface

**分支**: `bytebuf-interface`
**日期**: 2025-12-17

**改动内容**:
- 新增 `ReferenceCounted` 接口
  - 定义引用计数管理方法 refCnt(), retain(), release()
  - 支持增量 retain/release 操作
- 新增 `ByteBuf` 抽象类
  - 读写索引分离设计（readerIndex/writerIndex）
  - 支持随机访问（get/set 方法）
  - 支持顺序读写（read/write 方法）
  - 标记和重置功能
  - NIO ByteBuffer 转换

**学习要点**:
- 引用计数内存管理，避免 GC 开销
- 读写索引分离比 JDK ByteBuffer 的 flip() 更直观
- ByteBuf 索引布局：discardable | readable | writable

---

## [IT21] handler-adapter

**分支**: `handler-adapter`
**日期**: 2025-12-17

**改动内容**:
- 新增 `ChannelDuplexHandler` 双向 Handler 适配器
  - 同时实现 ChannelInboundHandler 和 ChannelOutboundHandler
  - 可以同时处理入站和出站事件
  - 典型用途：日志记录、监控统计、超时处理
- 验证异常传播机制
  - exceptionCaught 沿入站方向传递
  - 可以在任意 Handler 中拦截和处理异常
- 新增 `PipelineIntegrationTest` 共 10 个测试用例
  - DuplexHandlerTests: 双向 Handler 功能测试
  - ExceptionPropagationTests: 异常传播和拦截测试
  - FullEventFlowTests: 完整事件流测试
  - AcceptanceScenarioTests: Echo 服务器和异常处理链场景

**学习要点**:
- ChannelDuplexHandler 简化双向流量处理
- 异常沿入站方向传播（HeadContext → TailContext）
- 异常可以在任意 Handler 被拦截和处理

---

## [IT20] outbound-handler

**分支**: `outbound-handler`
**日期**: 2025-12-17

**改动内容**:
- 新增 `ChannelOutboundHandlerAdapter` 适配器类
  - 提供所有出站方法的默认实现
  - 默认行为是将操作传递给下一个 Handler
  - bind/connect/disconnect 暂时直接完成 Promise（ChannelHandlerContext 尚未支持这些方法）
  - write/flush/close/read 通过 Context 传递
- 验证出站操作机制
  - write, flush, close, read 操作通过 Context 触发
  - 出站事件从尾部向头部传递
- 新增 `OutboundHandlerTest` 共 10 个测试用例
  - AdapterTests: 验证 Adapter 默认实现和生命周期
  - OutboundOperationsTests: write/flush/close/read 操作测试
  - HandlerChainTests: Handler 链反向传递测试
  - AcceptanceScenarioTests: 典型编码器场景测试

**学习要点**:
- 出站事件传播方向：从尾部向头部（与入站相反）
- ChannelOutboundHandlerAdapter 简化出站处理器开发
- 编码器通常是出站 Handler 的典型应用

---

## [IT19] inbound-handler

**分支**: `inbound-handler`
**日期**: 2025-12-17

**改动内容**:
- 新增 `ChannelInboundHandlerAdapter` 适配器类
  - 提供所有入站方法的默认实现
  - 默认行为是将事件传递给下一个 Handler
  - 用户只需覆盖关心的方法
- 验证入站事件传递机制
  - channelRegistered, channelActive, channelRead 等事件按顺序传递
  - 支持拦截和修改消息后继续传递
- 新增 `InboundHandlerTest` 共 12 个测试用例
  - AdapterTests: 验证 Adapter 默认实现
  - InboundEventPropagationTests: 入站事件传递测试
  - EventInterceptionTests: 事件拦截和消息转换测试
  - AcceptanceScenarioTests: 典型消息处理和生命周期测试

**学习要点**:
- 适配器模式：提供默认实现，简化用户代码
- 事件传递控制：调用或不调用 super 方法决定是否传递
- 消息转换：在传递前可以修改消息内容
- Channel 生命周期：registered → active → read → inactive → unregistered

---

## [IT18] channel-handler-context

**分支**: `channel-handler-context`
**日期**: 2025-12-17

**改动内容**:
- 验证 `ChannelHandlerContext` 接口
  - 提供 Handler 与 Pipeline 交互的桥梁
  - 入站事件传播：fireChannelRegistered, fireChannelActive, fireChannelRead 等
  - 出站操作：write, flush, close, read
  - 创建 Promise：newPromise
- 验证 `AbstractChannelHandlerContext` 实现
  - 双向链表节点（prev, next）
  - 查找下一个入站/出站 Handler
  - 事件传递逻辑
- 验证 `HeadContext` 和 `TailContext`
  - HeadContext: 出站操作的最终执行点
  - TailContext: 入站事件的终点，处理未消费的消息和异常
- 新增 `ChannelHandlerContextTest` 共 11 个测试用例
  - ContextBasicPropertiesTests: Context 基本属性测试
  - EventPropagationTests: 事件传递测试
  - ChainPropagationTests: 链式传递测试
  - AcceptanceScenarioTests: 完整场景测试

**说明**: ChannelHandlerContext 相关实现已在 IT08 中预先创建，本迭代添加测试并确认其完整性。

**学习要点**:
- Context 作为 Handler 和 Pipeline 的桥梁
- 事件传播：从当前节点向下一个节点传递
- 入站事件从 Head 到 Tail，出站事件从 Tail 到 Head
- 可以选择停止传播或继续传递事件

---

## [IT17] channel-pipeline-basic

**分支**: `channel-pipeline-basic`
**日期**: 2025-12-17

**改动内容**:
- 验证 `ChannelPipeline` 接口
  - Handler 管理：addFirst, addLast, remove, get, context
  - 入站事件触发：fireChannelRegistered, fireChannelActive, fireChannelRead 等
  - 出站操作：read
- 验证 `DefaultChannelPipeline` 实现
  - 双向链表结构存储 Handler 链
  - HeadContext 处理出站操作的最终执行
  - TailContext 作为入站事件的终点
  - Handler 生命周期管理（handlerAdded, handlerRemoved）
- 新增 `ChannelPipelineTest` 共 20 个测试用例
  - PipelineStructureTests: Pipeline 结构测试
  - HandlerManagementTests: Handler 管理测试
  - HandlerLifecycleTests: Handler 生命周期测试
  - InboundEventPropagationTests: 入站事件传递测试
  - AcceptanceScenarioTests: 完整场景测试

**说明**: ChannelPipeline 和 DefaultChannelPipeline 已在 IT08 中预先创建，本迭代添加测试并确认其完整性。

**学习要点**:
- 责任链模式：事件按顺序通过 Handler 链传递
- 双向链表：高效的添加/删除操作
- Head/Tail 节点：封装 I/O 操作的起点和终点
- 入站/出站事件分离：入站从 Head 到 Tail，出站从 Tail 到 Head

---

## [IT16] channel-handler-interface

**分支**: `channel-handler-interface`
**日期**: 2025-12-17

**改动内容**:
- 验证 `ChannelHandler` 基接口
  - 定义 Handler 生命周期方法：handlerAdded, handlerRemoved
  - 作为所有事件处理器的基类
- 验证 `ChannelInboundHandler` 入站事件处理器接口
  - 入站事件：channelRegistered, channelUnregistered, channelActive, channelInactive
  - 数据事件：channelRead, channelReadComplete
  - 异常处理：exceptionCaught
- 验证 `ChannelOutboundHandler` 出站事件处理器接口
  - 连接管理：bind, connect, disconnect, close
  - 数据操作：write, flush, read

**说明**: 这些接口已在 IT08 (event-loop-interface) 中预先创建，本迭代确认其完整性。

**学习要点**:
- 入站/出站事件分离：职责清晰，便于理解
- 事件驱动模型：每个方法对应一种事件类型
- Handler 生命周期：添加和移除时的初始化/清理

---

## [IT15] channel-unsafe

**分支**: `channel-unsafe`
**日期**: 2025-12-17

**改动内容**:
- 新增 `Channel.Unsafe` 内部接口
  - 定义底层 I/O 操作：register, bind, connect, disconnect, close, beginRead, write, flush
  - 这些操作不应该直接暴露给用户代码
- 新增 `AbstractUnsafe` 在 AbstractChannel 中
  - 实现所有 Unsafe 方法的模板逻辑
  - 确保所有操作在 EventLoop 线程中执行（线程安全）
  - 使用 Promise 模式进行异步通知
  - 调用抽象的 doXxx 方法让子类实现具体逻辑
- 新增 `AbstractNioUnsafe` 在 AbstractNioChannel 中
  - NIO 特定的 Unsafe 实现基类
- 更新 `NioServerSocketChannel` 和 `NioSocketChannel`
  - 实现 `newUnsafe()` 方法
  - 添加 `doBind()` 实现端口绑定
  - NioSocketChannel 添加 `doConnect()` 实现连接逻辑
- 修复 `SingleThreadEventLoop.execute()` 自动启动问题
  - 添加 `startIfNeeded()` 方法
  - 确保提交任务时 EventLoop 自动启动

**测试**:
- 新增 `ChannelUnsafeTest` 共 13 个测试用例
  - UnsafeInterfaceTests: 验证接口定义
  - ServerChannelUnsafeTests: 服务端 Unsafe 操作
  - SocketChannelUnsafeTests: 客户端 Unsafe 操作
  - PromiseCallbackTests: Promise 回调机制
  - AcceptanceScenarioTests: 完整场景测试

**学习要点**:
- Unsafe 接口设计：封装底层操作，保护用户代码
- 模板方法模式：doXxx 方法由子类实现具体逻辑
- EventLoop 线程安全：所有操作通过 inEventLoop() 检查
- Promise 异步模式：操作完成时通知调用者

---

## [IT14] channel-config

**分支**: `channel-config`
**日期**: 2025-12-17

**改动内容**:
- 新增 `ChannelOption<T>` 类型安全的配置选项键
  - 预定义常用选项：SO_RCVBUF, SO_SNDBUF, TCP_NODELAY, SO_KEEPALIVE 等
  - 支持自定义选项扩展
- 新增 `ChannelConfig` 接口
  - 提供类型安全的选项获取/设置方法
  - 定义常用配置：连接超时、写缓冲区水位线、自动读取等
- 新增 `DefaultChannelConfig` 实现
  - 使用 ConcurrentHashMap 存储配置
  - 对常用选项使用直接字段存储（性能优化）
- 更新 `Channel` 接口
  - 添加 `config()` 方法返回 ChannelConfig
  - 添加 `read()` 方法请求读取数据
- 更新 `AbstractChannel`
  - 添加 config 字段和 newChannelConfig() 工厂方法
  - 实现 config() 和 read() 方法
- 更新 `ChannelPipeline` 接口
  - 添加 read() 出站操作方法
- 更新 `ChannelHandlerContext` 接口
  - 添加 read() 方法用于出站事件传播
- 更新 `AbstractChannelHandlerContext`
  - 实现 read() 方法，向前查找出站处理器并调用
- 更新 `DefaultChannelPipeline`
  - 实现 read() 方法，从 tail 开始传播

**学习要点**:
- **类型安全的配置设计**: ChannelOption<T> 使用泛型确保配置值类型正确
- **配置模式**: 区分 Socket 选项和应用级别选项
- **性能优化**: 常用选项使用直接字段避免 Map 查找开销
- **流量控制**: 写缓冲区水位线用于背压控制
- **自动读取**: autoRead 选项控制是否自动触发读操作

---

## [IT13] nio-channel-impl

**分支**: `nio-channel-impl`
**日期**: 2025-12-17

**改动内容**:
- 新增 `AbstractChannel` 抽象类
  - 提供 Channel 生命周期管理
  - 管理 Pipeline、EventLoop 注册
  - 实现 DefaultChannelId 内部类
- 新增 `AbstractNioChannel` 抽象类
  - 封装 NIO SelectableChannel 管理
  - 处理 Selector 注册和 SelectionKey 操作
- 新增 `NioServerSocketChannel` 服务端实现
  - 封装 ServerSocketChannel
  - 实现 bind() 和 accept() 操作
- 新增 `NioSocketChannel` 客户端/连接实现
  - 封装 SocketChannel
  - 实现 connect()、read()、write() 操作
- 新增 `DefaultChannelFuture` 和 `DefaultChannelPromise` 实现
- 新增 `DefaultChannelPipeline` 双向链表实现
  - HeadContext 和 TailContext 作为边界节点
- 新增 `AbstractChannelHandlerContext` 链表节点基类
- 更新接口：ChannelPipeline、ChannelHandlerContext、ChannelFuture、ChannelId
- 新增 `NioChannelTest` 测试 14 个测试用例

**学习要点**:
- Channel 抽象层次：Channel → AbstractChannel → AbstractNioChannel → NioXxxChannel
- Pipeline 是双向链表，入站从头到尾，出站从尾到头
- SelectableChannel 必须设置为非阻塞模式
- 每个 Channel 有唯一的 ChannelId 和专属的 Pipeline

---

## [IT11] event-loop-scheduled-task

**分支**: `event-loop-scheduled-task`
**日期**: 2025-12-17

**改动内容**:
- 新增 `ScheduledTask` 类实现 `ScheduledFuture<Void>` 接口
  - 支持一次性定时任务和周期性任务
  - 使用 `System.nanoTime()` 计算执行时间
  - 实现 `Delayed` 接口用于优先级队列排序
  - 支持任务取消和状态查询
- 更新 `SingleThreadEventLoop` 添加定时任务队列
  - 添加 `scheduledTaskQueue` 优先级队列
  - 实现 `schedule()` 延迟任务调度
  - 实现 `scheduleAtFixedRate()` 周期性任务调度
  - 更新 `runAllTasks()` 处理到期的定时任务
  - 添加 `scheduleFromEventLoop()` 用于周期任务重新调度
- 优化 `NioEventLoop.select()` 方法
  - 根据下一个定时任务延迟动态调整 select 超时时间
  - 避免定时任务因 select 阻塞而延迟执行
- 新增 `ScheduledTaskTest` 测试 17 个测试用例
  - 延迟任务执行测试
  - 周期性任务测试
  - 任务取消测试
  - ScheduledFuture 状态测试
  - 验收场景测试

**学习要点**:
- `PriorityQueue` 按截止时间排序定时任务
- `System.nanoTime()` 适合计算时间间隔（不受系统时钟调整影响）
- 周期性任务通过重新加入队列实现
- Selector 超时应与最近定时任务对齐，减少延迟

---

## [IT10] event-loop-task-queue

**分支**: `event-loop-task-queue`
**日期**: 2025-12-17

**改动内容**:
- 新增 `TaskQueueTest` 测试 12 个测试用例
  - 测试 execute() 方法基本功能
  - 测试 FIFO 执行顺序
  - 测试多线程并发提交任务
  - 测试异常处理（不影响后续任务）
  - 测试性能（1000 任务 ~4ms）

**学习要点**:
- execute() 方法将任务添加到 ConcurrentLinkedQueue
- 任务按 FIFO 顺序执行
- 任务异常不会中断事件循环，后续任务继续执行
- wakeup() 确保从其他线程提交的任务能立即被处理
- hasTasks() 用于检查是否有待处理任务

---

## [IT09] single-thread-event-loop

**分支**: `single-thread-event-loop`
**日期**: 2025-12-17

**改动内容**:
- 新增 `SingleThreadEventLoop` 抽象类 (`io.netty.channel.SingleThreadEventLoop`)
  - 实现 EventLoop 接口的基础功能
  - 使用 ConcurrentLinkedQueue 作为任务队列
  - 实现 `execute()`, `inEventLoop()` 方法
  - 提供 `start()`, `shutdownGracefully()` 生命周期方法
- 新增 `NioEventLoop` 类 (`io.netty.channel.nio.NioEventLoop`)
  - 基于 NIO Selector 的事件循环实现
  - 实现事件选择和处理逻辑
  - 集成任务执行和 I/O 事件处理
- 新增 `NioEventLoopTest` 测试 19 个测试用例
  - 测试启动/停止生命周期
  - 测试 inEventLoop() 线程判断
  - 测试任务执行顺序和线程安全

**学习要点**:
- SingleThreadEventLoop 保证所有操作在单线程执行
- ConcurrentLinkedQueue 用于线程安全的任务提交
- wakeup() 用于唤醒阻塞的 Selector
- 事件循环三步骤：select() → processSelectedKeys() → runAllTasks()
- inEventLoop() 通过 Thread 引用比较实现

---

## [IT08] event-loop-interface

**分支**: `event-loop-interface`
**日期**: 2025-12-16

**改动内容**:
- 新增 `EventLoopGroup` 接口 (`io.netty.channel.EventLoopGroup`)
  - 定义 `next()`, `register()`, `shutdownGracefully()` 方法
  - 管理一组 EventLoop 实例
- 新增 `EventLoop` 接口 (`io.netty.channel.EventLoop`)
  - 继承 EventLoopGroup（一个 EventLoop 也是只有自己的 Group）
  - 定义 `inEventLoop()`, `execute()`, `schedule()` 方法
- 新增基础接口定义
  - `Channel` - 网络通道抽象
  - `ChannelId` - Channel 唯一标识
  - `ChannelFuture` - 异步操作结果
  - `ChannelFutureListener` - 操作完成监听器
  - `ChannelPromise` - 可写入的 Future
  - `ChannelHandler` - 事件处理器基接口
  - `ChannelInboundHandler` - 入站事件处理器
  - `ChannelOutboundHandler` - 出站事件处理器
  - `ChannelPipeline` - Handler 容器
  - `ChannelHandlerContext` - Handler 上下文
- 新增 `EventLoopInterfaceTest` 测试 26 个测试用例

**学习要点**:
- EventLoop 是 Netty 的核心调度器，单线程处理 I/O 事件
- EventLoop 继承 EventLoopGroup 是设计特点（自身也是 Group）
- `inEventLoop()` 用于判断是否在 EventLoop 线程中执行
- `execute()` 用于提交任务，`schedule()` 用于定时任务
- ChannelFuture 是异步编程的核心，推荐使用 addListener()

---

## [IT07] nio-server-read-write

**分支**: `nio-server-read-write`
**日期**: 2025-12-16

**改动内容**:
- 更新 `NioServer` 类 (`io.netty.example.nio.NioServer`)
  - 实现 OP_READ 事件处理（`handleRead`）
  - 实现 OP_WRITE 事件处理（`handleWrite`）
  - 通过 `SelectionKey.attach()` 传递响应数据
  - 完整的客户端通信流程
- 新增 `NioClient` 类 (`io.netty.example.nio.NioClient`)
  - 基于 SocketChannel 的 NIO 客户端
  - 支持连接、发送、接收消息
  - 实现 AutoCloseable 接口
- 新增 `NioClientServerTest` 集成测试
  - 测试单客户端连接和通信
  - 测试多消息发送
  - 测试多客户端并发访问
  - 验收场景测试

**学习要点**:
- READ 事件：客户端发送数据触发，读取 ByteBuffer 并解析消息
- WRITE 事件：通过修改 interestOps 切换为 OP_WRITE
- SelectionKey.attach()/attachment() 用于在事件间传递数据
- ByteBuffer 读写需要正确调用 flip() 方法
- 客户端使用 SocketChannel.open() 和 connect()
- 消息协议使用换行符分隔

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
