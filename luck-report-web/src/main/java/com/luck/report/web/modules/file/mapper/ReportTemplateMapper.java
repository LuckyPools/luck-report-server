package com.luck.report.web.modules.file.mapper;

import com.luck.report.web.modules.file.domain.dto.ReportTemplateQueryDTO;
import com.luck.report.web.modules.file.domain.entity.ReportTemplate;
import com.luck.report.jdbc.Param;

import java.util.List;

/**
 * 报表文件 Mapper
 *
 * @author luck
 */
public interface ReportTemplateMapper {

    /**
     * 插入报表文件
     *
     * @param reportFile 报表文件实体
     * @return 影响行数
     */
    int insert(ReportTemplate reportFile);

    /**
     * 根据ID更新报表文件（动态更新非空字段），updateTime 由 Java 侧赋值
     *
     * @param reportFile 报表文件实体
     * @return 影响行数
     */
    int updateById(ReportTemplate reportFile);

    /**
     * 根据ID更新报表元数据（名称等，白名单列，不含 template 模板列）
     *
     * @param reportFile 报表文件实体（仅取 id、title 等元数据字段）
     * @return 影响行数
     */
    int updateMeta(ReportTemplate reportFile);

    /**
     * 根据ID查询报表文件（排除已删除，含 template）
     *
     * @param id 报表文件ID
     * @return 报表文件实体
     */
    ReportTemplate selectById(@Param("id") String id);

    /**
     * 按条件查询报表文件列表（非分页，不含 template）；name 模糊匹配
     *
     * @param queryDTO 查询条件
     * @return 报表文件列表
     */
    List<ReportTemplate> selectList(@Param("queryDTO") ReportTemplateQueryDTO queryDTO);

    /**
     * 根据ID逻辑删除报表文件
     *
     * @param id 报表文件ID
     * @return 影响行数
     */
    int deleteById(@Param("id") String id);

    /**
     * 分页条件查询报表文件（不含 template）
     *
     * @param queryDTO 查询条件
     * @param offset   偏移量（从 0 开始）
     * @param pageSize 每页大小
     * @return 报表文件列表
     */
    List<ReportTemplate> selectPage(@Param("queryDTO") ReportTemplateQueryDTO queryDTO,
                                                  @Param("offset") int offset,
                                                  @Param("pageSize") int pageSize);

    /**
     * 统计符合条件的报表文件数
     *
     * @param queryDTO 查询条件
     * @return 总数
     */
    long selectCount(@Param("queryDTO") ReportTemplateQueryDTO queryDTO);
}
