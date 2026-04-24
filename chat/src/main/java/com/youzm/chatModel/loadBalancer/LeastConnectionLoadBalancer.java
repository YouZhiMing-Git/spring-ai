package com.youzm.chatModel.loadBalancer;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * @author:youzhiming
 * @date: 2026/4/14
 * @description:
 */
@Component
public class LeastConnectionLoadBalancer implements LoadBalancerStrategy {
    @Override
    public ChatModel select(List<ModelInstance> instances) {
        return instances.stream()
                .min(Comparator.comparingInt(ModelInstance::getCurrentConnections))
                .map(ModelInstance::getModel)
                .orElse(null);
    }
}
