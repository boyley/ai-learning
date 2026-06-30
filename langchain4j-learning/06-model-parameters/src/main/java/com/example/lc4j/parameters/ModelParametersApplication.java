package com.example.lc4j.parameters;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 06：Model Parameters 模型参数 —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   同一个问题、同一个模型，为什么有时回答很“稳重”、有时很“天马行空”？
 *   答案在于【生成参数】。本模块演示几个最常用的参数如何影响模型输出，
 *   让你能根据业务需求（要稳定还是要创意、要长还是要短）去“调参”。
 *
 * 【最常用的几个参数】
 *   - temperature（温度，0~2）：控制随机性。
 *       低（如 0.0~0.3）→ 更确定、更稳定、更可复现，适合事实问答/抽取。
 *       高（如 0.8~1.5）→ 更随机、更有创意、更发散，适合写作/头脑风暴。
 *   - maxTokens（最大输出 Token 数）：限制回答最长能生成多少 Token，控制长度与成本。
 *   - topP（核采样，0~1）：另一种控制随机性的方式，只从累计概率 topP 的候选词里采样。
 *       通常 temperature 和 topP 二选一调，不要同时大改。
 *   - frequencyPenalty / presencePenalty：抑制重复用词 / 鼓励谈新话题。
 *   - seed（随机种子）：固定它可让相同输入尽量得到可复现的输出（便于测试）。
 *
 * 【怎么设置】
 *   两种途径：
 *   1) 在 OpenAiChatModel.builder() 上设：对该模型实例的【所有】请求都生效（全局默认）。
 *   2) 在 ChatRequest.builder() 上设：只对【这一次】请求生效（按请求覆盖）。
 *
 * 【达到的目的】
 *   理解 temperature / maxTokens / topP 的含义，学会在“稳定”和“创意”之间按需取舍。
 * ============================================================================
 */
@SpringBootApplication
public class ModelParametersApplication {
    public static void main(String[] args) {
        SpringApplication.run(ModelParametersApplication.class, args);
    }
}
