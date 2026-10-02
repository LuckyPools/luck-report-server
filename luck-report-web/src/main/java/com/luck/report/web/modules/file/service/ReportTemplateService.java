package com.luck.report.web.modules.file.service;

import com.luck.report.web.modules.file.domain.entity.ReportTemplate;
import com.luck.report.web.common.domain.vo.PageResultVO;

import java.io.InputStream;
import java.util.List;

/**
 * 报表文件服务接口
 *
 * @author luck
 */
public interface ReportTemplateService {

    /**
     * 根据ID查询报表文件
     *
     * @param id 报表文件ID
     * @return 报表文件实体
     */
    ReportTemplate getById(String id);

    /**
     * 新增报表文件
     *
     * @param reportFile 报表文件实体
     * @return 新增后的报表文件实体（含ID）
     */
    ReportTemplate save(ReportTemplate reportFile);

    /**
     * 更新报表文件
     *
     * @param reportFile 报表文件实体
     * @return 更新后的报表文件实体
     */
    ReportTemplate update(ReportTemplate reportFile);

    /**
     * 更新报表元数据（名称等描述性信息，不含模板内容）
     *
     * @param reportFile 报表文件实体（仅取 id、title 等元数据字段，template 字段被忽略）
     * @return 是否更新成功（目标记录不存在时返回 false）
     */
    boolean updateMetadata(ReportTemplate reportFile);

    /**
     * 根据ID逻辑删除报表文件
     *
     * @param id 报表文件ID
     * @return 是否删除成功
     */
    boolean removeById(String id);

    /**
     * 加载报表内容输入流
     *
     * @param id 报表文件ID
     * @return XML 内容输入流
     */
    InputStream loadReport(String id);

    /**
     * 查询所有未删除的报表文件列表
     *
     * @return 报表文件列表
     */
    List<ReportTemplate> listAll();

    /**
     * 分页查询报表文件（按更新时间倒序）
     *
     * @param name     标题模糊匹配（null/空不过滤）
     * @param pageNum  当前页码（从 1 开始）
     * @param pageSize 每页大小
     * @return 包含 records 和 total 的分页结果
     */
    PageResultVO<ReportTemplate> listPage(String name, int pageNum, int pageSize);
}
