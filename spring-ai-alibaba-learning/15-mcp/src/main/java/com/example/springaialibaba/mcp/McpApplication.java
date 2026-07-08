package com.example.springaialibaba.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 15：MCP（模型上下文协议）客户端 —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   让你理解 MCP 是什么，并演示“把 MCP Server 提供的外部工具，接入 ChatClient”的写法。
 *
 * 【什么是 MCP（零基础必读）】
 *   MCP = Model Context Protocol（模型上下文协议），是一套【开放标准】。
 *   痛点：以前每接一个外部能力（读文件、查数据库、调 GitHub…）都要为某个模型单独写适配，
 *         换个模型/换个框架又得重写，像“每个电器配一种插头”。
 *   MCP 的思路：定义一个“统一插座”。
 *     - 工具提供方把能力封装成一个【MCP Server】（比如官方的 filesystem server 能读写文件）；
 *     - AI 应用作为【MCP Client】按协议连上去，就能自动发现并调用这些工具；
 *     - 换模型、换应用都不用改 Server，一次封装、处处可用。
 *
 * 【MCP 的两种传输方式】
 *   1. stdio：把 MCP Server 当作一个本地子进程启动（例如用 npx 拉起官方 server），
 *             通过“标准输入/输出”通信。适合本地工具（文件系统、命令行等）。本模块演示这种。
 *   2. SSE / streamable-http：连接一个远程 HTTP 服务。适合线上共享的工具服务。
 *
 * 【本模块的运行策略（重要）】
 *   为了在“没有安装 npx / 没有配置任何 MCP Server”时也能正常跑通、编译通过，
 *   Runner 里对“MCP 工具提供者”做了判空兜底：
 *     - 若检测到已配置的 MCP Server，就把它的工具注入 ChatClient 让 AI 使用；
 *     - 若没有配置（默认情况），就打印提示并跳过，只做一次普通对话。
 *   真实接入方法见 application.yml 里被注释的示例，以及 README。
 * ============================================================================
 */
@SpringBootApplication
public class McpApplication {

    public static void main(String[] args) {
        // 启动 Spring Boot：自动配置 ChatClient.Builder，
        // 若 application.yml 里声明了 MCP Server，还会自动连接并生成工具回调(ToolCallback)。
        SpringApplication.run(McpApplication.class, args);
    }
}
