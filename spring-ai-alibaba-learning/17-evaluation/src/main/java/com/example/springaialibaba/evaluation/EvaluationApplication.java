package com.example.springaialibaba.evaluation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 17：模型评估(Evaluation) —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   AI 的回答质量怎么“自动打分”？本模块演示用【一个模型当裁判】去评估【另一个模型的回答】。
 *   具体用 Spring AI 的 RelevancyEvaluator 评估“回答与问题(及上下文)是否相关/切题”。
 *
 * 【为什么需要评估（零基础必读）】
 *   大模型会“一本正经地跑题”或“胡编(幻觉)”。上线前后都需要量化质量：
 *     - 相关性(Relevancy)：回答是否切题、是否基于给定上下文。→ RelevancyEvaluator
 *     - 事实性(Fact Checking)：回答的事实是否被上下文支持、有没有幻觉。→ FactCheckingEvaluator
 *   评估思路叫 “LLM-as-a-judge”（用大模型当裁判）：把 [问题 + 上下文 + 待评回答] 交给裁判模型，
 *   让它按预设标准判定通过与否并给分。
 *
 * 【核心 API（都来自 org.springframework.ai.*）】
 *   - RelevancyEvaluator  ：相关性评估器，构造时需要一个 ChatClient.Builder（它内部用模型当裁判）。
 *   - EvaluationRequest   ：一次评估的输入 = 用户问题 + 上下文文档列表 + 待评估的回答文本。
 *   - EvaluationResponse  ：评估结果 = isPass()(是否通过) + getScore()(分数) + getFeedback()(理由)。
 *
 * 【演示步骤】
 *   1. 先用普通 ChatClient 得到一个“回答”；
 *   2. 再用 RelevancyEvaluator 给这个回答打分（相关/不相关）；
 *   3. 为对比，故意构造一个“跑题的回答”，看评估器能否识别为不通过。
 * ============================================================================
 */
@SpringBootApplication
public class EvaluationApplication {

    public static void main(String[] args) {
        // 启动 Spring Boot：自动配置 ChatClient.Builder（既用于生成回答，也用于构造裁判评估器）。
        SpringApplication.run(EvaluationApplication.class, args);
    }
}
