package com.luck.report.web.modules.knowledge.utils;

import com.luck.report.infra.modules.vector.domain.dto.VectorStoreSearchResult;
import com.luck.report.web.modules.knowledge.constant.AgentKnowledgeMetadataConstant;
import com.luck.report.web.modules.knowledge.constant.BusinessKnowledgeMetadataConstant;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 智能体知识检索结果裁剪：同知识保留得分最高的 Top-N 个 chunk，
 * 各 chunk <strong>分条返回</strong>（各自保留 score），不再拼接正文。
 * <p>
 * 跨知识：按该知识最高分排序，最多保留 {@code finalTopK} 个知识，再展开其选中 chunk。
 *
 * @author luck
 */
public final class AgentKnowledgeChunkMerger {

    private AgentKnowledgeChunkMerger() {
    }

    /**
     * 按知识裁剪并展开为多条 chunk 结果。
     *
     * @param results   向量召回结果（可含同一知识多块）
     * @param mergeTopN 同知识最多保留的高分 chunk 数
     * @param finalTopK 最多保留的知识条数（按知识最高分排序）
     * @return 分条 chunk 列表（按 score 降序）
     */
    public static List<VectorStoreSearchResult> selectTopChunksByKnowledge(
            List<VectorStoreSearchResult> results,
            int mergeTopN,
            int finalTopK) {
        if (results == null || results.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, List<VectorStoreSearchResult>> byKnowledgeId = new LinkedHashMap<>();
        for (VectorStoreSearchResult result : results) {
            String knowledgeId = extractKnowledgeId(result);
            if (knowledgeId == null) {
                continue;
            }
            byKnowledgeId.computeIfAbsent(knowledgeId, k -> new ArrayList<>()).add(result);
        }

        int perKnowledgeLimit = Math.max(1, mergeTopN);
        int knowledgeLimit = Math.max(1, finalTopK);

        // 每个知识：取 Top-N chunk（按 score）
        List<KnowledgeChunkGroup> groups = new ArrayList<>();
        for (Map.Entry<String, List<VectorStoreSearchResult>> entry : byKnowledgeId.entrySet()) {
            List<VectorStoreSearchResult> selected = entry.getValue().stream()
                    .sorted(Comparator.comparingDouble(VectorStoreSearchResult::getScore).reversed())
                    .limit(perKnowledgeLimit)
                    .collect(Collectors.toList());
            double bestScore = selected.stream()
                    .mapToDouble(VectorStoreSearchResult::getScore)
                    .max()
                    .orElse(0.0);
            groups.add(new KnowledgeChunkGroup(bestScore, selected));
        }

        // 跨知识：保留最高分的 finalTopK 个知识，展开为分条结果
        List<VectorStoreSearchResult> selectedChunks = groups.stream()
                .sorted(Comparator.comparingDouble(KnowledgeChunkGroup::getBestScore).reversed())
                .limit(knowledgeLimit)
                .flatMap(g -> g.getChunks().stream())
                .sorted(Comparator.comparingDouble(VectorStoreSearchResult::getScore).reversed())
                .collect(Collectors.toList());

        return selectedChunks;
    }

    /**
     * @deprecated 使用 {@link #selectTopChunksByKnowledge(List, int, int)}；保留旧签名避免外部误用编译失败
     */
    @Deprecated
    public static List<VectorStoreSearchResult> mergeByKnowledgeId(
            List<VectorStoreSearchResult> results,
            int mergeTopN,
            int finalTopK,
            int mergedContentMaxChars) {
        return selectTopChunksByKnowledge(results, mergeTopN, finalTopK);
    }

    private static String extractKnowledgeId(VectorStoreSearchResult result) {
        if (result == null || result.getDocument() == null || result.getDocument().getMetadata() == null) {
            return null;
        }
        Map<String, Object> metadata = result.getDocument().getMetadata();
        Object id = metadata.get(AgentKnowledgeMetadataConstant.DB_AGENT_KNOWLEDGE_ID);
        if (id == null) {
            id = metadata.get(BusinessKnowledgeMetadataConstant.DB_BUSINESS_KNOWLEDGE_ID);
        }
        if (id == null) {
            return null;
        }
        String knowledgeId = String.valueOf(id);
        return knowledgeId.isEmpty() ? null : knowledgeId;
    }
}
