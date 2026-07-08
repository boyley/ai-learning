package com.example.springaialibaba.reactagent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * 模块 22：ReAct 智能体演示（ChatClient + @Tool 自动行动循环）
 * ============================================================================
 *
 * 【ReAct 循环在幕后是怎么跑的】
 *
 *   用户问：“杭州今天天气怎么样？再帮我算 18 + 24 等于多少。”
 *
 *   ┌── Reason 推理：模型判断“天气我不知道，需要查；加法要算得准，也调工具”
 *   │
 *   ├── Act 行动1：自动调用 queryWeather("杭州")  ── 观察 ──► “晴，26℃…”
 *   │
 *   ├── Act 行动2：自动调用 add(18, 24)           ── 观察 ──► 42
 *   │
 *   └── Reason 收尾：信息齐了 → 组织成一句自然语言答复返回给用户
 *
 *   这一整套“调工具 → 拿结果 → 再想 → 再答”的多轮循环，由 Spring AI 自动完成，
 *   我们只需 .tools(new ReactTools()) 把工具交给 ChatClient 即可。
 * ============================================================================
 */
@Component
public class ReactAgentRunner implements CommandLineRunner {

    private final ChatClient chatClient;

    public ReactAgentRunner(ChatClient.Builder chatClientBuilder) {
        // 把工具箱挂到 ChatClient 上：模型在需要时会自动调用其中的 @Tool 方法
        this.chatClient = chatClientBuilder
                .defaultSystem("你是一个善于使用工具的智能助手。当你缺少信息或需要精确计算时，"
                        + "请调用可用的工具来获取答案，不要凭空编造。")
                .defaultTools(new ReactTools())
                .build();
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块22：ReAct 智能体(推理→行动→再回答) ==========\n");

        String question = "杭州今天天气怎么样？另外帮我算一下 18 加 24 等于多少。";
        System.out.println("【用户提问】" + question + "\n");
        System.out.println("——— 智能体开始工作（下面若打印“工具被调用”，说明它在自主行动 Act）———");

        // 一次 call()，Spring AI 内部会自动完成“模型↔工具”的多轮 ReAct 循环，
        // 直到模型认为信息够了，返回最终自然语言答案。
        String answer = chatClient.prompt()
                .user(question)
                .call()
                .content();

        System.out.println("\n【智能体最终回答】\n" + answer);

        System.out.println("\n========== 演示结束：这就是 ReAct——AI 会自己动手再回答！ ==========\n");
    }
}
