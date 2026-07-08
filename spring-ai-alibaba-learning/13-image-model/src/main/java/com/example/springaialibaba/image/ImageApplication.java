package com.example.springaialibaba.image;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 13：文生图（通义万相）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   前面都是“文字进、文字出”。本模块跨入【多模态】：给一句文字描述（prompt），
 *   让模型画出一张图，返回图片的下载 URL。用的是阿里【通义万相(wanx)】文生图模型。
 *
 * 【需要先懂的几个概念】
 *   1. 文生图(Text-to-Image)：输入一段文字，输出一张图片。
 *   2. 通义万相(wanx)：阿里的文生图大模型，本项目用 wanx2.1-t2i-turbo（速度快）。
 *   3. ImageModel：Spring AI 定义的“图像生成”统一接口，
 *      底层由 DashScopeImageModel 实现（starter 自动配置好）。
 *   4. 返回形式：DashScope 万相返回的是图片【URL】（不是 base64），
 *      拿到 URL 后用浏览器打开即可看到/下载图片。
 *
 * 【★ 使用前提（重要）】
 *   文生图属于“需单独开通”的能力：请到百炼(DashScope)控制台确认已开通
 *   通义万相 wanx 模型的调用权限，且账户有额度，否则调用会报权限/欠费错误。
 *
 * 【怎么做】
 *   容器启动 → 自动配置 ImageModel → ImageRunner 里
 *   imageModel.call(new ImagePrompt("描述", 万相选项)) → 从响应里取图片 URL 打印。
 * ============================================================================
 */
@SpringBootApplication
public class ImageApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImageApplication.class, args);
    }
}
