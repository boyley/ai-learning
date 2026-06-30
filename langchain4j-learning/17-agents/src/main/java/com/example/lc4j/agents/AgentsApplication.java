package com.example.lc4j.agents;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 17：Agents 智能体 —— 启动类
 * ============================================================================
 *
 * 【什么是 Agent（智能体）】
 *   普通的“一问一答”只调用一次模型。而 Agent 能为了完成一个目标【自主地多步行动】：
 *   思考 → 调用工具 → 看结果 → 再思考 → 再调用 → ... → 直到得出最终答案。
 *
 *   一句话公式：
 *       Agent = LLM（大脑/决策） + Tools（手脚/能力） + Memory（记忆/上下文） + Loop（循环）
 *
 *   - LLM：负责“想清楚下一步该做什么”（要不要用工具、用哪个、传什么参数）。
 *   - Tools：模型自身不会算数/查数据，把这些能力做成 @Tool 方法供它调用。
 *   - Memory：记住对话历史与中间结果，让多步推理能“接着上一步继续”。
 *   - Loop：模型每调用一次工具，工具结果会回灌给模型，模型据此决定是否继续——
 *           这个“调用-观察-再决策”的循环由 LangChain4j 的 AiServices 自动驱动。
 *
 * 【这个模块怎么做】
 *   不引入额外的实验性 agentic 模块，直接用最稳定的组合实现一个“研究助手”智能体：
 *     - AiServices 自动实现接口（模块 04 的能力）；
 *     - @Tool 提供计算器、知识库查询等工具（模块 08 的能力）；
 *     - MessageWindowChatMemory 提供对话记忆（模块 05 的能力）。
 *   提一个需要“先查资料、再做计算、最后汇总”的复合问题，观察模型自动多步调用工具。
 *
 * 【达到的目的】
 *   理解 Agent 的本质，并能用稳定的 AiServices + 工具 + 记忆 亲手搭出一个可多步推理的智能体。
 * ============================================================================
 */
@SpringBootApplication
public class AgentsApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgentsApplication.class, args);
    }
}
