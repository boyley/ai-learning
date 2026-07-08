package com.example.springaialibaba.humanloop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 21：Graph 人类介入 (Human-in-the-Loop) —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   演示 AI 智能体里非常重要的一环：**人类介入 / 人工审批**。
 *   很多高风险场景（发邮件、下单、改数据库）不能让 AI 全自动做主，
 *   必须“AI 出方案 → 人来审核 → 通过才执行，否决就打回重改”。
 *
 * 【本模块如何演示（为保证编译稳妥而简化）】
 *   我们用 Graph 的**条件边**来表达这个决策分支：
 *     - 节点 generate：AI 生成一份方案，写入 state["plan"]。
 *     - 条件边：读取 state["approved"] 这个“人工审批结果(布尔)”，
 *         approved=true  → 走 approve 节点（通过，正式采纳）→ END
 *         approved=false → 走 revise  节点（被打回，记录需修改）→ END
 *   Runner 会先以 approved=false 跑一遍（看到“被人工打回”），
 *   再以 approved=true 跑一遍（看到“审批通过”），直观对比两条分支。
 *
 * 【真实生产怎么做（见 README）】
 *   真正的“暂停等人”应使用 CompileConfig.interruptBefore(...) 让图在某节点前**中断**，
 *   把状态持久化，等人工在前端点了“通过/驳回”后再用 compiledGraph.resume(...) 恢复执行。
 *   本模块用“条件边 + 预置 approved 标志”把概念讲清楚且保证代码简单可编译。
 * ============================================================================
 */
@SpringBootApplication
public class HumanLoopApplication {

    public static void main(String[] args) {
        SpringApplication.run(HumanLoopApplication.class, args);
    }
}
