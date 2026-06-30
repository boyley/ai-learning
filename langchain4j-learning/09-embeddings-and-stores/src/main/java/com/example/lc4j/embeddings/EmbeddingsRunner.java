package com.example.lc4j.embeddings;

import dev.langchain4j.data.embedding.Embedding;                              // 向量：一串浮点数
import dev.langchain4j.data.segment.TextSegment;                             // 文本片段：被向量化与存储的基本单位
import dev.langchain4j.model.embedding.EmbeddingModel;                       // 向量模型接口
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;                    // OpenAI 向量模型实现
import dev.langchain4j.store.embedding.EmbeddingMatch;                       // 一条检索命中（含分数+原文）
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;              // 检索请求（查询向量+条数+阈值）
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;     // 内存向量库
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ============================================================================
 * Embeddings & Stores 演示：向量化 → 入库 → 语义检索
 * ============================================================================
 *
 * 【流程图】
 *
 *   几条知识文本(字符串)
 *          │  TextSegment.from(...)
 *          ▼
 *   TextSegment ──► embeddingModel.embed(seg).content() ──► Embedding(向量)
 *          │                                                     │
 *          │                          store.add(embedding, segment)
 *          ▼                                                     ▼
 *   InMemoryEmbeddingStore<TextSegment>  ◄────────────────  向量 + 原文 一起存好
 *          ▲
 *          │  查询时：把问题也向量化 → EmbeddingSearchRequest → store.search(...)
 *          │
 *   用户查询("怎么养猫？")  ──►  返回按相似度排序的 EmbeddingMatch 列表(分数+原文)
 * ============================================================================
 */
@Component
public class EmbeddingsRunner implements CommandLineRunner {

    // ★ 注意：这里注入的是 embedding.* 配置（指向真正的 OpenAI），不是 chat.*（那是 DeepSeek）。
    @Value("${langchain4j.openai.embedding.base-url}")
    private String baseUrl;
    @Value("${langchain4j.openai.embedding.api-key}")
    private String apiKey;
    @Value("${langchain4j.openai.embedding.model}")
    private String modelName;

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块09：Embeddings & Stores 向量化与向量存储 ==========\n");

        // 1) 构建向量模型（连接 OpenAI，模型如 text-embedding-3-small，输出 1536 维向量）
        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .build();

        // 2) 创建一个内存向量库；泛型 <TextSegment> 表示“每条向量同时携带它的原文片段”
        InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();

        // 3) 准备几条知识，逐条：包装成 TextSegment → 向量化 → 连同原文存入向量库
        List<String> knowledge = List.of(
                "猫咪每天需要充足饮水，建议提供干净的流动水源。",
                "Java 是一种面向对象的编程语言，运行在 JVM 上。",
                "狗狗需要每天散步以保持健康和良好情绪。",
                "Spring Boot 让构建独立运行的 Java 应用变得非常简单。"
        );
        for (String text : knowledge) {
            TextSegment segment = TextSegment.from(text);                 // 文本 → 片段
            Embedding embedding = embeddingModel.embed(segment).content(); // 片段 → 向量（embed 返回 Response，再 .content() 取出向量）
            store.add(embedding, segment);                                 // 把【向量 + 原文】一起存入向量库
            System.out.println("  已入库：" + text);
        }
        System.out.println("当前向量库条数：" + store.size() + "\n");      // size() 查看库里有多少条

        // 4) 语义检索：给一个查询，看库里哪几条“意思最接近”
        String query = "怎么照顾我的宠物？";
        System.out.println("===== 语义检索：query = " + query + " =====");

        Embedding queryEmbedding = embeddingModel.embed(query).content();  // 查询文本也要先向量化

        // 构建检索请求：查询向量 + 最多返回几条 + 最低相似度阈值（0~1，越大越严格）
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)   // 用来比对的查询向量
                .maxResults(2)                    // 最多返回 2 条最相近的
                .minScore(0.5)                    // 相似度低于 0.5 的直接丢弃
                .build();

        // store.search(...) 返回 EmbeddingSearchResult，.matches() 得到按相似度降序排列的命中列表
        List<EmbeddingMatch<TextSegment>> matches = store.search(request).matches();
        for (EmbeddingMatch<TextSegment> match : matches) {
            // match.score()：相似度分数(越大越相近)；match.embedded()：取回当初存入的原文 TextSegment
            System.out.printf("  相似度 %.4f → %s%n", match.score(), match.embedded().text());
        }
        System.out.println("\n（说明：'养宠物' 与 '猫咪饮水 / 狗狗散步' 语义相近，与 'Java / Spring Boot' 相距很远）");

        System.out.println("\n========== 模块09 演示结束 ==========\n");
    }
}
