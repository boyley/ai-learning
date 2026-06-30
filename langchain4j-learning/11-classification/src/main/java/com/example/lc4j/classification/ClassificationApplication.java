package com.example.lc4j.classification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 11：文本分类（Text Classification）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   演示如何用 LangChain4j 的 AiServices 做“文本分类”——把一段自然语言文本
 *   归到若干个预先定义好的类别里。最典型的例子就是“情感分析”：判断一条评论是
 *   正面（POSITIVE）、负面（NEGATIVE）还是中性（NEUTRAL）。
 *
 * 【核心思想：让接口方法返回 enum】
 *   在模块 04 里我们见过 AiServices 让接口方法返回 String。
 *   这里更进一步：当接口方法的【返回类型是一个枚举（enum）】时，LangChain4j 会：
 *     1. 自动把这个枚举的所有常量名（POSITIVE / NEGATIVE / NEUTRAL）写进提示词，
 *        告诉模型“只能从这些选项里选一个回答”；
 *     2. 把模型返回的文本解析成对应的枚举常量返回给你。
 *   于是“分类”这件事，就被简化成了“声明一个 enum + 一个返回该 enum 的接口方法”。
 *
 * 【为什么用 enum 而不是 String】
 *   - 类型安全：返回值只可能是有限几个取值，编译期就能用 switch 穷举处理。
 *   - 约束模型：枚举常量列表会进提示词，模型不会“自由发挥”出意外的类别。
 *
 * 【达到的目的】
 *   学会用“返回枚举”的方式做分类任务，这是结构化输出（模块07）的一个特例，
 *   也是真实业务里（工单分类、意图识别、内容审核打标）最常用的轻量做法。
 * ============================================================================
 */
@SpringBootApplication
public class ClassificationApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClassificationApplication.class, args);
    }
}
