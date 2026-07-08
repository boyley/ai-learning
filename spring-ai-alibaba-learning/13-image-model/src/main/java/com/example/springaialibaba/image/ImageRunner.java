package com.example.springaialibaba.image;

import com.alibaba.cloud.ai.dashscope.image.DashScopeImageOptions;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * 文生图演示：一句话 → 通义万相 → 一张图（图片 URL）
 * ============================================================================
 *
 * 【核心流程图】
 *
 *   "一只戴帽子的柴犬"
 *        │
 *        ▼
 *   new ImagePrompt(描述, DashScopeImageOptions.withModel("wanx2.1-t2i-turbo"))
 *        │
 *        ▼
 *   imageModel.call(prompt)  ──►  通义万相作画（可能耗时几秒~十几秒）
 *        │
 *        ▼
 *   ImageResponse ──► getResult().getOutput().getUrl()  ──►  图片下载 URL
 *
 * 【关键 API 链路解读】
 *   DashScopeImageOptions.builder().withModel("wanx2.1-t2i-turbo").build()
 *                                          -> 指定用哪个万相模型
 *   new ImagePrompt("描述", options)        -> 组装文生图请求
 *   imageModel.call(prompt)                -> 发起生成，返回 ImageResponse
 *   response.getResult().getOutput().getUrl() -> 取图片 URL（万相返回 URL 而非 base64）
 *
 * 【重点】ImageModel / ImagePrompt / ImageResponse 来自 org.springframework.ai.image.*（通用）；
 *   DashScopeImageOptions 是 DashScope 特有选项，来自 com.alibaba.cloud.ai.dashscope.image.*。
 * ============================================================================
 */
@Component
public class ImageRunner implements CommandLineRunner {

    /** 图像生成模型：由 starter 自动配置（底层 DashScopeImageModel，接通义万相）。 */
    private final ImageModel imageModel;

    public ImageRunner(ImageModel imageModel) {
        this.imageModel = imageModel;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块13：文生图（通义万相 wanx）==========\n");

        String prompt = "一只戴帽子的柴犬，卡通风格，阳光明媚的草地背景";
        System.out.println("【我说】请画：" + prompt);
        System.out.println("（正在请求通义万相作画，可能需要几秒到十几秒，请稍候……）");

        try {
            // ★核心：组装并发起文生图请求
            ImageResponse response = imageModel.call(new ImagePrompt(
                    prompt,
                    DashScopeImageOptions.builder()
                            .withModel("wanx2.1-t2i-turbo")   // 指定万相模型
                            .build()
            ));

            // 判空兜底：无 Key / 无权限 / 欠费 等情况下响应可能为空，避免 NPE 崩溃
            if (response == null || response.getResult() == null
                    || response.getResult().getOutput() == null) {
                System.out.println("\n【结果】未拿到图片。请检查：是否已在百炼开通【通义万相 wanx】权限、Key 是否正确、账户是否有额度。");
            } else {
                String url = response.getResult().getOutput().getUrl();
                System.out.println("\n【AI 画好了】图片 URL（复制到浏览器即可查看/下载）：");
                System.out.println("  " + url);
                System.out.println("\n说明：通义万相返回的是图片 URL（不是 base64）。该 URL 有时效，请及时下载保存。");
            }
        } catch (Exception e) {
            // 兜底：把异常打印成友好提示，不让程序以堆栈崩溃收场
            System.out.println("\n【调用出错】" + e.getMessage());
            System.out.println("常见原因：未开通通义万相权限 / API Key 未配置或无效 / 账户额度不足。");
        }

        System.out.println("\n========== 演示结束：你已用一句话生成了一张图 ==========\n");
    }
}
