package com.luck.report.web.modules.chat.domain.vo;

import lombok.Data;

import java.util.Map;

/**
 * 工具函数定义
 *
 * @author luck
 */
@Data
public class FunctionDef {

    /**
     * 函数名称
     */
    private String name;

    /**
     * 函数描述，供 LLM 理解调用时机
     */
    private String description;

    /**
     * 函数参数 JSON Schema（OpenAI parameters，前端用 inputSchema 表达）
     */
    private Map<String, Object> parameters;

    /**
     * 函数返回结构 JSON Schema（可选）
     */
    private Map<String, Object> outputSchema;
}
