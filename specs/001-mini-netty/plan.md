# 实现计划: Mini-Netty 网络编程框架

**分支**: `001-mini-netty` | **日期**: 2025-12-16 | **规格**: [spec.md](spec.md)
**输入**: 功能规格说明 `/specs/001-mini-netty/spec.md`

**说明**: 本计划由 `/speckit.plan` 命令生成，包含技术上下文、研究成果和设计文档。

## 摘要

参照 mini-spring 的渐进式学习方法，从零开始实现一个简化版的 Netty 网络编程框架 mini-netty。通过37个细粒度的迭代分支，逐步引入 BIO、NIO、EventLoop、Channel、ChannelPipeline、ByteBuf、编解码器、Bootstrap 等核心概念，每个迭代都有独立的单元测试和集成测试验证。

## 技术上下文

**语言/版本**: Java 17 (LTS)
**构建工具**: Maven 3.6+
**核心依赖**: 无外部依赖（纯JDK实现，参考Netty 4.x设计）
**存储**: N/A（网络框架，不涉及持久化）
**测试框架**: JUnit 5 + AssertJ
**目标平台**: JVM (跨平台)
**项目类型**: 单项目（single）
**性能目标**: 
- Echo服务端响应延迟 < 10ms（本地环回）
- 稳定处理100个并发客户端连接
**约束**: 
- 代码极度简化，只保留核心功能
- 不实现池化ByteBuf
- 每个迭代分支独立可运行
**规模/范围**: 37个迭代分支，约50-80个核心类

## 准则检查

*门禁: 必须在Phase 0研究前通过。Phase 1设计后重新检查。*

| 准则 | 状态 | 说明 |
|------|------|------|
| I. 代码质量 | ✅ 通过 | 所有类和方法有JavaDoc，命名遵循PascalCase/camelCase，单一职责 |
| II. 测试标准 | ✅ 通过 | 每个迭代有单元测试+集成测试，覆盖率目标≥80%，TDD方式开发 |
| III. 用户体验一致性 | ✅ 通过 | API命名和包结构对齐Netty，异常信息清晰，changelog及时更新 |
| IV. 性能要求 | ✅ 通过 | 响应延迟<10ms，并发100连接，选择适当数据结构 |

**门禁结果**: ✅ 全部通过，无需复杂度跟踪

## 项目结构

### 文档 (本功能)

```text
specs/001-mini-netty/
├── plan.md              # 本文件 (/speckit.plan 输出)
├── research.md          # Phase 0 输出
├── data-model.md        # Phase 1 输出
├── quickstart.md        # Phase 1 输出
├── contracts/           # Phase 1 输出
└── tasks.md             # Phase 2 输出 (/speckit.tasks - 本命令不创建)
```

### 源代码 (仓库根目录)

```text
/                          # 仓库根目录即项目目录
├── pom.xml
├── changelog.md
├── README.md
├── specs/                 # 规格文档（已存在）
└── src/
    ├── main/java/io/netty/
    │   ├── bootstrap/
    │   │   ├── AbstractBootstrap.java
    │   │   ├── Bootstrap.java
    │   │   └── ServerBootstrap.java
    │   ├── buffer/
    │   │   ├── ByteBuf.java
    │   │   ├── ByteBufAllocator.java
    │   │   ├── HeapByteBuf.java
    │   │   └── ReferenceCounted.java
    │   ├── channel/
    │   │   ├── Channel.java
    │   │   ├── ChannelConfig.java
    │   │   ├── ChannelHandler.java
    │   │   ├── ChannelHandlerContext.java
    │   │   ├── ChannelInboundHandler.java
    │   │   ├── ChannelOutboundHandler.java
    │   │   ├── ChannelPipeline.java
    │   │   ├── DefaultChannelPipeline.java
    │   │   └── nio/
    │   │       ├── NioEventLoop.java
    │   │       ├── NioEventLoopGroup.java
    │   │       ├── NioServerSocketChannel.java
    │   │       └── NioSocketChannel.java
    │   ├── handler/
    │   │   ├── codec/
    │   │   │   ├── ByteToMessageDecoder.java
    │   │   │   ├── FixedLengthFrameDecoder.java
    │   │   │   ├── LengthFieldBasedFrameDecoder.java
    │   │   │   ├── StringDecoder.java
    │   │   │   └── StringEncoder.java
    │   │   └── timeout/
    │   │       └── IdleStateHandler.java
    │   └── util/
    │       └── concurrent/
    │           ├── EventLoop.java
    │           ├── EventLoopGroup.java
    │           └── SingleThreadEventLoop.java
    └── test/java/io/netty/
        ├── bootstrap/
        ├── buffer/
        ├── channel/
        ├── handler/
        └── integration/
            ├── EchoServerTest.java
            └── ClientServerTest.java
```

**结构决策**: 采用单项目结构，包名对齐Netty（io.netty.*），测试与源码包结构镜像。

## 准则检查 (Phase 1 设计后重新验证)

*Phase 1 设计完成后的重新检查*

| 准则 | 状态 | Phase 1 验证说明 |
|------|------|------------------|
| I. 代码质量 | ✅ 通过 | contracts/interfaces.md 定义了清晰的接口和JavaDoc，data-model.md 遵循单一职责 |
| II. 测试标准 | ✅ 通过 | quickstart.md 包含测试示例和Given-When-Then模式，每个迭代有测试验证 |
| III. 用户体验一致性 | ✅ 通过 | 接口设计完全对齐Netty API，包结构为io.netty.*，异常通过Pipeline传播 |
| IV. 性能要求 | ✅ 通过 | 研究文档明确了NIO Selector、单线程EventLoop等高效设计选择 |

**Phase 1 门禁结果**: ✅ 全部通过

## 生成的文档

| 文档 | 路径 | 说明 |
|------|------|------|
| 研究文档 | [research.md](research.md) | 技术决策和替代方案分析 |
| 数据模型 | [data-model.md](data-model.md) | 核心实体定义和关系图 |
| 接口契约 | [contracts/interfaces.md](contracts/interfaces.md) | 核心接口和方法签名 |
| 快速开始 | [quickstart.md](quickstart.md) | 环境配置和迭代开发流程 |
