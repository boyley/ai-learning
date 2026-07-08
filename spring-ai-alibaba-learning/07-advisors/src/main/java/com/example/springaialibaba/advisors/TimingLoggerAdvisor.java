package com.example.springaialibaba.advisors;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ============================================================================
 * 自定义 Advisor：测量并打印每次模型调用的耗时 + 时间戳
 * ============================================================================
 *
 * 【怎么写一个自定义 Advisor（非流式版）】
 *   实现 CallAdvisor 接口（org.springframework.ai.chat.client.advisor.api.CallAdvisor），
 *   需要实现三个方法：
 *     - adviseCall(request, chain)：核心！在 chain.nextCall(request) 前后插入逻辑。
 *     - getName()：给这个 Advisor 起个名字（日志/排序用）。
 *     - getOrder()：排序号，数字越小越靠外层（越先执行前置、越后执行后置）。
 *
 *   注：CallAdvisor 只处理【非流式 .call()】。若要拦截【流式 .stream()】，
 *       另有 StreamAdvisor 接口（方法 adviseStream，返回 Flux）。两者思路一致。
 * ============================================================================
 */
public class TimingLoggerAdvisor implements CallAdvisor {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    /**
     * 核心拦截方法：在“放行给下一环”的前后分别做前置/后置处理。
     */
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        // ---- 前置：请求真正发出前 ----
        long start = System.currentTimeMillis();
        System.out.println("  [⏱ TimingAdvisor] 请求发出 @ " + LocalDateTime.now().format(TS));

        // ---- 放行：把请求交给链上的下一个 Advisor / 最终交给模型 ----
        ChatClientResponse response = chain.nextCall(request);

        // ---- 后置：拿到响应后 ----
        long cost = System.currentTimeMillis() - start;
        System.out.println("  [⏱ TimingAdvisor] 收到响应 @ " + LocalDateTime.now().format(TS)
                + "，本次调用耗时 " + cost + " ms");
        return response;
    }

    /** Advisor 的名字（用于日志与识别）。 */
    @Override
    public String getName() {
        return "TimingLoggerAdvisor";
    }

    /** 排序号：越小越靠外层。这里给 0，让它包在较外层，从而统计到完整耗时。 */
    @Override
    public int getOrder() {
        return 0;
    }
}
