package com.example.lc4j.rag;

import dev.langchain4j.data.segment.TextSegment;                                  // 文本片段
import dev.langchain4j.model.chat.ChatModel;                                      // 对话模型接口
import dev.langchain4j.model.embedding.EmbeddingModel;                            // 向量模型接口
import dev.langchain4j.model.openai.OpenAiChatModel;                              // DeepSeek(OpenAI 兼容) 对话模型
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;                         // OpenAI 向量模型
import dev.langchain4j.rag.content.retriever.ContentRetriever;                    // 检索器接口
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;     // 基于向量库的检索器实现
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;          // 内存向量库
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ============================================================================
 * RAG 演示：知识入库 → 装配检索器 → 助手“先检索后回答”
 * ============================================================================
 *
 * 【流程图】
 *
 *   私有知识(几段文本)
 *          │  TextSegment.from(...) + embeddingModel.embed(...) 向量化
 *          ▼
 *   InMemoryEmbeddingStore<TextSegment>（向量库，存好“向量+原文”）
 *          │  EmbeddingStoreContentRetriever.builder()
 *          ▼
 *   ContentRetriever（检索器：向量库 + 向量模型 打包而成）
 *          │  AiServices.builder(Assistant).contentRetriever(检索器)
 *          ▼
 *   Assistant 助手
 *          │
 *   用户提问 assistant.ask("公司年假几天？")
 *          │  ① 框架自动用问题去检索器里捞相关片段
 *          │  ② 把命中的资料拼进提示词
 *          │  ③ 连同问题一起发给 ChatModel
 *          ▼
 *   模型“看着资料”给出有据可依的回答
 * ============================================================================
 */
@Component
public class RagRunner implements CommandLineRunner {

    // 对话能力：指向 DeepSeek（chat.*）
    @Value("${langchain4j.openai.chat.base-url}")
    private String chatBaseUrl;
    @Value("${langchain4j.openai.chat.api-key}")
    private String chatApiKey;
    @Value("${langchain4j.openai.chat.model}")
    private String chatModelName;

    // 向量化能力：指向真正的 OpenAI（embedding.*）
    @Value("${langchain4j.openai.embedding.base-url}")
    private String embeddingBaseUrl;
    @Value("${langchain4j.openai.embedding.api-key}")
    private String embeddingApiKey;
    @Value("${langchain4j.openai.embedding.model}")
    private String embeddingModelName;

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块10：RAG 检索增强生成 ==========\n");

        // 1) 构建对话模型（DeepSeek）与向量模型（OpenAI）——RAG 同时用到这两种模型
        ChatModel chatModel = OpenAiChatModel.builder()
                .baseUrl(chatBaseUrl)
                .apiKey(chatApiKey)
                .modelName(chatModelName)
                .build();

        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .baseUrl(embeddingBaseUrl)
                .apiKey(embeddingApiKey)
                .modelName(embeddingModelName)
                .build();

        // 2) 准备私有知识，向量化后存入内存向量库（同模块09 的入库流程）
        InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
        List<String> knowledge = List.of(
                "本公司全体员工每年享有 15 天带薪年假，入职满 10 年增加到 20 天。",
                "公司报销差旅费需在出差结束后 7 个工作日内提交发票与申请单。",
                "公司食堂工作日中午 12:00 至 13:00 开放，员工就餐免费。"
        );
        for (String text : knowledge) {
            TextSegment segment = TextSegment.from(text);                       // 文本 → 片段
            store.add(embeddingModel.embed(segment).content(), segment);        // 向量化并连同原文入库
        }
        System.out.println("知识库已就绪，条数：" + store.size() + "\n");

        // 3) ★★★ 把“向量库 + 向量模型”打包成一个检索器 ★★★
        //    maxResults / minScore 控制每次最多检索几条、相似度阈值多高。
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)              // 从哪个向量库检索
                .embeddingModel(embeddingModel)     // 用哪个向量模型把“问题”向量化（必须与入库时一致）
                .maxResults(2)                      // 每次问题最多召回 2 条相关片段
                .minScore(0.5)                      // 相似度低于 0.5 的片段不采纳
                .build();

        // 4) ★★★ 核心：用 .contentRetriever(...) 把检索器接到 AI Service 上 ★★★
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel)                   // 负责“生成”回答
                .contentRetriever(contentRetriever)     // 负责“检索”相关知识（回答前自动执行）
                .build();

        // 5) 提问：答案就在私有知识里。框架会自动“先检索 → 拼提示词 → 再回答”。
        System.out.println("===== 演示1：问年假（知识库里有）=====");
        System.out.println("问：公司员工每年有多少天年假？");
        System.out.println("AI：" + assistant.ask("公司员工每年有多少天年假？") + "\n");

        System.out.println("===== 演示2：问报销时限（知识库里有）=====");
        System.out.println("问：差旅费报销有什么时间要求？");
        System.out.println("AI：" + assistant.ask("差旅费报销有什么时间要求？") + "\n");

        System.out.println("===== 演示3：问知识库里没有的内容 =====");
        System.out.println("问：公司提供免费班车吗？");
        System.out.println("AI：" + assistant.ask("公司提供免费班车吗？"));

        System.out.println("\n========== 模块10 演示结束 ==========\n");
    }
}
