package com.luck.report.web.security.domain.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * 当前登录用户信息。
 *
 * @author luck-report
 * @since 1.2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {

    /**
     * 用户 ID（字符串形式）。
     */
    private String id;

    /**
     * 用户角色编码列表。
     */
    private List<String> roles;
}
