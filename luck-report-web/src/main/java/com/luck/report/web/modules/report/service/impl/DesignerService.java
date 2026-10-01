package com.luck.report.web.modules.report.service.impl;

import com.luck.report.web.common.domain.vo.PageResultVO;
import com.luck.report.web.common.domain.vo.ResultVO;
import com.luck.report.core.cache.ReportDefinitionWrapperCache;
import com.luck.report.core.definition.ReportDefinition;
import com.luck.report.core.definition.ReportDefinitionWrapper;
import com.luck.report.core.dsl.ReportParserLexer;
import com.luck.report.core.dsl.ReportParserParser;
import com.luck.report.core.export.ReportRender;
import com.luck.report.core.expression.ErrorInfo;
import com.luck.report.core.expression.ScriptErrorListener;
import com.luck.report.core.parser.ReportParser;
import com.luck.report.core.provider.report.ReportFile;
import com.luck.report.core.provider.report.ReportFilePage;
import com.luck.report.core.provider.report.ReportProvider;
import com.luck.report.web.cache.ReportScopedCache;
import com.luck.report.web.modules.report.domain.dto.ReportQueryDTO;
import com.luck.report.web.modules.report.domain.vo.report.ReportDefinitionVo;
import com.luck.report.web.modules.report.domain.vo.report.ReportProviderDetailVo;
import com.luck.report.web.modules.report.domain.vo.report.ReportProviderVo;
import com.luck.report.web.exception.ReportDesignException;
import com.luck.report.web.utils.ReportIdXmlUtils;
import com.luck.report.web.utils.ReportUtils;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 报表设计器服务，负责设计器相关业务（脚本校验、新建 / 保存 / 删除 / 加载报表等）。
 * <p>Bean 名：{@code bean.designerService}，避免与第三方系统 Bean 冲突。
 *
 * @author luck-report
 * @since 1.0.0
 */
@Service("bean.designerService")
public class DesignerService implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(DesignerService.class);

    /** 异常消息中的单元格名模式：如 [B3]、[AB12]，用于回查 XML 定义行号 */
    private static final Pattern CELL_NAME_IN_MESSAGE = Pattern.compile("\\[([A-Za-z]{1,3}\\d+)\\]");

    private final List<ReportProvider> reportProviders = new ArrayList<>();

    @Autowired
    @Qualifier("bean.reportRender")
    private ReportRender reportRender;

    @Autowired
    @Qualifier("bean.reportParser")
    private ReportParser reportParser;

    /**
     * 脚本验证：解析脚本中的表达式并收集错误信息。
     */
    public List<ErrorInfo> scriptValidation(String content) {
        CharStream input = CharStreams.fromString(content);
        ReportParserLexer lexer = new ReportParserLexer(input);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        ReportParserParser parser = new ReportParserParser(tokenStream);
        ScriptErrorListener errorListener = new ScriptErrorListener();
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);
        parser.expression();
        return errorListener.getInfos();
    }

    /**
     * 条件脚本验证：解析条件表达式并收集错误信息。
     */
    public List<ErrorInfo> conditionScriptValidation(String content) {
        CharStream input = CharStreams.fromString(content);
        ReportParserLexer lexer = new ReportParserLexer(input);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        ReportParserParser parser = new ReportParserParser(tokenStream);
        ScriptErrorListener errorListener = new ScriptErrorListener();
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);
        parser.expr();
        return errorListener.getInfos();
    }

    /**
     * 解析表达式中的数据集名称。
     */
    public String parseDatasetName(String expr) {
        CharStream input = CharStreams.fromString(expr);
        ReportParserLexer lexer = new ReportParserLexer(input);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        ReportParserParser parser = new ReportParserParser(tokenStream);
        parser.removeErrorListeners();
        ReportParserParser.DatasetContext ctx = parser.dataset();
        return ctx.Identifier().getText();
    }

    /**
     * 保存预览文件到作用域缓存。
     */
    public void savePreviewFile(String reportPath, String content) throws IOException {
        InputStream inputStream = IOUtils.toInputStream(content, "utf-8");
        try {
            ReportDefinition reportDef = reportParser.parse(inputStream, reportPath);
            ReportDefinitionWrapper wrapper = new ReportDefinitionWrapper(reportDef);
            ReportScopedCache.putObject(reportPath, wrapper);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * 加载报表定义。
     * <p>优先从预览作用域缓存加载；缓存不存在时通过 {@link ReportRender} 解析。
     */
    public ReportDefinitionVo loadReport(String reportPath) {
        if (reportPath == null) {
            throw new ReportDesignException("error.report.fileNull");
        }
        ReportDefinition reportDefinition = null;
        String defaultTemplatePath = ReportUtils.getClassTemplatePath();
        if (defaultTemplatePath.equals(reportPath)){
            Object obj = ReportScopedCache.getObject(reportPath);
            if (obj instanceof ReportDefinitionWrapper) {
                ReportDefinitionWrapper wrapper = (ReportDefinitionWrapper) obj;
                reportDefinition = wrapper.getReportDefinition();
                // 移除上传文件的缓存
                ReportScopedCache.removeObject(reportPath);
            }
        }
        if (reportDefinition == null){
            reportDefinition = reportRender.parseReport(reportPath);
        }
        ReportFile reportFile = resolveProvider(reportPath).getReportFile(reportPath);
        return new ReportDefinitionVo(reportDefinition, reportFile.getName());
    }

    /**
     * 删除报表文件。
     */
    public void deleteReportFile(String reportPath) {
        if (reportPath == null) {
            throw new ReportDesignException("error.report.fileNull");
        }
        resolveProvider(reportPath).deleteReport(reportPath);
    }

    /**
     * 将报表 XML 解析为定义 VO，不落盘、不写缓存。
     * <p>供 Virtual 引擎校验与转换为前端 JSON，无副作用。
     *
     * @param content 报表 XML，非空
     * @param reportPath 可选解析上下文路径；空则使用占位 path
     * @return 与 {@link #loadReport(String)} 同构的定义 VO
     */
    public ReportDefinitionVo parseReportXml(String content, String reportPath) {
        if (content == null || content.trim().isEmpty()) {
            throw new ReportDesignException("error.report.contentEmpty");
        }
        String path = (reportPath == null || reportPath.trim().isEmpty())
                ? "virtual:parse.ureport.xml"
                : reportPath.trim();
        InputStream inputStream = IOUtils.toInputStream(content, "utf-8");
        try {
            ReportDefinition reportDef = reportParser.parse(inputStream, path);
            String reportName = path.contains(":")
                    ? path.substring(path.lastIndexOf(':') + 1)
                    : path;
            if (reportName.isEmpty()) {
                reportName = "virtual-parse";
            }
            return new ReportDefinitionVo(reportDef, reportName);
        } catch (ReportDesignException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Parse Report XML Exception", e);
            String msg = e.getMessage();
            if (msg == null || msg.trim().isEmpty()) {
                msg = e.getClass().getSimpleName();
            }
            // 补充消息中涉及的单元格名在原文中的行号，便于调用方（如 AI 解析工具）按行定位
            throw new ReportDesignException(appendCellDefinitionLineHint(msg, content));
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * 方法说明：从异常消息中提取单元格名，回查原始 XML 中该单元格定义所在行号并追加到消息尾部
     * @param msg 解析失败的异常消息，可为空（为空时原样返回）
     * @param content 原始报表 XML 全文，可为空（为空时原样返回）
     * @return 追加行号提示后的消息；无法定位时返回原消息
     */
    private String appendCellDefinitionLineHint(String msg, String content) {
        if (msg == null || msg.trim().isEmpty() || content == null || content.isEmpty()) {
            return msg;
        }
        Set<String> cellNames = new LinkedHashSet<>();
        Matcher matcher = CELL_NAME_IN_MESSAGE.matcher(msg);
        while (matcher.find()) {
            cellNames.add(matcher.group(1));
        }
        if (cellNames.isEmpty()) {
            return msg;
        }
        List<String> lineHints = new ArrayList<>();
        for (String cellName : cellNames) {
            int line = findCellDefinitionLine(content, cellName);
            if (line > 0) {
                lineHints.add(cellName + " 定义位于第 " + line + " 行");
            }
        }
        if (lineHints.isEmpty()) {
            return msg;
        }
        return msg + "（" + String.join("；", lineHints) + "）";
    }

    /**
     * 方法说明：在 XML 原文中查找单元格定义（name="X"）首次出现的行号
     * @param content 原始报表 XML 全文，不可为空
     * @param cellName 单元格名（如 B3），不可为空
     * @return 1 起始的行号；未找到返回 -1
     */
    private int findCellDefinitionLine(String content, String cellName) {
        // 仅匹配定义属性（name="B3"），避免误命中 leftCell/topCell 等引用属性；兼容单双引号
        Pattern namePattern = Pattern.compile("name\\s*=\\s*[\"']" + Pattern.quote(cellName) + "[\"']");
        Matcher matcher = namePattern.matcher(content);
        if (!matcher.find()) {
            return -1;
        }
        // 统计定义位置之前的换行数 +1 得到 1 起始行号
        int lineBreaks = 0;
        for (int i = 0; i < matcher.start(); i++) {
            if (content.charAt(i) == '\n') {
                lineBreaks++;
            }
        }
        return lineBreaks + 1;
    }

    /**
     * 保存报表文件：解析 XML 内容、写入缓存并调用 Provider 持久化。
     */
    public ReportFile saveReportFile(String title, String reportPath, String content) {
        if (reportPath == null) {
            throw new ReportDesignException("error.report.fileNull");
        }
        if (title == null) {
            title = "";
        }
        ReportProvider provider = resolveProvider(reportPath);
        String idPart = reportPath;
        String prefix = provider.getPrefix();
        if (prefix != null && reportPath.startsWith(prefix)) {
            idPart = reportPath.substring(prefix.length());
        }
        if (ReportIdXmlUtils.isSnowflakeId(idPart)) {
            content = ReportIdXmlUtils.ensureReportId(content, idPart);
        }
        ReportDefinition reportDef;
        try {
            InputStream inputStream = IOUtils.toInputStream(content, "utf-8");
            try {
                reportDef = reportParser.parse(inputStream, reportPath);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        } catch (Exception e) {
            logger.error("Save Report Exception", e);
            throw e;
        }
        ReportDefinitionWrapper wrapper = new ReportDefinitionWrapper(reportDef);
        ReportDefinitionWrapperCache.putObject(reportPath, wrapper);
        ReportScopedCache.removeObject(reportPath);
        return provider.saveReport(title, reportPath, content);
    }

    /**
     * 加载所有已启用的报表提供者元数据列表。
     * 仅返回基础信息（name/prefix/disabled），不包含任何文件。
     * 禁用的 provider（{@code disabled() == true}）和未命名的 provider（{@code getName() == null}）会被过滤。
     */
    public List<ReportProviderVo> listReportProviders() {
        List<ReportProviderVo> list = new ArrayList<>();
        for (ReportProvider provider : getValidProviders()) {
            list.add(new ReportProviderVo(provider.getName(), provider.getPrefix(), provider.disabled()));
        }
        return list;
    }

    /**
     * 加载每个 provider 在指定路径下的报表文件列表（含目录）。
     * <p>复用 {@link #listReportProviders()} 的过滤逻辑：先获取已启用的 provider 列表，
     * 再遍历每个 provider 调用 {@code getReportFiles(path)} 收集文件，
     * 最终组装为 {@code List<ReportProviderDetailVo>}（与 {@link #listReportProviders()} 形式一致）。
     */
    public List<ReportProviderDetailVo> loadReportFiles(String path) {
        List<ReportProviderDetailVo> result = new ArrayList<>();
        for (ReportProvider provider : getValidProviders()) {
            result.add(new ReportProviderDetailVo(
                    provider.getName(),
                    provider.getPrefix(),
                    provider.disabled(),
                    provider.getReportFiles(path)
            ));
        }
        return result;
    }

    /**
     * 分页查询报表列表。
     * <p>过滤与分页下沉到 {@link ReportProvider#pageReportFiles(int, int, Map)}，避免在调用方拉取全量数据。
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    public PageResultVO<ReportFile> queryReports(ReportQueryDTO queryDTO) {
        try {
            String provider = queryDTO.getProvider();
            String reportName = queryDTO.getReportName();
            String directory = queryDTO.getDirectory();
            int pageNum = queryDTO.getPageNum() == null ? 1 : queryDTO.getPageNum();
            int pageSize = queryDTO.getPageSize() == null ? 10 : queryDTO.getPageSize();

            ReportProvider targetProvider = findProviderByPrefix(provider);
            if (targetProvider == null) {
                return PageResultVO.error("Report provider not found");
            }

            // 透传给 Provider 的参数：路径、名称模糊匹配、是否包含目录项
            Map<String, Object> params = new HashMap<>(4);
            if (directory != null && !directory.isEmpty()) {
                params.put("path", directory);
            }
            if (reportName != null && !reportName.isEmpty()) {
                params.put("name", reportName);
            }
            // 设计器场景下不展示目录项，仅展示报表文件
            params.put("includeDirectory", Boolean.FALSE);

            ReportFilePage result = targetProvider.pageReportFiles(pageNum, pageSize, params);
            List<ReportFile> records = result == null ? Collections.emptyList() : result.getRecords();
            long total = result == null ? 0L : result.getTotal();
            return PageResultVO.success(records, total, pageNum, pageSize);
        } catch (Exception e) {
            logger.error("查询报表列表异常", e);
            return PageResultVO.error("Failed to query report list: " + e.getMessage());
        }
    }

    /** 根据 prefix 查找对应的 ReportProvider。 */
    private ReportProvider findProviderByPrefix(String prefix) {
        if (prefix == null) {
            return null;
        }
        for (ReportProvider p : reportProviders) {
            if (prefix.equals(p.getPrefix())) {
                return p;
            }
        }
        return null;
    }

    /** 过滤出已启用且命名的 provider 列表，供 {@link #listReportProviders()} 与 {@link #loadReportFiles(String)} 复用。 */
    private List<ReportProvider> getValidProviders() {
        return reportProviders.stream()
                .filter(p -> !p.disabled() && p.getName() != null)
                .collect(Collectors.toList());
    }

    /**
     * 新建报表：使用空白模板在指定 Provider 下创建报表。
     * @return 创建结果，code=0 成功，data 为保存后的 {@link ReportFile}（path 不含 provider 前缀）；
     *         非 0 为错误码，message 为错误信息
     */
    public ResultVO<ReportFile> createReport(String fileName, String providerPrefix) {
        try {
            if (fileName == null || fileName.trim().isEmpty()) {
                return ResultVO.error(400, "File name can not be empty.");
            }
            if (providerPrefix == null || providerPrefix.trim().isEmpty()) {
                return ResultVO.error(400, "Report provider can not be empty.");
            }
            fileName = fileName.trim();
            providerPrefix = providerPrefix.trim();

            String reportPath = providerPrefix + fileName;
            ReportProvider targetProvider;
            try {
                targetProvider = resolveProvider(reportPath);
            } catch (ReportDesignException e) {
                return ResultVO.error(404, "Provider [" + providerPrefix + "] not found available report provider.");
            }

            // 检查报表是否已存在
            ReportFile existingFile = targetProvider.getReportFile(fileName);
            if(existingFile != null){
                return ResultVO.error(409, "Report [" + fileName + "] already exists in provider [" + providerPrefix + "].");
            }

            // 读取空白模板
            String content;
            InputStream templateStream = null;
            try {
                String defaultTemplatePath = ReportUtils.getDefaultTemplatePath();
                ClassPathResource resource = new ClassPathResource(defaultTemplatePath);
                if (!resource.exists()) {
                    return ResultVO.error(500, "Default report template not found: " + defaultTemplatePath);
                }
                templateStream = resource.getInputStream();
                content = IOUtils.toString(templateStream, "utf-8");
            } catch (IOException e) {
                logger.error("Failed to load default report template", e);
                return ResultVO.error(500, "Failed to load default report template: " + e.getMessage());
            } finally {
                IOUtils.closeQuietly(templateStream);
            }

            // 解析模板并写入缓存
            InputStream contentStream = null;
            try {
                contentStream = IOUtils.toInputStream(content, "utf-8");
                ReportDefinition reportDef = reportParser.parse(contentStream, reportPath);
                ReportDefinitionWrapper wrapper = new ReportDefinitionWrapper(reportDef);
                ReportDefinitionWrapperCache.putObject(reportPath, wrapper);
            } catch (Exception e) {
                logger.error("Failed to parse default report template", e);
                return ResultVO.error(500, "Failed to parse default report template: " + e.getMessage());
            } finally {
                IOUtils.closeQuietly(contentStream);
            }

            // 保存并取回权威 ReportFile（path 不带 provider 前缀）
            ReportFile savedFile = targetProvider.saveReport(fileName, reportPath, content);
            if (savedFile == null) {
                return ResultVO.error(500, "Provider returned empty ReportFile after save.");
            }
            return ResultVO.success("Created", savedFile);
        } catch (Exception e) {
            logger.error("Create report exception", e);
            return ResultVO.error(500, "Create report failed: " + e.getMessage());
        }
    }

    /**
     * 复制报表：读取源报表内容，保存到同一 provider 下的新 reportPath。
     * <p>新 reportPath 由调用方决定（推荐格式：{@code providerPrefix + newName}），由 provider 自行决定是否追加后缀 / 是否将 newName 视为主键 id。
     *
     * @param sourceFilePath 源报表唯一路径（带 provider 前缀），如 file:xxx.ureport.xml / db:123
     * @param newFilePath    目标报表唯一路径（带 provider 前缀），如 file:xxx_copy.ureport.xml / db:xxx_copy
     * @param newTitle       目标报表展示名（db: provider 用作 title）
     * @return 复制结果，code=0 成功，data 为保存后的 {@link ReportFile}（path 不含 provider 前缀）
     */
    public ResultVO<ReportFile> copyReport(String sourceFilePath, String newFilePath, String newTitle) {
        try {
            if (sourceFilePath == null || sourceFilePath.trim().isEmpty()) {
                return ResultVO.error(400, "Source report path can not be empty.");
            }
            if (newFilePath == null || newFilePath.trim().isEmpty()) {
                return ResultVO.error(400, "Target report path can not be empty.");
            }
            sourceFilePath = sourceFilePath.trim();
            newFilePath = newFilePath.trim();
            if (newTitle == null) {
                newTitle = "";
            } else {
                newTitle = newTitle.trim();
            }

            ReportProvider provider;
            try {
                provider = resolveProvider(sourceFilePath);
            } catch (ReportDesignException e) {
                return ResultVO.error(404, "Provider for [" + sourceFilePath + "] not found available report provider.");
            }
            // 新路径与源路径必须属于同一 provider；若新路径前缀指向其他 provider，复制会失去归属，视为非法
            if (!newFilePath.startsWith(provider.getPrefix())) {
                return ResultVO.error(400, "Target path [" + newFilePath + "] must share the same provider prefix [" + provider.getPrefix() + "] as source.");
            }

            // 读取源报表内容
            String content;
            InputStream sourceStream = null;
            try {
                sourceStream = provider.loadReport(sourceFilePath);
                if (sourceStream == null) {
                    return ResultVO.error(404, "Source report content not found: " + sourceFilePath);
                }
                content = IOUtils.toString(sourceStream, "utf-8");
            } catch (Exception e) {
                logger.error("Failed to load source report [{}]", sourceFilePath, e);
                return ResultVO.error(500, "Failed to load source report: " + e.getMessage());
            } finally {
                IOUtils.closeQuietly(sourceStream);
            }

            // 解析并写入缓存（与 createReport 保持一致，使新报表可在预览作用域中正确加载）
            InputStream contentStream = null;
            try {
                contentStream = IOUtils.toInputStream(content, "utf-8");
                ReportDefinition reportDef = reportParser.parse(contentStream, newFilePath);
                ReportDefinitionWrapper wrapper = new ReportDefinitionWrapper(reportDef);
                ReportDefinitionWrapperCache.putObject(newFilePath, wrapper);
            } catch (Exception e) {
                logger.error("Failed to parse copied report [{}]", newFilePath, e);
                return ResultVO.error(500, "Failed to parse copied report: " + e.getMessage());
            } finally {
                IOUtils.closeQuietly(contentStream);
            }

            ReportFile savedFile = provider.saveReport(newTitle, newFilePath, content);
            if (savedFile == null || savedFile.getPath() == null) {
                return ResultVO.error(500, "Provider returned empty ReportFile after copy (provider may be read-only).");
            }
            return ResultVO.success("Copied", savedFile);
        } catch (Exception e) {
            logger.error("Copy report exception", e);
            return ResultVO.error(500, "Copy report failed: " + e.getMessage());
        }
    }

    /**
     * 根据报表文件路径匹配对应的 ReportProvider，找不到时抛异常。
     */
    public ReportProvider resolveProvider(String reportPath) {
        for (ReportProvider provider : reportProviders) {
            String prefix = provider.getPrefix();
            if (prefix != null && reportPath.startsWith(prefix)) {
                return provider;
            }
        }
        throw new ReportDesignException("error.report.notSupport", reportPath);
    }

    /**
     * 暴露给同包或同模块的内部辅助：获取当前已注入的 provider 列表副本。
     */
    public List<ReportProvider> getReportProviders() {
        return Collections.unmodifiableList(reportProviders);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (!reportProviders.isEmpty()) {
            return;
        }
        for (ReportProvider provider : applicationContext.getBeansOfType(ReportProvider.class).values()) {
            if (provider.disabled()) {
                continue;
            }
            reportProviders.add(provider);
        }
        logger.info("报表设计器服务初始化完成,共加载 {} 个报表来源", reportProviders.size());
    }
}
