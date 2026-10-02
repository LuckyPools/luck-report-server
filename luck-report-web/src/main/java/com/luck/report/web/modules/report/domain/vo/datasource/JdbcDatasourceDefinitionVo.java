/**
 * ****************************************************************************
 */
package com.luck.report.web.modules.report.domain.vo.datasource;

/**
 * JDBC 数据源定义 VO
 *
 * @author system
 * @since 2026年
 */
public class JdbcDatasourceDefinitionVo extends DatasourceDefinitionVo {
    private static final long serialVersionUID = 1L;

    private String driver;
    private String url;
    private String username;
    private String password;

    /**
     * 默认无参构造器
     */
    public JdbcDatasourceDefinitionVo() {}

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
