#!/bin/bash
#
# generate-report.sh - 从 CSV 生成 Markdown 测试报告
#
# 用法: ./scripts/generate-report.sh
#

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
CSV_FILE="$REPO_ROOT/specs/003-ensure-tests-pass/test-results.csv"
REPORT_FILE="$REPO_ROOT/specs/003-ensure-tests-pass/test-report.md"

# 统计
TOTAL=$(tail -n +2 "$CSV_FILE" | wc -l | tr -d ' ')
PASS_COUNT=$(grep -c ",PASS," "$CSV_FILE" || echo "0")
FAIL_COUNT=$(grep -c ",FAIL," "$CSV_FILE" || echo "0")
COMPILE_ERROR_COUNT=$(grep -c ",COMPILE_ERROR," "$CSV_FILE" || echo "0")
UNKNOWN_COUNT=$(grep -c ",UNKNOWN," "$CSV_FILE" || echo "0")

# 生成报告
cat > "$REPORT_FILE" << EOF
# 测试状态报告

**生成时间**: $(date "+%Y-%m-%d %H:%M:%S")  
**总分支数**: $TOTAL  
**通过**: $PASS_COUNT | **失败**: $FAIL_COUNT | **编译错误**: $COMPILE_ERROR_COUNT | **未知**: $UNKNOWN_COUNT

---

EOF

# 失败分支
if [ "$FAIL_COUNT" -gt 0 ]; then
    echo "## ❌ 失败分支" >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"
    echo "| 分支 | 测试数 | 失败数 | 错误数 | 错误信息 |" >> "$REPORT_FILE"
    echo "|------|--------|--------|--------|----------|" >> "$REPORT_FILE"
    grep ",FAIL," "$CSV_FILE" | while IFS=',' read -r branch status tests failures errors skipped duration error_msg; do
        echo "| $branch | $tests | $failures | $errors | $error_msg |" >> "$REPORT_FILE"
    done
    echo "" >> "$REPORT_FILE"
fi

# 编译错误分支
if [ "$COMPILE_ERROR_COUNT" -gt 0 ]; then
    echo "## ⚠️ 编译错误分支" >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"
    echo "| 分支 | 错误信息 |" >> "$REPORT_FILE"
    echo "|------|----------|" >> "$REPORT_FILE"
    grep ",COMPILE_ERROR," "$CSV_FILE" | while IFS=',' read -r branch status tests failures errors skipped duration error_msg; do
        echo "| $branch | $error_msg |" >> "$REPORT_FILE"
    done
    echo "" >> "$REPORT_FILE"
fi

# 通过分支
if [ "$PASS_COUNT" -gt 0 ]; then
    echo "## ✅ 通过分支" >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"
    echo "| 分支 | 测试数 | 耗时(秒) |" >> "$REPORT_FILE"
    echo "|------|--------|----------|" >> "$REPORT_FILE"
    grep ",PASS," "$CSV_FILE" | while IFS=',' read -r branch status tests failures errors skipped duration error_msg; do
        echo "| $branch | $tests | $duration |" >> "$REPORT_FILE"
    done
    echo "" >> "$REPORT_FILE"
fi

echo "报告已生成: $REPORT_FILE"
