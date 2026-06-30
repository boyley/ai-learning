package com.example.lc4j.chatmodel;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.TokenUsage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * 底层 ChatModel API 演示：用消息对象 + ChatRequest/ChatResponse 精细控制
 * ============================================================================
 *
 * 【流程图】
 *
 *   SystemMessage("你是...")  ┐
 *                            ├─► ChatRequest ──► model.chat(request) ──► ChatResponse
 *   UserMessage("问题")       ┘                                              │
 *                                                                            ├─ aiMessage().text() 回答文本
 *                                                                            ├─ tokenUsage()       Token 用量
 *                                                                            └─ finishReason()     结束原因
 *
 * 【三种消息角色】
 *   - SystemMessage：系统指令（设定 AI 身份/风格），优先级最高。
 *   - UserMessage：用户输入。
 *   - AiMessage：模型输出（响应里取出来的就是它）。
 * ============================================================================
 */
@Component
public class ChatModelRunner implements CommandLineRunner {

    @Value("${langchain4j.openai.chat.base-url}")
    private String baseUrl;
    @Value("${langchain4j.openai.chat.api-key}")
    private String apiKey;
    @Value("${langchain4j.openai.chat.model}")
    private String modelName;

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块02：底层 ChatModel API ==========\n");

        // 构建对话模型（同模块01，用接口类型接收）
        ChatModel model = OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .build();

        demo1_simpleString(model);
        demo2_messagesAndRequest(model);
    }

    /** 演示1：最简调用 chat(String)，回顾模块01。 */
    private void demo1_simpleString(ChatModel model) {
        System.out.println("===== 演示1：最简调用 model.chat(String) =====");
        String question = "用一句话解释什么是 JVM？";          // 先把问题存成变量，便于打印
        System.out.println("我问：" + question);              // ★ 打印问题，让你看清问的是什么
        String answer = model.chat(question);
        System.out.println("AI ：" + answer + "\n");
    }

    /**
     * 演示2：用消息对象 + ChatRequest/ChatResponse。
     * 这是 LangChain4j 对话的“完整骨架”，能携带多条消息并取出 Token 用量等元信息。
     */
    private void demo2_messagesAndRequest(ChatModel model) {
        System.out.println("===== 演示2：SystemMessage + UserMessage + ChatRequest/ChatResponse =====");

        // SystemMessage：给 AI 设定身份与风格。SystemMessage.from(...) 是工厂方法。
        SystemMessage system = SystemMessage.from("你是一位耐心的 Java 老师，回答简洁、面向初学者。");
        // UserMessage：用户的问题。
        UserMessage user = UserMessage.from("什么是变量？请一句话说明。");
        // ★ 打印这次对话的两条输入消息，让你看清“问的是什么、设定了什么人设”
        System.out.println("我设定(System)：" + system.text());
        System.out.println("我问(User)    ：" + user.singleText());

        // ChatRequest：把多条消息打包成一次请求（消息顺序即对话顺序）。
        ChatRequest request = ChatRequest.builder()
                .messages(system, user)   // 可变参数：按顺序传入多条消息
                .build();

        // model.chat(ChatRequest) 返回完整的 ChatResponse（区别于 chat(String) 只返回文本）。
        ChatResponse response = model.chat(request);

        // 从响应里取出 AiMessage（AI 的回答消息），再 .text() 取纯文本。
        AiMessage aiMessage = response.aiMessage();
        System.out.println("AI：" + aiMessage.text());

        // tokenUsage()：本次调用消耗的 Token 数（输入/输出/总计），用于估算成本。
        TokenUsage usage = response.tokenUsage();
        if (usage != null) {
            System.out.println("Token 用量 -> 输入:" + usage.inputTokenCount()
                    + " 输出:" + usage.outputTokenCount()
                    + " 总计:" + usage.totalTokenCount());
        }
        // finishReason()：结束原因（如 STOP 正常结束、LENGTH 达到长度上限等）。
        System.out.println("结束原因：" + response.finishReason());
        System.out.println("\n========== 模块02 演示结束 ==========\n");
    }
}
