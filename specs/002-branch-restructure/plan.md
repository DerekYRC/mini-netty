# Implementation Plan: 分支重构与项目清理

**Branch**: `002-branch-restructure` | **Date**: 2025-12-17 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/002-branch-restructure/spec.md`

## Summary

对 Mini-Netty 项目进行跨分支重构，包括：
1. 将 `io.netty.example` 包从 `src/main/java` 迁移到 `src/test/java`
2. 重排 changelog.md 条目顺序（IT01 → IT37）
3. 删除 mini-spring 文件夹
4. 删除所有 package-info.java 文件
5. 将每个分支的变更合并到下一个分支，最终合并到 main

## Technical Context

**Language/Version**: Java 17  
**Primary Dependencies**: 无外部依赖（纯 JDK 实现）  
**Storage**: N/A  
**Testing**: JUnit 5 + AssertJ  
**Target Platform**: JVM (跨平台)
**Project Type**: 单项目（single）  
**Performance Goals**: N/A（重构任务）  
**Constraints**: 每次分支处理后必须通过 `mvn compile` 和 `mvn test`  
**Scale/Scope**: 37 个迭代分支

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. 代码质量 | ✅ PASS | 重构不涉及新代码，只是文件移动和删除 |
| II. 测试标准 | ✅ PASS | 每次分支处理后验证所有测试通过 |
| III. 用户体验一致性 | ✅ PASS | 保持与 Netty 一致的包结构 |
| IV. 性能要求 | N/A | 重构任务不影响性能 |

**质量门禁**:
- ✅ 编译通过（mvn compile）
- ✅ 所有测试通过（mvn test）

## Project Structure

### Documentation (this feature)

```text
specs/002-branch-restructure/
├── plan.md              # 本文件
├── research.md          # Phase 0 输出
├── quickstart.md        # Phase 1 输出
├── checklists/
│   └── requirements.md  # 需求检查清单
└── tasks.md             # Phase 2 输出
```

### Source Code (repository root)

```text
src/
├── main/java/io/netty/
│   ├── bootstrap/       # 启动器
│   ├── buffer/          # 缓冲区
│   ├── channel/         # 通道和事件循环
│   └── handler/         # 内置处理器
└── test/java/io/netty/
    ├── example/         # 示例代码（迁移目标）
    │   ├── bio/         # BIO 示例
    │   ├── nio/         # NIO 示例
    │   └── echo/        # Echo 示例
    └── ...              # 单元测试
```

**Structure Decision**: 单项目结构，示例代码放在测试包中

## Branch Processing Order

按迭代顺序处理 37 个分支：

| IT# | Branch Name | 说明 |
|-----|-------------|------|
| IT01 | simple-bio-server | 最简单的 BIO 服务端 |
| IT02 | simple-bio-client | BIO 客户端实现 |
| IT03 | multi-thread-bio-server | 多线程处理并发连接 |
| IT04 | nio-channel-buffer | NIO Channel 和 Buffer |
| IT05 | nio-selector | NIO Selector 多路复用 |
| IT06 | nio-server-accept | NIO 服务端 ACCEPT 事件 |
| IT07 | nio-server-read-write | NIO 完整读写流程 |
| IT08 | event-loop-interface | EventLoop 接口定义 |
| IT09 | single-thread-event-loop | 单线程事件循环实现 |
| IT10 | event-loop-task-queue | 任务队列机制 |
| IT11 | event-loop-scheduled-task | 定时任务支持 |
| IT12 | (merged into IT08) | Channel 接口定义 |
| IT13 | nio-channel-impl | NioChannel 实现 |
| IT14 | channel-config | Channel 配置 |
| IT15 | channel-unsafe | Channel.Unsafe 底层操作 |
| IT16 | channel-handler-interface | Handler 接口定义 |
| IT17 | channel-pipeline-basic | Pipeline 双向链表 |
| IT18 | channel-handler-context | HandlerContext 上下文 |
| IT19 | inbound-handler | 入站事件处理 |
| IT20 | outbound-handler | 出站事件处理 |
| IT21 | handler-adapter | Handler 适配器和异常传播 |
| IT22 | bytebuf-interface | ByteBuf 接口定义 |
| IT23 | heap-byte-buf | 堆内存 ByteBuf |
| IT24 | byte-buf-reference-count | 引用计数机制 |
| IT25 | byte-buf-allocator | ByteBuf 分配器 |
| IT26 | byte-to-message-decoder | 字节转消息解码器 |
| IT27 | fixed-length-decoder | 固定长度解码器 |
| IT28 | length-field-decoder | 长度字段解码器 |
| IT29 | string-codec | 字符串编解码器 |
| IT30 | abstract-bootstrap | Bootstrap 抽象基类 |
| IT31 | server-bootstrap | 服务端启动器 |
| IT32 | client-bootstrap | 客户端启动器 |
| IT33 | event-loop-group | EventLoopGroup 线程组 |
| IT34 | boss-worker-model | 主从 Reactor 模型 |
| IT35 | channel-chooser | 负载均衡策略 |
| IT36 | idle-state-handler | 空闲状态检测 |
| IT37 | logging-handler | 日志处理器 |

## Processing Steps Per Branch

对每个分支执行以下步骤：

### Step 1: Checkout Branch
```bash
git checkout <branch-name>
```

### Step 2: Delete mini-spring (if exists)
```bash
rm -rf mini-spring
```

### Step 3: Delete package-info.java (if exists)
```bash
find . -name "package-info.java" -type f -delete
```

### Step 4: Migrate example package (if exists)
```bash
# 如果 src/main/java/io/netty/example 存在
mkdir -p src/test/java/io/netty/example
mv src/main/java/io/netty/example/* src/test/java/io/netty/example/
rmdir src/main/java/io/netty/example
```

### Step 5: Verify Build
```bash
mvn compile
mvn test
```

### Step 6: Commit Changes
```bash
git add -A
git commit -m "refactor: 重构项目结构

- 迁移 example 包到测试目录
- 删除 mini-spring 文件夹
- 删除 package-info.java 文件"
```

### Step 7: Merge Previous Branch
```bash
git merge <previous-branch> --no-edit
# 冲突时优先保留旧分支的改动
```

## Changelog Format

每个迭代章节格式：

```markdown
## [IT##] branch-name

**分支**: `branch-name`
**日期**: YYYY-MM-DD

**改动内容**:
- 新增/修改的类、接口、方法

**学习要点**:
- 核心概念和设计模式

**测试**:
- 测试类和关键测试方法
```

---

## Post-Design Constitution Check

*GATE: Re-evaluated after Phase 1 design completion.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. 代码质量 | ✅ PASS | 使用标准 Shell 命令和 Git 操作，无新代码引入 |
| II. 测试标准 | ✅ PASS | 每个分支处理后验证 `mvn test` 通过 |
| III. 用户体验一致性 | ✅ PASS | 示例代码迁移到测试包，保持 Netty 风格 |
| IV. 性能要求 | N/A | 重构任务，不涉及性能指标 |

**Post-Design Decision Summary**:
1. 使用 `git merge --no-edit` 进行分支合并
2. 冲突时优先保留旧分支内容 (`--theirs`)
3. 验证策略: `mvn compile && mvn test`
4. 提交消息格式: `refactor: 重构项目结构`

---

## Generated Artifacts

| 文件 | 说明 | 状态 |
|------|------|------|
| [plan.md](plan.md) | 实现计划 | ✅ 完成 |
| [research.md](research.md) | 技术研究 | ✅ 完成 |
| [data-model.md](data-model.md) | 数据模型 | ✅ 完成 |
| [contracts/interfaces.md](contracts/interfaces.md) | 操作接口 | ✅ 完成 |
| [quickstart.md](quickstart.md) | 快速开始 | ✅ 完成 |
