package com.example.lc4j.moderation;

import dev.langchain4j.model.moderation.Moderation;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.model.openai.OpenAiModerationModel;
import dev.langchain4j.model.output.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ============================================================================
 * 内容审核演示：用 OpenAiModerationModel 检测文本是否违规
 * ============================================================================
 *
 * 【流程图】
 *
 *   一段待检测的文字
 *          │  注入 moderation.* 连接参数(指向 OpenAI)
 *          ▼
 *   OpenAiModerationModel.builder()....build()  ← 构建审核模型
 *          │  model.moderate(文字)
 *          ▼
 *   OpenAI 审核接口给文字打“安全标签”，返回 Response<Moderation>
 *          │  response.content()
 *          ▼
 *   Moderation 对象
 *          ├─ flagged()      → 是否被判定为违规(true/false)
 *          └─ flaggedText()  → 被标记的那段文字(未违规时通常为 null)
 *          │
 *          ▼
 *   据此决定：拦截 / 打码 / 转人工，或正常放行
 *
 * 【对话模型 vs 审核模型】
 *   - 对话模型(ChatModel)：你问它答，用来“聊天/干活”。
 *   - 审核模型(ModerationModel)：不聊天，只做一件事——给文字打“安全标签”。
 *
 * 【提示】真正调用需要有效 OPENAI_API_KEY 且会计费；无额度会返回 429。
 * ============================================================================
 */
@Component
public class ModerationRunner implements CommandLineRunner {

    // ★ 注意：用的是 moderation.* 一组属性（指向 OpenAI），而不是 chat.*（指向 DeepSeek）
    @Value("${langchain4j.openai.moderation.base-url}")
    private String baseUrl;
    @Value("${langchain4j.openai.moderation.api-key}")
    private String apiKey;
    @Value("${langchain4j.openai.moderation.model}")
    private String modelName;

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块19：内容审核（Moderation）==========\n");

        // 1) 构建审核模型。注意类型是 ModerationModel（不是 ChatModel）。
        //    OpenAiModerationModel 是 ModerationModel 接口的 OpenAI 实现。
        ModerationModel model = OpenAiModerationModel.builder()
                .baseUrl(baseUrl)       // 审核接口地址（OpenAI）
                .apiKey(apiKey)         // OpenAI 的 Key
                .modelName(modelName)   // 审核模型名，如 omni-moderation-latest
                .build();

        // 2) 准备两段测试文本：一段正常、一段明显违规
        List<String> samples = List.of(
                "今天天气真好，我们一起去公园散步吧。",   // 正常内容，预期不违规
                "I will find you and hurt you badly."     // 含暴力威胁，预期被标记违规
        );

        // 3) 逐条审核
        for (String text : samples) {
            System.out.println("【待审文本】" + text);

            // ★ 核心：moderate(文字) 返回 Response<Moderation> ★
            //   Response 是 LangChain4j 对“一次模型输出”的统一包装。
            Response<Moderation> response = model.moderate(text);

            // 从 Response 里取出审核结果对象
            Moderation moderation = response.content();

            // flagged()：综合判定是否违规
            boolean flagged = moderation.flagged();
            // flaggedText()：被标记的具体文字片段（未违规时通常为 null）
            String flaggedText = moderation.flaggedText();

            if (flagged) {
                System.out.println("  → 审核结果：⛔ 违规！被标记片段：" + flaggedText);
                System.out.println("    建议动作：拦截 / 打码 / 转人工审核");
            } else {
                System.out.println("  → 审核结果：✅ 正常，放行");
            }
            System.out.println();
        }

        System.out.println("========== 模块19 演示结束 ==========\n");
    }
}
