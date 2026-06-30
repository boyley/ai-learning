package com.example.lc4j.structured;

import dev.langchain4j.model.output.structured.Description;

import java.util.List;

/**
 * ============================================================================
 * 结构化输出的目标 POJO：从一段自然语言里抽取出的“人物信息”
 * ============================================================================
 *
 * 【它是怎么被用到的】
 *   当 AiService 接口方法声明返回 Person 时，LangChain4j 会：
 *   1) 读取这个类的字段（含下面 @Description 的说明）生成一份 JSON schema；
 *   2) 把 schema 塞进提示词，要求模型“按这个 JSON 结构作答”；
 *   3) 把模型返回的 JSON 自动转成一个 Person 实例交给你。
 *
 * 【@Description 的作用】
 *   给字段写一句“这是什么”，会一并发给模型，相当于给模型的填空提示，
 *   能显著提升抽取的准确率（尤其是含糊字段，如年龄要数字、爱好要列表）。
 *
 * 注：这里用 public 字段 + 无参构造，方便框架反序列化；真实项目也可用标准 JavaBean。
 * ============================================================================
 */
public class Person {

    @Description("人物的姓名")
    public String name;

    @Description("人物的年龄，必须是整数")
    public int age;

    @Description("人物所在的城市")
    public String city;

    @Description("人物的兴趣爱好列表")
    public List<String> hobbies;

    /** 无参构造：反序列化需要。 */
    public Person() {
    }

    /** 友好打印：把抽取到的字段一行展示出来。 */
    @Override
    public String toString() {
        return "Person{name='" + name + "', age=" + age
                + ", city='" + city + "', hobbies=" + hobbies + "}";
    }
}
