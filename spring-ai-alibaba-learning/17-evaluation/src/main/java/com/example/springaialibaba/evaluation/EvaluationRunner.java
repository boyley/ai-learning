package com.example.springaialibaba.evaluation;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * 模型评估演示：用 RelevancyEvaluator 给 AI 回答的“相关性”打分
 * ============================================================================
 *
 * 【核心流程图】
 *
 *   ①生成回答                          ②评估回答(LLM-as-a-judge)
 *   ┌───────────────┐                 ┌──────────────────────────────┐
 *   │ ChatClient    │  回答文本 answer │ RelevancyEvaluator(裁判模型)  │
 *   │ .user(问题)   │ ───────────────► │  输入: 问题 + 上下文 + 回答    │
 *   │ .call()       │                 │  判定: 回答是否切题/被上下文支持│
 *   └───────────────┘                 │  输出: isPass() + getScore()  │
 *                                      └──────────────────────────────┘
 *
 * 【关键 API 链路】
 *   new RelevancyEvaluator(chatClientBuilder)      -> 构造评估器（内部用模型当裁判）。
 *   new EvaluationRequest(问题, 上下文文档列表, 回答) -> 组装一次评估输入。
 *   evaluator.evaluate(request)                    -> 返回 EvaluationResponse。
 *   response.isPass() / getScore()                 -> 是否通过 / 分数(0~1)。
 * ============================================================================
 */
@Component
public class EvaluationRunner implements CommandLineRunner {

    /** 用来“生成回答”的对话客户端 */
    private final ChatClient chatClient;

    /** 相关性评估器（裁判）。它内部也需要一个模型，所以构造时传入 ChatClient.Builder。 */
    private final RelevancyEvaluator relevancyEvaluator;

    public EvaluationRunner(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        // ★ 用同一个 Builder 构造评估器：评估时会另起一次“裁判模型”调用 ★
        this.relevancyEvaluator = new RelevancyEvaluator(chatClientBuilder);
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块17：模型评估(Evaluation) ==========\n");

        // ---- 准备：一个问题 + 一段“上下文/知识”（模拟 RAG 检索到的资料）----
        String question = "Spring AI Alibaba 的对话入口 API 是什么？";
        // 上下文用 Document 列表表示（真实场景来自向量库检索，见模块 11-rag）
        List<Document> context = List.of(new Document(
                "Spring AI Alibaba 构建在 Spring AI 之上，最常用的对话入口是 ChatClient，" +
                "它提供 prompt().user().call().content() 的链式调用来与通义千问对话。"));

        // ======================= 第 1 步：生成一个“切题”的回答 =======================
        System.out.println("【第1步】先用 ChatClient 生成一个回答：");
        System.out.println("【我问】" + question);
        String goodAnswer = chatClient.prompt()
                .system("请只依据用户随后给出的资料作答，简洁准确。资料：" + context.get(0).getText())
                .user(question)
                .call()
                .content();
        System.out.println("【AI 答】" + goodAnswer);

        // ======================= 第 2 步：评估这个回答的相关性 =======================
        System.out.println("\n【第2步】用 RelevancyEvaluator 评估“该回答是否与问题+上下文相关”：");
        // 组装评估输入：问题 + 上下文文档 + 待评估回答
        EvaluationRequest goodRequest = new EvaluationRequest(question, context, goodAnswer);
        EvaluationResponse goodResult = relevancyEvaluator.evaluate(goodRequest);
        System.out.println("  是否通过 isPass = " + goodResult.isPass());
        System.out.println("  相关性分数 score = " + goodResult.getScore());

        // ======================= 第 3 步：对比——评估一个“跑题”的回答 =======================
        System.out.println("\n【第3步】对照实验：故意给一个明显跑题的回答，看裁判能否识别为不通过：");
        String badAnswer = "西红柿炒蛋的做法是：先打鸡蛋，热油下锅翻炒，再加西红柿和盐即可。";
        System.out.println("  跑题回答 = " + badAnswer);
        EvaluationRequest badRequest = new EvaluationRequest(question, context, badAnswer);
        EvaluationResponse badResult = relevancyEvaluator.evaluate(badRequest);
        System.out.println("  是否通过 isPass = " + badResult.isPass() + "  （预期为 false）");
        System.out.println("  相关性分数 score = " + badResult.getScore());

        System.out.println("\n【小结】RelevancyEvaluator = 让“裁判模型”判断回答是否切题，可用于 RAG 质量回归测试。");
        System.out.println("        另有 FactCheckingEvaluator 专门查“事实是否被上下文支持(有无幻觉)”，用法类似。");
        System.out.println("\n========== 演示结束：给 AI 回答自动打分 ==========\n");
    }
}
