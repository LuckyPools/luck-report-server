/*******************************************************************************
 * Copyright 2017 Bstek
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.luck.report.core.provider.report.classpath;

import com.luck.report.core.exception.ReportException;
import com.luck.report.core.provider.report.ReportFile;
import com.luck.report.core.provider.report.ReportFilePage;
import com.luck.report.core.provider.report.ReportProvider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author Jacky.gao
 * @since 2016年12月4日
 */
public class ClasspathReportProvider implements ReportProvider, ApplicationContextAware {
    public static final String SUFFIX = ".ureport.xml";
    private ApplicationContext applicationContext;

    @Override
    public InputStream loadReport(String reportPath) {
        assert reportPath != null;
        if (!reportPath.isEmpty() && !reportPath.endsWith(SUFFIX)) {
            reportPath = reportPath + SUFFIX;
        }
        Resource resource = applicationContext.getResource(reportPath);
        try {
            return resource.getInputStream();
        } catch (IOException e) {
            String newFileName = null;
            if (reportPath.startsWith("classpath:")) {
                newFileName = "classpath*:" + reportPath.substring(10);
            } else if (reportPath.startsWith("classpath*:")) {
                newFileName = "classpath:" + reportPath.substring(11);
            }
            if (newFileName != null) {
                try {
                    return applicationContext.getResource(reportPath).getInputStream();
                } catch (IOException ex) {
                    throw new ReportException(e);
                }
            }
            throw new ReportException(e);
        }
    }

    @Override
    public String getPrefix() {
        return "classpath";
    }

    @Override
    public void deleteReport(String file) {}

    @Override
    public ReportFile saveReport(String title, String reportPath, String content) {
        return new ReportFile();
    }

    @Override
    public List<ReportFile> getReportFiles() {
        return null;
    }

    @Override
    public ReportFilePage pageReportFiles(int pageNum, int pageSize, Map<String, Object> params) {
        return ReportFilePage.empty();
    }

    @Override
    public boolean disabled() {
        return false;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
