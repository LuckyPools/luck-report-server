package com.luck.report.web.modules.datasource.controller;

import com.luck.report.web.common.domain.vo.PageResultVO;
import com.luck.report.web.common.domain.vo.ResultVO;
import com.luck.report.web.i18n.ReportI18n;
import com.luck.report.web.modules.datasource.domain.dto.CreateLogicalRelationDTO;
import com.luck.report.web.modules.datasource.domain.dto.ReportDatasourceQueryDTO;
import com.luck.report.web.modules.datasource.domain.dto.ReportDatasourceTypeDTO;
import com.luck.report.web.modules.datasource.domain.dto.InitSchemaRequestDTO;
import com.luck.report.web.modules.datasource.domain.dto.SchemaDTO;
import com.luck.report.web.modules.datasource.domain.dto.UpdateLogicalRelationDTO;
import com.luck.report.web.modules.datasource.domain.entity.LogicalRelation;
import com.luck.report.web.modules.datasource.domain.enums.ReportDatasourceTypeEnum;
import com.luck.report.web.modules.datasource.domain.vo.ReportDatasourceVO;
import com.luck.report.web.modules.datasource.domain.vo.SchemaSearchResultVO;
import com.luck.report.web.modules.datasource.service.ReportDatasourceService;
import com.luck.report.core.definition.datasource.BuildinDatasource;
import com.luck.report.core.Utils;
import javax.validation.Valid;

import com.luck.report.web.modules.datasource.domain.entity.ReportDatasource;
import com.luck.report.web.utils.DownloadUtils;
import com.luck.report.web.utils.ResultVOUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据源管理Controller
 * 提供数据源的增删改查、连接测试、表管理、Schema初始化和逻辑外键管理接口
 *
 * @author luck
 */
@Slf4j
@RestController("bean.datasourceController")
@RequestMapping("${luck-report.servletPrefix:}/report_datasource")
@AllArgsConstructor
public class ReportDatasourceController {

    @Qualifier("bean.datasourceService")
    private final ReportDatasourceService reportDatasourceService;

    /**
     * 获取支持的数据源类型列表
     *
     * @return 数据源类型DTO列表
     */
    @GetMapping("/types")
    public ResultVO<List<ReportDatasourceTypeDTO>> getDatasourceTypes() {
        List<ReportDatasourceTypeEnum> supportedTypes = Arrays.asList(
                ReportDatasourceTypeEnum.MYSQL, ReportDatasourceTypeEnum.POSTGRESQL,
                ReportDatasourceTypeEnum.DAMENG, ReportDatasourceTypeEnum.SQL_SERVER,
                ReportDatasourceTypeEnum.ORACLE, ReportDatasourceTypeEnum.HIVE);

        List<ReportDatasourceTypeDTO> types = supportedTypes.stream()
                .map(type -> ReportDatasourceTypeDTO.builder()
                        .code(type.getCode())
                        .typeName(type.getTypeName())
                        .displayName(type.getDisplayName())
                        .driverClassName(type.getDriverClassName())
                        .build())
                .collect(Collectors.toList());

        return ResultVOUtils.success("success.datasource.typeLoaded", types);
    }

    /**
     * 获取数据源列表（支持按enabled、type筛选）
     *
     * @param enabled 启用状态筛选（可选）
     * @param type    类型筛选（可选）
     * @return 数据源VO列表
     */
    @GetMapping("/list")
    public ResultVO<List<ReportDatasourceVO>> list(
            @RequestParam(value = "enabled", required = false) Boolean enabled,
            @RequestParam(value = "type", required = false) String type) {
        List<ReportDatasourceVO> result;
        if (enabled != null) {
            result = reportDatasourceService.getDatasourceByEnabled(enabled);
        } else if (type != null && !type.isEmpty()) {
            result = reportDatasourceService.getDatasourceByType(type);
        } else {
            result = reportDatasourceService.getAllDatasource();
        }
        return ResultVOUtils.success("success.datasource.listLoaded", result);
    }

    /**
     * 分页查询数据源列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @PostMapping("/page")
    public PageResultVO<ReportDatasourceVO> queryByPage(@Valid @RequestBody ReportDatasourceQueryDTO queryDTO) {
        try {
            return reportDatasourceService.queryByPage(queryDTO);
        } catch (Exception e) {
            log.error("分页查询数据源列表失败", e);
            return PageResultVO.error(ReportI18n.getMessage("error.datasource.pageFailed", ReportI18n.messageOf(e)));
        }
    }

    /**
     * 获取数据源详情
     *
     * @param id 数据源ID
     * @return 数据源VO
     */
    @GetMapping("/detail/{id}")
    public ResultVO<ReportDatasourceVO> getDetail(@PathVariable String id) {
        ReportDatasourceVO vo = reportDatasourceService.getDatasourceById(id);
        if (vo == null) {
            return ResultVOUtils.error("error.datasource.notExist");
        }
        return ResultVOUtils.success("success.datasource.detailLoaded", vo);
    }

    /**
     * 创建数据源
     *
     * @param vo 数据源VO
     * @return 创建后的数据源VO
     */
    @PostMapping("/create")
    public ResultVO<ReportDatasourceVO> create(@RequestBody ReportDatasourceVO vo) {
        try {
            ReportDatasource entity = toEntity(vo);
            ReportDatasourceVO created = reportDatasourceService.createDatasource(entity);
            return ResultVOUtils.success("success.datasource.created", created);
        } catch (Exception e) {
            log.error("创建数据源失败", e);
            return ResultVOUtils.error("error.datasource.createFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 更新数据源
     *
     * @param id 数据源ID
     * @param vo 数据源VO
     * @return 更新后的数据源VO
     */
    @PutMapping("/update/{id}")
    public ResultVO<ReportDatasourceVO> update(@PathVariable String id, @RequestBody ReportDatasourceVO vo) {
        try {
            ReportDatasource entity = toEntity(vo);
            ReportDatasourceVO updated = reportDatasourceService.updateDatasource(id, entity);
            return ResultVOUtils.success("success.datasource.updated", updated);
        } catch (Exception e) {
            log.error("更新数据源失败", e);
            return ResultVOUtils.error("error.datasource.updateFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 删除数据源
     *
     * @param id 数据源ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    public ResultVO<String> delete(@PathVariable String id) {
        try {
            reportDatasourceService.deleteDatasource(id);
            return ResultVOUtils.success("success.datasource.deleted", ReportI18n.getMessage("success.datasource.deleted"));
        } catch (Exception e) {
            log.error("删除数据源失败", e);
            return ResultVOUtils.error("error.datasource.deleteFailed", ReportI18n.messageOf(e));
        }
    }

    @DeleteMapping("/batch/delete")
    public ResultVO<String> deleteBatch(@RequestBody List<String> ids) {
        try {
            reportDatasourceService.deleteDatasourceBatch(ids);
            return ResultVOUtils.success("success.datasource.deleted", ReportI18n.getMessage("success.datasource.deleted"));
        } catch (Exception e) {
            log.error("批量删除数据源失败", e);
            return ResultVOUtils.error("error.datasource.deleteFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 测试数据源连接
     *
     * @param id 数据源ID
     * @return 测试结果
     */
    @PostMapping("/test_connection/{id}")
    public ResultVO<Boolean> testConnection(@PathVariable String id) {
        try {
            boolean success = reportDatasourceService.testConnection(id);
            return ResultVO.success(
                    ReportI18n.getMessage(success ? "success.datasource.connectOk" : "error.datasource.connectFailedPlain"),
                    success);
        } catch (Exception e) {
            log.error("连接测试异常", e);
            return ResultVOUtils.<Boolean>error("error.datasource.connectFailed", ReportI18n.messageOf(e)).setData(false);
        }
    }

    /**
     * 更新数据源启用状态
     *
     * @param id      数据源ID
     * @param enabled 是否启用
     * @return 操作结果
     */
    @PostMapping("/enable/{id}")
    public ResultVO<String> updateEnabledStatus(@PathVariable String id,
                                               @RequestParam(value = "enabled") Boolean enabled) {
        try {
            reportDatasourceService.updateEnabledStatus(id, enabled);
            return ResultVOUtils.success("success.datasource.enabledUpdated", ReportI18n.getMessage("success.datasource.enabledUpdated"));
        } catch (Exception e) {
            log.error("更新启用状态失败", e);
            return ResultVOUtils.error("error.datasource.enabledFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 获取数据源的表列表
     *
     * @param id 数据源ID
     * @return 表名列表
     */
    @GetMapping("/{id}/tables")
    public ResultVO<List<String>> getTables(@PathVariable String id) {
        try {
            List<String> tables = reportDatasourceService.getDatasourceTables(id);
            return ResultVOUtils.success("success.datasource.tablesLoaded", tables);
        } catch (Exception e) {
            log.error("获取表列表失败", e);
            return ResultVOUtils.error("error.datasource.tablesFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 获取表的字段列表
     *
     * @param id        数据源ID
     * @param tableName 表名
     * @return 字段名列表
     */
    @GetMapping("/{id}/tables/{tableName}/columns")
    public ResultVO<List<String>> getTableColumns(@PathVariable String id,
                                                      @PathVariable String tableName) {
        try {
            List<String> columns = reportDatasourceService.getTableColumns(id, tableName);
            return ResultVOUtils.success("success.datasource.columnsLoaded", columns);
        } catch (Exception e) {
            log.error("获取字段列表失败", e);
            return ResultVOUtils.error("error.datasource.columnsFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 初始化表Schema到向量数据库
     * 将指定表的Schema信息向量化存储，供agent查询使用
     *
     * @param id      数据源ID
     * @param request 初始化请求，包含表名列表
     * @return 初始化结果
     */
    @PostMapping("/{id}/init_schema")
    public ResultVO<String> initSchema(@PathVariable String id,
                                         @RequestBody InitSchemaRequestDTO request) {
        try {
            reportDatasourceService.initTableSchema(id, request.getTables(), request.getModelId());
            return ResultVOUtils.success("success.datasource.schemaInited", ReportI18n.getMessage("success.datasource.schemaInited"));
        } catch (Exception e) {
            log.error("初始化Schema失败", e);
            return ResultVOUtils.error("error.datasource.schemaInitFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 获取数据源的逻辑外键列表
     *
     * @param id 数据源ID
     * @return 逻辑外键列表
     */
    @GetMapping("/{id}/logical_relations/list")
    public ResultVO<List<LogicalRelation>> getLogicalRelations(@PathVariable String id) {
        try {
            List<LogicalRelation> relations = reportDatasourceService.getLogicalRelations(id);
            return ResultVOUtils.success("success.datasource.relationsLoaded", relations);
        } catch (Exception e) {
            log.error("获取逻辑外键列表失败", e);
            return ResultVOUtils.error("error.datasource.relationsFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 添加逻辑外键
     *
     * @param id  数据源ID
     * @param dto 创建逻辑外键DTO
     * @return 添加后的逻辑外键
     */
    @PostMapping("/{id}/logical_relations/create")
    public ResultVO<LogicalRelation> addLogicalRelation(@PathVariable String id,
                                                            @Valid @RequestBody CreateLogicalRelationDTO dto) {
        try {
            LogicalRelation relation = new LogicalRelation();
            relation.setSourceTableName(dto.getSourceTableName());
            relation.setSourceColumnName(dto.getSourceColumnName());
            relation.setTargetTableName(dto.getTargetTableName());
            relation.setTargetColumnName(dto.getTargetColumnName());
            relation.setRelationType(dto.getRelationType());
            relation.setDescription(dto.getDescription());
            LogicalRelation created = reportDatasourceService.addLogicalRelation(id, relation);
            return ResultVOUtils.success("success.datasource.relationCreated", created);
        } catch (Exception e) {
            log.error("添加逻辑外键失败", e);
            return ResultVOUtils.error("error.datasource.relationCreateFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 更新逻辑外键
     *
     * @param id         数据源ID
     * @param relationId 逻辑外键ID
     * @param dto        更新逻辑外键DTO
     * @return 更新后的逻辑外键
     */
    @PutMapping("/{id}/logical_relations/update/{relationId}")
    public ResultVO<LogicalRelation> updateLogicalRelation(@PathVariable String id,
                                                               @PathVariable String relationId,
                                                               @RequestBody UpdateLogicalRelationDTO dto) {
        try {
            LogicalRelation relation = new LogicalRelation();
            relation.setSourceTableName(dto.getSourceTableName());
            relation.setSourceColumnName(dto.getSourceColumnName());
            relation.setTargetTableName(dto.getTargetTableName());
            relation.setTargetColumnName(dto.getTargetColumnName());
            relation.setRelationType(dto.getRelationType());
            relation.setDescription(dto.getDescription());
            LogicalRelation updated = reportDatasourceService.updateLogicalRelation(id, relationId, relation);
            return ResultVOUtils.success("success.datasource.relationUpdated", updated);
        } catch (Exception e) {
            log.error("更新逻辑外键失败", e);
            return ResultVOUtils.error("error.datasource.relationUpdateFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 删除逻辑外键
     *
     * @param id         数据源ID
     * @param relationId 逻辑外键ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}/logical_relations/delete/{relationId}")
    public ResultVO<String> deleteLogicalRelation(@PathVariable String id,
                                                    @PathVariable String relationId) {
        try {
            reportDatasourceService.deleteLogicalRelation(id, relationId);
            return ResultVOUtils.success("success.datasource.relationDeleted", ReportI18n.getMessage("success.datasource.relationDeleted"));
        } catch (Exception e) {
            log.error("删除逻辑外键失败", e);
            return ResultVOUtils.error("error.datasource.relationDeleteFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 批量保存逻辑外键（替换现有的所有外键）
     *
     * @param id               数据源ID
     * @param logicalRelations 逻辑外键列表
     * @return 保存后的逻辑外键列表
     */
    @PutMapping("/{id}/logical_relations/save")
    public ResultVO<List<LogicalRelation>> saveLogicalRelations(@PathVariable String id,
                                                                    @RequestBody List<LogicalRelation> logicalRelations) {
        try {
            List<LogicalRelation> saved = reportDatasourceService.saveLogicalRelations(id, logicalRelations);
            return ResultVOUtils.success("success.datasource.relationsSaved", saved);
        } catch (Exception e) {
            log.error("批量保存逻辑外键失败", e);
            return ResultVOUtils.error("error.datasource.relationsSaveFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 构建 Schema：向量检索召回相关表结构并合并逻辑外键
     *
     * @param id    数据源ID
     * @param query 用户自然语言查询
     * @return SchemaDTO
     */
    @PostMapping("/{id}/build_schema")
    public ResultVO<SchemaDTO> buildSchemaDTO(@PathVariable String id,
                                                 @RequestParam(value = "query") String query) {
        try {
            SchemaDTO schemaDTO = reportDatasourceService.buildSchemaDTO(id, query);
            return ResultVOUtils.success("success.datasource.schemaBuilt", schemaDTO);
        } catch (Exception e) {
            log.error("构建SchemaDTO失败", e);
            return ResultVOUtils.error("error.datasource.schemaBuildFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 获取与查询相关的表结构信息（结构化 SchemaDTO）
     * 传入查询文本，通过向量检索召回相关表并组装为结构化 SchemaDTO
     * 支持通过数据源ID或名称查询（二选一），返回结构化数据由前端/Agent 转发给 LLM 消费
     *
     * @param name   数据源名称（与id二选一）
     * @param id     数据源ID（与name二选一）
     * @param query  用户自然语言查询
     * @return SchemaDTO 结构化数据（包含表结构、字段、外键关系）
     */
    @PostMapping("/table_relations")
    public ResultVO<SchemaDTO> getTableRelations(
        @RequestParam(value = "name", required = false) String name,
        @RequestParam(value = "id", required = false) String id,
        @RequestParam(value = "query") String query) {
        try {
            // 优先使用ID，ID为空时通过名称查询
            String datasourceId = id;
            if (datasourceId == null && name != null) {
                ReportDatasourceVO reportDatasource = reportDatasourceService.getDatasourceByName(name);
                if (reportDatasource == null) {
                    return ResultVOUtils.error("error.datasource.notExistNamed", name);
                }
                datasourceId = reportDatasource.getId();
            }

            if (datasourceId == null) {
                return ResultVOUtils.error("error.datasource.idOrNameRequired");
            }

            SchemaDTO schemaDTO = reportDatasourceService.getTableRelations(datasourceId, query);
            return ResultVOUtils.success("success.datasource.tableSchemaLoaded", schemaDTO);
        } catch (Exception e) {
            log.error("获取表结构信息失败", e);
            return ResultVOUtils.error("error.datasource.tableSchemaFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 跨数据源搜索Schema
     * 遍历所有active状态的数据源，通过向量检索召回与查询相关的表结构
     * 返回每个匹配数据源的基本信息和格式化的Schema提示词，供Agent快速定位合适的数据源
     *
     * @param query 用户自然语言查询
     * @return 搜索结果列表，每项包含数据源ID、名称、类型和Schema提示词
     */
    @PostMapping("/search_schema")
    public ResultVO<List<SchemaSearchResultVO>> searchSchema(
            @RequestParam(value = "query") String query) {
        try {
            List<SchemaSearchResultVO> results = reportDatasourceService.searchSchema(query);
            return ResultVOUtils.success("success.datasource.schemaSearched", results);
        } catch (Exception e) {
            log.error("搜索Schema失败", e);
            return ResultVOUtils.error("error.datasource.schemaSearchFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 获取内置数据源列表（包含ID和名称）
     * 返回所有注册到Spring容器的BuildinDatasource Bean信息
     * 用于设计器端获取可用的数据源列表
     *
     * @return 内置数据源列表，每项包含name和id
     */
    @GetMapping("/builtin/list")
    public ResultVO<List<Map<String, Object>>> getBuildinDatasources() {
        try {
            Collection<BuildinDatasource> datasources = Utils.getBuildinDatasources();
            List<Map<String, Object>> result = new ArrayList<>();

            for (BuildinDatasource ds : datasources) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", ds.name());
                item.put("id", ds.getId());
                result.add(item);
            }

            return ResultVOUtils.success("success.datasource.builtinListLoaded", result);
        } catch (Exception e) {
            log.error("获取内置数据源列表失败", e);
            return ResultVOUtils.error("error.datasource.builtinListFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * VO转实体
     *
     * @param vo 数据源VO
     * @return 数据源实体
     */
    private ReportDatasource toEntity(ReportDatasourceVO vo) {
        ReportDatasource entity = new ReportDatasource();
        entity.setId(vo.getId());
        entity.setName(vo.getName());
        entity.setType(vo.getType());
        entity.setHost(vo.getHost());
        entity.setPort(vo.getPort());
        entity.setDatabaseName(vo.getDatabaseName());
        entity.setUsername(vo.getUsername());
        entity.setPassword(vo.getPassword());
        entity.setConnectionUrl(vo.getConnectionUrl());
        entity.setEnabled(vo.getEnabled());
        entity.setDescription(vo.getDescription());
        entity.setCreateBy(vo.getCreateBy());
        return entity;
    }
}
