# 24 · Nacos 动态 Prompt（阿里特色）

> 本模块目标：把 **Prompt(提示词) 模板托管到 Nacos 配置中心**，实现“改配置不重启就能更新提示词”。这是频繁调优 Prompt 的线上 AI 应用的利器。

## 一、要懂的核心概念

| 概念 | 大白话解释 |
|---|---|
| **Nacos** | 阿里开源的**配置中心 + 服务发现**组件，能集中管理配置并实时下发变更。 |
| **动态 Prompt** | 把提示词模板放 Nacos，运营在控制台改，应用**热更新**，不用改代码、不用重启。 |
| **PromptTemplate** | Spring AI 的模板渲染器：把 `{占位符}` 替换成实际变量，得到最终提示词。 |
| **配置热更新** | 应用监听 Nacos 配置变更，收到新模板后立即生效。 |

> 写死在代码里的 Prompt：改一次要“改代码→打包→重启”。
> 托管到 Nacos 的 Prompt：改一次只需“在控制台点保存”，应用秒级生效。

## 二、流程图

```mermaid
flowchart LR
    subgraph Nacos[Nacos 配置中心]
      T["Prompt 模板<br/>你是一位{role}..."]
    end
    Ops[运营/产品修改模板] --> T
    T -->|监听变更, 热下发| App[应用]
    App --> R[PromptTemplate 渲染]
    R --> LLM[通义千问回答]
```

## 三、关键代码（渲染机制，本地/ Nacos 通用）

```java
// 模板字符串：Nacos 场景下这段来自配置中心；本地兜底则写在代码里。渲染方式完全一样。
String templateText = "你是一位{role}。请用{style}的风格，回答用户的问题：{question}";

// 同一模板 + 不同变量 → 不同提示词
PromptTemplate pt = new PromptTemplate(templateText);
String finalPrompt = pt.render(Map.of("role","法律顾问","style","严谨","question","签合同注意啥？"));

String answer = chatClient.prompt().user(finalPrompt).call().content();
```

> 本模块用 `ApplicationContext` **探测**有无 Nacos 相关 Bean（零硬编码类名，兼容各版本），
> 探测不到就打印“未连接 Nacos，跳过”，并退回**本地 PromptTemplate** 演示同样的渲染效果。
> 因此**没有 Nacos 环境也能编译、启动、跑完**。

## 四、怎么搭 Nacos 并接入（真实用法）

1. **启动 Nacos**（本地最快用 Docker）：
   ```bash
   docker run -d --name nacos -e MODE=standalone -p 8848:8848 nacos/nacos-server:v2.3.0
   ```
   打开控制台 <http://127.0.0.1:8848/nacos>（默认账号密码 `nacos/nacos`）。

2. **在 Nacos 里新建一个配置**（配置管理 → 新建配置）：
   - `Data ID`：例如 `my-prompt.txt`（或该 starter 约定的 dataId）
   - `Group`：`DEFAULT_GROUP`
   - 配置内容：就是你的 Prompt 模板，例如
     ```
     你是一位{role}。请用{style}的风格，回答用户的问题：{question}
     ```

3. **打开本模块 `application.yml` 里被注释的 Nacos 连接配置**，填好 `server-addr` 等：
   ```yaml
   spring:
     ai:
       alibaba:
         nacos:
           prompt:
             enabled: true
     nacos:
       config:
         server-addr: 127.0.0.1:8848
         group: DEFAULT_GROUP
   ```
   （具体键名/dataId 约定以你引入的 `spring-ai-alibaba-starter-nacos-prompt` 版本文档为准。）

4. 启动应用后，在 Nacos 控制台修改这条配置并保存，应用会**监听到变更并热更新**提示词——全程无需重启。

## 五、怎么运行（无 Nacos 也能跑）

```bash
cd 24-nacos-prompt
mvn spring-boot:run
```

## 六、预期输出（示例，无 Nacos 的兜底演示）

```
========== 模块24：Nacos 动态 Prompt(配置中心托管提示词) ==========

【探测结果】未连接 Nacos，跳过动态拉取；见 README 学习如何搭建 Nacos。

【提示词模板】你是一位{role}。请用{style}的风格，回答用户的问题：{question}

----------------------------------------
【本次变量】{role=严谨的法律顾问, style=正式、谨慎, question=签合同前要注意什么？}
【渲染后的提示词】你是一位严谨的法律顾问。请用正式、谨慎的风格，回答用户的问题：签合同前要注意什么？
【AI 答】签订合同前，建议重点核查以下几点……

----------------------------------------
【本次变量】{role=幽默的旅行博主, style=轻松、有趣, question=第一次去杭州玩，有什么建议？}
【AI 答】哈喽旅行家！第一次来杭州，西湖必须安排……
```

## 七、小结

- Nacos 动态 Prompt = 把提示词放**配置中心**，改配置**不重启**即可热更新，适合频繁调优的线上应用。
- 渲染机制就是 Spring AI 的 `PromptTemplate`：**模板 + 变量 → 最终提示词**，Prompt 来源本地或 Nacos 都一样。
- 本模块用探测 + 本地兜底保证任何环境都能跑通。
- 你已走完 19~24 的**阿里特色 Graph 多智能体与扩展**全部内容！回到 [项目总览 README](../README.md) 复盘整条学习路线，或从 [01-quickstart](../01-quickstart) 重新温习基础。
