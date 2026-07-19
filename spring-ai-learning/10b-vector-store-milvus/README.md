# 10b · VectorStore · Milvus 版（生产级向量库）

> 模块 [10-vector-store](../10-vector-store) 用的是**内存版** `SimpleVectorStore`（重启即丢、扛不了量）。本模块把它换成**生产级 Milvus**，展示 Spring AI"底层向量库可插拔"——业务代码几乎不动，只换依赖和配置。

## 一、和内存版的区别（核心认知）

| | 模块 10（内存版） | 本模块 10b（Milvus） |
|---|---|---|
| 实现 | `SimpleVectorStore` | `MilvusVectorStore`（自动配置） |
| 数据 | 只在内存，退出即丢 | 持久化到 Milvus，可扩展 |
| 依赖 | `spring-ai-vector-store` | `spring-ai-starter-vector-store-milvus` |
| 业务代码 | `vectorStore.add()/similaritySearch()` | **一字未改** |

> ★ 关键：`@Autowired VectorStore` 注入的是**接口**，换 starter 就换了底层实现，`add`/`similaritySearch` 代码不动。这就是选型 [向量库合集](../../../vector-db-learning) 里各库能平滑切换的原因。

## 二、先启动 Milvus（必须）

本模块要连真实 Milvus，先用 Docker 起一个（compose 已备好）：

```bash
cd ../../../vector-db-learning/01-milvus
docker compose up -d          # 起 etcd + MinIO + Milvus + Attu
docker compose ps             # 等 milvus-standalone 变 healthy（约 30~90s）
```

> Milvus 部署详解见 [`vector-db-learning/01-milvus`](../../../vector-db-learning/01-milvus)。

## 三、运行

```bash
cd 10b-vector-store-milvus
mvn spring-boot:run
```

需要 OpenAI Key（做 embedding，已在 `../config/spring-ai-common.yml`）。运行后会：写入 4 条文档 → 语义检索 Top-3 → 演示 `category='db'` 元数据过滤检索。

> ⚠️ **没启动 Milvus 会怎样**：应用启动时创建 `MilvusVectorStore` Bean 会连接失败（Connection refused）。这是正常的——数据库 demo 必须先有数据库。`mvn compile` 不受影响（编译不连库）。

## 四、关键配置（application.yml）

```yaml
spring:
  ai:
    vectorstore:
      milvus:
        client: { host: localhost, port: 19530 }   # gRPC
        collection-name: spring_ai_milvus_demo
        embedding-dimension: 1536    # ★ 必须与 embedding 模型维度一致
        index-type: HNSW
        metric-type: COSINE          # 与检索一致
        initialize-schema: true      # 首次自动建 collection + 索引
```

## 五、易错点

- ⚠️ **`embedding-dimension` 必须与 embedding 模型一致**（OpenAI `text-embedding-3-small`=1536）；填错插入即报维度错误。
- ⚠️ **`metric-type` 建索引与检索要一致**（都 COSINE）。
- ⚠️ **换 embedding 模型要重建 collection**（向量空间不通用）。
- ⚠️ Milvus **最终一致**：insert 后不一定立刻可搜（需 flush/load），测试查不到先确认。

---

> 🔗 Milvus 部署/概念/面试 → [`vector-db-learning/01-milvus`](../../../vector-db-learning/01-milvus)；RAG 用向量库 → [11-rag-etl](../11-rag-etl)；向量检索原理 → [`llm-app-learning/12-similarity-ann`](../../llm-app-learning/12-similarity-ann.md)。
