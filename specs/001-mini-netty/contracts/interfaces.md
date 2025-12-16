# Mini-Netty 核心接口契约

**创建日期**: 2025-12-16
**目的**: 定义 Mini-Netty 的核心接口和方法签名

## 1. EventLoop 接口

```java
package io.netty.channel;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 事件循环接口，负责处理Channel的I/O事件和异步任务
 */
public interface EventLoop extends EventLoopGroup {
    
    /**
     * 返回父EventLoopGroup
     */
    EventLoopGroup parent();
    
    /**
     * 判断当前线程是否是EventLoop线程
     */
    boolean inEventLoop();
    
    /**
     * 判断指定线程是否是EventLoop线程
     */
    boolean inEventLoop(Thread thread);
    
    /**
     * 提交一个任务到EventLoop执行
     */
    void execute(Runnable task);
    
    /**
     * 提交一个定时任务
     */
    ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit);
    
    /**
     * 提交一个周期性任务
     */
    ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit);
}
```

## 2. Channel 接口

```java
package io.netty.channel;

import java.net.SocketAddress;

/**
 * 网络连接的抽象，代表一个可进行I/O操作的通道
 */
public interface Channel {
    
    /**
     * 返回Channel的唯一标识
     */
    ChannelId id();
    
    /**
     * 返回关联的EventLoop
     */
    EventLoop eventLoop();
    
    /**
     * 返回父Channel（服务端Channel返回null）
     */
    Channel parent();
    
    /**
     * 返回Channel配置
     */
    ChannelConfig config();
    
    /**
     * 返回ChannelPipeline
     */
    ChannelPipeline pipeline();
    
    /**
     * 判断Channel是否打开
     */
    boolean isOpen();
    
    /**
     * 判断Channel是否已注册到EventLoop
     */
    boolean isRegistered();
    
    /**
     * 判断Channel是否处于活动状态
     */
    boolean isActive();
    
    /**
     * 返回本地地址
     */
    SocketAddress localAddress();
    
    /**
     * 返回远程地址
     */
    SocketAddress remoteAddress();
    
    /**
     * 请求从Channel读取数据
     */
    Channel read();
    
    /**
     * 写入数据到Channel
     */
    ChannelFuture write(Object msg);
    
    /**
     * 刷新所有待写入的数据
     */
    Channel flush();
    
    /**
     * 写入并刷新
     */
    ChannelFuture writeAndFlush(Object msg);
    
    /**
     * 关闭Channel
     */
    ChannelFuture close();
    
    /**
     * 返回底层I/O操作接口
     */
    Unsafe unsafe();
    
    /**
     * 底层I/O操作接口（内部使用）
     */
    interface Unsafe {
        void register(EventLoop eventLoop, ChannelPromise promise);
        void bind(SocketAddress localAddress, ChannelPromise promise);
        void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise);
        void disconnect(ChannelPromise promise);
        void close(ChannelPromise promise);
        void read();
        void write(Object msg, ChannelPromise promise);
        void flush();
    }
}
```

## 3. ChannelPipeline 接口

```java
package io.netty.channel;

/**
 * Handler的容器，使用双向链表组织，负责事件的分发
 */
public interface ChannelPipeline {
    
    /**
     * 在链头添加Handler
     */
    ChannelPipeline addFirst(String name, ChannelHandler handler);
    
    /**
     * 在链尾添加Handler
     */
    ChannelPipeline addLast(String name, ChannelHandler handler);
    
    /**
     * 在指定Handler之前添加
     */
    ChannelPipeline addBefore(String baseName, String name, ChannelHandler handler);
    
    /**
     * 在指定Handler之后添加
     */
    ChannelPipeline addAfter(String baseName, String name, ChannelHandler handler);
    
    /**
     * 移除指定Handler
     */
    ChannelPipeline remove(ChannelHandler handler);
    
    /**
     * 获取关联的Channel
     */
    Channel channel();
    
    // ========== 入站事件触发 ==========
    
    ChannelPipeline fireChannelRegistered();
    ChannelPipeline fireChannelUnregistered();
    ChannelPipeline fireChannelActive();
    ChannelPipeline fireChannelInactive();
    ChannelPipeline fireChannelRead(Object msg);
    ChannelPipeline fireChannelReadComplete();
    ChannelPipeline fireExceptionCaught(Throwable cause);
    
    // ========== 出站事件触发 ==========
    
    ChannelFuture bind(java.net.SocketAddress localAddress);
    ChannelFuture connect(java.net.SocketAddress remoteAddress);
    ChannelFuture disconnect();
    ChannelFuture close();
    ChannelFuture write(Object msg);
    ChannelPipeline flush();
    ChannelFuture writeAndFlush(Object msg);
}
```

## 4. ChannelHandler 接口

```java
package io.netty.channel;

/**
 * 事件处理器基接口
 */
public interface ChannelHandler {
    
    /**
     * Handler被添加到Pipeline时调用
     */
    void handlerAdded(ChannelHandlerContext ctx) throws Exception;
    
    /**
     * Handler从Pipeline移除时调用
     */
    void handlerRemoved(ChannelHandlerContext ctx) throws Exception;
}

/**
 * 入站事件处理器
 */
public interface ChannelInboundHandler extends ChannelHandler {
    
    void channelRegistered(ChannelHandlerContext ctx) throws Exception;
    void channelUnregistered(ChannelHandlerContext ctx) throws Exception;
    void channelActive(ChannelHandlerContext ctx) throws Exception;
    void channelInactive(ChannelHandlerContext ctx) throws Exception;
    void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception;
    void channelReadComplete(ChannelHandlerContext ctx) throws Exception;
    void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception;
}

/**
 * 出站事件处理器
 */
public interface ChannelOutboundHandler extends ChannelHandler {
    
    void bind(ChannelHandlerContext ctx, java.net.SocketAddress localAddress, ChannelPromise promise) throws Exception;
    void connect(ChannelHandlerContext ctx, java.net.SocketAddress remoteAddress, java.net.SocketAddress localAddress, ChannelPromise promise) throws Exception;
    void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception;
    void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception;
    void read(ChannelHandlerContext ctx) throws Exception;
    void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception;
    void flush(ChannelHandlerContext ctx) throws Exception;
}
```

## 5. ChannelHandlerContext 接口

```java
package io.netty.channel;

/**
 * 连接Handler和Pipeline，提供事件传递能力
 */
public interface ChannelHandlerContext {
    
    /**
     * 获取关联的Channel
     */
    Channel channel();
    
    /**
     * 获取关联的EventLoop
     */
    EventLoop executor();
    
    /**
     * 获取Handler名称
     */
    String name();
    
    /**
     * 获取关联的Handler
     */
    ChannelHandler handler();
    
    /**
     * 获取关联的Pipeline
     */
    ChannelPipeline pipeline();
    
    // ========== 入站事件传递 ==========
    
    ChannelHandlerContext fireChannelRegistered();
    ChannelHandlerContext fireChannelActive();
    ChannelHandlerContext fireChannelRead(Object msg);
    ChannelHandlerContext fireChannelReadComplete();
    ChannelHandlerContext fireExceptionCaught(Throwable cause);
    
    // ========== 出站事件传递 ==========
    
    ChannelFuture bind(java.net.SocketAddress localAddress);
    ChannelFuture connect(java.net.SocketAddress remoteAddress);
    ChannelFuture write(Object msg);
    ChannelHandlerContext flush();
    ChannelFuture writeAndFlush(Object msg);
    ChannelFuture close();
}
```

## 6. ByteBuf 接口

```java
package io.netty.buffer;

/**
 * 高效的字节缓冲区，读写索引分离
 */
public interface ByteBuf extends ReferenceCounted {
    
    /**
     * 返回容量
     */
    int capacity();
    
    /**
     * 设置容量
     */
    ByteBuf capacity(int newCapacity);
    
    /**
     * 返回最大容量
     */
    int maxCapacity();
    
    /**
     * 返回读索引
     */
    int readerIndex();
    
    /**
     * 设置读索引
     */
    ByteBuf readerIndex(int readerIndex);
    
    /**
     * 返回写索引
     */
    int writerIndex();
    
    /**
     * 设置写索引
     */
    ByteBuf writerIndex(int writerIndex);
    
    /**
     * 返回可读字节数
     */
    int readableBytes();
    
    /**
     * 返回可写字节数
     */
    int writableBytes();
    
    /**
     * 是否可读
     */
    boolean isReadable();
    
    /**
     * 是否可写
     */
    boolean isWritable();
    
    // ========== 读操作 ==========
    
    byte readByte();
    short readShort();
    int readInt();
    long readLong();
    ByteBuf readBytes(byte[] dst);
    ByteBuf readBytes(ByteBuf dst, int length);
    
    // ========== 写操作 ==========
    
    ByteBuf writeByte(int value);
    ByteBuf writeShort(int value);
    ByteBuf writeInt(int value);
    ByteBuf writeLong(long value);
    ByteBuf writeBytes(byte[] src);
    ByteBuf writeBytes(ByteBuf src);
    
    // ========== 标记和重置 ==========
    
    ByteBuf markReaderIndex();
    ByteBuf resetReaderIndex();
    ByteBuf markWriterIndex();
    ByteBuf resetWriterIndex();
    
    /**
     * 丢弃已读字节，压缩缓冲区
     */
    ByteBuf discardReadBytes();
    
    /**
     * 清空缓冲区
     */
    ByteBuf clear();
    
    // ========== 引用计数 (继承自ReferenceCounted) ==========
    
    @Override
    ByteBuf retain();
    
    @Override
    boolean release();
}

/**
 * 引用计数接口
 */
public interface ReferenceCounted {
    
    int refCnt();
    ReferenceCounted retain();
    ReferenceCounted retain(int increment);
    boolean release();
    boolean release(int decrement);
}
```

## 7. Bootstrap 接口

```java
package io.netty.bootstrap;

import io.netty.channel.*;

/**
 * 启动器基类
 */
public abstract class AbstractBootstrap<B extends AbstractBootstrap<B, C>, C extends Channel> {
    
    /**
     * 设置EventLoopGroup
     */
    public B group(EventLoopGroup group);
    
    /**
     * 设置Channel类型
     */
    public B channel(Class<? extends C> channelClass);
    
    /**
     * 设置Handler
     */
    public B handler(ChannelHandler handler);
    
    /**
     * 设置Channel选项
     */
    public <T> B option(ChannelOption<T> option, T value);
    
    /**
     * 绑定端口
     */
    public ChannelFuture bind(int port);
    
    /**
     * 绑定地址
     */
    public ChannelFuture bind(java.net.SocketAddress localAddress);
}

/**
 * 服务端启动器
 */
public class ServerBootstrap extends AbstractBootstrap<ServerBootstrap, ServerChannel> {
    
    /**
     * 设置Boss和Worker EventLoopGroup
     */
    public ServerBootstrap group(EventLoopGroup parentGroup, EventLoopGroup childGroup);
    
    /**
     * 设置子Channel的Handler
     */
    public ServerBootstrap childHandler(ChannelHandler childHandler);
    
    /**
     * 设置子Channel的选项
     */
    public <T> ServerBootstrap childOption(ChannelOption<T> option, T value);
}

/**
 * 客户端启动器
 */
public class Bootstrap extends AbstractBootstrap<Bootstrap, Channel> {
    
    /**
     * 连接远程地址
     */
    public ChannelFuture connect(String host, int port);
    
    /**
     * 连接远程地址
     */
    public ChannelFuture connect(java.net.SocketAddress remoteAddress);
}
```

## 8. 编解码器接口

```java
package io.netty.handler.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import java.util.List;

/**
 * 字节到消息解码器抽象类
 */
public abstract class ByteToMessageDecoder extends ChannelInboundHandlerAdapter {
    
    /**
     * 解码方法，子类实现
     * @param ctx 上下文
     * @param in 输入ByteBuf
     * @param out 解码后的消息列表
     */
    protected abstract void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception;
    
    /**
     * 解码完成时调用
     */
    protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.isReadable()) {
            decode(ctx, in, out);
        }
    }
}

/**
 * 消息到字节编码器抽象类
 */
public abstract class MessageToByteEncoder<I> extends ChannelOutboundHandlerAdapter {
    
    /**
     * 编码方法，子类实现
     */
    protected abstract void encode(ChannelHandlerContext ctx, I msg, ByteBuf out) throws Exception;
}
```
