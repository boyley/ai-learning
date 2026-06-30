package com.example.lc4j.classification;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * 文本分类演示：用“返回枚举的 AiService 方法”做情感分析
 * ============================================================================
 *
 * 【流程图】
 *
 *   一批待分类评论(String[])
 *          │
 *          ▼
 *   SentimentAnalyzer 接口(只声明) ──AiServices.create(接口, model)──► 动态实现
 *          │  对每条评论调用 analyzer.classify(评论)
 *          ▼
 *   LangChain4j 把 Sentiment 的常量(POSITIVE/NEGATIVE/NEUTRAL)拼进提示词
 *          │
 *          ▼
 *   调用 ChatModel ──► 大模型只回答其中一个类别名
 *          │
 *          ▼
 *   框架把回答解析成 Sentiment 枚举返回 ──► 我们打印 [类别] 原文
 * ============================================================================
 */
@Component
public class ClassificationRunner implements CommandLineRunner {

    @Value("${langchain4j.openai.chat.base-url}")
    private String baseUrl;
    @Value("${langchain4j.openai.chat.api-key}")
    private String apiKey;
    @Value("${langchain4j.openai.chat.model}")
    private String modelName;

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块11：文本分类（情感分析）==========\n");

        // 1) 构建底层对话模型（和前面模块一样，指向 DeepSeek）
        ChatModel model = OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                // temperature 设低一点，让分类结果更稳定、更确定
                .temperature(0.0)
                .build();

        // 2) ★ 用 AiServices 为“情感分析接口”生成实现 ★
        //    因为 classify 返回的是 enum，框架会自动把可选类别写进提示词并解析回枚举。
        SentimentAnalyzer analyzer = AiServices.create(SentimentAnalyzer.class, model);

        // 3) 准备一批待分类的评论
        String[] comments = {
                "这家餐厅的菜太好吃了，服务也超级棒，下次还来！",
                "等了一个小时菜还没上，态度还很差，再也不来了。",
                "就是一家普通的快餐店，没什么特别的。",
                "物流很快，但包装有点简陋。"
        };

        // 4) 逐条分类并打印——调用接口方法，就像调用本地方法一样
        System.out.println("===== 对 " + comments.length + " 条评论做情感分类 =====\n");
        for (String comment : comments) {
            Sentiment sentiment = analyzer.classify(comment);  // ★ 返回值就是 Sentiment 枚举
            System.out.println("[" + sentiment + "]\t" + comment);
        }

        // 5) 演示：拿到枚举后可以用 switch 做类型安全的后续处理
        System.out.println("\n===== 演示：用 switch 对枚举做后续处理 =====");
        Sentiment s = analyzer.classify("这个产品改变了我的生活，强烈推荐！");
        String action = switch (s) {                            // 枚举可被穷举，编译期安全
            case POSITIVE -> "好评 -> 自动点赞并感谢用户";
            case NEGATIVE -> "差评 -> 转人工客服跟进";
            case NEUTRAL  -> "中性 -> 暂不处理";
        };
        System.out.println("分类结果=" + s + "，处理动作=" + action);

        System.out.println("\n========== 模块11 演示结束 ==========\n");
    }
}
