package com.example.springaialibaba.vectorstore.milvus;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 * Spring AI Alibaba + Milvus 演示：DashScope 向量化 → 存入 Milvus → 语义检索
 * ============================================================================
 * 代码与纯 Spring AI 的 10b 完全一致（都是 VectorStore 接口）——只是底层 embedding
 * 换成 DashScope（1024 维）。再次印证：上层业务代码与"用哪个模型/哪个向量库"解耦。
 * ============================================================================
 */
@Component
public class VectorStoreMilvusRunner implements CommandLineRunner {

    private final VectorStore vectorStore;

    public VectorStoreMilvusRunner(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块10b：Alibaba · VectorStore · Milvus 版 ==========\n");
        System.out.println("VectorStore 实现类：" + vectorStore.getClass().getSimpleName()
                + "（应为 MilvusVectorStore）\n");

        try {
            List<Document> docs = List.of(
                    new Document("通义千问是阿里巴巴的大语言模型。", Map.of("category", "model")),
                    new Document("Milvus 是云原生的分布式向量数据库。", Map.of("category", "db")),
                    new Document("Spring AI Alibaba 构建在 Spring AI 之上，提供 DashScope 与 Graph。", Map.of("category", "framework")),
                    new Document("百炼是阿里云的一站式大模型服务平台。", Map.of("category", "platform"))
            );

            vectorStore.add(docs);
            System.out.println("✅ 已写入 " + docs.size() + " 条文档到 Milvus（DashScope 向量，1024 维）\n");

            String query = "阿里有哪些大模型相关的东西？";
            System.out.println("===== 语义检索：query = " + query + " =====");
            List<Document> hits = vectorStore.similaritySearch(
                    SearchRequest.builder().query(query).topK(3).build());
            hits.forEach(d -> System.out.println("  🔎 " + d.getText()
                    + "  (category=" + d.getMetadata().get("category") + ")"));

        } catch (Exception e) {
            System.out.println("！Milvus 操作失败——通常是 Milvus 没启动。");
            System.out.println("  先启动：cd vector-db-learning/01-milvus && docker compose up -d");
            System.out.println("  原始错误：" + e.getClass().getSimpleName() + " - " + e.getMessage());
        }

        System.out.println("\n========== 模块10b 演示结束 ==========\n");
    }
}
