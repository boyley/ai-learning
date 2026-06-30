package com.example.lc4j.skills;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.skills.Skill;
import dev.langchain4j.skills.Skills;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * Skills 演示：定义一个「周报写作规范」技能，让模型按需激活后据规范产出周报
 * ============================================================================
 *
 * 【流程图】（ASCII）
 *
 *   Skill.builder().name/description/content(...)   ← 定义一个技能(打包好的说明书)
 *          │  Skills.from(skill)
 *          ▼
 *   Skills  ──.toolProvider()──►  ToolProvider     ← 技能被适配成「激活技能」工具
 *          │
 *          │  AiServices.builder(接口).chatModel(m).toolProvider(provider).build()
 *          ▼
 *   SkillsAssistant (AI Service 代理)
 *          │  assistant.chat("帮我写本周周报：完成了登录模块和单元测试")
 *          ▼
 *   模型看到技能「描述」→ 判断相关 → 激活技能(读到完整规范) → 按规范输出周报
 *
 * 【对比】没有技能时，模型只会按自己的习惯随意排版；
 *        装上技能后，模型会读到我们规定的格式(三段式 + emoji + 字数限制)并照做。
 *
 * 【提示】真正调用需要有效 DeepSeek Key 且会计费。未配置 Key 时本类用 try/catch 友好提示，
 *        不让程序崩溃；本模块以「编译通过」为目标。
 * ============================================================================
 */
@Component
public class SkillsRunner implements CommandLineRunner {

    // 用 chat.* 一组属性（指向 DeepSeek），和前面对话类模块一致
    @Value("${langchain4j.openai.chat.base-url}")
    private String baseUrl;
    @Value("${langchain4j.openai.chat.api-key}")
    private String apiKey;
    @Value("${langchain4j.openai.chat.model}")
    private String modelName;

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块21：Skills（技能）==========\n");

        // 0) 构建底层对话模型（指向 DeepSeek）
        ChatModel model = OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .build();

        // 1) ★ 定义一个技能：周报写作规范 ★
        //    - name：技能标识；description：让模型判断「何时该用」；content：完整的操作规范(按需注入)。
        Skill weeklyReportSkill = Skill.builder()
                // 技能名：模型激活技能时要原样回传这个名字。
                // ★用中文名「周报生成」而非英文，避免模型把中文描述“脑补”成一个对不上的名字导致激活失败。
                .name("周报生成")                                  // 技能名(激活时的标识，需与模型回传一致)
                .description("把零散的工作记录整理成符合公司规范的标准周报")  // 简短描述(平时常驻上下文)
                .content("""
                        # 周报写作规范
                        当需要撰写周报时，严格按以下规范输出（这段内容只有技能被激活后才进入上下文）：
                        1. 必须包含三个小节，且标题固定为：
                           ## 本周完成
                           ## 存在问题
                           ## 下周计划
                        2. 每个小节用「- 」开头的要点列举，每条不超过 30 字。
                        3. 「本周完成」每条要点结尾加一个 ✅。
                        4. 若用户没提到「存在问题」或「下周计划」，则在对应小节写「- 暂无」。
                        5. 全文不加任何寒暄或多余说明，直接给周报正文。
                        """)                                      // 技能正文(渐进式披露：激活后才注入)
                .build();

        // 2) 把技能聚合进 Skills，并取出它提供的 ToolProvider。
        //    Skills 会自动注册一个「激活技能」工具，让模型能在需要时把上面的 content 拉进上下文。
        Skills skills = Skills.from(weeklyReportSkill);
        ToolProvider skillToolProvider = skills.toolProvider();

        // formatAvailableSkills()：把当前可用技能格式化成一段文本，便于我们查看/调试
        System.out.println("【已装配的技能清单】\n" + skills.formatAvailableSkills());

        // 3) ★★★ 把技能装到 AI Service 上 ★★★
        //    和模块 16 一样用 .toolProvider(...)：技能在运行时作为「可激活工具」提供给模型。
        SkillsAssistant assistant = AiServices.builder(SkillsAssistant.class)
                .chatModel(model)
                .toolProvider(skillToolProvider)
                .build();

        // 4) 提问：给一段零散的工作记录，让模型整理成规范周报。
        try {
            System.out.println("\n===== 向带有「周报技能」的助手提问 =====");
            String answer = assistant.chat(
                    "帮我写本周周报：这周完成了用户登录模块、补齐了单元测试；" +
                    "遇到的问题是测试环境数据库不稳定。");
            System.out.println("AI（已按周报规范输出）：\n" + answer);
        } catch (Exception e) {
            // 最典型的失败原因：未配置有效 DeepSeek Key（鉴权失败）。
            System.out.println("！调用模型失败（通常是未配置有效的 DeepSeek Key）。");
            System.out.println("  这是正常现象：本模块以【编译通过】为目标，实际运行需有效 Key。");
            System.out.println("  原始错误：" + e.getClass().getSimpleName() + " - " + e.getMessage());
        }

        System.out.println("\n========== 模块21 演示结束 ==========\n");
    }
}
