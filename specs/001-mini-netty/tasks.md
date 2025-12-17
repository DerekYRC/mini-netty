# 任务列表: Mini-Netty 网络编程框架

**输入**: 设计文档 `/specs/001-mini-netty/`
**前置条件**: plan.md (必需), spec.md (必需), research.md, data-model.md, contracts/

**测试**: 规格说明要求每次改动必须补充完整的单元测试和集成测试 (FR-013, FR-014)

**组织方式**: 任务按迭代分支组织，每个迭代对应一个独立的git分支

## 格式: `[ID] [P?] [迭代] 描述 + 文件路径`

- **[P]**: 可并行执行（不同文件，无依赖）
- **[迭代]**: 任务所属的迭代分支（如 IT01, IT02...）
- 描述中包含确切的文件路径

## 路径规范

- **源码**: `src/main/java/io/netty/`
- **测试**: `src/test/java/io/netty/`
- **配置**: 仓库根目录 (`pom.xml`, `changelog.md`, `README.md`)

---

## Phase 1: 项目初始化 (Setup)

**目的**: 创建项目基础结构和配置

- [X] T001 创建 pom.xml，配置 Java 17 和 JUnit 5 依赖
- [X] T002 [P] 创建 README.md，说明项目目的和学习路径
- [X] T003 [P] 创建 changelog.md，准备记录迭代改动
- [X] T004 [P] 创建 src/main/java 和 src/test/java 目录结构
- [X] T005 提交初始代码到 main 分支

**检查点**: 项目可编译，`mvn compile` 和 `mvn test` 通过

---

## Phase 2: 用户故事 1 - 最简单的网络通信 (P1) 🎯 MVP

**目标**: 理解Socket编程基础，实现BIO和NIO服务端/客户端

**独立测试**: 启动服务端，客户端连接发送消息，服务端返回响应

### 迭代 1: simple-bio-server (IT01)

- [X] T006 [IT01] 创建分支 `simple-bio-server`
- [X] T007 [P] [IT01] 实现 SimpleBioServer 在 src/main/java/io/netty/example/bio/SimpleBioServer.java
- [X] T008 [P] [IT01] 编写单元测试 SimpleBioServerTest 在 src/test/java/io/netty/example/bio/SimpleBioServerTest.java
- [X] T009 [IT01] 更新 changelog.md 记录本次迭代
- [X] T010 [IT01] 运行测试验证并提交代码

### 迭代 2: simple-bio-client (IT02)

- [X] T011 [IT02] 基于 IT01 创建分支 `simple-bio-client`
- [X] T012 [P] [IT02] 实现 SimpleBioClient 在 src/main/java/io/netty/example/bio/SimpleBioClient.java
- [X] T013 [P] [IT02] 编写集成测试 ClientServerIntegrationTest 在 src/test/java/io/netty/integration/ClientServerIntegrationTest.java
- [X] T014 [IT02] 更新 changelog.md
- [X] T015 [IT02] 启动服务端和客户端进行实际验证

### 迭代 3: multi-thread-bio-server (IT03)

- [X] T016 [IT03] 基于 IT02 创建分支 `multi-thread-bio-server`
- [X] T017 [P] [IT03] 实现 MultiThreadBioServer 在 src/main/java/io/netty/example/bio/MultiThreadBioServer.java
- [X] T018 [P] [IT03] 编写并发测试 ConcurrentClientTest 在 src/test/java/io/netty/integration/ConcurrentClientTest.java
- [X] T019 [IT03] 更新 changelog.md
- [X] T020 [IT03] 验证多客户端同时连接

### 迭代 4: nio-channel-buffer (IT04)

- [X] T021 [IT04] 基于 IT03 创建分支 `nio-channel-buffer`
- [X] T022 [P] [IT04] 实现 NioChannelBufferDemo 在 src/main/java/io/netty/example/nio/NioChannelBufferDemo.java
- [X] T023 [P] [IT04] 编写测试 NioChannelBufferTest 在 src/test/java/io/netty/example/nio/NioChannelBufferTest.java
- [X] T024 [IT04] 更新 changelog.md

### 迭代 5: nio-selector (IT05)

- [X] T025 [IT05] 基于 IT04 创建分支 `nio-selector`
- [X] T026 [P] [IT05] 实现 NioSelectorDemo 在 src/main/java/io/netty/example/nio/NioSelectorDemo.java
- [X] T027 [P] [IT05] 编写测试 NioSelectorTest 在 src/test/java/io/netty/example/nio/NioSelectorTest.java
- [X] T028 [IT05] 更新 changelog.md

### 迭代 6: nio-server-accept (IT06)

- [X] T029 [IT06] 基于 IT05 创建分支 `nio-server-accept`
- [X] T030 [P] [IT06] 实现 NioServer (ACCEPT事件) 在 src/main/java/io/netty/example/nio/NioServer.java
- [X] T031 [P] [IT06] 编写测试 NioServerAcceptTest 在 src/test/java/io/netty/example/nio/NioServerAcceptTest.java
- [X] T032 [IT06] 更新 changelog.md

### 迭代 7: nio-server-read-write (IT07)

- [X] T033 [IT07] 基于 IT06 创建分支 `nio-server-read-write`
- [X] T034 [IT07] 完善 NioServer 支持 READ/WRITE 事件
- [X] T035 [P] [IT07] 实现 NioClient 在 src/main/java/io/netty/example/nio/NioClient.java
- [X] T036 [P] [IT07] 编写集成测试 NioClientServerTest 在 src/test/java/io/netty/integration/NioClientServerTest.java
- [X] T037 [IT07] 更新 changelog.md 并验证

**检查点 (US1)**: BIO 和 NIO 服务端/客户端均可正常通信

---

## Phase 3: 用户故事 2 - 基于NIO的事件循环 (P1)

**目标**: 理解EventLoop机制，学习单线程处理多连接

**独立测试**: 创建EventLoop，注册Channel，验证事件分发

### 迭代 8: event-loop-interface (IT08)

- [X] T038 [IT08] 基于 IT07 创建分支 `event-loop-interface`
- [X] T039 [P] [IT08] 定义 EventLoop 接口在 src/main/java/io/netty/channel/EventLoop.java
- [X] T040 [P] [IT08] 定义 EventLoopGroup 接口在 src/main/java/io/netty/channel/EventLoopGroup.java
- [X] T041 [P] [IT08] 编写测试 EventLoopInterfaceTest 在 src/test/java/io/netty/channel/EventLoopInterfaceTest.java
- [X] T042 [IT08] 更新 changelog.md

### 迭代 9: single-thread-event-loop (IT09)

- [X] T043 [IT09] 基于 IT08 创建分支 `single-thread-event-loop`
- [X] T044 [P] [IT09] 实现 SingleThreadEventLoop 在 src/main/java/io/netty/channel/SingleThreadEventLoop.java
- [X] T045 [P] [IT09] 实现 NioEventLoop 在 src/main/java/io/netty/channel/nio/NioEventLoop.java
- [X] T046 [P] [IT09] 编写测试 NioEventLoopTest 在 src/test/java/io/netty/channel/nio/NioEventLoopTest.java
- [X] T047 [IT09] 更新 changelog.md 并验证事件循环

### 迭代 10: event-loop-task-queue (IT10)

- [X] T048 [IT10] 基于 IT09 创建分支 `event-loop-task-queue`
- [X] T049 [IT10] 为 SingleThreadEventLoop 添加任务队列
- [X] T050 [IT10] 实现 execute(Runnable) 方法
- [X] T051 [P] [IT10] 编写测试 TaskQueueTest 在 src/test/java/io/netty/channel/TaskQueueTest.java
- [X] T052 [IT10] 更新 changelog.md

### 迭代 11: event-loop-scheduled-task (IT11)

- [X] T053 [IT11] 基于 IT10 创建分支 `event-loop-scheduled-task`
- [X] T054 [IT11] 添加定时任务队列
- [X] T055 [IT11] 实现 schedule() 和 scheduleAtFixedRate() 方法
- [X] T056 [P] [IT11] 编写测试 ScheduledTaskTest 在 src/test/java/io/netty/channel/ScheduledTaskTest.java
- [X] T057 [IT11] 更新 changelog.md 并验证

**检查点 (US2)**: EventLoop 可执行任务和定时任务

---

## Phase 4: 用户故事 3 - Channel和ChannelHandler处理链 (P1)

**目标**: 理解Channel抽象和Pipeline责任链模式

**独立测试**: 配置多个Handler，验证消息按顺序处理

### 迭代 12: channel-interface (IT12)

- [X] T058 [IT12] 基于 IT11 创建分支 `channel-interface`
- [X] T059 [P] [IT12] 定义 Channel 接口在 src/main/java/io/netty/channel/Channel.java
- [X] T060 [P] [IT12] 定义 ChannelId 在 src/main/java/io/netty/channel/ChannelId.java
- [X] T061 [P] [IT12] 定义 ChannelFuture 在 src/main/java/io/netty/channel/ChannelFuture.java
- [X] T062 [IT12] 更新 changelog.md

Note: IT12 已在 IT08 中完成（Channel 相关接口作为 EventLoop 依赖一起创建）

### 迭代 13: nio-channel-impl (IT13)

- [X] T063 [IT13] 基于 IT12 创建分支 `nio-channel-impl`
- [X] T064 [P] [IT13] 实现 AbstractChannel 在 src/main/java/io/netty/channel/AbstractChannel.java
- [X] T065 [P] [IT13] 实现 AbstractNioChannel 在 src/main/java/io/netty/channel/nio/AbstractNioChannel.java
- [X] T066 [P] [IT13] 实现 NioServerSocketChannel 在 src/main/java/io/netty/channel/nio/NioServerSocketChannel.java
- [X] T067 [P] [IT13] 实现 NioSocketChannel 在 src/main/java/io/netty/channel/nio/NioSocketChannel.java
- [X] T068 [P] [IT13] 编写测试 NioChannelTest 在 src/test/java/io/netty/channel/nio/NioChannelTest.java
- [X] T069 [IT13] 更新 changelog.md

### 迭代 14: channel-config (IT14)

- [X] T070 [IT14] 基于 IT13 创建分支 `channel-config`
- [X] T071 [P] [IT14] 定义 ChannelConfig 接口在 src/main/java/io/netty/channel/ChannelConfig.java
- [X] T072 [P] [IT14] 定义 ChannelOption 在 src/main/java/io/netty/channel/ChannelOption.java
- [X] T073 [P] [IT14] 实现 DefaultChannelConfig 在 src/main/java/io/netty/channel/DefaultChannelConfig.java
- [X] T074 [IT14] 更新 changelog.md

### 迭代 15: channel-unsafe (IT15)

- [X] T075 [IT15] 基于 IT14 创建分支 `channel-unsafe`
- [X] T076 [IT15] 定义 Channel.Unsafe 内部接口
- [X] T077 [IT15] 实现 AbstractUnsafe 在 AbstractChannel 中
- [X] T078 [P] [IT15] 编写测试 ChannelUnsafeTest 在 src/test/java/io/netty/channel/ChannelUnsafeTest.java
- [X] T079 [IT15] 更新 changelog.md

### 迭代 16: channel-handler-interface (IT16)

- [X] T080 [IT16] 基于 IT15 创建分支 `channel-handler-interface`
- [X] T081 [P] [IT16] 定义 ChannelHandler 接口在 src/main/java/io/netty/channel/ChannelHandler.java
- [X] T082 [P] [IT16] 定义 ChannelInboundHandler 在 src/main/java/io/netty/channel/ChannelInboundHandler.java
- [X] T083 [P] [IT16] 定义 ChannelOutboundHandler 在 src/main/java/io/netty/channel/ChannelOutboundHandler.java
- [X] T084 [IT16] 更新 changelog.md

### 迭代 17: channel-pipeline-basic (IT17)

- [X] T085 [IT17] 基于 IT16 创建分支 `channel-pipeline-basic`
- [X] T086 [P] [IT17] 定义 ChannelPipeline 接口在 src/main/java/io/netty/channel/ChannelPipeline.java
- [X] T087 [IT17] 实现 DefaultChannelPipeline (双向链表) 在 src/main/java/io/netty/channel/DefaultChannelPipeline.java
- [X] T088 [P] [IT17] 编写测试 ChannelPipelineTest 在 src/test/java/io/netty/channel/ChannelPipelineTest.java
- [X] T089 [IT17] 更新 changelog.md

### 迭代 18: channel-handler-context (IT18)

- [X] T090 [IT18] 基于 IT17 创建分支 `channel-handler-context`
- [X] T091 [P] [IT18] 定义 ChannelHandlerContext 接口在 src/main/java/io/netty/channel/ChannelHandlerContext.java
- [X] T092 [IT18] 实现 AbstractChannelHandlerContext 在 src/main/java/io/netty/channel/AbstractChannelHandlerContext.java
- [X] T093 [IT18] 实现 HeadContext 和 TailContext 在 DefaultChannelPipeline 中
- [X] T094 [P] [IT18] 编写测试 ChannelHandlerContextTest 在 src/test/java/io/netty/channel/ChannelHandlerContextTest.java
- [X] T095 [IT18] 更新 changelog.md

### 迭代 19: inbound-handler (IT19)

- [X] T096 [IT19] 基于 IT18 创建分支 `inbound-handler`
- [X] T097 [IT19] 实现入站事件传递 (fireChannelRead, fireChannelActive 等)
- [X] T098 [P] [IT19] 实现 ChannelInboundHandlerAdapter 在 src/main/java/io/netty/channel/ChannelInboundHandlerAdapter.java
- [X] T099 [P] [IT19] 编写测试 InboundHandlerTest 在 src/test/java/io/netty/channel/InboundHandlerTest.java
- [X] T100 [IT19] 更新 changelog.md

### 迭代 20: outbound-handler (IT20)

- [X] T101 [IT20] 基于 IT19 创建分支 `outbound-handler`
- [X] T102 [IT20] 实现出站事件传递 (write, flush, close 等)
- [X] T103 [P] [IT20] 实现 ChannelOutboundHandlerAdapter 在 src/main/java/io/netty/channel/ChannelOutboundHandlerAdapter.java
- [X] T104 [P] [IT20] 编写测试 OutboundHandlerTest 在 src/test/java/io/netty/channel/OutboundHandlerTest.java
- [X] T105 [IT20] 更新 changelog.md

### 迭代 21: handler-adapter (IT21)

- [X] T106 [IT21] 基于 IT20 创建分支 `handler-adapter`
- [X] T107 [IT21] 实现 ChannelDuplexHandler 在 src/main/java/io/netty/channel/ChannelDuplexHandler.java
- [X] T108 [IT21] 实现异常传播 exceptionCaught
- [X] T109 [P] [IT21] 编写集成测试 PipelineIntegrationTest 在 src/test/java/io/netty/integration/PipelineIntegrationTest.java
- [X] T110 [IT21] 更新 changelog.md 并验证

**检查点 (US3)**: Pipeline 可正确处理入站和出站事件

---

## Phase 5: 用户故事 4 - ByteBuf缓冲区 (P2)

**目标**: 理解读写索引分离和引用计数

**独立测试**: ByteBuf读写操作，验证索引和引用计数

### 迭代 22: byte-buf-interface (IT22)

- [X] T111 [IT22] 基于 IT21 创建分支 `byte-buf-interface`
- [X] T112 [P] [IT22] 定义 ReferenceCounted 接口在 src/main/java/io/netty/buffer/ReferenceCounted.java
- [X] T113 [P] [IT22] 定义 ByteBuf 抽象类在 src/main/java/io/netty/buffer/ByteBuf.java
- [X] T114 [IT22] 更新 changelog.md

### 迭代 23: heap-byte-buf (IT23)

- [X] T115 [IT23] 基于 IT22 创建分支 `heap-byte-buf`
- [X] T116 [IT23] 实现 AbstractByteBuf 在 src/main/java/io/netty/buffer/AbstractByteBuf.java
- [X] T117 [IT23] 实现 HeapByteBuf 在 src/main/java/io/netty/buffer/HeapByteBuf.java
- [X] T118 [P] [IT23] 编写测试 HeapByteBufTest 在 src/test/java/io/netty/buffer/HeapByteBufTest.java
- [X] T119 [IT23] 更新 changelog.md

### 迭代 24: byte-buf-reference-count (IT24)

- [X] T120 [IT24] 基于 IT23 创建分支 `byte-buf-reference-count`
- [X] T121 [IT24] 实现 retain() 和 release() 方法
- [X] T122 [IT24] 实现 AbstractReferenceCountedByteBuf 在 src/main/java/io/netty/buffer/AbstractReferenceCountedByteBuf.java
- [X] T123 [P] [IT24] 编写测试 ReferenceCountTest 在 src/test/java/io/netty/buffer/ReferenceCountTest.java
- [X] T124 [IT24] 更新 changelog.md

### 迭代 25: byte-buf-allocator (IT25)

- [X] T125 [IT25] 基于 IT24 创建分支 `byte-buf-allocator`
- [X] T126 [P] [IT25] 定义 ByteBufAllocator 接口在 src/main/java/io/netty/buffer/ByteBufAllocator.java
- [X] T127 [IT25] 实现 UnpooledByteBufAllocator 在 src/main/java/io/netty/buffer/UnpooledByteBufAllocator.java
- [X] T128 [P] [IT25] 编写测试 ByteBufAllocatorTest 在 src/test/java/io/netty/buffer/ByteBufAllocatorTest.java
- [X] T129 [IT25] 更新 changelog.md 并验证

**检查点 (US4)**: ByteBuf 支持读写索引分离和引用计数

---

## Phase 6: 用户故事 5 - 编解码器 (P2)

**目标**: 理解粘包/拆包解决方案

**独立测试**: 发送多条消息，验证解码器正确拆分

### 迭代 26: byte-to-message-decoder (IT26)

- [X] T130 [IT26] 基于 IT25 创建分支 `byte-to-message-decoder`
- [X] T131 [IT26] 实现 ByteToMessageDecoder 在 src/main/java/io/netty/handler/codec/ByteToMessageDecoder.java
- [X] T132 [P] [IT26] 编写测试 ByteToMessageDecoderTest 在 src/test/java/io/netty/handler/codec/ByteToMessageDecoderTest.java
- [X] T133 [IT26] 更新 changelog.md

### 迭代 27: fixed-length-decoder (IT27)

- [X] T134 [IT27] 基于 IT26 创建分支 `fixed-length-decoder`
- [X] T135 [IT27] 实现 FixedLengthFrameDecoder 在 src/main/java/io/netty/handler/codec/FixedLengthFrameDecoder.java
- [X] T136 [P] [IT27] 编写测试 FixedLengthFrameDecoderTest 在 src/test/java/io/netty/handler/codec/FixedLengthFrameDecoderTest.java
- [X] T137 [IT27] 更新 changelog.md

### 迭代 28: length-field-decoder (IT28)

- [X] T138 [IT28] 基于 IT27 创建分支 `length-field-decoder`
- [X] T139 [IT28] 实现 LengthFieldBasedFrameDecoder 在 src/main/java/io/netty/handler/codec/LengthFieldBasedFrameDecoder.java
- [X] T140 [P] [IT28] 编写测试 LengthFieldBasedFrameDecoderTest 在 src/test/java/io/netty/handler/codec/LengthFieldBasedFrameDecoderTest.java
- [X] T141 [IT28] 更新 changelog.md

### 迭代 29: string-codec (IT29)

- [X] T142 [IT29] 基于 IT28 创建分支 `string-codec`
- [X] T143 [P] [IT29] 实现 StringDecoder 在 src/main/java/io/netty/handler/codec/string/StringDecoder.java
- [X] T144 [P] [IT29] 实现 StringEncoder 在 src/main/java/io/netty/handler/codec/string/StringEncoder.java
- [X] T145 [P] [IT29] 实现 MessageToByteEncoder 在 src/main/java/io/netty/handler/codec/MessageToByteEncoder.java
- [X] T146 [P] [IT29] 编写测试 StringCodecTest 在 src/test/java/io/netty/handler/codec/string/StringCodecTest.java
- [X] T147 [IT29] 更新 changelog.md 并验证

**检查点 (US5)**: 编解码器可正确解决粘包/拆包问题

---

## Phase 7: 用户故事 6 - Bootstrap启动器 (P2)

**目标**: 体验流式API配置服务端/客户端

**独立测试**: 使用Bootstrap配置并启动服务端

### 迭代 30: abstract-bootstrap (IT30)

- [X] T148 [IT30] 基于 IT29 创建分支 `abstract-bootstrap`
- [X] T149 [IT30] 实现 AbstractBootstrap 在 src/main/java/io/netty/bootstrap/AbstractBootstrap.java
- [X] T150 [P] [IT30] 定义 ChannelInitializer 在 src/main/java/io/netty/channel/ChannelInitializer.java
- [X] T151 [IT30] 更新 changelog.md

### 迭代 31: server-bootstrap (IT31)

- [X] T152 [IT31] 基于 IT30 创建分支 `server-bootstrap`
- [X] T153 [IT31] 实现 ServerBootstrap 在 src/main/java/io/netty/bootstrap/ServerBootstrap.java
- [X] T154 [P] [IT31] 编写测试 ServerBootstrapTest 在 src/test/java/io/netty/bootstrap/ServerBootstrapTest.java
- [X] T155 [IT31] 更新 changelog.md

### 迭代 32: client-bootstrap (IT32)

- [X] T156 [IT32] 基于 IT31 创建分支 `client-bootstrap`
- [X] T157 [IT32] 实现 Bootstrap (客户端) 在 src/main/java/io/netty/bootstrap/Bootstrap.java
- [X] T158 [P] [IT32] 编写集成测试 BootstrapIntegrationTest 在 src/test/java/io/netty/integration/BootstrapIntegrationTest.java
- [X] T159 [IT32] 更新 changelog.md 并验证

**检查点 (US6)**: Bootstrap 可配置和启动服务端/客户端

---

## Phase 8: 多线程模型扩展 (P2)

**目标**: 实现主从Reactor模型

### 迭代 33: event-loop-group (IT33)

- [ ] T160 [IT33] 基于 IT32 创建分支 `event-loop-group`
- [ ] T161 [IT33] 实现 NioEventLoopGroup 在 src/main/java/io/netty/channel/nio/NioEventLoopGroup.java
- [ ] T162 [P] [IT33] 编写测试 NioEventLoopGroupTest 在 src/test/java/io/netty/channel/nio/NioEventLoopGroupTest.java
- [ ] T163 [IT33] 更新 changelog.md

### 迭代 34: boss-worker-model (IT34)

- [ ] T164 [IT34] 基于 IT33 创建分支 `boss-worker-model`
- [ ] T165 [IT34] 修改 ServerBootstrap 支持 Boss/Worker 线程模型
- [ ] T166 [P] [IT34] 编写测试 BossWorkerModelTest 在 src/test/java/io/netty/integration/BossWorkerModelTest.java
- [ ] T167 [IT34] 更新 changelog.md

### 迭代 35: channel-chooser (IT35)

- [ ] T168 [IT35] 基于 IT34 创建分支 `channel-chooser`
- [ ] T169 [IT35] 实现 EventLoopChooser 负载均衡策略
- [ ] T170 [P] [IT35] 编写测试 ChannelChooserTest 在 src/test/java/io/netty/channel/ChannelChooserTest.java
- [ ] T171 [IT35] 更新 changelog.md 并验证

---

## Phase 9: 实用Handler扩展 (P2)

**目标**: 实现常用的Handler

### 迭代 36: idle-state-handler (IT36)

- [ ] T172 [IT36] 基于 IT35 创建分支 `idle-state-handler`
- [ ] T173 [P] [IT36] 定义 IdleStateEvent 在 src/main/java/io/netty/handler/timeout/IdleStateEvent.java
- [ ] T174 [IT36] 实现 IdleStateHandler 在 src/main/java/io/netty/handler/timeout/IdleStateHandler.java
- [ ] T175 [P] [IT36] 编写测试 IdleStateHandlerTest 在 src/test/java/io/netty/handler/timeout/IdleStateHandlerTest.java
- [ ] T176 [IT36] 更新 changelog.md

### 迭代 37: logging-handler (IT37)

- [ ] T177 [IT37] 基于 IT36 创建分支 `logging-handler`
- [ ] T178 [IT37] 实现 LoggingHandler 在 src/main/java/io/netty/handler/logging/LoggingHandler.java
- [ ] T179 [P] [IT37] 编写测试 LoggingHandlerTest 在 src/test/java/io/netty/handler/logging/LoggingHandlerTest.java
- [ ] T180 [IT37] 更新 changelog.md 并验证

---

## Phase 10: 收尾与验证 (Polish)

**目的**: 完善文档和最终验证

- [ ] T181 [P] 更新 README.md，添加完整的学习指南
- [ ] T182 [P] 整理 changelog.md，确保所有迭代都有记录
- [ ] T183 运行所有分支的测试验证
- [ ] T184 创建 Echo 示例应用在 src/main/java/io/netty/example/echo/
- [ ] T185 运行 quickstart.md 中的验证步骤

---

## 依赖关系与执行顺序

### 阶段依赖

- **Phase 1 (Setup)**: 无依赖 - 立即开始
- **Phase 2-4 (US1-3)**: P1 优先级，依次执行
- **Phase 5-7 (US4-6)**: P2 优先级，可在 P1 完成后开始
- **Phase 8-9**: 扩展功能，可选执行
- **Phase 10 (Polish)**: 所有迭代完成后执行

### 迭代依赖

```
IT01 → IT02 → IT03 → IT04 → IT05 → IT06 → IT07
                                              ↓
IT08 → IT09 → IT10 → IT11 ←─────────────────┘
                        ↓
IT12 → IT13 → IT14 → IT15 → IT16 → IT17 → IT18 → IT19 → IT20 → IT21
                                                                  ↓
IT22 → IT23 → IT24 → IT25 ←──────────────────────────────────────┘
                        ↓
IT26 → IT27 → IT28 → IT29
                        ↓
IT30 → IT31 → IT32 → IT33 → IT34 → IT35 → IT36 → IT37
```

### 并行机会

- 同一迭代内标记 [P] 的任务可并行
- 测试和实现可由不同人并行编写
- 不同迭代必须顺序执行（每个迭代基于前一个分支）

---

## 实现策略

### MVP优先 (用户故事 1-3)

1. 完成 Phase 1: Setup
2. 完成 Phase 2: 用户故事 1 (网络通信基础)
3. 完成 Phase 3: 用户故事 2 (EventLoop)
4. 完成 Phase 4: 用户故事 3 (Pipeline)
5. **验证**: 此时 mini-netty 核心功能已完整

### 增量交付

每个迭代完成后：
1. 运行测试验证
2. 启动服务端/客户端实际验证
3. 更新 changelog.md
4. 提交代码到对应分支

---

## 备注

- 每个 [IT##] 标记表示任务所属的迭代分支
- 每个迭代结束后必须更新 changelog.md
- 测试必须在实现前编写（TDD）
- 所有分支名称与迭代计划中的名称一致
- 验证时必须同时运行服务端和客户端

---

## 报告摘要

| 指标 | 值 |
|------|-----|
| 总任务数 | 185 |
| Phase 1 (Setup) | 5 |
| Phase 2-4 (P1 用户故事) | 105 |
| Phase 5-7 (P2 用户故事) | 52 |
| Phase 8-9 (扩展) | 18 |
| Phase 10 (收尾) | 5 |
| 迭代数量 | 37 |
| 可并行任务 | 约 40% |
