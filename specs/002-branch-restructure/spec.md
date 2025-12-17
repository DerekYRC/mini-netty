# Feature Specification: 分支重构与项目清理

**Feature Branch**: `002-branch-restructure`  
**Created**: 2025-12-17  
**Status**: Draft  
**Input**: User description: "将io.netty.example包及包下面的文件挪到单元测试包中、将每个迭代的主要改动按时间顺序排序、删除mini-spring文件夹、删除package-info.java文件、将前一个分支的变更合并到下一个分支中"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 示例代码迁移到测试包 (Priority: P1)

作为一个学习者，我希望示例代码放在测试包中，这样主代码包只包含框架核心代码，符合标准 Maven 项目结构。

**Why this priority**: 示例代码是学习辅助材料，不应该混在生产代码中。这是最基本的项目结构规范。

**Independent Test**: 迁移后 `src/main/java/io/netty/example` 不存在，`src/test/java/io/netty/example` 包含所有示例代码，且所有测试通过。

**Acceptance Scenarios**:

1. **Given** `io.netty.example` 包在 `src/main/java` 中, **When** 执行迁移, **Then** 该包及所有子包和文件移动到 `src/test/java/io/netty/example`
2. **Given** 迁移完成后, **When** 运行 `mvn test`, **Then** 所有测试通过
3. **Given** 迁移完成后, **When** 查看 `src/main/java/io/netty/example`, **Then** 该目录不存在

---

### User Story 2 - Changelog 按时间顺序排列 (Priority: P1)

作为一个学习者，我希望 changelog.md 按照迭代的时间顺序排列（IT01 在前，IT37 在后），这样我可以按顺序学习项目演进过程。

**Why this priority**: changelog 是学习路径的重要参考，正确的顺序对理解项目演进至关重要。

**Independent Test**: 查看 changelog.md，IT01 排在文件最前面（Unreleased 之后），IT37 排在最后。

**Acceptance Scenarios**:

1. **Given** changelog.md 中 IT37 在最前面, **When** 执行重排, **Then** IT01 在最前面，IT37 在最后
2. **Given** 重排后的 changelog.md, **When** 查看迭代顺序, **Then** 顺序为 IT01 → IT02 → ... → IT37

---

### User Story 3 - 删除 mini-spring 文件夹 (Priority: P2)

作为一个学习者，我希望项目只包含 Mini-Netty 相关代码，不包含无关的 mini-spring 文件夹。

**Why this priority**: 清理无关文件，保持项目结构清晰。

**Independent Test**: 执行删除后，mini-spring 文件夹不存在。

**Acceptance Scenarios**:

1. **Given** mini-spring 文件夹存在, **When** 执行删除, **Then** 该文件夹及其所有内容被删除
2. **Given** 删除后, **When** 运行 `mvn compile`, **Then** 编译成功（没有对 mini-spring 的依赖）

---

### User Story 4 - 删除 package-info.java 文件 (Priority: P2)

作为一个学习者，我希望项目不包含冗余的 package-info.java 文件。

**Why this priority**: 简化项目结构，减少维护负担。

**Independent Test**: 执行删除后，项目中不存在 package-info.java 文件。

**Acceptance Scenarios**:

1. **Given** 项目中存在 package-info.java 文件, **When** 执行删除, **Then** 所有 package-info.java 文件被删除
2. **Given** 删除后, **When** 运行 `mvn compile`, **Then** 编译成功

---

### User Story 5 - 分支合并到 main (Priority: P1)

作为一个学习者，我希望每个迭代分支都能独立运行，且所有变更最终合并到 main 分支。

**Why this priority**: 保证每个迭代分支的完整性，学习者可以切换到任意分支学习。

**Independent Test**: 每个迭代分支（IT01-IT37）都可以独立 checkout 并通过编译和测试。

**Acceptance Scenarios**:

1. **Given** 分支 IT(N), **When** 执行合并, **Then** IT(N) 包含 IT(N-1) 的所有改动
2. **Given** 所有分支合并完成, **When** checkout main 分支, **Then** main 包含所有 37 个迭代的改动
3. **Given** checkout 任意迭代分支, **When** 运行 `mvn test`, **Then** 该分支的测试通过

---

### Edge Cases

- 迁移示例代码时，如果测试包中已存在同名文件怎么办？→ 覆盖或合并
- 合并分支时出现冲突怎么办？→ 手动解决冲突，优先保留新分支的改动
- 某些分支没有 example 代码怎么办？→ 跳过该分支的 example 迁移步骤

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统 MUST 将 `src/main/java/io/netty/example` 包及其所有内容迁移到 `src/test/java/io/netty/example`
- **FR-002**: 系统 MUST 在每个迭代分支上执行此迁移操作
- **FR-003**: 系统 MUST 将 changelog.md 中的迭代条目按时间顺序重新排列（IT01 → IT37）
- **FR-004**: 系统 MUST 删除项目根目录下的 mini-spring 文件夹
- **FR-005**: 系统 MUST 删除项目中所有的 package-info.java 文件
- **FR-006**: 系统 MUST 将每个分支的变更合并到下一个分支（IT01 → IT02 → ... → IT37）
- **FR-007**: 系统 MUST 将每个迭代分支合并到 main 分支

### Key Entities

- **迭代分支**: 37 个迭代分支（simple-bio-server, simple-bio-client, ... , logging-handler）
- **示例包**: `io.netty.example` 包及其子包（bio, nio, echo）
- **changelog.md**: 记录所有迭代改动的文件

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 迁移后 `src/main/java/io/netty/example` 目录不存在
- **SC-002**: 迁移后 `src/test/java/io/netty/example` 包含所有示例代码
- **SC-003**: changelog.md 中第一个迭代条目为 IT01，最后一个为 IT37
- **SC-004**: mini-spring 文件夹不存在
- **SC-005**: 项目中没有 package-info.java 文件
- **SC-006**: 所有 37 个迭代分支都可以独立编译通过
- **SC-007**: main 分支包含所有迭代的改动且测试通过（434+ 测试）

## Assumptions

- 所有 37 个迭代分支已存在
- mini-spring 文件夹与 Mini-Netty 项目无依赖关系
- package-info.java 文件仅包含文档注释，删除不影响编译
- 示例代码迁移到测试包后功能不变
