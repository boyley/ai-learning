package com.example.springaialibaba.vectorstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 10：向量数据库（内存版 SimpleVectorStore）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   在模块 09 里我们学会了“文本→向量”。本模块把很多条文本(Document)一次性
 *   向量化并【存起来】，之后给一个问题，就能【按语义相近】把最相关的几条捞出来。
 *   这个“存 + 按相近检索”的组件，就叫【向量数据库 / 向量存储 VectorStore】。
 *
 * 【需要先懂的几个概念】
 *   1. Document(文档)：向量库里的一条数据 = 一段文本 + 可选的元数据(metadata)。
 *   2. VectorStore(向量库)：存 Document 并支持“相似度检索”的组件。
 *   3. SimpleVectorStore：Spring AI 自带的【内存版】向量库，
 *      数据存在内存里、进程退出就没了，最适合学习/小demo（生产可换 Redis/PGVector 等）。
 *   4. similaritySearch(相似度检索)：给一句 query，返回语义最相近的前 K 条 Document。
 *
 * 【怎么做】
 *   容器启动 → 自动配置 EmbeddingModel → VectorStoreRunner 里
 *   用 EmbeddingModel 建一个 SimpleVectorStore → 存文档 → 检索 → 打印。
 * ============================================================================
 */
@SpringBootApplication
public class VectorStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(VectorStoreApplication.class, args);
    }
}
