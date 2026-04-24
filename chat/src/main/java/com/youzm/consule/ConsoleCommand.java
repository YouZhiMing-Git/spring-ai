package com.youzm.consule;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Scanner;

/**
 * @author:youzhiming
 * @date: 2026/4/8
 * @description: 控制台对话命令 - 支持多模型选择和连续对话
 */
public class ConsoleCommand extends Thread {

    private final Map<String, ChatModel> modelMap;
    private final Scanner scanner;
    private volatile boolean running = true;

    public ConsoleCommand(Map<String, ChatModel> modelMap) throws UnsupportedEncodingException {
        this.modelMap = modelMap;
        // 设置Scanner使用UTF-8编码读取输入
        this.scanner = new Scanner(System.in, "UTF-8");
    }

    @Override
    public void run() {
        System.out.println("========================================");
        System.out.println("   Spring AI 控制台对话系统");
        System.out.println("========================================");
        System.out.println();

        // 显示可用模型列表
        displayAvailableModels();

        // 选择模型
        ChatModel selectedModel = selectModel();
        if (selectedModel == null) {
            System.out.println("未选择有效模型，程序退出。");
            return;
        }

        System.out.println("\n✓ 已选择模型，开始对话（输入 'quit' 或 'exit' 退出，输入 'help' 查看帮助）");
        System.out.println("----------------------------------------");

        // 开始连续对话
        startChat(selectedModel);
    }

    /**
     * 显示可用的模型列表
     */
    private void displayAvailableModels() {
        System.out.println("可用模型列表：");
        int index = 1;
        for (String modelName : modelMap.keySet()) {
            System.out.println(index + ". " + getModelDisplayName(modelName));
            index++;
        }
        System.out.println();
    }

    /**
     * 获取模型的显示名称
     */
    private String getModelDisplayName(String modelName) {
        switch (modelName) {
            case "zhipuAssistantModel":
                return "智谱AI - glm-4 (助手模式)";
            case "zhipuCreativeModel":
                return "智谱AI - glm-5.1 (创意写作)";
            case "deepseekModel":
                return "DeepSeek - deepseek-chat (支持工具调用: 天气/时间/计算器)";
            default:
                return modelName;
        }
    }

    /**
     * 让用户选择模型
     */
    private ChatModel selectModel() {
        while (running) {
            System.out.print("请选择模型（输入编号）：");
            String input = scanner.nextLine().trim();

            try {
                int choice = Integer.parseInt(input);
                if (choice < 1 || choice > modelMap.size()) {
                    System.out.println("❌ 无效的选择，请输入 1-" + modelMap.size() + " 之间的数字");
                    continue;
                }

                String[] modelNames = modelMap.keySet().toArray(new String[0]);
                String selectedModelName = modelNames[choice - 1];
                System.out.println("✓ 您选择了：" + getModelDisplayName(selectedModelName));
                return modelMap.get(selectedModelName);
            } catch (NumberFormatException e) {
                System.out.println("❌ 请输入有效的数字");
            }
        }
        return null;
    }

    /**
     * 开始连续对话
     */
    private void startChat(ChatModel chatModel) {
        while (running) {
            System.out.print("\n你：");
            String userInput = scanner.nextLine().trim();

            if (userInput.isEmpty()) {
                continue;
            }

            // 处理特殊命令
            if (handleCommands(userInput)) {
                continue;
            }

            // 发送消息并获取回复
            try {
                System.out.print("AI：");
                ChatResponse response = chatModel.call(new Prompt(userInput));
                System.out.println(response.getResult().getOutput().getText());
                System.out.println("----------------------------------------");
            } catch (Exception e) {
                System.err.println("\n❌ 调用AI模型失败：" + e.getMessage());
                System.out.println("----------------------------------------");
            }
        }
    }

    /**
     * 处理特殊命令
     *
     * @return true 如果执行了命令，false 如果不是命令
     */
    private boolean handleCommands(String input) {
        String lowerInput = input.toLowerCase();

        switch (lowerInput) {
            case "quit":
            case "exit":
            case "q":
                System.out.println("\n感谢使用，再见！");
                running = false;
                scanner.close();
                return true;

            case "help":
            case "h":
            case "？":
                showHelp();
                return true;
            case "/choose":
                displayAvailableModels();
                return true;
            default:
                return false;
        }
    }

    /**
     * 显示帮助信息
     */
    private void showHelp() {
        System.out.println("\n========== 帮助信息 ==========");
        System.out.println("直接输入消息即可与AI对话");
        System.out.println("\nDeepSeek 模型支持的工具调用示例：");
        System.out.println("  - 北京天气怎么样？");
        System.out.println("  - 现在几点了？");
        System.out.println("  - 计算 123 + 456 等于多少？");
        System.out.println("\n特殊命令：");
        System.out.println("  quit / exit / q  - 退出程序");
        System.out.println("  help / h / ？     - 显示此帮助信息");
        System.out.println("==============================\n");
    }
}
