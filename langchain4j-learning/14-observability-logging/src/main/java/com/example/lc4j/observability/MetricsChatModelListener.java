package com.example.lc4j.observability;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;

/**
 * ============================================================================
 * 自定义模型监听器：统计每次 LLM 调用的耗时与 Token 用量
 * ============================================================================
 *
 * 【它是什么】
 *   实现 LangChain4j 的 dev.langchain4j.model.chat.listener.ChatModelListener 接口。
 *   该接口有三个【default 方法】（所以你只需重写关心的那几个）：
 *     - onRequest(ChatModelRequestContext)   ：发起请求前触发
 *     - onResponse(ChatModelResponseContext) ：收到响应后触发
 *     - onError(ChatModelErrorContext)       ：调用出错时触发
 *
 * 【耗时怎么算】
 *   onRequest 与 onResponse 三个 Context 都暴露了一个 attributes() 方法，
 *   返回【同一次调用内共享】的 Map<Object,Object>。
 *   我们在 onRequest 里把开始时间戳塞进这个 Map，
 *   到 onResponse 里再取出来，相减即得本次调用耗时。这是官方推荐的传值方式。
 * ============================================================================
 */
public class MetricsChatModelListener implements ChatModelListener {

    /** 在共享 attributes Map 中存放“开始时间”用的 key（用常量避免拼写错） */
    private static final String START_TIME = "start_time_nanos";

    /**
     * 请求发起前回调。
     * ctx.chatRequest()    -> 即将发送的 ChatRequest（含 messages 等）
     * ctx.modelProvider()  -> 模型厂商枚举（如 OPEN_AI）
     * ctx.attributes()     -> 本次调用的共享 Map，用于把数据传给 onResponse
     */
    @Override
    public void onRequest(ChatModelRequestContext ctx) {
        // 把当前纳秒时间戳放进共享 Map，供 onResponse 计算耗时
        ctx.attributes().put(START_TIME, System.nanoTime());
        // 取出请求里的消息条数，作为一个简单的可观测指标打印出来
        int messageCount = ctx.chatRequest().messages().size();
        System.out.println("[监听器·onRequest] 即将调用模型，厂商=" + ctx.modelProvider()
                + "，消息条数=" + messageCount);
    }

    /**
     * 收到响应后回调。
     * ctx.chatResponse() -> 完整的 ChatResponse（可取 tokenUsage / finishReason / aiMessage）
     * ctx.attributes()   -> 与 onRequest 同一个 Map，可取回开始时间
     */
    @Override
    public void onResponse(ChatModelResponseContext ctx) {
        // 从共享 Map 取回开始时间，计算本次调用耗时（毫秒）
        Object start = ctx.attributes().get(START_TIME);
        long costMs = (start == null) ? -1
                : (System.nanoTime() - (Long) start) / 1_000_000;

        ChatResponse response = ctx.chatResponse();
        // tokenUsage()：本次调用的 Token 用量（输入/输出/总计），可能为 null（视厂商而定）
        TokenUsage usage = response.tokenUsage();

        System.out.println("[监听器·onResponse] 调用完成，耗时=" + costMs + " ms");
        if (usage != null) {
            System.out.println("[监听器·onResponse] Token 用量 -> 输入:" + usage.inputTokenCount()
                    + " 输出:" + usage.outputTokenCount()
                    + " 总计:" + usage.totalTokenCount());
        }
        // finishReason()：结束原因（STOP 正常结束 / LENGTH 触顶 等）
        System.out.println("[监听器·onResponse] 结束原因=" + response.finishReason());
    }

    /**
     * 调用出错时回调（网络异常、鉴权失败、限流等）。
     * ctx.error() -> 抛出的 Throwable，可记录/上报告警
     */
    @Override
    public void onError(ChatModelErrorContext ctx) {
        // error()：本次调用抛出的异常对象
        System.out.println("[监听器·onError] 调用失败：" + ctx.error().getMessage());
    }
}
