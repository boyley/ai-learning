package com.example.springaialibaba.reactagent;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * ============================================================================
 * ReAct 智能体的“工具箱”
 * ============================================================================
 *
 * 【工具是什么】
 *   工具 = 一个带 @Tool 注解的普通 Java 方法。大模型看到它的
 *   名字 + description + 参数说明后，会在“需要时”自动决定调用它。
 *   - @Tool(description=...)：告诉模型“这个工具能干嘛”，写清楚模型才会用对。
 *   - @ToolParam(description=...)：说明每个参数的含义。
 *
 * 这里放两个简单工具：一个计算器、一个（假的）天气查询。
 * 让模型在回答问题时自己判断该调用哪个、调用几次。
 * ============================================================================
 */
public class ReactTools {

    /**
     * 工具1：加法计算器。
     * 大模型自己算数容易出错，交给代码算最可靠——这正是 ReAct 里“行动(Act)”的意义。
     */
    @Tool(description = "计算两个整数相加的结果。当用户需要做加法运算时调用它。")
    public int add(@ToolParam(description = "第一个加数") int a,
                   @ToolParam(description = "第二个加数") int b) {
        int sum = a + b;
        System.out.println("    >> [工具被调用] add(" + a + ", " + b + ") = " + sum);
        return sum;
    }

    /**
     * 工具2：查询城市天气（演示用，返回写死的数据）。
     * 真实项目里这里会去调用天气 API；本模块为了不依赖外部服务，返回模拟数据。
     */
    @Tool(description = "查询指定城市今天的天气情况。当用户询问某个城市的天气时调用它。")
    public String queryWeather(@ToolParam(description = "城市名称，例如“杭州”") String city) {
        System.out.println("    >> [工具被调用] queryWeather(\"" + city + "\")");
        // 模拟数据：真实场景应调用天气服务
        return city + "今天晴，气温 26℃，微风，空气质量优。";
    }
}
