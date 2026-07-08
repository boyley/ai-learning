package com.example.springaialibaba.promptengineering;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * 提示工程演示：用纯 ChatClient 对比 零样本 / 少样本 / 思维链 / 角色扮演 四种模式
 * ============================================================================
 *
 * 【核心思想】
 *   模式不同 = system()/user() 里的文字不同。同一个模型，换个问法，效果差异明显。
 *
 *   ┌── 零样本 ──► 直接下指令               （最省事，简单任务够用）
 *   ├── 少样本 ──► 指令 + 几个示例           （教模型“照这个格式/风格来”）
 *   ├── 思维链 ──► 指令 + “请一步步思考”      （逼模型展开推理，提升正确率）
 *   └── 角色扮演 ► system 设定专家身份        （改变视角/专业度/口吻）
 *
 * 【关键 API】
 *   chatClient.prompt().system(设定/示例).user(问题).call().content();
 *   —— 四种模式都只是往 system()/user() 塞不同文本，API 完全一样。
 * ============================================================================
 */
@Component
public class PromptEngineeringRunner implements CommandLineRunner {

    private final ChatClient chatClient;

    public PromptEngineeringRunner(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /** 小工具：发一次对话，返回回答文本 */
    private String ask(String system, String user) {
        ChatClient.ChatClientRequestSpec spec = chatClient.prompt();
        if (system != null && !system.isBlank()) {
            spec = spec.system(system);   // 有 system 才设置
        }
        return spec.user(user).call().content();
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块18：提示工程四大模式 ==========\n");

        // =====================================================================
        // 模式 1：零样本 Zero-shot —— 直接下指令，不给任何示例
        // 适用：任务简单、模型本来就会。最省 token、最省事。
        // =====================================================================
        System.out.println("---------- ① 零样本 Zero-shot（直接下指令） ----------");
        String zeroShot = ask(
                null,
                "判断这句话的情感是“正面”还是“负面”，只回答两个字：这家店的服务太贴心了，下次还来。");
        System.out.println("【提示】直接问，不给例子。");
        System.out.println("【AI 答】" + zeroShot + "\n");

        // =====================================================================
        // 模式 2：少样本 Few-shot —— 给几个“输入→输出”示例，让模型照格式来
        // 适用：需要固定输出格式/风格，用语言不好描述时，用例子最直接。
        // =====================================================================
        System.out.println("---------- ② 少样本 Few-shot（给几个示例） ----------");
        String fewShotSystem = """
                你是情感分类器。请严格按示例的格式输出，只输出一个标签：正面 / 负面 / 中性。
                示例：
                输入：这手机电池一天一充，太不耐用了。 输出：负面
                输入：物流很快，包装也完好。         输出：正面
                输入：颜色和描述一致，没别的感受。     输出：中性
                """;
        String fewShot = ask(fewShotSystem, "输入：客服半天不回消息，问题也没解决。 输出：");
        System.out.println("【提示】system 里给了3个示例，规定输出为单个标签。");
        System.out.println("【AI 答】" + fewShot + "\n");

        // =====================================================================
        // 模式 3：思维链 CoT —— 让模型“一步步思考”再给结论
        // 适用：数学/逻辑/多步推理。展开推理能显著减少“拍脑袋答错”。
        // =====================================================================
        System.out.println("---------- ③ 思维链 CoT（请一步步思考） ----------");
        String mathQuestion = "小明有 3 盒铅笔，每盒 12 支，他送给同学 8 支后，又买了 2 盒。现在他有多少支铅笔？";
        // 3-1 不用 CoT（容易直接报一个数，可能算错）
        String noCot = ask(null, mathQuestion + " 请只给出最终数字。");
        // 3-2 用 CoT（要求先分步计算，再给结论）
        String cot = ask(
                "你是严谨的数学助教。解题时请先一步步列出计算过程，最后一行用“答案：”给出结果。",
                mathQuestion);
        System.out.println("【问题】" + mathQuestion);
        System.out.println("【不用CoT · 只要答案】" + noCot);
        System.out.println("【用CoT · 分步推理】\n" + cot + "\n");

        // =====================================================================
        // 模式 4：角色扮演 Role —— system 设定专家身份，改变视角与口吻
        // 适用：想要特定专业视角/语气时。同一问题，不同角色答法完全不同。
        // =====================================================================
        System.out.println("---------- ④ 角色扮演 Role（设定专家身份） ----------");
        String question4 = "我该怎么开始学习编程？";
        String asTeacher = ask(
                "你是一位耐心的少儿编程老师，面向10岁孩子，用生活比喻、鼓励的口吻回答，控制在3句话内。",
                question4);
        String asArchitect = ask(
                "你是一位资深软件架构师，面向准备转行的成年人，语言精炼专业，给出可执行的路线，控制在3句话内。",
                question4);
        System.out.println("【同一问题】" + question4);
        System.out.println("【角色A·少儿编程老师】" + asTeacher);
        System.out.println("【角色B·资深架构师】" + asArchitect + "\n");

        System.out.println("【总结】四种模式适用场景：");
        System.out.println("  零样本→简单任务省事；少样本→要固定格式/风格；");
        System.out.println("  思维链→数学逻辑多步推理；角色扮演→要特定专业视角与口吻。");
        System.out.println("\n========== 演示结束：把话说好，模型才答得好 ==========\n");
    }
}
