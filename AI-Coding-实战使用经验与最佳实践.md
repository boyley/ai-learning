# AI Coding 实战使用经验与最佳实践 🤖⌨️

> 面向**用 AI 工具写生产代码**的开发者（Claude Code / Cursor / Copilot / Windsurf / Kiro / 通义灵码…）。不讲工具广告，讲**真正拉开差距的使用手段**：上下文工程、记录与追溯、Spec 驱动的任务执行、分工并行、质量把关。
> 配套：面试速答见 [`../interview/05-ai/09-ai-coding.md`](../interview/05-ai/09-ai-coding.md)；底层原理（代码 RAG / Agent 循环）见 [`llm-app-learning`](llm-app-learning)。

---

## 🎯 一句话心法

高手用 AI 写代码，拼的不是"会提问"，而是把 AI 当一个**需要喂对上下文、留下记录、按计划推进、还得盯着验收**的团队成员来管。你负责**想清楚要什么 + 把关质量**，AI 负责**快速产出草稿**。

---

## 一、🧠 上下文工程（让 AI 真懂你的项目）

AI 写错的头号原因是**上下文不足**——它不知道你的项目约定，只能按"通用写法"猜。上下文工程就是系统性地喂对信息。

### 1. 规则文件（一次配置，长期生效）

在项目根放一个规则文件，工具每次自动加载，免得反复交代：

| 工具 | 规则文件 |
|---|---|
| Claude Code | `CLAUDE.md`（可分层：家目录 / 项目 / 子目录） |
| Cursor | `.cursorrules` 或 `.cursor/rules/*.mdc`（可按目录/文件类型生效） |
| GitHub Copilot | `.github/copilot-instructions.md` |
| 通用约定 | `AGENTS.md` |

**该写什么**（写"约定"，不写"废话"）：
```markdown
# 项目约定
- 技术栈：Spring Boot 3.5 + Java 17 + MyBatis-Plus + MySQL 8
- 分层：controller → service → mapper，DTO 用 record，禁止 service 层直接返回实体
- 命名：REST 路径 kebab-case，方法名动词开头
- 测试：改动必须带 JUnit5 单测，用 AssertJ 断言
- 禁忌：不准改 `legacy/` 目录；不准引入新依赖前先问
```

### 2. 精准喂上下文（别让它瞎搜）

- **主动指明相关文件/符号**：Cursor 的 `@Files`/`@Code`，Claude Code 直接给路径让它 Read——比让它自己 grep 半天更准更省。
- **给"范例"**：改造一个模块时，把一个**已有的类似实现**贴给它当模板，产出风格立刻对齐。
- **说清约束**：用哪个库、不准动什么、边界条件。

### 3. 上下文卫生

- **长会话及时 `/clear`**：一个任务做完就清，避免上一个任务的上下文污染下一个。
- **子代理隔离**：把"调研/搜索整个代码库"丢给子代理，主线只拿回结论，省主上下文（大项目尤其关键）。
- **警惕上下文窗口**：超大改动分批做，别指望它一次"看全"几十个文件。

---

## 二、📝 记录与可追溯（跨会话不失忆、改动能回溯）

> "有记录什么的吗"——有，而且是分层的。这是把 AI 从"一次性工具"升级成"工程流程一环"的关键。

| 层 | 记录什么 | 载体 / 手段 |
|---|---|---|
| **长期记忆** | 项目约定、个人偏好、踩过的坑 | Claude Code `memory/` + `MEMORY.md`、Cursor Memories、Kiro Steering |
| **会话历史** | 每轮对话、工具调用、可续接 | transcript 日志（`.jsonl`），`--resume` 接着上次干 |
| **执行轨迹** | 子任务/agent 的产出与返回 | workflow journal、agent 输出文件 |
| **变更留痕** | 谁改了什么、为什么 | Git commit（AI 参与带 `Co-Authored-By`）、PR 描述、CHANGELOG |
| **决策记录** | 为什么这么设计（架构权衡） | ADR（Architecture Decision Record）、design 文档 |

**实践要点：**
- **小步提交**，commit message 写清"做了什么 + 为什么"，出错 `git revert` 即可回滚。
- **AI 参与的提交标注出来**（`Co-Authored-By`），责任和溯源都清晰。
- **关键决策落文档**：换人或隔几个月后，顺着 commit + spec + memory 能还原"当时怎么想的、做到哪了"。

---

## 三、📋 Spec 驱动与任务执行追踪（"按工作执行、执行到哪了"）

Agentic 工具最大的痛点是**长任务跑着跑着就跑偏**。解法是把"一句话大需求"拆成**可审阅、可追踪、可中断续接**的执行流。

### 三种粒度（从轻到重）

| 形态 | 怎么工作 | 适用 | 代表 |
|---|---|---|---|
| **TODO 列表** | 会话内维护任务表，实时标 in-progress/done | 中等任务 | Claude Code TodoWrite、Cursor to-dos |
| **Plan 模式** | AI 先出计划（涉及哪些文件/分几步/风险）给你审批，批了再动手 | 有风险的改动 | Claude Code Plan Mode、Cursor Plan |
| **Spec 驱动** ⭐ | 落三份文档，AI 照任务清单逐条实现并勾选 | 大功能/新模块 | **Kiro specs**、GitHub **Spec Kit** |

### Spec 驱动开发标准流程

```
① requirements.md   用户故事 + 验收标准（EARS 语法：WHEN…THE SYSTEM SHALL…）
② design.md         架构 / 接口 / 数据模型 / 时序图 / 技术选型
③ tasks.md          [x] 1. 建表迁移
                    [x] 2. 写 Repository + 单测
                    [ ] 3. 写 Service 业务逻辑   ← 正在做
                    [ ] 4. 写 Controller + 集成测试
                    [ ] 5. 加权限校验
                            ↓ 每完成一条：跑测试 → commit → 把 [ ] 变 [x]
```

**为什么有效：**
- **不跑偏**：AI 始终对着 tasks 干，不会自由发挥。
- **进度透明**：随时看 tasks.md 就知道"执行到第几步、还剩哪些"。
- **可中断续接**：今天做到第 3 步，明天接着从第 4 步开始，spec 就是"进度存档"。
- **可审阅**：需求和设计先确认再写码，避免"写完才发现方向错"。

> 本质是把敏捷的"需求拆解 + 增量交付"套在 AI 上。GitHub Spec Kit 的 `/specify → /plan → /tasks` 三条命令就是这套流程的产品化。

---

## 四、⚙️ 分工·并行·能力扩展

| 手段 | 干什么 | 场景 |
|---|---|---|
| **子代理 / 多智能体** | 并行跑调研、审查、批量改造 | 一次改几十处、多角度审查 |
| **工作流编排** | 确定性的"扫描→修改→验证"流水线 | 大规模迁移、批量重构 |
| **MCP（Model Context Protocol）** | 标准化接外部能力：数据库、Jira、浏览器、内部系统 | 让 AI 读真实数据/操作真实系统 |
| **Hooks** | 提交前自动 lint/测试、自定义斜杠命令 | 把规范自动化，如"每轮结束自动 commit+push" |
| **git worktree 隔离** | 每个任务独立工作副本，并行改同一仓库不冲突 | 多任务/多分支并行 |

> MCP 是近两年最重要的扩展：过去每接一个工具都要定制集成，MCP 把它标准化成"即插即用"，AI 编程工具能统一接入。详见 [`../interview/05-ai/04-function-calling-mcp.md`](../interview/05-ai/04-function-calling-mcp.md)。

---

## 五、✅ 质量与安全把关（AI 提效不改变"谁提交谁负责"）

### 质量
- **让 AI 自己跑测试/编译**，而不是"看起来对就交"——agentic 工具能自己看报错迭代。
- **对抗式审查**：另开一个 agent 专门挑毛病、验证 bug 是否真实存在。
- **人审每个 diff**：把 AI 当"手很快的初级同事"，产出必看、必测；核心业务逻辑人定夺。
- **小步 diff**：一次一个明确任务，diff 小才 review 得动。

### 安全与合规
- **别把密钥/私有代码/客户数据喂云端模型** → 用企业版/私有化部署、脱敏、密钥隔离（`.gitignore`）。
- **AI 代码过安全扫描**（SAST）：可能生成注入/越权/硬编码密钥的代码。
- **许可证污染**：可能复现受限（GPL）代码 → 用带过滤/溯源的企业版，关键代码人工确认。
- **不可逆/涉钱操作**：AI 起草，人必须过目才执行。

---

## 六、🗺️ 一套可复制的工作流（把上面串起来）

```
1. 配规则文件（CLAUDE.md/.cursorrules）  → 一次性，让 AI 懂项目
2. 大需求先 Spec：requirements → design → tasks（审批）
3. 按 tasks 逐条执行：
     喂相关文件/范例 → AI 实现一条 → 跑测试 → review diff → commit → 勾选
4. 卡住/调研 → 派子代理，主线不被污染
5. 需要真实数据/外部系统 → 接 MCP
6. 关键决策 → 记 ADR / 更新 design；踩坑 → 写进 memory
7. 收尾 → 人审全量 diff + 安全扫描 → PR（描述讲清 what/why）
```

---

## 七、📋 自查清单（你是"深度用户"还是"浅尝"）

- [ ] 项目里有规则文件（CLAUDE.md/.cursorrules），固化了技术栈和约定
- [ ] 大需求会先出 spec/plan 再让 AI 动手，而不是一句话闷头写
- [ ] 能随时说清"这个任务 AI 执行到第几步、还剩什么"
- [ ] AI 的改动都小步 commit、可回滚、message 讲清 why
- [ ] 让 AI 跑测试自验，且我 review 每个 diff
- [ ] 用过子代理/MCP 之一（并行调研 或 接外部系统）
- [ ] 清楚哪些代码不该喂云端模型（密钥/涉密/核心）

> 勾满 5 个以上，你已经是把 AI 纳入工程流程的深度用户，而不只是"用它补全"。

---

## 🔗 延伸

- 面试速答（"你怎么用 AI 提效"话术）→ [`../interview/05-ai/09-ai-coding.md`](../interview/05-ai/09-ai-coding.md)
- 代码 RAG / Codebase Indexing 原理 → [`../interview/05-ai/03-rag.md`](../interview/05-ai/03-rag.md)
- Agent / ReAct 循环（agentic coding 的底层）→ [`../interview/05-ai/05-agent.md`](../interview/05-ai/05-agent.md)
- MCP 协议 → [`../interview/05-ai/04-function-calling-mcp.md`](../interview/05-ai/04-function-calling-mcp.md)
- AI 工程化落地（缓存/限流/可观测/成本）→ [`../interview/05-ai/06-ai-engineering.md`](../interview/05-ai/06-ai-engineering.md)
