package com.example.springaialibaba.graphbasic;

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

// ★★★ 注意：Graph 的“把普通节点包装成异步节点”的静态方法名是【下划线】写法：node_async ★★★
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * ============================================================================
 * 模块 19：用 StateGraph 搭一个最小工作流（翻译 → 加工）
 * ============================================================================
 *
 * 【本次要搭的流程图】
 *
 *     START
 *       │
 *       ▼
 *   ┌─────────────────────────┐
 *   │ 节点A：translate         │  读 state["input"](中文)
 *   │  用 ChatClient 让通义千问 │  → 调通义千问翻译成英文
 *   │  把中文翻译成英文        │  → 写 state["translated"]
 *   └─────────────────────────┘
 *       │
 *       ▼
 *   ┌─────────────────────────┐
 *   │ 节点B：process           │  读 state["translated"](英文)
 *   │  把英文全部变成大写      │  → 转大写
 *   │  （纯 Java，不调模型）    │  → 写 state["result"]
 *   └─────────────────────────┘
 *       │
 *       ▼
 *      END
 *
 * 【四大核心概念（务必记住）】
 *   1. StateGraph（图/蓝图）：描述“有哪些节点、怎么连线”。它只是蓝图，不能直接跑。
 *   2. Node（节点）：一个处理步骤。用 NodeAction 表达，本质是
 *          `OverAllState state -> Map<String,Object>`
 *      —— 读入全局状态，返回“要更新到全局状态里的键值对”。
 *   3. Edge（边）：节点之间的连线，决定执行顺序。START 是入口，END 是出口。
 *   4. OverAllState（全局状态）：一个在所有节点间流动的“共享字典”。
 *      每个节点返回的 Map 会按“合并策略(KeyStrategy)”写回这个字典。
 *
 * 【KeyStrategy 合并策略（新手最容易忽略但很关键）】
 *   状态里每个 key 都要事先声明“新值来了怎么合并旧值”：
 *     - ReplaceStrategy（覆盖）：用新值直接替换旧值（本模块三个 key 都用它）。
 *     - AppendStrategy（追加）：把新值追加进列表（并行汇聚时常用，见模块 20）。
 *   这些策略由 KeyStrategyFactory 统一注册。
 *
 * 【执行链路】
 *   建图(StateGraph) → compile() 得到 CompiledGraph → invoke(初始状态) → 得到最终 OverAllState
 * ============================================================================
 */
@Component // 标记为 Spring 组件，容器启动后自动执行 run()
public class GraphBasicRunner implements CommandLineRunner {

    /** 与通义千问对话的高级客户端，注入到“翻译节点”里使用 */
    private final ChatClient chatClient;

    /**
     * 构造器注入：ChatClient.Builder 由 spring-ai-alibaba-starter-dashscope 自动配置。
     * 我们 build() 出一个可复用的 ChatClient，供图里的节点调用大模型。
     */
    public GraphBasicRunner(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n========== 模块19：Graph 入门 —— 最小工作流(翻译→加工) ==========\n");

        // ------------------------------------------------------------------
        // 第 1 步：注册“状态键 + 合并策略”。
        //   KeyStrategyFactory 是一个函数式接口，返回 “键名 -> 合并策略” 的 Map。
        //   我们声明三个键，全部用 ReplaceStrategy（后写覆盖先写）。
        // ------------------------------------------------------------------
        KeyStrategyFactory keyStrategyFactory = () -> {
            HashMap<String, KeyStrategy> strategies = new HashMap<>();
            strategies.put("input", new ReplaceStrategy());       // 用户原始输入(中文)
            strategies.put("translated", new ReplaceStrategy());  // 翻译后的英文
            strategies.put("result", new ReplaceStrategy());      // 最终加工结果(大写英文)
            return strategies;
        };

        // ------------------------------------------------------------------
        // 第 2 步：定义“节点A —— 翻译节点”。
        //   NodeAction = `state -> Map`：读全局状态，返回要更新的键值对。
        //   这里读出 input(中文)，调通义千问翻译成英文，写回 translated。
        // ------------------------------------------------------------------
        NodeAction translateNode = state -> {
            // 从全局状态取出 input；取不到就用一个默认值兜底
            String input = state.value("input", String.class).orElse("你好，世界");
            System.out.println("  [节点A·translate] 收到中文输入 = " + input);

            // 调用大模型翻译（system 约束“只输出译文”，避免模型啰嗦）
            String english = chatClient.prompt()
                    .system("你是专业翻译。请把用户的中文翻译成地道英文，只输出英文译文本身，不要任何解释。")
                    .user(input)
                    .call()
                    .content();
            english = english == null ? "" : english.trim();

            System.out.println("  [节点A·translate] 翻译成英文 = " + english);
            // 返回值会被合并进全局状态（translated 用 ReplaceStrategy 覆盖写入）
            return Map.of("translated", english);
        };

        // ------------------------------------------------------------------
        // 第 3 步：定义“节点B —— 加工节点”。
        //   纯 Java 逻辑：把英文全部转成大写，写回 result。
        //   （故意不调模型，让你看清“节点里可以是任意逻辑，不一定都要调 AI”。）
        // ------------------------------------------------------------------
        NodeAction processNode = state -> {
            String english = state.value("translated", String.class).orElse("");
            String upper = english.toUpperCase();
            System.out.println("  [节点B·process ] 转大写后 = " + upper);
            return Map.of("result", upper);
        };

        // ------------------------------------------------------------------
        // 第 4 步：建图（蓝图）。
        //   把节点用 node_async(...) 包装成异步节点加进去，再用 addEdge 连线。
        //   连线：START → translate → process → END。
        // ------------------------------------------------------------------
        StateGraph graph = new StateGraph(keyStrategyFactory)
                .addNode("translate", node_async(translateNode))   // 加入节点A
                .addNode("process", node_async(processNode))       // 加入节点B
                .addEdge(START, "translate")                        // 入口 → 节点A
                .addEdge("translate", "process")                    // 节点A → 节点B
                .addEdge("process", END);                           // 节点B → 出口

        // ------------------------------------------------------------------
        // 第 5 步：编译并运行。
        //   compile() 把蓝图变成可执行的 CompiledGraph；
        //   invoke(初始状态) 从 START 一路跑到 END，返回最终的全局状态。
        // ------------------------------------------------------------------
        CompiledGraph compiledGraph = graph.compile();

        String userInput = "人工智能正在改变世界。";
        System.out.println("【输入】" + userInput + "\n");

        Optional<OverAllState> finalState = compiledGraph.invoke(Map.of("input", userInput));

        // ------------------------------------------------------------------
        // 第 6 步：打印最终状态里的每个 key，观察数据如何一路流转、累积。
        // ------------------------------------------------------------------
        finalState.ifPresent(state -> {
            System.out.println("\n【最终全局状态 OverAllState】");
            System.out.println("  input      = " + state.value("input", ""));
            System.out.println("  translated = " + state.value("translated", ""));
            System.out.println("  result     = " + state.value("result", ""));
        });

        System.out.println("\n========== 演示结束：这就是一张最小的 Graph 工作流！ ==========\n");
    }
}
