package com.youzm.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

/**
 * @author:youzhiming
 * @date: 2026/4/8
 * @description: AI 工具服务示例
 */
@Service
public class ToolService {

    /**
     * 这是一个工具方法，AI 可以调用它来查询天气
     * description 属性非常重要，它会告诉 AI 这个方法是干什么的
     */
    @Tool(description = "获取指定城市的当前天气情况")
    public String getWeather(String city) {
        // 在实际项目中，这里会调用真实的天气 API
        System.out.println("AI 调用了 getWeather 工具，城市: " + city);
        return city + " 今天天气晴朗，气温 25°C";
    }

    /**
     * 获取当前时间
     */
    @Tool(description = "获取当前的北京时间")
    public String getCurrentTime() {
        System.out.println("AI 调用了 getCurrentTime 工具");
        return java.time.LocalDateTime.now().toString();
    }

    /**
     * 简单的计算器
     */
    @Tool(description = "执行两个数字的加法运算")
    public double add(double a, double b) {
        System.out.println("AI 调用了 add 工具: " + a + " + " + b);
        return a + b;
    }
}
