package com.example.springaialibaba.nacosprompt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 24：Nacos 动态 Prompt —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   演示阿里特色能力：把 **Prompt(提示词) 模板托管到 Nacos 配置中心**。
 *
 * 【为什么需要它？（零基础理解）】
 *   提示词经常要反复打磨（改语气、加约束、换角色）。如果 Prompt 写死在代码里，
 *   每改一次就得**改代码 → 重新打包 → 重启上线**，又慢又重。
 *   把 Prompt 放到 Nacos（阿里开源的配置中心）后：
 *     - 运营/产品在 Nacos 控制台直接改提示词；
 *     - 应用**监听到配置变更，实时热更新**，无需改代码、无需重启。
 *   这对需要频繁调 Prompt 的线上 AI 应用极其有用。
 *
 * 【本模块如何演示（保证无 Nacos 环境也能编译/启动）】
 *   Runner 先探测容器里有没有 Nacos 动态 Prompt 相关组件：
 *     - 有：说明已接入（真实用法见 README）；
 *     - 没有：打印“未连接 Nacos，跳过”，并**退回本地 PromptTemplate** 演示
 *       “同一个模板 + 不同变量 → 渲染出不同提示词”的效果（渲染机制与 Nacos 版一致，
 *        区别只是模板来源是本地字符串还是 Nacos 配置）。
 *   这样在任何环境都能跑通，把概念讲清楚。
 * ============================================================================
 */
@SpringBootApplication
public class NacosPromptApplication {

    public static void main(String[] args) {
        SpringApplication.run(NacosPromptApplication.class, args);
    }
}
