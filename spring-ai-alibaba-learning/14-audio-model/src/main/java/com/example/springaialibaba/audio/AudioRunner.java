package com.example.springaialibaba.audio;

import com.alibaba.cloud.ai.dashscope.audio.tts.DashScopeAudioSpeechModel;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ============================================================================
 * 语音合成(TTS)演示：把“你好，欢迎学习”读成一段 mp3 音频
 * ============================================================================
 *
 * 【核心流程图】
 *
 *   "你好，欢迎学习"
 *        │
 *        ▼
 *   new TextToSpeechPrompt("你好，欢迎学习")
 *        │
 *        ▼
 *   speechModel.call(prompt)  ──►  通义 cosyvoice 合成语音
 *        │
 *        ▼
 *   TextToSpeechResponse ──► getResult().getOutput()  ──►  byte[] 音频字节
 *        │
 *        ▼
 *   写入 generated-output/hello.mp3
 *
 * 【关键 API 链路解读】
 *   new TextToSpeechPrompt("文字")          -> 组装 TTS 请求（模型/音色取自 yaml 默认配置）
 *   speechModel.call(prompt)               -> 发起合成，返回 TextToSpeechResponse
 *   response.getResult().getOutput()       -> 取出音频字节 byte[]
 *
 * 【重点】TextToSpeechPrompt / TextToSpeechResponse 来自 org.springframework.ai.audio.tts.*（通用）；
 *   DashScopeAudioSpeechModel 是 DashScope 的实现，来自 com.alibaba.cloud.ai.dashscope.audio.tts.*。
 *
 * 【关于 ASR（语音转文字）】见本模块 README——为保证跨版本编译稳定，代码只演示 TTS。
 * ============================================================================
 */
@Component
public class AudioRunner implements CommandLineRunner {

    /** TTS 模型：由 starter 自动配置（底层通义 cosyvoice-v1 + 音色 longxiaochun）。 */
    private final DashScopeAudioSpeechModel speechModel;

    public AudioRunner(DashScopeAudioSpeechModel speechModel) {
        this.speechModel = speechModel;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块14：语音合成 TTS（文字转语音）==========\n");

        String text = "你好，欢迎学习 Spring AI Alibaba";
        System.out.println("【要合成的文字】" + text);
        System.out.println("（正在请求通义语音合成，请稍候……）");

        try {
            // ★核心：把文字交给 TTS 模型，拿回音频字节
            TextToSpeechResponse response = speechModel.call(new TextToSpeechPrompt(text));

            // 判空兜底：无 Key / 无权限 / 欠费 等情况下响应可能为空，避免 NPE 崩溃
            byte[] audio = null;
            if (response != null && response.getResult() != null) {
                audio = response.getResult().getOutput();
            }

            if (audio == null || audio.length == 0) {
                System.out.println("\n【结果】未拿到音频字节。请检查：是否已在百炼开通【语音合成 cosyvoice】权限、Key 是否正确、账户是否有额度。");
            } else {
                // 输出到【本模块目录下】的 generated-output/hello.mp3（该目录已被 .gitignore 忽略）
                Path outDir = Paths.get("generated-output");
                Files.createDirectories(outDir);                 // 目录不存在则创建
                Path outFile = outDir.resolve("hello.mp3");
                Files.write(outFile, audio);                     // 把字节写成 mp3 文件

                System.out.println("\n【合成成功】音频已保存：");
                System.out.println("  文件：" + outFile.toAbsolutePath());
                System.out.println("  大小：" + audio.length + " 字节");
                System.out.println("  用系统播放器打开这个 mp3 就能听到语音了。");
            }
        } catch (Exception e) {
            // 兜底：把异常打印成友好提示，不让程序以堆栈崩溃收场
            System.out.println("\n【调用出错】" + e.getMessage());
            System.out.println("常见原因：未开通语音合成权限 / API Key 未配置或无效 / 账户额度不足。");
        }

        System.out.println("\n提示：反方向的【ASR 语音转文字】用法见本模块 README（注入 DashScopeAudioTranscriptionModel）。");
        System.out.println("\n========== 演示结束：你已把文字合成为语音 ==========\n");
    }
}
