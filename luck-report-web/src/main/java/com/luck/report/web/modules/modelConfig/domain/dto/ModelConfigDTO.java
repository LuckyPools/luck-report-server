package com.luck.report.web.modules.modelConfig.domain.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 大模型配置DTO
 *
 * @author luck
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelConfigDTO {

    /**
     * 配置ID
     */
    private String id;

    /**
     * 厂商标识(如 openai、deepseek、qwen)
     */
    @NotBlank(message = "提供商不能为空")
    private String provider;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * API基础地址(如 https://api.openai.com)
     */
    @NotBlank(message = "baseUrl不能为空")
    private String baseUrl;

    /**
     * 模型名称(如 gpt-4、deepseek-chat、qwen-plus)
     */
    @NotBlank(message = "模型名称不能为空")
    private String modelName;

    /**
     * 自定义名称,最多50个字
     */
    @Size(max = 50, message = "自定义名称不能超过50个字")
    private String configName;

    /**
     * 排序字段,数字越小越靠前
     */
    private Integer sort;

    /**
     * 模型类型(CHAT/EMBEDDING/RERANK)
     */
    @NotBlank(message = "模型类型不能为空")
    private String modelType;

    /**
     * API路径，拼在baseUrl后；空则调用方用类型默认值
     */
    private String apiPath;

    /**
     * 温度参数,控制生成随机性,默认0.0
     */
    private Double temperature = 0.0;

    /**
     * 上下文窗口大小（token），供 Agent 压缩判断；默认 128000；不作为 API 输出上限
     */
    @JsonAlias("maxTokens")
    private Integer contextWindowTokens = 128000;

    /**
     * 是否启用:true-启用,false-禁用
     */
    private Boolean enabled = true;

    /**
     * 是否启用代理,默认关闭(使用直连)
     */
    private Boolean proxyEnabled = false;

    /**
     * 代理主机地址
     */
    private String proxyHost;

    /**
     * 代理端口
     */
    private Integer proxyPort;

    /**
     * 代理用户名(可选)
     */
    private String proxyUsername;

    /**
     * 代理密码(可选)
     */
    private String proxyPassword;
}
