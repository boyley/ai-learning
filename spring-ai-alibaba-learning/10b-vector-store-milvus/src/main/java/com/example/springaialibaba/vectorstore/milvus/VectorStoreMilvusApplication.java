package com.example.springaialibaba.vectorstore.milvus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 10b：Spring AI Alibaba · VectorStore · Milvus 版 —— 启动类
 * ============================================================================
 *
 * 【要点】
 *   - Spring AI Alibaba 复用 Spring AI 的 VectorStore 抽象，所以直接用官方
 *     spring-ai-starter-vector-store-milvus 自动配置 MilvusVectorStore，写法与纯 Spring AI 完全一致。
 *   - 区别只在 embedding 走 DashScope（通义 text-embedding-v3），★维度是 1024（不是 OpenAI 的 1536）。
 *   - 对比模块 10（内存版 SimpleVectorStore）：业务代码不变，换依赖 + 配置即从内存换成生产级 Milvus。
 *
 * 【运行前提】先启动 Milvus：vector-db-learning/01-milvus/docker-compose.yml。
 * ============================================================================
 */
@SpringBootApplication
public class VectorStoreMilvusApplication {
    public static void main(String[] args) {
        SpringApplication.run(VectorStoreMilvusApplication.class, args);
    }
}
