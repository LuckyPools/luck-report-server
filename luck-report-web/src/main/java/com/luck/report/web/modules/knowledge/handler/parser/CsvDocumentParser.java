package com.luck.report.web.modules.knowledge.handler.parser;

import com.luck.report.web.config.properties.KnowledgeDocumentParseProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV → Markdown 表，供 TableTextSplitter（FastGPT markdownTableSplit）使用。
 */
@Component("bean.csvDocumentParser")
@Order(45)
public class CsvDocumentParser implements DocumentParser {

    private final KnowledgeDocumentParseProperties properties;

    public CsvDocumentParser(@Qualifier("bean.knowledgeDocumentParseProperties") KnowledgeDocumentParseProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean supports(String filename, String contentType) {
        if (filename != null && filename.toLowerCase().endsWith(".csv")) {
            return true;
        }
        return contentType != null && contentType.toLowerCase().contains("csv");
    }

    @Override
    public String parse(MultipartFile file) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        int cellCount = 0;
        int maxCells = properties.getExcelMaxCells();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> cells = parseCsvLine(line);
                boolean allBlank = true;
                for (String c : cells) {
                    if (!c.isEmpty()) {
                        allBlank = false;
                        break;
                    }
                }
                if (allBlank) {
                    continue;
                }
                cellCount += cells.size();
                if (maxCells > 0 && cellCount > maxCells) {
                    throw new com.luck.report.core.exception.ReportBizException(
                            "error.knowledge.agentExcelTooLarge", maxCells);
                }
                rows.add(cells);
            }
        }
        if (rows.isEmpty()) {
            return "";
        }
        return "## sheet\n\n" + rowsToMarkdown(rows);
    }

    private static List<String> parseCsvLine(String line) {
        List<String> cells = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(ch);
                }
            } else if (ch == '"') {
                inQuotes = true;
            } else if (ch == ',') {
                cells.add(escapeCell(cur.toString().trim()));
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        cells.add(escapeCell(cur.toString().trim()));
        return cells;
    }

    private static String rowsToMarkdown(List<List<String>> rows) {
        int colCount = 0;
        for (List<String> row : rows) {
            if (row.size() > colCount) {
                colCount = row.size();
            }
        }
        for (List<String> row : rows) {
            while (row.size() < colCount) {
                row.add("");
            }
        }
        StringBuilder sb = new StringBuilder();
        List<String> header = rows.get(0);
        sb.append('|');
        for (String h : header) {
            sb.append(' ').append(h).append(" |");
        }
        sb.append('\n');
        sb.append('|');
        for (int i = 0; i < colCount; i++) {
            sb.append(" --- |");
        }
        sb.append('\n');
        for (int r = 1; r < rows.size(); r++) {
            sb.append('|');
            for (String val : rows.get(r)) {
                sb.append(' ').append(val).append(" |");
            }
            sb.append('\n');
        }
        return sb.toString().trim();
    }

    private static String escapeCell(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.replace("|", "\\|").replace("\n", " ");
    }
}
