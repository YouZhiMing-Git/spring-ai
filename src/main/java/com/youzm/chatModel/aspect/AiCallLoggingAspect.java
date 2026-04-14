
package com.youzm.chatModel.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

/**
 * @author:youzhiming
 * @date: 2026/4/8
 * @description: AI 调用日志切面 - 记录提示词和 token 消耗
 */
@Aspect
@Component
public class AiCallLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(AiCallLoggingAspect.class);

    @Around("execution(* org.springframework.ai.chat.model.ChatModel.call(org.springframework.ai.chat.prompt.Prompt))")    public Object logChatCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取输入参数
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof Prompt) {
            Prompt prompt = (Prompt) args[0];
            log.info("========== AI 调用开始 ==========");
            log.info("提示词内容: {}", prompt.getContents());
            log.info("模型选项: {}", prompt.getOptions());
        } else if (args.length > 0 && args[0] instanceof String) {
            log.info("========== AI 调用开始 ==========");
            log.info("提示词内容: {}", args[0]);
        }

        try {
            // 执行原始方法
            Object result = joinPoint.proceed();

            // 记录响应信息
            long endTime = System.currentTimeMillis();
            if (result instanceof ChatResponse) {
                ChatResponse response = (ChatResponse) result;

                // 提取 token 使用信息
                if (response.getMetadata() != null) {
                    Integer promptTokens = response.getMetadata().getUsage() != null
                            ? response.getMetadata().getUsage().getPromptTokens() : null;
                    Integer generationTokens = response.getMetadata().getUsage() != null
                            ? response.getMetadata().getUsage().getCompletionTokens() : null;
                    Integer totalTokens = response.getMetadata().getUsage() != null
                            ? response.getMetadata().getUsage().getTotalTokens() : null;

                    log.info("响应内容: {}", response.getResult() != null ? response.getResult().getOutput().getText() : "null");
                    log.info("Token 消耗 - 输入: {}, 输出: {}, 总计: {}",
                            promptTokens, generationTokens, totalTokens);
                } else {
                    log.info("响应内容: {}", response.getResult() != null ? response.getResult().getOutput().getText() : "null");
                }

                log.info("耗时: {} ms", (endTime - startTime));
            }

            log.info("========== AI 调用结束 ==========");
            return result;

        } catch (Exception e) {
            log.error("AI 调用失败: {}", e.getMessage(), e);
            throw e;
        }
    }
}

