# 快速入门：确保所有分支单元测试通过

**日期**: 2025-12-18  
**功能**: 003-ensure-tests-pass

## 概述

本功能的目标是验证 mini-netty 项目所有 42 个分支的单元测试全部通过，并修复任何发现的测试失败或编译错误。

## 前置条件

- ✅ Java 17 已安装
- ✅ Maven 3.6+ 已安装
- ✅ Git 已安装
- ✅ 项目已克隆且有所有本地分支

## 快速执行

### 1. 执行单个分支测试

```bash
# 检出分支并运行测试
git checkout <branch-name>
mvn test
```

### 2. 批量测试所有分支

```bash
# 使用脚本批量测试（在项目根目录执行）
for branch in $(git branch | sed 's/^[ *]//'); do
  echo "=== 测试分支: $branch ==="
  git checkout "$branch" 2>/dev/null
  mvn test -q 2>&1 | tail -5
done
```

### 3. 查看测试报告

测试报告位于：`specs/003-ensure-tests-pass/test-report.md`

## 关键命令

| 操作 | 命令 |
|------|------|
| 运行所有测试 | `mvn test` |
| 运行单个测试类 | `mvn test -Dtest=ClassName` |
| 运行单个测试方法 | `mvn test -Dtest=ClassName#methodName` |
| 安静模式 | `mvn test -q` |
| 跳过测试 | `mvn compile -DskipTests` |

## 预期结果

- **42 个分支**全部测试通过
- **0 个**编译错误
- **0 个**测试失败
- **完整测试报告**已生成

## 故障排除

### 编译错误

如果分支存在编译错误：
1. 查看错误信息确定问题类型
2. 检查接口定义是否一致
3. 修复后重新测试

### 测试失败

如果测试失败：
1. 查看失败测试的堆栈跟踪
2. 定位失败断言
3. 分析预期值与实际值差异
4. 修复代码或测试

### 测试超时

如果测试执行时间过长：
1. 检查是否有无限循环
2. 检查网络相关测试的超时设置
3. 考虑使用 mock 替代真实网络操作
