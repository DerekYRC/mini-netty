# 快速开始: Mini-Netty

**创建日期**: 2025-12-16
**目的**: 提供 Mini-Netty 的快速入门指南和验证步骤

## 环境要求

- **JDK**: 17 或更高版本
- **Maven**: 3.6 或更高版本
- **Git**: 用于分支管理

## 项目初始化

### 1. 项目目录

```bash
# 使用仓库根目录作为项目目录，无需创建子目录
cd /path/to/mini-netty  # 仓库根目录
```

### 2. 创建 pom.xml（在仓库根目录）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.netty</groupId>
    <artifactId>mini-netty</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>mini-netty</name>
    <description>简化版Netty网络编程框架，用于学习Netty核心原理</description>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <junit.version>5.10.0</junit.version>
        <assertj.version>3.24.2</assertj.version>
    </properties>

    <dependencies>
        <!-- 测试依赖 -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <version>${assertj.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.1.2</version>
            </plugin>
        </plugins>
    </build>
</project>
```

## 迭代开发流程

每个迭代遵循以下步骤：

### 1. 创建新分支

```bash
# 基于前一个迭代分支创建新分支
git checkout -b <迭代名称>

# 示例：创建第一个迭代分支
git checkout -b simple-bio-server
```

### 2. 实现功能

按照 changelog.md 中的描述实现功能代码。

### 3. 编写测试

```java
// 单元测试示例
@Test
void testXxx() {
    // Given
    // ...
    
    // When
    // ...
    
    // Then
    assertThat(result).isEqualTo(expected);
}
```

### 4. 运行测试

```bash
mvn test
```

### 5. 启动服务端验证

```bash
# 编译
mvn compile

# 运行服务端（根据迭代内容调整主类）
mvn exec:java -Dexec.mainClass="io.netty.example.EchoServer"
```

### 6. 启动客户端验证

```bash
# 在另一个终端运行客户端
mvn exec:java -Dexec.mainClass="io.netty.example.EchoClient"
```

### 7. 更新 changelog.md

在 changelog.md 中记录本次迭代的改动点。

### 8. 提交代码

```bash
git add .
git commit -m "feat: <迭代名称> - <简要描述>"
```

## 第一个迭代: simple-bio-server

### 目标

实现最简单的 BIO（阻塞I/O）服务端，理解 Socket 编程基础。

### 代码示例

**服务端 (SimpleBioServer.java)**:

```java
package io.netty.example.bio;

import java.io.*;
import java.net.*;

public class SimpleBioServer {
    
    public static void main(String[] args) throws IOException {
        int port = 8080;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("服务端启动，监听端口: " + port);
            
            while (true) {
                // 阻塞等待客户端连接
                Socket clientSocket = serverSocket.accept();
                System.out.println("客户端连接: " + clientSocket.getRemoteSocketAddress());
                
                // 处理客户端请求
                handleClient(clientSocket);
            }
        }
    }
    
    private static void handleClient(Socket socket) {
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("收到消息: " + line);
                out.println("hello, mini-netty: " + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

### 测试

```java
package io.netty.example.bio;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class SimpleBioServerTest {
    
    @Test
    void testServerStartsAndAcceptsConnection() throws Exception {
        // Given: 启动服务端（后台线程）
        int port = 9999;
        Thread serverThread = new Thread(() -> {
            try {
                SimpleBioServer.start(port);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
        Thread.sleep(500); // 等待服务端启动
        
        // When: 客户端连接并发送消息
        try (Socket socket = new Socket("localhost", port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(
                 new InputStreamReader(socket.getInputStream()))) {
            
            out.println("hello");
            String response = in.readLine();
            
            // Then: 收到响应
            assertThat(response).contains("hello, mini-netty");
        }
    }
}
```

## 验证清单

每个迭代完成后，验证以下内容：

- [ ] 代码编译通过 (`mvn compile`)
- [ ] 所有测试通过 (`mvn test`)
- [ ] 服务端能正常启动
- [ ] 客户端能连接并通信
- [ ] changelog.md 已更新
- [ ] 代码已提交到对应分支

## 常用命令

```bash
# 编译项目
mvn compile

# 运行测试
mvn test

# 清理并构建
mvn clean package

# 查看测试覆盖率
mvn jacoco:report

# 切换分支
git checkout <分支名>

# 查看所有分支
git branch -a
```

## 学习路径建议

1. **基础篇 (1-21)**：按顺序学习，每个分支代表一个概念
2. **扩展篇 (22-37)**：可根据兴趣选择性学习
3. **对照阅读**：每完成一个分支后，对照 Netty 源码加深理解
4. **动手实践**：尝试修改代码，观察行为变化

## 问题排查

### Q: 端口被占用

```bash
# 查找占用端口的进程
lsof -i :8080

# 终止进程
kill -9 <PID>
```

### Q: 测试失败

1. 检查端口是否被其他测试占用
2. 确保使用不同的测试端口
3. 添加适当的等待时间

### Q: 类找不到

确保 `mvn compile` 已执行成功。
