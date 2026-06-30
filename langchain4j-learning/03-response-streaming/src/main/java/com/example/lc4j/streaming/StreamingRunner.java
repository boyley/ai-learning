package com.example.lc4j.streaming;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * ============================================================================
 * 流式输出演示：打字机效果
 * ============================================================================
 *
 * 【流式 vs 非流式 流程图】
 *
 *   非流式 chat():
 *     提问 ──► 模型 ──(等全部生成完)──► 一次性返回整段文字
 *
 *   流式 chat(question, handler):
 *     提问 ──► 模型 ──► "你" ─► "好" ─► "，" ─► "我" ─► ...   每来一段回调一次 onPartialResponse
 *                                                          全部完成回调 onCompleteResponse
 *
 * 【为什么需要 CountDownLatch】
 *   流式调用 chat(...) 会立刻返回、在后台线程异步推送结果。
 *   若主线程不等待就结束，程序会在回答还没打印完时就退出。
 *   所以用 CountDownLatch 等到 onCompleteResponse / onError 被调用后再继续。
 * ============================================================================
 */
@Component
public class StreamingRunner implements CommandLineRunner {

    @Value("${langchain4j.openai.chat.base-url}")
    private String baseUrl;
    @Value("${langchain4j.openai.chat.api-key}")
    private String apiKey;
    @Value("${langchain4j.openai.chat.model}")
    private String modelName;

    @Override
    public void run(String... args) throws InterruptedException {
        System.out.println("\n========== 模块03：流式输出（打字机效果）==========\n");

        // ★ 构建“流式对话模型” StreamingChatModel（注意是 OpenAiStreamingChatModel）★
        StreamingChatModel model = OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .build();

        // CountDownLatch(1)：一个简单的“一次性闸门”，count=1。
        // 等到 onCompleteResponse 或 onError 调用 countDown() 后，await() 才会放行。
        CountDownLatch latch = new CountDownLatch(1);

        String question = "请分三点说明学习编程的好处。";   // 先存成变量，便于打印
        System.out.println("我问：" + question);            // ★ 先打印问题，让你看清问的是什么
        System.out.print("AI ：");

        // ★★★ 核心：chat(问题, 回调处理器) ★★★
        // 模型每生成一小段就回调一次，我们边收边打印，形成打字机效果。
        model.chat(question, new StreamingChatResponseHandler() {
            /** 收到一小段文本：立刻打印（不换行，拼成完整回答）。 */
            @Override
            public void onPartialResponse(String partialResponse) {
                System.out.print(partialResponse);
            }

            /** 全部生成完成：拿到完整响应，打印元信息并放行主线程。 */
            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                System.out.println(); // 换行
                System.out.println("（流式结束，结束原因：" + completeResponse.finishReason() + "）");
                latch.countDown(); // 闸门 -1 -> 归零 -> await() 放行
            }

            /** 出错：打印错误并放行，避免主线程一直等。 */
            @Override
            public void onError(Throwable error) {
                System.err.println("\n流式出错：" + error.getMessage());
                latch.countDown();
            }
        });

        // 主线程在此等待，直到上面回调里 countDown()（最多等 60 秒，防止卡死）。
        latch.await(60, TimeUnit.SECONDS);
        System.out.println("\n========== 模块03 演示结束 ==========\n");
    }
}
