package com.example.springaialibaba.rag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 11：检索增强生成 RAG —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   大模型只“背”过公开知识，对你公司的【内部资料】一无所知（比如今年放假怎么排、
 *   报销走什么流程）。RAG 让它先去你的知识库里【检索】相关资料，再【带着资料回答】，
 *   这样它就能答对原本不知道的问题了。
 *
 * 【RAG 三个字母】
 *   R = Retrieval  检索：把问题拿去向量库里找最相关的几段资料
 *   A = Augmented  增强：把找到的资料塞进提示词，作为“参考背景”
 *   G = Generation 生成：大模型基于这些资料生成答案
 *
 * 【需要先懂的几个概念】
 *   1. 知识库：这里用模块10 的 SimpleVectorStore，存几条“公司内部知识”。
 *   2. QuestionAnswerAdvisor：Spring AI 内置的 RAG 顾问(Advisor)。挂到 ChatClient 上后，
 *      它会在每次提问前【自动检索向量库 + 把结果拼进提示词】，你几乎不用自己写检索代码。
 *   3. 对比实验：同一个问题，【不挂】advisor 时 AI 答不上来；【挂上】后就能答对。
 *
 * 【怎么做】
 *   容器启动 → 自动配置 ChatClient.Builder + EmbeddingModel →
 *   RagRunner 里建向量库存知识 → 建挂了 QuestionAnswerAdvisor 的 ChatClient →
 *   同一问题问两次做对比。
 * ============================================================================
 */
@SpringBootApplication
public class RagApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagApplication.class, args);
    }
}
