package com.example.springai.vectorstore.milvus;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 * Milvus 向量库演示：文档入库(自动向量化) → 语义相似检索(可带元数据过滤)
 * ============================================================================
 *
 * 【流程图】
 *
 *   几条文档(Document: 文本 + 元数据)
 *          │  vectorStore.add(docs)
 *          │  （MilvusVectorStore 内部自动调 EmbeddingModel 把文本转向量，再写入 Milvus）
 *          ▼
 *   Milvus collection: spring_ai_milvus_demo（HNSW 索引, COSINE 度量）
 *          ▲
 *          │  vectorStore.similaritySearch(SearchRequest.query(...).topK(n))
 *          │  （查询文本也被自动向量化，再到 Milvus 做 ANN Top-K 检索）
 *   用户查询 ─────────────────────────►  返回按相似度排序的 Document 列表
 *
 * 【对比模块 10 内存版】除了依赖与 yml 配置不同，下面的 add / similaritySearch 代码一字未改。
 * ============================================================================
 */
@Component
public class VectorStoreMilvusRunner implements CommandLineRunner {

    // ★ 注入的是接口 VectorStore；因为引入了 milvus starter，Spring Boot 注入的实现是 MilvusVectorStore。
    private final VectorStore vectorStore;

    public VectorStoreMilvusRunner(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块10b：VectorStore · Milvus 版 ==========\n");
        System.out.println("VectorStore 实现类：" + vectorStore.getClass().getSimpleName()
                + "（应为 MilvusVectorStore）\n");

        try {
            // 1) 准备几条文档（带元数据 category，用于演示过滤检索）
            List<Document> docs = List.of(
                    new Document("Milvus 是云原生的分布式向量数据库，适合海量向量检索。", Map.of("category", "db")),
                    new Document("Spring AI 让 Java 开发者用熟悉的方式调用大模型。", Map.of("category", "framework")),
                    new Document("HNSW 是一种图索引，用分层可导航小世界图加速近似最近邻检索。", Map.of("category", "algo")),
                    new Document("Redis 也能通过 RediSearch 模块存向量做相似检索。", Map.of("category", "db"))
            );

            // 2) 入库：add 内部自动把文本向量化后写入 Milvus
            vectorStore.add(docs);
            System.out.println("✅ 已写入 " + docs.size() + " 条文档到 Milvus\n");

            // 3) 语义检索：Top-3 最相关（查询文本会被自动向量化）
            String query = "有哪些向量数据库？";
            System.out.println("===== 语义检索：query = " + query + " =====");
            List<Document> hits = vectorStore.similaritySearch(
                    SearchRequest.builder().query(query).topK(3).build());
            hits.forEach(d -> System.out.println("  🔎 " + d.getText()
                    + "  (category=" + d.getMetadata().get("category") + ")"));

            // 4) 带元数据过滤的检索：只在 category == 'db' 的文档里语义检索
            System.out.println("\n===== 过滤检索：仅 category='db' =====");
            List<Document> dbHits = vectorStore.similaritySearch(SearchRequest.builder()
                    .query(query).topK(3)
                    .filterExpression("category == 'db'")   // ★ 标量过滤 + 向量检索
                    .build());
            dbHits.forEach(d -> System.out.println("  🔎 " + d.getText()));

        } catch (Exception e) {
            // 最典型失败：Milvus 没启动（Connection refused）。
            System.out.println("！Milvus 操作失败——通常是 Milvus 没启动。");
            System.out.println("  先启动 Milvus：cd vector-db-learning/01-milvus && docker compose up -d");
            System.out.println("  原始错误：" + e.getClass().getSimpleName() + " - " + e.getMessage());
        }

        System.out.println("\n========== 模块10b 演示结束 ==========\n");
    }
}
