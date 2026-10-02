package com.luck.report.web.modules.dataset.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luck.report.core.utils.SqlSecurityUtils;
import com.luck.report.web.common.domain.vo.PageResultVO;
import com.luck.report.core.exception.ReportBizException;
import com.luck.report.web.i18n.ReportI18n;
import com.luck.report.web.modules.dataset.domain.dto.ReportDatasetQueryDTO;
import com.luck.report.web.modules.dataset.domain.dto.ReportDatasetSaveDTO;
import com.luck.report.web.modules.dataset.domain.entity.ReportDataset;
import com.luck.report.web.modules.dataset.domain.vo.ReportDatasetVO;
import com.luck.report.web.modules.dataset.mapper.ReportDatasetMapper;
import com.luck.report.web.modules.dataset.service.ReportDatasetService;
import com.luck.report.web.modules.datasource.domain.entity.ReportDatasource;
import com.luck.report.web.modules.datasource.mapper.ReportDatasourceMapper;
import com.luck.report.web.security.utils.SecurityUtils;
import com.luck.report.web.utils.SnowflakeIdGenerator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 公共数据集服务实现
 *
 * @author luck
 */
@Slf4j
@Service("bean.reportDatasetService")
@AllArgsConstructor
public class ReportDatasetServiceImpl implements ReportDatasetService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 数据集类型常量
     */
    private static final String TYPE_SQL = "sql";
    private static final String TYPE_JSON = "json";

    @Qualifier("bean.reportDatasetMapper")
    private final ReportDatasetMapper reportDatasetMapper;
    @Qualifier("bean.reportDatasourceMapper")
    private final ReportDatasourceMapper reportDatasourceMapper;

    @Override
    public List<ReportDatasetVO> listByEnabled(Boolean enabled) {
        List<ReportDataset> list = enabled != null
                ? reportDatasetMapper.selectByEnabled(enabled)
                : reportDatasetMapper.selectAll();
        return toVOList(list);
    }

    @Override
    public PageResultVO<ReportDatasetVO> queryByPage(ReportDatasetQueryDTO queryDTO) {
        int offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();
        Long total = reportDatasetMapper.countByConditions(queryDTO);
        List<ReportDataset> dataList = reportDatasetMapper.selectByConditionsWithPage(queryDTO, offset, queryDTO.getPageSize());
        List<ReportDatasetVO> voList = toVOList(dataList);
        return PageResultVO.success(voList, total, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public ReportDatasetVO getById(String id) {
        ReportDataset dataset = reportDatasetMapper.selectById(id);
        if (dataset == null) {
            return null;
        }
        return toVOList(Collections.singletonList(dataset)).get(0);
    }

    @Override
    public ReportDatasetVO create(ReportDatasetSaveDTO dto) {
        validateSave(dto);
        checkNameUnique(dto.getName(), null);
        ReportDataset entity = buildEntity(null, dto);
        entity.setId(SnowflakeIdGenerator.generateId());
        String userId = SecurityUtils.getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();
        entity.setCreateBy(userId);
        entity.setUpdateBy(userId);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setDelFlag(0);
        reportDatasetMapper.insert(entity);
        log.info("创建公共数据集: id={}, name={}, type={}", entity.getId(), entity.getName(), entity.getType());
        return getById(entity.getId());
    }

    @Override
    public ReportDatasetVO update(String id, ReportDatasetSaveDTO dto) {
        ReportDataset existing = reportDatasetMapper.selectById(id);
        if (existing == null) {
            throw new ReportBizException("error.dataset.notExistId", id);
        }
        validateSave(dto);
        checkNameUnique(dto.getName(), id);
        ReportDataset entity = buildEntity(id, dto);
        entity.setUpdateBy(SecurityUtils.getCurrentUserId());
        entity.setUpdateTime(LocalDateTime.now());
        reportDatasetMapper.updateById(entity);
        log.info("更新公共数据集: id={}, name={}", id, entity.getName());
        return getById(id);
    }

    @Override
    public void deleteById(String id) {
        reportDatasetMapper.deleteById(id);
        log.info("删除公共数据集: id={}", id);
    }

    @Override
    public void deleteByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (String id : ids) {
            if (id != null && !id.isEmpty()) {
                deleteById(id);
            }
        }
    }

    @Override
    public void updateEnabledStatus(String id, Boolean enabled) {
        if (enabled == null) {
            throw new ReportBizException("error.dataset.invalidEnabled");
        }
        reportDatasetMapper.updateEnabledById(id, enabled, LocalDateTime.now());
        log.info("更新公共数据集启用状态: id={}, enabled={}", id, enabled);
    }

    @Override
    public Long countByDatasourceId(String datasourceId) {
        return reportDatasetMapper.countByDatasourceId(datasourceId);
    }

    /**
     * 保存前统一校验
     *
     * @param dto 保存参数
     */
    private void validateSave(ReportDatasetSaveDTO dto) {
        if (!TYPE_SQL.equals(dto.getType()) && !TYPE_JSON.equals(dto.getType())) {
            throw new ReportBizException("error.dataset.invalidType", dto.getType());
        }
        if (TYPE_SQL.equals(dto.getType())) {
            validateSqlDataset(dto);
        } else {
            validateJsonDataset(dto);
        }
        validateJsonArrayIfPresent(dto.getParameters(), "Parameter definition");
        validateJsonArrayIfPresent(dto.getFields(), "Field list");
    }

    /**
     * 校验SQL类型公共数据集
     *
     * @param dto 保存参数
     */
    private void validateSqlDataset(ReportDatasetSaveDTO dto) {
        if (StringUtils.isBlank(dto.getDatasourceId())) {
            throw new ReportBizException("error.dataset.datasourceRequired");
        }
        ReportDatasource datasource = reportDatasourceMapper.selectById(dto.getDatasourceId());
        if (datasource == null) {
            throw new ReportBizException("error.dataset.datasourceNotExist");
        }
        if (!Boolean.TRUE.equals(datasource.getEnabled())) {
            throw new ReportBizException("error.dataset.datasourceDisabled");
        }
        if (StringUtils.isBlank(dto.getSqlContent())) {
            throw new ReportBizException("error.sql.empty");
        }
        try {
            SqlSecurityUtils.validate(dto.getSqlContent());
        } catch (Exception e) {
            throw new ReportBizException("error.dataset.sqlSecurityFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 校验JSON类型公共数据集
     *
     * @param dto 保存参数
     */
    private void validateJsonDataset(ReportDatasetSaveDTO dto) {
        if (StringUtils.isBlank(dto.getJsonContent())) {
            throw new ReportBizException("error.dataset.jsonContentEmpty");
        }
        validateJsonArrayIfPresent(dto.getJsonContent(), "JSON content");
    }

    /**
     * 校验名称唯一性
     *
     * @param name      数据集名称
     * @param excludeId 更新时排除自身ID，创建时传null
     */
    private void checkNameUnique(String name, String excludeId) {
        ReportDataset existing = reportDatasetMapper.selectByName(name);
        if (existing != null && !existing.getId().equals(excludeId)) {
            throw new ReportBizException("error.dataset.nameExists", name);
        }
    }

    /**
     * 校验字符串为合法JSON数组格式
     *
     * @param content    JSON字符串
     * @param fieldName 字段中文名（用于错误提示）
     */
    private void validateJsonArrayIfPresent(String content, String fieldName) {
        if (StringUtils.isBlank(content)) {
            return;
        }
        try {
            List<Object> parsed = OBJECT_MAPPER.readValue(content, new TypeReference<List<Object>>() {
            });
            if (parsed == null) {
                throw new ReportBizException("error.dataset.invalidJsonArray", fieldName);
            }
        } catch (Exception e) {
            throw new ReportBizException("error.dataset.invalidJsonArray", fieldName);
        }
    }

    /**
     * 由保存DTO构建实体
     *
     * @param id  数据集ID，创建时为null
     * @param dto 保存参数
     * @return 公共数据集实体
     */
    private ReportDataset buildEntity(String id, ReportDatasetSaveDTO dto) {
        ReportDataset entity = new ReportDataset();
        entity.setId(id);
        entity.setName(dto.getName());
        entity.setType(dto.getType());
        entity.setDatasourceId(TYPE_SQL.equals(dto.getType()) ? dto.getDatasourceId() : null);
        entity.setSqlContent(TYPE_SQL.equals(dto.getType()) ? dto.getSqlContent() : null);
        entity.setJsonContent(TYPE_JSON.equals(dto.getType()) ? dto.getJsonContent() : null);
        entity.setParameters(dto.getParameters());
        entity.setFields(dto.getFields());
        entity.setDescription(dto.getDescription());
        entity.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
        return entity;
    }

    /**
     * 实体列表转VO列表，并批量回填所属数据源名称与状态
     *
     * @param list 公共数据集实体列表
     * @return 公共数据集VO列表
     */
    private List<ReportDatasetVO> toVOList(List<ReportDataset> list) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, ReportDatasource> datasourceMap = loadDatasourceMap(list);
        return list.stream()
                .map(dataset -> toVO(dataset, datasourceMap))
                .collect(Collectors.toList());
    }

    /**
     * 加载数据集列表涉及的数据源映射（批量查询避免N+1）
     *
     * @param list 公共数据集实体列表
     * @return datasourceId -> Datasource 映射
     */
    private Map<String, ReportDatasource> loadDatasourceMap(List<ReportDataset> list) {
        List<String> datasourceIds = list.stream()
                .map(ReportDataset::getDatasourceId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (datasourceIds.isEmpty()) {
            return new HashMap<>();
        }
        return reportDatasourceMapper.selectByIds(datasourceIds).stream()
                .collect(Collectors.toMap(ReportDatasource::getId, ds -> ds, (a, b) -> a));
    }

    /**
     * 单个实体转VO并回填数据源信息
     *
     * @param dataset       公共数据集实体
     * @param datasourceMap 数据源映射
     * @return 公共数据集VO
     */
    private ReportDatasetVO toVO(ReportDataset dataset, Map<String, ReportDatasource> datasourceMap) {
        ReportDatasource datasource = dataset.getDatasourceId() == null ? null : datasourceMap.get(dataset.getDatasourceId());
        return ReportDatasetVO.builder()
                .id(dataset.getId())
                .name(dataset.getName())
                .type(dataset.getType())
                .datasourceId(dataset.getDatasourceId())
                .datasourceName(datasource != null ? datasource.getName() : null)
                .datasourceEnabled(datasource != null ? datasource.getEnabled() : null)
                .sqlContent(dataset.getSqlContent())
                .jsonContent(dataset.getJsonContent())
                .parameters(dataset.getParameters())
                .fields(dataset.getFields())
                .description(dataset.getDescription())
                .enabled(dataset.getEnabled())
                .createBy(dataset.getCreateBy())
                .createTime(dataset.getCreateTime())
                .updateTime(dataset.getUpdateTime())
                .build();
    }
}
