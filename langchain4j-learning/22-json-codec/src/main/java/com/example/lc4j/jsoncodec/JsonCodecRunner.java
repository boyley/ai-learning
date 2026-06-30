package com.example.lc4j.jsoncodec;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageJsonCodec;
import dev.langchain4j.data.message.JacksonChatMessageJsonCodec;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.internal.Json;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ============================================================================
 * JSON 编解码演示：Json 工具类 + ChatMessageJsonCodec + SPI 机制说明
 * ============================================================================
 *
 * 【流程图】
 *
 *   演示1：通用对象 <-> JSON
 *     Java 对象(Book)
 *          │  Json.toJson(对象)            ← LangChain4j 内置 JSON 工具类
 *          ▼
 *     JSON 字符串  ──  Json.fromJson(串, 类)  ──►  还原成 Java 对象
 *
 *   演示2：对话消息 <-> JSON（对话历史持久化的底层做法）
 *     List<ChatMessage>(系统/用户/AI)
 *          │  codec.messagesToJson(列表)     ← JacksonChatMessageJsonCodec
 *          ▼
 *     JSON 字符串(可存数据库/文件) ── codec.messagesFromJson(串) ──► 还原对话
 *
 *   演示3：真实对话 + 历史序列化
 *     DeepSeek 对话 ──► 得到 AiMessage ──► 整段历史 toJson ──► 打印“落库形态”
 *
 * 【两个层次的编解码器，别混淆】
 *   - dev.langchain4j.internal.Json
 *       通用对象编解码工具类（toJson/fromJson），结构化输出/工具调用底层用它。
 *   - dev.langchain4j.data.message.ChatMessageJsonCodec
 *       【专门】给 ChatMessage（多态：System/User/Ai/Tool…）做编解码的接口，
 *       默认实现 JacksonChatMessageJsonCodec 内置了各消息子类型的多态识别。
 *
 * 【SPI 替换机制（原理，见 README 详解）】
 *   Json 类在初始化时通过 ServiceLoader 查找 SPI 接口
 *   dev.langchain4j.spi.json.JsonCodecFactory 的实现；
 *   找不到才退回内置的 Jackson 实现。所以你只要在
 *   META-INF/services/ 下注册自己的 JsonCodecFactory，就能全局替换默认 JSON 实现。
 * ============================================================================
 */
@Component
public class JsonCodecRunner implements CommandLineRunner {

    // 对话用 chat.*（指向 DeepSeek）
    @Value("${langchain4j.openai.chat.base-url}")
    private String baseUrl;
    @Value("${langchain4j.openai.chat.api-key}")
    private String apiKey;
    @Value("${langchain4j.openai.chat.model}")
    private String modelName;

    /**
     * 一个普通的 Java 记录类，用作“通用对象 <-> JSON”的演示样本。
     * record 会自动生成构造器、getter、equals/toString，适合做数据载体。
     */
    public record Book(String title, String author, int year) {}

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块22：JSON 编解码定制（JSON Codec）==========\n");

        // ===================================================================
        // 演示1：用 LangChain4j 内置的 Json 工具类做通用对象的序列化/反序列化
        // ===================================================================
        System.out.println("===== 演示1：Json.toJson / Json.fromJson（通用对象）=====");
        Book book = new Book("深入理解 LangChain4j", "张三", 2026);

        // toJson(Object)：把任意 Java 对象序列化成 JSON 字符串
        String bookJson = Json.toJson(book);
        System.out.println("对象 -> JSON：" + bookJson);

        // fromJson(String, Class)：把 JSON 字符串反序列化回 Java 对象
        Book restored = Json.fromJson(bookJson, Book.class);
        System.out.println("JSON -> 对象：" + restored);
        // 反序列化得到的对象与原对象内容相等（record 自带 equals）
        System.out.println("还原是否与原对象相等：" + book.equals(restored));
        System.out.println();

        // ===================================================================
        // 演示2：用 ChatMessageJsonCodec 序列化“对话消息”——对话历史持久化的底层做法
        // ===================================================================
        System.out.println("===== 演示2：ChatMessageJsonCodec（对话历史序列化）=====");

        // JacksonChatMessageJsonCodec 是 ChatMessageJsonCodec 接口的默认 Jackson 实现。
        // 它能正确处理 ChatMessage 的多态（System/User/Ai...），序列化时会带上类型信息。
        ChatMessageJsonCodec codec = new JacksonChatMessageJsonCodec();

        // 单条消息：messageToJson(ChatMessage) -> JSON 字符串
        UserMessage one = UserMessage.from("你好，请介绍一下你自己。");
        String oneJson = codec.messageToJson(one);
        System.out.println("单条 UserMessage -> JSON：" + oneJson);

        // 多条消息：messagesToJson(List) -> JSON 字符串（这就是“整段对话历史”的存储形态）
        List<ChatMessage> history = List.of(
                SystemMessage.from("你是一个乐于助人的中文助手。"),
                UserMessage.from("1 加 1 等于几？"),
                AiMessage.from("1 加 1 等于 2。")
        );
        String historyJson = codec.messagesToJson(history);
        System.out.println("对话历史 -> JSON：" + historyJson);

        // 再从 JSON 还原回 List<ChatMessage>（演示“从数据库读回对话历史”）
        List<ChatMessage> back = codec.messagesFromJson(historyJson);
        System.out.println("JSON -> 对话历史：还原出 " + back.size() + " 条消息，首条类型="
                + back.get(0).type());
        System.out.println("用途：把这串 JSON 存进数据库/Redis/文件，下次启动读回来，AI 就“记得”上次聊了什么。");
        System.out.println();

        // ===================================================================
        // 演示3：发起一次真实对话，再把整段历史序列化成 JSON（落库形态）
        // ===================================================================
        System.out.println("===== 演示3：真实对话 + 历史落库（DeepSeek）=====");
        // 构建底层对话模型（chat.* 指向 DeepSeek）
        ChatModel model = OpenAiChatModel.builder()
                .baseUrl(baseUrl)        // 接口地址（DeepSeek 兼容地址）
                .apiKey(apiKey)          // API 密钥
                .modelName(modelName)    // 模型名（deepseek-chat）
                .build();

        String question = "用一句话解释什么是 JSON 序列化？";
        System.out.println("【我问】" + question);

        // 同步对话，得到字符串回答
        String answer = model.chat(question);
        System.out.println("【AI 答】" + answer);

        // 把“用户问 + AI 答”组装成历史，序列化成可持久化的 JSON
        List<ChatMessage> realHistory = List.of(
                UserMessage.from(question),
                AiMessage.from(answer)
        );
        System.out.println("【可落库的对话历史 JSON】\n" + codec.messagesToJson(realHistory));

        System.out.println("\n========== 模块22 演示结束 ==========\n");
    }
}
