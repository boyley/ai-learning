package com.example.lc4j.milvus;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ============================================================================
 * LangChain4j + Milvus 演示：向量化 → 存入 Milvus → 语义检索
 * ============================================================================
 *
 * 【流程图】
 *
 *   几条知识文本
 *      │ TextSegment.from → embeddingModel.embed → Embedding(1536维)
 *      ▼
 *   MilvusEmbeddingStore.add(embedding, segment)   ← 向量 + 原文 存入 Milvus(持久化)
 *      ▲
 *      │ 查询也向量化 → EmbeddingSearchRequest → store.search(...)
 *   用户查询 ──────────────────────────► EmbeddingMatch 列表(分数 + 原文)
 *
 * 【对比模块 09】唯一区别是把 new InMemoryEmbeddingStore<>() 换成 MilvusEmbeddingStore.builder()...build()。
 * ============================================================================
 */
@Component
public class MilvusStoreRunner implements CommandLineRunner {

    // embedding 走真正的 OpenAI（embedding.* 配置），不是 chat.*（DeepSeek）
    @Value("${langchain4j.openai.embedding.base-url}")
    private String baseUrl;
    @Value("${langchain4j.openai.embedding.api-key}")
    private String apiKey;
    @Value("${langchain4j.openai.embedding.model}")
    private String modelName;

    @Value("${milvus.host:localhost}")
    private String milvusHost;
    @Value("${milvus.port:19530}")
    private int milvusPort;
    @Value("${milvus.collection:lc4j_milvus_demo}")
    private String collection;

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块09b：LangChain4j · Milvus EmbeddingStore ==========\n");

        // 1) 向量模型（OpenAI text-embedding-3-small，1536 维）
        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .baseUrl(baseUrl).apiKey(apiKey).modelName(modelName).build();

        try {
            // 2) ★ 把内存库换成 Milvus 库：只有这一处不同（对比模块09的 new InMemoryEmbeddingStore<>()）
            EmbeddingStore<TextSegment> store = MilvusEmbeddingStore.builder()
                    .host(milvusHost)
                    .port(milvusPort)
                    .collectionName(collection)
                    .dimension(1536)              // ★ 必须与 embedding 模型维度一致
                    .build();

            // 3) 入库：逐条向量化后存入 Milvus
            List<String> knowledge = List.of(
                    "Milvus 是云原生的分布式向量数据库，适合海量向量。",
                    "LangChain4j 是对标 Python LangChain 的 Java 库。",
                    "HNSW 是主流的近似最近邻图索引。",
                    "猫咪每天需要充足饮水。"
            );
            for (String text : knowledge) {
                TextSegment seg = TextSegment.from(text);
                Embedding emb = embeddingModel.embed(seg).content();
                store.add(emb, seg);
                System.out.println("  已入库：" + text);
            }

            // 4) 语义检索 Top-2
            String query = "有什么向量数据库？";
            Embedding queryEmb = embeddingModel.embed(query).content();
            EmbeddingSearchResult<TextSegment> result = store.search(EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmb)
                    .maxResults(2)
                    .build());
            System.out.println("\n===== 检索：query = " + query + " =====");
            for (EmbeddingMatch<TextSegment> m : result.matches()) {
                System.out.printf("  🔎 [%.4f] %s%n", m.score(), m.embedded().text());
            }

        } catch (Exception e) {
            System.out.println("！Milvus 操作失败——通常是 Milvus 没启动。");
            System.out.println("  先启动：cd vector-db-learning/01-milvus && docker compose up -d");
            System.out.println("  原始错误：" + e.getClass().getSimpleName() + " - " + e.getMessage());
        }

        System.out.println("\n========== 模块09b 演示结束 ==========\n");
    }
}
