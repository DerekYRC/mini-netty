#!/bin/bash
#
# run-all-tests.sh - 对所有分支执行测试
#
# 用法: ./scripts/run-all-tests.sh
#

set -e

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
CSV_FILE="$REPO_ROOT/specs/003-ensure-tests-pass/test-results.csv"
SCRIPT_DIR="$REPO_ROOT/scripts"

# 初始化 CSV 文件
echo "分支,状态,测试数,失败数,错误数,跳过数,耗时(秒),错误信息" > "$CSV_FILE"

# 获取所有分支（排除当前功能分支）
BRANCHES=$(git branch | grep -v "003-ensure-tests-pass" | sed 's/^[* ]*//')

TOTAL=$(echo "$BRANCHES" | wc -l | tr -d ' ')
COUNT=0

echo "=========================================="
echo "开始测试所有 $TOTAL 个分支"
echo "=========================================="

for branch in $BRANCHES; do
    COUNT=$((COUNT + 1))
    echo ""
    echo "[$COUNT/$TOTAL] 测试分支: $branch"
    echo "------------------------------------------"
    "$SCRIPT_DIR/run-branch-tests.sh" "$branch" "$CSV_FILE"
done

echo ""
echo "=========================================="
echo "所有分支测试完成！"
echo "结果保存到: $CSV_FILE"
echo "=========================================="

# 生成简要统计
PASS_COUNT=$(grep -c ",PASS," "$CSV_FILE" || echo "0")
FAIL_COUNT=$(grep -c ",FAIL," "$CSV_FILE" || echo "0")
COMPILE_ERROR_COUNT=$(grep -c ",COMPILE_ERROR," "$CSV_FILE" || echo "0")
UNKNOWN_COUNT=$(grep -c ",UNKNOWN," "$CSV_FILE" || echo "0")

echo ""
echo "统计摘要:"
echo "  ✅ 通过: $PASS_COUNT"
echo "  ❌ 失败: $FAIL_COUNT"
echo "  ⚠️  编译错误: $COMPILE_ERROR_COUNT"
echo "  ❓ 未知: $UNKNOWN_COUNT"
