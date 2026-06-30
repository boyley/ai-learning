package com.example.lc4j.springboot;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * Spring Boot 集成演示：直接 @Autowired 注入 @AiService 接口即可使用
 * ============================================================================
 *
 * 【流程图】
 *
 *   application.yml(langchain4j.open-ai.chat-model.*)
 *          │  OpenAI starter 自动配置
 *          ▼
 *   容器中自动出现 OpenAiChatModel Bean
 *          │  langchain4j-spring-boot-starter 扫描 @AiService
 *          ▼
 *   Assistant 接口被自动实现 + 注册为 Bean（自动装配上面的模型）
 *          │  Spring 构造器注入
 *          ▼
 *   本 Runner 拿到 assistant，直接调用 —— 全程零样板模型构建代码
 * ============================================================================
 */
@Component
public class SpringBootRunner implements CommandLineRunner {

    /**
     * 直接注入 @AiService 接口。
     * 这个 Bean 由 langchain4j-spring-boot-starter 自动创建并注入了自动配置的 ChatModel，
     * 我们这边一行模型构建代码都没写。
     */
    private final Assistant assistant;

    // 构造器注入（Spring 推荐方式）：容器启动时把自动生成的 Assistant Bean 传进来。
    public SpringBootRunner(Assistant assistant) {
        this.assistant = assistant;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块18：Spring Boot 集成（starter + @AiService）==========\n");

        System.out.println("说明：本模块的 ChatModel 与 Assistant 实现均由官方 starter 自动配置，");
        System.out.println("     业务代码里没有任何 OpenAiChatModel.builder() 或 AiServices.create(...)。\n");

        // 像调用普通 Bean 的方法一样使用它——背后由 starter 自动装配的模型完成对话。
        System.out.println("===== 直接使用自动注入的 Assistant Bean =====");
        String answer = assistant.chat("用一句话介绍 Spring Boot 的'自动配置'是什么。");
        System.out.println("AI：" + answer);

        System.out.println("\n========== 模块18 演示结束 ==========\n");
    }
}
