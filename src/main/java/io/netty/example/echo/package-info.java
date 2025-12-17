/**
 * Echo 示例应用包
 *
 * <p>这是 Mini-Netty 的经典示例，演示了最基本的客户端-服务端通信模式。
 *
 * <h2>包含类</h2>
 * <ul>
 *   <li>{@link io.netty.example.echo.EchoServerHandler} - 服务端处理器，回显收到的消息</li>
 *   <li>{@link io.netty.example.echo.EchoClientHandler} - 客户端处理器，发送并接收消息</li>
 * </ul>
 *
 * <h2>Echo 模式说明</h2>
 * <p>Echo 是网络编程中最基础的模式：
 * <pre>
 * Client                    Server
 *   │                          │
 *   │──── "Hello" ────────────►│
 *   │                          │
 *   │◄───── "Hello" ──────────│
 *   │                          │
 * </pre>
 *
 * <h2>学习建议</h2>
 * <ol>
 *   <li>先理解服务端如何接收和回显消息</li>
 *   <li>再理解客户端如何发送和接收消息</li>
 *   <li>最后学习如何组合使用 Bootstrap 启动服务</li>
 * </ol>
 *
 * @see io.netty.channel.ChannelInboundHandlerAdapter
 * @see io.netty.bootstrap.ServerBootstrap
 */
package io.netty.example.echo;
