# Research: 分支重构与项目清理

**Date**: 2025-12-17 | **Spec**: [spec.md](spec.md) | **Plan**: [plan.md](plan.md)

## 1. Git Branch Merge Strategy

### Decision
使用 `git merge --no-edit` 进行分支合并，冲突时优先保留旧分支的改动。

### Rationale
- 旧分支的改动是迭代的基础，必须保留
- 新分支的重构改动是统一的（删除 mini-spring、迁移 example）
- `--no-edit` 自动使用默认合并消息，提高效率

### Alternatives Considered
- **Rebase**: 会改变提交历史，不适合已推送的分支
- **Cherry-pick**: 需要选择特定提交，不如 merge 完整
- **Squash merge**: 会丢失提交历史，不便于追溯

## 2. Example Package Migration

### Decision
将 `src/main/java/io/netty/example` 整个目录移动到 `src/test/java/io/netty/example`。

### Rationale
- 示例代码不是库的一部分，应该在测试包中
- 保持与 Netty 官方项目结构一致
- 避免打包时包含示例代码

### Alternatives Considered
- **单独模块**: 创建 example 子模块，增加项目复杂度
- **保留在 main**: 违反约定，示例不应打包到 JAR

## 3. mini-spring Folder Deletion

### Decision
直接删除 mini-spring 文件夹，包括其 .git 目录。

### Rationale
- mini-spring 是独立项目，有自己的 git 仓库
- 与 Mini-Netty 项目无关，不应出现在此仓库
- 删除后减少仓库体积和混淆

### Alternatives Considered
- **移动到其他位置**: 仍会污染仓库
- **保留为子模块**: 增加复杂度，且与项目目标不符

## 4. package-info.java Handling

### Decision
删除所有 `package-info.java` 文件。

### Rationale
- 当前的 package-info.java 只包含简单的包描述
- 作为学习项目，不需要详细的包级文档
- 简化项目结构

### Files to Delete
1. `src/main/java/io/netty/package-info.java`
2. `src/main/java/io/netty/example/echo/package-info.java`

### Alternatives Considered
- **保留并更新**: 增加维护成本，收益低
- **移动到测试包**: package-info 不需要在测试中

## 5. Changelog Ordering

### Decision
按迭代顺序（IT01 → IT37）排列 changelog.md 条目。

### Rationale
- 时间顺序便于阅读和理解学习路径
- 与分支迭代顺序一致
- 符合用户期望的自然阅读顺序

### Format
```markdown
## [IT01] simple-bio-server
**分支**: `simple-bio-server`
**日期**: YYYY-MM-DD
**改动内容**: ...
**学习要点**: ...
**测试**: ...
```

## 6. Verification Strategy

### Decision
每个分支处理后执行 `mvn compile` 和 `mvn test`。

### Rationale
- 编译验证代码移动后的引用正确性
- 测试验证功能完整性
- 早期发现问题，避免问题累积

### Success Criteria
- `mvn compile` 返回 BUILD SUCCESS
- `mvn test` 返回 BUILD SUCCESS 且所有测试通过

## 7. Conflict Resolution

### Decision
合并冲突时，优先保留旧分支（被合并分支）的内容。

### Rationale
- 旧分支的代码是迭代的核心实现
- 新分支的改动主要是删除和移动文件
- 保留旧分支确保功能不丢失

### Strategy
```bash
# 冲突时使用旧分支版本
git checkout --theirs <conflicting-file>
```

## 8. Same-Name File Handling

### Decision
迁移 example 包时，如果测试包中存在同名文件，直接覆盖。

### Rationale
- 测试包中不应该有与 example 同名的文件
- 如果有，说明是错误放置的文件
- 覆盖后统一管理

### Implementation
```bash
# 使用 mv -f 强制覆盖
mv -f src/main/java/io/netty/example/* src/test/java/io/netty/example/
```
