#!/bin/bash
#
# run-branch-tests.sh - 在指定分支上运行 Maven 测试并记录结果
#
# 用法: ./scripts/run-branch-tests.sh <branch-name> <csv-file>
#
# 输出: 将测试结果追加到 CSV 文件

set -e

BRANCH_NAME="$1"
CSV_FILE="$2"
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"

if [ -z "$BRANCH_NAME" ] || [ -z "$CSV_FILE" ]; then
    echo "用法: $0 <branch-name> <csv-file>"
    exit 1
fi

cd "$REPO_ROOT"

# 保存当前分支
CURRENT_BRANCH=$(git branch --show-current)

# 切换到目标分支
echo ">>> 切换到分支: $BRANCH_NAME"
git checkout "$BRANCH_NAME" 2>/dev/null

# 记录开始时间
START_TIME=$(date +%s)

# 执行测试
echo ">>> 运行测试..."
TEST_OUTPUT=$(mvn test 2>&1) || true

# 记录结束时间
END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

# 解析测试结果
if echo "$TEST_OUTPUT" | grep -q "BUILD SUCCESS"; then
    STATUS="PASS"
    # 提取测试统计信息
    TESTS_RUN=$(echo "$TEST_OUTPUT" | grep -oE "Tests run: [0-9]+" | tail -1 | grep -oE "[0-9]+" || echo "0")
    FAILURES=$(echo "$TEST_OUTPUT" | grep -oE "Failures: [0-9]+" | tail -1 | grep -oE "[0-9]+" || echo "0")
    ERRORS=$(echo "$TEST_OUTPUT" | grep -oE "Errors: [0-9]+" | tail -1 | grep -oE "[0-9]+" || echo "0")
    SKIPPED=$(echo "$TEST_OUTPUT" | grep -oE "Skipped: [0-9]+" | tail -1 | grep -oE "[0-9]+" || echo "0")
    ERROR_MSG=""
elif echo "$TEST_OUTPUT" | grep -q "BUILD FAILURE"; then
    if echo "$TEST_OUTPUT" | grep -q "Compilation failure\|compiler error\|编译失败"; then
        STATUS="COMPILE_ERROR"
        TESTS_RUN="0"
        FAILURES="0"
        ERRORS="0"
        SKIPPED="0"
        # 提取编译错误信息（取第一行）
        ERROR_MSG=$(echo "$TEST_OUTPUT" | grep -A2 "\[ERROR\]" | head -3 | tr '\n' ' ' | sed 's/,/ /g' | cut -c1-200)
    else
        STATUS="FAIL"
        TESTS_RUN=$(echo "$TEST_OUTPUT" | grep -oE "Tests run: [0-9]+" | tail -1 | grep -oE "[0-9]+" || echo "0")
        FAILURES=$(echo "$TEST_OUTPUT" | grep -oE "Failures: [0-9]+" | tail -1 | grep -oE "[0-9]+" || echo "0")
        ERRORS=$(echo "$TEST_OUTPUT" | grep -oE "Errors: [0-9]+" | tail -1 | grep -oE "[0-9]+" || echo "0")
        SKIPPED=$(echo "$TEST_OUTPUT" | grep -oE "Skipped: [0-9]+" | tail -1 | grep -oE "[0-9]+" || echo "0")
        # 提取失败测试信息
        ERROR_MSG=$(echo "$TEST_OUTPUT" | grep -A1 "Failed tests:" | tail -1 | tr '\n' ' ' | sed 's/,/ /g' | cut -c1-200)
    fi
else
    STATUS="UNKNOWN"
    TESTS_RUN="0"
    FAILURES="0"
    ERRORS="0"
    SKIPPED="0"
    ERROR_MSG="无法解析测试输出"
fi

# 设置默认值
TESTS_RUN=${TESTS_RUN:-0}
FAILURES=${FAILURES:-0}
ERRORS=${ERRORS:-0}
SKIPPED=${SKIPPED:-0}

# 追加结果到 CSV
echo "$BRANCH_NAME,$STATUS,$TESTS_RUN,$FAILURES,$ERRORS,$SKIPPED,$DURATION,\"$ERROR_MSG\"" >> "$CSV_FILE"

echo ">>> 结果: $STATUS (测试: $TESTS_RUN, 失败: $FAILURES, 错误: $ERRORS, 跳过: $SKIPPED, 耗时: ${DURATION}s)"

# 返回原分支
git checkout "$CURRENT_BRANCH" 2>/dev/null

exit 0
