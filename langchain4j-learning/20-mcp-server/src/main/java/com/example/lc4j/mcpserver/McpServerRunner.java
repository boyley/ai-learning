package com.example.lc4j.mcpserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * ============================================================================
 * MCP 服务端演示：用官方 MCP Java SDK 构建一个 stdio 服务端，注册两个 Java 工具
 * ============================================================================
 *
 * 【流程图】（ASCII）
 *
 *   ObjectMapper → JacksonMcpJsonMapper (MCP 的 JSON 编解码器)
 *          │
 *          ▼
 *   StdioServerTransportProvider(jsonMapper)  ← stdio 传输：用 stdin/stdout 收发 JSON-RPC
 *          │  McpServer.sync(transport)
 *          ▼
 *   McpServer.SyncSpecification          ← 声明服务端：名字/能力/工具
 *          │  .serverInfo(...).capabilities(tools=true)
 *          │  .tool(getWeather 定义, 处理器)
 *          │  .tool(add 定义,        处理器)
 *          │  .build()
 *          ▼
 *   McpSyncServer (运行中)               ← 通过 stdin 等客户端请求，stdout 回结果
 *          ▲                                   ▲
 *          │ tools/list                        │ tools/call(name,args)
 *   16-mcp 客户端 / MCP Inspector  ─────────────┘ 把 Java 返回值包成 CallToolResult 回传
 *
 * 【每个工具由两部分组成】
 *   1) McpSchema.Tool        —— 工具的「说明书」：名称 + 描述 + 入参的 JSON Schema。
 *      客户端靠它告诉大模型「有这么个工具、参数长这样」。
 *   2) 调用处理器 BiFunction  —— 真正干活的 Java 逻辑：拿到参数 Map，返回 CallToolResult。
 *
 * 【为什么所有提示都走 System.err】
 *   stdio 传输把 stdout 当作 JSON-RPC 专用通道，往 stdout 打印任何杂讯都会破坏协议，
 *   所以人类可读的日志必须输出到【标准错误流 System.err】。
 * ============================================================================
 */
@Component
public class McpServerRunner implements CommandLineRunner {

    @Override
    public void run(String... args) {
        // 注意：用 err 而不是 out，避免污染 stdio 的 JSON-RPC 通道
        System.err.println("\n========== 模块20：MCP（模型上下文协议）服务端 ==========\n");

        // 1) 准备 MCP 的 JSON 编解码器。
        //    官方 SDK 把 JSON 能力抽象成 McpJsonMapper 接口，这里用基于 Jackson 的实现。
        McpJsonMapper jsonMapper = new JacksonMcpJsonMapper(new ObjectMapper());

        // 2) 构建【stdio 传输提供者】：服务端将通过本进程的 stdin/stdout 收发 JSON-RPC 消息。
        //    （不传 InputStream/OutputStream 时默认用 System.in / System.out。）
        StdioServerTransportProvider transport = new StdioServerTransportProvider(jsonMapper);

        // 3) ===== 定义第 1 个工具：getWeather(city) 查天气 =====
        //    3a) 入参的 JSON Schema（标准 JSON Schema 文本）：一个必填的字符串参数 city。
        String weatherSchema = """
                {
                  "type": "object",
                  "properties": {
                    "city": { "type": "string", "description": "城市名，如 北京" }
                  },
                  "required": ["city"]
                }
                """;
        //    3b) 工具说明书：名称 + 描述 + 入参 schema（用 jsonMapper 把 schema 文本解析进去）。
        McpSchema.Tool getWeatherTool = McpSchema.Tool.builder()
                .name("getWeather")                       // 工具名（客户端/模型按它来调用）
                .description("查询指定城市的当前天气")        // 让模型理解何时该用它
                .inputSchema(jsonMapper, weatherSchema)    // 入参约束
                .build();

        // 4) ===== 定义第 2 个工具：add(a, b) 求和 =====
        String addSchema = """
                {
                  "type": "object",
                  "properties": {
                    "a": { "type": "number", "description": "加数 a" },
                    "b": { "type": "number", "description": "加数 b" }
                  },
                  "required": ["a", "b"]
                }
                """;
        McpSchema.Tool addTool = McpSchema.Tool.builder()
                .name("add")
                .description("计算两个数字之和")
                .inputSchema(jsonMapper, addSchema)
                .build();

        // 5) ★★★ 用 builder 组装并启动服务端 ★★★
        //    McpServer.sync(transport) 得到「同步规格」；链式声明能力与工具后 build()。
        McpSyncServer server = McpServer.sync(transport)
                // 服务端身份信息（名称 + 版本），客户端握手时会看到
                .serverInfo("lc4j-learning-20-mcp-server", "1.0.0")
                // 声明本服务端「具备 tools 能力」（true 表示工具列表可变更时会通知客户端）
                .capabilities(McpSchema.ServerCapabilities.builder()
                        .tools(true)
                        .build())
                // 注册工具1：第二个参数是「调用处理器」——
                //   入参 (exchange 会话上下文, arguments 参数Map) → 返回 CallToolResult。
                .tool(getWeatherTool, (exchange, arguments) -> {
                    // 从参数 Map 取出 city（客户端按上面的 schema 传进来）
                    Object city = arguments.get("city");
                    String text = "【" + city + "】当前天气：晴，26℃，东南风 2 级。（演示数据）";
                    // CallToolResult(文本, isError)：把结果文本包成标准返回；false 表示非错误
                    return new McpSchema.CallToolResult(text, false);
                })
                // 注册工具2：把两个数字相加
                .tool(addTool, (exchange, arguments) -> {
                    // JSON 数字在 Map 里通常是 Number，统一按 double 取值再相加
                    double a = ((Number) arguments.get("a")).doubleValue();
                    double b = ((Number) arguments.get("b")).doubleValue();
                    String text = "结果：" + a + " + " + b + " = " + (a + b);
                    return new McpSchema.CallToolResult(text, false);
                })
                .build();   // build() 完成后，stdio 传输即开始监听 stdin，服务端进入工作状态

        // 6) 至此服务端已就绪。它会通过 stdin/stdout 等待客户端的 JSON-RPC 请求（正常会一直运行）。
        //    这里仅打印已注册的工具数到 stderr 作为「启动成功」的提示。
        System.err.println("MCP 服务端已启动（stdio 传输）。已注册工具数：" + server.listTools().size());
        server.listTools().forEach(t ->
                System.err.println("  · " + t.name() + " —— " + t.description()));
        System.err.println("等待 MCP 客户端通过 stdin/stdout 连接……（运行方式见 README）");
        System.err.println("\n========== 模块20 演示结束 ==========\n");

        // 说明：本项目只要求【编译通过】，不在此处阻塞等待。
        //   真实场景中，stdio 服务端进程会持续存活、由客户端拉起并保持连接（见 README）。
        //   若想让进程常驻以便用 MCP Inspector 连接，可改为：Thread.currentThread().join();
    }
}
