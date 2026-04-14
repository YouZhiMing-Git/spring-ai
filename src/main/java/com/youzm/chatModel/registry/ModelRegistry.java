package com.youzm.chatModel.registry;

import com.youzm.chatModel.loadBalancer.ModelInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author:youzhiming
 * @date: 2026/4/14
 * @description:
 */
@Component
@Slf4j
public class ModelRegistry {
    private final List<ModelInstance> modelInstances;
    private final Map<String, AtomicBoolean> healthCheckMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public ModelRegistry(List<ModelInstance> modelInstances) {
        this.modelInstances = modelInstances;
        for (ModelInstance modelInstance : modelInstances) {
            healthCheckMap.put(modelInstance.getName(), new AtomicBoolean(true));
        }
    }
    private void startHealthCheck() {
        for (ModelInstance instance : modelInstances) {
            try {
                // 快速健康检测
                instance.getModel().call("hello");
                healthCheckMap.get(instance.getModel()).set(true);
            } catch (Exception e) {
                log.warn("Health check failed: {}", instance.getName());
                healthCheckMap.get(instance.getModel()).set(false);
            }
        }
    }

    public List<ModelInstance> getHealthyInstances() {
        return modelInstances.stream()
                .filter(i -> healthCheckMap.get(i.getModel()).get())
                .collect(Collectors.toList());
    }
}
