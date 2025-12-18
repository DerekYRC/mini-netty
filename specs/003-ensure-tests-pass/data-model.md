# 数据模型：确保所有分支单元测试通过

**日期**: 2025-12-18  
**功能**: 003-ensure-tests-pass

## 实体定义

### 1. Branch（分支）

| 属性 | 类型 | 说明 |
|------|------|------|
| name | String | 分支名称，如 "byte-buf-allocator" |
| type | Enum | 分支类型：SPEC, MAIN, FEATURE |
| testStatus | TestStatus | 测试状态（关联） |

### 2. TestStatus（测试状态）

| 属性 | 类型 | 说明 |
|------|------|------|
| branch | String | 关联分支名称 |
| status | Enum | PASS, FAIL, COMPILE_ERROR, NO_TESTS, UNKNOWN |
| testsRun | Integer | 执行的测试数量 |
| failures | Integer | 失败的测试数量 |
| errors | Integer | 错误的测试数量 |
| skipped | Integer | 跳过的测试数量 |
| executionTime | Duration | 执行耗时 |
| errorMessages | List<String> | 错误信息列表 |
| timestamp | DateTime | 测试执行时间 |

### 3. TestReport（测试报告）

| 属性 | 类型 | 说明 |
|------|------|------|
| generatedAt | DateTime | 报告生成时间 |
| totalBranches | Integer | 总分支数 |
| passedBranches | Integer | 通过分支数 |
| failedBranches | Integer | 失败分支数 |
| compileErrorBranches | Integer | 编译错误分支数 |
| results | List<TestStatus> | 各分支测试状态列表 |

### 4. TestFix（测试修复）

| 属性 | 类型 | 说明 |
|------|------|------|
| branch | String | 目标分支 |
| issue | String | 问题描述 |
| rootCause | String | 根因分析 |
| solution | String | 解决方案 |
| filesChanged | List<String> | 修改的文件列表 |
| fixStatus | Enum | PENDING, IN_PROGRESS, FIXED, SKIPPED |

## 状态流转

### TestStatus 状态机

```
UNKNOWN → COMPILE_ERROR (编译失败)
UNKNOWN → NO_TESTS (无测试文件)
UNKNOWN → PASS (测试通过)
UNKNOWN → FAIL (测试失败)

COMPILE_ERROR → PASS (修复后重新测试)
FAIL → PASS (修复后重新测试)
```

### TestFix 状态机

```
PENDING → IN_PROGRESS (开始修复)
IN_PROGRESS → FIXED (修复完成)
IN_PROGRESS → SKIPPED (无法修复，记录原因)
```

## 关系图

```
TestReport 1 ──── * TestStatus
                      │
                      ▼
Branch 1 ──── 1 TestStatus
                      │
                      ▼ (if FAIL or COMPILE_ERROR)
TestFix 1 ──── 1 TestStatus
```

## 验证规则

1. **TestStatus.testsRun** ≥ TestStatus.failures + TestStatus.errors + TestStatus.skipped
2. **TestStatus.status = PASS** 当且仅当 failures = 0 且 errors = 0
3. **TestStatus.status = COMPILE_ERROR** 当编译阶段失败时
4. **TestReport.totalBranches** = passedBranches + failedBranches + compileErrorBranches + noTestsBranches
