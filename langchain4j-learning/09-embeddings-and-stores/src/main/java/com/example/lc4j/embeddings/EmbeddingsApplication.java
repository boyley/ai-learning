package com.example.lc4j.embeddings;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 09：Embeddings & Stores 文本向量化与向量存储 —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   讲两件“RAG 的地基”：
 *   1. Embedding（向量化）：把一段文本变成一串浮点数（向量）。语义越相近的文本，
 *      向量在空间里就越“靠近”。于是“语义相似度”就能用“向量距离”来计算。
 *   2. EmbeddingStore（向量存储）：把这些向量连同原文存起来，之后给一段查询文本，
 *      就能快速找出“语义最接近”的若干条——这就是“语义检索”。
 *
 * 【怎么做（核心四步）】
 *   1. 用 OpenAiEmbeddingModel.builder()...build() 构建向量模型（连接真正的 OpenAI）。
 *   2. 把每条知识 TextSegment.from(文本) → model.embed(seg).content() 得到向量 Embedding。
 *   3. 存进 InMemoryEmbeddingStore：store.add(embedding, segment)。
 *   4. 查询：把问题也向量化，用 EmbeddingSearchRequest + store.search(...) 找出最相近的几条。
 *
 * 【和关键词搜索的区别】
 *   关键词搜索要“字面命中”；向量检索理解“语义”——问“怎么养猫”，能召回“宠物饲养指南”，
 *   即使两句没有一个相同的词。
 *
 * 【注意】
 *   真正运行需要 OpenAI 的 Key 和网络（embedding 指向 api.openai.com）；
 *   本模块只要求 `mvn compile` 通过即可，理解 API 用法是重点。
 * ============================================================================
 */
@SpringBootApplication
public class EmbeddingsApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmbeddingsApplication.class, args);
    }
}
