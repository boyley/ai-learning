package com.example.springaialibaba.vectorstore;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ============================================================================
 * 内存向量库演示：存入几条文档 → 用一个问题按语义检索最相近的几条
 * ============================================================================
 *
 * 【核心流程图】
 *
 *   建库：  EmbeddingModel ──► SimpleVectorStore.builder(...).build()
 *
 *   写入：  List<Document> ──► store.add(docs)
 *              （store 内部会把每条 Document 的文本自动向量化后存起来）
 *
 *   检索：  "问题" ──► store.similaritySearch(SearchRequest.query("问题").topK(2))
 *                        └─► 返回语义最相近的前 2 条 Document
 *
 * 【关键 API 链路解读】
 *   SimpleVectorStore.builder(embeddingModel).build()   -> 创建内存向量库
 *   new Document("文本")                                 -> 一条待存的数据
 *   store.add(List<Document>)                            -> 写入（内部自动向量化）
 *   SearchRequest.builder().query(q).topK(k).build()     -> 构造检索请求
 *   store.similaritySearch(request)                      -> 返回 List<Document>（带相似度分 score）
 *
 * 【重点】这些类全部来自 org.springframework.ai.*（Spring AI 通用 API）。
 * ============================================================================
 */
@Component
public class VectorStoreRunner implements CommandLineRunner {

    /** 向量化模型：由 starter 自动配置（text-embedding-v3），向量库靠它把文本变成向量。 */
    private final EmbeddingModel embeddingModel;

    public VectorStoreRunner(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块10：内存向量库 SimpleVectorStore ==========\n");

        // -------- 第 1 步：用 EmbeddingModel 建一个内存向量库 --------
        // builder(embeddingModel) 告诉向量库“用哪个模型来向量化文本”。
        SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();
        System.out.println("【1) 已创建内存向量库 SimpleVectorStore（数据存内存，退出即清空）】");

        // -------- 第 2 步：准备几条文档并写入 --------
        // 每条 Document 就是一段文本；这里故意混入不同话题，方便观察检索是否“找得准”。
        List<Document> docs = List.of(
                new Document("苹果是一种常见的水果，富含维生素，口感清脆香甜。"),
                new Document("香蕉含有丰富的钾元素，是运动后补充能量的好选择。"),
                new Document("Java 是一门广泛使用的面向对象编程语言，一次编写到处运行。"),
                new Document("Python 语法简洁，常用于数据分析、人工智能和脚本开发。"),
                new Document("长城是中国古代伟大的军事防御工程，绵延上万里。")
        );
        store.add(docs);   // ★写入：向量库内部会自动把每条文本向量化后存起来
        System.out.println("【2) 已写入 " + docs.size() + " 条文档（水果 2 条、编程语言 2 条、名胜 1 条）】");

        // -------- 第 3 步：按语义检索 --------
        // 注意：问题里并没有出现“苹果/香蕉”字样，但因为是“按语义”而非“按关键词”，
        // 向量库依然能把两条“水果”文档捞出来。
        String question = "有哪些好吃又健康的食物？";
        List<Document> hits = store.similaritySearch(
                SearchRequest.builder()
                        .query(question)   // 检索问题
                        .topK(2)           // 只要最相近的前 2 条
                        .build()
        );

        System.out.println("\n【3) 检索】问题：" + question);
        System.out.println("  向量库返回语义最相近的 " + (hits == null ? 0 : hits.size()) + " 条：");
        if (hits != null) {
            int i = 1;
            for (Document d : hits) {
                // getScore() 是相似度得分（越大越相近）；可能为 null，做个兜底。
                Double score = d.getScore();
                System.out.printf("    [%d] (相似度 %s) %s%n",
                        i++, score == null ? "-" : String.format("%.4f", score), d.getText());
            }
        }

        System.out.println("\n【结论】检索靠的是“语义相近”而非“关键词命中”——问题里没写“苹果/香蕉”，");
        System.out.println("        照样把两条水果文档排到最前面。这正是 RAG(模块11) 的检索环节。");
        System.out.println("\n========== 演示结束：你已会“存文档 + 按语义检索” ==========\n");
    }
}
