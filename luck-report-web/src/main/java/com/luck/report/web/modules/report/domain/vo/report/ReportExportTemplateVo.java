package com.luck.report.web.modules.report.domain.vo.report;

import com.luck.report.web.modules.report.controller.manage.ManageController;

import java.io.Serializable;

/**
 * 报表模板导出结果视图对象
 *
 * @author luck-report
 * @since 1.0.0
 */
public class ReportExportTemplateVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 下载文件名（已含 .ureport.xml 后缀）
     */
    private String fileName;

    /**
     * 报表 XML 字节内容
     */
    private byte[] content;

    public ReportExportTemplateVo() {
    }

    public ReportExportTemplateVo(String fileName, byte[] content) {
        this.fileName = fileName;
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
