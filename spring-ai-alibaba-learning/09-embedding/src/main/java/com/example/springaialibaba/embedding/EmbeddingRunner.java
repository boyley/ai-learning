package com.example.springaialibaba.embedding;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * ============================================================================
 * 文本向量化演示：把文字变成 float[]，并用余弦相似度比较语义远近
 * ============================================================================
 *
 * 【核心流程图】
 *
 *    "你好"  ──►  EmbeddingModel.embed(...)  ──►  [0.01, -0.7, 0.33, ... ] (1024 个数)
 *                        │                                    │
 *                   text-embedding-v3                    这就是“语义坐标”
 *
 *    比相似度：
 *        句A ─► 向量A ┐
 *                     ├─► 余弦相似度 cos(A,B)  ── 越接近 1，两句越相近
 *        句B ─► 向量B ┘
 *
 * 【关键 API 链路解读】
 *   embeddingModel.embed("文本")   -> 返回 float[]（该文本的向量）
 *   embeddingModel.dimensions()    -> 返回向量维度（text-embedding-v3 = 1024）
 *
 * 【重点】EmbeddingModel 来自 org.springframework.ai.embedding.*（Spring AI 通用接口）。
 *   Spring AI Alibaba 只是把底层实现换成 DashScope 的 text-embedding-v3，写法不变。
 * ============================================================================
 */
@Component
public class EmbeddingRunner implements CommandLineRunner {

    /** 向量化模型：由 spring-ai-alibaba-starter-dashscope 自动配置（底层 text-embedding-v3）。 */
    private final EmbeddingModel embeddingModel;

    // 构造器注入（Spring 推荐方式）。容器里只有一个 EmbeddingModel（文本版），不会有歧义。
    public EmbeddingRunner(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块09：文本向量化 Embedding ==========\n");

        // -------- 第 1 步：把一句话变成向量，观察它长什么样 --------
        String text = "你好";
        float[] vector = embeddingModel.embed(text);   // ★核心：文本 → float[]

        System.out.println("【1) 文本变向量】");
        System.out.println("  原文：" + text);
        System.out.println("  向量维度(dimensions)：" + vector.length + "（text-embedding-v3 应为 1024）");
        // 向量有 1024 个数，太长了，只打印前 8 维“尝个鲜”
        float[] preview = Arrays.copyOf(vector, 8);
        System.out.println("  前 8 维：" + Arrays.toString(preview) + " ...(后面还有 " + (vector.length - 8) + " 维)");

        // -------- 第 2 步：用“余弦相似度”比较句子语义的远近 --------
        // 直觉：句1 和 句2 都在讲“猫”，应该很像；句3 在讲“天气”，应该和前两句差得远。
        String s1 = "小猫喜欢吃鱼";
        String s2 = "猫咪爱吃小鱼干";
        String s3 = "今天北京的天气很晴朗";

        float[] v1 = embeddingModel.embed(s1);
        float[] v2 = embeddingModel.embed(s2);
        float[] v3 = embeddingModel.embed(s3);

        System.out.println("\n【2) 语义相似度对比（余弦相似度，越接近 1 越相近）】");
        System.out.printf("  句1：%s%n", s1);
        System.out.printf("  句2：%s%n", s2);
        System.out.printf("  句3：%s%n", s3);
        System.out.printf("  相似度(句1, 句2 都在讲猫)   = %.4f%n", cosineSimilarity(v1, v2));
        System.out.printf("  相似度(句1, 句3 猫 vs 天气) = %.4f%n", cosineSimilarity(v1, v3));

        System.out.println("\n【结论】讲同一件事的两句话相似度更高；话题不同的两句话相似度明显更低。");
        System.out.println("        “按语义找相近内容”正是向量数据库(模块10)与 RAG(模块11)的核心原理。");
        System.out.println("\n========== 演示结束：你已理解“文本→向量→比相似度” ==========\n");
    }

    /**
     * 计算两个向量的【余弦相似度】= (A·B) / (|A| × |B|)。
     * A·B 是点积；|A| 是向量长度(模)。结果范围 -1~1，越大越相近。
     */
    private static double cosineSimilarity(float[] a, float[] b) {
        double dot = 0.0;   // 点积 A·B
        double normA = 0.0; // |A|^2
        double normB = 0.0; // |B|^2
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        // 兜底：任一向量为零向量时，分母为 0，直接返回 0，避免除零得到 NaN
        if (normA == 0 || normB == 0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
