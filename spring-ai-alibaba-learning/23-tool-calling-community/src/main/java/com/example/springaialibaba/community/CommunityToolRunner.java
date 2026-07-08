package com.example.springaialibaba.community;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * 模块 23：社区工具集演示（一行依赖接入百度搜索）
 * ============================================================================
 *
 * 【思路】
 *   引入 baidusearch starter 后，框架会自动注册“百度搜索”工具的 Bean。
 *   我们不知道（也不必关心）它的确切类名，只需按 Spring AI 的统一抽象把它收集起来：
 *     - ToolCallbackProvider：一个“工具提供者”，能吐出一批 ToolCallback；
 *     - ToolCallback：一个具体的“可被模型调用的工具”。
 *   把收集到的工具交给 ChatClient 的 .toolCallbacks(...)，模型就能在需要时联网搜索。
 *
 * 【稳健性设计（关键）】
 *   用 ObjectProvider 做“可选注入”：容器里有就拿来用，没有也不报错。
 *   若一个工具都没收集到（比如版本差异/缺 Key），就打印提示并退回“不带工具直接问”，
 *   保证本模块在**任何环境都能编译、启动、跑完**，不会崩溃。
 * ============================================================================
 */
@Component
public class CommunityToolRunner implements CommandLineRunner {

    private final ChatClient chatClient;

    /** 容器里所有的“工具提供者”（百度搜索 starter 通常会注册一个） */
    private final ObjectProvider<ToolCallbackProvider> toolCallbackProviders;
    /** 容器里所有独立注册的“工具”Bean（有的 starter 直接注册 ToolCallback） */
    private final ObjectProvider<ToolCallback> toolCallbacks;

    public CommunityToolRunner(ChatClient.Builder chatClientBuilder,
                               ObjectProvider<ToolCallbackProvider> toolCallbackProviders,
                               ObjectProvider<ToolCallback> toolCallbacks) {
        this.chatClient = chatClientBuilder.build();
        this.toolCallbackProviders = toolCallbackProviders;
        this.toolCallbacks = toolCallbacks;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块23：社区工具集(一行接入百度搜索) ==========\n");

        // 1) 收集容器里自动注册的所有工具（两种来源都兜底收集）
        List<ToolCallback> tools = new ArrayList<>();
        toolCallbackProviders.forEach(provider -> {
            ToolCallback[] cbs = provider.getToolCallbacks();
            if (cbs != null) {
                for (ToolCallback cb : cbs) {
                    tools.add(cb);
                }
            }
        });
        toolCallbacks.forEach(tools::add);

        System.out.println("【自动发现的社区工具数量】" + tools.size());
        for (ToolCallback cb : tools) {
            // getToolDefinition().name() 是 Spring AI 里工具的统一元信息
            System.out.println("  - 工具：" + cb.getToolDefinition().name());
        }

        String question = "请帮我用百度搜索一下：Spring AI Alibaba 最新版本有哪些新特性？用中文简要总结。";
        System.out.println("\n【提问】" + question + "\n");

        if (tools.isEmpty()) {
            // 兜底分支：没收集到工具（可能是版本差异或未配 Key），不带工具直接问，保证能跑完
            System.out.println("【提示】未发现自动注册的社区工具，可能是版本差异或未配置搜索 Key。");
            System.out.println("        本次退回“不联网直接回答”（内容可能不够新）。接入方法见 README。\n");
            String answer = chatClient.prompt().user(question).call().content();
            System.out.println("【AI 答(无联网)】" + answer);
        } else {
            // 正常分支：把百度搜索工具交给 ChatClient，模型会自动联网搜索再回答
            String answer = chatClient.prompt()
                    .user(question)
                    .toolCallbacks(tools)   // ★ 一行把社区工具挂上去
                    .call()
                    .content();
            System.out.println("【AI 答(已联网搜索)】" + answer);
        }

        System.out.println("\n========== 演示结束：社区工具集=站在生态肩膀上！ ==========\n");
    }
}
