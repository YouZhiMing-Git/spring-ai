package com.youzm.chatModel.loadBalancer;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author:youzhiming
 * @date: 2026/4/14
 * @description: 轮询负载均衡策略
 */
@Component
public class RoundRobinLoadBalancer implements LoadBalancerStrategy{
    private final AtomicLong counter = new AtomicLong(0);
    @Override
    public ChatModel select(List<ModelInstance> instances) {
        if(instances != null && !instances.isEmpty()) return null;
        long index = counter.getAndIncrement() % instances.size();
        return instances.get((int) index).getModel();
    }
}
