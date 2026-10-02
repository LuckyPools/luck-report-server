package com.luck.report.web.common.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据实体类
 *
 * @author luck
 * @date 2023-10-26
 */
@Data
public abstract class DataEntity<T extends DataEntity<T>> extends BaseEntity<T> {

    /**
     * 主键ID
     */
    private String id;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /**
     * 删除标志(0-未删除,1-已删除)
     */
    private Integer delFlag = 0;

}
