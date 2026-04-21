package com.youzm;

import com.youzm.consule.ConsoleCommand;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author:youzhiming
 * @date: 2026/4/8
 * @description: Spring AI 应用启动类 - 支持Web和控制台双模式
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // 强制设置控制台编码为UTF-8（解决Windows IDEA控制台中文乱码问题）
        System.setOut(new PrintStream(System.out, true, "UTF-8"));
        System.setErr(new PrintStream(System.err, true, "UTF-8"));
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.stdout.encoding", "UTF-8");
        System.setProperty("sun.stderr.encoding", "UTF-8");

        SpringApplication.run(Application.class, args);

    }

    public  void initCommand() {
        // 获取Spring应用上下文


        // 获取所有ChatModel Bean
//        Map<String, ChatModel> modelMap = new HashMap<>();
//        modelMap.put("zhipuAssistantModel", context.getBean("zhipuAssistantModel", ChatModel.class));
//        modelMap.put("zhipuCreativeModel", context.getBean("zhipuCreativeModel", ChatModel.class));
//        modelMap.put("deepseekModel", context.getBean("deepseekModel", ChatModel.class));
//
//        // 启动控制台对话线程
//        try {
//            ConsoleCommand consoleCommand = new ConsoleCommand(modelMap);
//            consoleCommand.start();
//        } catch (UnsupportedEncodingException e) {
//            System.err.println("Failed to initialize console: " + e.getMessage());
//            return;
//        }
//
//        System.out.println("\n✓ Web服务已启动，访问 http://localhost:8999");
//        System.out.println("✓ 控制台对话系统已就绪\n");
    }

}
