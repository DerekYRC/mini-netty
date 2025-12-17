#!/bin/bash
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

echo "=========================================="
echo "开始测试所有分支"
echo "=========================================="

FAILED_BRANCHES=()
SUCCESS_COUNT=0

for branch in "${BRANCHES[@]}"; do
    echo ""
    echo ">>> 测试分支: $branch"
    git checkout "$branch" -q 2>/dev/null
    if [ $? -ne 0 ]; then
        echo "✗ 无法切换到分支 $branch"
        FAILED_BRANCHES+=("$branch (checkout failed)")
        continue
    fi
    
    result=$(mvn test -q 2>&1)
    exit_code=$?
    echo "$result" | grep -E "Tests run|BUILD" | tail -2
    if [ $exit_code -eq 0 ]; then
        echo "✓ $branch 测试通过"
        ((SUCCESS_COUNT++))
    else
        echo "✗ $branch 测试失败"
        FAILED_BRANCHES+=("$branch")
    fi
done

echo ""
echo "=========================================="
echo "测试结果汇总"
echo "=========================================="
echo "成功: $SUCCESS_COUNT / ${#BRANCHES[@]}"
if [ ${#FAILED_BRANCHES[@]} -gt 0 ]; then
    echo "失败分支:"
    for fb in "${FAILED_BRANCHES[@]}"; do
        echo "  - $fb"
    done
else
    echo "所有分支测试通过！"
fi

git checkout main -q
