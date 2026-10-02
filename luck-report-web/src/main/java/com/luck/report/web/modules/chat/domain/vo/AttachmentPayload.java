package com.luck.report.web.modules.chat.domain.vo;

import lombok.Data;

/**
 * 消息附件
 *
 * @author luck
 */
@Data
public class AttachmentPayload {

    /**
     * MIME 类型
     */
    private String mimeType;

    /**
     * Base64 编码数据
     */
    private String data;
}
