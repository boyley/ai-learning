package com.example.lc4j.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 18：Spring Boot 集成（官方 starter + @AiService）—— 启动类
 * ============================================================================
 *
 * 【本模块和前面 01~17 的根本区别】
 *   - 前面模块：Spring Boot 只当"运行外壳"，模型全靠我们手写 OpenAiChatModel.builder()
 *     并用 @Value 读取自定义属性来构建。AiServices 也是手动 AiServices.create(...) 创建。
 *   - 本模块：真正接入 LangChain4j 官方的 Spring Boot starter，享受【自动配置】：
 *       1) langchain4j-open-ai-spring-boot-starter 读取 application.yml 的
 *          langchain4j.open-ai.chat-model.* 属性，自动创建好一个 OpenAiChatModel Bean；
 *       2) langchain4j-spring-boot-starter 扫描所有带 @AiService 注解的接口，
 *          自动把它实现成 Spring Bean，并（默认 AUTOMATIC 模式）自动注入上面那个模型。
 *     于是我们【一行模型构建代码都不用写】，直接 @Autowired 注入接口就能用。
 *
 * 【自动配置链路】
 *   application.yml(langchain4j.open-ai.chat-model.*)
 *        └─► OpenAI starter 的 AutoConfig ─► 生成 OpenAiChatModel Bean
 *                                               └─► @AiService 接口自动装配该模型 ─► 成为可注入的 Bean
 *
 * 【达到的目的】
 *   体会"约定优于配置"：把 LangChain4j 真正融入 Spring 生态，用最少的样板代码构建 AI 功能。
 *   这也是本学习项目的最后一站。
 * ============================================================================
 */
@SpringBootApplication
public class SpringBootIntegrationApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootIntegrationApplication.class, args);
    }
}
