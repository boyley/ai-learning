package com.example.springaialibaba.etl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 12：文档 ETL 管道 —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   模块 10/11 里我们手写了几条 Document。真实项目里，知识来自一个个【文件】
 *   （txt/pdf/word...）。本模块演示一条标准的“文档处理流水线”，把一个文本文件
 *   自动变成可检索的知识，喂给向量库。
 *
 * 【ETL 是什么（数据领域经典三步）】
 *   E = Extract   读取：从文件/网页等数据源把原始文本读出来（TextReader）
 *   T = Transform 切分：把长文本切成一小块一小块（TokenTextSplitter）
 *   L = Load      入库：把每一小块向量化后写进向量库（VectorStore.add）
 *
 * 【为什么要“切分”？】
 *   1. 大模型/向量模型对单次处理的长度有上限，太长塞不下；
 *   2. 检索时“小块”更精准——能只召回真正相关的那一段，而不是整篇文章。
 *
 * 【怎么做】
 *   容器启动 → 自动配置 EmbeddingModel → DocumentEtlRunner 里
 *   TextReader 读 classpath 下的 docs/company.txt → TokenTextSplitter 切块 →
 *   打印块数与内容 → 写入 SimpleVectorStore → 检索验证。
 * ============================================================================
 */
@SpringBootApplication
public class DocumentEtlApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentEtlApplication.class, args);
    }
}
