package com.youzm.chatModel.loadBalancer;

import com.youzm.chatModel.registry.ModelRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.functions.CheckedFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.naming.ServiceUnavailableException;
import java.util.List;

/**
 * @author:youzhiming
 * @date: 2026/4/14
 * @description:
 */
@Component
@Slf4j
public class ResilientModelRouter {
    private final ModelRegistry modelRegistry;
    private final LoadBalancerStrategy loadBalancer;
    private final RetryRegistry retryRegistry;
    private final CircuitBreakerRegistry cbRegistry;

    public ResilientModelRouter(ModelRegistry modelRegistry, @Qualifier("weightedLoadBalancer") LoadBalancerStrategy loadBalancer, RetryRegistry retryRegistry, CircuitBreakerRegistry cbRegistry) {
        this.modelRegistry = modelRegistry;
        this.loadBalancer = loadBalancer;
        this.retryRegistry = retryRegistry;
        this.cbRegistry = cbRegistry;
    }

    public String call(String userMessage) throws Exception {
        List<ModelInstance> healthyInstances = modelRegistry.getHealthyInstances();
        if (healthyInstances.isEmpty()) {
            log.error("No healthy model instances available");
            throw new ServiceUnavailableException("All models unavailable");
        }

        // 负载均衡选择模型
        ChatModel selected = loadBalancer.select(healthyInstances);
        return executeWithResilience(selected, userMessage);
    }

    private String executeWithResilience(ChatModel model, String userMessage) {
        Retry retry = retryRegistry.retry("model-retry");
        CircuitBreaker circuitBreaker = cbRegistry.circuitBreaker("model-cb");

        // 先装饰熔断器，再装饰重试。顺序很重要：Retry(CircuitBreaker(Supplier))
        java.util.function.Supplier<String> decorated = Retry.decorateSupplier(
                retry,
                CircuitBreaker.decorateSupplier(circuitBreaker,
                        () -> doCall(model, userMessage)));

        try {
            return decorated.get();
        } catch (Throwable e) {
            log.error("Primary model failed after retries and circuit breaker check: {}", e.getMessage());
            // 当主调用链路（重试+熔断）失败后，执行降级/故障转移逻辑
            return fallback(userMessage);
        }

    }

    private String doCall(ChatModel model, String userMessage) {
        log.info("Calling model: {}", model.getClass().getSimpleName());
        long start = System.currentTimeMillis();
        try {
            ChatResponse response = model.call(new Prompt(userMessage));
            log.info("Model response time: {}ms", System.currentTimeMillis() - start);
            return response.getResult().getOutput().getText();
        } catch (Exception e) {
            log.warn("Model call failed: {}", e.getMessage());
            throw new TransientAiException("Model call failed", e);
        }
    }

    private String fallback(String userMessage) {
        log.info("Executing fallback for message: {}", userMessage);
        // 故障转移：尝试所有健康模型（轮询兜底）
        List<ModelInstance> instances = modelRegistry.getHealthyInstances();
        for (ModelInstance instance : instances) {
            try {
                return doCall(instance.getModel(), userMessage);
            } catch (Exception ignored) {
            }
        }
        throw new RuntimeException("All models failed");
    }
}
