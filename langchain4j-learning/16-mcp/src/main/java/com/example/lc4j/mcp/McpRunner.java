package com.example.lc4j.mcp;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * ============================================================================
 * MCP 客户端演示：连接远程 MCP Server，把它的工具交给大模型自动调用
 * ============================================================================
 *
 * 【流程图】
 *
 *   StreamableHttpMcpTransport (HTTP 传输通道, 指向 localhost:8090)
 *          │  DefaultMcpClient.builder().transport(t).build()
 *          ▼
 *   DefaultMcpClient (MCP 客户端: 握手 / 列工具 / 调工具)
 *          │  McpToolProvider.builder().mcpClients(client).build()
 *          ▼
 *   McpToolProvider (把远程工具适配成 LangChain4j 的 ToolProvider)
 *          │  AiServices.builder(接口).chatModel(m).toolProvider(provider).build()
 *          ▼
 *   McpAssistant (AI Service 代理对象)
 *          │  assistant.chat("帮我算一下 ...")
 *          ▼
 *   模型判断需要工具 ──► MCP 客户端转发给 Server 执行 ──► 结果回灌模型 ──► 最终回答
 * ============================================================================
 */
@Component
public class McpRunner implements CommandLineRunner {

    @Value("${langchain4j.openai.chat.base-url}")
    private String baseUrl;
    @Value("${langchain4j.openai.chat.api-key}")
    private String apiKey;
    @Value("${langchain4j.openai.chat.model}")
    private String modelName;

    /** 演示用的 MCP Server 地址。Streamable HTTP 传输通常以 /mcp 作为端点。 */
    private static final String MCP_SERVER_URL = "http://localhost:8090/mcp";

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块16：MCP（模型上下文协议）客户端 ==========\n");

        // 0) 构建底层对话模型（和前面模块一样，指向 DeepSeek）
        ChatModel model = OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .build();

        // 用 try/catch 包裹整个连接流程：未启动 MCP Server 时给出友好提示，不让程序崩溃。
        try {
            // 1) 建立到 MCP Server 的【传输通道】。
            //    StreamableHttpMcpTransport 是基于 HTTP(SSE) 的可流式传输，适合远程 Server。
            //    （另有 HttpMcpTransport、StdioMcpTransport(本地子进程)、WebSocketMcpTransport 可选。）
            McpTransport transport = StreamableHttpMcpTransport.builder()
                    .url(MCP_SERVER_URL)            // MCP Server 的地址
                    .timeout(Duration.ofSeconds(20)) // 单次请求超时
                    .logRequests(true)               // 打印发往 Server 的请求，便于学习/排错
                    .logResponses(true)              // 打印 Server 返回的响应
                    .build();

            // 2) 用传输通道构建【MCP 客户端】。
            //    build() 时会与 Server 完成初始化握手（client/server 能力协商）。
            McpClient mcpClient = new DefaultMcpClient.Builder()
                    .transport(transport)
                    .clientName("lc4j-learning-16-mcp") // 客户端名称（Server 端日志可见）
                    .clientVersion("1.0.0")
                    .build();

            // 3) 把“客户端能列出的远程工具”适配成 LangChain4j 的 ToolProvider。
            //    可挂多个 mcpClients（连多个 Server），这里只连一个。
            ToolProvider toolProvider = McpToolProvider.builder()
                    .mcpClients(mcpClient)
                    .build();

            // 4) ★★★ 核心：用 toolProvider 把远程工具交给模型 ★★★
            //    注意是 .toolProvider(...)（运行时动态发现工具），区别于模块08的 .tools(本地对象)。
            McpAssistant assistant = AiServices.builder(McpAssistant.class)
                    .chatModel(model)
                    .toolProvider(toolProvider)
                    .build();

            // 5) 像普通方法一样提问；模型会自动决定是否调用远程 MCP 工具。
            System.out.println("===== 向带有远程 MCP 工具的助手提问 =====");
            String question = "请使用你可用的工具完成这个任务，并告诉我结果。";
            System.out.println("我问：" + question);     // ★ 先打印问题
            String answer = assistant.chat(question);
            System.out.println("AI ：" + answer);

            // 6) 用完关闭客户端，释放连接资源。
            mcpClient.close();

        } catch (Exception e) {
            // 最典型的失败原因：localhost:8090 上没有运行 MCP Server（连接被拒绝）。
            System.out.println("！未能连接到 MCP Server（" + MCP_SERVER_URL + "）。");
            System.out.println("  这是正常现象：本模块以【编译通过】为目标，实际运行需先启动一个 MCP Server。");
            System.out.println("  如何启动 MCP Server 请见本模块 README.md。");
            System.out.println("  原始错误：" + e.getClass().getSimpleName() + " - " + e.getMessage());
        }

        System.out.println("\n========== 模块16 演示结束 ==========\n");
    }
}
