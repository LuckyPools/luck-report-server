package com.luck.report.web.modules.chat.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 对话压缩结果 DTO
 *
 * @author luck
 */
@Data
public class CompactResult {

    /**
     * 新的摘要内容
     */
    private String summary;

    /**
     * 更新后的关键操作列表
     */
    private List<String> keyOperations;
}
