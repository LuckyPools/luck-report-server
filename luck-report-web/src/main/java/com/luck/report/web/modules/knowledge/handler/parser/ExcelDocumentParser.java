package com.luck.report.web.modules.knowledge.handler.parser;

import com.luck.report.core.exception.ReportBizException;
import com.luck.report.web.config.KnowledgeDocumentParseProperties;
import com.luck.report.web.modules.knowledge.handler.parser.DocumentParser;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel 文档解析器（.xlsx / .xls）→ Markdown 表格文本
 *
 * @author luck
 */
@Component
@Order(40)
public class ExcelDocumentParser implements DocumentParser {

    private final KnowledgeDocumentParseProperties properties;
    private final DataFormatter dataFormatter = new DataFormatter();

    public ExcelDocumentParser(KnowledgeDocumentParseProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean supports(String filename, String contentType) {
        if (filename != null) {
            String lower = filename.toLowerCase();
            if (lower.endsWith(".xlsx") || lower.endsWith(".xls")) {
                return true;
            }
        }
        if (contentType == null) {
            return false;
        }
        String ct = contentType.toLowerCase();
        return ct.contains("spreadsheet")
                || ct.contains("excel")
                || ct.contains("ms-excel");
    }

    @Override
    public String parse(MultipartFile file) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cellCount = 0;
        int maxCells = properties.getExcelMaxCells();

        try (InputStream in = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(in)) {
            int sheetCount = workbook.getNumberOfSheets();
            for (int s = 0; s < sheetCount; s++) {
                Sheet sheet = workbook.getSheetAt(s);
                if (sheet == null) {
                    continue;
                }
                List<List<String>> rows = new ArrayList<>();
                for (Row row : sheet) {
                    if (row == null) {
                        continue;
                    }
                    List<String> cells = new ArrayList<>();
                    short lastCell = row.getLastCellNum();
                    for (int c = 0; c < lastCell; c++) {
                        Cell cell = row.getCell(c);
                        String value = cell == null ? "" : dataFormatter.formatCellValue(cell).trim();
                        cells.add(escapeCell(value));
                        cellCount++;
                        if (cellCount > maxCells) {
                            throw new ReportBizException("error.knowledge.agentExcelTooLarge", maxCells);
                        }
                    }
                    // 跳过全空行
                    boolean allBlank = true;
                    for (String cell : cells) {
                        if (!cell.isEmpty()) {
                            allBlank = false;
                            break;
                        }
                    }
                    if (!allBlank) {
                        rows.add(cells);
                    }
                }
                if (rows.isEmpty()) {
                    continue;
                }
                if (sb.length() > 0) {
                    sb.append("\n\n");
                }
                sb.append("## ").append(sheet.getSheetName()).append("\n\n");
                sb.append(rowsToMarkdown(rows));
            }
        } catch (ReportBizException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Excel parse failed: " + e.getMessage(), e);
        }

        return sb.toString().trim();
    }

    private String rowsToMarkdown(List<List<String>> rows) {
        int colCount = 0;
        for (List<String> row : rows) {
            if (row.size() > colCount) {
                colCount = row.size();
            }
        }
        if (colCount == 0) {
            return "";
        }

        // 对齐列数
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
            List<String> row = rows.get(r);
            sb.append('|');
            for (String val : row) {
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
