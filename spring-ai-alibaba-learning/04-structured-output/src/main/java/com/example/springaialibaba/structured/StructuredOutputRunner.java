package com.example.springaialibaba.structured;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ============================================================================
 * 模块 04 演示：结构化输出（把回答转成 Java 对象 / List）
 * ============================================================================
 *
 * 【为什么需要结构化输出】
 *   业务代码想要的是 book.title()、book.year() 这样能直接取字段的对象，
 *   而不是一坨需要自己解析的文字。结构化输出就是让 AI 的回答“落地成对象”。
 *
 * 【核心 API：把 .content() 换成 .entity(...)】
 *
 *   .call().content()                              -> 返回 String（原始文字）
 *   .call().entity(Book.class)                     -> 返回一个 Book 对象
 *   .call().entity(new ParameterizedTypeReference<List<Book>>(){})
 *                                                  -> 返回 List<Book>
 *
 *   为什么 List 要用 ParameterizedTypeReference？
 *     因为 Java 泛型在运行时会“擦除”，List<Book> 会退化成 List。
 *     ParameterizedTypeReference 是 Spring 用来“保住泛型信息”的小技巧，
 *     它用一个匿名子类 {} 把 <List<Book>> 这个类型信息保留下来，转换器才知道要转成 List<Book>。
 *
 * 【数据模型】用 record 定义（Java 16+ 的不可变数据类，超适合当 DTO）：
 *   record Book(String title, String author, int year)
 * ============================================================================
 */
@Component
public class StructuredOutputRunner implements CommandLineRunner {

    private final ChatClient chatClient;

    public StructuredOutputRunner(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 用 record 定义一本书的数据结构。
     *   - record 会自动生成构造器、getter(名字就是字段名，如 title())、equals/hashCode/toString。
     *   - Spring AI 会读取这些字段名，生成“请按 {title, author, year} 的 JSON 返回”的指令。
     */
    public record Book(String title, String author, int year) {
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块04：结构化输出（回答直接变对象）==========\n");

        // ---------------- 演示1：转成单个对象 Book ----------------
        System.out.println("---------- 演示1：.entity(Book.class) 把回答转成一个 Java 对象 ----------");
        String q1 = "推荐一本 Java 编程经典书，给出书名、作者、出版年份。";
        System.out.println("【我问】" + q1);
        // 注意结尾不是 .content()，而是 .entity(Book.class)：直接拿到 Book 对象！
        Book book = chatClient
                .prompt()
                .user(q1)
                .call()
                .entity(Book.class);   // ★ 自动把模型返回的 JSON 转成 Book
        // 现在可以像操作普通对象一样访问字段
        System.out.println("【拿到 Java 对象】" + book);
        System.out.println("  书名 title  = " + book.title());
        System.out.println("  作者 author = " + book.author());
        System.out.println("  年份 year   = " + book.year() + "\n");

        // ---------------- 演示2：转成对象列表 List<Book> ----------------
        System.out.println("---------- 演示2：.entity(ParameterizedTypeReference<List<Book>>) 转成列表 ----------");
        String q2 = "推荐 3 本适合编程入门的书，分别给出书名、作者、出版年份。";
        System.out.println("【我问】" + q2);
        // List<Book> 因泛型擦除，必须用 ParameterizedTypeReference 保住 <List<Book>> 类型信息
        List<Book> books = chatClient
                .prompt()
                .user(q2)
                .call()
                .entity(new ParameterizedTypeReference<List<Book>>() {});  // ★ 注意结尾的 {} 匿名子类
        System.out.println("【拿到 List<Book>，共 " + books.size() + " 本】");
        for (int i = 0; i < books.size(); i++) {
            Book b = books.get(i);
            System.out.println("  " + (i + 1) + ". 《" + b.title() + "》 - " + b.author() + " (" + b.year() + ")");
        }
        System.out.println();

        System.out.println("========== 演示结束：AI 回答已直接变成可用的 Java 对象/列表 ==========\n");
    }
}
