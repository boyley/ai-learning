package com.example.lc4j.agents;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.util.Map;

/**
 * ============================================================================
 * 研究助手的“工具箱”：给智能体装上手脚
 * ============================================================================
 *
 * 【关键点】
 *   - 每个 @Tool 方法就是模型可以自主调用的一个能力。
 *   - @Tool 的字符串是给【模型看】的工具说明：写清楚“这个工具能干什么”，模型才知道何时调用。
 *   - @P 描述每个参数的含义，帮助模型正确地填参数。
 *   - 模型不会算数、不知道我们的私有数据，把这些做成工具，它就能在推理过程中按需调用。
 *
 * 这里提供两类工具，模拟“研究助手”的典型工作：先查资料(knowledgeLookup)，再做计算(calculate)。
 * ============================================================================
 */
public class ResearchTools {

    /** 一个极简的“内部知识库”：真实项目里可能是数据库、检索服务或外部 API。 */
    private static final Map<String, String> KNOWLEDGE_BASE = Map.of(
            "深圳", "深圳 2023 年常住人口约 1779 万人。",
            "广州", "广州 2023 年常住人口约 1883 万人。",
            "杭州", "杭州 2023 年常住人口约 1252 万人。"
    );

    /**
     * 工具1：知识库查询。模型想了解某城市的数据时会调用它。
     *
     * @param city 城市名（如“深圳”）
     * @return 该城市的资料；查不到时返回提示
     */
    @Tool("根据城市名查询该城市的人口等资料。当你需要某个城市的具体数据时调用本工具。")
    public String knowledgeLookup(@P("城市名称，例如 深圳 / 广州 / 杭州") String city) {
        System.out.println("  [工具被调用] knowledgeLookup(city=" + city + ")");
        return KNOWLEDGE_BASE.getOrDefault(city, "知识库中暂无【" + city + "】的资料。");
    }

    /**
     * 工具2：两数相加。大模型本身不擅长精确计算，把算术交给工具更可靠。
     *
     * @param a 第一个加数
     * @param b 第二个加数
     * @return a + b 的精确结果
     */
    @Tool("计算两个整数的和。当你需要做加法时调用本工具，不要自己心算。")
    public long add(@P("第一个加数") long a, @P("第二个加数") long b) {
        long result = a + b;
        System.out.println("  [工具被调用] add(a=" + a + ", b=" + b + ") = " + result);
        return result;
    }
}
