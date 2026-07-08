package com.example.springaialibaba.graphbasic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 19：Spring AI Alibaba Graph 入门 —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   带你认识 Spring AI Alibaba 最有特色、也是最强大的能力：**Graph（图）编排**。
 *   我们会用 StateGraph 搭一个最小的“工作流”：
 *       START →（节点A：把中文翻译成英文）→（节点B：把英文变大写）→ END
 *   让你直观感受“多个步骤像流水线一样自动流转，每一步共享同一份状态”。
 *
 * 【为什么需要 Graph？（零基础理解）】
 *   前面 01~18 模块，我们都是“一问一答”地直接调用 ChatClient。
 *   但真实的 AI 应用往往需要**多个步骤配合**：先检索、再总结、再翻译、最后审核……
 *   如果全塞进一段代码，会又长又乱、难以复用和调试。
 *   Graph 把每个步骤抽象成一个“节点(Node)”，用“边(Edge)”把它们连成一张流程图，
 *   步骤之间通过一份“全局状态(State)”传递数据。这就是“工作流/多智能体编排”。
 *
 * 【和 LangGraph 的类比（有经验的同学秒懂）】
 *   Spring AI Alibaba 的 Graph 借鉴了 Python 生态里大名鼎鼎的 LangGraph：
 *     - StateGraph  ≈ LangGraph 的 StateGraph（图的蓝图）
 *     - Node        ≈ LangGraph 的 node（一个处理步骤）
 *     - Edge        ≈ LangGraph 的 edge（步骤间的连线）
 *     - OverAllState≈ LangGraph 的 State（在节点间流动的共享状态字典）
 *   区别只是：这里是 Java + Spring，节点里可以直接注入并调用通义千问。
 *
 * 【怎么做】
 *   - @SpringBootApplication 标记这是 Spring Boot 应用入口。
 *   - 容器启动后自动执行 GraphBasicRunner（CommandLineRunner），在那里建图并运行。
 * ============================================================================
 */
@SpringBootApplication
public class GraphBasicApplication {

    public static void main(String[] args) {
        // 启动 Spring Boot：创建容器 → 自动配置 ChatClient.Builder → 执行 GraphBasicRunner
        SpringApplication.run(GraphBasicApplication.class, args);
    }
}
