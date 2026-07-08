package com.example.springaialibaba.humanloop;

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
// ★ 条件边用的静态方法名同样是【下划线】写法：edge_async
import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;

/**
 * ============================================================================
 * 模块 21：人类介入工作流（AI 出方案 → 人工审批 → 通过/打回）
 * ============================================================================
 *
 * 【本次要搭的流程图（含条件分支）】
 *
 *     START
 *       │
 *       ▼
 *   ┌──────────────────┐
 *   │ generate         │  调模型生成一份方案 → 写 state["plan"]
 *   └──────────────────┘
 *       │
 *       ▼  ★条件边：读 state["approved"]（人工审批结果）
 *       ├───────── approved == true  ──────► ┌──────────┐
 *       │                                    │ approve  │ 采纳方案 → END
 *       │                                    └──────────┘
 *       └───────── approved == false ──────► ┌──────────┐
 *                                            │ revise   │ 记录“被打回，需修改” → END
 *                                            └──────────┘
 *
 * 【核心 API：条件边 addConditionalEdges】
 *   .addConditionalEdges(
 *        "generate",                                   // 从哪个节点出发做判断
 *        edge_async(state -> 返回一个"路由名字符串"),    // 判断逻辑（读状态返回走哪条路）
 *        Map.of("pass", "approve", "reject", "revise")  // 路由名 → 目标节点 的映射表
 *   )
 *   注意：edge_async 的下划线写法；它包装的是 `state -> String`（返回路由 key）。
 * ============================================================================
 */
@Component
public class HumanLoopRunner implements CommandLineRunner {

    private final ChatClient chatClient;

    public HumanLoopRunner(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n========== 模块21：Graph 人类介入(AI出方案→人工审批) ==========\n");

        // 1) 注册状态键
        KeyStrategyFactory keyStrategyFactory = () -> {
            HashMap<String, KeyStrategy> m = new HashMap<>();
            m.put("task", new ReplaceStrategy());      // 交给 AI 的任务
            m.put("plan", new ReplaceStrategy());      // AI 生成的方案
            m.put("approved", new ReplaceStrategy());  // 人工审批结果(布尔)：true=通过 false=打回
            m.put("outcome", new ReplaceStrategy());   // 最终处置结果说明
            return m;
        };

        // 2) 节点 generate：AI 生成方案
        NodeAction generateNode = state -> {
            String task = state.value("task", String.class).orElse("");
            System.out.println("  [generate] AI 正在为任务生成方案：" + task);
            String plan = chatClient.prompt()
                    .system("你是项目助理。请针对用户任务给出一个简短可执行的方案，控制在 3 条要点以内。")
                    .user(task)
                    .call()
                    .content();
            plan = plan == null ? "" : plan.trim();
            System.out.println("  [generate] AI 方案已生成：\n    " + plan.replace("\n", "\n    "));
            return Map.of("plan", plan);
        };

        // 3) 节点 approve：审批通过时执行（采纳方案）
        NodeAction approveNode = state -> {
            System.out.println("  [approve ] ✅ 审批通过：方案被正式采纳，进入执行阶段。");
            return Map.of("outcome", "已通过审批，方案被采纳执行。");
        };

        // 4) 节点 revise：审批被打回时执行（要求修改）
        NodeAction reviseNode = state -> {
            System.out.println("  [revise  ] ❌ 被人工打回：方案需按反馈修改后重新提交。");
            return Map.of("outcome", "未通过审批，方案被打回，需要修改。");
        };

        // 5) 建图：generate 后用条件边根据 approved 决定走 approve 还是 revise
        StateGraph graph = new StateGraph(keyStrategyFactory)
                .addNode("generate", node_async(generateNode))
                .addNode("approve", node_async(approveNode))
                .addNode("revise", node_async(reviseNode))
                .addEdge(START, "generate")
                // ★ 条件边：读 approved，true→"pass"→approve 节点；false→"reject"→revise 节点
                .addConditionalEdges("generate",
                        edge_async(state -> {
                            boolean approved = state.value("approved", Boolean.class).orElse(false);
                            return approved ? "pass" : "reject";
                        }),
                        Map.of("pass", "approve", "reject", "revise"))
                .addEdge("approve", END)
                .addEdge("revise", END);

        CompiledGraph compiledGraph = graph.compile();

        String task = "为部门组织一次线上技术分享会";

        // ---- 第一次：模拟人工“打回” approved=false ----
        System.out.println("——— 第 1 次运行：人工审批结果 = 打回(approved=false) ———");
        Optional<OverAllState> r1 = compiledGraph.invoke(Map.of("task", task, "approved", false));
        r1.ifPresent(s -> System.out.println("  [结果] " + s.value("outcome", "") + "\n"));

        // ---- 第二次：模拟人工“通过” approved=true ----
        System.out.println("——— 第 2 次运行：人工审批结果 = 通过(approved=true) ———");
        Optional<OverAllState> r2 = compiledGraph.invoke(Map.of("task", task, "approved", true));
        r2.ifPresent(s -> System.out.println("  [结果] " + s.value("outcome", "") + "\n"));

        System.out.println("========== 演示结束：条件边=让人类决定 AI 是否放行！ ==========\n");
    }
}
