package com.example.springaialibaba.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ============================================================================
 * RAG 演示：同一个问题，“不挂知识库” vs “挂上知识库”，答案天差地别
 * ============================================================================
 *
 * 【核心流程图】
 *
 *   ┌── 不挂 advisor ──┐   问题 ─► ChatClient ─► 通义千问 ─► “我不知道贵公司的安排”
 *   │                  │
 *   └── 挂 advisor ────┘   问题 ─► QuestionAnswerAdvisor
 *                                   │ 1) 去向量库检索最相关的几条“公司知识”
 *                                   │ 2) 把检索到的资料拼进提示词
 *                                   ▼
 *                               ChatClient ─► 通义千问 ─► 基于资料答对
 *
 * 【关键 API 链路解读】
 *   SimpleVectorStore.builder(embeddingModel).build()      -> 建知识库
 *   store.add(List<Document>)                              -> 存入“公司内部知识”
 *   QuestionAnswerAdvisor.builder(vectorStore).build()     -> 创建 RAG 顾问
 *   chatClient.prompt().advisors(qaAdvisor).user(问题)...   -> 提问时挂上顾问，自动检索+增强
 *
 * 【重点】QuestionAnswerAdvisor 来自 org.springframework.ai.chat.client.advisor.vectorstore.*，
 *   由额外依赖 spring-ai-advisors-vector-store 提供。
 * ============================================================================
 */
@Component
public class RagRunner implements CommandLineRunner {

    private final ChatClient chatClient;      // 对话客户端（通义千问）
    private final EmbeddingModel embeddingModel; // 向量化模型，用于给知识库文档编码

    public RagRunner(ChatClient.Builder chatClientBuilder, EmbeddingModel embeddingModel) {
        this.chatClient = chatClientBuilder.build();
        this.embeddingModel = embeddingModel;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块11：检索增强生成 RAG ==========\n");

        // -------- 第 1 步：建“公司内部知识库”，存几条只有本公司才知道的信息 --------
        VectorStore knowledgeBase = SimpleVectorStore.builder(embeddingModel).build();
        knowledgeBase.add(List.of(
                new Document("【放假安排】2026 年春节公司放假时间为 2 月 14 日至 2 月 22 日，共 9 天，2 月 23 日正常上班。"),
                new Document("【报销流程】员工报销需在钉钉提交发票照片，由直属主管审批后，财务每周五统一打款。"),
                new Document("【网络密码】公司访客 WiFi 名称为 Guest-AI，密码为 welcome2026。"),
                new Document("【工位调整】研发部已于本月搬迁至 B 座 8 层，前台在 B 座 1 层。")
        ));
        System.out.println("【1) 已建立公司知识库，存入 4 条内部信息（放假/报销/WiFi/工位）】");

        // 我们要问的问题——这是“公司私有信息”，大模型自己绝对不可能知道。
        String question = "公司 2026 年春节放几天假？几号开始上班？";

        // -------- 第 2 步：对照组——不挂知识库，直接问大模型 --------
        System.out.println("\n【2) 不挂知识库直接问】问题：" + question);
        String answerWithout = chatClient.prompt()
                .user(question)
                .call()
                .content();
        System.out.println("  AI 答：" + answerWithout);
        System.out.println("  （预期：AI 表示不清楚——因为它没见过你公司的内部安排）");

        // -------- 第 3 步：实验组——挂上 QuestionAnswerAdvisor 再问同一个问题 --------
        // advisor 会自动：先去 knowledgeBase 检索相关文档，再把文档塞进提示词，最后交给模型。
        QuestionAnswerAdvisor qaAdvisor = QuestionAnswerAdvisor.builder(knowledgeBase).build();

        System.out.println("\n【3) 挂上 RAG 顾问再问同一问题】问题：" + question);
        String answerWith = chatClient.prompt()
                .advisors(qaAdvisor)   // ★关键：挂上 RAG 顾问，自动“检索 + 增强”
                .user(question)
                .call()
                .content();
        System.out.println("  AI 答：" + answerWith);
        System.out.println("  （预期：AI 依据知识库答出“放 9 天，2 月 23 日上班”）");

        System.out.println("\n【结论】RAG = 先检索你的私有知识，再让模型带着资料回答。");
        System.out.println("        这样无需重新训练模型，就能让它“懂”你公司的专属知识。");
        System.out.println("\n========== 演示结束：你已理解 RAG 的检索-增强-生成 ==========\n");
    }
}
