package com.luck.report.web.modules.knowledge.handler.parser;

import com.luck.report.core.exception.ReportBizException;
import com.luck.report.web.config.KnowledgeDocumentParseProperties;
import com.luck.report.web.i18n.ReportI18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 文档解析器工厂：按文件类型选择 {@link DocumentParser}，产出纯文本后再交切割链路
 *
 * @author luck
 */
@Component
public class DocumentParserFactory {

    private static final Logger log = LoggerFactory.getLogger(DocumentParserFactory.class);

    private final List<DocumentParser> parsers;
    private final KnowledgeDocumentParseProperties properties;

    public DocumentParserFactory(List<DocumentParser> parsers, KnowledgeDocumentParseProperties properties) {
        this.parsers = parsers;
        this.properties = properties;
    }

    /**
     * 解析上传文件为纯文本
     *
     * @param file 上传文件
     * @return 文本内容
     */
    public String parse(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ReportBizException("error.knowledge.agentFileRequired");
        }

        long maxBytes = properties.getMaxFileBytes();
        if (maxBytes > 0 && file.getSize() > maxBytes) {
            throw new ReportBizException("error.knowledge.agentFileTooLarge", maxBytes);
        }

        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();
        DocumentParser parser = resolve(filename, contentType);
        if (parser == null) {
            throw new ReportBizException("error.knowledge.agentUnsupportedFileType",
                    contentType != null ? contentType : filename);
        }

        try {
            String content = parser.parse(file);
            if (content == null) {
                content = "";
            }
            log.info("文档解析完成, parser={}, filename={}, chars={}",
                    parser.getClass().getSimpleName(), filename, content.length());
            return content;
        } catch (ReportBizException e) {
            throw e;
        } catch (IOException e) {
            log.error("文档解析失败, filename={}", filename, e);
            throw new ReportBizException("error.knowledge.agentReadFileFailed", ReportI18n.messageOf(e));
        } catch (Exception e) {
            log.error("文档解析异常, filename={}", filename, e);
            throw new ReportBizException("error.knowledge.agentParseFailed", ReportI18n.messageOf(e));
        }
    }

    private DocumentParser resolve(String filename, String contentType) {
        for (DocumentParser parser : parsers) {
            if (parser.supports(filename, contentType)) {
                return parser;
            }
        }
        return null;
    }
}
