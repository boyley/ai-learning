package com.example.lc4j.image;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.openai.OpenAiImageModel;
import dev.langchain4j.model.output.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * ============================================================================
 * 图像模型演示：用 OpenAiImageModel 根据文字生成图片
 * ============================================================================
 *
 * 【流程图】
 *
 *   一句文字提示词(prompt)
 *          │  注入 image.* 连接参数(指向 OpenAI)
 *          ▼
 *   OpenAiImageModel.builder()....build()  ← 构建文生图模型
 *          │  model.generate(prompt)
 *          ▼
 *   OpenAI 图像接口生成图片，返回 Response<Image>
 *          │  response.content()
 *          ▼
 *   Image 对象 ──► image.url() 取图片地址(URI)
 *          │
 *          ▼
 *   打印图片 URL（也可顺便打印 revisedPrompt：模型改写后的提示词）
 *
 * 【提示】真正调用需要有效 OPENAI_API_KEY 且会计费；本模块按要求只验证编译。
 * ============================================================================
 */
@Component
public class ImageModelsRunner implements CommandLineRunner {

    // ★ 注意：这里用的是 image.* 一组属性（指向 OpenAI），而不是 chat.*（指向 DeepSeek）
    @Value("${langchain4j.openai.image.base-url}")
    private String baseUrl;
    @Value("${langchain4j.openai.image.api-key}")
    private String apiKey;
    @Value("${langchain4j.openai.image.model}")
    private String modelName;

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块12：图像模型（文生图）==========\n");

        // 1) 构建图像模型。注意类型是 OpenAiImageModel（不是 ChatModel）。
        OpenAiImageModel model = OpenAiImageModel.builder()
                .baseUrl(baseUrl)       // 图像接口地址（OpenAI）
                .apiKey(apiKey)         // OpenAI 的 Key
                .modelName(modelName)   // 模型名，如 dall-e-3
                .size("1024x1024")      // 可选：图片尺寸
                .quality("standard")    // 可选：图片质量
                .build();

        // 2) 准备提示词（描述你想要的画面，越具体越好）
        String prompt = "一只戴着宇航头盔的橘猫，漂浮在星空中，卡通风格，明亮色彩";
        System.out.println("提示词(prompt)：" + prompt + "\n");

        // 3) ★ 核心：generate(提示词) 生成图片，返回 Response<Image> ★
        //    Response 是 LangChain4j 对“一次模型输出”的统一包装。
        Response<Image> response = model.generate(prompt);

        // 4) 从 Response 里取出图片对象（content() 返回 Image）
        Image image = response.content();

        // 5) image.url() 返回 java.net.URI——这是图片的下载地址
        URI url = image.url();
        System.out.println("生成的图片 URL：" + url);

        // 6) （可选）DALL·E 3 通常会“改写”你的提示词后再画，这里打印它改写后的版本
        System.out.println("模型改写后的提示词(revisedPrompt)：" + image.revisedPrompt());

        System.out.println("\n说明：要真正生成图片，需要在共享配置里填好 OPENAI_API_KEY（会产生费用）。");
        System.out.println("\n========== 模块12 演示结束 ==========\n");
    }
}
