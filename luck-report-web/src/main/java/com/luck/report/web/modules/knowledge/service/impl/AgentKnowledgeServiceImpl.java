package com.luck.report.web.modules.knowledge.service.impl;

import com.luck.report.core.exception.ReportBizException;
import com.luck.report.web.modules.knowledge.domain.enums.EmbeddingStatus;
import com.luck.report.web.modules.vector.service.impl.AgentVectorStore;
import com.luck.report.web.utils.SnowflakeIdGenerator;
import com.luck.report.web.modules.knowledge.constant.AgentKnowledgeMetadataConstant;
import com.luck.report.web.modules.knowledge.converter.AgentKnowledgeConverter;
import com.luck.report.web.modules.knowledge.domain.dto.AgentKnowledgeQueryDTO;
import com.luck.report.web.modules.knowledge.domain.dto.CreateAgentKnowledgeDTO;
import com.luck.report.web.modules.knowledge.domain.dto.UpdateAgentKnowledgeDTO;
import com.luck.report.web.modules.knowledge.domain.dto.UpdateKnowledgeChunkDTO;
import com.luck.report.web.modules.knowledge.domain.entity.AgentKnowledge;
import com.luck.report.web.modules.knowledge.domain.enums.KnowledgeType;
import com.luck.report.web.modules.knowledge.domain.vo.AgentKnowledgeVO;
import com.luck.report.web.modules.knowledge.domain.vo.KnowledgeChunkVO;
import com.luck.report.web.modules.knowledge.handler.splitter.TextSplitter;
import com.luck.report.web.modules.knowledge.handler.splitter.TextSplitterFactory;
import com.luck.report.web.common.domain.vo.PageResultVO;
import com.luck.report.web.modules.knowledge.mapper.AgentKnowledgeMapper;
import com.luck.report.web.modules.knowledge.handler.parser.DocumentParserFactory;
import com.luck.report.web.modules.knowledge.service.AgentKnowledgeService;
import com.luck.report.web.modules.knowledge.service.KnowledgeChunkService;
import com.luck.report.web.modules.knowledge.utils.AgentKnowledgeChunkMerger;
import com.luck.report.web.modules.knowledge.utils.KnowledgeContentCleaner;
import com.luck.report.web.modules.knowledge.utils.KnowledgeSimpleText;
import com.luck.report.web.modules.knowledge.utils.SplitterTypeResolver;
import com.luck.report.web.modules.knowledge.domain.enums.SplitterType;
import com.luck.report.web.config.properties.KnowledgeRetrievalProperties;
import com.luck.report.web.modules.chat.service.impl.EmbeddingService;
import com.luck.report.infra.modules.vector.domain.entity.VectorDocument;
import com.luck.report.infra.modules.vector.domain.dto.VectorStoreSearchResult;
import com.luck.report.web.security.utils.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 智能体知识服务实现类
 *
 * @author luck
 */
@Slf4j
@Service("bean.agentKnowledgeService")
@AllArgsConstructor
public class AgentKnowledgeServiceImpl implements AgentKnowledgeService {

    @Qualifier("bean.agentKnowledgeMapper")
    private final AgentKnowledgeMapper agentKnowledgeMapper;
    @Qualifier("bean.agentVectorStore")
    private final AgentVectorStore reportAgentVectorStore;
    @Qualifier("bean.agentKnowledgeConverter")
    private final AgentKnowledgeConverter agentKnowledgeConverter;
    @Qualifier("bean.transactionTemplate")
    private final TransactionTemplate transactionTemplate;
    @Qualifier("bean.textSplitterFactory")
    private final TextSplitterFactory textSplitterFactory;
    @Qualifier("bean.documentParserFactory")
    private final DocumentParserFactory documentParserFactory;
    @Qualifier("bean.knowledgeRetrievalProperties")
    private final KnowledgeRetrievalProperties retrievalProperties;
    @Qualifier("bean.embeddingService")
    private final EmbeddingService embeddingService;
    @Qualifier("bean.knowledgeChunkService")
    private final KnowledgeChunkService knowledgeChunkService;

    /**
     * 根据ID查询智能体知识详情
     *
     * @param id 智能体知识ID
     * @return 智能体知识VO
     */
    @Override
    public AgentKnowledgeVO getKnowledgeById(String id) {
        AgentKnowledge knowledge = agentKnowledgeMapper.selectById(id);
        return knowledge == null ? null : agentKnowledgeConverter.toVo(knowledge);
    }

    /**
     * 创建智能体知识
     *
     * @param createKnowledgeDTO 创建智能体知识DTO
     * @return 智能体知识VO
     */
    @Override
    public AgentKnowledgeVO createKnowledge(CreateAgentKnowledgeDTO createKnowledgeDTO) {
        validateCreateKnowledgeDTO(createKnowledgeDTO);

        AgentKnowledge entity = agentKnowledgeConverter.toEntityForCreate(createKnowledgeDTO);
        entity.setId(SnowflakeIdGenerator.generateId());
        entity.setCreateBy(SecurityUtils.getCurrentUserId());
        entity.setUpdateBy(SecurityUtils.getCurrentUserId());

        if (KnowledgeType.DOCUMENT.getValue().equals(createKnowledgeDTO.getType())
                && createKnowledgeDTO.getFile() != null) {
            String fileContent = readFileContent(createKnowledgeDTO.getFile());
            entity.setContent(KnowledgeContentCleaner.prepareDocumentText(fileContent));
            if (!StringUtils.hasText(entity.getSourceFilename())
                    && createKnowledgeDTO.getFile().getOriginalFilename() != null) {
                entity.setSourceFilename(createKnowledgeDTO.getFile().getOriginalFilename());
            }
        }

        transactionTemplate.executeWithoutResult(status -> {
            if (agentKnowledgeMapper.insert(entity) <= 0) {
                throw new ReportBizException("error.knowledge.agentAddFailed");
            }
        });

        embedToVectorStore(entity);

        return agentKnowledgeConverter.toVo(entity);
    }

    /**
     * 读取文件内容
     *
     * @param file 上传的文件
     * @return 文件内容字符串
     */
    private String readFileContent(MultipartFile file) {
        return documentParserFactory.parse(file);
    }

    /**
     * 参数校验
     *
     * @param dto 创建智能体知识DTO
     */
    private void validateCreateKnowledgeDTO(CreateAgentKnowledgeDTO dto) {
        if (KnowledgeType.DOCUMENT.getValue().equals(dto.getType()) && dto.getFile() == null) {
            throw new ReportBizException("error.knowledge.agentFileRequired");
        }
        if (KnowledgeType.QA.getValue().equals(dto.getType()) || KnowledgeType.FAQ.getValue().equals(dto.getType())) {
            if (!StringUtils.hasText(dto.getQuestion())) {
                throw new ReportBizException("error.knowledge.agentQuestionRequired");
            }
            if (!StringUtils.hasText(dto.getContent())) {
                throw new ReportBizException("error.knowledge.agentContentRequired");
            }
        }
        validateEmbeddingModelConsistency(dto.getModelId());
    }

    /**
     * 校验嵌入模型与默认模型一致性
     */
    private void validateEmbeddingModelConsistency(String modelId) {
        if (!StringUtils.hasText(modelId)) {
            return;
        }
        String defaultModelId;
        try {
            defaultModelId = embeddingService.resolveDefaultEmbeddingModelId();
        } catch (Exception e) {
            log.warn("无法解析默认嵌入模型，跳过一致性校验: {}", e.getMessage());
            return;
        }
        if (Objects.equals(modelId, defaultModelId)) {
            return;
        }
        log.warn("智能体知识嵌入模型与默认不一致: specified={}, default={}", modelId, defaultModelId);
        if (retrievalProperties.isRejectMismatchedModel()) {
            throw new ReportBizException("error.knowledge.agentEmbeddingModelMismatch", modelId, defaultModelId);
        }
    }

    /**
     * 将知识向量化并存储到向量库
     *
     * @param knowledge 智能体知识实体
     */
    private void embedToVectorStore(AgentKnowledge knowledge) {
        try {
            List<VectorDocument> documents = convertToVectorDocuments(knowledge);
            reportAgentVectorStore.addDocuments(documents, knowledge.getModelId());
            knowledge.setEmbeddingStatus(EmbeddingStatus.COMPLETED);
            knowledge.setErrorMsg(null);
            agentKnowledgeMapper.update(knowledge);
            log.info("智能体知识向量化成功, id: {}, 分块数: {}", knowledge.getId(), documents.size());
        } catch (Exception e) {
            String errorMsg = truncateErrorMsg("向量化失败: " + e.getMessage());
            knowledge.setEmbeddingStatus(EmbeddingStatus.FAILED);
            knowledge.setErrorMsg(errorMsg);
            agentKnowledgeMapper.update(knowledge);
            log.error("智能体知识向量化失败, id: {}, error: {}", knowledge.getId(), errorMsg);
        }
    }

    /**
     * 将智能体知识转换为向量文档列表
     *
     * @param knowledge 智能体知识实体
     * @return 向量文档列表
     */
    private List<VectorDocument> convertToVectorDocuments(AgentKnowledge knowledge) {
        String content;
        if (knowledge.getType() == KnowledgeType.DOCUMENT) {
            content = KnowledgeContentCleaner.prepareDocumentText(
                    knowledge.getContent() != null ? knowledge.getContent() : "");
            if (!StringUtils.hasText(content)) {
                throw new ReportBizException("error.knowledge.agentDocContentEmpty");
            }
        } else {
            content = String.format("问题: %s, 答案: %s",
                    knowledge.getQuestion() != null ? knowledge.getQuestion() : "",
                    knowledge.getContent() != null ? knowledge.getContent() : "");
            if (!StringUtils.hasText(content)) {
                throw new ReportBizException("error.knowledge.agentQaContentEmpty");
            }
        }

        List<String> chunks;
        if (knowledge.getType() == KnowledgeType.DOCUMENT) {
            SplitterType resolved = SplitterTypeResolver.resolve(
                    knowledge.getSplitterType(), knowledge.getSourceFilename());
            TextSplitter splitter = textSplitterFactory.getSplitter(resolved.getValue(), knowledge.getModelId());
            chunks = splitter.split(content);
            List<String> normalized = new ArrayList<>();
            for (String chunk : chunks) {
                String t = KnowledgeSimpleText.simpleText(chunk);
                if (StringUtils.hasText(t)) {
                    normalized.add(t);
                }
            }
            chunks = normalized;
            log.info("文档分块, id: {}, splitterType: {}, resolved: {}, 分块数: {}",
                    knowledge.getId(), knowledge.getSplitterType(), resolved.getValue(), chunks.size());
        } else {
            chunks = Collections.singletonList(content);
        }

        List<VectorDocument> documents = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put(AgentKnowledgeMetadataConstant.VECTOR_TYPE, AgentKnowledgeMetadataConstant.AGENT_KNOWLEDGE);
            metadata.put(AgentKnowledgeMetadataConstant.DB_AGENT_KNOWLEDGE_ID, knowledge.getId());
            metadata.put("chunk_index", i);
            metadata.put("chunk_total", chunks.size());
            if (knowledge.getSplitterType() != null) {
                metadata.put("splitter_type", knowledge.getSplitterType());
            }
            String modelIdForMeta = knowledge.getModelId();
            if (!StringUtils.hasText(modelIdForMeta)) {
                try {
                    modelIdForMeta = embeddingService.resolveDefaultEmbeddingModelId();
                } catch (Exception ignored) {
                    modelIdForMeta = null;
                }
            }
            if (StringUtils.hasText(modelIdForMeta)) {
                metadata.put(AgentKnowledgeMetadataConstant.MODEL_ID, modelIdForMeta);
            }
            documents.add(new VectorDocument(chunks.get(i), metadata));
        }

        return documents;
    }

    /**
     * 更新智能体知识
     *
     * @param id 智能体知识ID
     * @param updateKnowledgeDTO 更新智能体知识DTO
     * @return 智能体知识VO
     */
    @Override
    public AgentKnowledgeVO updateKnowledge(String id, UpdateAgentKnowledgeDTO updateKnowledgeDTO) {
        AgentKnowledge knowledge = agentKnowledgeMapper.selectById(id);
        if (knowledge == null) {
            throw new ReportBizException("error.knowledge.agentNotFoundId", id);
        }

        agentKnowledgeConverter.applyUpdateToEntity(knowledge, updateKnowledgeDTO);
        knowledge.setUpdateBy(SecurityUtils.getCurrentUserId());
        knowledge.setEmbeddingStatus(EmbeddingStatus.PROCESSING);

        transactionTemplate.executeWithoutResult(status -> {
            if (agentKnowledgeMapper.update(knowledge) <= 0) {
                throw new ReportBizException("error.knowledge.agentUpdateFailed");
            }
        });

        try {
            syncToVectorStore(knowledge);
            knowledge.setEmbeddingStatus(EmbeddingStatus.COMPLETED);
            knowledge.setErrorMsg(null);
            agentKnowledgeMapper.update(knowledge);
        } catch (Exception e) {
            String errorMsg = truncateErrorMsg("更新向量存储失败: " + e.getMessage());
            knowledge.setEmbeddingStatus(EmbeddingStatus.FAILED);
            knowledge.setErrorMsg(errorMsg);
            agentKnowledgeMapper.update(knowledge);
            log.error("更新向量存储失败, id: {}, error: {}", id, errorMsg);
        }

        return agentKnowledgeConverter.toVo(knowledge);
    }

    /**
     * 同步知识到向量库（先删除旧向量，再添加新向量）
     *
     * @param knowledge 智能体知识实体
     */
    private void syncToVectorStore(AgentKnowledge knowledge) {
        deleteVectorByKnowledgeId(knowledge.getId());

        List<VectorDocument> documents = convertToVectorDocuments(knowledge);
        reportAgentVectorStore.addDocuments(documents, knowledge.getModelId());
        log.info("成功更新向量存储, id: {}, 分块数: {}", knowledge.getId(), documents.size());
    }

    /**
     * 删除智能体知识
     *
     * @param id 智能体知识ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteKnowledge(String id) {
        AgentKnowledge knowledge = agentKnowledgeMapper.selectById(id);
        if (knowledge == null) {
            log.warn("智能体知识不存在, id: {}", id);
            return true;
        }

        deleteVectorByKnowledgeId(id);

        transactionTemplate.executeWithoutResult(status -> {
            knowledge.setDelFlag(1);
            knowledge.setUpdateBy(SecurityUtils.getCurrentUserId());
            if (agentKnowledgeMapper.update(knowledge) <= 0) {
                throw new ReportBizException("error.knowledge.agentDeleteFailed");
            }
        });

        return true;
    }

    @Override
    public void deleteKnowledgeBatch(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (String id : ids) {
            if (StringUtils.hasText(id)) {
                deleteKnowledge(id);
            }
        }
    }

    /**
     * 根据知识ID删除向量数据
     *
     * @param knowledgeId 智能体知识ID
     */
    private void deleteVectorByKnowledgeId(String knowledgeId) {
        reportAgentVectorStore.deleteByMetadata(
            AgentKnowledgeMetadataConstant.AGENT_KNOWLEDGE,
            AgentKnowledgeMetadataConstant.DB_AGENT_KNOWLEDGE_ID,
            knowledgeId
        );
    }

    /**
     * 分页条件查询智能体知识
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @Override
    public PageResultVO<AgentKnowledgeVO> queryByPage(AgentKnowledgeQueryDTO queryDTO) {
        int offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();

        Long total = agentKnowledgeMapper.selectCount(queryDTO);

        List<AgentKnowledge> dataList = agentKnowledgeMapper.selectPage(queryDTO, offset, queryDTO.getPageSize());
        List<AgentKnowledgeVO> dataListVO = dataList.stream()
                .map(agentKnowledgeConverter::toVo)
                .collect(Collectors.toList());

        return PageResultVO.success(dataListVO, total, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    /**
     * 更新智能体知识是否生效
     *
     * @param id 智能体知识ID
     * @param enabled 是否生效
     * @return 智能体知识VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentKnowledgeVO updateEnabledStatus(String id, Boolean enabled) {
        AgentKnowledge knowledge = agentKnowledgeMapper.selectById(id);
        if (knowledge == null) {
            throw new ReportBizException("error.knowledge.agentNotFoundId", id);
        }

        knowledge.setEnabled(Boolean.TRUE.equals(enabled));
        agentKnowledgeMapper.update(knowledge);

        return agentKnowledgeConverter.toVo(knowledge);
    }

    /**
     * 重试向量化
     *
     * @param id 智能体知识ID
     */
    @Override
    public void retryEmbedding(String id) {
        AgentKnowledge knowledge = agentKnowledgeMapper.selectById(id);
        if (knowledge == null) {
            throw new ReportBizException("error.knowledge.agentNotFoundId", id);
        }

        if (knowledge.getEmbeddingStatus() == EmbeddingStatus.PROCESSING) {
            throw new ReportBizException("error.knowledge.agentProcessing");
        }

        if (!Boolean.TRUE.equals(knowledge.getEnabled())) {
            throw new ReportBizException("error.knowledge.agentNotEnabled");
        }

        knowledge.setEmbeddingStatus(EmbeddingStatus.PENDING);
        knowledge.setErrorMsg(null);
        agentKnowledgeMapper.update(knowledge);

        try {
            syncToVectorStore(knowledge);
            knowledge.setEmbeddingStatus(EmbeddingStatus.COMPLETED);
            knowledge.setErrorMsg(null);
            agentKnowledgeMapper.update(knowledge);
        } catch (Exception e) {
            String errorMsg = truncateErrorMsg(e.getMessage());
            knowledge.setEmbeddingStatus(EmbeddingStatus.FAILED);
            knowledge.setErrorMsg(errorMsg);
            agentKnowledgeMapper.update(knowledge);
            throw new ReportBizException("error.knowledge.agentRetryFailed", errorMsg);
        }
    }

    /**
     * 截断错误信息，确保不超过数据库字段长度
     *
     * @param errorMsg 原始错误信息
     * @return 截断后的错误信息，最大长度为 500 字符
     */
    private String truncateErrorMsg(String errorMsg) {
        if (errorMsg == null) {
            return null;
        }
        return errorMsg.length() > 490 ? errorMsg.substring(0, 490) : errorMsg;
    }

    /**
     * 根据ID列表批量查询智能体知识实体
     *
     * @param ids 智能体知识ID列表
     * @return 智能体知识实体列表
     */
    @Override
    public List<AgentKnowledge> listByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return agentKnowledgeMapper.selectByIds(ids);
    }

    /**
     * 查询所有已生效的智能体知识ID列表
     *
     * @return 已生效的智能体知识ID列表
     */
    @Override
    public List<String> listEnabledIds() {
        return agentKnowledgeMapper.selectEnabledKnowledgeIds();
    }

    /**
     * 回填智能体知识原文内容
     *
     * @param results 向量检索结果列表（会被原地替换为裁剪后的分条结果）
     */
    @Override
    public void fillAgentKnowledgeContent(List<VectorStoreSearchResult> results) {
        if (results == null || results.isEmpty()) {
            return;
        }

        int recallCount = results.size();

        List<String> ids = results.stream()
                .map(r -> r.getDocument().getMetadata())
                .filter(m -> m != null && m.containsKey(AgentKnowledgeMetadataConstant.DB_AGENT_KNOWLEDGE_ID))
                .map(m -> String.valueOf(m.get(AgentKnowledgeMetadataConstant.DB_AGENT_KNOWLEDGE_ID)))
                .filter(id -> id != null && !id.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        if (ids.isEmpty()) {
            log.warn("向量检索结果中没有有效的 agentKnowledgeId, recallCount={}", recallCount);
            return;
        }

        List<AgentKnowledge> knowledgeList = listByIds(ids);
        if (knowledgeList.isEmpty()) {
            log.warn("根据 agentKnowledgeId 未查询到任何智能体知识: ids={}", ids);
            return;
        }

        Map<String, AgentKnowledge> knowledgeMap = knowledgeList.stream()
                .collect(Collectors.toMap(AgentKnowledge::getId, k -> k, (a, b) -> a));

        for (VectorStoreSearchResult result : results) {
            Map<String, Object> metadata = result.getDocument().getMetadata();
            if (metadata == null || !metadata.containsKey(AgentKnowledgeMetadataConstant.DB_AGENT_KNOWLEDGE_ID)) {
                continue;
            }
            String knowledgeId = String.valueOf(metadata.get(AgentKnowledgeMetadataConstant.DB_AGENT_KNOWLEDGE_ID));
            AgentKnowledge knowledge = knowledgeMap.get(knowledgeId);
            if (knowledge == null) {
                continue;
            }

            String content = result.getDocument().getContent();
            if (content == null || content.isEmpty()) {
                if (knowledge.getType() == KnowledgeType.DOCUMENT) {
                    content = knowledge.getContent();
                } else {
                    content = String.format("问题: %s, 答案: %s",
                            knowledge.getQuestion() != null ? knowledge.getQuestion() : "",
                            knowledge.getContent() != null ? knowledge.getContent() : "");
                }
                content = truncateFallbackContent(content);
                result.getDocument().setContent(content);
            }
            metadata.put("title", knowledge.getTitle());
        }

        List<VectorStoreSearchResult> selected = AgentKnowledgeChunkMerger.selectTopChunksByKnowledge(
                results,
                retrievalProperties.getMergeTopN(),
                retrievalProperties.getFinalTopK());

        results.clear();
        results.addAll(selected);

        log.info("回填智能体知识完成: recallCount={}, afterSelectCount={}, mergeTopN={}, finalTopK={}",
                recallCount, results.size(),
                retrievalProperties.getMergeTopN(), retrievalProperties.getFinalTopK());
    }

    private String truncateFallbackContent(String content) {
        if (content == null) {
            return "";
        }
        int max = retrievalProperties.getFallbackContentMaxChars();
        if (content.length() <= max) {
            return content;
        }
        return content.substring(0, max);
    }

    @Override
    public List<KnowledgeChunkVO> listChunks(String knowledgeId) {
        if (getKnowledgeById(knowledgeId) == null) {
            throw new ReportBizException("error.knowledge.agentNotFoundId", knowledgeId);
        }
        return knowledgeChunkService.listChunks(
                AgentKnowledgeMetadataConstant.AGENT_KNOWLEDGE,
                AgentKnowledgeMetadataConstant.DB_AGENT_KNOWLEDGE_ID,
                knowledgeId);
    }

    @Override
    public KnowledgeChunkVO updateChunk(String knowledgeId, String vectorId, UpdateKnowledgeChunkDTO dto) {
        AgentKnowledgeVO knowledge = getKnowledgeById(knowledgeId);
        if (knowledge == null) {
            throw new ReportBizException("error.knowledge.agentNotFoundId", knowledgeId);
        }
        String modelId = knowledge.getModelId();
        if (!StringUtils.hasText(modelId)) {
            modelId = embeddingService.resolveDefaultEmbeddingModelId();
        }
        return knowledgeChunkService.updateChunk(
                AgentKnowledgeMetadataConstant.AGENT_KNOWLEDGE,
                AgentKnowledgeMetadataConstant.DB_AGENT_KNOWLEDGE_ID,
                knowledgeId, vectorId, dto.getContent(), modelId);
    }

    @Override
    public void deleteChunk(String knowledgeId, String vectorId) {
        if (getKnowledgeById(knowledgeId) == null) {
            throw new ReportBizException("error.knowledge.agentNotFoundId", knowledgeId);
        }
        knowledgeChunkService.deleteChunk(
                AgentKnowledgeMetadataConstant.AGENT_KNOWLEDGE,
                AgentKnowledgeMetadataConstant.DB_AGENT_KNOWLEDGE_ID,
                knowledgeId, vectorId);
    }
}
