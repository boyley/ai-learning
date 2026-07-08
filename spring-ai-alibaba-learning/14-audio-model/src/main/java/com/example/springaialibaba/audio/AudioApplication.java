package com.example.springaialibaba.audio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 14：语音（TTS 文字转语音 / ASR 语音转文字）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   多模态的“声音”篇。语音有两个方向，正好互为逆过程：
 *     - TTS（Text-To-Speech，文字转语音）：把文字读成一段音频。本模块【动手演示】。
 *     - ASR（Automatic Speech Recognition，语音转文字）：把音频听写成文字。本模块在
 *       README 里用代码片段 + 文字讲解（不同版本 API 差异较大，为保证编译稳定，代码只演示 TTS）。
 *
 * 【需要先懂的几个概念】
 *   1. TTS 模型：本项目用通义 cosyvoice-v1 + 音色 longxiaochun（共享配置里已设好）。
 *   2. DashScopeAudioSpeechModel：DashScope 的 TTS 模型实现，
 *      它实现了 Spring AI 的 org.springframework.ai.audio.tts.TextToSpeechModel 接口，
 *      由 starter 自动配置成 Bean，可直接注入。
 *   3. 合成结果是【音频字节 byte[]】，我们把它写成 generated-output/hello.mp3。
 *
 * 【★ 使用前提（重要）】
 *   语音合成/识别属于“需单独开通”的能力：请到百炼(DashScope)控制台确认已开通对应
 *   语音模型（cosyvoice / paraformer）的调用权限，且账户有额度，否则调用会报错。
 *
 * 【怎么做】
 *   容器启动 → 自动配置 DashScopeAudioSpeechModel → AudioRunner 里
 *   speechModel.call(new TextToSpeechPrompt("你好，欢迎学习")) → 取字节 → 写 mp3 文件。
 * ============================================================================
 */
@SpringBootApplication
public class AudioApplication {

    public static void main(String[] args) {
        SpringApplication.run(AudioApplication.class, args);
    }
}
