# 任务清单：确保所有分支单元测试通过

**输入**: 来自 `/specs/003-ensure-tests-pass/` 的设计文档
**前置条件**: plan.md (必需), spec.md (必需), research.md, data-model.md, contracts/

**测试**: 本功能不涉及编写新测试，而是验证和修复现有测试。

**组织**: 任务按用户故事分组，以便独立实施和测试每个故事。

## 格式: `[ID] [P?] [Story?] 描述`

- **[P]**: 可并行执行（不同文件，无依赖）
- **[Story]**: 任务所属用户故事（如 US1, US2, US3）
- 描述中包含确切文件路径

---

## Phase 1: 准备工作 (Setup)

**目的**: 环境准备和工具配置

- [x] T001 确认 Java 17 和 Maven 已正确安装和配置
- [x] T002 确认所有 42 个本地分支存在
- [x] T003 创建测试结果 CSV 文件 specs/003-ensure-tests-pass/test-results.csv

---

## Phase 2: 基础设施 (Foundational)

**目的**: 建立测试执行和报告生成机制

**⚠️ 关键**: 在此阶段完成前无法开始用户故事工作

- [x] T004 创建分支测试执行脚本 scripts/run-branch-tests.sh
- [x] T005 [P] 创建测试结果解析逻辑（提取 testsRun, failures, errors, skipped）
- [x] T006 [P] 创建 Markdown 报告生成逻辑

**检查点**: 测试基础设施就绪 - 可以开始执行用户故事

---

## Phase 3: 用户故事 1 - 验证所有功能分支测试通过 (优先级: P1) 🎯 MVP

**目标**: 对所有 42 个分支执行单元测试，记录每个分支的测试状态

**独立测试**: 运行脚本后检查 test-results.csv 是否包含所有分支的测试结果

### 批次 1: BIO 和基础分支 (10 个分支)

- [x] T007 [US1] 测试分支 simple-bio-server 并记录结果
- [x] T008 [US1] 测试分支 simple-bio-client 并记录结果
- [x] T009 [US1] 测试分支 multi-thread-bio-server 并记录结果
- [x] T010 [US1] 测试分支 main 并记录结果
- [x] T011 [US1] 测试分支 main-backup 并记录结果
- [x] T012 [US1] 测试分支 001-mini-netty 并记录结果
- [x] T013 [US1] 测试分支 002-branch-restructure 并记录结果
- [x] T014 [US1] 测试分支 echo-example 并记录结果
- [x] T015 [US1] 测试分支 heap-byte-buf 并记录结果
- [x] T016 [US1] 测试分支 bytebuf-interface 并记录结果

### 批次 2: NIO 相关分支 (8 个分支)

- [x] T017 [US1] 测试分支 nio-selector 并记录结果
- [x] T018 [US1] 测试分支 nio-server-accept 并记录结果
- [x] T019 [US1] 测试分支 nio-server-read-write 并记录结果
- [x] T020 [US1] 测试分支 nio-channel-buffer 并记录结果
- [x] T021 [US1] 测试分支 nio-channel-impl 并记录结果
- [x] T022 [US1] 测试分支 single-thread-event-loop 并记录结果
- [x] T023 [US1] 测试分支 event-loop-interface 并记录结果
- [x] T024 [US1] 测试分支 event-loop-task-queue 并记录结果

### 批次 3: EventLoop 和 Channel 分支 (8 个分支)

- [x] T025 [US1] 测试分支 event-loop-group 并记录结果
- [x] T026 [US1] 测试分支 event-loop-scheduled-task 并记录结果
- [x] T027 [US1] 测试分支 channel-config 并记录结果
- [x] T028 [US1] 测试分支 channel-unsafe 并记录结果
- [x] T029 [US1] 测试分支 channel-chooser 并记录结果
- [x] T030 [US1] 测试分支 channel-handler-interface 并记录结果
- [x] T031 [US1] 测试分支 channel-handler-context 并记录结果
- [x] T032 [US1] 测试分支 channel-pipeline-basic 并记录结果

### 批次 4: Handler 相关分支 (8 个分支)

- [x] T033 [US1] 测试分支 inbound-handler 并记录结果
- [x] T034 [US1] 测试分支 outbound-handler 并记录结果
- [x] T035 [US1] 测试分支 handler-adapter 并记录结果
- [x] T036 [US1] 测试分支 logging-handler 并记录结果
- [x] T037 [US1] 测试分支 idle-state-handler 并记录结果
- [x] T038 [US1] 测试分支 string-codec 并记录结果
- [x] T039 [US1] 测试分支 byte-to-message-decoder 并记录结果
- [x] T040 [US1] 测试分支 fixed-length-decoder 并记录结果

### 批次 5: Bootstrap 和 Buffer 分支 (7 个分支)

- [x] T041 [US1] 测试分支 length-field-decoder 并记录结果
- [x] T042 [US1] 测试分支 abstract-bootstrap 并记录结果（已知编译错误）
- [x] T043 [US1] 测试分支 server-bootstrap 并记录结果
- [x] T044 [US1] 测试分支 client-bootstrap 并记录结果
- [x] T045 [US1] 测试分支 boss-worker-model 并记录结果
- [x] T046 [US1] 测试分支 byte-buf-allocator 并记录结果
- [x] T047 [US1] 测试分支 byte-buf-reference-count 并记录结果

**检查点**: 此时，所有 42 个分支的测试状态应已记录到 test-results.csv

---

## Phase 4: 用户故事 2 - 记录每个分支的测试状态 (优先级: P2)

**目标**: 生成清晰的测试状态报告，便于快速识别需要关注的分支

**独立测试**: 查看生成的报告，确认每个分支都有测试结果

### 报告生成

- [x] T048 [US2] 汇总 test-results.csv 数据，统计各状态分支数量
- [x] T049 [US2] 生成 Markdown 格式测试报告 specs/003-ensure-tests-pass/test-report.md
- [x] T050 [US2] 在报告中按状态分组（失败优先、编译错误次之、通过最后）
- [x] T051 [US2] 为失败分支记录详细错误信息

**检查点**: 此时，应有完整的测试状态报告可供查阅

---

## Phase 5: 用户故事 3 - 修复已识别的测试失败 (优先级: P1)

**目标**: 修复所有发现的测试失败和编译错误，达到 100% 通过

**独立测试**: 对每个修复后的分支重新运行测试，确认通过

### 编译错误修复

- [ ] T052 [US3] 分析 abstract-bootstrap 分支的编译错误根因
- [ ] T053 [US3] 修复 abstract-bootstrap 分支的 EventLoop 接口参数不匹配问题
- [ ] T054 [US3] 验证 abstract-bootstrap 分支修复后测试通过

### 测试失败修复（根据实际测试结果）

- [ ] T055 [US3] 分析并修复第一个失败分支（如有）
- [ ] T056 [US3] 分析并修复第二个失败分支（如有）
- [ ] T057 [US3] 分析并修复第三个失败分支（如有）
- [ ] T058 [US3] 分析并修复其他失败分支（根据实际情况扩展）

### 验证与确认

- [ ] T059 [US3] 重新运行所有分支测试，生成最终报告
- [ ] T060 [US3] 确认所有分支达到 PASS 状态

**检查点**: 此时，所有分支应达到 100% 测试通过

---

## Phase 6: 收尾与跨功能事项

**目的**: 完成文档和清理工作

- [ ] T061 [P] 更新 specs/003-ensure-tests-pass/test-report.md 为最终版本
- [ ] T062 [P] 更新 research.md 添加最终测试状态
- [ ] T063 提交所有修复到各自分支
- [ ] T064 运行 quickstart.md 验证流程可重复

---

## 依赖与执行顺序

### 阶段依赖

- **Setup (Phase 1)**: 无依赖 - 可立即开始
- **Foundational (Phase 2)**: 依赖 Setup 完成 - 阻塞所有用户故事
- **用户故事 (Phase 3+)**: 全部依赖 Foundational 完成
  - 用户故事 1 和 3 均为 P1，但 US1 必须先完成（需要先知道哪些分支失败）
  - 用户故事 2 可在 US1 完成后并行或顺序执行
- **收尾 (Final Phase)**: 依赖所有用户故事完成

### 用户故事依赖

- **用户故事 1 (P1)**: 可在 Foundational 完成后开始 - 无其他故事依赖
- **用户故事 2 (P2)**: 依赖 US1 完成（需要测试结果数据）
- **用户故事 3 (P1)**: 依赖 US1 完成（需要知道哪些分支失败）

### 各用户故事内部

- US1: 各批次可并行执行（不同分支）
- US2: 必须等 US1 全部完成
- US3: 各失败分支修复可并行执行

### 并行机会

- Phase 2 中标记 [P] 的任务可并行
- US1 中各批次的分支测试可并行执行
- US3 中各失败分支的修复可并行执行
- Phase 6 中标记 [P] 的任务可并行

---

## 并行示例

```bash
# US1: 并行测试多个分支
Task T007: "测试分支 simple-bio-server"
Task T008: "测试分支 simple-bio-client"
Task T009: "测试分支 multi-thread-bio-server"
# 可同时执行（不同分支，无冲突）

# US3: 并行修复多个失败分支
Task T052: "分析 abstract-bootstrap 编译错误"
Task T055: "分析并修复第一个失败分支"
# 可同时执行（不同分支，无冲突）
```

---

## 实施策略

### MVP 优先（仅用户故事 1）

1. 完成 Phase 1: Setup
2. 完成 Phase 2: Foundational
3. 完成 Phase 3: 用户故事 1（所有分支测试执行）
4. **停止并验证**: 查看 test-results.csv 确认所有分支已测试
5. 如需继续则进入 US2 和 US3

### 增量交付

1. Setup + Foundational → 基础就绪
2. 用户故事 1 → 所有分支已测试 → 有完整状态数据
3. 用户故事 2 → 生成报告 → 可视化进度
4. 用户故事 3 → 修复问题 → 100% 通过
5. 每个故事独立可验证

---

## 备注

- [P] 任务 = 不同文件/分支，无依赖冲突
- [Story] 标签用于追踪任务所属用户故事
- 每个用户故事应可独立完成和测试
- 每个任务或逻辑组完成后提交
- 避免: 模糊任务、同一分支冲突操作、破坏独立性的跨故事依赖
