package com.luck.report.web.common.domain.entity;

import lombok.Data;

/**
 * 基础实体类
 *
 * @author luck
 * @date 2023-10-26
 */
@Data
public abstract class BaseEntity<T extends BaseEntity<T>>  {

    private static final long serialVersionUID = 1L;

    protected String id;
}

