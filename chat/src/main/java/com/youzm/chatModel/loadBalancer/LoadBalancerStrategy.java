package com.youzm.chatModel.loadBalancer;

import org.springframework.ai.chat.model.ChatModel;

import java.util.List;

public interface LoadBalancerStrategy {
    ChatModel select(List<ModelInstance> instances);
}
