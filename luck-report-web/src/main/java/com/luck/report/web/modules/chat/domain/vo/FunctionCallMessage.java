package com.luck.report.web.modules.chat.domain.vo;

import lombok.Data;

/**
 * 函数调用消息
 *
 * @author luck
 */
@Data
public class FunctionCallMessage {

    /**
     * 函数名称
     */
    private String name;

    /**
     * 函数调用参数 JSON 字符串
     */
    private String arguments;
}
