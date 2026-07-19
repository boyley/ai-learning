# 09b · Milvus EmbeddingStore（LangChain4j）

> 模块 [09-embeddings-and-stores](../09-embeddings-and-stores) 用内存版 `InMemoryEmbeddingStore`；本模块换成生产级 **Milvus**（`MilvusEmbeddingStore`）。上层向量化/检索代码几乎不变——体现 LangChain4j `EmbeddingStore` 接口"底层向量库可插拔"。

## 一、和内存版的唯一区别

```java
// 模块 09（内存）：
EmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();

// 本模块 09b（Milvus）：只换这一处
EmbeddingStore<TextSegment> store = MilvusEmbeddingStore.builder()
        .host("localhost").port(19530)
        .collectionName("lc4j_milvus_demo")
        .dimension(1536)          // ★ 必须与 embedding 模型维度一致
        .build();
```

其余 `embeddingModel.embed(...)` / `store.add(...)` / `store.search(...)` 一字未改。

## 二、先启动 Milvus

```bash
cd ../../../vector-db-learning/01-milvus
docker compose up -d
```

部署详解见 [`vector-db-learning/01-milvus`](../../../vector-db-learning/01-milvus)。

## 三、运行

```bash
cd 09b-milvus-store
mvn spring-boot:run
```

需要 OpenAI Key（embedding），已在 `../config/langchain4j-common.yml`。运行会入库 4 条并语义检索 Top-2。

> 没启动 Milvus 时 `MilvusEmbeddingStore.builder().build()` 会连接失败，代码已 try/catch 给友好提示，不崩溃；`mvn compile` 不受影响。

## 四、易错点

- ⚠️ **`dimension` 必须与 embedding 模型一致**（OpenAI `text-embedding-3-small`=1536）。
- ⚠️ **换 embedding 模型要重建 collection**（向量空间不通用）。
- ⚠️ `MilvusEmbeddingStore.builder()` 还可配 `uri`/`token`/`metricType`/`consistencyLevel` 等；生产按需设。

---

> 🔗 Milvus 部署/概念/面试 → [`vector-db-learning/01-milvus`](../../../vector-db-learning/01-milvus)；Spring AI 版 → [`../../spring-ai-learning/10b-vector-store-milvus`](../../spring-ai-learning/10b-vector-store-milvus)。
