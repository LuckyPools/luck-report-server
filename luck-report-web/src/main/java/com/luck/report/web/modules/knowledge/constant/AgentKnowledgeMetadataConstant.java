package com.luck.report.web.modules.knowledge.constant;

/**
 * 智能体知识文档元数据常量类
 *
 * @author luck
 */
public final class AgentKnowledgeMetadataConstant {

    private AgentKnowledgeMetadataConstant() {
    }

    /**
     * 向量类型
     */
    public static final String VECTOR_TYPE = "vectorType";

    /**
     * 智能体知识类型
     */
    public static final String AGENT_KNOWLEDGE = "agentKnowledge";

    /**
     * 智能体知识ID
     */
    public static final String DB_AGENT_KNOWLEDGE_ID = "agentKnowledgeId";

    /**
     * 嵌入模型配置 ID（入库时写入，便于排查模型错位）
     */
    public static final String MODEL_ID = "modelId";
}
