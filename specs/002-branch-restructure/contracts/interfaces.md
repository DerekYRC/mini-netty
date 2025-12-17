# Contracts: 分支重构操作接口

**Date**: 2025-12-17 | **Spec**: [spec.md](spec.md) | **Plan**: [plan.md](plan.md)

## Overview

本任务主要涉及 Shell 命令操作，以下定义了标准化的操作流程接口。

## Branch Processing Interface

### 1. checkout_branch

**描述**: 切换到指定分支

**输入**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| branchName | String | 是 | 分支名称 |

**命令**:
```bash
git checkout <branchName>
```

**输出**:
- 成功: 返回 `Switched to branch '<branchName>'`
- 失败: 返回错误信息

---

### 2. delete_mini_spring

**描述**: 删除 mini-spring 文件夹

**前置条件**: 当前目录为仓库根目录

**命令**:
```bash
rm -rf mini-spring
```

**输出**:
- 成功: 无输出（静默成功）
- 失败: 返回权限或路径错误

---

### 3. delete_package_info

**描述**: 删除所有 package-info.java 文件

**前置条件**: 当前目录为仓库根目录

**命令**:
```bash
find . -name "package-info.java" -type f -delete
```

**输出**:
- 成功: 无输出（静默成功）
- 失败: 返回路径错误

---

### 4. migrate_example

**描述**: 将 example 包从 main 迁移到 test

**前置条件**: 
- `src/main/java/io/netty/example` 目录存在
- `src/test/java/io/netty` 目录存在

**命令**:
```bash
# 检查源目录是否存在
if [ -d "src/main/java/io/netty/example" ]; then
    mkdir -p src/test/java/io/netty/example
    cp -r src/main/java/io/netty/example/* src/test/java/io/netty/example/
    rm -rf src/main/java/io/netty/example
fi
```

**输出**:
- 成功: 无输出（静默成功）
- 跳过: 源目录不存在时跳过
- 失败: 返回路径或权限错误

---

### 5. verify_build

**描述**: 验证项目编译和测试

**命令**:
```bash
mvn compile -q && mvn test -q
```

**输出**:
- 成功: `BUILD SUCCESS`
- 失败: 编译错误或测试失败信息

---

### 6. commit_changes

**描述**: 提交所有变更

**输入**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| message | String | 是 | 提交消息 |

**命令**:
```bash
git add -A
git commit -m "<message>"
```

**输出**:
- 成功: 提交哈希和摘要
- 无变更: `nothing to commit, working tree clean`

---

### 7. merge_branch

**描述**: 合并前一个分支

**输入**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| sourceBranch | String | 是 | 被合并的分支 |

**命令**:
```bash
git merge <sourceBranch> --no-edit
```

**输出**:
- 成功: 合并摘要
- 冲突: 冲突文件列表

---

### 8. resolve_conflict

**描述**: 解决合并冲突（优先保留被合并分支）

**输入**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| conflictFile | String | 是 | 冲突文件路径 |

**命令**:
```bash
git checkout --theirs <conflictFile>
git add <conflictFile>
```

**输出**:
- 成功: 无输出

---

## Complete Processing Script

```bash
#!/bin/bash
# process_branch.sh - 处理单个分支

BRANCH=$1
PREV_BRANCH=$2

# Step 1: Checkout
git checkout "$BRANCH"

# Step 2: Delete mini-spring
rm -rf mini-spring

# Step 3: Delete package-info.java
find . -name "package-info.java" -type f -delete

# Step 4: Migrate example (if exists)
if [ -d "src/main/java/io/netty/example" ]; then
    mkdir -p src/test/java/io/netty/example
    cp -r src/main/java/io/netty/example/* src/test/java/io/netty/example/
    rm -rf src/main/java/io/netty/example
fi

# Step 5: Verify build
mvn compile -q || exit 1
mvn test -q || exit 1

# Step 6: Commit
git add -A
git commit -m "refactor: 重构项目结构" --allow-empty

# Step 7: Merge previous branch (if not first)
if [ -n "$PREV_BRANCH" ]; then
    git merge "$PREV_BRANCH" --no-edit || {
        # Resolve conflicts by keeping theirs
        git checkout --theirs .
        git add -A
        git commit --no-edit
    }
fi

echo "Branch $BRANCH processed successfully"
```

## Branch Order

```
BRANCHES=(
    "simple-bio-server"
    "simple-bio-client"
    "multi-thread-bio-server"
    "nio-channel-buffer"
    "nio-selector"
    "nio-server-accept"
    "nio-server-read-write"
    "event-loop-interface"
    "single-thread-event-loop"
    "event-loop-task-queue"
    "event-loop-scheduled-task"
    "nio-channel-impl"
    "channel-config"
    "channel-unsafe"
    "channel-handler-interface"
    "channel-pipeline-basic"
    "channel-handler-context"
    "inbound-handler"
    "outbound-handler"
    "handler-adapter"
    "bytebuf-interface"
    "heap-byte-buf"
    "byte-buf-reference-count"
    "byte-buf-allocator"
    "byte-to-message-decoder"
    "fixed-length-decoder"
    "length-field-decoder"
    "string-codec"
    "abstract-bootstrap"
    "server-bootstrap"
    "client-bootstrap"
    "event-loop-group"
    "boss-worker-model"
    "channel-chooser"
    "idle-state-handler"
    "logging-handler"
    "main"
)
```
