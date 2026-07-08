package com.example.springaialibaba.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ============================================================================
 * 一个“工具类”：里面的方法用 @Tool 标记后，就变成模型可调用的“工具”
 * ============================================================================
 *
 * 【要点】
 *   - 普通 Java 方法 + @Tool 注解 = 一个工具。description 一定要写清楚这个工具能干嘛，
 *     模型正是靠这段中文描述来判断“该不该、什么时候”调用它。
 *   - 方法参数用 @ToolParam(description="...") 说明每个参数的含义，模型据此填参。
 *   - 方法返回值(String/数字/对象等)会被回填给模型，作为它继续作答的依据。
 * ============================================================================
 */
public class DateTimeTools {

    /**
     * 工具1：获取当前日期时间（无参数）。
     * 模型看到用户问“现在几点/今天几号”这类问题时，会自动调用它。
     */
    @Tool(description = "获取用户所在时区的当前日期和时间")
    public String getCurrentDateTime() {
        String now = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        // 打印一行，方便你在控制台直观看到“工具确实被模型调用了”
        System.out.println("  [🔧 工具被调用] getCurrentDateTime() -> " + now);
        return now;
    }

    /**
     * 工具2：带参数的工具——计算某人的出生年份。
     * @param age 年龄，由模型从用户问题中解析后填入。
     */
    @Tool(description = "根据一个人的年龄，推算其出生年份")
    public int birthYearOf(
            @ToolParam(description = "此人的年龄（整数岁）") int age) {
        int year = LocalDate.now().getYear() - age;
        System.out.println("  [🔧 工具被调用] birthYearOf(age=" + age + ") -> " + year);
        return year;
    }
}
