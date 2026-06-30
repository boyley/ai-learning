package com.example.lc4j.httpclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 23：底层 HTTP 客户端定制（Customizable HTTP Client）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   LangChain4j 调用大模型，本质是发 HTTP 请求。这一层“怎么发 HTTP”是可替换的：
 *   你可以显式指定一个 HttpClientBuilder 交给模型，从而精确控制底层 HTTP 行为。
 *
 * 【为什么要换/定制 HTTP 客户端】
 *   - 超时控制：连接超时、读超时设多少，避免请求挂死拖垮线程。
 *   - 代理：公司内网必须走 HTTP 代理才能访问外网大模型。
 *   - 连接池/复用：高并发下复用连接，提升吞吐、降低握手开销。
 *   - 基础设施统一：和公司既有的 HTTP 栈（监控、链路追踪、TLS 配置）保持一致。
 *
 * 【几种变体（由不同 Maven 依赖提供）】
 *   - langchain4j-http-client          ：抽象层（HttpClient / HttpClientBuilder 接口 + SPI）。
 *   - langchain4j-http-client-jdk       ：用 Java 17 自带的 java.net.http.HttpClient 实现（本模块用它）。
 *   - langchain4j-http-client-okhttp    ：用 OkHttp 实现（生态成熟、连接池强）。
 *
 * 【怎么做】
 *   1. new JdkHttpClientBuilder()，设置 connectTimeout / readTimeout，
 *      并可进一步定制底层 java.net.http.HttpClient.Builder（重定向策略、HTTP 版本等）。
 *   2. 通过 OpenAiChatModel.builder().httpClientBuilder(...) 把它交给模型；
 *      之后这个模型的每次请求都走你定制的 HTTP 客户端（chat.* 指向 DeepSeek）。
 *
 * 【达到的目的】
 *   知道 LangChain4j 的 HTTP 层在哪、怎么显式替换、生产上为何要这么做。
 * ============================================================================
 */
@SpringBootApplication
public class HttpClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(HttpClientApplication.class, args);
    }
}
