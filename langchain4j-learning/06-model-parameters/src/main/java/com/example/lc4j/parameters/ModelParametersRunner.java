package com.example.lc4j.parameters;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * Model Parameters 演示：temperature / maxTokens / topP 等参数对输出的影响
 * ============================================================================
 *
 * 【流程图】
 *
 *   同一个问题
 *      │
 *      ├─► 低 temperature(0.0) 的模型 ──► 稳重、确定、可复现的回答
 *      │
 *      ├─► 高 temperature(1.5) 的模型 ──► 发散、有创意、每次不同的回答
 *      │
 *      ├─► maxTokens(很小) ──► 回答被强行截断（演示长度控制）
 *      │
 *      └─► ChatRequest 按请求覆盖 temperature/topP/maxOutputTokens ──► 只影响这一次调用
 *
 *   设参两条途径：
 *     A) builder 上设  → 对该模型实例的所有请求都生效（全局默认）
 *     B) ChatRequest 上设 → 只对这一次请求生效（更灵活）
 * ============================================================================
 */
@Component
public class ModelParametersRunner implements CommandLineRunner {

    @Value("${langchain4j.openai.chat.base-url}")
    private String baseUrl;
    @Value("${langchain4j.openai.chat.api-key}")
    private String apiKey;
    @Value("${langchain4j.openai.chat.model}")
    private String modelName;

    /** 演示用的同一个问题（开放性问题更能看出 temperature 的差异）。 */
    private static final String QUESTION = "请用一句话给一家咖啡店起个有创意的名字。";

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块06：Model Parameters 模型参数 ==========\n");

        demo1_temperatureOnBuilder();
        demo2_maxTokens();
        demo3_perRequestOverride();

        System.out.println("\n========== 模块06 演示结束 ==========\n");
    }

    /** 演示1：在 builder 上设 temperature，对比“低温稳重” vs “高温发散”。 */
    private void demo1_temperatureOnBuilder() {
        System.out.println("===== 演示1：temperature 温度（在 builder 上设，全局生效） =====");

        // 低温模型：temperature=0.0 → 输出更确定、更保守、更可复现。
        ChatModel coldModel = OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(0.0)   // ★ 关键参数：温度调到最低，追求稳定
                .build();

        // 高温模型：temperature=1.5 → 输出更随机、更有创意、每次可能不同。
        ChatModel hotModel = OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(1.5)   // ★ 关键参数：温度调高，追求创意/发散
                .build();

        System.out.println("我问（同一个问题，对比两种温度）：" + QUESTION);  // ★ 先打印问题
        System.out.println("【低温 0.0】" + coldModel.chat(QUESTION));
        System.out.println("【高温 1.5】" + hotModel.chat(QUESTION));
        System.out.println("（提示：高温通常更花哨、更多样；低温更朴实、更稳定）\n");
    }

    /** 演示2：maxTokens 限制输出长度，回答会被截断。 */
    private void demo2_maxTokens() {
        System.out.println("===== 演示2：maxTokens 限制最大输出长度 =====");

        // maxTokens=20 → 模型最多生成约 20 个 Token，长回答会被强行截断。
        ChatModel shortModel = OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .maxTokens(20)      // ★ 关键参数：限制输出 Token 上限，控制长度与成本
                .build();

        String q = "请详细介绍一下 Java 的历史。";
        System.out.println("我问：" + q);                       // ★ 先打印问题
        System.out.println("【maxTokens=20】" + shortModel.chat(q));
        System.out.println("（提示：回答会在约 20 个 Token 处被截断，用于控制长度与花费）\n");
    }

    /** 演示3：用 ChatRequest 按“单次请求”覆盖参数（同一个模型实例，不同请求用不同参数）。 */
    private void demo3_perRequestOverride() {
        System.out.println("===== 演示3：ChatRequest 按单次请求覆盖 temperature/topP/maxOutputTokens =====");

        // 模型 builder 上不设这些参数，改为在每次请求里动态指定，更灵活。
        ChatModel model = OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .build();

        // ChatRequest.builder() 上可直接设 temperature / topP / maxOutputTokens，
        // 这些只对【这一次】请求生效，不影响该模型实例的其它调用。
        ChatRequest request = ChatRequest.builder()
                .messages(UserMessage.from(QUESTION)) // 本次要问的问题
                .temperature(1.2)        // ★ 本次温度：偏创意
                .topP(0.9)               // ★ 核采样：只从累计概率 90% 的候选词里挑（与 temperature 互补）
                .maxOutputTokens(60)     // ★ 本次最多输出 60 Token
                .build();

        System.out.println("我问：" + QUESTION);   // ★ 先打印问题
        // chat(ChatRequest) 返回完整 ChatResponse，可顺带查看 Token 用量。
        ChatResponse response = model.chat(request);
        System.out.println("【单次覆盖 temp=1.2, topP=0.9, maxOut=60】" + response.aiMessage().text());
        if (response.tokenUsage() != null) {
            System.out.println("本次输出 Token：" + response.tokenUsage().outputTokenCount());
        }
    }
}
