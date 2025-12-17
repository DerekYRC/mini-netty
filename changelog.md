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

## [IT36] idle-state-handler

**分支**: `idle-state-handler`
**日期**: 2025-12-17

**改动内容**:
- 新增 `IdleState` 枚举定义空闲状态类型
  - READER_IDLE: 读空闲
  - WRITER_IDLE: 写空闲
  - ALL_IDLE: 读写都空闲
- 新增 `IdleStateEvent` 空闲状态事件类
  - 预定义静态实例 (FIRST_READER_IDLE_STATE_EVENT, READER_IDLE_STATE_EVENT 等)
  - state() 获取空闲类型, isFirst() 判断是否首次空闲
- 新增 `IdleStateHandler` 空闲状态处理器
  - 配置读空闲超时、写空闲超时、全部空闲超时
  - 使用 EventLoop.schedule() 实现定时检测
  - 内部类 ReaderIdleTimeoutTask, WriterIdleTimeoutTask, AllIdleTimeoutTask
  - 在 channelActive 时初始化定时器
  - 在 channelInactive/handlerRemoved 时销毁定时器
- 扩展 `ChannelInboundHandler` 接口
  - 新增 userEventTriggered(ctx, evt) 方法
- 扩展 `ChannelHandlerContext` 接口
  - 新增 fireUserEventTriggered(evt) 方法
- 更新所有 ChannelInboundHandler 实现添加 userEventTriggered
- 新增 `IdleStateHandlerTest` 共 24 个测试用例

**学习要点**:
- 定时任务与超时检测机制
- 用户事件 (User Event) 扩展机制
- 心跳检测在网络编程中的应用
- 策略模式在超时处理中的应用

---

## [IT35] channel-chooser

**分支**: `channel-chooser`
**日期**: 2025-12-17

**改动内容**:
- 新增 `EventLoopChooser` 接口定义 EventLoop 选择策略
- 新增 `EventLoopChooserFactory` 工厂接口
- 新增 `RoundRobinEventLoopChooser` 轮询选择器
  - 使用 AtomicInteger 保证线程安全
  - 使用取模运算实现循环
- 新增 `PowerOfTwoEventLoopChooser` 优化的 2 的幂选择器
  - 当 EventLoop 数量是 2 的幂时使用位运算代替取模
  - `isPowerOfTwo()` 静态方法判断是否是 2 的幂
- 新增 `DefaultEventLoopChooserFactory` 默认工厂
  - 根据 EventLoop 数量自动选择最优策略
  - 使用单例模式 (INSTANCE)
- 新增 `ChannelChooserTest` 共 14 个测试用例
- 修复 `ServerBootstrapTest` 中 TestEventLoopGroup 的 shutdownGracefully 实现

**学习要点**:
- 策略模式：将选择算法封装为独立的类
- 工厂模式：封装对象创建逻辑
- 位运算优化：`n & (n-1) == 0` 判断 2 的幂
- 位运算代替取模：`index & (length-1)` 等价于 `index % length`（当 length 是 2 的幂时）

---

## [IT34] boss-worker-model

**分支**: `boss-worker-model`
**日期**: 2025-12-17

**改动内容**:
- 新增 `BossWorkerModelTest` 集成测试 10 个测试用例
  - ConfigurationTests: Boss/Worker 配置、绑定、默认值
  - ThreadModelTests: 线程组隔离、多线程支持、轮询分配
  - LifecycleTests: 优雅关闭、关闭后状态
  - AcceptanceScenarioTests: 完整配置场景

**学习要点**:
- 主从 Reactor 模型：Boss 处理连接，Worker 处理 I/O
- Boss 通常使用 1 个线程接受连接
- Worker 使用多个线程（CPU 核心数 × 2）处理 I/O
- EventLoopGroup 隔离确保 Boss 和 Worker 独立运行

---

## [IT33] event-loop-group

**分支**: `event-loop-group`
**日期**: 2025-12-17

**改动内容**:
- 新增 `NioEventLoopGroup` 事件循环组实现
  - 管理多个 NioEventLoop 实例
  - 默认线程数为 CPU 核心数 × 2
  - 支持轮询(Round-Robin)策略分配 EventLoop
  - `next()` 返回下一个 EventLoop
  - `eventLoop(index)` 按索引获取 EventLoop
  - `register(Channel)` 注册 Channel 到组
  - `shutdownGracefully()` 优雅关闭所有 EventLoop
  - `start()` 启动所有 EventLoop
- 新增 `NioEventLoopGroupTest` 共 10 个测试用例
  - 创建测试: 默认线程数、指定线程数、边界条件
  - 轮询测试: 循环分配、均匀分布
  - 索引访问测试: 按索引获取、越界异常
  - 生命周期测试: isShutdown 状态
  - 验收场景测试: 主从 Reactor 线程组

**学习要点**:
- EventLoopGroup 是 EventLoop 的容器和管理者
- 轮询策略(Round-Robin)保证负载均衡
- Channel 一旦分配到 EventLoop，整个生命周期内不会改变
- 主从 Reactor 模型：Boss 处理连接，Worker 处理 I/O

---

## [IT31] server-bootstrap

**分支**: `server-bootstrap`
**日期**: 2025-12-17

**改动内容**:
- 新增 `ServerBootstrap` 服务端启动器
  - 继承自 AbstractBootstrap，专用于服务端配置
  - group(parentGroup, childGroup) 支持主从 Reactor 模型
  - childHandler() 设置子 Channel 处理器
  - childOption() 设置子 Channel 选项
  - childAttr() 设置子 Channel 属性
- 内置 `ServerBootstrapAcceptor` 连接接受器
  - 自动将新连接注册到 childGroup
  - 自动为子 Channel 配置 childHandler
  - 自动应用 childOption 配置
- 新增 `ServerBootstrapTest` 共 24 个测试用例
  - ConfigurationTests: 链式配置、克隆测试
  - ValidationTests: 参数验证、默认值处理
  - NullValidationTests: 空值检查
  - DuplicateSettingsTests: 重复设置检查
  - BindTests: 端口绑定测试

**学习要点**:
- ServerBootstrap 实现主从 Reactor 模型
- Boss 线程负责 accept，Worker 线程负责 read/write
- ServerBootstrapAcceptor 是连接分发的核心

---

## [IT30] abstract-bootstrap

**分支**: `abstract-bootstrap`
**日期**: 2025-12-17

**改动内容**:
- 新增 `AbstractBootstrap<B, C>` 启动器抽象基类
  - 流式 API (Builder 模式) 配置
  - group() 设置 EventLoopGroup
  - channel() 设置 Channel 类型
  - option() 设置 Channel 选项
  - handler() 设置 ChannelHandler
  - bind() 绑定到本地地址
- 新增 `ChannelInitializer<C>` Channel 初始化器
  - 在 Channel 注册后初始化 Pipeline
  - 初始化完成后自动从 Pipeline 移除
  - 抽象 initChannel() 方法供子类实现
- 内置 ReflectiveChannelFactory 反射工厂

**学习要点**:
- AbstractBootstrap 是 ServerBootstrap 和 Bootstrap 的基类
- 流式 API 使配置代码简洁易读
- ChannelInitializer 用于配置新连接的处理器链

---

## [IT29] string-codec

**分支**: `string-codec`
**日期**: 2025-12-17

**改动内容**:
- 新增 `MessageToByteEncoder<I>` 消息到字节编码器基类
  - 泛型支持，自动类型匹配
  - 输出缓冲区自动分配
- 新增 `EncoderException` 编码异常类
- 新增 `StringDecoder` 字符串解码器
  - 将 ByteBuf 解码为 String
  - 支持自定义字符编码
  - 自动释放输入 ByteBuf
- 新增 `StringEncoder` 字符串编码器
  - 将 CharSequence 编码为 ByteBuf
  - 支持自定义字符编码
- 新增 `StringCodecTest` 共 14 个测试用例
  - StringDecoderTests: UTF-8/中文/ByteBuf释放/非ByteBuf透传
  - StringEncoderTests: UTF-8/中文/空字符串编码
  - CombinedCodecTests: 与帧解码器配合使用
  - AcceptanceScenarioTests: 聊天消息和RPC响应场景

**检查点 (US5)**: 编解码器可正确解决粘包/拆包问题 ✓

**学习要点**:
- MessageToByteEncoder 是出站编码器的基类
- StringDecoder/StringEncoder 通常与帧解码器配合使用
- 完整的编解码流程：帧解码 → 字符串解码 → 业务处理

---

## [IT28] length-field-decoder

**分支**: `length-field-decoder`
**日期**: 2025-12-17

**改动内容**:
- 新增 `LengthFieldBasedFrameDecoder` 基于长度字段的帧解码器
  - 支持 1, 2, 3, 4, 8 字节长度字段
  - lengthFieldOffset: 长度字段偏移量（跳过消息头）
  - lengthFieldLength: 长度字段字节数
  - lengthAdjustment: 长度值调整量
  - initialBytesToStrip: 解码后跳过的字节数
  - maxFrameLength: 最大帧长度防止 OOM
- 新增 `LengthFieldBasedFrameDecoderTest` 共 14 个测试用例
  - ConstructorTests: 参数验证
  - BasicDecodingTests: 2/4字节长度字段、不完整数据等待
  - LengthAdjustmentTests: 长度包含头部、头部偏移处理
  - MultipleFramesTests: 粘包和拆包处理
  - ErrorHandlingTests: 超长帧异常
  - AcceptanceScenarioTests: 协议消息和RPC场景

**学习要点**:
- LengthFieldBasedFrameDecoder 是最通用的拆包解决方案
- 4个参数组合可适配各种协议格式
- lengthAdjustment 用于处理长度含/不含头部的情况

---

## [IT27] fixed-length-decoder

**分支**: `fixed-length-decoder`
**日期**: 2025-12-17

**改动内容**:
- 新增 `FixedLengthFrameDecoder` 定长帧解码器
  - 继承 ByteToMessageDecoder
  - 将字节流按固定长度切分成帧
  - 构造函数验证帧长度必须 > 0
- 新增 `FixedLengthFrameDecoderTest` 共 9 个测试用例
  - ConstructorTests: 构造参数验证
  - BasicDecodingTests: 单帧/多帧/不完整帧解码
  - PacketSplitMergeTests: 跨包帧和混合数据处理
  - AcceptanceScenarioTests: 传感器数据和命令协议场景

**学习要点**:
- 定长帧解码是最简单的拆包策略
- 继承 ByteToMessageDecoder 只需实现 decode() 方法
- 利用 while 循环一次读取尽可能多的完整帧

---

## [IT26] byte-to-message-decoder

**分支**: `byte-to-message-decoder`
**日期**: 2025-12-17

**改动内容**:
- 新增 `ByteToMessageDecoder` 抽象类
  - 字节累积缓冲区管理
  - 循环调用 decode() 直到数据不足
  - 解决 TCP 粘包/拆包问题的核心组件
- 新增 `DecoderException` 解码异常类
- 新增 `ByteToMessageDecoderTest` 共 10 个测试用例
  - BasicDecodingTests: 整数解码基本功能
  - PacketSplitMergeTests: 粘包拆包处理
  - LineDecoderTests: 行解码器示例
  - AcceptanceScenarioTests: 长度前缀解码和混合场景

**学习要点**:
- TCP 是流式协议，不保证消息边界
- 粘包：多条消息合并到一个 TCP 包
- 拆包：一条消息分散到多个 TCP 包
- 解码器需要累积字节并检查完整性

---

## [IT25] byte-buf-allocator

**分支**: `byte-buf-allocator`
**日期**: 2025-12-17

**改动内容**:
- 新增 `ByteBufAllocator` 接口
  - 定义 buffer/heapBuffer/directBuffer 分配方法
  - 支持指定初始容量和最大容量
- 新增 `UnpooledByteBufAllocator` 非池化实现
  - DEFAULT 静态实例，开箱即用
  - preferDirect 选项控制默认分配类型
  - 简化实现：directBuffer 暂时使用堆内存
- 新增 `ByteBufAllocatorTest` 共 11 个测试用例
  - UnpooledAllocatorTests: 基本分配功能
  - AllocatorConfigTests: 配置选项测试
  - AcceptanceScenarioTests: 消息处理、独立生命周期、扩容场景

**学习要点**:
- 分配器模式解耦 ByteBuf 创建和使用
- 池化 vs 非池化的权衡
- 工厂方法提供默认配置的便利性

**检查点 (US4)**: ByteBuf 支持读写索引分离和引用计数 ✅

---

## [IT24] byte-buf-reference-count

**分支**: `byte-buf-reference-count`
**日期**: 2025-12-17

**改动内容**:
- 新增 `AbstractReferenceCountedByteBuf` 抽象类
  - 使用 AtomicIntegerFieldUpdater 实现线程安全的引用计数
  - CAS 操作保证并发正确性
  - IllegalReferenceCountException 异常处理
- 重构 `HeapByteBuf` 继承新的基类
  - 移除手动引用计数实现
  - 继承原子引用计数能力
- 新增 `ReferenceCountTest` 共 14 个测试用例
  - BasicReferenceCountTests: 基本 retain/release 操作
  - ValidationTests: 参数验证测试
  - ConcurrencySafetyTests: 多线程安全测试
  - AcceptanceScenarioTests: 资源传递、try-finally、池化场景

**学习要点**:
- AtomicIntegerFieldUpdater 比 AtomicInteger 更节省内存
- CAS 循环确保并发操作的原子性
- 引用计数的典型使用模式：retain 传递，release 完成

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
