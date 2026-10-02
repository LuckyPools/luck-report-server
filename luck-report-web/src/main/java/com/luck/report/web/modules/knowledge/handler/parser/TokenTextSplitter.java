package com.luck.report.web.modules.knowledge.handler.parser;

import com.luck.report.web.modules.knowledge.handler.splitter.TextSplitter;

import java.util.ArrayList;
import java.util.List;

/**
 * Token分块器
 *
 * @author luck
 */
public class TokenTextSplitter implements TextSplitter {

    private final int chunkSize;
    private final int overlapSize;

    public TokenTextSplitter(int chunkSize) {
        this(chunkSize, 100);
    }

    public TokenTextSplitter(int chunkSize, int overlapSize) {
        this.chunkSize = chunkSize > 0 ? chunkSize : 1000;
        this.overlapSize = overlapSize >= 0 ? overlapSize : 100;
    }

    @Override
    public List<String> split(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> chunks = new ArrayList<>();
        int textLen = text.length();

        if (textLen <= chunkSize) {
            chunks.add(text);
            return chunks;
        }

        int start = 0;
        while (start < textLen) {
            int end = Math.min(start + chunkSize, textLen);
            String candidate = text.substring(start, end);

            if (end == textLen && !chunks.isEmpty() && candidate.length() < chunkSize * 0.5) {
                String last = chunks.get(chunks.size() - 1);
                if (last.endsWith(candidate)) {
                    break;
                }
                chunks.set(chunks.size() - 1, last + candidate);
                break;
            }

            chunks.add(candidate);

            start += chunkSize - overlapSize;
            if (start <= end - chunkSize) {
                start = end;
            }
        }

        return chunks;
    }
}
