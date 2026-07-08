package com.example.springaialibaba.graphparallel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 20：Graph 并行节点 —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   在模块 19（串行流水线）的基础上，学习 Graph 的“并行(分叉-汇聚)”能力：
 *   同一份输入，让【情感分析】和【关键词提取】两个节点**同时**跑，
 *   再把两者的结果**汇聚**到一个【汇总】节点，生成一份综合报告。
 *
 * 【为什么要并行？（零基础理解）】
 *   如果两个步骤互不依赖（分析情感 ≠ 提关键词），串行执行=白白等待。
 *   让它们并行，总耗时≈两者中较慢的那个，而不是两者相加，**能显著省时间**。
 *   这在“同时调用多个模型/多个工具”的智能体里非常常见。
 *
 * 【并行怎么表达】
 *   在图里让 START 同时连到两个节点（两条出边），
 *   再让这两个节点都连到同一个汇聚节点：
 *       START ─┬─► 情感分析 ─┐
 *              └─► 关键词   ─┴─► 汇总 ─► END
 *   Graph 引擎会自动识别“这两个分支可以并发”，跑完后在汇聚点等齐再继续。
 * ============================================================================
 */
@SpringBootApplication
public class GraphParallelApplication {

    public static void main(String[] args) {
        SpringApplication.run(GraphParallelApplication.class, args);
    }
}
