package com.example.lc4j.streaming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 03：流式输出（Response Streaming）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   演示“流式输出”：让模型像打字机一样，一个字一个字地实时返回回答，
 *   而不是等全部生成完才一次性返回。聊天界面的“逐字显示”就是这么实现的。
 *
 * 【需要先懂的概念】
 *   1. StreamingChatModel：LangChain4j 的“流式对话模型”接口（区别于阻塞式 ChatModel）。
 *   2. StreamingChatResponseHandler：回调处理器。模型每吐出一小段就回调一次，
 *      关键回调：
 *        - onPartialResponse(String)：收到一小段文本（最常用，边到边打印）。
 *        - onCompleteResponse(ChatResponse)：全部完成，给出完整响应。
 *        - onError(Throwable)：出错时回调。
 *   3. 异步特性：流式是异步的，主线程不能直接退出，需用 CountDownLatch 等到完成。
 *
 * 【达到的目的】
 *   掌握流式调用的写法与回调时机，理解流式 vs 非流式的差异与适用场景。
 * ============================================================================
 */
@SpringBootApplication
public class StreamingApplication {
    public static void main(String[] args) {
        SpringApplication.run(StreamingApplication.class, args);
    }
}
