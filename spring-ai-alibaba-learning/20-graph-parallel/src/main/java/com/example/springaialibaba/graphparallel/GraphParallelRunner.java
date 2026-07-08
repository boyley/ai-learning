package com.example.springaialibaba.graphparallel;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * ============================================================================
 * 模块 20：并行分叉-汇聚工作流（情感分析 ∥ 关键词提取 → 汇总）
 * ============================================================================
 *
 * 【本次要搭的流程图】
 *
 *                      ┌──────────────────────┐
 *                 ┌───►│ 节点1：sentiment      │──┐  写 state["sentiment"]
 *                 │    │  调模型判断情感倾向    │  │
 *     START ──────┤    └──────────────────────┘  │
 *   写 state[input]│                              ├──►┌─────────────────┐
 *                 │    ┌──────────────────────┐  │   │ 节点3：summary   │──► END
 *                 └───►│ 节点2：keywords       │──┘   │ 读 sentiment +   │  写 report
 *                      │  调模型提取关键词      │      │    keywords 汇总  │
 *                      └──────────────────────┘      └─────────────────┘
 *
 *   ▲ 节点1 和节点2 没有先后依赖，Graph 引擎会**并发执行**它们；
 *     两者都完成后，才进入汇聚节点 summary。
 *
 * 【状态键与合并策略】
 *   这里两个并行节点写的是**不同的 key**（sentiment / keywords），互不覆盖，
 *   所以各用 ReplaceStrategy 即可，最后在 summary 节点分别读取二者。
 *   （若两个并行节点要往【同一个 key】里塞结果，则应改用 AppendStrategy 追加成列表。）
 * ============================================================================
 */
@Component
public class GraphParallelRunner implements CommandLineRunner {

    private final ChatClient chatClient;

    public GraphParallelRunner(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n========== 模块20：Graph 并行节点(分叉→汇聚) ==========\n");

        // 1) 注册状态键：input/sentiment/keywords/report，均用“覆盖”策略
        KeyStrategyFactory keyStrategyFactory = () -> {
            HashMap<String, KeyStrategy> m = new HashMap<>();
            m.put("input", new ReplaceStrategy());     // 原始评论文本
            m.put("sentiment", new ReplaceStrategy());  // 情感分析结果
            m.put("keywords", new ReplaceStrategy());   // 关键词提取结果
            m.put("report", new ReplaceStrategy());     // 汇总报告
            return m;
        };

        // 2) 并行节点1：情感分析
        NodeAction sentimentNode = state -> {
            String input = state.value("input", String.class).orElse("");
            System.out.println("  [并行·sentiment] 开始分析情感...");
            String sentiment = chatClient.prompt()
                    .system("你是情感分析助手。请判断这段文字的情感倾向，只回答“正面/负面/中性”其中之一，并附一句话理由。")
                    .user(input)
                    .call()
                    .content();
            System.out.println("  [并行·sentiment] 完成 = " + safeTrim(sentiment));
            return Map.of("sentiment", safeTrim(sentiment));
        };

        // 3) 并行节点2：关键词提取
        NodeAction keywordsNode = state -> {
            String input = state.value("input", String.class).orElse("");
            System.out.println("  [并行·keywords ] 开始提取关键词...");
            String keywords = chatClient.prompt()
                    .system("你是关键词提取助手。请从用户文字中提取 3~5 个核心关键词，用顿号分隔，只输出关键词。")
                    .user(input)
                    .call()
                    .content();
            System.out.println("  [并行·keywords ] 完成 = " + safeTrim(keywords));
            return Map.of("keywords", safeTrim(keywords));
        };

        // 4) 汇聚节点：读取两个并行结果，合成一份报告（纯 Java 拼装，也可再调模型）
        NodeAction summaryNode = state -> {
            String sentiment = state.value("sentiment", String.class).orElse("(无)");
            String keywords = state.value("keywords", String.class).orElse("(无)");
            String report = "【综合报告】\n    - 情感倾向：" + sentiment + "\n    - 核心关键词：" + keywords;
            System.out.println("  [汇聚·summary  ] 两路结果已到齐，生成综合报告");
            return Map.of("report", report);
        };

        // 5) 建图：START 分叉到两个并行节点，两者再汇聚到 summary
        StateGraph graph = new StateGraph(keyStrategyFactory)
                .addNode("sentiment", node_async(sentimentNode))
                .addNode("keywords", node_async(keywordsNode))
                .addNode("summary", node_async(summaryNode))
                // 分叉：START 同时指向两个节点（两条出边 = 并行）
                .addEdge(START, "sentiment")
                .addEdge(START, "keywords")
                // 汇聚：两个节点都指向 summary（summary 会等两路都完成）
                .addEdge("sentiment", "summary")
                .addEdge("keywords", "summary")
                .addEdge("summary", END);

        CompiledGraph compiledGraph = graph.compile();

        String comment = "这家餐厅的菜品味道很棒，服务也热情，就是等位时间有点长。";
        System.out.println("【输入评论】" + comment + "\n");

        long start = System.currentTimeMillis();
        Optional<OverAllState> finalState = compiledGraph.invoke(Map.of("input", comment));
        long cost = System.currentTimeMillis() - start;

        finalState.ifPresent(s -> {
            System.out.println("\n" + s.value("report", "(无报告)"));
        });
        System.out.println("\n  两个分支并行执行，总耗时约 " + cost + " ms（若改成串行，两次模型调用会相加）");

        System.out.println("\n========== 演示结束：并行=省时间！ ==========\n");
    }

    /** 小工具：去空格并防 null */
    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
}
