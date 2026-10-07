package com.luck.report.web.modules.datasource.service.impl;

import com.luck.report.core.exception.ReportBizException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luck.report.web.modules.vector.service.impl.AgentVectorStore;
import com.luck.report.web.utils.SnowflakeIdGenerator;
import com.luck.report.web.common.domain.vo.PageResultVO;
import com.luck.report.web.modules.datasource.domain.dto.ColumnDTO;
import com.luck.report.web.modules.datasource.domain.dto.ReportDatasourceQueryDTO;
import com.luck.report.web.modules.datasource.domain.dto.ForeignKeyDTO;
import com.luck.report.web.modules.datasource.domain.dto.SchemaDTO;
import com.luck.report.web.modules.datasource.domain.vo.SchemaSearchResultVO;
import com.luck.report.web.modules.datasource.domain.dto.TableDTO;
import com.luck.report.web.modules.datasource.domain.entity.ReportDatasource;
import com.luck.report.web.modules.datasource.domain.enums.DatasourceTestStatusEnum;
import com.luck.report.web.modules.datasource.domain.entity.LogicalRelation;
import com.luck.report.web.modules.datasource.domain.vo.ReportDatasourceVO;
import com.luck.report.web.modules.datasource.service.impl.BuildinDatasourceLoader;
import com.luck.report.web.modules.datasource.handler.DatasourceTypeHandler;
import com.luck.report.web.modules.datasource.handler.DatasourceTypeHandlerRegistry;
import com.luck.report.web.modules.datasource.mapper.ReportDatasourceMapper;
import com.luck.report.web.modules.datasource.mapper.LogicalRelationMapper;
import com.luck.report.web.modules.dataset.mapper.ReportDatasetMapper;
import com.luck.report.web.modules.datasource.service.ReportDatasourceService;
import com.luck.report.web.security.utils.SecurityUtils;
import com.luck.report.core.security.SensitiveConfigCipher;
import com.luck.report.infra.modules.vector.domain.dto.VectorStoreSearchResult;
import com.luck.report.infra.modules.vector.domain.entity.VectorDocument;
import com.luck.report.infra.modules.vector.domain.param.VectorSearchParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据源服务实现
 *
 * @author luck
 */
@Slf4j
@Service("bean.datasourceService")
@AllArgsConstructor
public class ReportDatasourceServiceImpl implements ReportDatasourceService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Qualifier("bean.reportDatasourceMapper")
    private final ReportDatasourceMapper reportDatasourceMapper;
    @Qualifier("bean.logicalRelationMapper")
    private final LogicalRelationMapper logicalRelationMapper;
    @Qualifier("bean.reportDatasetMapper")
    private final ReportDatasetMapper reportDatasetMapper;
    @Qualifier("bean.dynamicDatasourceManager")
    private final DynamicDatasourceManager dynamicDatasourceManager;
    @Qualifier("bean.datasourceTypeHandlerRegistry")
    private final DatasourceTypeHandlerRegistry handlerRegistry;
    @Qualifier("bean.agentVectorStore")
    private final AgentVectorStore agentVectorStore;
    /**
     * 内置数据源加载器，用于同步更新数据源缓存
     */
    @Qualifier("bean.buildinDatasourceLoader")
    private final BuildinDatasourceLoader buildinDatasourceLoader;

    @Override
    public List<ReportDatasourceVO> list(ReportDatasourceQueryDTO queryDTO) {
        if (queryDTO == null) {
            queryDTO = new ReportDatasourceQueryDTO();
        }
        List<ReportDatasource> list = reportDatasourceMapper.selectList(queryDTO);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public ReportDatasourceVO getById(String id) {
        ReportDatasource reportDatasource = reportDatasourceMapper.selectById(id);
        return reportDatasource != null ? toVO(reportDatasource) : null;
    }

    @Override
    public ReportDatasourceVO create(ReportDatasource reportDatasource) {
        DatasourceTypeHandler handler = handlerRegistry.getRequired(reportDatasource.getType());
        String connectionUrl = handler.resolveConnectionUrl(reportDatasource);
        if (StringUtils.isNotBlank(connectionUrl)) {
            reportDatasource.setConnectionUrl(connectionUrl);
        }

        if (reportDatasource.getEnabled() == null) {
            reportDatasource.setEnabled(false);
        }
        if (reportDatasource.getTestStatus() == null) {
            reportDatasource.setTestStatus(DatasourceTestStatusEnum.UNKNOWN.getValue());
        }
        if (reportDatasource.getPassword() == null) {
            reportDatasource.setPassword("");
        }
        if (reportDatasource.getUsername() == null) {
            reportDatasource.setUsername("");
        }
        reportDatasource.setPassword(SensitiveConfigCipher.encrypt(reportDatasource.getPassword()));

        reportDatasource.setId(SnowflakeIdGenerator.generateId());
        String userId = SecurityUtils.getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();
        reportDatasource.setCreateBy(userId);
        reportDatasource.setUpdateBy(userId);
        reportDatasource.setCreateTime(now);
        reportDatasource.setUpdateTime(now);
        reportDatasource.setDelFlag(0);

        reportDatasourceMapper.insert(reportDatasource);
        buildinDatasourceLoader.addOrUpdateDatasource(reportDatasource);
        log.info("创建数据源: id={}, name={}, type={}", reportDatasource.getId(), reportDatasource.getName(), reportDatasource.getType());
        return toVO(reportDatasource);
    }

    @Override
    public ReportDatasourceVO update(String id, ReportDatasource reportDatasource) {
        DatasourceTypeHandler handler = handlerRegistry.getRequired(reportDatasource.getType());
        String connectionUrl = handler.resolveConnectionUrl(reportDatasource);
        if (StringUtils.isNotBlank(connectionUrl)) {
            reportDatasource.setConnectionUrl(connectionUrl);
        }
        reportDatasource.setId(id);

        ReportDatasource oldReportDatasource = reportDatasourceMapper.selectById(id);
        if (StringUtils.isBlank(reportDatasource.getPassword())) {
            if (oldReportDatasource != null) {
                reportDatasource.setPassword(oldReportDatasource.getPassword());
            }
        } else {
            reportDatasource.setPassword(SensitiveConfigCipher.encrypt(reportDatasource.getPassword()));
        }
        if (reportDatasource.getEnabled() == null) {
            if (oldReportDatasource != null) {
                reportDatasource.setEnabled(oldReportDatasource.getEnabled());
            }
        }
        if (reportDatasource.getUsername() == null) {
            reportDatasource.setUsername("");
        }

        reportDatasource.setUpdateBy(SecurityUtils.getCurrentUserId());
        reportDatasource.setUpdateTime(LocalDateTime.now());
        reportDatasourceMapper.updateById(reportDatasource);
        dynamicDatasourceManager.removeDatasourcePool(id);
        ReportDatasource updated = reportDatasourceMapper.selectById(id);
        buildinDatasourceLoader.addOrUpdateDatasource(updated);
        log.info("更新数据源: id={}", id);
        return toVO(updated);
    }

    @Override
    @Transactional
    public void removeById(String id) {
        ReportDatasource reportDatasource = reportDatasourceMapper.selectById(id);
        String datasourceName = reportDatasource != null ? reportDatasource.getName() : null;

        Long reportDatasetCount = reportDatasetMapper.countByDatasourceId(id);
        if (reportDatasetCount != null && reportDatasetCount > 0) {
            throw new ReportBizException("error.datasource.referenced", reportDatasetCount);
        }

        logicalRelationMapper.deleteByDatasourceId(id);
        reportDatasourceMapper.deleteById(id);
        dynamicDatasourceManager.removeDatasourcePool(id);
        if (datasourceName != null) {
            buildinDatasourceLoader.removeDatasource(datasourceName);
        }
        for (String vectorType : Arrays.asList("TABLE", "COLUMN", "DATASOURCE")) {
            agentVectorStore.deleteByMetadata(vectorType, "datasourceId", id);
        }
        log.info("删除数据源: id={}", id);
    }

    @Override
    public void removeByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (String id : ids) {
            if (id != null && !id.isEmpty()) {
                removeById(id);
            }
        }
    }

    @Override
    public boolean testConnection(String id) {
        ReportDatasource reportDatasource = reportDatasourceMapper.selectById(id);
        if (reportDatasource == null) {
            return false;
        }
        try {
            boolean success = dynamicDatasourceManager.testConnection(reportDatasource);
            log.info("数据源连接测试: id={}, name={}, result={}", id, reportDatasource.getName(), success);
            reportDatasourceMapper.updateTestStatusById(id, success ? DatasourceTestStatusEnum.SUCCESS.getValue() : DatasourceTestStatusEnum.FAILED.getValue(), LocalDateTime.now());
            return success;
        } catch (Exception e) {
            reportDatasourceMapper.updateTestStatusById(id, DatasourceTestStatusEnum.FAILED.getValue(), LocalDateTime.now());
            log.error("数据源连接测试异常: id={}, error={}", id, e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> getDatasourceTables(String id) throws Exception {
        ReportDatasource reportDatasource = reportDatasourceMapper.selectById(id);
        if (reportDatasource == null) {
            throw new ReportBizException("error.datasource.notExistId", id);
        }
        return dynamicDatasourceManager.getDatasourceTables(reportDatasource);
    }

    @Override
    public List<String> getTableColumns(String id, String tableName) throws Exception {
        ReportDatasource reportDatasource = reportDatasourceMapper.selectById(id);
        if (reportDatasource == null) {
            throw new ReportBizException("error.datasource.notExistId", id);
        }
        return dynamicDatasourceManager.getTableColumns(reportDatasource, tableName);
    }

    @Override
    public void initTableSchema(String id, List<String> tables, String modelId) throws Exception {
        ReportDatasource reportDatasource = reportDatasourceMapper.selectById(id);
        if (reportDatasource == null) {
            throw new ReportBizException("error.datasource.notExistId", id);
        }

        for (String vectorType : Arrays.asList("TABLE", "COLUMN", "DATASOURCE")) {
            agentVectorStore.deleteByMetadata(vectorType, "datasourceId", id);
        }

        Map<String, List<String>> foreignKeyMap = new HashMap<>();
        try {
            List<String> physicalForeignKeys = dynamicDatasourceManager.getForeignKeys(reportDatasource);
            for (String fk : physicalForeignKeys) {
                String[] parts = fk.split("=");
                if (parts.length == 2) {
                    String[] sourceParts = parts[0].trim().split("\\.");
                    if (sourceParts.length == 2) {
                        String sourceTable = sourceParts[0];
                        foreignKeyMap.computeIfAbsent(sourceTable, k -> new ArrayList<>()).add(fk);
                    }
                    String[] targetParts = parts[1].trim().split("\\.");
                    if (targetParts.length == 2) {
                        String targetTable = targetParts[0];
                        foreignKeyMap.computeIfAbsent(targetTable, k -> new ArrayList<>()).add(fk);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("查询物理外键失败: datasourceId={}, error={}", id, e.getMessage());
        }

        List<VectorDocument> tableDocuments = new ArrayList<>();
        List<VectorDocument> columnDocuments = new ArrayList<>();

        for (String tableName : tables) {
            try {
                String tableComment = dynamicDatasourceManager.getTableComment(reportDatasource, tableName);
                List<String> columnDetails = dynamicDatasourceManager.getTableColumnsDetail(reportDatasource, tableName);
                List<String> primaryKeys = dynamicDatasourceManager.getTablePrimaryKeys(reportDatasource, tableName);
                Map<String, List<String>> sampleData = dynamicDatasourceManager.getTableSampleData(reportDatasource, tableName, 5);

                List<String> tableForeignKeys = foreignKeyMap.getOrDefault(tableName, new ArrayList<>());

                String tableContent = StringUtils.isNotBlank(tableComment) ? tableComment : tableName;
                Map<String, Object> tableMeta = new HashMap<>();
                tableMeta.put("vectorType", "TABLE");
                tableMeta.put("datasourceId", id);
                tableMeta.put("name", tableName);
                tableMeta.put("description", tableComment != null ? tableComment : "");
                tableMeta.put("primaryKeys", String.join(",", primaryKeys));
                tableMeta.put("foreignKey", String.join("、", tableForeignKeys));
                tableMeta.put("datasourceName", reportDatasource.getName());
                tableMeta.put("datasourceType", reportDatasource.getType());
                tableDocuments.add(new VectorDocument(tableContent, tableMeta));

                for (String col : columnDetails) {
                    String[] parts = col.split("\\|");
                    String colName = parts.length > 0 ? parts[0].trim() : "";
                    String colType = parts.length > 1 ? parts[1].trim() : "";
                    String colComment = parts.length > 2 ? parts[2].trim() : "";
                    boolean isPrimary = primaryKeys.contains(colName);

                    String colContent = StringUtils.isNotBlank(colComment) ? colComment : colName;
                    List<String> samples = sampleData.getOrDefault(colName, new ArrayList<>());
                    List<String> filteredSamples = samples.stream()
                            .filter(Objects::nonNull)
                            .distinct()
                            .limit(3)
                            .filter(s -> s.length() <= 100)
                            .collect(Collectors.toList());

                    Map<String, Object> colMeta = new HashMap<>();
                    colMeta.put("vectorType", "COLUMN");
                    colMeta.put("datasourceId", id);
                    colMeta.put("tableName", tableName);
                    colMeta.put("name", colName);
                    colMeta.put("description", colComment);
                    colMeta.put("type", colType);
                    colMeta.put("primary", isPrimary);
                    try {
                        colMeta.put("samples", objectMapper.writeValueAsString(filteredSamples));
                    } catch (Exception e) {
                        log.warn("序列化字段示例数据失败: table={}, column={}, error={}", tableName, colName, e.getMessage());
                        colMeta.put("samples", "[]");
                    }
                    columnDocuments.add(new VectorDocument(colContent, colMeta));
                }
            } catch (Exception e) {
                log.error("获取表Schema失败: datasourceId={}, table={}, error={}", id, tableName, e.getMessage());
            }
        }

        int batchSize = 10;
        List<VectorDocument> allDocuments = new ArrayList<>();
        allDocuments.addAll(tableDocuments);
        allDocuments.addAll(columnDocuments);

        for (int i = 0; i < allDocuments.size(); i += batchSize) {
            int end = Math.min(i + batchSize, allDocuments.size());
            List<VectorDocument> batch = allDocuments.subList(i, end);
            agentVectorStore.addDocuments(new ArrayList<>(batch), modelId);
            log.info("向量化第 {}/{} 批, 本批 {} 条文档", (i / batchSize + 1),
                    (allDocuments.size() + batchSize - 1) / batchSize, batch.size());
        }
        log.info("初始化数据源Schema到向量库: datasourceId={}, tables={}, TABLE文档={}, COLUMN文档={}, 总文档数={}",
                id, tables.size(), tableDocuments.size(), columnDocuments.size(), allDocuments.size());

        try {
            String initializedTablesJson = objectMapper.writeValueAsString(tables);
            reportDatasourceMapper.updateInitializedTables(id, initializedTablesJson, LocalDateTime.now());
            log.info("保存已初始化表列表: datasourceId={}, tables={}", id, tables);
        } catch (Exception e) {
            log.warn("保存已初始化表列表失败: datasourceId={}, error={}", id, e.getMessage());
        }
    }

    @Override
    public void updateEnabledStatus(String id, Boolean enabled) {
        reportDatasourceMapper.updateEnabledById(id, enabled, LocalDateTime.now());
        ReportDatasource reportDatasource = reportDatasourceMapper.selectById(id);
        if (reportDatasource != null) {
            if (Boolean.TRUE.equals(enabled)) {
                buildinDatasourceLoader.addOrUpdateDatasource(reportDatasource);
            } else {
                buildinDatasourceLoader.removeDatasource(reportDatasource.getName());
            }
        }
        log.info("更新数据源启用状态: id={}, enabled={}", id, enabled);
    }

    @Override
    public List<LogicalRelation> getLogicalRelations(String datasourceId) {
        return logicalRelationMapper.selectByDatasourceId(datasourceId);
    }

    @Override
    public LogicalRelation addLogicalRelation(String datasourceId, LogicalRelation logicalRelation) {
        logicalRelation.setDatasourceId(datasourceId);

        int exists = logicalRelationMapper.checkExists(datasourceId,
                logicalRelation.getSourceTableName(), logicalRelation.getSourceColumnName(),
                logicalRelation.getTargetTableName(), logicalRelation.getTargetColumnName());
        if (exists > 0) {
            throw new ReportBizException("error.datasource.relationExists");
        }

        logicalRelation.setId(SnowflakeIdGenerator.generateId());
        String userId = SecurityUtils.getCurrentUserId();
        logicalRelation.setCreateBy(userId);
        logicalRelation.setUpdateBy(userId);
        logicalRelation.setCreateTime(LocalDateTime.now());
        logicalRelation.setUpdateTime(LocalDateTime.now());
        logicalRelation.setDelFlag(0);
        logicalRelationMapper.insert(logicalRelation);
        log.info("添加逻辑外键: datasourceId={}, id={}", datasourceId, logicalRelation.getId());
        return logicalRelation;
    }

    @Override
    public LogicalRelation updateLogicalRelation(String datasourceId, String relationId, LogicalRelation logicalRelation) {
        LogicalRelation existing = logicalRelationMapper.selectById(relationId);
        if (existing == null) {
            throw new ReportBizException("error.datasource.relationNotExist", relationId);
        }
        if (!existing.getDatasourceId().equals(datasourceId)) {
            throw new ReportBizException("error.datasource.relationNotMatch");
        }

        logicalRelation.setId(relationId);
        logicalRelation.setDatasourceId(datasourceId);
        logicalRelation.setUpdateBy(SecurityUtils.getCurrentUserId());
        logicalRelation.setUpdateTime(LocalDateTime.now());
        logicalRelationMapper.updateById(logicalRelation);
        log.info("更新逻辑外键: datasourceId={}, relationId={}", datasourceId, relationId);
        return logicalRelationMapper.selectById(relationId);
    }

    @Override
    public void removeLogicalRelation(String datasourceId, String relationId) {
        LogicalRelation existing = logicalRelationMapper.selectById(relationId);
        if (existing == null) {
            throw new ReportBizException("error.datasource.relationNotExist", relationId);
        }
        if (!existing.getDatasourceId().equals(datasourceId)) {
            throw new ReportBizException("error.datasource.relationNotMatch");
        }

        logicalRelationMapper.deleteById(relationId);
        log.info("删除逻辑外键: datasourceId={}, relationId={}", datasourceId, relationId);
    }

    @Override
    @Transactional
    public List<LogicalRelation> saveLogicalRelations(String datasourceId, List<LogicalRelation> logicalRelations) {
        List<LogicalRelation> existingRelations = logicalRelationMapper.selectByDatasourceId(datasourceId);
        Map<String, LogicalRelation> existingMap = existingRelations.stream()
                .collect(Collectors.toMap(LogicalRelation::getId, r -> r));

        Set<String> incomingIds = logicalRelations.stream()
                .map(LogicalRelation::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (LogicalRelation existing : existingRelations) {
            if (!incomingIds.contains(existing.getId())) {
                logicalRelationMapper.deleteById(existing.getId());
            }
        }

        List<LogicalRelation> uniqueRelations = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (LogicalRelation relation : logicalRelations) {
            String key = relation.getSourceTableName() + "|" + relation.getSourceColumnName() + "|"
                    + relation.getTargetTableName() + "|" + relation.getTargetColumnName();
            if (!seen.contains(key)) {
                seen.add(key);
                uniqueRelations.add(relation);
            }
        }

        for (LogicalRelation relation : uniqueRelations) {
            relation.setDatasourceId(datasourceId);
            if (relation.getId() != null && existingMap.containsKey(relation.getId())) {
                relation.setUpdateBy(SecurityUtils.getCurrentUserId());
                logicalRelationMapper.updateById(relation);
            } else {
                relation.setId(SnowflakeIdGenerator.generateId());
                String userId = SecurityUtils.getCurrentUserId();
                relation.setCreateBy(userId);
                relation.setUpdateBy(userId);
                relation.setCreateTime(LocalDateTime.now());
                relation.setUpdateTime(LocalDateTime.now());
                relation.setDelFlag(0);
                logicalRelationMapper.insert(relation);
            }
        }

        log.info("批量保存逻辑外键: datasourceId={}, count={}", datasourceId, uniqueRelations.size());
        return logicalRelationMapper.selectByDatasourceId(datasourceId);
    }

    /**
     * 实体转VO
     *
     * @param reportDatasource 数据源实体
     * @return 数据源VO
     */
    private ReportDatasourceVO toVO(ReportDatasource reportDatasource) {
        return ReportDatasourceVO.builder()
                .id(reportDatasource.getId())
                .name(reportDatasource.getName())
                .type(reportDatasource.getType())
                .host(reportDatasource.getHost())
                .port(reportDatasource.getPort())
                .databaseName(reportDatasource.getDatabaseName())
                .username(reportDatasource.getUsername())
                .connectionUrl(reportDatasource.getConnectionUrl())
                .enabled(reportDatasource.getEnabled())
                .testStatus(reportDatasource.getTestStatus())
                .description(reportDatasource.getDescription())
                .initializedTables(reportDatasource.getInitializedTables())
                .createBy(reportDatasource.getCreateBy())
                .createTime(reportDatasource.getCreateTime())
                .updateTime(reportDatasource.getUpdateTime())
                .build();
    }

    @Override
    public SchemaDTO buildSchemaDTO(String datasourceId, String query) {
        ReportDatasource reportDatasource = reportDatasourceMapper.selectById(datasourceId);
        if (reportDatasource == null) {
            throw new ReportBizException("error.datasource.notExistId", datasourceId);
        }

        List<VectorStoreSearchResult> tableSearchResults = agentVectorStore.search(query,
            VectorSearchParam.builder()
                .topK(10).threshold(0.5)
                .vectorType("TABLE")
                .metadataEquals(Collections.singletonMap("datasourceId", datasourceId))
                .build());

        if (tableSearchResults.isEmpty()) {
            log.warn("向量检索未找到相关表Schema: datasourceId={}, query={}", datasourceId, query);
            return SchemaDTO.builder()
                    .name(reportDatasource.getDatabaseName())
                    .tableCount(0)
                    .table(new ArrayList<>())
                    .foreignKeys(new ArrayList<>())
                    .build();
        }

        Set<String> recalledTableNames = new LinkedHashSet<>();
        Map<String, VectorDocument> tableDocMap = new LinkedHashMap<>();
        for (VectorStoreSearchResult result : tableSearchResults) {
            VectorDocument doc = result.getDocument();
            if (doc == null || doc.getMetadata() == null) {
                continue;
            }
            String tableName = (String) doc.getMetadata().get("name");
            if (tableName != null && !recalledTableNames.contains(tableName)) {
                recalledTableNames.add(tableName);
                tableDocMap.put(tableName, doc);
            }
        }

        expandMissingForeignKeyTables(datasourceId, tableDocMap, recalledTableNames);
        Map<String, List<VectorDocument>> columnDocMap = loadColumnDocsByTableNames(datasourceId, recalledTableNames);
        List<TableDTO> tableList = buildTableList(recalledTableNames, tableDocMap, columnDocMap);

        List<ForeignKeyDTO> foreignKeyList = new ArrayList<>();
        Set<String> foreignKeyKeys = new LinkedHashSet<>();

        for (VectorDocument tableDoc : tableDocMap.values()) {
            String fkStr = (String) tableDoc.getMetadata().getOrDefault("foreignKey", "");
            if (StringUtils.isBlank(fkStr)) {
                continue;
            }
            for (String fk : fkStr.split("、")) {
                ForeignKeyDTO parsed = parseForeignKeyString(fk);
                if (parsed != null && foreignKeyKeys.add(foreignKeyKey(parsed))) {
                    foreignKeyList.add(parsed);
                }
            }
        }

        List<LogicalRelation> allRelations = logicalRelationMapper.selectByDatasourceId(datasourceId);
        for (LogicalRelation relation : allRelations) {
            boolean sourceInRecalled = recalledTableNames.contains(relation.getSourceTableName());
            boolean targetInRecalled = recalledTableNames.contains(relation.getTargetTableName());
            if (!sourceInRecalled && !targetInRecalled) {
                continue;
            }
            ForeignKeyDTO fk = ForeignKeyDTO.builder()
                    .sourceTable(relation.getSourceTableName())
                    .sourceColumn(relation.getSourceColumnName())
                    .targetTable(relation.getTargetTableName())
                    .targetColumn(relation.getTargetColumnName())
                    .build();
            if (foreignKeyKeys.add(foreignKeyKey(fk))) {
                foreignKeyList.add(fk);
            }
        }

        return SchemaDTO.builder()
                .name(reportDatasource.getDatabaseName())
                .description(reportDatasource.getDescription())
                .tableCount(tableList.size())
                .table(tableList)
                .foreignKeys(foreignKeyList)
                .build();
    }

    @Override
    public SchemaDTO getTableRelations(String datasourceId, String query) {
        return buildSchemaDTO(datasourceId, query);
    }

    @Override
    public ReportDatasourceVO getByName(String name) {
        List<ReportDatasource> list = reportDatasourceMapper.selectList(
                ReportDatasourceQueryDTO.builder().name(name).build());
        return list.isEmpty() ? null : toVO(list.get(0));
    }

    /**
     * 从向量文档metadata构建TableDTO
     *
     * @param tableDoc   TABLE类型的向量文档
     * @param columnDocs 该表对应的COLUMN类型向量文档列表
     * @return TableDTO
     */
    private TableDTO buildTableDTOFromMetadata(VectorDocument tableDoc, List<VectorDocument> columnDocs) {
        Map<String, Object> tableMeta = tableDoc.getMetadata();
        String tableName = (String) tableMeta.get("name");
        String tableDescription = (String) tableMeta.getOrDefault("description", "");
        String primaryKeysStr = (String) tableMeta.getOrDefault("primaryKeys", "");
        List<String> primaryKeys = StringUtils.isNotBlank(primaryKeysStr)
                ? Arrays.asList(primaryKeysStr.split(",")) : new ArrayList<>();

        List<ColumnDTO> columns = new ArrayList<>();
        Set<String> seenColumnNames = new LinkedHashSet<>();
        for (VectorDocument colDoc : columnDocs) {
            Map<String, Object> colMeta = colDoc.getMetadata();
            String colName = (String) colMeta.getOrDefault("name", "");
            if (StringUtils.isBlank(colName) || !seenColumnNames.add(colName)) {
                continue;
            }
            ColumnDTO columnDTO = ColumnDTO.builder()
                    .name(colName)
                    .type((String) colMeta.getOrDefault("type", ""))
                    .description((String) colMeta.getOrDefault("description", ""))
                    .build();

            String samplesStr = (String) colMeta.getOrDefault("samples", "");
            if (StringUtils.isNotBlank(samplesStr)) {
                try {
                    List<String> samples = objectMapper.readValue(samplesStr, new TypeReference<List<String>>() {});
                    columnDTO.setData(samples);
                } catch (Exception e) {
                    log.warn("解析字段示例数据失败: column={}, samples={}", columnDTO.getName(), samplesStr);
                }
            }
            columns.add(columnDTO);
        }

        return TableDTO.builder()
                .name(tableName)
                .description(tableDescription)
                .column(columns)
                .primaryKeys(primaryKeys)
                .build();
    }

    /**
     * 按数据源列举 COLUMN 文档并按命中表名分组
     *
     * @param datasourceId 数据源 ID
     * @param tableNames 已召回表名
     * @return 表名到列文档列表
     */
    private Map<String, List<VectorDocument>> loadColumnDocsByTableNames(String datasourceId, Set<String> tableNames) {
        Map<String, List<VectorDocument>> columnDocMap = new LinkedHashMap<>();
        if (tableNames == null || tableNames.isEmpty()) {
            return columnDocMap;
        }
        List<VectorDocument> columnDocs = agentVectorStore.listByMetadata("COLUMN", "datasourceId", datasourceId);
        for (VectorDocument doc : columnDocs) {
            if (doc == null || doc.getMetadata() == null) {
                continue;
            }
            String tableName = (String) doc.getMetadata().get("tableName");
            if (tableName == null || !tableNames.contains(tableName)) {
                continue;
            }
            columnDocMap.computeIfAbsent(tableName, k -> new ArrayList<>()).add(doc);
        }
        return columnDocMap;
    }

    /**
     * 按已召回表物理外键补全缺失 TABLE 文档（一跳）
     *
     * @param datasourceId 数据源 ID
     * @param tableDocMap 表文档（方法内写入补全）
     * @param recalledTableNames 表名集合（方法内写入补全）
     */
    private void expandMissingForeignKeyTables(String datasourceId,
                                               Map<String, VectorDocument> tableDocMap,
                                               Set<String> recalledTableNames) {
        Set<String> missing = new LinkedHashSet<>();
        for (VectorDocument tableDoc : tableDocMap.values()) {
            if (tableDoc == null || tableDoc.getMetadata() == null) {
                continue;
            }
            String fkStr = (String) tableDoc.getMetadata().getOrDefault("foreignKey", "");
            if (StringUtils.isBlank(fkStr)) {
                continue;
            }
            for (String fk : fkStr.split("、")) {
                ForeignKeyDTO parsed = parseForeignKeyString(fk);
                if (parsed == null) {
                    continue;
                }
                if (StringUtils.isNotBlank(parsed.getSourceTable())
                        && !recalledTableNames.contains(parsed.getSourceTable())) {
                    missing.add(parsed.getSourceTable());
                }
                if (StringUtils.isNotBlank(parsed.getTargetTable())
                        && !recalledTableNames.contains(parsed.getTargetTable())) {
                    missing.add(parsed.getTargetTable());
                }
            }
        }
        if (missing.isEmpty()) {
            return;
        }
        List<VectorDocument> tableDocs = agentVectorStore.listByMetadata("TABLE", "datasourceId", datasourceId);
        for (VectorDocument doc : tableDocs) {
            if (doc == null || doc.getMetadata() == null) {
                continue;
            }
            String tableName = (String) doc.getMetadata().get("name");
            if (tableName != null && missing.contains(tableName) && !recalledTableNames.contains(tableName)) {
                recalledTableNames.add(tableName);
                tableDocMap.put(tableName, doc);
            }
        }
    }

    /**
     * 按召回表顺序组装 TableDTO
     *
     * @param recalledTableNames 表名顺序
     * @param tableDocMap 表文档
     * @param columnDocMap 列文档
     * @return 表 DTO 列表
     */
    private List<TableDTO> buildTableList(Set<String> recalledTableNames,
                                          Map<String, VectorDocument> tableDocMap,
                                          Map<String, List<VectorDocument>> columnDocMap) {
        List<TableDTO> tableList = new ArrayList<>();
        for (String tableName : recalledTableNames) {
            VectorDocument tableDoc = tableDocMap.get(tableName);
            if (tableDoc == null) {
                continue;
            }
            tableList.add(buildTableDTOFromMetadata(tableDoc,
                    columnDocMap.getOrDefault(tableName, new ArrayList<>())));
        }
        return tableList;
    }

    /**
     * 分页条件查询数据源
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @Override
    public PageResultVO<ReportDatasourceVO> listPage(ReportDatasourceQueryDTO queryDTO) {
        int offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();

        Long total = reportDatasourceMapper.selectCount(queryDTO);

        List<ReportDatasource> dataList = reportDatasourceMapper.selectPage(queryDTO, offset, queryDTO.getPageSize());
        List<ReportDatasourceVO> dataListVO = dataList.stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return PageResultVO.success(dataListVO, total, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    /**
     * 跨数据源搜索 Schema：TABLE 向量召回后按表拉全列，并按外键补全缺失表
     *
     * @param query 自然语言查询
     * @return 命中数据源及 Schema 列表
     */
    @Override
    public List<SchemaSearchResultVO> searchSchema(String query) {
        List<VectorStoreSearchResult> tableSearchResults = agentVectorStore.search(query,
            VectorSearchParam.builder()
                .topK(30).threshold(0.5)
                .vectorType("TABLE")
                .build());
        if (tableSearchResults.isEmpty()) {
            log.info("跨数据源搜索未命中任何TABLE文档: query={}", query);
            return new ArrayList<>();
        }

        Map<String, List<VectorStoreSearchResult>> tableResultsByDsId = new LinkedHashMap<>();
        Map<String, Map<String, VectorDocument>> tableDocMapByDsId = new LinkedHashMap<>();
        Map<String, Set<String>> recalledTableNamesByDsId = new LinkedHashMap<>();

        for (VectorStoreSearchResult result : tableSearchResults) {
            VectorDocument doc = result.getDocument();
            if (doc == null || doc.getMetadata() == null) {
                continue;
            }
            Object dsIdObj = doc.getMetadata().get("datasourceId");
            if (dsIdObj == null) {
                continue;
            }
            String dsId = String.valueOf(dsIdObj);
            String tableName = (String) doc.getMetadata().get("name");

            tableResultsByDsId.computeIfAbsent(dsId, k -> new ArrayList<>()).add(result);
            tableDocMapByDsId.computeIfAbsent(dsId, k -> new LinkedHashMap<>());
            recalledTableNamesByDsId.computeIfAbsent(dsId, k -> new LinkedHashSet<>());

            if (tableName != null && !recalledTableNamesByDsId.get(dsId).contains(tableName)) {
                recalledTableNamesByDsId.get(dsId).add(tableName);
                tableDocMapByDsId.get(dsId).put(tableName, doc);
            }
        }

        if (tableResultsByDsId.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> hitDsIds = new ArrayList<>(tableResultsByDsId.keySet());
        Map<String, ReportDatasource> datasourceMap = reportDatasourceMapper.selectByIds(hitDsIds).stream()
                .collect(Collectors.toMap(ReportDatasource::getId, ds -> ds, (a, b) -> a));
        Map<String, List<LogicalRelation>> relationMapByDsId = logicalRelationMapper.selectByDatasourceIds(hitDsIds).stream()
                .collect(Collectors.groupingBy(LogicalRelation::getDatasourceId));

        List<SchemaSearchResultVO> results = new ArrayList<>();
        for (String dsId : hitDsIds) {
            ReportDatasource reportDatasource = datasourceMap.get(dsId);
            if (reportDatasource == null || !Boolean.TRUE.equals(reportDatasource.getEnabled())) {
                continue;
            }

            Set<String> recalledTableNames = recalledTableNamesByDsId.get(dsId);
            Map<String, VectorDocument> tableDocMap = tableDocMapByDsId.get(dsId);
            expandMissingForeignKeyTables(dsId, tableDocMap, recalledTableNames);
            Map<String, List<VectorDocument>> columnDocMap = loadColumnDocsByTableNames(dsId, recalledTableNames);
            List<TableDTO> tableList = buildTableList(recalledTableNames, tableDocMap, columnDocMap);

            List<ForeignKeyDTO> foreignKeyList = new ArrayList<>();
            Set<String> foreignKeyKeys = new LinkedHashSet<>();
            for (VectorDocument tableDoc : tableDocMap.values()) {
                String fkStr = (String) tableDoc.getMetadata().getOrDefault("foreignKey", "");
                if (StringUtils.isBlank(fkStr)) {
                    continue;
                }
                for (String fk : fkStr.split("、")) {
                    ForeignKeyDTO parsed = parseForeignKeyString(fk);
                    if (parsed != null && foreignKeyKeys.add(foreignKeyKey(parsed))) {
                        foreignKeyList.add(parsed);
                    }
                }
            }
            List<LogicalRelation> relations = relationMapByDsId.getOrDefault(dsId, new ArrayList<>());
            for (LogicalRelation relation : relations) {
                boolean sourceInRecalled = recalledTableNames.contains(relation.getSourceTableName());
                boolean targetInRecalled = recalledTableNames.contains(relation.getTargetTableName());
                if (!sourceInRecalled && !targetInRecalled) {
                    continue;
                }
                ForeignKeyDTO fk = ForeignKeyDTO.builder()
                        .sourceTable(relation.getSourceTableName())
                        .sourceColumn(relation.getSourceColumnName())
                        .targetTable(relation.getTargetTableName())
                        .targetColumn(relation.getTargetColumnName())
                        .build();
                if (foreignKeyKeys.add(foreignKeyKey(fk))) {
                    foreignKeyList.add(fk);
                }
            }

            SchemaDTO schemaDTO = SchemaDTO.builder()
                    .name(reportDatasource.getDatabaseName())
                    .description(reportDatasource.getDescription())
                    .tableCount(tableList.size())
                    .table(tableList)
                    .foreignKeys(foreignKeyList)
                    .build();

            results.add(SchemaSearchResultVO.builder()
                    .datasourceId(dsId)
                    .datasourceName(reportDatasource.getName())
                    .datasourceType(reportDatasource.getType())
                    .schema(schemaDTO)
                    .build());

            log.info("跨数据源搜索命中: datasourceId={}, name={}, 匹配表数={}",
                    dsId, reportDatasource.getName(), tableList.size());
        }

        log.info("跨数据源搜索完成: query={}, 命中数据源数={}", query, results.size());
        return results;
    }

    /**
     * 解析物理外键字符串（格式：t1.col1=t2.col2）
     *
     * @param fkStr 物理外键字符串
     * @return 解析成功返回 ForeignKeyDTO，格式不合法返回 null
     */
    private ForeignKeyDTO parseForeignKeyString(String fkStr) {
        if (StringUtils.isBlank(fkStr)) {
            return null;
        }
        String[] sides = fkStr.split("=", 2);
        if (sides.length != 2) {
            return null;
        }
        String[] source = sides[0].split("\\.", 2);
        String[] target = sides[1].split("\\.", 2);
        if (source.length != 2 || target.length != 2) {
            return null;
        }
        if (StringUtils.isBlank(source[0]) || StringUtils.isBlank(source[1])
                || StringUtils.isBlank(target[0]) || StringUtils.isBlank(target[1])) {
            return null;
        }
        return ForeignKeyDTO.builder()
                .sourceTable(source[0].trim())
                .sourceColumn(source[1].trim())
                .targetTable(target[0].trim())
                .targetColumn(target[1].trim())
                .build();
    }

    /**
     * 外键去重 key（按 sourceTable.sourceColumn=targetTable.targetColumn 拼接）
     */
    private String foreignKeyKey(ForeignKeyDTO fk) {
        return fk.getSourceTable() + "." + fk.getSourceColumn()
                + "=" + fk.getTargetTable() + "." + fk.getTargetColumn();
    }
}
