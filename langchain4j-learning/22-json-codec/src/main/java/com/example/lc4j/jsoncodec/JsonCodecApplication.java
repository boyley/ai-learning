package com.example.lc4j.jsoncodec;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 22：JSON 编解码定制（JSON Codec）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   揭开 LangChain4j 的一个“底层基础设施”：JSON 编解码器。
 *   你平时用的很多高级特性，底层都依赖它把对象 <-> JSON 字符串互转：
 *     - 结构化输出（把模型回答转成 Java 对象）
 *     - 工具调用（把方法签名转成 JSON Schema，把参数从 JSON 解析出来）
 *     - 对话历史持久化（把 ChatMessage 列表存进数据库/文件，再读回来）
 *
 * 【怎么做（本模块演示三件事）】
 *   1. 用 LangChain4j 的 JSON 工具类 dev.langchain4j.internal.Json，
 *      把一个普通 Java 对象 toJson 序列化、再 fromJson 反序列化回来。
 *   2. 用 dev.langchain4j.data.message.JacksonChatMessageJsonCodec，
 *      把一段“对话消息”序列化成 JSON 字符串（再读回来），
 *      这正是“保存/恢复对话历史”的底层做法。
 *   3. 讲清 SPI 机制：LangChain4j 如何允许你用自定义实现替换默认 JSON 编解码器。
 *
 * 【为什么默认就够用，但仍要懂它】
 *   - 默认实现基于 Jackson（dev.langchain4j.internal.JacksonJsonCodec），开箱即用。
 *   - 但在企业里你可能要：统一用公司规定的 ObjectMapper、改日期格式、
 *     注册自定义序列化器、或干脆换成 Gson —— 这就要懂 SPI 替换原理。
 *
 * 【与对话的关系】
 *   本模块仍用 DeepSeek 对话（chat.* 配置）发起一次真实对话，
 *   然后把“用户问 + AI 答”的整段历史用编解码器序列化成 JSON，演示落库形态。
 *
 * 【达到的目的】
 *   理解 LangChain4j 的 JSON 编解码层在哪、长什么样、怎么被替换。
 * ============================================================================
 */
@SpringBootApplication
public class JsonCodecApplication {
    public static void main(String[] args) {
        SpringApplication.run(JsonCodecApplication.class, args);
    }
}
