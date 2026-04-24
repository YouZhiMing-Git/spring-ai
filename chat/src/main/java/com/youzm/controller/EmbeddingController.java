package com.youzm.controller;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author:youzhiming
 * @date: 2026/4/22
 * @description:
 */
@RestController
@RequestMapping("/embedding")
public class EmbeddingController {


    private final EmbeddingModel embeddingModel;

    public EmbeddingController(@Qualifier("qwenEmbeddingModel") EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }


    @GetMapping("/embed")
    public Map embed(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        EmbeddingResponse embeddingResponse = this.embeddingModel
                .embedForResponse(List.of(message));
        return Map.of("embedding", embeddingResponse);
    }
}
