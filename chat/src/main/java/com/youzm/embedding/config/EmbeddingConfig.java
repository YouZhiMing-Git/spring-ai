package com.youzm.embedding.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author:youzhiming
 * @date: 2026/4/21
 * @description: 嵌入模型配置
 */
@Configuration
public class EmbeddingConfig {

    @Value("${spring.ai.qwen.api-key}")
    private String qwenApiKey;

    @Value("${spring.ai.qwen.base-url}")
    private String qwenBaseUrl;


    @Bean("qwenEmbeddingModel")
    public OpenAiEmbeddingModel qwenEmbeddingModel() {
        OpenAiApi openAiApi = new OpenAiApi.Builder()
                .apiKey(qwenApiKey)
                .baseUrl(qwenBaseUrl)
//                .completionsPath("/chat/completions")
//                .embeddingsPath("/embeddings")
                .build();
        OpenAiEmbeddingOptions embeddingOptions = OpenAiEmbeddingOptions.builder()
                .model("text-embedding-v3")
                .build();

        return new OpenAiEmbeddingModel(
                openAiApi,
                MetadataMode.EMBED,
                embeddingOptions
        );
    }

}
