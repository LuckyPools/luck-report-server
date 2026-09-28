package com.luck.report.web.modules.knowledge.handler.splitter;

import com.luck.report.web.modules.knowledge.handler.splitter.TextSplitter;

import java.util.ArrayList;
import java.util.List;

/**
 * 递归字符分块器。
 * 默认分隔符对齐 Dify FixedRecursiveCharacterTextSplitter：
 * ["\\n\\n", "\\n", "。", ". ", " ", ""]
 *
 * @author luck
 */
public class RecursiveTextSplitter implements TextSplitter {

    private final int chunkSize;
    private final int overlapSize;
    private final List<String> separators;

    public RecursiveTextSplitter(int chunkSize) {
        this(chunkSize, 100);
    }

    public RecursiveTextSplitter(int chunkSize, int overlapSize) {
        this.chunkSize = chunkSize > 0 ? chunkSize : 1000;
        this.overlapSize = overlapSize >= 0 ? overlapSize : 100;
        // Dify fixed_text_splitter.py 默认 separators
        this.separators = new ArrayList<>();
        this.separators.add("\n\n");
        this.separators.add("\n");
        this.separators.add("。");
        this.separators.add(". ");
        this.separators.add(" ");
        this.separators.add("");
    }

    @Override
    public List<String> split(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> chunks = new ArrayList<>();
        recursiveSplit(text, separators, 0, chunks);

        // 合并相邻的小块以利用空间（带重叠）
        return mergeChunks(chunks);
    }

    /**
     * 递归切分：按当前分隔符切分，超长的部分用下一级分隔符继续切
     */
    private void recursiveSplit(String text, List<String> seps, int sepIndex, List<String> result) {
        if (text.isEmpty()) {
            return;
        }

        // 最后一层：硬切
        if (sepIndex >= seps.size()) {
            hardSplit(text, result);
            return;
        }

        String sep = seps.get(sepIndex);

        // 短文本直接返回
        if (text.length() <= chunkSize) {
            result.add(text);
            return;
        }

        if (sep.isEmpty()) {
            // 空分隔符 = 按字符硬切
            hardSplit(text, result);
            return;
        }

        List<String> parts;
        if ("\n\n".equals(sep)) {
            parts = splitByRegex(text, "\n\\s*\n+");
        } else if ("\n".equals(sep)) {
            parts = splitByRegex(text, "\n");
        } else if (" ".equals(sep)) {
            // Dify: re.split(r" +", text)
            parts = splitByRegex(text, " +");
        } else {
            // Dify: keep separator on preceding chunk for 。 and ". "
            parts = splitByLiteralKeepSep(text, sep);
        }

        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            if (trimmed.length() <= chunkSize) {
                result.add(trimmed);
            } else {
                // 超长部分用下一级分隔符继续切
                recursiveSplit(trimmed, seps, sepIndex + 1, result);
            }
        }
    }

    private List<String> splitByRegex(String text, String regex) {
        String[] arr = text.split(regex, -1);
        List<String> parts = new ArrayList<>();
        for (String s : arr) {
            parts.add(s);
        }
        return parts;
    }

    /** Dify: splits = [s + separator for s in splits[:-1]] + splits[-1:] */
    private List<String> splitByLiteralKeepSep(String text, String sep) {
        List<String> parts = new ArrayList<>();
        int from = 0;
        int idx;
        while ((idx = text.indexOf(sep, from)) >= 0) {
            parts.add(text.substring(from, idx + sep.length()));
            from = idx + sep.length();
        }
        if (from < text.length() || parts.isEmpty()) {
            parts.add(text.substring(from));
        }
        return parts;
    }

    /**
     * 按字符硬切（带 overlap）
     */
    private void hardSplit(String text, List<String> result) {
        int start = 0;
        int len = text.length();
        while (start < len) {
            int end = Math.min(start + chunkSize, len);
            String candidate = text.substring(start, end);

            if (end == len && !result.isEmpty() && candidate.length() < chunkSize * 0.5) {
                String last = result.get(result.size() - 1);
                if (last.endsWith(candidate)) {
                    break;
                }
                result.set(result.size() - 1, last + candidate);
                break;
            }

            result.add(candidate);
            if (end >= len) {
                break;
            }
            int next = end - overlapSize;
            if (next <= start) {
                next = end; // 防死循环
            }
            start = next;
        }
    }

    /**
     * 合并相邻小块，充分利用 chunkSize 空间，并添加重叠
     */
    private List<String> mergeChunks(List<String> rawChunks) {
        if (rawChunks.isEmpty()) {
            return rawChunks;
        }

        List<String> merged = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String chunk : rawChunks) {
            if (current.length() == 0) {
                current.append(chunk);
            } else if (current.length() + chunk.length() + 1 <= chunkSize) {
                current.append("\n").append(chunk);
            } else {
                merged.add(current.toString());
                // 重叠：保留尾部
                String tail = current.length() > overlapSize
                        ? current.substring(current.length() - overlapSize)
                        : current.toString();
                current = new StringBuilder(tail);
                current.append("\n").append(chunk);
            }
        }

        if (current.length() > 0) {
            merged.add(current.toString());
        }

        return merged;
    }
}
