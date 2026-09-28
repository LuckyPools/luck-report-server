package com.luck.report.web.modules.knowledge.handler.splitter;

import com.luck.report.core.exception.ReportBizException;
import com.luck.report.web.modules.chat.service.impl.EmbeddingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 语义分块器
 * 移植自 data-agent 的 SemanticTextSplitter，适配为使用本项目的 EmbeddingService
 * 滑动窗口 Embedding + 语义相似度切分 + 最大长度强制切分
 * 尽可能把句子往一个块里塞，直到超长或语义突变才切分
 *
 * @author luck
 */
public class SemanticTextSplitter implements TextSplitter {

    private static final Logger log = LoggerFactory.getLogger(SemanticTextSplitter.class);

    private static final Pattern SENTENCE_PATTERN = Pattern.compile("([^。！？；.!?;\\n]+[。！？；.!?;]?|[^。！？；.!?;\\n]*\\n)");

    private static final int DEFAULT_MIN_CHUNK_SIZE = 200;
    private static final int DEFAULT_MAX_CHUNK_SIZE = 1000;
    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.65;
    private static final int DEFAULT_EMBEDDING_BATCH_SIZE = 10;

    private final EmbeddingService embeddingService;
    private final String modelId;
    private final int minChunkSize;
    private final int maxChunkSize;
    private final double similarityThreshold;
    private final int embeddingBatchSize;

    public SemanticTextSplitter(EmbeddingService embeddingService, String modelId,
                                int minChunkSize, int maxChunkSize, double similarityThreshold) {
        this.embeddingService = embeddingService;
        this.modelId = modelId;
        this.minChunkSize = minChunkSize > 0 ? minChunkSize : DEFAULT_MIN_CHUNK_SIZE;
        this.maxChunkSize = maxChunkSize > 0 ? maxChunkSize : DEFAULT_MAX_CHUNK_SIZE;
        this.similarityThreshold = similarityThreshold > 0 ? similarityThreshold : DEFAULT_SIMILARITY_THRESHOLD;
        this.embeddingBatchSize = DEFAULT_EMBEDDING_BATCH_SIZE;
    }

    public SemanticTextSplitter(EmbeddingService embeddingService, String modelId) {
        this(embeddingService, modelId, DEFAULT_MIN_CHUNK_SIZE, DEFAULT_MAX_CHUNK_SIZE, DEFAULT_SIMILARITY_THRESHOLD);
    }

    public SemanticTextSplitter(EmbeddingService embeddingService, String modelId, int maxChunkSize) {
        this(embeddingService, modelId, DEFAULT_MIN_CHUNK_SIZE, maxChunkSize, DEFAULT_SIMILARITY_THRESHOLD);
    }

    @Override
    public List<String> split(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 提取句子
        List<String> sentences = extractSentences(text);
        if (sentences.isEmpty()) {
            List<String> result = new ArrayList<>();
            result.add(text);
            return result;
        }

        // 2. 只有一句，直接返回
        if (sentences.size() == 1) {
            return splitLargeChunk(sentences.get(0));
        }

        // 3. 构建滑动窗口上下文
        List<String> contextSentences = buildContextSentences(sentences);

        // 4. 计算 Embeddings
        List<float[]> embeddings = batchEmbed(contextSentences);

        // 5. 基于语义+长度双重约束合并
        return combineSentences(sentences, embeddings);
    }

    private List<String> combineSentences(List<String> sentences, List<float[]> embeddings) {
        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();

        for (int i = 0; i < sentences.size(); i++) {
            String sentence = sentences.get(i);

            // 单句超出最大长度
            if (sentence.length() > maxChunkSize) {
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk.setLength(0);
                }
                chunks.addAll(splitLargeChunk(sentence));
                continue;
            }

            boolean shouldSplit = false;

            if (currentChunk.length() > 0) {
                // 长度检查
                if (currentChunk.length() + sentence.length() > maxChunkSize) {
                    shouldSplit = true;
                }
                // 语义检查
                else if (i < embeddings.size() && i > 0 && i - 1 < embeddings.size()) {
                    double similarity = cosineSimilarity(embeddings.get(i - 1), embeddings.get(i));
                    if (similarity < similarityThreshold && currentChunk.length() >= minChunkSize) {
                        log.debug("语义切分 at index {}, similarity={}", i, similarity);
                        shouldSplit = true;
                    }
                }
            }

            if (shouldSplit) {
                chunks.add(currentChunk.toString().trim());
                currentChunk.setLength(0);
            }

            if (currentChunk.length() > 0 && !isChinese(sentence)) {
                currentChunk.append(" ");
            }
            currentChunk.append(sentence);
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    private boolean isChinese(String str) {
        return str.codePoints()
                .anyMatch(codepoint -> Character.UnicodeScript.of(codepoint) == Character.UnicodeScript.HAN);
    }

    private List<String> extractSentences(String text) {
        List<String> sentences = new ArrayList<>();
        Matcher matcher = SENTENCE_PATTERN.matcher(text);
        int lastEnd = 0;

        while (matcher.find()) {
            String s = matcher.group().trim();
            if (!s.isEmpty()) {
                sentences.add(s);
            }
            lastEnd = matcher.end();
        }

        if (lastEnd < text.length()) {
            String tail = text.substring(lastEnd).trim();
            if (!tail.isEmpty()) {
                sentences.add(tail);
            }
        }

        return sentences;
    }

    private List<String> buildContextSentences(List<String> sentences) {
        List<String> contextSentences = new ArrayList<>();
        for (int i = 0; i < sentences.size(); i++) {
            StringBuilder context = new StringBuilder();
            if (i > 0) context.append(sentences.get(i - 1)).append(" ");
            context.append(sentences.get(i));
            if (i < sentences.size() - 1) context.append(" ").append(sentences.get(i + 1));
            contextSentences.add(context.toString());
        }
        return contextSentences;
    }

    private List<float[]> batchEmbed(List<String> texts) {
        List<float[]> allEmbeddings = new ArrayList<>();

        for (int i = 0; i < texts.size(); i += embeddingBatchSize) {
            int endIdx = Math.min(i + embeddingBatchSize, texts.size());
            List<String> batch = texts.subList(i, endIdx);
            try {
                List<float[]> batchResult = embeddingService.embedBatch(batch, modelId);
                allEmbeddings.addAll(batchResult);
            } catch (ReportBizException e) {
                log.error("语义分块 Embedding 失败, batch {}-{}", i, endIdx, e);
                throw e;
            } catch (Exception e) {
                log.error("语义分块 Embedding 失败, batch {}-{}", i, endIdx, e);
                throw new ReportBizException("error.embedding.apiRequestError",
                        "semantic split batch " + i + "-" + endIdx + ": " + e.getMessage());
            }
        }
        return allEmbeddings;
    }

    private double cosineSimilarity(float[] vec1, float[] vec2) {
        if (vec1 == null || vec2 == null || vec1.length == 0 || vec2.length == 0 || vec1.length != vec2.length) {
            return 0.0;
        }
        double dot = 0.0, norm1 = 0.0, norm2 = 0.0;
        for (int i = 0; i < vec1.length; i++) {
            dot += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }
        if (norm1 == 0 || norm2 == 0) return 0.0;
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private List<String> splitLargeChunk(String text) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < text.length(); i += maxChunkSize) {
            result.add(text.substring(i, Math.min(i + maxChunkSize, text.length())));
        }
        return result;
    }
}
