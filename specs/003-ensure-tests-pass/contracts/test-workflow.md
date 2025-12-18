# 契约定义：测试执行流程

**日期**: 2025-12-18  
**功能**: 003-ensure-tests-pass

## 操作契约

### 1. 执行分支测试 (ExecuteBranchTest)

**输入**:
- branchName: String (必填) - 分支名称

**前置条件**:
- 分支必须存在于本地仓库
- 工作目录无未提交的更改（或已stash）

**操作步骤**:
1. `git checkout {branchName}`
2. `mvn test -q`
3. 解析测试输出

**输出**: TestStatus
- status: PASS | FAIL | COMPILE_ERROR | NO_TESTS
- testsRun: Integer
- failures: Integer
- errors: Integer
- skipped: Integer
- errorMessages: List<String>

**后置条件**:
- 测试状态已记录
- 工作目录仍在测试分支

---

### 2. 生成测试报告 (GenerateTestReport)

**输入**:
- testResults: List<TestStatus> - 所有分支测试结果

**前置条件**:
- 所有分支测试已执行完成

**操作步骤**:
1. 汇总各状态分支数量
2. 按状态分组排序（失败优先）
3. 生成 Markdown 格式报告

**输出**: TestReport
- generatedAt: DateTime
- totalBranches: Integer
- passedBranches: Integer
- failedBranches: Integer
- compileErrorBranches: Integer
- results: List<TestStatus>

**后置条件**:
- 报告文件已生成在 specs/003-ensure-tests-pass/ 目录

---

### 3. 修复测试失败 (FixTestFailure)

**输入**:
- testStatus: TestStatus (status = FAIL 或 COMPILE_ERROR)

**前置条件**:
- 已执行测试并发现失败
- 已分析根因

**操作步骤**:
1. 检出目标分支
2. 定位失败测试或编译错误
3. 分析根因
4. 应用修复
5. 重新执行测试验证

**输出**: TestFix
- issue: String - 问题描述
- rootCause: String - 根因
- solution: String - 解决方案
- filesChanged: List<String>
- fixStatus: FIXED | SKIPPED

**后置条件**:
- 修复已提交到分支
- 测试状态更新为 PASS

---

## 输出格式契约

### CSV 测试结果格式

```csv
分支,状态,测试数,失败数,错误数,跳过数,耗时(秒)
branch-name,PASS,10,0,0,0,5
branch-name,FAIL,10,2,0,0,5
branch-name,COMPILE_ERROR,0,0,0,0,0
```

### Markdown 报告格式

```markdown
# 测试状态报告

**生成时间**: YYYY-MM-DD HH:MM:SS
**总分支数**: N
**通过**: N | **失败**: N | **编译错误**: N

## 失败分支

| 分支 | 测试数 | 失败数 | 错误数 | 错误信息 |
|------|--------|--------|--------|----------|
| xxx  | 10     | 2      | 0      | xxx      |

## 编译错误分支

| 分支 | 错误信息 |
|------|----------|
| xxx  | xxx      |

## 通过分支

| 分支 | 测试数 | 耗时 |
|------|--------|------|
| xxx  | 10     | 5s   |
```
