package com.luck.report.web.modules.knowledge.handler.parser;

import com.luck.report.web.modules.knowledge.handler.parser.DocumentParser;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 纯文本文档解析器（txt / md / json / xml / csv / log 等）
 *
 * @author luck
 */
@Component
@Order(100)
public class PlainTextDocumentParser implements DocumentParser {

    @Override
    public boolean supports(String filename, String contentType) {
        if (contentType != null && contentType.toLowerCase().startsWith("text/")) {
            return true;
        }
        if (filename == null) {
            return false;
        }
        String lower = filename.toLowerCase();
        return lower.endsWith(".txt")
                || lower.endsWith(".md")
                || lower.endsWith(".markdown")
                || lower.endsWith(".json")
                || lower.endsWith(".xml")
                || lower.endsWith(".log");
    }

    @Override
    public String parse(MultipartFile file) throws IOException {
        return new String(file.getBytes(), StandardCharsets.UTF_8);
    }
}
