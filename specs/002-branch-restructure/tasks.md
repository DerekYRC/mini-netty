# 任务清单: 分支重构与项目清理

**输入**: 设计文档 `/specs/002-branch-restructure/`  
**前置条件**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅

**测试**: 未要求 - 不包含测试任务。

**组织方式**: 按用户故事组织任务以支持独立实现。由于任务性质（37个分支迭代），任务结构为顺序处理分支，每个分支内可并行操作。

## 格式说明: `[ID] [P?] [Story?] 描述`

- **[P]**: 可并行执行（不同文件，无依赖）
- **[Story]**: 所属用户故事（US1-US5）
- 描述中包含确切的文件路径/分支名

---

## 第一阶段: 准备工作

**目的**: 处理分支前准备脚本并验证环境

- [X] T001 使用 `git branch --list` 验证所有 37 个迭代分支存在
- [X] T002 [P] 创建处理脚本 `scripts/process-branch.sh` 包含可复用操作
- [X] T003 [P] 使用 `git log --oneline -1` 记录每个分支当前状态
- [X] T004 使用 `git branch main-backup` 创建主分支备份

**检查点**: 环境已验证，脚本就绪，备份已创建

---

## 第二阶段: 基础工作（首个分支处理）

**目的**: 处理 IT01 (simple-bio-server) 作为基础 - 无需合并

**⚠️ 关键**: 这是第一个分支，为后续所有分支建立处理模式

- [X] T005 [US1] [US3] [US4] 使用 `git checkout simple-bio-server` 切换到分支
- [X] T006 [P] [US3] 使用 `rm -rf mini-spring` 删除 mini-spring 文件夹
- [X] T007 [P] [US4] 使用 `find . -name "package-info.java" -type f -delete` 删除 package-info.java 文件
- [X] T008 [US1] 迁移 example 包（如存在）从 `src/main/java/io/netty/example` 到 `src/test/java/io/netty/example`
- [X] T009 使用 `mvn compile && mvn test` 验证构建
- [X] T010 使用 `git add -A && git commit -m "refactor: 重构项目结构"` 提交变更

**检查点**: IT01 (simple-bio-server) 已处理 - 基础已建立

---

## 第三阶段: 用户故事5 - 分支处理 IT02-IT11（优先级: P1）

**目标**: 处理迭代 02-11，将前一个分支合并到每个分支

**独立测试**: 每个处理后的分支通过 `mvn compile && mvn test`

### IT02: simple-bio-client

- [X] T011 [US5] 切换到分支 simple-bio-client
- [X] T012 [P] [US3] 删除 mini-spring 文件夹
- [X] T013 [P] [US4] 删除 package-info.java 文件
- [X] T014 [US1] 迁移 example 包（如存在）
- [X] T015 [US5] 将 simple-bio-server 合并到当前分支
- [X] T016 验证构建并提交变更

### IT03: multi-thread-bio-server

- [X] T017 [US5] 切换到分支 multi-thread-bio-server
- [X] T018 [P] [US3] 删除 mini-spring 文件夹
- [X] T019 [P] [US4] 删除 package-info.java 文件
- [X] T020 [US1] 迁移 example 包（如存在）
- [X] T021 [US5] 将 simple-bio-client 合并到当前分支
- [X] T022 验证构建并提交变更

### IT04: nio-channel-buffer

- [X] T023 [US5] 切换到分支 nio-channel-buffer
- [X] T024 [P] [US3] 删除 mini-spring 文件夹
- [X] T025 [P] [US4] 删除 package-info.java 文件
- [X] T026 [US1] 迁移 example 包（如存在）
- [X] T027 [US5] 将 multi-thread-bio-server 合并到当前分支
- [X] T028 验证构建并提交变更

### IT05: nio-selector

- [X] T029 [US5] 切换到分支 nio-selector
- [X] T030 [P] [US3] 删除 mini-spring 文件夹
- [X] T031 [P] [US4] 删除 package-info.java 文件
- [X] T032 [US1] 迁移 example 包（如存在）
- [X] T033 [US5] 将 nio-channel-buffer 合并到当前分支
- [X] T034 验证构建并提交变更

### IT06: nio-server-accept

- [X] T035 [US5] 切换到分支 nio-server-accept
- [X] T036 [P] [US3] 删除 mini-spring 文件夹
- [X] T037 [P] [US4] 删除 package-info.java 文件
- [X] T038 [US1] 迁移 example 包（如存在）
- [X] T039 [US5] 将 nio-selector 合并到当前分支
- [X] T040 验证构建并提交变更

### IT07: nio-server-read-write

- [X] T041 [US5] 切换到分支 nio-server-read-write
- [X] T042 [P] [US3] 删除 mini-spring 文件夹
- [X] T043 [P] [US4] 删除 package-info.java 文件
- [X] T044 [US1] 迁移 example 包（如存在）
- [X] T045 [US5] 将 nio-server-accept 合并到当前分支
- [X] T046 验证构建并提交变更

### IT08: event-loop-interface

- [X] T047 [US5] 切换到分支 event-loop-interface
- [X] T048 [P] [US3] 删除 mini-spring 文件夹
- [X] T049 [P] [US4] 删除 package-info.java 文件
- [X] T050 [US1] 迁移 example 包（如存在）
- [X] T051 [US5] 将 nio-server-read-write 合并到当前分支
- [X] T052 验证构建并提交变更

### IT09: single-thread-event-loop

- [X] T053 [US5] 切换到分支 single-thread-event-loop
- [X] T054 [P] [US3] 删除 mini-spring 文件夹
- [X] T055 [P] [US4] 删除 package-info.java 文件
- [X] T056 [US1] 迁移 example 包（如存在）
- [X] T057 [US5] 将 event-loop-interface 合并到当前分支
- [X] T058 验证构建并提交变更

### IT10: event-loop-task-queue

- [X] T059 [US5] 切换到分支 event-loop-task-queue
- [X] T060 [P] [US3] 删除 mini-spring 文件夹
- [X] T061 [P] [US4] 删除 package-info.java 文件
- [X] T062 [US1] 迁移 example 包（如存在）
- [X] T063 [US5] 将 single-thread-event-loop 合并到当前分支
- [X] T064 验证构建并提交变更

### IT11: event-loop-scheduled-task

- [X] T065 [US5] 切换到分支 event-loop-scheduled-task
- [X] T066 [P] [US3] 删除 mini-spring 文件夹
- [X] T067 [P] [US4] 删除 package-info.java 文件
- [X] T068 [US1] 迁移 example 包（如存在）
- [X] T069 [US5] 将 event-loop-task-queue 合并到当前分支
- [X] T070 验证构建并提交变更

**检查点**: IT02-IT11 已处理并合并

---

## 第四阶段: 用户故事5 - 分支处理 IT13-IT21（优先级: P1）

**目标**: 处理迭代 13-21（注意: IT12 已合并到 IT08）

### IT13: nio-channel-impl

- [X] T071 [US5] 切换到分支 nio-channel-impl
- [X] T072 [P] [US3] 删除 mini-spring 文件夹
- [X] T073 [P] [US4] 删除 package-info.java 文件
- [X] T074 [US1] 迁移 example 包（如存在）
- [X] T075 [US5] 将 event-loop-scheduled-task 合并到当前分支
- [X] T076 验证构建并提交变更

### IT14: channel-config

- [X] T077 [US5] 切换到分支 channel-config
- [X] T078 [P] [US3] 删除 mini-spring 文件夹
- [X] T079 [P] [US4] 删除 package-info.java 文件
- [X] T080 [US1] 迁移 example 包（如存在）
- [X] T081 [US5] 将 nio-channel-impl 合并到当前分支
- [X] T082 验证构建并提交变更

### IT15: channel-unsafe

- [X] T083 [US5] 切换到分支 channel-unsafe
- [X] T084 [P] [US3] 删除 mini-spring 文件夹
- [X] T085 [P] [US4] 删除 package-info.java 文件
- [X] T086 [US1] 迁移 example 包（如存在）
- [X] T087 [US5] 将 channel-config 合并到当前分支
- [X] T088 验证构建并提交变更

### IT16: channel-handler-interface

- [X] T089 [US5] 切换到分支 channel-handler-interface
- [X] T090 [P] [US3] 删除 mini-spring 文件夹
- [X] T091 [P] [US4] 删除 package-info.java 文件
- [X] T092 [US1] 迁移 example 包（如存在）
- [X] T093 [US5] 将 channel-unsafe 合并到当前分支
- [X] T094 验证构建并提交变更

### IT17: channel-pipeline-basic

- [X] T095 [US5] 切换到分支 channel-pipeline-basic
- [X] T096 [P] [US3] 删除 mini-spring 文件夹
- [X] T097 [P] [US4] 删除 package-info.java 文件
- [X] T098 [US1] 迁移 example 包（如存在）
- [X] T099 [US5] 将 channel-handler-interface 合并到当前分支
- [X] T100 验证构建并提交变更

### IT18: channel-handler-context

- [X] T101 [US5] 切换到分支 channel-handler-context
- [X] T102 [P] [US3] 删除 mini-spring 文件夹
- [X] T103 [P] [US4] 删除 package-info.java 文件
- [X] T104 [US1] 迁移 example 包（如存在）
- [X] T105 [US5] 将 channel-pipeline-basic 合并到当前分支
- [X] T106 验证构建并提交变更

### IT19: inbound-handler

- [X] T107 [US5] 切换到分支 inbound-handler
- [X] T108 [P] [US3] 删除 mini-spring 文件夹
- [X] T109 [P] [US4] 删除 package-info.java 文件
- [X] T110 [US1] 迁移 example 包（如存在）
- [X] T111 [US5] 将 channel-handler-context 合并到当前分支
- [X] T112 验证构建并提交变更

### IT20: outbound-handler

- [X] T113 [US5] 切换到分支 outbound-handler
- [X] T114 [P] [US3] 删除 mini-spring 文件夹
- [X] T115 [P] [US4] 删除 package-info.java 文件
- [X] T116 [US1] 迁移 example 包（如存在）
- [X] T117 [US5] 将 inbound-handler 合并到当前分支
- [X] T118 验证构建并提交变更

### IT21: handler-adapter

- [X] T119 [US5] 切换到分支 handler-adapter
- [X] T120 [P] [US3] 删除 mini-spring 文件夹
- [X] T121 [P] [US4] 删除 package-info.java 文件
- [X] T122 [US1] 迁移 example 包（如存在）
- [X] T123 [US5] 将 outbound-handler 合并到当前分支
- [X] T124 验证构建并提交变更

**检查点**: IT13-IT21 已处理并合并

---

## 第五阶段: 用户故事5 - 分支处理 IT22-IT29（优先级: P1）

**目标**: 处理迭代 22-29（ByteBuf 和 Codec）

### IT22: bytebuf-interface

- [X] T125 [US5] 切换到分支 bytebuf-interface
- [X] T126 [P] [US3] 删除 mini-spring 文件夹
- [X] T127 [P] [US4] 删除 package-info.java 文件
- [X] T128 [US1] 迁移 example 包（如存在）
- [X] T129 [US5] 将 handler-adapter 合并到当前分支
- [X] T130 验证构建并提交变更

### IT23: heap-byte-buf

- [X] T131 [US5] 切换到分支 heap-byte-buf
- [X] T132 [P] [US3] 删除 mini-spring 文件夹
- [X] T133 [P] [US4] 删除 package-info.java 文件
- [X] T134 [US1] 迁移 example 包（如存在）
- [X] T135 [US5] 将 bytebuf-interface 合并到当前分支
- [X] T136 验证构建并提交变更

### IT24: byte-buf-reference-count

- [X] T137 [US5] 切换到分支 byte-buf-reference-count
- [X] T138 [P] [US3] 删除 mini-spring 文件夹
- [X] T139 [P] [US4] 删除 package-info.java 文件
- [X] T140 [US1] 迁移 example 包（如存在）
- [X] T141 [US5] 将 heap-byte-buf 合并到当前分支
- [X] T142 验证构建并提交变更

### IT25: byte-buf-allocator

- [X] T143 [US5] 切换到分支 byte-buf-allocator
- [X] T144 [P] [US3] 删除 mini-spring 文件夹
- [X] T145 [P] [US4] 删除 package-info.java 文件
- [X] T146 [US1] 迁移 example 包（如存在）
- [X] T147 [US5] 将 byte-buf-reference-count 合并到当前分支
- [X] T148 验证构建并提交变更

### IT26: byte-to-message-decoder

- [X] T149 [US5] 切换到分支 byte-to-message-decoder
- [X] T150 [P] [US3] 删除 mini-spring 文件夹
- [X] T151 [P] [US4] 删除 package-info.java 文件
- [X] T152 [US1] 迁移 example 包（如存在）
- [X] T153 [US5] 将 byte-buf-allocator 合并到当前分支
- [X] T154 验证构建并提交变更

### IT27: fixed-length-decoder

- [X] T155 [US5] 切换到分支 fixed-length-decoder
- [X] T156 [P] [US3] 删除 mini-spring 文件夹
- [X] T157 [P] [US4] 删除 package-info.java 文件
- [X] T158 [US1] 迁移 example 包（如存在）
- [X] T159 [US5] 将 byte-to-message-decoder 合并到当前分支
- [X] T160 验证构建并提交变更

### IT28: length-field-decoder

- [X] T161 [US5] 切换到分支 length-field-decoder
- [X] T162 [P] [US3] 删除 mini-spring 文件夹
- [X] T163 [P] [US4] 删除 package-info.java 文件
- [X] T164 [US1] 迁移 example 包（如存在）
- [X] T165 [US5] 将 fixed-length-decoder 合并到当前分支
- [X] T166 验证构建并提交变更

### IT29: string-codec

- [X] T167 [US5] 切换到分支 string-codec
- [X] T168 [P] [US3] 删除 mini-spring 文件夹
- [X] T169 [P] [US4] 删除 package-info.java 文件
- [X] T170 [US1] 迁移 example 包（如存在）
- [X] T171 [US5] 将 length-field-decoder 合并到当前分支
- [X] T172 验证构建并提交变更

**检查点**: IT22-IT29 已处理并合并

---

## 第六阶段: 用户故事5 - 分支处理 IT30-IT37（优先级: P1）

**目标**: 处理迭代 30-37（Bootstrap 和 Polish）

### IT30: abstract-bootstrap

- [X] T173 [US5] 切换到分支 abstract-bootstrap
- [X] T174 [P] [US3] 删除 mini-spring 文件夹
- [X] T175 [P] [US4] 删除 package-info.java 文件
- [X] T176 [US1] 迁移 example 包（如存在）
- [X] T177 [US5] 将 string-codec 合并到当前分支
- [X] T178 验证构建并提交变更

### IT31: server-bootstrap

- [X] T179 [US5] 切换到分支 server-bootstrap
- [X] T180 [P] [US3] 删除 mini-spring 文件夹
- [X] T181 [P] [US4] 删除 package-info.java 文件
- [X] T182 [US1] 迁移 example 包（如存在）
- [X] T183 [US5] 将 abstract-bootstrap 合并到当前分支
- [X] T184 验证构建并提交变更

### IT32: client-bootstrap

- [X] T185 [US5] 切换到分支 client-bootstrap
- [X] T186 [P] [US3] 删除 mini-spring 文件夹
- [X] T187 [P] [US4] 删除 package-info.java 文件
- [X] T188 [US1] 迁移 example 包（如存在）
- [X] T189 [US5] 将 server-bootstrap 合并到当前分支
- [X] T190 验证构建并提交变更

### IT33: event-loop-group

- [X] T191 [US5] 切换到分支 event-loop-group
- [X] T192 [P] [US3] 删除 mini-spring 文件夹
- [X] T193 [P] [US4] 删除 package-info.java 文件
- [X] T194 [US1] 迁移 example 包（如存在）
- [X] T195 [US5] 将 client-bootstrap 合并到当前分支
- [X] T196 验证构建并提交变更

### IT34: boss-worker-model

- [X] T197 [US5] 切换到分支 boss-worker-model
- [X] T198 [P] [US3] 删除 mini-spring 文件夹
- [X] T199 [P] [US4] 删除 package-info.java 文件
- [X] T200 [US1] 迁移 example 包（如存在）
- [X] T201 [US5] 将 event-loop-group 合并到当前分支
- [X] T202 验证构建并提交变更

### IT35: channel-chooser

- [X] T203 [US5] 切换到分支 channel-chooser
- [X] T204 [P] [US3] 删除 mini-spring 文件夹
- [X] T205 [P] [US4] 删除 package-info.java 文件
- [X] T206 [US1] 迁移 example 包（如存在）
- [X] T207 [US5] 将 boss-worker-model 合并到当前分支
- [X] T208 验证构建并提交变更

### IT36: idle-state-handler

- [X] T209 [US5] 切换到分支 idle-state-handler
- [X] T210 [P] [US3] 删除 mini-spring 文件夹
- [X] T211 [P] [US4] 删除 package-info.java 文件
- [X] T212 [US1] 迁移 example 包（如存在）
- [X] T213 [US5] 将 channel-chooser 合并到当前分支
- [X] T214 验证构建并提交变更

### IT37: logging-handler

- [X] T215 [US5] 切换到分支 logging-handler
- [X] T216 [P] [US3] 删除 mini-spring 文件夹
- [X] T217 [P] [US4] 删除 package-info.java 文件
- [X] T218 [US1] 迁移 example 包（如存在）
- [X] T219 [US5] 将 idle-state-handler 合并到当前分支
- [X] T220 验证构建并提交变更

**检查点**: IT30-IT37 已处理并合并

---

## 第七阶段: 用户故事2 - Changelog 重排序（优先级: P1）

**目标**: 在所有分支上将 changelog.md 条目按 IT01 → IT37 顺序重排

**独立测试**: `changelog.md` 在 Unreleased 之后显示 IT01，最后显示 IT37

- [X] T221 [US2] 使用 `git checkout main` 切换到主分支
- [X] T222 [US2] 读取当前 changelog.md 并提取所有迭代章节
- [X] T223 [US2] 按迭代编号重排章节（IT01 → IT37）到 `changelog.md`
- [X] T224 [US2] 确保每个章节包含：主要改动、知识点、关键单元测试
- [X] T225 [US2] 验证 changelog 格式并提交变更

**检查点**: 主分支上 Changelog 已正确排序

---

## 第八阶段: 用户故事5 - 最终合并到 Main（优先级: P1）

**目标**: 将 logging-handler 的所有变更合并到 main

- [X] T226 [US5] 使用 `git checkout main` 切换到主分支
- [X] T227 [US5] 使用 `git merge logging-handler --no-edit` 将 logging-handler 合并到 main
- [X] T228 [US5] 解决合并冲突（代码使用 --theirs，changelog 手动处理）
- [X] T229 使用 `mvn compile && mvn test` 验证最终构建（预期 434+ 测试）
- [X] T230 [US5] 提交最终合并变更

**检查点**: 主分支包含所有迭代变更

---

## 第九阶段: 收尾与验证（跨领域）

**目的**: 最终验证和清理

- [X] T231 验证主分支上 `src/main/java/io/netty/example` 不存在
- [X] T232 验证 `src/test/java/io/netty/example` 包含所有示例代码
- [X] T233 验证主分支上 `mini-spring` 文件夹不存在
- [X] T234 使用 `find . -name "package-info.java"` 验证没有 package-info.java 文件
- [X] T235 [P] 使用 `mvn test` 在主分支运行完整测试套件
- [X] T236 [P] 验证 changelog.md 顺序（IT01 在前，IT37 在后）
- [X] T237 抽查 3 个随机分支（如 IT10、IT20、IT30）结构是否正确
- [X] T238 验证通过后使用 `git branch -D main-backup` 删除备份分支
- [X] T239 运行 quickstart.md 验证清单

---

## 依赖与执行顺序

### 阶段依赖

- **准备工作（第一阶段）**: 无依赖 - 可立即开始
- **基础工作（第二阶段）**: 依赖准备工作 - 处理 IT01
- **分支处理（第三到六阶段）**: 阶段内顺序执行，每个 IT 依赖前一个 IT
- **Changelog（第七阶段）**: 可在第六阶段完成后在 main 上运行
- **最终合并（第八阶段）**: 依赖第三到七阶段完成
- **收尾（第九阶段）**: 依赖第八阶段完成

### 用户故事依赖

| 用户故事 | 描述 | 依赖 |
|----------|------|------|
| US1 | 示例代码迁移 | 无 - 应用到每个分支 |
| US2 | Changelog 重排序 | 所有分支处理完成后 |
| US3 | 删除 mini-spring | 无 - 应用到每个分支 |
| US4 | 删除 package-info | 无 - 应用到每个分支 |
| US5 | 分支合并 | 每个 IT 依赖前一个 IT |

### 并行机会

每个分支迭代（T0xx 组）内：
```bash
# 这些可以并行执行：
- 删除 mini-spring (US3)
- 删除 package-info (US4)

# 这些必须顺序执行：
- 切换 → 删除/迁移 → 验证 → 提交 → 合并
```

---

## 并行示例: 单分支处理

```bash
# IT05 内的并行操作：
任务 T030: "删除 mini-spring 文件夹"
任务 T031: "删除 package-info.java 文件"

# 并行后的顺序操作：
任务 T032: "迁移 example 包（如存在）"
任务 T033: "合并前一个分支"
任务 T034: "验证构建并提交"
```

---

## 实施策略

### 建议执行顺序

1. **完成第一阶段**: 准备脚本和备份
2. **处理 IT01（第二阶段）**: 建立基础
3. **处理 IT02-IT37（第三到六阶段）**: 顺序处理分支
4. **重排 Changelog（第七阶段）**: 在主分支上
5. **最终合并（第八阶段）**: 将 logging-handler 合并到 main
6. **验证（第九阶段）**: 完成验证

### 时间估算

- 准备工作：约 10 分钟
- 每个分支：约 5 分钟（IT01 之后 ×36 个分支）
- Changelog：约 15 分钟
- 最终合并与验证：约 20 分钟
- **总计**: 约 4-5 小时

### 恢复策略

如果某个分支验证失败：
1. 调查并修复问题
2. 重新运行 `mvn compile && mvn test`
3. 如无法修复，从前一个分支状态恢复
4. 记录问题以便手动解决

---

## 注意事项

- [P] 任务 = 可在同一分支内并行执行
- [US#] 标签将任务映射到特定用户故事
- 每个分支必须通过 `mvn compile && mvn test` 才能继续
- 合并冲突时使用 `--theirs`（优先保留旧分支代码）
- 提交消息格式：`refactor: 重构项目结构`
- IT12 不作为独立分支存在（已合并到 IT08）
