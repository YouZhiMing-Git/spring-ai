package com.youzm.chatModel.loadBalancer;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

/**
 * @author:youzhiming
 * @date: 2026/4/14
 * @description: 随机负载均衡策略
 */
@Component
public class RandomLoadBalancer implements LoadBalancerStrategy {

    private final Random random = new Random();

    @Override
    public ChatModel select(List<ModelInstance> instances) {
        if(instances != null && !instances.isEmpty()) {
            return instances.get(random.nextInt(instances.size())).getModel();
        }
        return null;
    }
}
