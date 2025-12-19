# Changelog

This document records all changes to the mini-netty project, organized by iteration branch.

## Format Description

Each iteration record contains:
- **Branch Name**: The corresponding Git branch
- **Changes**: Specific changes in this iteration
- **Learning Points**: Core knowledge points of this iteration

---

## [IT01] simple-bio-server

**Branch**: `simple-bio-server`

**Changes**:
- Added `SimpleBioServer` class (`io.netty.example.bio.SimpleBioServer`)
  - Implements simplest blocking I/O server
  - Supports single client connection handling
  - Provides `start()`, `stop()`, `startInBackground()` methods
- Added `SimpleBioServerTest` test class
  - Tests server start/stop
  - Tests client message send/receive
  - Tests client disconnection handling

**Learning Points**:
- `ServerSocket` listens on port, `accept()` blocks waiting for connection
- `Socket` represents a TCP connection
- Blocking I/O characteristic: one thread can only handle one connection
- Using `BufferedReader` and `PrintWriter` simplifies text message handling

---

## [IT02] simple-bio-client

**Branch**: `simple-bio-client`

**Changes**:
- Added `SimpleBioClient` class (`io.netty.example.bio.SimpleBioClient`)
  - Implements blocking I/O client
  - Supports `connect()`, `sendAndReceive()`, `close()` methods
  - Implements `AutoCloseable` interface for try-with-resources support
- Added `ClientServerIntegrationTest` integration test class
  - Tests client connecting to server
  - Tests message sending and receiving
  - Acceptance scenario: client sends "hello", receives "hello, mini-netty"

**Learning Points**:
- `Socket` constructor automatically initiates connection (blocking operation)
- Using `AutoCloseable` interface supports automatic resource release
- Integration tests verify client-server collaboration
- In BIO mode, single-threaded server can only process client requests sequentially

---

## [IT03] multi-thread-bio-server

**Branch**: `multi-thread-bio-server`

**Changes**:
- Added `MultiThreadBioServer` class (`io.netty.example.bio.MultiThreadBioServer`)
  - Uses `ExecutorService` thread pool to handle concurrent connections
  - Supports configurable thread pool size
  - Uses `AtomicInteger` to track active connections
- Added `ConcurrentClientTest` concurrency test class
  - Tests multiple clients connecting simultaneously
  - Tests concurrent message send/receive
  - Acceptance scenario: multiple clients connect simultaneously, each can send/receive normally

**Learning Points**:
- `ExecutorService` thread pool usage
- `CountDownLatch` for synchronizing multi-threaded tests
- BIO + thread pool model: solves single-thread concurrency limitation
- Thread pool size determines maximum concurrent connections
- Each connection occupies one thread, relatively high resource consumption

---

## [IT04] nio-channel-buffer

**Branch**: `nio-channel-buffer`

**Changes**:
- Added `NioChannelBufferDemo` class (`io.netty.example.nio.NioChannelBufferDemo`)
  - Demonstrates ByteBuffer basic operations
  - Demonstrates FileChannel read/write
  - Demonstrates compact() and direct buffer
- Added `NioChannelBufferTest` test class
  - Tests ByteBuffer allocate, flip, clear, compact operations
  - Tests FileChannel read/write
  - Tests wrap() and slice() methods

**Learning Points**:
- Buffer's three key properties: capacity, position, limit
- `flip()` switches to read mode: position→0, limit→original position
- `clear()` switches to write mode: position→0, limit→capacity
- `compact()` preserves unread data and switches to write mode
- Direct buffer (`allocateDirect`) vs heap buffer (`allocate`)
- Channel always reads/writes data from/to Buffer

---

## [IT05] nio-selector

**Branch**: `nio-selector`

**Changes**:
- Added `NioSelectorDemo` class (`io.netty.example.nio.NioSelectorDemo`)
  - Demonstrates basic Selector usage
  - Demonstrates multi-Channel registration
  - Demonstrates complete event loop
- Added `NioSelectorTest` test class
  - Tests Selector creation and closure
  - Tests Channel registration and event listening
  - Tests select(), selectNow(), wakeup() methods

**Learning Points**:
- Selector is the core of NIO multiplexing
- Channel must be in non-blocking mode to register with Selector
- Four event types: OP_ACCEPT(16), OP_CONNECT(8), OP_READ(1), OP_WRITE(4)
- `select()` blocks waiting for events, `selectNow()` doesn't block
- `wakeup()` can wake up blocked select()
- Must remove from selectedKeys after processing SelectionKey

---

## [IT06] nio-server-accept

**Branch**: `nio-server-accept`

**Changes**:
- Added `NioServer` class (`io.netty.example.nio.NioServer`)
  - Implements NIO-based server
  - Handles OP_ACCEPT events
  - Uses Selector event loop
  - Supports background startup and graceful shutdown
- Added `NioServerAcceptTest` test class
  - Tests server start/stop
  - Tests accepting single and multiple client connections
  - Tests client Channel registration for READ events

**Learning Points**:
- ServerSocketChannel must be configured to non-blocking mode
- Register OP_ACCEPT event to listen for new connections
- accept() returned SocketChannel also needs non-blocking configuration
- New connection Channels register OP_READ to prepare for data reception
- wakeup() used for graceful server shutdown

---

## [IT07] nio-server-read-write

**Branch**: `nio-server-read-write`

**Changes**:
- Updated `NioServer` class (`io.netty.example.nio.NioServer`)
  - Implemented OP_READ event handling (`handleRead`)
  - Implemented OP_WRITE event handling (`handleWrite`)
  - Pass response data through `SelectionKey.attach()`
  - Complete client communication flow
- Added `NioClient` class (`io.netty.example.nio.NioClient`)
  - NIO client based on SocketChannel
  - Supports connect, send, receive messages
  - Implements AutoCloseable interface
- Added `NioClientServerTest` integration test
  - Tests single client connection and communication
  - Tests multi-message sending
  - Tests multi-client concurrent access
  - Acceptance scenario tests

**Learning Points**:
- READ event: triggered when client sends data, read ByteBuffer and parse message
- WRITE event: switch to OP_WRITE by modifying interestOps
- SelectionKey.attach()/attachment() passes data between events
- ByteBuffer read/write requires correct flip() calls
- Client uses SocketChannel.open() and connect()
- Message protocol uses newline delimiters

---

## [IT08-IT17] EventLoop and Channel Architecture

**Summary of Core Components**:
- EventLoop: Single-threaded event processing with task queue and scheduling
- Channel: Network I/O abstraction with lifecycle management
- Pipeline: Handler chain for processing inbound/outbound events
- Unsafe: Internal interface for low-level I/O operations
- Configuration: Type-safe channel option management

**Key Architecture Patterns**:
- Template method pattern for extensible implementations
- Promise-based asynchronous operations
- Event-driven programming model
- Netty-style handler chain design

---

## Project Evolution Summary

### Phase 1: Basic I/O (IT01-IT03)
- Blocking I/O server/client implementation
- Thread pool for concurrency handling
- Foundation for understanding I/O models

### Phase 2: NIO Fundamentals (IT04-IT07)  
- ByteBuffer and Channel operations
- Selector-based multiplexing
- Complete NIO client-server implementation

### Phase 3: Netty Architecture (IT08-IT17)
- EventLoop abstraction and implementation
- Channel hierarchy and configuration
- Handler pipeline for event processing
- Unsafe interface for internal operations

**Learning Progression**: From simple blocking I/O to complete Netty-style architecture, demonstrating evolution from traditional socket programming to modern asynchronous I/O framework design.

---

*This changelog documents the 37-iteration learning journey of building a mini-Netty framework, progressing from basic concepts to advanced architectural patterns.*