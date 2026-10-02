package com.luck.report.web.modules.knowledge.handler.parser;

import com.luck.report.web.modules.knowledge.handler.parser.DocumentParser;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Word 文档解析器（.docx / .doc）
 *
 * @author luck
 */
@Component("bean.wordDocumentParser")
@Order(30)
public class WordDocumentParser implements DocumentParser {

    @Override
    public boolean supports(String filename, String contentType) {
        if (filename != null) {
            String lower = filename.toLowerCase();
            if (lower.endsWith(".docx") || lower.endsWith(".doc")) {
                return true;
            }
        }
        if (contentType == null) {
            return false;
        }
        String ct = contentType.toLowerCase();
        return ct.contains("word")
                || ct.contains("msword")
                || ct.contains("officedocument.wordprocessingml");
    }

    @Override
    public String parse(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        String lower = filename != null ? filename.toLowerCase() : "";
        String contentType = file.getContentType() != null ? file.getContentType().toLowerCase() : "";
        if (lower.endsWith(".doc") && !lower.endsWith(".docx")) {
            return parseDoc(file);
        }
        if (lower.isEmpty() && contentType.contains("msword")
                && !contentType.contains("officedocument")) {
            return parseDoc(file);
        }
        return parseDocx(file);
    }

    private String parseDocx(MultipartFile file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (InputStream in = file.getInputStream();
             XWPFDocument document = new XWPFDocument(in)) {
            for (IBodyElement element : document.getBodyElements()) {
                if (element instanceof XWPFParagraph) {
                    XWPFParagraph paragraph = (XWPFParagraph) element;
                    String text = paragraph.getText();
                    if (text != null && !text.trim().isEmpty()) {
                        if (sb.length() > 0) {
                            sb.append('\n');
                        }
                        sb.append(toMarkdownHeadingPrefix(paragraph)).append(text.trim());
                    }
                } else if (element instanceof XWPFTable) {
                    if (sb.length() > 0) {
                        sb.append("\n\n");
                    }
                    sb.append(tableToMarkdown((XWPFTable) element));
                    sb.append('\n');
                }
            }
        }
        return sb.toString().trim();
    }

    private String parseDoc(MultipartFile file) throws IOException {
        try (InputStream in = file.getInputStream();
             HWPFDocument document = new HWPFDocument(in);
             WordExtractor extractor = new WordExtractor(document)) {
            String text = extractor.getText();
            return text != null ? text.trim() : "";
        }
    }

    private String tableToMarkdown(XWPFTable table) {
        List<List<String>> rows = new ArrayList<>();
        for (XWPFTableRow row : table.getRows()) {
            List<String> cells = new ArrayList<>();
            for (XWPFTableCell cell : row.getTableCells()) {
                String cellText = cell.getText();
                cells.add(escapeCell(cellText != null ? cellText.trim() : ""));
            }
            if (!cells.isEmpty()) {
                rows.add(cells);
            }
        }
        if (rows.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        List<String> header = rows.get(0);
        sb.append('|');
        for (String h : header) {
            sb.append(' ').append(h).append(" |");
        }
        sb.append('\n');
        sb.append('|');
        for (int i = 0; i < header.size(); i++) {
            sb.append(" --- |");
        }
        sb.append('\n');
        for (int r = 1; r < rows.size(); r++) {
            List<String> row = rows.get(r);
            sb.append('|');
            for (int c = 0; c < header.size(); c++) {
                String val = c < row.size() ? row.get(c) : "";
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

    /**
     * Heading1..6 → Markdown # 前缀，便于 StructureTextSplitter
     */
    private static String toMarkdownHeadingPrefix(XWPFParagraph paragraph) {
        String style = paragraph.getStyle();
        if (style == null) {
            return "";
        }
        String lower = style.toLowerCase();
        if (lower.startsWith("heading")) {
            String num = lower.replace("heading", "").trim();
            try {
                int level = Integer.parseInt(num);
                if (level >= 1 && level <= 6) {
                    StringBuilder hashes = new StringBuilder();
                    for (int i = 0; i < level; i++) {
                        hashes.append('#');
                    }
                    return hashes.append(' ').toString();
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return "";
    }
}
