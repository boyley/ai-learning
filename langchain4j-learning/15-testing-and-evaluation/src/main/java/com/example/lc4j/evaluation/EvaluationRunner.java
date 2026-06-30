package com.example.lc4j.evaluation;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * 测试与评估演示：LLM 当裁判（LLM-as-a-Judge）
 * ============================================================================
 *
 * 【流程图】
 *
 *   问题 question
 *      │
 *      ▼
 *   被测助手 Assistant.answer(question) ──► 回答 answer
 *      │                                       │
 *      └──────────────┬────────────────────────┘
 *                     ▼
 *           裁判 Judge（另一个 LLM，按评分标准评估）
 *             ├─ isAcceptable(question, answer) ──► boolean 是否合格
 *             └─ score(question, answer)        ──► int 1-10 分
 *                     │
 *                     ▼
 *           打印评估结果（可当断言 / 写进监控指标）
 *
 * 【为什么要这样测】
 *   LLM 输出是自然语言、不唯一，传统 assertEquals 失效；
 *   于是用“另一个 LLM + 明确评分标准”来自动判分，实现可重复的质量评估。
 * ============================================================================
 */
@Component
public class EvaluationRunner implements CommandLineRunner {

    @Value("${langchain4j.openai.chat.base-url}")
    private String baseUrl;
    @Value("${langchain4j.openai.chat.api-key}")
    private String apiKey;
    @Value("${langchain4j.openai.chat.model}")
    private String modelName;

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块15：测试与评估（LLM 当裁判） ==========\n");

        // 构建底层模型（助手与裁判可以共用一个模型，也可分别用不同模型）
        ChatModel model = OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .build();

        // 1) 生成“被测助手”和“裁判”两个 AI Service 实现
        Assistant assistant = AiServices.create(Assistant.class, model);  // 答题者
        Judge judge = AiServices.create(Judge.class, model);              // 评委

        // 2) 让助手先回答一个问题
        String question = "什么是哈希表（HashMap）？它的查找时间复杂度大致是多少？";
        System.out.println("【问题】" + question);
        String answer = assistant.answer(question);  // 调用被测助手得到回答
        System.out.println("【助手回答】" + answer + "\n");

        // 3) ★ 让裁判按评分标准评估这条回答 ★
        System.out.println("===== 裁判评估 =====");
        // isAcceptable 返回 boolean：框架要求模型只回 true/false，并解析成布尔值
        boolean acceptable = judge.isAcceptable(question, answer);
        System.out.println("是否合格(boolean)：" + acceptable);
        // score 返回 int：框架要求模型只回一个整数，并解析成 int
        int score = judge.score(question, answer);
        System.out.println("综合评分(1-10)：" + score);

        // 4) 把评估结论当“软断言”使用：达到阈值才算通过
        int threshold = 7;
        System.out.println("\n===== 评估结论 =====");
        System.out.println(acceptable && score >= threshold
                ? "✔ 通过：回答合格且评分 >= " + threshold
                : "✘ 未通过：需要改进（合格=" + acceptable + ", 评分=" + score + "）");

        System.out.println("\n========== 模块15 演示结束 ==========\n");
    }
}
