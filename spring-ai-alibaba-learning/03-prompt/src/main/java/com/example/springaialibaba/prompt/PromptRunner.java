package com.example.springaialibaba.prompt;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * ============================================================================
 * 模块 03 演示：System/User 角色 + PromptTemplate 占位符模板
 * ============================================================================
 *
 * 【一次对话里的消息角色】
 *
 *   System(系统提示) ──► 设定 AI 的身份/风格/规则（“你是谁、要怎么答”）
 *   User(用户提示)   ──► 用户真正的问题（“我想问什么”）
 *   Assistant(助手)  ──► AI 的回答（由模型产生，我们只需读取）
 *
 *   同一个问题，配不同的 System，回答风格会截然不同——这就是“角色”的威力。
 *
 * 【PromptTemplate 模板渲染】
 *
 *   模板串： "请给一位叫 {name} 的{role}写一句{festival}祝福语。"
 *                       │        │          │
 *                       └────────┴──────────┴──►  占位符，运行时用 Map 填充
 *
 *   template.render(Map.of("name","小明", ...))  ->  得到填好的最终文本(String)
 *
 * 【关键 API】
 *   chatClient.prompt().system("人设").user("问题").call().content()
 *   new PromptTemplate("...{x}...").render(Map.of("x", "值"))  // 渲染成 String
 * ============================================================================
 */
@Component
public class PromptRunner implements CommandLineRunner {

    private final ChatClient chatClient;

    public PromptRunner(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块03：Prompt 提示词（角色 + 模板）==========\n");

        // ---------------- 演示1：同一问题，不同 System 人设 ----------------
        System.out.println("---------- 演示1：System 角色决定回答风格（同一个问题，两种人设）----------");
        String question = "怎么才能睡个好觉？";
        System.out.println("【用户问题】" + question + "\n");

        // 人设A：严谨的医生
        String answerDoctor = chatClient
                .prompt()
                .system("你是一位严谨专业的医生，回答简洁、有条理，只讲科学建议。")  // ★ System 设定身份/风格
                .user(question)                                                    // User 提问
                .call()
                .content();
        System.out.println("【人设A·严谨医生】\n" + answerDoctor + "\n");

        // 人设B：俏皮的诗人
        String answerPoet = chatClient
                .prompt()
                .system("你是一位俏皮的诗人，喜欢用比喻和押韵，回答充满诗意。")     // ★ 换个 System
                .user(question)
                .call()
                .content();
        System.out.println("【人设B·俏皮诗人】\n" + answerPoet + "\n");

        // ---------------- 演示2：PromptTemplate 占位符模板 ----------------
        System.out.println("---------- 演示2：PromptTemplate 占位符 {..} 模板渲染 ----------");
        // 定义一个带 3 个占位符的模板串
        PromptTemplate template = new PromptTemplate(
                "请给一位叫 {name} 的{role}，写一句温暖的{festival}祝福语，20 字以内。");
        // 用真实值填充占位符，得到最终提示词文本
        String rendered = template.render(Map.of(
                "name", "小明",
                "role", "小学老师",
                "festival", "教师节"));
        System.out.println("【模板渲染后的最终提示词】" + rendered);

        // 把渲染好的文本作为 user 消息发给模型
        String blessing = chatClient
                .prompt()
                .user(rendered)
                .call()
                .content();
        System.out.println("【AI 生成的祝福语】" + blessing + "\n");

        // ---------------- 演示3：System + User 一起用（角色扮演客服）----------------
        System.out.println("---------- 演示3：System(人设) + User(问题) 组合 ----------");
        String answer = chatClient
                .prompt()
                .system("你是“悦读书店”的智能客服，语气亲切，回答控制在两句话内。")
                .user("你们周末营业到几点？")
                .call()
                .content();
        System.out.println("【客服回答】" + answer + "\n");

        System.out.println("========== 演示结束：你已掌握角色设定与模板渲染 ==========\n");
    }
}
