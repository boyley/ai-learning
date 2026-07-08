package com.example.springaialibaba.multimodality;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 05：多模态（图片 + 文字）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   前面几个模块都是“纯文字”输入。本模块演示“多模态(Multimodality)”：
 *   同时把【一张图片】和【一句文字问题】喂给模型，让它“看图说话”。
 *
 * 【关键前提：必须用视觉模型 qwen-vl-max】
 *   普通 qwen-plus/qwen-max 只会读文字，看不懂图片。
 *   要理解图片，必须换成 DashScope 的多模态视觉模型 qwen-vl-max（vl = Vision-Language）。
 *   本模块通过 DashScopeChatOptions.builder().withModel("qwen-vl-max") 在调用时临时切换。
 *
 * 【图片怎么传给模型：Media 附件】
 *   Spring AI 用 Media(媒体附件) 表示图片/音频等非文字内容。
 *   在 ChatClient 里：.user(u -> u.text("问题").media(图片类型, 图片资源))
 *   本模块用一个【公开图片 URL】作为图片资源（UrlResource）。
 *
 * 【怎么做】
 *   @SpringBootApplication 启动容器；容器就绪后自动执行 MultimodalityRunner 演示。
 * ============================================================================
 */
@SpringBootApplication
public class MultimodalityApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultimodalityApplication.class, args);
    }
}
