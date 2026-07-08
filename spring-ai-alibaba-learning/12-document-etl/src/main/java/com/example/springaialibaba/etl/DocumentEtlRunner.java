package com.example.springaialibaba.etl;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ============================================================================
 * 文档 ETL 演示：company.txt → 读取 → 切分 → 入库 → 检索
 * ============================================================================
 *
 * 【核心流程图】
 *
 *   docs/company.txt
 *        │  (E) Extract 读取
 *        ▼
 *   TextReader.get()  ──► List<Document>（此时通常只有 1 大条 = 整篇）
 *        │  (T) Transform 切分
 *        ▼
 *   TokenTextSplitter.split(...) ──► List<Document>（切成 N 小块）
 *        │  (L) Load 入库
 *        ▼
 *   SimpleVectorStore.add(...)  ──► 每小块自动向量化并存入
 *        │
 *        ▼
 *   similaritySearch("问题")  ──► 召回最相关的那一小块
 *
 * 【关键 API 链路解读】
 *   new TextReader(resource).get()                 -> 读取文本，返回 List<Document>
 *   new TokenTextSplitter(...).split(docs)         -> 按 token 数把文档切成小块
 *   SimpleVectorStore.builder(embeddingModel)...   -> 建库
 *   store.add(chunks)                              -> 入库（自动向量化）
 *
 * 【重点】TextReader / TokenTextSplitter / SimpleVectorStore 均来自 org.springframework.ai.*。
 * ============================================================================
 */
@Component
public class DocumentEtlRunner implements CommandLineRunner {

    /** 向量化模型：由 starter 自动配置（text-embedding-v3）。 */
    private final EmbeddingModel embeddingModel;

    /**
     * classpath 下的待处理文档。
     * @Value("classpath:docs/company.txt") 会把 src/main/resources/docs/company.txt
     * 注入成一个 Spring 的 Resource 对象，交给 TextReader 读取。
     */
    @Value("classpath:docs/company.txt")
    private Resource companyDoc;

    public DocumentEtlRunner(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块12：文档 ETL 管道（读取→切分→入库→检索）==========\n");

        // -------- (E) Extract 读取：把整个 txt 文件读成 Document --------
        TextReader reader = new TextReader(companyDoc);
        List<Document> rawDocs = reader.get();
        System.out.println("【E) 读取 Extract】");
        System.out.println("  从 docs/company.txt 读出 " + rawDocs.size() + " 个原始 Document（通常是整篇 1 条）。");
        System.out.println("  整篇字符数：" + rawDocs.get(0).getText().length());

        // -------- (T) Transform 切分：把长文切成小块 --------
        // 用 builder 配置切分参数（跨版本稳定）：
        //   withChunkSize(每块目标 token 数)、withMinChunkSizeChars(每块最小字符数)、
        //   withMinChunkLengthToEmbed(短于此长度的块丢弃)、withMaxNumChunks(最多切几块)、
        //   withKeepSeparator(是否保留换行等分隔符)。
        // 这里故意把 chunkSize 调小(60)，好让这篇短文也能切出多块，便于观察效果。
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(60)
                .withMinChunkSizeChars(30)
                .withMinChunkLengthToEmbed(5)
                .withMaxNumChunks(1000)
                .withKeepSeparator(true)
                .build();
        List<Document> chunks = splitter.split(rawDocs);
        System.out.println("\n【T) 切分 Transform】");
        System.out.println("  切分后共得到 " + chunks.size() + " 个文本块（chunk）。逐块预览：");
        for (int i = 0; i < chunks.size(); i++) {
            String text = chunks.get(i).getText().replaceAll("\\s+", "");
            String preview = text.length() > 40 ? text.substring(0, 40) + "..." : text;
            System.out.printf("    块[%d]（约 %d 字）：%s%n", i + 1, text.length(), preview);
        }

        // -------- (L) Load 入库：把每个块向量化后写进向量库 --------
        SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();
        store.add(chunks);
        System.out.println("\n【L) 入库 Load】已把 " + chunks.size() + " 个块写入 SimpleVectorStore（自动向量化）。");

        // -------- 验证：拿一个问题去检索，看能否召回相关那一块 --------
        String question = "星辰科技有哪些主营业务？";
        List<Document> hits = store.similaritySearch(
                SearchRequest.builder().query(question).topK(2).build());
        System.out.println("\n【验证检索】问题：" + question);
        if (hits != null) {
            int i = 1;
            for (Document d : hits) {
                String text = d.getText().replaceAll("\\s+", "");
                String preview = text.length() > 50 ? text.substring(0, 50) + "..." : text;
                System.out.printf("    命中[%d]：%s%n", i++, preview);
            }
        }

        System.out.println("\n【结论】ETL 把“一个文件”自动变成了“一批可检索的知识块”。");
        System.out.println("        真实项目里把 TextReader 换成 PDF/Word 读取器即可处理各种文档。");
        System.out.println("\n========== 演示结束：你已跑通一条文档 ETL 管道 ==========\n");
    }
}
