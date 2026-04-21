package com.youzm.chatModel.config;

import com.youzm.chatModel.advisors.multiRout.MultiModelRoutingAdvisor;
import com.youzm.service.ToolService;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * @author:youzhiming
 * @date: 2026/4/8
 * @description: AI模型配置（智谱AI + DeepSeek）
 */
@Configuration
public class ModelConfig {

    @Value("${spring.ai.zhipuai.api-key}")
    private String zhipuApiKey;

    @Value("${spring.ai.openai.api-key}")
    private String deepseekApiKey;

    @Value("${spring.ai.openai.base-url}")
    private String deepseekBaseUrl;

    /**
     * 创建自定义重试模板 - 生产环境推荐配置
     */
    private RetryTemplate createCustomRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 1. 配置重试策略：最多重试3次
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);

        // 2. 配置退避策略：指数退避（避免频繁请求导致限流）
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);  // 初始间隔 1秒
        backOffPolicy.setMaxInterval(10000);     // 最大间隔 10秒
        backOffPolicy.setMultiplier(2.0);        // 每次翻倍：1s -> 2s -> 4s

        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }

    /**
     * 创建自定义观察注册表 - 用于性能监控
     */
    private ObservationRegistry createCustomObservationRegistry() {
        return ObservationRegistry.create();
    }

    @Bean("zhipuAssistantModel")
    @Primary
    public ChatModel zhipuChatModel() {
        ZhiPuAiApi zhiPuAiApi = new ZhiPuAiApi(zhipuApiKey);
        return new ZhiPuAiChatModel(zhiPuAiApi,
                ZhiPuAiChatOptions.builder()
                        .model("glm-4")
                        .temperature(0.7)
                        .build()
        );
    }

    // 创意写作角色
    @Bean("zhipuCreativeModel")
    public ChatModel zhipuCreativeModel() {
        ZhiPuAiApi api = new ZhiPuAiApi(zhipuApiKey);
        return new ZhiPuAiChatModel(api,
                ZhiPuAiChatOptions.builder()
                        .model("glm-5.1")
                        .temperature(0.9)
                        .build());
    }

    @Bean("zhipuMetaphysicsModel")
    public ChatModel zhipuMetaphysicsModel() {
        ZhiPuAiApi zhiPuAiApi = new ZhiPuAiApi(zhipuApiKey);
        return new ZhiPuAiChatModel(zhiPuAiApi,
                ZhiPuAiChatOptions.builder()
                        .model("glm-5.1")
                        .temperature(0.7)
                        .build()
        );
    }

    // DeepSeek 模型（集成工具调用）
    @Bean("deepseekModel")
    public ChatModel deepseekModel(ToolService toolService, ToolCallingManager toolCallingManager) {

        OpenAiApi openAiApi = new OpenAiApi.Builder()
                .baseUrl(deepseekBaseUrl)
                .apiKey(deepseekApiKey)
                .build();

        // 将 ToolService 转换为 ToolCallback 数组
        ToolCallback[] toolCallbacks = ToolCallbacks.from(toolService);

        // 创建支持工具调用的选项
        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .model("deepseek-chat")
                .temperature(0.7)
                .toolCallbacks(toolCallbacks)  // 注册工具回调
                .internalToolExecutionEnabled(true)  // 启用内部工具执行
                .build();

        return new OpenAiChatModel(
                openAiApi,
                chatOptions,
                toolCallingManager,
                createCustomRetryTemplate(),
                createCustomObservationRegistry()
        );
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, MultiModelRoutingAdvisor routingAdvisor, SimpleLoggerAdvisor simpleLoggerAdvisor, MessageChatMemoryAdvisor messageChatMemoryAdvisor) {

        return builder
                .defaultAdvisors(routingAdvisor,simpleLoggerAdvisor,messageChatMemoryAdvisor)  // 全局注册，所有请求都会经过路由
                .build();

    }

}
