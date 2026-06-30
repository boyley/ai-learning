package com.example.lc4j.evaluation;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * ============================================================================
 * 裁判接口（LLM-as-a-Judge）：让大模型按评分标准给回答打分
 * ============================================================================
 *
 * 【核心思想】
 *   它也是一个 AI Service 接口，但角色是“评委”：
 *   输入 = 原始问题 + 被测助手的回答 + 我们写在提示词里的评分标准；
 *   输出 = 结构化结果（boolean 是否合格 / int 1-10 分）。
 *
 * 【为什么方法能直接返回 boolean / int】
 *   AI Services 会根据返回类型自动追加格式约束，并把模型文本解析成对应 Java 类型：
 *     - 返回 boolean -> 框架要求模型只回 true/false；
 *     - 返回 int     -> 框架要求模型只回一个整数。
 *   于是裁判的结论可以直接拿来当“断言”或写进监控指标。
 * ============================================================================
 */
public interface Judge {

    /**
     * 评估维度一：是否合格（二元判断）。
     *
     * @SystemMessage 把模型“摆正位置”——它现在是评委，不是答题者。
     * @UserMessage   写清楚评分标准，并用 {{question}}/{{answer}} 占位注入。
     * @V 把方法参数绑定到对应占位符。
     *
     * @param question 原始问题
     * @param answer   被测助手给出的回答
     * @return true=合格，false=不合格
     */
    @SystemMessage("你是一位严格、客观的回答质量评审，只依据给定标准判断，不带个人偏好。")
    @UserMessage("""
            请判断下面这条回答是否【合格】。
            合格标准：内容准确、直接回应了问题、表达简洁清晰、没有明显事实错误。

            【问题】{{question}}
            【回答】{{answer}}

            如果合格，只回答 true；如果不合格，只回答 false。
            """)
    boolean isAcceptable(@V("question") String question, @V("answer") String answer);

    /**
     * 评估维度二：打 1-10 分（连续评分，便于做趋势/对比）。
     *
     * @param question 原始问题
     * @param answer   被测助手给出的回答
     * @return 1 到 10 的整数分（10 最好）
     */
    @SystemMessage("你是一位严格、客观的回答质量评审，只依据给定标准打分。")
    @UserMessage("""
            请给下面这条回答打分，范围 1 到 10 分（10 分最好）。
            评分维度：准确性、相关性、简洁性、表达清晰度，综合给一个整体分。

            【问题】{{question}}
            【回答】{{answer}}

            只输出一个 1 到 10 之间的整数，不要任何解释或文字。
            """)
    int score(@V("question") String question, @V("answer") String answer);
}
