# Data Model: 分支重构与项目清理

**Date**: 2025-12-17 | **Spec**: [spec.md](spec.md) | **Plan**: [plan.md](plan.md)

## Entities

本任务主要涉及文件操作和 Git 操作，不涉及代码实体建模。以下是操作对象的描述。

### 1. Branch

**描述**: Git 分支，代表一个迭代版本。

| 字段 | 类型 | 说明 |
|------|------|------|
| name | String | 分支名称，如 `simple-bio-server` |
| iterationNumber | Integer | 迭代编号，如 IT01 → 1 |
| status | Enum | PENDING, PROCESSING, COMPLETED, FAILED |

**状态转换**:
```
PENDING → PROCESSING → COMPLETED
                    ↘ FAILED
```

### 2. FileOperation

**描述**: 文件操作记录。

| 字段 | 类型 | 说明 |
|------|------|------|
| type | Enum | DELETE, MOVE, MODIFY |
| sourcePath | String | 源文件路径 |
| targetPath | String | 目标文件路径（仅 MOVE 时） |
| status | Enum | SUCCESS, FAILED, SKIPPED |

### 3. ChangelogEntry

**描述**: changelog.md 中的迭代条目。

| 字段 | 类型 | 说明 |
|------|------|------|
| iterationNumber | Integer | 迭代编号 |
| branchName | String | 分支名称 |
| date | Date | 日期 |
| changes | List<String> | 改动内容 |
| learningPoints | List<String> | 学习要点 |
| tests | List<String> | 关键测试 |

## File Paths

### 需要删除的文件/目录

| 路径 | 类型 | 说明 |
|------|------|------|
| `mini-spring/` | Directory | 独立项目，需删除 |
| `src/main/java/io/netty/package-info.java` | File | 包描述文件 |
| `src/main/java/io/netty/example/echo/package-info.java` | File | 包描述文件 |

### 需要迁移的目录

| 源路径 | 目标路径 |
|--------|----------|
| `src/main/java/io/netty/example/` | `src/test/java/io/netty/example/` |

### Example 包结构

```
src/main/java/io/netty/example/
├── bio/
│   ├── SimpleBioServer.java
│   └── SimpleBioClient.java
├── nio/
│   ├── SimpleNioServer.java
│   └── SimpleNioClient.java
└── echo/
    ├── EchoServerHandler.java
    ├── EchoClientHandler.java
    └── package-info.java (需删除)
```

## Processing Order

按迭代顺序处理分支，确保合并时依赖关系正确：

```
IT01 → IT02 → IT03 → ... → IT37 → main
```

每个分支处理后合并到下一个分支：
```
checkout IT02
process IT02
merge IT01 into IT02
verify build
commit
```
