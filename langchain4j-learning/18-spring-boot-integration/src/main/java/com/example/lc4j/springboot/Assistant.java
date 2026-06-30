package com.example.lc4j.springboot;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;

/**
 * ============================================================================
 * @AiService 声明式 AI Service —— Spring Bean 化的助手接口
 * ============================================================================
 *
 * 【关键点】★本模块的核心★
 *   - @dev.langchain4j.service.spring.AiService 是官方 starter 提供的注解。
 *     被它标注的接口，会在启动时由 langchain4j-spring-boot-starter 自动：
 *       (1) 用 AiServices 生成实现；
 *       (2) 注册成一个 Spring Bean。
 *   - 它的 wiringMode 默认是 AUTOMATIC：自动从 Spring 容器里找到唯一的 ChatModel Bean
 *     （即 OpenAI starter 根据 application.yml 自动创建的那个）并装配进来。
 *     所以这里【不需要】写 chatModel = "xxx"，也【不需要】手动 AiServices.create(...)。
 *   - 对比模块 04 的 Assistant：那里要手写 AiServices.create(接口, model)；
 *     这里只要一个注解，剩下的交给 Spring + starter 自动完成。
 * ============================================================================
 */
@AiService   // ← 仅此一个注解：自动实现 + 自动注册为 Bean + 自动注入模型
public interface Assistant {

    @SystemMessage("你是一个简洁的中文助手，回答尽量控制在两句话以内。")
    String chat(String userMessage);
}
