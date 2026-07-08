package com.example.springaialibaba.multimodality;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import java.net.MalformedURLException;

/**
 * ============================================================================
 * 模块 05 演示：多模态视觉——图片 + 文字一起输入，让 AI 看图说话
 * ============================================================================
 *
 * 【流程图】
 *
 *   ┌──────────────┐   ┌───────────────────────────┐
 *   │ 文字：这张图是什么？│ + │ 图片：一个公开的图片 URL      │
 *   └──────┬───────┘   └──────────────┬────────────┘
 *          │        组装成一条多模态 User 消息          │
 *          └───────────────┬──────────────────────────┘
 *                          ▼
 *          ChatClient（临时切换到 qwen-vl-max 视觉模型）
 *                          ▼
 *              通义千问 VL 模型“看图 + 读字” → 文字描述
 *
 * 【核心 API】
 *   chatClient.prompt()
 *       .options(DashScopeChatOptions.builder().withModel("qwen-vl-max").build()) // 换视觉模型
 *       .user(u -> u.text("这张图里有什么？")
 *                   .media(MimeTypeUtils.IMAGE_JPEG, new UrlResource(图片URL)))    // 附加图片
 *       .call().content();
 *
 * 【稳健性】UrlResource 构造可能抛 MalformedURLException（URL 格式错误），
 *   这里用 try/catch 兜底，保证即使没配 Key/图片不可达，程序也能正常结束、代码可编译。
 * ============================================================================
 */
@Component
public class MultimodalityRunner implements CommandLineRunner {

    private final ChatClient chatClient;

    /** 一张公开可访问的示例图片（阿里云官方示例图：一个女孩和一只狗在沙滩上）。 */
    private static final String IMAGE_URL =
            "https://dashscope.oss-cn-beijing.aliyuncs.com/images/dog_and_girl.jpeg";

    public MultimodalityRunner(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块05：多模态（图片 + 文字）==========\n");
        System.out.println("【使用模型】qwen-vl-max（视觉模型，能看懂图片）");
        System.out.println("【图片地址】" + IMAGE_URL);

        String question = "这张图片里有什么？请用中文简要描述画面内容。";
        System.out.println("【我问】" + question + "\n");

        try {
            // 把公开图片 URL 包装成 Spring 的资源对象（可能抛 MalformedURLException）
            UrlResource imageResource = new UrlResource(IMAGE_URL);

            String answer = chatClient
                    .prompt()
                    // ★ 关键1：临时切换到多模态视觉模型 qwen-vl-max（默认的 qwen-plus 看不懂图）
                    .options(DashScopeChatOptions.builder()
                            .withModel("qwen-vl-max")
                            .build())
                    // ★ 关键2：一条 User 消息同时带“文字 + 图片”
                    .user(u -> u
                            .text(question)                                       // 文字部分
                            .media(MimeTypeUtils.IMAGE_JPEG, imageResource))       // 图片附件
                    .call()
                    .content();

            System.out.println("【AI 看图说话】" + answer + "\n");
        } catch (MalformedURLException e) {
            // URL 格式不对时的兜底（正常情况下不会走到这里）
            System.out.println("[跳过] 图片 URL 格式有误：" + e.getMessage());
        } catch (Exception e) {
            // 未配置 Key / 网络不可达 / 图片打不开 等运行期异常的兜底，保证演示不崩
            System.out.println("[跳过] 多模态调用失败（可能未配置 Key 或网络不可达）：" + e.getMessage());
        }

        System.out.println("========== 演示结束：你已学会给模型同时喂图片和文字 ==========\n");
    }
}
