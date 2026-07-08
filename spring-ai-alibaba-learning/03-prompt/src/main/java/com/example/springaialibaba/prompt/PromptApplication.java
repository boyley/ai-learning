package com.example.springaialibaba.prompt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 03：Prompt 提示词 —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   讲清“怎么把话说给大模型听”，也就是 Prompt(提示词) 的两大要点：
 *     1. 角色(Role)：一次对话可以有不同角色的消息——
 *          - System(系统)：设定 AI 的“人设/规则”，如“你是一位严谨的中医”。
 *          - User(用户)：用户真正问的问题。
 *        System 决定 AI 的“说话风格与身份”，User 决定“具体问什么”。
 *     2. PromptTemplate(提示词模板)：把提示词写成带占位符 {name} 的模板，
 *        运行时用真实值填充，避免手工拼接字符串，方便复用。
 *
 * 【怎么做】
 *   @SpringBootApplication 启动容器；容器就绪后自动执行 PromptRunner 演示。
 * ============================================================================
 */
@SpringBootApplication
public class PromptApplication {

    public static void main(String[] args) {
        SpringApplication.run(PromptApplication.class, args);
    }
}
