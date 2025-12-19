# mini-netty

[![Java](https://img.shields.io/badge/Java-17%2B-blue)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/Maven-3.6%2B-orange)](https://maven.apache.org/)
[![Tests](https://img.shields.io/badge/Tests-400+%20passing-brightgreen)]()
[![License](https://img.shields.io/badge/License-Educational-yellow)]()

**English | [简体中文](./README.md)**

**Sister Projects:**
- [**mini-spring**](https://github.com/DerekYRC/mini-spring) **(simplified Spring framework)**
- [**mini-spring-cloud**](https://github.com/DerekYRC/mini-spring-cloud) **(simplified Spring Cloud framework)**

## Project Introduction

**mini-netty** is a simplified **Netty** network programming framework implemented from scratch. Through 37 fine-grained iteration branches, it gradually introduces core concepts of network programming. This project uses **AI** (**Claude** + **Spec-Kit**) for assisted development.


### ✨ Features

- 📚 **Progressive Learning**: 37 iterations, each can run independently
- 🔬 **Complete Testing**: 400+ unit tests and integration tests
- 📖 **Detailed Documentation**: Each iteration has [changelog](https://github.com/DerekYRC/mini-netty/blob/main/changelog_en.md) records
- 🎯 **Zero Dependencies**: Pure JDK implementation, no third-party dependencies
- 🏗️ **Real Architecture**: Maintains the same package structure and naming as Netty

### 🏛️ Architecture Overview

```
                           ┌─────────────────────────────────────┐
                           │           ServerBootstrap           │
                           └──────────────────┬──────────────────┘
                                              │
                    ┌─────────────────────────┼─────────────────────────┐
                    │                         │                         │
           ┌────────┴────────┐      ┌────────┴────────┐      ┌────────┴────────┐
           │  BossGroup (1)  │      │ WorkerGroup (N) │      │  ChannelOption  │
           └────────┬────────┘      └────────┬────────┘      └─────────────────┘
                    │                        │
           ┌────────┴────────┐      ┌────────┴────────┐
           │  NioEventLoop   │      │  NioEventLoop   │ × N
           │  (Selector)     │      │  (Selector)     │
           └────────┬────────┘      └────────┬────────┘
                    │                        │
           ┌────────┴────────┐      ┌────────┴────────┐
           │ ServerChannel   │─────▶│  SocketChannel  │ × Connections
           └────────┬────────┘      └────────┬────────┘
                    │                        │
           ┌────────┴────────────────────────┴────────┐
           │              ChannelPipeline             │
           │  ┌─────┐   ┌─────┐   ┌─────┐   ┌─────┐   │
           │  │HEAD │◀─▶│ H1  │◀─▶│ H2  │◀─▶│TAIL │   │
           │  └─────┘   └─────┘   └─────┘   └─────┘   │
           │    ▼ Inbound                 Outbound ▲  │
           └──────────────────────────────────────────┘
```

### 📁 Project Structure

```
src/main/java/io/netty/
├── bootstrap/          # Bootstraps (ServerBootstrap, Bootstrap)
├── buffer/             # Buffers (ByteBuf, HeapByteBuf)
├── channel/            # Core channels
│   ├── nio/            # NIO implementation (NioEventLoop, NioChannel)
│   ├── Channel.java    # Channel interface
│   ├── ChannelPipeline.java
│   ├── ChannelHandler.java
│   └── EventLoop.java
└── handler/            # Built-in handlers
    ├── codec/          # Codecs
    ├── logging/        # Logging handlers
    └── timeout/        # Timeout handlers
```

## Learning Path

### Phase 1: Network Communication Basics (IT01-IT07)

Learn BIO and NIO programming models, understand the difference between blocking I/O and non-blocking I/O.

| Iteration | Branch Name | Learning Goal |
|-----------|-------------|---------------|
| IT01 | simple-bio-server | Simplest BIO server |
| IT02 | simple-bio-client | BIO client implementation |
| IT03 | multi-thread-bio-server | Multi-threading for concurrent connections |
| IT04 | nio-channel-buffer | NIO Channel and Buffer |
| IT05 | nio-selector | NIO Selector multiplexing |
| IT06 | nio-server-accept | NIO server ACCEPT event |
| IT07 | nio-server-read-write | Complete NIO read-write flow |

### Phase 2: EventLoop Event Loop (IT08-IT11)

Learn Netty's event loop mechanism, understand how single thread handles multiple connections.

| Iteration | Branch Name | Learning Goal |
|-----------|-------------|---------------|
| IT08 | event-loop-interface | EventLoop interface definition |
| IT09 | single-thread-event-loop | Single-threaded event loop implementation |
| IT10 | event-loop-task-queue | Task queue mechanism |
| IT11 | event-loop-scheduled-task | Scheduled task support |

### Phase 3: Channel and Pipeline (IT12-IT21)

Learn the channel abstraction and handler pipeline, understand data processing flow.

| Iteration | Branch Name | Learning Goal |
|-----------|-------------|---------------|
| IT12 | channel-interface | Channel interface design |
| IT13 | nio-socket-channel | NioSocketChannel implementation |
| IT14 | pipeline-handler-context | Pipeline and HandlerContext |
| IT15 | inbound-outbound-handler | Inbound and Outbound handlers |
| IT16 | channel-pipeline-fire-events | Event propagation mechanism |
| IT17 | channel-future-promise | Asynchronous programming model |
| IT18 | channel-handler-lifecycle | Handler lifecycle management |
| IT19 | exception-handling | Exception handling mechanism |
| IT20 | user-event-trigger | User event triggering |
| IT21 | channel-attr-option | Channel attributes and options |

### Phase 4: Bootstrap and Server (IT22-IT28)

Learn server startup process and client connection establishment.

| Iteration | Branch Name | Learning Goal |
|-----------|-------------|---------------|
| IT22 | server-bootstrap-basic | Basic ServerBootstrap |
| IT23 | server-channel-factory | Server channel factory |
| IT24 | event-loop-group | EventLoopGroup management |
| IT25 | boss-worker-pattern | Boss-Worker thread model |
| IT26 | client-bootstrap | Client bootstrap |
| IT27 | channel-initializer | Channel initialization |
| IT28 | full-server-client | Complete server-client communication |

### Phase 5: ByteBuf and Memory Management (IT29-IT32)

Learn Netty's buffer management and memory optimization.

| Iteration | Branch Name | Learning Goal |
|-----------|-------------|---------------|
| IT29 | bytebuf-interface | ByteBuf interface design |
| IT30 | heap-bytebuf | Heap-based ByteBuf |
| IT31 | direct-bytebuf | Direct memory ByteBuf |
| IT32 | bytebuf-allocator | ByteBuf allocation strategy |

### Phase 6: Built-in Handlers (IT33-IT37)

Learn commonly used built-in handlers and protocol support.

| Iteration | Branch Name | Learning Goal |
|-----------|-------------|---------------|
| IT33 | logging-handler | Logging handler |
| IT34 | string-codec | String codec |
| IT35 | length-field-codec | Length-field based codec |
| IT36 | idle-state-handler | Idle state detection |
| IT37 | complete-mini-netty | Complete mini-netty framework |

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.6+

### Clone and Build

```bash
git clone https://github.com/DerekYRC/mini-netty.git
cd mini-netty
mvn clean compile
```

### Run Tests

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=EchoServerTest
```

### Example Usage

```java
// Server
ServerBootstrap serverBootstrap = new ServerBootstrap();
serverBootstrap
    .group(bossGroup, workerGroup)
    .channel(NioServerSocketChannel.class)
    .childHandler(new ChannelInitializer<SocketChannel>() {
        @Override
        protected void initChannel(SocketChannel ch) {
            ch.pipeline()
                .addLast(new LoggingHandler())
                .addLast(new StringDecoder())
                .addLast(new StringEncoder())
                .addLast(new EchoServerHandler());
        }
    })
    .bind(8080)
    .sync();

// Client
Bootstrap bootstrap = new Bootstrap();
bootstrap
    .group(group)
    .channel(NioSocketChannel.class)
    .handler(new ChannelInitializer<SocketChannel>() {
        @Override
        protected void initChannel(SocketChannel ch) {
            ch.pipeline()
                .addLast(new StringDecoder())
                .addLast(new StringEncoder())
                .addLast(new EchoClientHandler());
        }
    })
    .connect("localhost", 8080)
    .sync();
```

## Usage

Read [changelog.md](https://github.com/DerekYRC/mini-netty/blob/main/changelog_en.md)

## Questions

[**Ask Questions Here**](https://github.com/DerekYRC/mini-netty/issues)

## Contributing

Pull Requests are welcome

## About Me

[**Learn More**](https://github.com/DerekYRC)

Phone/WeChat: **15975984828**  Email: **15975984828@163.com**

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=DerekYRC/mini-netty&type=Date)](https://star-history.com/#DerekYRC/mini-netty&Date)

## Copyright Notice

This project may not be used for commercial purposes without my written permission.

## References

- [Netty in Action](https://www.manning.com/books/netty-in-action)
- [Netty 4.x User Guide](https://netty.io/wiki/user-guide-for-4.x.html)
- [Java NIO Tutorial](https://jenkov.com/tutorials/java-nio/index.html)