package com.luck.report.web.modules.knowledge.handler.parser;

import com.luck.report.core.exception.ReportBizException;
import com.luck.report.web.config.properties.KnowledgeDocumentParseProperties;
import com.luck.report.web.modules.knowledge.handler.parser.DocumentParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * PDF 文本层解析器（无 OCR；扫描件在有效字符不足时失败）
 *
 * @author luck
 */
@Component("bean.pdfDocumentParser")
@Order(20)
public class PdfDocumentParser implements DocumentParser {

    private final KnowledgeDocumentParseProperties properties;

    public PdfDocumentParser(@Qualifier("bean.knowledgeDocumentParseProperties") KnowledgeDocumentParseProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean supports(String filename, String contentType) {
        if (contentType != null && contentType.toLowerCase().contains("pdf")) {
            return true;
        }
        return filename != null && filename.toLowerCase().endsWith(".pdf");
    }

    @Override
    public String parse(MultipartFile file) throws IOException {
        try (InputStream in = file.getInputStream();
             PDDocument document = PDDocument.load(in)) {
            if (document.getNumberOfPages() <= 0) {
                throw new ReportBizException("error.knowledge.agentPdfNoText");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            if (text == null) {
                text = "";
            }
            String trimmed = text.trim();
            if (trimmed.length() < properties.getPdfMinChars()) {
                throw new ReportBizException("error.knowledge.agentPdfNoText");
            }
            return text;
        }
    }
}
