package com.example.springaialibaba.nacosprompt;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 * 模块 24：Nacos 动态 Prompt 演示（带无 Nacos 环境的本地兜底）
 * ============================================================================
 *
 * 【核心思想（无论 Prompt 来自哪里，渲染机制都一样）】
 *   一个 Prompt 模板长这样（{xxx} 是占位符）：
 *       "你是一位{role}。请用{style}的风格回答用户的问题。"
 *   运行时把占位符替换成实际变量，得到最终提示词。
 *   - Nacos 版：模板字符串来自 Nacos 配置中心，改配置即热更新；
 *   - 本地版：模板字符串写在代码里（本模块兜底演示用）。
 *   渲染都用 Spring AI 的 PromptTemplate，效果完全一致。
 *
 * 【本 Runner 做两件事】
 *   1. 探测容器里有没有 Nacos 动态 Prompt 相关组件（按 Bean 名关键字，兼容各版本、零硬编码类名）。
 *   2. 用本地 PromptTemplate 演示“同一模板 + 不同变量 → 不同提示词 → 不同回答”。
 * ============================================================================
 */
@Component
public class NacosPromptRunner implements CommandLineRunner {

    private final ChatClient chatClient;
    /** 注入 Spring 容器本身，用来“探测”是否存在 Nacos 相关 Bean（不依赖任何具体 Nacos 类，保证编译稳） */
    private final ApplicationContext applicationContext;

    public NacosPromptRunner(ChatClient.Builder chatClientBuilder,
                             ApplicationContext applicationContext) {
        this.chatClient = chatClientBuilder.build();
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块24：Nacos 动态 Prompt(配置中心托管提示词) ==========\n");

        // ---- 第 1 步：探测是否已接入 Nacos 动态 Prompt ----
        List<String> nacosBeans = new ArrayList<>();
        for (String name : applicationContext.getBeanDefinitionNames()) {
            String lower = name.toLowerCase();
            if (lower.contains("nacos") || lower.contains("prompttemplate")) {
                nacosBeans.add(name);
            }
        }

        if (nacosBeans.isEmpty()) {
            System.out.println("【探测结果】未连接 Nacos，跳过动态拉取；见 README 学习如何搭建 Nacos。");
        } else {
            System.out.println("【探测结果】检测到疑似 Nacos / Prompt 相关组件 " + nacosBeans.size()
                    + " 个（说明可能已接入 Nacos 动态 Prompt）：");
            nacosBeans.forEach(b -> System.out.println("  - " + b));
            System.out.println("  真实用法：从 Nacos 拉取模板字符串，替换掉下面的本地模板即可（渲染方式相同）。");
        }

        // ---- 第 2 步：本地 PromptTemplate 演示“模板 + 变量 → 提示词” ----
        // 在真实 Nacos 场景里，这段模板字符串会从 Nacos 配置中心读取；改配置即热更新。
        String templateText = "你是一位{role}。请用{style}的风格，回答用户的问题：{question}";
        System.out.println("\n【提示词模板】" + templateText);

        // 同一个模板，喂不同的变量，得到完全不同的提示词与回答 —— 这正是“动态 Prompt”的价值
        renderAndAsk(templateText,
                Map.of("role", "严谨的法律顾问", "style", "正式、谨慎",
                        "question", "签合同前要注意什么？"));

        renderAndAsk(templateText,
                Map.of("role", "幽默的旅行博主", "style", "轻松、有趣",
                        "question", "第一次去杭州玩，有什么建议？"));

        System.out.println("========== 演示结束：改 Prompt 不改代码——这就是动态 Prompt！ ==========\n");
    }

    /** 用给定变量渲染模板，得到最终提示词，再交给大模型回答 */
    private void renderAndAsk(String templateText, Map<String, Object> vars) {
        // Spring AI 的 PromptTemplate：把 {占位符} 替换成实际变量值
        PromptTemplate promptTemplate = new PromptTemplate(templateText);
        String finalPrompt = promptTemplate.render(vars);

        System.out.println("\n----------------------------------------");
        System.out.println("【本次变量】" + vars);
        System.out.println("【渲染后的提示词】" + finalPrompt);

        String answer = chatClient.prompt()
                .user(finalPrompt)
                .call()
                .content();
        System.out.println("【AI 答】" + answer);
    }
}
