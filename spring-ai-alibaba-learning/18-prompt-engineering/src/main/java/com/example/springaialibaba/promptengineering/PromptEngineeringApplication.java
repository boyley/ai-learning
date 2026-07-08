package com.example.springaialibaba.promptengineering;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 18：提示工程模式(Prompt Engineering) —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   同一个模型，问法不同、效果天差地别。本模块用【纯 ChatClient】演示四种最常用的提示模式，
 *   并打印对比效果，帮你建立“怎么把话说好，模型才答得好”的直觉。
 *
 * 【四大提示模式（零基础必读）】
 *   1. 零样本 Zero-shot：直接下指令，不给任何例子。适合简单/模型已熟悉的任务。
 *   2. 少样本 Few-shot ：在提示里给几个“输入→输出”示例，让模型照葫芦画瓢。
 *                       适合有固定格式/风格要求、且难以用语言描述清楚的任务。
 *   3. 思维链 CoT      ：让模型“一步步思考”再给结论。显著提升数学/逻辑/多步推理的正确率。
 *   4. 角色扮演 Role   ：用 system 给模型设定一个专家身份/口吻。影响回答的视角、专业度与风格。
 *
 * 【与 Spring AI 的关系】
 *   这些模式不是新 API，就是用 ChatClient 的 system()/user() 传入不同文本而已：
 *     - system(...) 设定“身份/规则/示例”，全局生效、权重高；
 *     - user(...)   放“本次具体问题”。
 *   学会“把提示写好”，往往比换更贵的模型更划算。
 * ============================================================================
 */
@SpringBootApplication
public class PromptEngineeringApplication {

    public static void main(String[] args) {
        // 启动 Spring Boot：自动配置 ChatClient.Builder，供下面四种提示模式演示使用。
        SpringApplication.run(PromptEngineeringApplication.class, args);
    }
}
