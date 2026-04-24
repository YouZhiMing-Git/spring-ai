package com.youzm.chatModel.advisors.log;

import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author:youzhiming
 * @date: 2026/4/16
 * @description:
 */
@Configuration
public class LoggerAdvisorConfig {
    @Bean
    public SimpleLoggerAdvisor simpleLoggerAdvisor() {
        return new SimpleLoggerAdvisor();
    }
}
