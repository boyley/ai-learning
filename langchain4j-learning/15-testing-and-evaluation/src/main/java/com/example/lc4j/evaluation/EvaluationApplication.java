package com.example.lc4j.evaluation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 15：测试与评估（Testing & Evaluation）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   传统软件用断言（assertEquals）测试：输入固定，输出也固定，对就是对。
 *   但大模型的输出是【自然语言、且每次都可能不同】，没有唯一正确答案，
 *   于是经典的 assertEquals 用不上了。怎么办？
 *
 *   业界最常用的自动评估手段就是 ——「LLM 当裁判」（LLM-as-a-Judge）：
 *   再请【另一个大模型】来当评委，按照我们给定的【评分标准】去打分，
 *   返回“是否通过（boolean）”或“1-10 分（int）”。
 *
 * 【本模块怎么演示】
 *   1) 先有一个被测助手 Assistant（普通 AI Service），让它回答一个问题。
 *   2) 再定义一个裁判 Judge（也是 AI Service 接口）：
 *      - 用 @SystemMessage 设定“你是严格的评审”，
 *      - 用 @UserMessage 写清楚评分标准 + 填入问题与回答，
 *      - 方法返回 boolean（是否合格）或 int（1-10 分）。
 *   3) 把助手的回答喂给裁判，打印评估结果。
 *
 * 【为什么返回类型能是 boolean / int】
 *   这正是 AI Services 的“结构化输出”能力（见模块07）：
 *   框架会自动在提示词里要求模型只输出布尔/数字，并把模型文本解析成 Java 类型。
 *
 * 【达到的目的】
 *   理解为何 LLM 应用不能用传统断言测试，掌握“LLM 当裁判”这一自动评估套路，
 *   它是回归测试、A/B 选模型、线上质量打分的常用基础设施。
 *
 * 【注意】
 *   真正打分需要联网调用模型（需有效 Key）。本模块只要求 mvn compile 通过。
 * ============================================================================
 */
@SpringBootApplication
public class EvaluationApplication {
    public static void main(String[] args) {
        SpringApplication.run(EvaluationApplication.class, args);
    }
}
