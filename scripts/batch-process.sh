#!/bin/bash
# batch-process.sh - 批量处理分支的简化脚本（跳过测试）
# 用法: ./scripts/batch-process.sh

set -e

# 分支处理顺序
BRANCHES=(
    "event-loop-interface:nio-server-read-write"
    "single-thread-event-loop:event-loop-interface"
    "event-loop-task-queue:single-thread-event-loop"
    "event-loop-scheduled-task:event-loop-task-queue"
    "nio-channel-impl:event-loop-scheduled-task"
    "channel-config:nio-channel-impl"
    "channel-unsafe:channel-config"
    "channel-handler-interface:channel-unsafe"
    "channel-pipeline-basic:channel-handler-interface"
    "channel-handler-context:channel-pipeline-basic"
    "inbound-handler:channel-handler-context"
    "outbound-handler:inbound-handler"
    "handler-adapter:outbound-handler"
    "bytebuf-interface:handler-adapter"
    "heap-byte-buf:bytebuf-interface"
    "byte-buf-reference-count:heap-byte-buf"
    "byte-buf-allocator:byte-buf-reference-count"
    "byte-to-message-decoder:byte-buf-allocator"
    "fixed-length-decoder:byte-to-message-decoder"
    "length-field-decoder:fixed-length-decoder"
    "string-codec:length-field-decoder"
    "abstract-bootstrap:string-codec"
    "server-bootstrap:abstract-bootstrap"
    "client-bootstrap:server-bootstrap"
    "event-loop-group:client-bootstrap"
    "boss-worker-model:event-loop-group"
    "channel-chooser:boss-worker-model"
    "idle-state-handler:channel-chooser"
    "logging-handler:idle-state-handler"
)

process_branch() {
    local BRANCH=$1
    local PREV_BRANCH=$2
    
    echo "=========================================="
    echo "处理分支: $BRANCH (合并自: $PREV_BRANCH)"
    echo "=========================================="
    
    # 切换分支
    git checkout "$BRANCH"
    
    # 删除 mini-spring
    rm -rf mini-spring
    
    # 删除 package-info.java
    find . -name "package-info.java" -type f -delete
    
    # 迁移 example 包
    if [ -d "src/main/java/io/netty/example" ]; then
        mkdir -p src/test/java/io/netty/example
        cp -r src/main/java/io/netty/example/* src/test/java/io/netty/example/ 2>/dev/null || true
        rm -rf src/main/java/io/netty/example
    fi
    
    # 提交变更
    git add -A
    git commit -m "refactor: 重构项目结构" --allow-empty 2>/dev/null || true
    
    # 合并前一个分支
    git merge "$PREV_BRANCH" --no-edit 2>/dev/null || {
        git checkout --theirs . 2>/dev/null || true
        git add -A
        git commit --no-edit 2>/dev/null || true
    }
    
    echo "✓ 分支 $BRANCH 处理完成"
    echo ""
}

# 处理所有分支
for item in "${BRANCHES[@]}"; do
    BRANCH="${item%%:*}"
    PREV="${item##*:}"
    process_branch "$BRANCH" "$PREV"
done

echo "=========================================="
echo "所有分支处理完成！"
echo "=========================================="
