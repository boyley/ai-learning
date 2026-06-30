package com.example.lc4j.tools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 08：Tools 工具 / 函数调用（Function Calling）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   大模型本身只会“生成文本”，它不会算数学、查不到今天的天气、也连不上你的数据库。
 *   “工具调用（Tool / Function Calling）”就是把你的【普通 Java 方法】注册给模型，
 *   让模型在需要时【主动请求调用】这些方法，框架执行后把结果回填，模型再据此作答。
 *
 * 【怎么做（核心三步）】
 *   1. 写一个普通类，给需要暴露的方法加 @Tool 注解（可选 @P 描述参数）。
 *      —— @Tool / @P 来自包 dev.langchain4j.agent.tool。
 *   2. 用 AiServices.builder(接口).chatModel(model).tools(工具对象).build() 装配助手。
 *   3. 正常提问。模型若判断需要工具，会自动“调用 → 拿结果 → 继续推理 → 给出最终回答”。
 *      这一整套“函数调用协议”的来回交互，全部由 LangChain4j 自动完成，你无需手写。
 *
 * 【为什么重要】
 *   工具调用是把 LLM 从“只会聊天”升级为“能办事的智能体（Agent）”的关键能力，
 *   后续的 16-mcp、17-agents 都建立在它之上。
 *
 * 【达到的目的】
 *   学会用 @Tool 给模型“装上手脚”，让它能调用计算、查询等真实功能。
 * ============================================================================
 */
@SpringBootApplication
public class ToolsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ToolsApplication.class, args);
    }
}
