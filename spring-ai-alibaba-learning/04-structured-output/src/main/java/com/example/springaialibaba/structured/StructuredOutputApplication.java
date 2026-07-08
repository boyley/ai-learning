package com.example.springaialibaba.structured;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 04：结构化输出 —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   大模型默认返回的是“一段自然语言文字(String)”，但程序更想要“能直接用的 Java 对象”。
 *   本模块演示 Spring AI 的“结构化输出”能力：一行 .entity(...) 就把回答自动转成
 *     - 一个 Java 对象（record Book）
 *     - 一个对象列表（List<Book>）
 *
 * 【它的原理（一句话）】
 *   ChatClient 会在后台自动往提示词里追加“请按这个 JSON 格式回答”的说明，
 *   模型返回 JSON 后，再用转换器把 JSON 反序列化成你要的 Java 类型。你只管声明类型即可。
 *
 * 【怎么做】
 *   @SpringBootApplication 启动容器；容器就绪后自动执行 StructuredOutputRunner 演示。
 * ============================================================================
 */
@SpringBootApplication
public class StructuredOutputApplication {

    public static void main(String[] args) {
        SpringApplication.run(StructuredOutputApplication.class, args);
    }
}
