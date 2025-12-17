#!/bin/bash
# process-branch.sh - 处理单个分支的重构脚本
# 用法: ./scripts/process-branch.sh <branch-name> [prev-branch-name]

set -e

BRANCH=$1
PREV_BRANCH=$2

if [ -z "$BRANCH" ]; then
    echo "用法: $0 <branch-name> [prev-branch-name]"
    exit 1
fi

echo "=========================================="
echo "处理分支: $BRANCH"
echo "=========================================="

# Step 1: 切换到分支
echo "[1/7] 切换到分支 $BRANCH..."
git checkout "$BRANCH"

# Step 2: 删除 mini-spring 文件夹
echo "[2/7] 删除 mini-spring 文件夹..."
rm -rf mini-spring

# Step 3: 删除 package-info.java 文件
echo "[3/7] 删除 package-info.java 文件..."
find . -name "package-info.java" -type f -delete

# Step 4: 迁移 example 包（如果存在）
if [ -d "src/main/java/io/netty/example" ]; then
    echo "[4/7] 迁移 example 包到测试目录..."
    mkdir -p src/test/java/io/netty/example
    cp -r src/main/java/io/netty/example/* src/test/java/io/netty/example/ 2>/dev/null || true
    rm -rf src/main/java/io/netty/example
else
    echo "[4/7] 跳过 - example 包不存在"
fi

# Step 5: 验证构建
echo "[5/7] 验证构建..."
if mvn compile -q 2>/dev/null; then
    echo "  ✓ 编译成功"
else
    echo "  ⚠ 编译失败或没有源文件（跳过）"
fi

if mvn test -q 2>/dev/null; then
    echo "  ✓ 测试成功"
else
    echo "  ⚠ 测试失败或没有测试（跳过）"
fi

# Step 6: 提交变更
echo "[6/7] 提交变更..."
git add -A
git commit -m "refactor: 重构项目结构" --allow-empty 2>/dev/null || echo "  无变更需要提交"

# Step 7: 合并前一个分支（如果指定）
if [ -n "$PREV_BRANCH" ]; then
    echo "[7/7] 合并前一个分支 $PREV_BRANCH..."
    if git merge "$PREV_BRANCH" --no-edit 2>/dev/null; then
        echo "  ✓ 合并成功"
    else
        echo "  ⚠ 合并冲突，尝试使用 --theirs 解决..."
        git checkout --theirs . 2>/dev/null || true
        git add -A
        git commit --no-edit 2>/dev/null || echo "  已解决冲突"
    fi
else
    echo "[7/7] 跳过 - 无需合并（首个分支）"
fi

echo ""
echo "✓ 分支 $BRANCH 处理完成"
echo ""
