package com.example.springaialibaba.chatmodel;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * ============================================================================
 * 模块 02 演示：ChatModel(底层) vs ChatClient(高级) + 流式/非流式 + 运行时选项
 * ============================================================================
 *
 * 【两层抽象是什么关系】
 *
 *   ┌─────────────────────────────────────────────────────────────┐
 *   │  ChatClient（高级客户端，推荐日常使用）                        │
 *   │    - 链式 API：prompt().system().user().call()/.stream()      │
 *   │    - 自动帮你封装消息、挂载 Advisor、做结构化输出等            │
 *   └───────────────────────────┬─────────────────────────────────┘
 *                               │ 内部委托给
 *                               ▼
 *   ┌─────────────────────────────────────────────────────────────┐
 *   │  ChatModel（底层模型接口，最原始）                            │
 *   │    - 一个方法：call(Prompt) -> ChatResponse                   │
 *   │    - 由 DashScopeChatModel 实现，直接对接通义千问 HTTP 接口   │
 *   └─────────────────────────────────────────────────────────────┘
 *
 *   一句话：ChatClient 是“方便的外壳”，ChatModel 是“干活的引擎”。
 *   99% 的业务代码用 ChatClient；想看最底层怎么发一次请求，就用 ChatModel。
 *
 * 【三个演示】
 *   演示1  非流式：chatClient.prompt().user(..).call().content()   -> 一次性拿完整回答
 *   演示2  流式  ：chatClient.prompt().user(..).stream().content() -> Flux<String> 逐段打印
 *   演示3  底层  ：chatModel.call(new Prompt(..))                  -> 直接用底层模型
 *   演示4  运行时覆盖参数：.options(DashScopeChatOptions.builder()
 *                              .withModel("qwen-max").withTemperature(0.9).build())
 *          —— 临时把模型换成 qwen-max、把温度调高，只对这一次调用生效。
 * ============================================================================
 */
@Component
public class ChatModelRunner implements CommandLineRunner {

    /** 高级客户端：日常首选。由 starter 自动配置的 Builder 构建而来。 */
    private final ChatClient chatClient;

    /** 底层模型：Spring AI 的 ChatModel 接口，实际实现是 DashScopeChatModel（也由 starter 自动配置）。 */
    private final ChatModel chatModel;

    /**
     * 构造器注入：
     *   - ChatClient.Builder 与 ChatModel 都是 spring-ai-alibaba-starter-dashscope 自动配置的 Bean。
     *   - 这里同时拿到“高级外壳”和“底层引擎”，方便下面对比演示。
     */
    public ChatModelRunner(ChatClient.Builder chatClientBuilder, ChatModel chatModel) {
        this.chatClient = chatClientBuilder.build();
        this.chatModel = chatModel;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块02：ChatModel 与 ChatClient ==========\n");

        // ---------------- 演示1：非流式（.call）----------------
        System.out.println("---------- 演示1：非流式 .call().content()（等全部想完再返回）----------");
        String q1 = "用一句话介绍你自己。";
        System.out.println("【我问】" + q1);
        String answer1 = chatClient
                .prompt()          // 开始一次请求
                .user(q1)          // 用户问题
                .call()            // 同步调用，阻塞直到完整返回
                .content();        // 取纯文本
        System.out.println("【AI 答（非流式）】" + answer1 + "\n");

        // ---------------- 演示2：流式（.stream）----------------
        System.out.println("---------- 演示2：流式 .stream().content()（边生成边返回，打字机效果）----------");
        String q2 = "用三句话描述杭州西湖的美。";
        System.out.println("【我问】" + q2);
        System.out.print("【AI 答（流式，逐段到达）】");
        // .stream().content() 返回 Flux<String>：一串“会陆续到达”的文字片段。
        Flux<String> stream = chatClient
                .prompt()
                .user(q2)
                .stream()          // 改成流式调用
                .content();        // 得到 Flux<String>
        // toStream() 把响应式 Flux 转成阻塞式 Java Stream，逐段打印，模拟打字机。
        // （也可用 stream.blockLast() 等它结束；这里用 forEach 逐段输出更直观。）
        stream.toStream().forEach(System.out::print);
        System.out.println("\n");

        // ---------------- 演示3：底层 ChatModel ----------------
        System.out.println("---------- 演示3：底层 chatModel.call(Prompt)（最原始的一次请求-响应）----------");
        String q3 = "1 加 1 等于几？只回答数字。";
        System.out.println("【我问】" + q3);
        // Prompt 是 Spring AI 对“一次对话请求”的封装；这里最简单地只放一句用户文本。
        ChatResponse response = chatModel.call(new Prompt(q3));
        // 从响应里逐层取出：结果 -> 输出消息 -> 文本
        String answer3 = response.getResult().getOutput().getText();
        System.out.println("【AI 答（底层）】" + answer3 + "\n");

        // ---------------- 演示4：运行时覆盖模型/温度 ----------------
        System.out.println("---------- 演示4：DashScopeChatOptions 运行时覆盖参数（临时换 qwen-max + 高温度）----------");
        String q4 = "给我的猫起 3 个有创意的名字。";
        System.out.println("【我问】" + q4 + "（本次强制使用 qwen-max，temperature=0.9 更有创造力）");
        // DashScopeChatOptions 是 DashScope 专属选项类（阿里特有）：
        //   withModel     -> 覆盖默认模型（配置文件里默认是 qwen-plus，这里临时换成 qwen-max）
        //   withTemperature-> 覆盖采样温度（越高越发散）
        // 通过 .options(...) 挂上去，只对本次调用生效，不影响其它调用。
        DashScopeChatOptions options = DashScopeChatOptions.builder()
                .withModel("qwen-max")
                .withTemperature(0.9)
                .build();
        String answer4 = chatClient
                .prompt()
                .options(options)  // ★ 运行时覆盖参数
                .user(q4)
                .call()
                .content();
        System.out.println("【AI 答（qwen-max）】" + answer4 + "\n");

        System.out.println("========== 演示结束：你已掌握底层/高级、流式/非流式、运行时选项 ==========\n");
    }
}
