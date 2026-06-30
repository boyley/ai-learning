package com.example.lc4j.image;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 12：图像模型（Image Models / 文生图）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   前面所有模块用的都是“对话模型（ChatModel）”——文字进、文字出。
 *   本模块介绍另一类模型：“图像模型（ImageModel）”，最常见的能力是
 *   【文生图（text-to-image）】：给一段文字描述（提示词），模型生成一张图片。
 *
 * 【用到的核心类型（已通过 javap 确认签名）】
 *   - dev.langchain4j.model.openai.OpenAiImageModel：OpenAI 的图像模型实现（如 DALL·E 3）。
 *   - Response<Image> generate(String prompt)：传入提示词，返回包装了图片的 Response。
 *   - response.content() -> Image：取出生成的图片对象。
 *   - image.url() -> java.net.URI：图片的下载地址（DALL·E 默认返回临时 URL）。
 *
 * 【为什么连接参数指向 OpenAI 而不是 DeepSeek】
 *   DeepSeek 不提供图像生成能力，所以本模块从共享配置的 image.* 一组属性读取
 *   连接参数（base-url / api-key / model），它们指向真正的 OpenAI。
 *
 * 【注意】
 *   真正调用需要有效的 OPENAI_API_KEY 且会产生费用，本模块按规范只要求能编译通过；
 *   运行时若未配置 Key，调用 generate(...) 会报鉴权错误，属于正常现象。
 *
 * 【达到的目的】
 *   理解 ImageModel 与 ChatModel 的区别，掌握 OpenAiImageModel 文生图的基本调用。
 * ============================================================================
 */
@SpringBootApplication
public class ImageModelsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ImageModelsApplication.class, args);
    }
}
