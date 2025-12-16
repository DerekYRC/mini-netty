# Changelog

本文件记录 Mini-Netty 项目的所有改动，按迭代分支组织。

## 格式说明

每个迭代记录包含：
- **分支名**: 对应的 Git 分支
- **日期**: 完成日期
- **改动内容**: 本次迭代的具体改动
- **学习要点**: 本次迭代的核心知识点

---

## [Unreleased]

### 项目初始化

**日期**: 2025-12-16

**改动内容**:
- 创建项目基础结构
- 配置 Maven 构建（Java 17, JUnit 5, AssertJ）
- 创建 README.md 说明项目目的和学习路径
- 创建 changelog.md 记录改动

**学习要点**:
- Maven 项目结构
- JUnit 5 测试框架配置

---

## [IT01] simple-bio-server

**分支**: `simple-bio-server`
**日期**: 2025-12-16

**改动内容**:
- 新增 `SimpleBioServer` 类 (`io.netty.example.bio.SimpleBioServer`)
  - 实现最简单的阻塞式 I/O 服务端
  - 支持单客户端连接处理
  - 提供 `start()`, `stop()`, `startInBackground()` 方法
- 新增 `SimpleBioServerTest` 测试类
  - 测试服务端启动/停止
  - 测试客户端消息收发
  - 测试客户端断开连接处理

**学习要点**:
- `ServerSocket` 用于监听端口，`accept()` 方法阻塞等待连接
- `Socket` 代表一个 TCP 连接
- 阻塞 I/O 的特点：一个线程只能处理一个连接
- 使用 `BufferedReader` 和 `PrintWriter` 简化文本消息收发

---

*后续迭代记录将添加在此处*
