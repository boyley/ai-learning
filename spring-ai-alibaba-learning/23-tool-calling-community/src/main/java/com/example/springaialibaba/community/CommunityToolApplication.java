package com.example.springaialibaba.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 23：社区工具集 (Community Tools) —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   演示 Spring AI Alibaba 的一大生态优势：**社区工具集**。
 *   模块 08/22 我们自己写 @Tool 工具；而很多常用能力（联网搜索、地图、翻译……）
 *   社区已经封装成了“一个 starter = 一个工具”，引入即用，无需自己实现。
 *
 * 【本模块的例子：百度搜索】
 *   只要在 pom 里加一行 spring-ai-alibaba-starter-tool-calling-baidusearch，
 *   它就会自动把“百度搜索”注册成一个工具 Bean。我们把它交给 ChatClient，
 *   模型遇到“需要实时/最新信息”的问题时，就会自动联网搜索再回答。
 *
 * 【社区工具集生态（举例）】
 *   - spring-ai-alibaba-starter-tool-calling-baidusearch  百度搜索
 *   - spring-ai-alibaba-starter-tool-calling-amap         高德地图（路线/POI）
 *   - spring-ai-alibaba-starter-tool-calling-baidutranslate 百度翻译
 *   - 还有天气、时间、序列化、爬虫……几十个开箱即用工具。
 *
 * 【稳健性说明（见 Runner 与 README）】
 *   不同版本自动注册的 Bean 名称/类型可能不同，为保证在任何环境都能编译启动，
 *   Runner 用 ObjectProvider “可选注入”，取不到就跳过并给出提示，绝不因缺 Key/Bean 崩溃。
 * ============================================================================
 */
@SpringBootApplication
public class CommunityToolApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunityToolApplication.class, args);
    }
}
