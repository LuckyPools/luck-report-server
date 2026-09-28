package com.luck.report.web.modules.knowledge.handler.splitter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * FastGPT markdownTableSplit / strIsMdTable 移植（char 模式）。
 */
public class TableTextSplitter implements TextSplitter {

    private static final int DEFAULT_MAX_SIZE = 8000;
    private static final Pattern SEPARATOR_LINE =
            Pattern.compile("^(\\|[\\s:]*-+[\\s:]*)+\\|$");

    private final int chunkSize;
    private final int maxSize;

    public TableTextSplitter(int chunkSize) {
        this(chunkSize, DEFAULT_MAX_SIZE);
    }

    public TableTextSplitter(int chunkSize, int maxSize) {
        if (!Double.isFinite(chunkSize) || chunkSize <= 0) {
            throw new IllegalArgumentException("Chunk size must be a positive finite number");
        }
        this.chunkSize = chunkSize;
        this.maxSize = maxSize > 0 ? maxSize : DEFAULT_MAX_SIZE;
    }

    /** FastGPT strIsMdTable */
    public static boolean strIsMdTable(String str) {
        if (str == null || !str.contains("|")) {
            return false;
        }
        String[] lines = str.split("\n", -1);
        if (lines.length < 2) {
            return false;
        }
        String headerLine = lines[0].trim();
        if (!headerLine.startsWith("|") || !headerLine.endsWith("|")) {
            return false;
        }
        String separatorLine = lines[1].trim();
        if (!SEPARATOR_LINE.matcher(separatorLine).matches()) {
            return false;
        }
        for (int i = 2; i < lines.length; i++) {
            String dataLine = lines[i].trim();
            if (!dataLine.isEmpty() && (!dataLine.startsWith("|") || !dataLine.endsWith("|"))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<String> split(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<String>();
        }
        if (strIsMdTable(text.trim())) {
            return markdownTableSplit(text);
        }
        // Excel/CSV Parser 输出 "## Sheet\n\n|...|"：按 Sheet 分段后再走 markdownTableSplit
        List<String> sections = splitByMarkdownH2(text);
        List<String> chunks = new ArrayList<String>();
        for (String section : sections) {
            String trimmed = section.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String title = "";
            String body = trimmed;
            if (trimmed.startsWith("## ")) {
                int nl = trimmed.indexOf('\n');
                if (nl < 0) {
                    chunks.add(trimmed);
                    continue;
                }
                title = trimmed.substring(0, nl).trim();
                body = trimmed.substring(nl + 1).trim();
            }
            if (strIsMdTable(body)) {
                for (String tableChunk : markdownTableSplit(body)) {
                    if (title.isEmpty()) {
                        chunks.add(tableChunk);
                    } else {
                        chunks.add(title + "\n\n" + tableChunk);
                    }
                }
            } else if (!body.isEmpty()) {
                chunks.add(trimmed);
            }
        }
        return chunks;
    }

    private static List<String> splitByMarkdownH2(String text) {
        List<String> sections = new ArrayList<String>();
        String[] lines = text.split("\n", -1);
        StringBuilder current = new StringBuilder();
        for (String line : lines) {
            if (line.startsWith("## ") && current.length() > 0) {
                sections.add(current.toString());
                current.setLength(0);
            }
            if (current.length() > 0) {
                current.append('\n');
            }
            current.append(line);
        }
        if (current.length() > 0) {
            sections.add(current.toString());
        }
        return sections;
    }

    /** FastGPT markdownTableSplit（仅 char 分支） */
    List<String> markdownTableSplit(String text) {
        List<String> splitText2Lines = new ArrayList<String>();
        for (String line : text.split("\n", -1)) {
            if (!line.trim().isEmpty()) {
                splitText2Lines.add(line);
            }
        }

        if (splitText2Lines.size() < 2) {
            List<String> single = new ArrayList<String>();
            single.add(text);
            return single;
        }

        String header = splitText2Lines.get(0);
        String mdSplitString = splitText2Lines.get(1);
        String defaultChunk = header + "\n" + mdSplitString + "\n";

        if (splitText2Lines.size() == 2) {
            return new ArrayList<String>();
        }

        List<String> chunks = new ArrayList<String>();
        String chunk = defaultChunk;

        for (int i = 2; i < splitText2Lines.size(); i++) {
            int chunkLength = chunk.length();
            int nextLineLength = splitText2Lines.get(i).length();

            if (chunkLength + nextLineLength > chunkSize) {
                if (chunkLength > maxSize) {
                    String content = chunk.replace(defaultChunk, "").trim();
                    chunks.addAll(fallbackCommonSplit(content));
                } else if (!chunk.equals(defaultChunk)) {
                    chunks.add(chunk);
                }
                chunk = defaultChunk;
            }
            chunk += splitText2Lines.get(i) + "\n";
        }

        if (chunk.length() > 0 && (!chunk.equals(defaultChunk) || chunks.isEmpty())) {
            chunks.add(chunk);
        }
        return chunks;
    }

    private List<String> fallbackCommonSplit(String content) {
        if (content == null || content.isEmpty()) {
            return new ArrayList<String>();
        }
        int overlapSize = (int) Math.round(chunkSize * 0.15);
        return new StructureTextSplitter(chunkSize, overlapSize).split(content);
    }
}
