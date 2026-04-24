package com.youzm.chatModel.loadBalancer;

import lombok.Data;
import org.springframework.ai.chat.model.ChatModel;

/**
 * @author:youzhiming
 * @date: 2026/4/14
 * @description:
 */
@Data
public class ModelInstance {
    private final String name;
    private final ChatModel model;
    private final int weight;
    private volatile int currentConnections = 0;
}
