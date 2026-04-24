
package com.youzm.chatModel.advisors.multiRout;


public record ModelRoute(
        String modelName,
        String reason,
        double confidence
) {

    public ModelRoute {
        // 验证模型名称非空
        if (modelName == null || modelName.trim().isEmpty()) {
            throw new IllegalArgumentException("模型名称不能为空");
        }
        
        // 规范化路由原因：null 值设为默认值
        if (reason == null) {
            reason = "unknown";
        }
        
        // 验证置信度范围
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("置信度必须在 0.0 到 1.0 之间，当前值: " + confidence);
        }
    }
}
