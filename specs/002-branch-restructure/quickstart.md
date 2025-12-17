# Quickstart: 分支重构与项目清理

**Date**: 2025-12-17 | **Spec**: [spec.md](spec.md) | **Plan**: [plan.md](plan.md)

## 概述

本文档描述如何执行分支重构任务，将 37 个迭代分支按顺序处理并合并。

## 前置条件

- Git 已安装
- Maven 3.9+ 已安装
- JDK 17 已配置
- 所有分支已拉取到本地

## 快速开始

### 1. 验证环境

```bash
cd /Users/deyi/idea/mini-netty
git branch --list | wc -l  # 应该显示 37+ 个分支
mvn --version              # 确认 Maven 版本
java --version             # 确认 Java 17
```

### 2. 处理第一个分支

```bash
# 切换到第一个分支
git checkout simple-bio-server

# 删除 mini-spring
rm -rf mini-spring

# 删除 package-info.java
find . -name "package-info.java" -type f -delete

# 迁移 example 包（如果存在）
if [ -d "src/main/java/io/netty/example" ]; then
    mkdir -p src/test/java/io/netty/example
    cp -r src/main/java/io/netty/example/* src/test/java/io/netty/example/
    rm -rf src/main/java/io/netty/example
fi

# 验证构建
mvn compile
mvn test

# 提交变更
git add -A
git commit -m "refactor: 重构项目结构"
```

### 3. 处理后续分支

对于 IT02 及之后的分支，需要额外执行合并操作：

```bash
# 切换到下一个分支
git checkout simple-bio-client

# 执行相同的清理操作...

# 合并前一个分支
git merge simple-bio-server --no-edit

# 验证并提交
mvn compile && mvn test
git add -A
git commit -m "refactor: 合并 simple-bio-server"
```

### 4. 处理合并冲突

如果遇到冲突：

```bash
# 查看冲突文件
git status

# 优先保留旧分支的改动
git checkout --theirs <conflicting-file>

# 继续合并
git add -A
git commit --no-edit
```

## 分支处理顺序

按以下顺序处理：

1. `simple-bio-server` (IT01)
2. `simple-bio-client` (IT02)
3. `multi-thread-bio-server` (IT03)
4. ... (参见 [plan.md](plan.md) 完整列表)
37. `logging-handler` (IT37)
38. `main`

## 验证清单

每个分支处理后检查：

- [ ] `rm -rf mini-spring` 已执行
- [ ] `find . -name "package-info.java"` 返回空
- [ ] `ls src/main/java/io/netty/example` 返回 "No such file"（如果迁移了）
- [ ] `mvn compile` 返回 BUILD SUCCESS
- [ ] `mvn test` 返回 BUILD SUCCESS
- [ ] 所有变更已提交

## 预期结果

完成后：

1. 所有分支中 `mini-spring` 文件夹已删除
2. 所有分支中 `package-info.java` 已删除
3. `io.netty.example` 包已迁移到 `src/test/java`
4. 每个分支都包含前一个分支的所有改动
5. `main` 分支包含所有迭代的完整代码
