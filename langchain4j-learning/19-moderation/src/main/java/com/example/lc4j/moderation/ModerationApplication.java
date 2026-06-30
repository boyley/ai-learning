package com.example.lc4j.moderation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 19：内容审核（Moderation）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   用 AI「审核模型」自动检查一段文字是否包含违规/有害内容
 *   （暴力、仇恨、骚扰、色情、自残等），并告诉你：
 *     - 是否被标记为违规（flagged）
 *     - 命中的是哪段文字（flaggedText）
 *
 * 【为什么要有它（典型用途）】
 *   - UGC 过滤：用户评论/弹幕/私信发布前先审一遍，违规的拦下来。
 *   - 合规风控：把用户的话喂给大模型之前先审，防止有人用越界内容诱导 AI。
 *   - 出站兜底：连大模型自己生成的回答也可以再审一遍，双保险。
 *
 * 【怎么做】
 *   审核模型是【独立于对话模型】的专用模型。本模块用 LangChain4j 的
 *   OpenAiModerationModel，对“正常文本”和“违规文本”分别调用 moderate()，
 *   对比它们的审核结果。
 *
 * 【⚠️ 重要：需要 OpenAI】
 *   内容审核只有 OpenAI 支持，DeepSeek 不支持。所以本模块连接参数指向真正的
 *   OpenAI（共享配置里的 moderation.* 一组属性），运行需要有效的 OPENAI_API_KEY。
 *   若账户无额度会返回 HTTP 429（insufficient_quota）——这是账户问题，不是代码问题。
 *
 * 【达到的目的】
 *   理解“内容审核”这一独立能力，知道它返回什么、怎么用它做内容安全把关。
 * ============================================================================
 */
@SpringBootApplication
public class ModerationApplication {
    public static void main(String[] args) {
        SpringApplication.run(ModerationApplication.class, args);
    }
}
