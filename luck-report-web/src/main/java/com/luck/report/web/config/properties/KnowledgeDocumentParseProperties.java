package com.luck.report.web.config.properties;

/**
 * 知识库文档解析配置
 * <p>前缀：{@code luck-report.vector.document-parse}（由 {@link com.luck.report.web.config.KnowledgeChunkConfiguration} 绑定）。
 *
 * @author luck
 */
public class KnowledgeDocumentParseProperties {

    /** 单文件解析大小上限（字节），默认 20MB */
    private long maxFileBytes = 20L * 1024 * 1024;

    /** PDF 抽取有效字符下限，低于则视为疑似扫描件 */
    private int pdfMinChars = 50;

    /** Excel 最大单元格数护栏 */
    private int excelMaxCells = 50000;

    public long getMaxFileBytes() {
        return maxFileBytes;
    }

    public void setMaxFileBytes(long maxFileBytes) {
        this.maxFileBytes = maxFileBytes;
    }

    public int getPdfMinChars() {
        return pdfMinChars;
    }

    public void setPdfMinChars(int pdfMinChars) {
        this.pdfMinChars = pdfMinChars;
    }

    public int getExcelMaxCells() {
        return excelMaxCells;
    }

    public void setExcelMaxCells(int excelMaxCells) {
        this.excelMaxCells = excelMaxCells;
    }
}
