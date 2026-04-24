package com.youzm.chatModel.advisors.multiRout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * 智能模型路由器
 * 
 * <p>核心职责：基于用户查询内容的特征，智能选择最适合的 AI 模型</p>
 * @author youzhiming
 * @date 2026/4/9
 */
public class ModelRouter  {
    
    private static final Logger log = LoggerFactory.getLogger(ModelRouter.class);

    /** 代码类查询匹配模式：包含编程相关关键词 */
    private static final Pattern CODE_PATTERN = Pattern.compile(
            ".*?(代码|算法|函数|bug|调试|实现|编程|python|java|javascript|怎么写|如何写).*?",
            Pattern.CASE_INSENSITIVE  // 忽略大小写
    );
    
    /** 数学类查询匹配模式：包含数学计算相关关键词 */
    private static final Pattern MATH_PATTERN = Pattern.compile(
            ".*?(数学|公式|计算|导数|积分|方程|证明|几何|代数|微积分).*?",
            Pattern.CASE_INSENSITIVE
    );
    
    /** 创作类查询匹配模式：包含创意写作相关关键词 */
    private static final Pattern CREATIVE_PATTERN = Pattern.compile(
            ".*?(创作|写作|故事|诗歌|文案|创意|想象|虚构).*?",
            Pattern.CASE_INSENSITIVE
    );
    
    /** 分析类查询匹配模式：包含逻辑分析相关关键词 */
    private static final Pattern ANALYSIS_PATTERN = Pattern.compile(
            ".*?(分析|解释|总结|对比|评估|报告|研究).*?",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 根据查询内容路由到最合适的模型
     * 
     * <p>路由决策流程：</p>
     * <ol>
     *   <li>检查查询是否为空，为空则返回默认路由</li>
     *   <li>将查询转换为小写，统一处理</li>
     *   <li>按优先级依次匹配模式：代码 → 数学 → 创作 → 分析 → 通用</li>
     *   <li>返回包含模型名称、路由原因和置信度的路由决策对象</li>
     * </ol>
     * 
     * @param query 用户查询文本
     * @return 模型路由决策结果，包含目标模型、原因和置信度
     */
    public ModelRoute route(String query) {
        // 边界检查：空查询直接返回默认路由
        if (query == null || query.trim().isEmpty()) {
            return new ModelRoute("zhipuAssistantModel", "empty_query", 0.7);
        }

        // 标准化：转换为小写，确保模式匹配不受大小写影响
        String normalizedQuery = query.toLowerCase();
        
        // 优先级1：代码类查询 → 使用 DeepSeek（代码能力强，性价比高）
        if (CODE_PATTERN.matcher(normalizedQuery).matches()) {
            log.debug("路由决策：代码类查询 → deepseekModel | 查询内容: {}", query);
            return new ModelRoute("deepseekModel", "code_query", 0.85);
        } 
        
        // 优先级2：数学类查询 → 使用智谱 GLM-4（计算精度高）
        if (MATH_PATTERN.matcher(normalizedQuery).matches()) {
            log.debug("路由决策：数学类查询 → zhipuAssistantModel | 查询内容: {}", query);
            return new ModelRoute("zhipuAssistantModel", "math_query", 0.9);
        }
        
        // 优先级3：创作类查询 → 使用智谱 GLM-5.1（temperature=0.9，更有创意）
        if (CREATIVE_PATTERN.matcher(normalizedQuery).matches()) {
            log.debug("路由决策：创作类查询 → zhipuCreativeModel | 查询内容: {}", query);
            return new ModelRoute("zhipuCreativeModel", "creative_query", 0.9);
        }
        
        // 优先级4：分析类查询 → 使用智谱 GLM-4（逻辑推理能力强）
        if (ANALYSIS_PATTERN.matcher(normalizedQuery).matches()) {
            log.debug("路由决策：分析类查询 → zhipuAssistantModel | 查询内容: {}", query);
            return new ModelRoute("zhipuAssistantModel", "analysis_query", 0.85);
        }

        // 默认：通用对话 → 使用智谱 GLM-4（平衡成本与效果）
        log.debug("路由决策：通用对话 → zhipuAssistantModel | 查询内容: {}", query);
        return new ModelRoute("zhipuAssistantModel", "general_query", 0.7);
    }
}



