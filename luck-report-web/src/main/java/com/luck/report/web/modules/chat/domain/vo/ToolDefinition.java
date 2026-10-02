package com.luck.report.web.modules.chat.domain.vo;

import lombok.Data;

/**
 * 工具定义
 *
 * @author luck
 */
@Data
public class ToolDefinition {

    /**
     * 工具类型，固定为 "function"
     */
    private String type = "function";

    /**
     * 工具函数定义
     */
    private FunctionDef function;
}
