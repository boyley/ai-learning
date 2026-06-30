package com.example.lc4j.mcpserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 20：MCP（Model Context Protocol，模型上下文协议）服务端 —— 启动类
 * ============================================================================
 *
 * 【MCP 服务端是什么】
 *   在模块 16 里我们写的是 MCP【客户端】：它连别人家的 Server，把对方暴露的工具交给大模型用。
 *   本模块反过来——我们自己用 Java 写一个 MCP【服务端】，把若干「工具(tools)」对外发布出去，
 *   让任何 MCP 客户端（16 模块、Claude Desktop、各种 AI IDE、MCP Inspector）都能连上来调用。
 *   一句话：客户端是「用工具的人」，服务端是「造工具、摆摊的人」。
 *
 * 【stdio 传输是什么】
 *   MCP 底层是 JSON-RPC 2.0 消息。消息怎么在两个进程间传递，由「传输(transport)」决定，常见两种：
 *     - HTTP/SSE 传输：服务端是个常驻的网络服务，客户端通过 URL 连接（适合远程/多客户端）。
 *     - stdio 传输（本模块）：服务端是一个普通命令行程序，
 *       客户端把它当【子进程】拉起，然后通过这个子进程的
 *         · 标准输入  stdin  ← 客户端把请求(JSON-RPC)写进来
 *         · 标准输出  stdout → 服务端把响应(JSON-RPC)写出去
 *       来收发消息。就像两个程序用一根「管道」对接。
 *   ★ 因此有一条铁律：stdio 服务端【绝不能往 stdout 打印普通日志】，否则会把 JSON-RPC 流弄乱。
 *      所有人类可读的提示都要走 stderr（本模块演示如此）。
 *
 * 【它和 16 客户端如何配对（闭环）】
 *       16-mcp 客户端  ──启动子进程&写 stdin──►  20-mcp-server（本模块）
 *                      ◄──读 stdout 返回结果──   注册的 Java 工具（getWeather / add）
 *   客户端发 "tools/list" 发现工具 → 大模型决定调用 → 客户端发 "tools/call" →
 *   本服务端执行对应 Java 方法 → 把结果回传 → 大模型据此作答。这就是「服务端 + 客户端」完整闭环。
 *
 * 【这个模块怎么做】
 *   1. 引入【官方 MCP Java SDK】(io.modelcontextprotocol.sdk:mcp)，它提供服务端 API
 *      （LangChain4j 自身的 langchain4j-mcp 只做客户端，不做服务端）。
 *   2. 在 Runner 里用 McpServer.sync(stdio传输) 构建一个同步服务端，注册两个工具。
 *   3. 启动后服务端通过 stdin/stdout 等待客户端连接（正常会阻塞，这是 stdio 服务端的正确姿势）。
 *
 * 【运行说明】本项目只要求【编译通过】。真正运行见 README：通常由客户端把它作为子进程拉起，
 *   或手动 `java -jar` 后用 MCP Inspector 连接调试。
 * ============================================================================
 */
@SpringBootApplication
public class McpServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }
}
