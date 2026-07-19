package com.example.lc4j.milvus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 09b：LangChain4j · Milvus EmbeddingStore —— 启动类
 * ============================================================================
 *
 * 【和模块 09（InMemoryEmbeddingStore）的区别】
 *   - 09 用 InMemoryEmbeddingStore：向量只在内存，退出即丢，仅供学习。
 *   - 本模块用 MilvusEmbeddingStore：向量持久化到 Milvus，可扩展、能上生产。
 *   - ★ 关键：上层代码几乎一样——都是 embeddingModel.embed(...) + store.add(...) + store.search(...)，
 *     只是把 new InMemoryEmbeddingStore<>() 换成 MilvusEmbeddingStore.builder()...build()。
 *     这体现 LangChain4j 的 EmbeddingStore 接口抽象：底层向量库可插拔。
 *
 * 【运行前提】先启动 Milvus：vector-db-learning/01-milvus/docker-compose.yml。
 * ============================================================================
 */
@SpringBootApplication
public class MilvusStoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(MilvusStoreApplication.class, args);
    }
}
