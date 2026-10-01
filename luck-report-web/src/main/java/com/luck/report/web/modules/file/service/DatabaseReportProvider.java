package com.luck.report.web.modules.file.service;

import com.luck.report.web.modules.file.domain.entity.ReportTemplate;
import com.luck.report.web.common.domain.vo.PageResultVO;
import com.luck.report.web.utils.ReportIdXmlUtils;
import com.luck.report.core.exception.ReportException;
import com.luck.report.core.provider.report.ReportFile;
import com.luck.report.core.provider.report.ReportFilePage;
import com.luck.report.core.provider.report.ReportProvider;
import com.luck.report.core.security.ReportJdbcPasswordXmlUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 数据库存储报表 Provider
 * - prefix: db:
 * - 报表 ID 即 luck_report_template.id，file 入参格式为 "db:123"
 *
 * @author luck
 */
@Slf4j
@Component("bean.databaseReportProvider")
@AllArgsConstructor
public class DatabaseReportProvider implements ReportProvider {

    public static final String PREFIX = "db:";

    @Qualifier("bean.reportTemplateService")
    private final ReportTemplateService luckReportFileService;

    /**
     * 去除 PREFIX 前缀，返回纯 id
     */
    private String sliceId(String id) {
        if (id != null && id.startsWith(PREFIX)) {
            return id.substring(PREFIX.length());
        }
        return id;
    }

    @Override
    public InputStream loadReport(String reportPath) {
        String id = sliceId(reportPath);
        ReportTemplate reportFile = luckReportFileService.getById(id);
        if (reportFile == null) {
            throw new ReportException("error.report.notExist", id);
        }
        String template = Objects.toString(reportFile.getTemplate(), StringUtils.EMPTY);
        return new ByteArrayInputStream(template.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void deleteReport(String reportPath) {
        String id = sliceId(reportPath);
        boolean ok = luckReportFileService.deleteById(id);
        if (!ok) {
            log.warn("Delete report file failed, id={}", id);
        }
    }

    @Override
    public List<ReportFile> getReportFiles() {
        List<ReportTemplate> list = luckReportFileService.listAll();
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(rf -> new ReportFile(
                        rf.getTitle(),
                        rf.getUpdateTime() == null ? new Date() : Date.from(rf.getUpdateTime().atZone(java.time.ZoneId.systemDefault()).toInstant()),
                        false,
                        rf.getId()))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public ReportFilePage pageReportFiles(int pageNum, int pageSize, Map<String, Object> params) {
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        String name = params == null ? null : (String) params.get("name");
        PageResultVO<ReportTemplate> result = luckReportFileService.listPage(name, pageNum, pageSize);
        if (result == null || result.getRecords() == null || result.getRecords().isEmpty()) {
            return ReportFilePage.of(Collections.emptyList(), result == null ? 0L : result.getTotal());
        }
        List<ReportFile> records = new java.util.ArrayList<>(result.getRecords().size());
        for (ReportTemplate rf : result.getRecords()) {
            Date updateDate = rf.getUpdateTime() == null
                    ? null
                    : Date.from(rf.getUpdateTime().atZone(java.time.ZoneId.systemDefault()).toInstant());
            records.add(new ReportFile(rf.getTitle(), updateDate, false, rf.getId()));
        }
        return ReportFilePage.of(records, result.getTotal());
    }

    @Override
    public ReportFile saveReport(String title, String reportPath, String content) {
        // db:123（已存在）或 db:title（创建）
        String id = sliceId(reportPath);
        title = title == null ? StringUtils.EMPTY : title.trim();
        Date now = new Date();
        content = content == null ? StringUtils.EMPTY : content;
        // 持久化前加密自定义 JDBC 密码
        content = ReportJdbcPasswordXmlUtils.encryptJdbcPasswords(content);

        if (StringUtils.isNotBlank(id)) {
            ReportTemplate existing = luckReportFileService.getById(id);
            if (existing != null) {
                existing.setTemplate(ReportIdXmlUtils.ensureReportId(content, existing.getId()));
                if (StringUtils.isNotBlank(title)) {
                    existing.setTitle(title);
                }
                ReportTemplate updated = luckReportFileService.update(existing);
                return buildReportFile(title, updated, now);
            }
            if (ReportIdXmlUtils.isSnowflakeId(id)) {
                ReportTemplate entity = new ReportTemplate();
                entity.setId(id);
                entity.setTitle(title);
                entity.setTemplate(ReportIdXmlUtils.ensureReportId(content, id));
                ReportTemplate saved = luckReportFileService.save(entity);
                return buildReportFile(title, saved, now);
            }
        }
        ReportTemplate entity = new ReportTemplate();
        entity.setTitle(title);
        entity.setTemplate(content);
        ReportTemplate saved = luckReportFileService.save(entity);
        String synced = ReportIdXmlUtils.ensureReportId(content, saved.getId());
        if (!Objects.equals(synced, content)) {
            saved.setTemplate(synced);
            saved = luckReportFileService.update(saved);
        }
        return buildReportFile(title, saved, now);
    }

    /**
     * 构造 saveReport 返回的 ReportFile
     * - path 为数据库主键 id（不含 PREFIX 前缀），与 getReportFiles 保持一致
     * - name 为展示标题
     * - directory 固定 false
     * - updateDate 优先用实体的更新时间，缺失时回退到本次保存时间
     */
    private ReportFile buildReportFile(String title, ReportTemplate entity, Date fallback) {
        String id = entity == null ? null : entity.getId();
        String name = StringUtils.isNotBlank(title) ? title : (entity == null ? null : entity.getTitle());
        Date updateDate = fallback;
        if (entity != null && entity.getUpdateTime() != null) {
            updateDate = Date.from(entity.getUpdateTime().atZone(java.time.ZoneId.systemDefault()).toInstant());
        }
        return new ReportFile(name, updateDate, false, id);
    }

    @Override
    public ReportFile getReportFile(String reportPath) {
        String id = sliceId(reportPath);
        if (StringUtils.isBlank(id)) {
            return null;
        }
        ReportTemplate entity = luckReportFileService.getById(id);
        if (entity == null) {
            return null;
        }
        Date updateDate = entity.getUpdateTime() == null
                ? null
                : Date.from(entity.getUpdateTime().atZone(java.time.ZoneId.systemDefault()).toInstant());
        return new ReportFile(entity.getTitle(), updateDate, false, entity.getId());
    }

    @Override
    public String getName() {
        return "Database";
    }

    @Override
    public boolean disabled() {
        return false;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public ReportFile updateReport(String reportPath, String title) {
        String id = sliceId(reportPath);
        if (StringUtils.isBlank(id)) {
            return null;
        }
        title = title == null ? StringUtils.EMPTY : title.trim();
        if (title.isEmpty()) {
            throw new ReportException("error.report.nameEmpty");
        }
        // 走 updateMeta 白名单更新：只改 title，物理上不会触碰 template 列
        ReportTemplate patch = new ReportTemplate();
        patch.setId(id);
        patch.setTitle(title);
        boolean ok = luckReportFileService.updateMeta(patch);
        if (!ok) {
            throw new ReportException("error.report.notExist", id);
        }
        return new ReportFile(title, new Date(), false, id);
    }
}
