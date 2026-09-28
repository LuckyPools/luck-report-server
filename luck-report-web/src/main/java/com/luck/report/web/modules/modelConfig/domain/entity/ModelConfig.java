package com.luck.report.web.modules.modelConfig.domain.entity;

import com.luck.report.web.common.domain.entity.DataEntity;
import com.luck.report.web.modules.file.domain.entity.ReportTemplate;
import com.luck.report.web.modules.modelConfig.domain.enums.ModelType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 大模型配置实体
 * 存储大模型的连接信息和调用参数，后期会提供管理界面维护
 * 当前从内存缓存中读取，默认返回千问的配置
 *
 * @author luck
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelConfig extends DataEntity<ModelConfig> {

    /** 厂商标识（如 alibaba、openai），方便前端展示回显 */
    private String provider;

    /** API 基础地址（如 https://dashscope.aliyuncs.com/compatible-mode/v1） */
    private String baseUrl;

    /** API 密钥（落库加密；调用前解密） */
    private String apiKey;

    /** 模型名称（如 qwen3.6-plus、text-embedding-v3） */
    private String modelName;

    /** 自定义名称，最多50个字 */
    private String configName;

    /** 排序字段，数字越小越靠前 */
    private Integer sort;

    /** 温度参数，控制生成随机性，0~1 */
    private Double temperature;

    /** 是否激活：true-当前使用，false-未使用 */
    private Boolean isActive;

    /** 上下文窗口大小（token），供 Agent 压缩判断；不作为 API 输出上限 */
    private Integer contextWindowTokens;

    /** 模型类型：CHAT / EMBEDDING / RERANK */
    private ModelType modelType;

    /** API 路径，拼在 baseUrl 后；按类型默认不同（如 /v1/chat/completions、/embeddings、/rerank） */
    private String apiPath;

    /** 是否启用代理：false-禁用，true-启用 */
    private Boolean proxyEnabled;

    /** 代理主机地址 */
    private String proxyHost;

    /** 代理端口 */
    private Integer proxyPort;

    /** 代理用户名（可选） */
    private String proxyUsername;

    /** 代理密码（可选） */
    private String proxyPassword;
}
