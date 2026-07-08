package com.example.springaialibaba.advisors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * 模块 07 演示：Advisor 顾问/拦截器
 * ============================================================================
 *
 * 【两个演示】
 *   演示1：内置 SimpleLoggerAdvisor —— 自动把每次请求/响应按 DEBUG 日志打印出来。
 *          （需在 application.yml 把 advisor 包日志级别调到 DEBUG，才能看到打印。）
 *   演示2：自定义 TimingLoggerAdvisor —— 我们自己实现的 CallAdvisor，
 *          在每次调用前后打印时间戳并统计耗时。
 *
 * 【怎么挂 Advisor】
 *   - 默认挂（对该 ChatClient 的所有调用生效）：builder.defaultAdvisors(...).build()
 *   - 单次挂（只对这一次调用生效）：           .advisors(...) 挂在 prompt() 链上
 *   本模块用 defaultAdvisors 一次性把两个 Advisor 都挂上。
 * ============================================================================
 */
@Component
public class AdvisorsRunner implements CommandLineRunner {

    private final ChatClient chatClient;

    public AdvisorsRunner(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                // 同时挂两个 Advisor：
                //   1) 内置日志顾问：按 DEBUG 打印请求/响应全文
                //   2) 我们自定义的耗时顾问：打印时间戳 + 耗时
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        new TimingLoggerAdvisor())
                .build();
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块07：Advisor 顾问/拦截器 ==========\n");

        System.out.println("已挂载的 Advisor：");
        System.out.println("  1) SimpleLoggerAdvisor（内置）—— 按 DEBUG 日志打印请求/响应");
        System.out.println("  2) TimingLoggerAdvisor（自定义）—— 打印时间戳并统计耗时\n");

        String question = "用一句话说明什么是“拦截器”。";
        System.out.println("【我问】" + question);
        System.out.println("（注意观察下方由自定义 Advisor 打印的“请求发出/收到响应/耗时”）\n");

        // 这一次普通调用，会依次穿过上面两个 Advisor 的“前置 → 模型 → 后置”
        String answer = chatClient
                .prompt()
                .user(question)
                .call()
                .content();

        System.out.println("\n【AI 答】" + answer + "\n");

        System.out.println("提示：SimpleLoggerAdvisor 的请求/响应明细以 DEBUG 日志形式输出，");
        System.out.println("     已在 application.yml 把 org.springframework.ai.chat.client.advisor 设为 DEBUG。\n");

        System.out.println("========== 演示结束：你已理解 Advisor 的洋葱式拦截 ==========\n");
    }
}
