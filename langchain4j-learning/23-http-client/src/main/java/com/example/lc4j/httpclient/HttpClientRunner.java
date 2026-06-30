package com.example.lc4j.httpclient;

import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.jdk.JdkHttpClientBuilder;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * ============================================================================
 * 底层 HTTP 客户端定制演示：自定义 JdkHttpClientBuilder 交给对话模型
 * ============================================================================
 *
 * 【流程图】
 *
 *   OpenAiChatModel.builder()
 *          │  .httpClientBuilder(自定义的 JdkHttpClientBuilder)
 *          ▼
 *   HttpClientBuilder（抽象层接口）
 *          │  build()
 *          ▼
 *   实际 HTTP 实现（JdkHttpClient，封装 java.net.http.HttpClient）
 *          │  按你设的 connectTimeout/readTimeout/代理/重定向发请求
 *          ▼
 *   大模型服务（DeepSeek）
 *
 * 【关键点】
 *   - JdkHttpClientBuilder 实现了 LangChain4j 的 HttpClientBuilder 接口；
 *     模型只认接口，所以将来换成 OkHttp 变体，业务代码一行不用改。
 *   - connectTimeout/readTimeout 直接定义在 HttpClientBuilder 接口上；
 *     更底层的旋钮（代理、重定向、HTTP 版本、连接池）通过它内部持有的
 *     java.net.http.HttpClient.Builder 来设。
 * ============================================================================
 */
@Component
public class HttpClientRunner implements CommandLineRunner {

    // 对话用 chat.*（指向 DeepSeek）
    @Value("${langchain4j.openai.chat.base-url}")
    private String baseUrl;
    @Value("${langchain4j.openai.chat.api-key}")
    private String apiKey;
    @Value("${langchain4j.openai.chat.model}")
    private String modelName;

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块23：底层 HTTP 客户端定制 ==========\n");

        // ===================================================================
        // 第1步：构建一个自定义的 JDK HTTP 客户端构建器，并设置底层 HTTP 行为
        // ===================================================================
        // 先拿到 JDK 原生 HttpClient 的 Builder，用来设“更底层”的旋钮：
        java.net.http.HttpClient.Builder jdkBuilder = HttpClient.newBuilder()
                // 重定向策略：NORMAL=跟随安全的重定向（默认 NEVER 不跟随）。
                .followRedirects(HttpClient.Redirect.NORMAL)
                // 强制使用 HTTP/2（拿不到则自动回退 HTTP/1.1）。
                .version(HttpClient.Version.HTTP_2);
        // 说明：如需走公司代理，可在这里 .proxy(ProxySelector.of(new InetSocketAddress("proxy.company.com", 8080)))

        // JdkHttpClientBuilder 是 LangChain4j 对 java.net.http.HttpClient 的适配器，
        // 它实现了通用接口 HttpClientBuilder，所以最终能交给任意模型使用。
        JdkHttpClientBuilder jdkHttpClientBuilder = new JdkHttpClientBuilder()
                // 把上面定制好的 JDK 原生 Builder 注入进去（代理/重定向/HTTP 版本等都在它身上）
                .httpClientBuilder(jdkBuilder)
                // 连接超时：与服务器“建立 TCP 连接”最多等多久，超时即失败。
                .connectTimeout(Duration.ofSeconds(10))
                // 读超时：连接建立后，等“服务器返回数据”最多等多久（防止请求挂死）。
                .readTimeout(Duration.ofSeconds(60));

        // 面向接口持有：模型只需要 HttpClientBuilder，不关心具体是 JDK 还是 OkHttp 实现。
        HttpClientBuilder httpClientBuilder = jdkHttpClientBuilder;
        System.out.println("已构建自定义 HTTP 客户端构建器：");
        System.out.println("  - connectTimeout = " + httpClientBuilder.connectTimeout());
        System.out.println("  - readTimeout    = " + httpClientBuilder.readTimeout());
        System.out.println("  - 底层实现        = JdkHttpClient（java.net.http.HttpClient，HTTP/2 + 跟随重定向）");
        System.out.println();

        // ===================================================================
        // 第2步：把自定义 HTTP 客户端构建器交给对话模型
        // ===================================================================
        ChatModel model = OpenAiChatModel.builder()
                .baseUrl(baseUrl)        // 接口地址（DeepSeek 兼容地址）
                .apiKey(apiKey)          // API 密钥
                .modelName(modelName)    // 模型名（deepseek-chat）
                // ★★★ 核心：显式指定底层 HTTP 客户端 ★★★
                // 之后这个模型发出的每一次请求，都走我们上面定制的 HTTP 客户端。
                .httpClientBuilder(httpClientBuilder)
                .build();

        // ===================================================================
        // 第3步：发起一次真实对话，证明自定义 HTTP 客户端生效
        // ===================================================================
        String question = "用一句话说明：为什么生产环境要给 HTTP 请求设置超时时间？";
        System.out.println("【我问】" + question);

        // 这次 chat 调用就是经由我们定制的 JdkHttpClient 发出的 HTTP 请求。
        String answer = model.chat(question);
        System.out.println("【AI 答】" + answer);
        System.out.println("\n（若能正常拿到回答，说明自定义 HTTP 客户端已生效——它替代了默认的 HTTP 客户端。）");

        System.out.println("\n========== 模块23 演示结束 ==========\n");
    }
}
