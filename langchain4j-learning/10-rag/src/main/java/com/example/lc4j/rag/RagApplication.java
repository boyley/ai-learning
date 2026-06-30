package com.example.lc4j.rag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 10：RAG 检索增强生成（Retrieval-Augmented Generation）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   大模型只“知道”训练时见过的通用知识，它不了解你的私有资料（公司制度、产品手册……），
 *   还可能“一本正经地胡说八道”（幻觉）。RAG 的思路是：
 *     回答前，先从你的知识库里【检索】出最相关的几段资料，
 *     把它们【拼进提示词】一起喂给模型，让模型“看着资料回答”。
 *   = 检索（Retrieval）+ 增强（Augmented）+ 生成（Generation）。
 *
 * 【怎么做（在模块09 的基础上再进一步）】
 *   1. 把知识向量化后存入 InMemoryEmbeddingStore（同模块09）。
 *   2. 用 EmbeddingStoreContentRetriever.builder() 把“向量库 + 向量模型”包成一个【检索器】。
 *   3. AiServices.builder(接口).chatModel(model).contentRetriever(检索器).build()。
 *   4. 之后正常提问——框架会自动：先用问题检索相关片段 → 拼进提示词 → 调模型作答。
 *      这一切由 LangChain4j 自动完成，业务代码依然只是“调一个接口方法”。
 *
 * 【RAG 解决了什么】
 *   - 让模型能回答“它本不知道”的私有/最新知识；
 *   - 答案有据可依，显著减少幻觉。
 *
 * 【注意】
 *   向量化用 OpenAI（embedding.*），对话用 DeepSeek（chat.*）；
 *   真正运行需对应 Key 与网络，本模块只要求 `mvn compile` 通过。
 * ============================================================================
 */
@SpringBootApplication
public class RagApplication {
    public static void main(String[] args) {
        SpringApplication.run(RagApplication.class, args);
    }
}
