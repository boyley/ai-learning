package com.example.springaialibaba.embedding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 09：文本向量化 Embedding —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   把一句“人话”（文本）交给模型，换回一串数字（向量 float[]）。
 *   这串数字就是这句话在“语义空间”里的坐标——含义相近的句子，坐标也相近。
 *   它是后面【向量数据库(10)】【检索增强 RAG(11)】【文档 ETL(12)】的地基。
 *
 * 【需要先懂的几个概念（零基础必读）】
 *   1. 向量(Vector)：就是一组小数，比如 [0.12, -0.03, 0.88, ...]。
 *   2. Embedding(向量化/嵌入)：把文本 → 向量的过程。
 *   3. 维度(Dimension)：向量里有多少个数字。text-embedding-v3 输出 1024 维。
 *   4. 余弦相似度(Cosine Similarity)：衡量两个向量“方向有多接近”的指标，
 *      取值 -1~1，越接近 1 表示两句话语义越相近。
 *   5. EmbeddingModel：Spring AI 定义的“向量化模型”统一接口，
 *      底层由 DashScope 的 text-embedding-v3 实现（starter 自动配置好）。
 *
 * 【怎么做】
 *   @SpringBootApplication 启动容器 → 自动配置出 EmbeddingModel Bean →
 *   容器就绪后执行 EmbeddingRunner，演示“文本变向量”和“比相似度”。
 *
 * 【达到的目的】
 *   看懂：文字是怎么变成数字的、为什么能用数字比较语义远近。
 * ============================================================================
 */
@SpringBootApplication
public class EmbeddingApplication {

    public static void main(String[] args) {
        // 启动 Spring Boot：创建容器 → 自动配置 EmbeddingModel → 执行 CommandLineRunner
        SpringApplication.run(EmbeddingApplication.class, args);
    }
}
