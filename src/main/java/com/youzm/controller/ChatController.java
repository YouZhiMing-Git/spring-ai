package com.youzm.controller;

import com.youzm.chatModel.system.SystemPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author:youzhiming
 * @date: 2026/4/8
 * @description: AI聊天控制器（支持智谱AI和DeepSeek）
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    @Qualifier("zhipuAssistantModel")
    private ChatModel zhipuChatModel;


    @Autowired
    @Qualifier("zhipuCreativeModel")
    private ChatModel zhipuCreativeModel;

    @Autowired
    @Qualifier("deepseekModel")
    private ChatModel deepseekChatModel;

    @Autowired
    @Qualifier("zhipuMetaphysicsModel")
    private ChatModel zhipuMetaphysicsModel;

    @Autowired
    private ChatClient chatClient;


    @GetMapping("/zhipu")
    public String chat(@RequestParam String message) {
        return zhipuChatModel.call(message);
    }

    @GetMapping("/zhipuCreative")
    public String chatCreative(@RequestParam String message) {
        return zhipuCreativeModel.call(message);
    }

    //流式接口
    @GetMapping("/deepseek")
    public Flux<String> stream(@RequestParam String message) {
        return deepseekChatModel.stream(message);
    }

    // 同步接口（支持工具调用）
    @GetMapping("/deepseek-sync")
    public String chatWithTools(@RequestParam String message) {
        return deepseekChatModel.call(message);
    }


    // JSON 格式输出
    @GetMapping("/zhipu-json")
    public String chatJson(@RequestParam String message) {
        String jsonPrompt = message +
                "\n\n请以 JSON 格式回答，包含以下字段：\n" +
                "{\n" +
                "  \"summary\": \"简要总结\",\n" +
                "  \"keyPoints\": [\"要点1\", \"要点2\"],\n" +
                "  \"details\": \"详细说明\"\n" +
                "}" +
                "\n\n 注意，直接返回json文本，不要使用json等markdown代码块标记";

        ChatResponse response = zhipuChatModel.call(new Prompt(jsonPrompt));
        return response.getResult().getOutput().getText();
    }

    // Markdown 表格格式
    @GetMapping("/zhipu-table")
    public String chatTable(@RequestParam String message) {
        String tablePrompt = message +
                "\n\n请使用 Markdown 表格格式展示关键信息。";

        ChatResponse response = zhipuChatModel.call(new Prompt(tablePrompt));
        return response.getResult().getOutput().getText();
    }

    @GetMapping("/metaphysics/zhipu")
    public String chatZhipuMetaphysics(@RequestParam String message) {
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(SystemPrompt.ZHIPU_METAPHYSICS_PROMPT),
                new UserMessage(message)
        ));
        ChatResponse response = zhipuMetaphysicsModel.call(prompt);
        return response.getResult().getOutput().getText();
    }

    /**
     * 智能路由接口 - 根据查询内容自动选择最合适的模型
     *
     * <p>此接口通过全局 ChatClient 调用，会自动触发 MultiModelRoutingAdvisor 进行模型路由</p>
     *
     * @param message 用户消息
     * @return AI 响应内容
     */
    @GetMapping("/smart")
    public String smartChat(@RequestParam String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    @PostMapping("/createContent")
    public String createContent(@RequestBody Map<String, Object> body) {

        PromptTemplate promptTemplate = new PromptTemplate("""
                你是一位{role}，请用{style}风格，写一个关于{topic}的{contentType}，要求如下{require}
                """);
        Prompt prompt = promptTemplate.create(body);
        Generation result = zhipuChatModel.call(prompt).getResult();
        return result.getOutput().getText();
    }


    List<Message> history = new ArrayList<>();

    @GetMapping("/chatWithHistory")
    public String chatWithHistory(@RequestParam String msg) {
        history.add(new UserMessage(msg));
        Generation result = zhipuChatModel.call(new Prompt(history)).getResult();
        history.add(new UserMessage(msg));
        return result.getOutput().getText();
    }


}
