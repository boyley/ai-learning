package com.example.springai.vectorstore.milvus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 10b：VectorStore · Milvus 版 —— 启动类
 * ============================================================================
 *
 * 【和模块 10（内存版 SimpleVectorStore）的区别】
 *   - 模块 10 用 SimpleVectorStore：数据只在内存，程序退出即丢，扛不了量，仅供学习。
 *   - 本模块用 MilvusVectorStore：数据持久化到 Milvus（生产级向量库），可扩展、能上生产。
 *   - ★ 关键：业务代码几乎一模一样——都是 @Autowired VectorStore + add()/similaritySearch()。
 *     只是换了依赖(spring-ai-starter-vector-store-milvus)和 application.yml 配置。
 *     这就是"面向 VectorStore 接口编程、底层向量库可插拔"的价值。
 *
 * 【运行前提】必须先启动 Milvus（见 README / vector-db-learning/01-milvus/docker-compose.yml）。
 *   否则启动时自动配置 MilvusVectorStore Bean 会连接失败（Connection refused）。
 * ============================================================================
 */
@SpringBootApplication
public class VectorStoreMilvusApplication {
    public static void main(String[] args) {
        SpringApplication.run(VectorStoreMilvusApplication.class, args);
    }
}
