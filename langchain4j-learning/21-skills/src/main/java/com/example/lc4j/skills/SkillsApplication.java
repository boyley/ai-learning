package com.example.lc4j.skills;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 21：Skills（技能）—— 启动类
 * ============================================================================
 *
 * 【Skills 是什么】
 *   Skills 是 LangChain4j 较新的特性（artifact: langchain4j-skills）。
 *   一个「技能(Skill)」= 一整套「教模型怎么做某件事」的打包知识，包含：
 *     · name        技能名（简短标识）
 *     · description  一句话描述（告诉模型「这个技能能干什么、何时该用」）
 *     · content      技能正文（详细的操作指南/规则/模板，可以很长，通常是 Markdown）
 *     · 可选：resources（附带的文件资源）、tools/toolProviders（技能专属的工具）
 *
 * 【解决什么问题：渐进式披露(progressive disclosure)】
 *   如果把所有详细指南都塞进系统提示词，会非常占 token，且很多内容当前问题根本用不上。
 *   Skills 的做法是：
 *     1. 平时只把每个技能的「简短描述」放进上下文（很省 token）。
 *     2. langchain4j-skills 会自动给模型注册一个「激活技能(activate skill)」工具。
 *     3. 当模型判断某技能与当前任务相关时，调用该工具「激活」它——
 *        此时技能的【完整 content】才被注入上下文，模型据此按规范完成任务。
 *   即：详细说明书「按需加载」，而不是「全程常驻」。
 *
 * 【与普通 Tools（模块 08）的区别】
 *   - 普通 @Tool：是一个「可执行的函数」，模型调用它去【做一个动作】（查库、算数、发请求…），
 *     返回的是「执行结果」。
 *   - Skill：本质是「按需加载的一段【指令/知识】」，模型激活它是为了【获得怎么做的指导】，
 *     返回的是「指南文本」（技能还可以顺带捆绑自己的工具）。
 *   一句话：Tool 给模型「手脚」，Skill 给模型「说明书」。
 *
 * 【这个模块怎么做】
 *   1. 用 Skill.builder() 定义一个技能（一份「周报写作规范」指南）。
 *   2. 用 Skills.from(skill) 聚合，并取出它提供的 ToolProvider。
 *   3. 用 AiServices.builder(...).toolProvider(skills.toolProvider()) 把技能装到一个 AI Service 上。
 *   4. 提问时，模型会先激活该技能、读到规范，再按规范产出周报。
 *
 * 【运行说明】本项目只要求【编译通过】。真正运行需有效的 DeepSeek Key（见 README）。
 * ============================================================================
 */
@SpringBootApplication
public class SkillsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SkillsApplication.class, args);
    }
}
