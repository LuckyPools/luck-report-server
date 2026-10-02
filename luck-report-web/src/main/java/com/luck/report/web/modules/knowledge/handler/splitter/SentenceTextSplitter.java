package com.luck.report.web.modules.knowledge.handler.splitter;

import com.luck.report.web.modules.knowledge.handler.splitter.TextSplitter;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 句子分块器
 *
 * @author luck
 */
public class SentenceTextSplitter implements TextSplitter {

    /**
     * 句子切分正则
     */
    private static final Pattern SENTENCE_PATTERN = Pattern
            .compile("([^。！？；.!?;\\n]+(?:[。！？；.!?;]|\\n+)[\"'）\\)\\]]*\\s*)");

    private static final int DEFAULT_CHUNK_SIZE = 1000;
    private static final int DEFAULT_SENTENCE_OVERLAP = 1;

    private final int chunkSize;
    private final int sentenceOverlap;

    public SentenceTextSplitter(int chunkSize) {
        this(chunkSize, DEFAULT_SENTENCE_OVERLAP);
    }

    public SentenceTextSplitter(int chunkSize, int sentenceOverlap) {
        this.chunkSize = chunkSize > 0 ? chunkSize : DEFAULT_CHUNK_SIZE;
        this.sentenceOverlap = sentenceOverlap >= 0 ? sentenceOverlap : DEFAULT_SENTENCE_OVERLAP;
    }

    @Override
    public List<String> split(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<String> sentences = extractSentences(text);
        if (sentences.isEmpty()) {
            List<String> result = new ArrayList<>();
            result.add(text);
            return result;
        }

        List<String> result = new ArrayList<>();
        List<String> currentChunk = new ArrayList<>();
        int currentSize = 0;

        for (String sentence : sentences) {
            int sentenceLen = sentence.length();

            if (sentenceLen > this.chunkSize) {
                if (!currentChunk.isEmpty()) {
                    result.add(joinSentences(currentChunk));
                    currentChunk.clear();
                    currentSize = 0;
                }

                List<String> parts = splitLongSentence(sentence);
                for (String part : parts) {
                    if (currentSize + part.length() > this.chunkSize && !currentChunk.isEmpty()) {
                        result.add(joinSentences(currentChunk));
                        handleOverlap(currentChunk);
                        currentSize = calculateSize(currentChunk);
                    }
                    currentChunk.add(part);
                    currentSize += part.length();
                }
                continue;
            }

            if (currentSize + sentenceLen > this.chunkSize && !currentChunk.isEmpty()) {
                result.add(joinSentences(currentChunk));
                handleOverlap(currentChunk);
                currentSize = calculateSize(currentChunk);
            }

            currentChunk.add(sentence);
            currentSize += sentenceLen;
        }

        if (!currentChunk.isEmpty()) {
            result.add(joinSentences(currentChunk));
        }

        return result;
    }

    private String joinSentences(List<String> chunkSentences) {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < chunkSentences.size(); i++) {
            String s = chunkSentences.get(i);
            if (i > 0 && !isChinese(s)) {
                content.append(" ");
            }
            content.append(s);
        }
        return content.toString();
    }

    private void handleOverlap(List<String> currentChunk) {
        if (this.sentenceOverlap > 0 && currentChunk.size() > this.sentenceOverlap) {
            List<String> overlap = new ArrayList<>(
                    currentChunk.subList(currentChunk.size() - this.sentenceOverlap, currentChunk.size()));
            currentChunk.clear();
            currentChunk.addAll(overlap);
        } else {
            currentChunk.clear();
        }
    }

    private int calculateSize(List<String> chunk) {
        return chunk.stream().mapToInt(String::length).sum();
    }

    private List<String> extractSentences(String text) {
        List<String> sentences = new ArrayList<>();
        Matcher matcher = SENTENCE_PATTERN.matcher(text);
        int lastEnd = 0;

        while (matcher.find()) {
            String sentence = matcher.group(1).trim();
            if (StringUtils.hasText(sentence)) {
                sentences.add(sentence);
            }
            lastEnd = matcher.end();
        }

        if (lastEnd < text.length()) {
            String remaining = text.substring(lastEnd).trim();
            if (StringUtils.hasText(remaining)) {
                if (remaining.length() > this.chunkSize) {
                    sentences.addAll(splitLongSentence(remaining));
                } else {
                    sentences.add(remaining);
                }
            }
        }
        return sentences;
    }

    private List<String> splitLongSentence(String sentence) {
        List<String> result = new ArrayList<>();
        int i = 0;
        int len = sentence.length();

        while (i < len) {
            int end = Math.min(i + this.chunkSize, len);

            if (end < len && isAsciiLetter(sentence.charAt(end))) {
                int adjustedEnd = end;
                int minEnd = Math.max(i, end - 50);

                while (adjustedEnd > minEnd && isAsciiLetter(sentence.charAt(adjustedEnd))) {
                    adjustedEnd--;
                }

                if (adjustedEnd > minEnd && adjustedEnd < end) {
                    end = adjustedEnd;
                }
            }

            result.add(sentence.substring(i, end));
            i = end;
        }
        return result;
    }

    private boolean isAsciiLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
    }

    private boolean isChinese(String str) {
        if (str == null || str.isEmpty()) return false;
        int codePoint = str.codePointAt(0);
        return Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.HAN;
    }
}
