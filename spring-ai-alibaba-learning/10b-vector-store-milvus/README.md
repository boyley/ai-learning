# 10b · VectorStore · Milvus 版（Spring AI Alibaba）

> 模块 [10-vector-store](../10-vector-store) 用内存版 `SimpleVectorStore`；本模块换成生产级 **Milvus**。因为 Spring AI Alibaba **复用 Spring AI 的 `VectorStore` 抽象**，直接用官方 `spring-ai-starter-vector-store-milvus`，写法和纯 Spring AI 一模一样。

## 一、和内存版 / 纯 Spring AI 的区别

| | 内存版(10) | 本模块(10b) |
|---|---|---|
| 向量库 | `SimpleVectorStore`（内存） | `MilvusVectorStore`（持久化） |
| embedding | DashScope text-embedding-v3 | 同（**1024 维**） |
| 依赖 | dashscope starter | + `spring-ai-starter-vector-store-milvus` |
| 业务代码 | `add()/similaritySearch()` | 不变 |

> ⚠️ **维度差异**：DashScope `text-embedding-v3` 是 **1024 维**，纯 Spring AI 用的 OpenAI `text-embedding-3-small` 是 **1536 维**。`embedding-dimension` 必须对应填对，否则插入报维度错误。

## 二、先启动 Milvus

```bash
cd ../../../vector-db-learning/01-milvus
docker compose up -d
```

部署详解见 [`vector-db-learning/01-milvus`](../../../vector-db-learning/01-milvus)。

## 三、运行

```bash
cd 10b-vector-store-milvus
mvn spring-boot:run
```

需要 DashScope（百炼）Key，已在 `../config/spring-ai-alibaba-common.yml`。运行会写入 4 条文档并语义检索 Top-3。

> 没启动 Milvus 时启动会连接失败（正常，DB demo 必须先有 DB）；`mvn compile` 不受影响。

## 四、关键配置

```yaml
spring:
  ai:
    vectorstore:
      milvus:
        client: { host: localhost, port: 19530 }
        collection-name: saa_milvus_demo
        embedding-dimension: 1024     # ★ DashScope text-embedding-v3 = 1024
        index-type: HNSW
        metric-type: COSINE
        initialize-schema: true
```

---

> 🔗 Milvus 部署/概念/面试 → [`vector-db-learning/01-milvus`](../../../vector-db-learning/01-milvus)；纯 Spring AI 版 → [`../../spring-ai-learning/10b-vector-store-milvus`](../../spring-ai-learning/10b-vector-store-milvus)。
