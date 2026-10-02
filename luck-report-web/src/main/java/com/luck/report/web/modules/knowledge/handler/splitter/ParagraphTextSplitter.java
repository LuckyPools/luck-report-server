package com.luck.report.web.modules.knowledge.handler.splitter;

import com.luck.report.web.modules.knowledge.handler.splitter.TextSplitter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 段落分块器
 *
 * @author luck
 */
public class ParagraphTextSplitter implements TextSplitter {

    private static final int DEFAULT_CHUNK_SIZE = 1000;
    private static final int DEFAULT_OVERLAP = 100;

    private static final Pattern PARAGRAPH_PATTERN = Pattern.compile("\\n\\s*\\n+");
    private static final Pattern SENTENCE_PATTERN = Pattern.compile("[^。！？.!?\\n]+[。！？.!?\\n]*");

    private final int chunkSize;
    private final int paragraphOverlapChars;

    public ParagraphTextSplitter(int chunkSize) {
        this(chunkSize, DEFAULT_OVERLAP);
    }

    public ParagraphTextSplitter(int chunkSize, int paragraphOverlapChars) {
        this.chunkSize = chunkSize > 0 ? chunkSize : DEFAULT_CHUNK_SIZE;
        this.paragraphOverlapChars = paragraphOverlapChars >= 0 ? paragraphOverlapChars : DEFAULT_OVERLAP;
    }

    @Override
    public List<String> split(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String[] paragraphs = PARAGRAPH_PATTERN.split(text);
        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {
            String trimmedParagraph = paragraph.trim();
            if (trimmedParagraph.isEmpty()) {
                continue;
            }

            if (trimmedParagraph.length() > chunkSize) {
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk = extractOverlap(currentChunk.toString());
                }
                for (String subChunk : splitLargeParagraph(trimmedParagraph)) {
                    currentChunk = packInto(chunks, currentChunk, subChunk);
                }
                continue;
            }

            currentChunk = packInto(chunks, currentChunk, trimmedParagraph);
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }
        return chunks;
    }

    /**
     * 将文本块装入当前分块
     */
    private StringBuilder packInto(List<String> chunks, StringBuilder currentChunk, String piece) {
        int separatorLength = currentChunk.length() > 0 ? 2 : 0;
        int potentialLength = currentChunk.length() + separatorLength + piece.length();

        if (potentialLength > chunkSize && currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
            currentChunk = extractOverlap(currentChunk.toString());
        }

        if (currentChunk.length() > 0) {
            currentChunk.append("\n\n");
        }
        currentChunk.append(piece);
        return currentChunk;
    }

    private StringBuilder extractOverlap(String chunk) {
        if (paragraphOverlapChars <= 0 || chunk == null || chunk.isEmpty()) {
            return new StringBuilder();
        }

        int len = chunk.length();
        if (len <= paragraphOverlapChars) {
            return new StringBuilder(chunk);
        }

        int overlapStart = len - paragraphOverlapChars;
        String rawOverlap = chunk.substring(overlapStart);

        int firstParagraphBreak = rawOverlap.indexOf("\n\n");
        if (firstParagraphBreak != -1 && firstParagraphBreak + 2 < rawOverlap.length()) {
            return new StringBuilder(rawOverlap.substring(firstParagraphBreak + 2));
        }

        return new StringBuilder(rawOverlap.trim());
    }

    private List<String> splitLargeParagraph(String paragraph) {
        List<String> subChunks = new ArrayList<>();
        Matcher matcher = SENTENCE_PATTERN.matcher(paragraph);

        StringBuilder currentChunk = new StringBuilder();
        int lastMatchEnd = 0;

        while (matcher.find()) {
            String sentence = matcher.group();
            lastMatchEnd = matcher.end();

            if (sentence.length() > chunkSize) {
                if (currentChunk.length() > 0) {
                    subChunks.add(currentChunk.toString().trim());
                    currentChunk = new StringBuilder();
                }
                subChunks.addAll(splitByChars(sentence));
                continue;
            }

            if (currentChunk.length() + sentence.length() > chunkSize && currentChunk.length() > 0) {
                subChunks.add(currentChunk.toString().trim());
                currentChunk = extractOverlap(currentChunk.toString());
            }
            currentChunk.append(sentence);
        }

        if (lastMatchEnd < paragraph.length()) {
            String remaining = paragraph.substring(lastMatchEnd);
            if (!remaining.trim().isEmpty()) {
                if (remaining.length() > chunkSize) {
                    if (currentChunk.length() > 0) {
                        subChunks.add(currentChunk.toString().trim());
                        currentChunk = new StringBuilder();
                    }
                    subChunks.addAll(splitByChars(remaining));
                } else {
                    if (currentChunk.length() + remaining.length() > chunkSize && currentChunk.length() > 0) {
                        subChunks.add(currentChunk.toString().trim());
                        currentChunk = extractOverlap(currentChunk.toString());
                    }
                    currentChunk.append(remaining);
                }
            }
        }

        if (currentChunk.length() > 0) {
            subChunks.add(currentChunk.toString().trim());
        }

        return subChunks;
    }

    private List<String> splitByChars(String text) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        int len = text.length();
        while (start < len) {
            int end = Math.min(start + chunkSize, len);
            String candidate = text.substring(start, end).trim();
            if (candidate.isEmpty()) {
                start = end;
                continue;
            }

            if (end == len && !chunks.isEmpty() && candidate.length() < chunkSize * 0.5) {
                String last = chunks.get(chunks.size() - 1);
                if (last.endsWith(candidate)) {
                    break;
                }
                chunks.set(chunks.size() - 1, last + candidate);
                break;
            }

            chunks.add(candidate);
            if (end >= len) {
                break;
            }
            int next = end - paragraphOverlapChars;
            if (next <= start) {
                next = end;
            }
            start = next;
        }
        return chunks;
    }
}
