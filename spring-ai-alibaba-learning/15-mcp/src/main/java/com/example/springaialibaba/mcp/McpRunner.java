package com.example.springaialibaba.mcp;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * MCP 客户端演示：把 MCP Server 的工具注入 ChatClient，让 AI 自动调用
 * ============================================================================
 *
 * 【核心流程图】
 *
 *   你的应用(MCP Client)                                MCP Server（例如 filesystem）
 *        │                                                     │
 *        │  ① Spring Boot 按 spring.ai.mcp.client.stdio 配置    │
 *        │     用 npx 拉起 Server 子进程，握手/列出可用工具       │
 *        │ ──────────────────────────────────────────────────► │
 *        │  ② Server 返回它支持的工具清单(read_file/list_dir…)   │
 *        │ ◄────────────────────────────────────────────────── │
 *        │                                                     │
 *   ChatClient  ──"用户问题" + 工具清单──►  通义千问(DashScope)  │
 *        │                                     │               │
 *        │            ③ 模型决定“我要调 read_file”              │
 *        │ ◄───────────────────────────────────┘               │
 *        │  ④ 客户端按 MCP 协议把调用转发给 Server               │
 *        │ ──────────────────────────────────────────────────► │
 *        │  ⑤ Server 执行(真的去读文件) 并返回结果               │
 *        │ ◄────────────────────────────────────────────────── │
 *        │  ⑥ 把结果回填给模型 → 模型生成最终自然语言回答         │
 *
 * 【关键 API 链路】
 *   ToolCallbackProvider          -> MCP Starter 自动配置出来的“工具提供者”，
 *                                    内部封装了从各 MCP Server 发现到的所有工具。
 *   provider.getToolCallbacks()   -> 取出这些工具（ToolCallback 数组）。
 *   chatClient.prompt()
 *             .toolCallbacks(provider)   -> 把 MCP 工具挂到本次对话，模型可按需调用。
 *
 * 【为什么用 ObjectProvider 注入】
 *   默认情况下你可能一个 MCP Server 都没配，此时容器里不一定有可用的工具提供者。
 *   用 ObjectProvider<ToolCallbackProvider> “可选注入”，取不到时返回 null，
 *   我们判空后优雅跳过，保证没有 npx 环境也能编译、运行通过。
 * ============================================================================
 */
@Component
public class McpRunner implements CommandLineRunner {

    /** 与大模型对话的高级客户端（由 DashScope Starter 自动配置的 Builder 构建） */
    private final ChatClient chatClient;

    /**
     * “MCP 工具提供者”的可选注入。
     * - 若配置了 MCP Server：容器里会有 SyncMcpToolCallbackProvider（类型是 ToolCallbackProvider）；
     * - 若没配置：可能取不到，getIfAvailable() 会返回 null。
     */
    private final ObjectProvider<ToolCallbackProvider> mcpToolProvider;

    public McpRunner(ChatClient.Builder chatClientBuilder,
                     ObjectProvider<ToolCallbackProvider> mcpToolProvider) {
        this.chatClient = chatClientBuilder.build();
        this.mcpToolProvider = mcpToolProvider;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块15：MCP（模型上下文协议）客户端 ==========\n");

        // ---- 尝试取出 MCP 工具提供者（可能为 null）----
        ToolCallbackProvider provider = mcpToolProvider.getIfAvailable();

        // getToolCallbacks() 返回从所有已连接 MCP Server 发现的工具数组
        int toolCount = (provider == null) ? 0 : provider.getToolCallbacks().length;

        if (toolCount == 0) {
            // ---------- 情况 A：没有可用的 MCP Server ----------
            System.out.println("【提示】当前未检测到已配置的 MCP Server（工具数量=0），跳过“工具增强”演示。");
            System.out.println("        这是默认情况，属正常现象——本机没有连接任何 MCP Server。");
            System.out.println("        如何真正接入一个 MCP Server（例如官方 filesystem 文件工具）？");
            System.out.println("          1) 本机需能运行 npx（安装 Node.js）；");
            System.out.println("          2) 打开 application.yml，解开被注释的 spring.ai.mcp.client.stdio 配置；");
            System.out.println("          3) 重新运行，AI 就能通过 MCP 去读写你指定目录下的文件了。");
            System.out.println("        （详细步骤见本模块 README。）\n");

            // 即便没有 MCP 工具，也做一次普通对话，让你看到 AI 对“MCP 是什么”的解释
            System.out.println("【降级演示】先做一次普通对话，请 AI 解释 MCP：");
            String answer = chatClient.prompt()
                    .user("请用两句话通俗解释什么是 MCP（模型上下文协议），以及它解决了什么问题。")
                    .call()
                    .content();
            System.out.println("【AI 答】" + answer);

        } else {
            // ---------- 情况 B：检测到 MCP Server，演示“工具增强对话” ----------
            System.out.println("【已连接 MCP Server】发现可用工具 " + toolCount + " 个，下面让 AI 借助这些工具回答问题。\n");

            String question = "请列出当前允许访问目录下的文件，并用中文简要总结你看到了什么。";
            System.out.println("【我问】" + question);

            // ★★★ 核心：用 .toolCallbacks(provider) 把 MCP 工具挂到本次对话 ★★★
            // 模型会在需要时自动决定调用哪个 MCP 工具，客户端按协议转发给 Server 执行。
            String answer = chatClient.prompt()
                    .user(question)
                    .toolCallbacks(provider)   // 注入 MCP 工具提供者
                    .call()
                    .content();

            System.out.println("\n【AI 答（已借助 MCP 工具）】" + answer);
        }

        System.out.println("\n========== 演示结束：MCP = 给 AI 接“外部工具”的统一插座 ==========\n");
    }
}
