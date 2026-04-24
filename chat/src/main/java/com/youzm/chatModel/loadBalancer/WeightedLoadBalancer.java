package com.youzm.chatModel.loadBalancer;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

/**
 * @author:youzhiming
 * @date: 2026/4/14
 * @description:
 */
@Component
public class WeightedLoadBalancer implements LoadBalancerStrategy {

    private final Random random = new Random();

    @Override
    public ChatModel select(List<ModelInstance> instances) {
        int totalWeight = instances.stream().mapToInt(ModelInstance::getWeight).sum();
        int randomWeight = random.nextInt(totalWeight);
        int currentSum = 0;
        for (ModelInstance instance : instances) {
            currentSum += instance.getWeight();
            if (randomWeight < currentSum) return instance.getModel();
        }
        return null;
    }
}
