package com.example.lc4j.agents;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * 智能体演示：模型 + 工具 + 记忆 + 循环 = Agent
 * ============================================================================
 *
 * 【流程图】（一次 research(task) 调用内部发生的“多步循环”）
 *
 *   你: research("查深圳和广州人口，并算出两市人口总和")
 *          │
 *          ▼
 *   ┌─────────────────────────────────────────────────────────────┐
 *   │  AiServices 自动驱动的 Agent 循环（由 LangChain4j 负责）       │
 *   │                                                               │
 *   │   LLM 思考 ──► 决定调用 knowledgeLookup("深圳")               │
 *   │      ▲                         │                              │
 *   │      │         工具结果回灌      ▼                              │
 *   │   LLM 思考 ◄── "深圳约1779万" ── 执行工具                      │
 *   │      │                                                        │
 *   │      ├─► 再调用 knowledgeLookup("广州") ─► "广州约1883万"      │
 *   │      │                                                        │
 *   │      ├─► 再调用 add(1779, 1883) ─────────► 3662              │
 *   │      │                                                        │
 *   │   LLM 判断信息已足够 ──► 不再调用工具 ──► 生成最终中文结论       │
 *   └─────────────────────────────────────────────────────────────┘
 *          │
 *          ▼
 *   返回最终答案；全过程的对话与中间结果都存进 ChatMemory，可支撑后续追问。
 * ============================================================================
 */
@Component
public class AgentRunner implements CommandLineRunner {

    @Value("${langchain4j.openai.chat.base-url}")
    private String baseUrl;
    @Value("${langchain4j.openai.chat.api-key}")
    private String apiKey;
    @Value("${langchain4j.openai.chat.model}")
    private String modelName;

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块17：Agents 智能体 ==========\n");

        // 1) 大脑：底层对话模型（负责“想清楚下一步做什么”）
        ChatModel model = OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .build();

        // 2) 手脚：工具箱实例（@Tool 方法将作为可调用能力交给模型）
        ResearchTools tools = new ResearchTools();

        // 3) 记忆：保留最近 20 条消息（含工具调用与工具结果），支撑多步推理与连续追问
        MessageWindowChatMemory memory = MessageWindowChatMemory.withMaxMessages(20);

        // 4) ★★★ 组装智能体：把 模型 + 工具 + 记忆 组合进同一个 AiService ★★★
        //    .tools(tools) 让模型可以自主调用；调用-观察-再决策的“循环”由 AiServices 自动驱动。
        ResearchAgent agent = AiServices.builder(ResearchAgent.class)
                .chatModel(model)        // 大脑
                .tools(tools)            // 手脚（本地 @Tool 工具）
                .chatMemory(memory)      // 记忆
                .build();

        // 5) 给一个需要“多步”才能完成的复合任务：先查两市资料，再做加法，最后汇总。
        System.out.println("===== 任务1：需要先查询、再计算的复合问题 =====");
        String task1 = "请查询深圳和广州的人口，然后算出这两座城市的人口总和，并用一句话告诉我结论。";
        System.out.println("用户：" + task1);
        System.out.println("智能体推理过程中调用的工具如下：");
        String answer1 = agent.research(task1);   // 内部可能多次调用工具
        System.out.println("智能体最终回答：" + answer1 + "\n");

        // 6) 追问：因为有记忆，智能体能“接着上文”继续推理（再查一座城市并累加）。
        System.out.println("===== 任务2：基于上文的追问（验证记忆 + 继续多步推理）=====");
        String task2 = "在刚才的总和基础上，再加上杭州的人口，最终三市总人口是多少？";
        System.out.println("用户：" + task2);
        System.out.println("智能体推理过程中调用的工具如下：");
        String answer2 = agent.research(task2);
        System.out.println("智能体最终回答：" + answer2);

        System.out.println("\n========== 模块17 演示结束 ==========\n");
    }
}
