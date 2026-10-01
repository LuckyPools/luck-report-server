package com.luck.report.web.modules.knowledge.service.impl;

import com.luck.report.core.exception.ReportBizException;
import com.luck.report.web.modules.knowledge.domain.enums.EmbeddingStatus;
import com.luck.report.web.modules.vector.service.impl.AgentVectorStore;
import com.luck.report.web.utils.SnowflakeIdGenerator;
import com.luck.report.web.modules.knowledge.constant.BusinessKnowledgeMetadataConstant;
import com.luck.report.web.modules.knowledge.converter.BusinessKnowledgeConverter;
import com.luck.report.web.modules.knowledge.domain.dto.BusinessKnowledgeQueryDTO;
import com.luck.report.web.modules.knowledge.domain.dto.CreateBusinessKnowledgeDTO;
import com.luck.report.web.modules.knowledge.domain.dto.UpdateBusinessKnowledgeDTO;
import com.luck.report.web.modules.knowledge.domain.dto.UpdateKnowledgeChunkDTO;
import com.luck.report.web.modules.knowledge.domain.entity.BusinessKnowledge;
import com.luck.report.web.modules.knowledge.domain.enums.KnowledgeType;
import com.luck.report.web.modules.knowledge.domain.vo.BusinessKnowledgeVO;
import com.luck.report.web.modules.knowledge.domain.vo.KnowledgeChunkVO;
import com.luck.report.web.modules.knowledge.handler.splitter.TextSplitter;
import com.luck.report.web.modules.knowledge.handler.splitter.TextSplitterFactory;
import com.luck.report.web.common.domain.vo.PageResultVO;
import com.luck.report.web.modules.knowledge.mapper.BusinessKnowledgeMapper;
import com.luck.report.web.modules.knowledge.handler.parser.DocumentParserFactory;
import com.luck.report.web.modules.knowledge.service.BusinessKnowledgeService;
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
 * 业务知识服务实现（对齐 Agent 知识库：解析/分块/嵌入/回填）
 *
 * @author luck
 */
@Slf4j
@Service("bean.businessKnowledgeService")
@AllArgsConstructor
public class BusinessKnowledgeServiceImpl implements BusinessKnowledgeService {

    @Qualifier("bean.businessKnowledgeMapper")
    private final BusinessKnowledgeMapper businessKnowledgeMapper;
    @Qualifier("bean.agentVectorStore")
    private final AgentVectorStore reportAgentVectorStore;
    @Qualifier("bean.businessKnowledgeConverter")
    private final BusinessKnowledgeConverter businessKnowledgeConverter;
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

    @Override
    public BusinessKnowledgeVO getKnowledgeById(String id) {
        BusinessKnowledge knowledge = businessKnowledgeMapper.selectById(id);
        return knowledge == null ? null : businessKnowledgeConverter.toVo(knowledge);
    }

    /** 事务内写库，提交后再向量化，避免半截数据 */
    @Override
    public BusinessKnowledgeVO createKnowledge(CreateBusinessKnowledgeDTO createKnowledgeDTO) {
        validateCreateKnowledgeDTO(createKnowledgeDTO);

        BusinessKnowledge entity = businessKnowledgeConverter.toEntityForCreate(createKnowledgeDTO);
        entity.setId(SnowflakeIdGenerator.generateId());
        entity.setCreateBy(SecurityUtils.getCurrentUserId());
        entity.setUpdateBy(SecurityUtils.getCurrentUserId());

        if (KnowledgeType.DOCUMENT.getValue().equals(createKnowledgeDTO.getType())
                && createKnowledgeDTO.getFile() != null) {
            entity.setContent(KnowledgeContentCleaner.prepareDocumentText(readFileContent(createKnowledgeDTO.getFile())));
            if (!StringUtils.hasText(entity.getSourceFilename())
                    && createKnowledgeDTO.getFile().getOriginalFilename() != null) {
                entity.setSourceFilename(createKnowledgeDTO.getFile().getOriginalFilename());
            }
        }

        transactionTemplate.executeWithoutResult(status -> {
            if (businessKnowledgeMapper.insert(entity) <= 0) {
                throw new ReportBizException("error.knowledge.bizAddFailed");
            }
        });

        embedToVectorStore(entity);
        return businessKnowledgeConverter.toVo(entity);
    }

    private String readFileContent(MultipartFile file) {
        return documentParserFactory.parse(file);
    }

    private void validateCreateKnowledgeDTO(CreateBusinessKnowledgeDTO dto) {
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
        log.warn("业务知识嵌入模型与默认不一致: specified={}, default={}", modelId, defaultModelId);
        if (retrievalProperties.isRejectMismatchedModel()) {
            throw new ReportBizException("error.knowledge.agentEmbeddingModelMismatch", modelId, defaultModelId);
        }
    }

    private void embedToVectorStore(BusinessKnowledge knowledge) {
        try {
            List<VectorDocument> documents = convertToVectorDocuments(knowledge);
            reportAgentVectorStore.addDocuments(documents, knowledge.getModelId());
            knowledge.setEmbeddingStatus(EmbeddingStatus.COMPLETED);
            knowledge.setErrorMsg(null);
            businessKnowledgeMapper.update(knowledge);
            log.info("业务知识向量化成功, id: {}, 分块数: {}", knowledge.getId(), documents.size());
        } catch (Exception e) {
            String errorMsg = truncateErrorMsg("向量化失败: " + e.getMessage());
            knowledge.setEmbeddingStatus(EmbeddingStatus.FAILED);
            knowledge.setErrorMsg(errorMsg);
            businessKnowledgeMapper.update(knowledge);
            log.error("业务知识向量化失败, id: {}, error: {}", knowledge.getId(), errorMsg);
        }
    }

    private List<VectorDocument> convertToVectorDocuments(BusinessKnowledge knowledge) {
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
            metadata.put(BusinessKnowledgeMetadataConstant.VECTOR_TYPE, BusinessKnowledgeMetadataConstant.BUSINESS_KNOWLEDGE);
            metadata.put(BusinessKnowledgeMetadataConstant.DB_BUSINESS_KNOWLEDGE_ID, knowledge.getId());
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
                metadata.put(BusinessKnowledgeMetadataConstant.MODEL_ID, modelIdForMeta);
            }
            documents.add(new VectorDocument(chunks.get(i), metadata));
        }
        return documents;
    }

    @Override
    public BusinessKnowledgeVO updateKnowledge(String id, UpdateBusinessKnowledgeDTO updateKnowledgeDTO) {
        BusinessKnowledge knowledge = businessKnowledgeMapper.selectById(id);
        if (knowledge == null) {
            throw new ReportBizException("error.knowledge.bizNotFoundId", id);
        }

        businessKnowledgeConverter.applyUpdateToEntity(knowledge, updateKnowledgeDTO);
        knowledge.setUpdateBy(SecurityUtils.getCurrentUserId());
        knowledge.setEmbeddingStatus(EmbeddingStatus.PROCESSING);

        transactionTemplate.executeWithoutResult(status -> {
            if (businessKnowledgeMapper.update(knowledge) <= 0) {
                throw new ReportBizException("error.knowledge.bizUpdateFailed");
            }
        });

        try {
            syncToVectorStore(knowledge);
            knowledge.setEmbeddingStatus(EmbeddingStatus.COMPLETED);
            knowledge.setErrorMsg(null);
            businessKnowledgeMapper.update(knowledge);
        } catch (Exception e) {
            String errorMsg = truncateErrorMsg("更新向量存储失败: " + e.getMessage());
            knowledge.setEmbeddingStatus(EmbeddingStatus.FAILED);
            knowledge.setErrorMsg(errorMsg);
            businessKnowledgeMapper.update(knowledge);
            log.error("更新向量存储失败, id: {}, error: {}", id, errorMsg);
        }

        return businessKnowledgeConverter.toVo(knowledge);
    }

    private void syncToVectorStore(BusinessKnowledge knowledge) {
        deleteVectorByKnowledgeId(knowledge.getId());
        List<VectorDocument> documents = convertToVectorDocuments(knowledge);
        reportAgentVectorStore.addDocuments(documents, knowledge.getModelId());
        log.info("成功更新向量存储, id: {}, 分块数: {}", knowledge.getId(), documents.size());
    }

    @Override
    public boolean deleteKnowledge(String id) {
        BusinessKnowledge knowledge = businessKnowledgeMapper.selectById(id);
        if (knowledge == null) {
            log.warn("业务知识不存在, id: {}", id);
            return true;
        }

        deleteVectorByKnowledgeId(id);

        transactionTemplate.executeWithoutResult(status -> {
            knowledge.setDelFlag(1);
            knowledge.setUpdateBy(SecurityUtils.getCurrentUserId());
            if (businessKnowledgeMapper.update(knowledge) <= 0) {
                throw new ReportBizException("error.knowledge.bizDeleteFailed");
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

    private void deleteVectorByKnowledgeId(String knowledgeId) {
        reportAgentVectorStore.deleteByMetadata(
                BusinessKnowledgeMetadataConstant.BUSINESS_KNOWLEDGE,
                BusinessKnowledgeMetadataConstant.DB_BUSINESS_KNOWLEDGE_ID,
                knowledgeId
        );
    }

    @Override
    public PageResultVO<BusinessKnowledgeVO> queryByPage(BusinessKnowledgeQueryDTO queryDTO) {
        int offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();
        Long total = businessKnowledgeMapper.countByConditions(queryDTO);
        List<BusinessKnowledge> dataList = businessKnowledgeMapper.selectByConditionsWithPage(
                queryDTO, offset, queryDTO.getPageSize());
        List<BusinessKnowledgeVO> dataListVO = dataList.stream()
                .map(businessKnowledgeConverter::toVo)
                .collect(Collectors.toList());
        return PageResultVO.success(dataListVO, total, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BusinessKnowledgeVO updateEnabledStatus(String id, Boolean enabled) {
        BusinessKnowledge knowledge = businessKnowledgeMapper.selectById(id);
        if (knowledge == null) {
            throw new ReportBizException("error.knowledge.bizNotFoundId", id);
        }
        knowledge.setEnabled(Boolean.TRUE.equals(enabled));
        businessKnowledgeMapper.update(knowledge);
        return businessKnowledgeConverter.toVo(knowledge);
    }

    @Override
    public void retryEmbedding(String id) {
        BusinessKnowledge knowledge = businessKnowledgeMapper.selectById(id);
        if (knowledge == null) {
            throw new ReportBizException("error.knowledge.bizNotFoundId", id);
        }
        if (knowledge.getEmbeddingStatus() == EmbeddingStatus.PROCESSING) {
            throw new ReportBizException("error.knowledge.bizProcessing");
        }
        if (!Boolean.TRUE.equals(knowledge.getEnabled())) {
            throw new ReportBizException("error.knowledge.bizNotEnabled");
        }

        knowledge.setEmbeddingStatus(EmbeddingStatus.PENDING);
        knowledge.setErrorMsg(null);
        businessKnowledgeMapper.update(knowledge);

        try {
            syncToVectorStore(knowledge);
            knowledge.setEmbeddingStatus(EmbeddingStatus.COMPLETED);
            knowledge.setErrorMsg(null);
            businessKnowledgeMapper.update(knowledge);
        } catch (Exception e) {
            String errorMsg = truncateErrorMsg(e.getMessage());
            knowledge.setEmbeddingStatus(EmbeddingStatus.FAILED);
            knowledge.setErrorMsg(errorMsg);
            businessKnowledgeMapper.update(knowledge);
            throw new ReportBizException("error.knowledge.bizRetryFailed", errorMsg);
        }
    }

    private String truncateErrorMsg(String errorMsg) {
        if (errorMsg == null) {
            return null;
        }
        return errorMsg.length() > 490 ? errorMsg.substring(0, 490) : errorMsg;
    }

    @Override
    public List<BusinessKnowledge> selectByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return businessKnowledgeMapper.selectByIds(ids);
    }

    @Override
    public List<String> selectEnabledKnowledgeIds() {
        return businessKnowledgeMapper.selectEnabledKnowledgeIds();
    }

    @Override
    public void fillBusinessKnowledgeContent(List<VectorStoreSearchResult> results) {
        if (results == null || results.isEmpty()) {
            return;
        }

        int recallCount = results.size();

        List<String> ids = results.stream()
                .map(r -> r.getDocument().getMetadata())
                .filter(m -> m != null && m.containsKey(BusinessKnowledgeMetadataConstant.DB_BUSINESS_KNOWLEDGE_ID))
                .map(m -> String.valueOf(m.get(BusinessKnowledgeMetadataConstant.DB_BUSINESS_KNOWLEDGE_ID)))
                .filter(id -> id != null && !id.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        if (ids.isEmpty()) {
            log.warn("向量检索结果中没有有效的 businessKnowledgeId, recallCount={}", recallCount);
            return;
        }

        List<BusinessKnowledge> knowledgeList = selectByIds(ids);
        if (knowledgeList.isEmpty()) {
            log.warn("根据 businessKnowledgeId 未查询到任何业务知识: ids={}", ids);
            return;
        }

        Map<String, BusinessKnowledge> knowledgeMap = knowledgeList.stream()
                .collect(Collectors.toMap(BusinessKnowledge::getId, k -> k, (a, b) -> a));

        for (VectorStoreSearchResult result : results) {
            Map<String, Object> metadata = result.getDocument().getMetadata();
            if (metadata == null || !metadata.containsKey(BusinessKnowledgeMetadataConstant.DB_BUSINESS_KNOWLEDGE_ID)) {
                continue;
            }
            String knowledgeId = String.valueOf(metadata.get(BusinessKnowledgeMetadataConstant.DB_BUSINESS_KNOWLEDGE_ID));
            BusinessKnowledge knowledge = knowledgeMap.get(knowledgeId);
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

        log.info("回填业务知识完成: recallCount={}, afterSelectCount={}, mergeTopN={}, finalTopK={}",
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
            throw new ReportBizException("error.knowledge.bizNotFoundId", knowledgeId);
        }
        return knowledgeChunkService.listChunks(
                BusinessKnowledgeMetadataConstant.BUSINESS_KNOWLEDGE,
                BusinessKnowledgeMetadataConstant.DB_BUSINESS_KNOWLEDGE_ID,
                knowledgeId);
    }

    @Override
    public KnowledgeChunkVO updateChunk(String knowledgeId, String vectorId, UpdateKnowledgeChunkDTO dto) {
        BusinessKnowledgeVO knowledge = getKnowledgeById(knowledgeId);
        if (knowledge == null) {
            throw new ReportBizException("error.knowledge.bizNotFoundId", knowledgeId);
        }
        String modelId = knowledge.getModelId();
        if (!StringUtils.hasText(modelId)) {
            modelId = embeddingService.resolveDefaultEmbeddingModelId();
        }
        return knowledgeChunkService.updateChunk(
                BusinessKnowledgeMetadataConstant.BUSINESS_KNOWLEDGE,
                BusinessKnowledgeMetadataConstant.DB_BUSINESS_KNOWLEDGE_ID,
                knowledgeId, vectorId, dto.getContent(), modelId);
    }

    @Override
    public void deleteChunk(String knowledgeId, String vectorId) {
        if (getKnowledgeById(knowledgeId) == null) {
            throw new ReportBizException("error.knowledge.bizNotFoundId", knowledgeId);
        }
        knowledgeChunkService.deleteChunk(
                BusinessKnowledgeMetadataConstant.BUSINESS_KNOWLEDGE,
                BusinessKnowledgeMetadataConstant.DB_BUSINESS_KNOWLEDGE_ID,
                knowledgeId, vectorId);
    }
}
