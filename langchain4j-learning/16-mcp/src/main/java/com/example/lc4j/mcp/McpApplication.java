package com.example.lc4j.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 16：MCP（Model Context Protocol，模型上下文协议）客户端 —— 启动类
 * ============================================================================
 *
 * 【MCP 是什么】
 *   MCP 是 Anthropic 在 2024 年提出的一套【开放协议】，用来标准化“大模型 <-> 外部工具/数据源”
 *   之间的通信。你可以把它理解为“给 AI 用的 USB 接口”：
 *     - 任何人都可以写一个 MCP Server，把自己的能力（查数据库、读文件、调用 API、操作浏览器…）
 *       按 MCP 规范暴露成一组“工具(tools)”。
 *     - 任何支持 MCP 的客户端（如本模块、Claude Desktop、各种 AI IDE）都能即插即用地连上它，
 *       自动发现这些工具并交给大模型调用。
 *   于是“工具”不再写死在你的 Java 代码里（对比模块 08-tools 的 @Tool），而是来自一个
 *   独立运行、可热插拔、可被多个应用复用的远程服务。
 *
 * 【这个模块怎么做】
 *   1. 用 StreamableHttpMcpTransport 建立一条到 MCP Server 的 HTTP 传输通道（指向本地 8090 端口）。
 *   2. 用 DefaultMcpClient 包装这条通道，得到一个 MCP 客户端（负责握手、列工具、调工具）。
 *   3. 用 McpToolProvider 把“客户端列出的远程工具”适配成 LangChain4j 的 ToolProvider。
 *   4. 用 AiServices.builder(...).toolProvider(mcpToolProvider) 把这些远程工具交给模型——
 *      此后模型在回答问题时，可以像调用本地 @Tool 一样自动调用远程 MCP 工具。
 *
 * 【运行前提】★重要★
 *   本模块需要先有一个运行在 http://localhost:8090 的 MCP Server，否则连接会失败。
 *   本项目只要求【编译通过】，演示代码用 try/catch 包裹，未启动 Server 时会友好提示，
 *   不会让程序崩溃。如何起一个 MCP Server 见 README。
 *
 * 【达到的目的】
 *   理解 MCP 的价值（工具与应用解耦、生态复用），并掌握 LangChain4j 作为 MCP 客户端的标准接法。
 * ============================================================================
 */
@SpringBootApplication
public class McpApplication {
    public static void main(String[] args) {
        SpringApplication.run(McpApplication.class, args);
    }
}
