package com.example.springaialibaba.reactagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 22：ReAct 智能体 (Reasoning + Acting) —— 启动类
 * ============================================================================
 *
 * 【什么是 ReAct？（零基础理解）】
 *   ReAct = Reasoning(推理) + Acting(行动) 的循环，是最经典的智能体范式：
 *     1. Reason 推理：AI 先想“要回答这个问题，我需要做什么？缺什么信息？”
 *     2. Act    行动：如果缺信息，就【调用一个工具】（查天气、算数、搜索……）。
 *     3. Observe 观察：拿到工具返回的结果。
 *     4. 回到第 1 步继续推理，直到信息够了，给出最终答案。
 *   一句话：**AI 不再只会“张口就答”，而是会“自己动手查/算，再回答”。**
 *
 * 【本模块如何演示（为稳妥优先，用最成熟的写法）】
 *   我们给 ChatClient 挂上两个用 @Tool 标注的工具（计算器、天气查询），
 *   然后问一个“必须用工具才能答对”的问题。Spring AI 会自动完成 ReAct 循环：
 *     模型判断需要算/查 → 自动调用我们的 @Tool 方法 → 把结果喂回模型 → 模型给出最终答案。
 *   这套“工具自动多轮调用”本质上就是 ReAct，且编译运行最稳。
 *
 * 【Spring AI Alibaba 的高级封装：ReactAgent】
 *   框架还提供了开箱即用的 com.alibaba.cloud.ai.graph.agent.ReactAgent，
 *   用 builder 一行就能造出一个 ReAct 智能体（底层就是 Graph 编排的推理-行动循环）。
 *   其用法见 README，本模块示例代码用更稳的 ChatClient+@Tool 呈现同样的思想。
 * ============================================================================
 */
@SpringBootApplication
public class ReactAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReactAgentApplication.class, args);
    }
}
