package com.example.lc4j.getstarted;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * 第一个 LangChain4j 演示：调用大模型并打印回答
 * ============================================================================
 *
 * 【核心流程图】
 *
 *   你的代码                 ChatModel(OpenAiChatModel)        大模型(DeepSeek)
 *     │                            │                               │
 *     │  model.chat("问题")         │                               │
 *     │ ──────────────────────────►│   HTTP 请求(带你的问题)         │
 *     │                            │ ────────────────────────────► │
 *     │                            │                               │ 思考中...
 *     │                            │   HTTP 响应(AI 的回答)          │
 *     │                            │ ◄──────────────────────────── │
 *     │   返回 String 回答          │                               │
 *     │ ◄──────────────────────────│                               │
 *     ▼
 *   打印到控制台
 *
 * 【关键 API 解读】
 *   OpenAiChatModel.builder()         -> 开始构建一个对接 OpenAI 兼容接口的对话模型
 *       .baseUrl("...")               -> 接口地址（DeepSeek 兼容地址，需带 /v1）
 *       .apiKey("...")                -> 你的 API 密钥
 *       .modelName("deepseek-chat")   -> 使用哪个模型
 *       .build()                      -> 得到一个可复用的 ChatModel 实例
 *   model.chat("问题")                -> 最简单的同步调用：传入字符串，返回字符串回答
 * ============================================================================
 */
@Component // 标记为 Spring 组件，容器启动时会自动创建它的实例并执行 run()
public class GetStartedRunner implements CommandLineRunner {

    // ---- 从共享配置文件(config/langchain4j-common.yml)注入对话模型所需的连接参数 ----
    // @Value 会把 yml 里 langchain4j.openai.chat.* 的值注入到下面的字段。

    /** 对话接口地址（DeepSeek 的 OpenAI 兼容地址，带 /v1）。 */
    @Value("${langchain4j.openai.chat.base-url}")
    private String baseUrl;

    /** 对话用的 API Key（DeepSeek 的密钥）。 */
    @Value("${langchain4j.openai.chat.api-key}")
    private String apiKey;

    /** 使用的模型名（如 deepseek-chat）。 */
    @Value("${langchain4j.openai.chat.model}")
    private String modelName;

    /**
     * CommandLineRunner 的 run() 方法会在 Spring 容器启动完成后自动执行。
     * 我们把演示代码写在这里。
     */
    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块01：第一个 LangChain4j 调用 ==========\n");

        // ★ 用 LangChain4j 原生构建器，手动构建一个 ChatModel（对话模型）★
        // 注意：返回类型用接口 ChatModel，而不是具体的 OpenAiChatModel，
        //      这样换厂商（如 Ollama）时业务代码无需改动，体现“面向接口编程”。
        ChatModel model = OpenAiChatModel.builder()
                .baseUrl(baseUrl)        // 接口地址（DeepSeek 兼容地址）
                .apiKey(apiKey)          // API 密钥
                .modelName(modelName)    // 模型名
                .build();

        // 我们要问大模型的问题
        String question = "请用一句话向 Java 初学者解释什么是 LangChain4j？";
        System.out.println("【我问】" + question);

        // ★★★ 核心：一行调用完成“提问 -> 调用 -> 取回答” ★★★
        // chat(String) 是 LangChain4j 最简洁的同步调用：入参问题字符串，返回回答字符串。
        String answer = model.chat(question);

        System.out.println("\n【AI 答】" + answer);
        System.out.println("\n========== 演示结束：恭喜，你已成功调用大模型！ ==========\n");
    }
}
