package com.example.lc4j.observability;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ============================================================================
 * 可观测性演示：日志开关 + ChatModelListener 监听器
 * ============================================================================
 *
 * 【流程图】
 *
 *   构建 OpenAiChatModel
 *     ├─ logRequests(true)  ──► 框架把【请求体】写入日志(DEBUG)
 *     ├─ logResponses(true) ──► 框架把【响应体】写入日志(DEBUG)
 *     └─ listeners(List.of(MetricsChatModelListener)) 注册监听器
 *                 │
 *   model.chat("...") 调用模型
 *                 │
 *      ┌──────────┼─────────────────────────────┐
 *      ▼          ▼                              ▼
 *   onRequest   onResponse                     onError
 *  (记开始时间) (算耗时 + 打印 Token)         (打印异常)
 *                 │
 *                 ▼
 *   你在控制台同时看到：① 框架日志  ② 监听器统计指标
 *
 * 【两部分对应】
 *   - 演示1：只开日志开关，观察框架自动打印的请求/响应日志。
 *   - 演示2：注册监听器，观察 onRequest/onResponse 打印的耗时与 Token。
 * ============================================================================
 */
@Component
public class ObservabilityRunner implements CommandLineRunner {

    @Value("${langchain4j.openai.chat.base-url}")
    private String baseUrl;
    @Value("${langchain4j.openai.chat.api-key}")
    private String apiKey;
    @Value("${langchain4j.openai.chat.model}")
    private String modelName;

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块14：可观测性与日志 ==========\n");

        demo1_logging();
        demo2_listener();

        System.out.println("\n========== 模块14 演示结束 ==========\n");
    }

    /**
     * 演示1：开启内置请求/响应日志。
     * logRequests(true)/logResponses(true) 后，框架会用 SLF4J 在 DEBUG 级别
     * 打印完整的 HTTP 请求体与响应体，便于排查“提示词拼成了什么、模型原样回了什么”。
     */
    private void demo1_logging() {
        System.out.println("===== 演示1：logRequests / logResponses 日志开关 =====");

        ChatModel model = OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .logRequests(true)   // ★ 打印每次请求体到日志（DEBUG）
                .logResponses(true)  // ★ 打印每次响应体到日志（DEBUG）
                .build();

        // 真正联网调用后，控制台/日志里会出现框架自动记录的请求与响应内容
        String answer = model.chat("用一句话解释什么是“可观测性”？");
        System.out.println("AI：" + answer + "\n");
    }

    /**
     * 演示2：注册 ChatModelListener 监听器，统计耗时与 Token。
     * 监听器拿到的是结构化对象，适合做埋点/监控（比纯文本日志更易统计告警）。
     */
    private void demo2_listener() {
        System.out.println("===== 演示2：ChatModelListener 监听耗时与 Token =====");

        // 1) 创建我们自定义的监听器实例
        MetricsChatModelListener listener = new MetricsChatModelListener();

        // 2) 通过 builder.listeners(List.of(...)) 把监听器注册到模型上
        //    可注册多个监听器，框架会按顺序回调它们
        ChatModel model = OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .listeners(List.of(listener))  // ★ 注册监听器
                .build();

        // 3) 普通调用即可——调用前后监听器的 onRequest/onResponse 会被自动触发
        String answer = model.chat("请用两句话介绍一下 LangChain4j。");
        System.out.println("AI：" + answer);
    }
}
