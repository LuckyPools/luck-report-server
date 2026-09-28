package com.luck.report.web.modules.knowledge.handler.parser;

import com.luck.report.web.modules.knowledge.handler.splitter.TextSplitter;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 知识库文档解析器
 * 将上传文件抽取为纯文本，再交给 {@link TextSplitter} 切分
 *
 * @author luck
 */
public interface DocumentParser {

    /**
     * 是否支持该文件
     *
     * @param filename    原始文件名，可为 null
     * @param contentType Content-Type，可为 null
     * @return true 表示可由本解析器处理
     */
    boolean supports(String filename, String contentType);

    /**
     * 解析文件为纯文本（UTF-8 语义）
     *
     * @param file 上传文件
     * @return 抽取后的文本，不能为 null
     * @throws IOException 读写失败
     */
    String parse(MultipartFile file) throws IOException;
}
