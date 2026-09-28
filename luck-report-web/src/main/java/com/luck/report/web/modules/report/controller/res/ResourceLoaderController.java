package com.luck.report.web.modules.report.controller.res;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.infra.modules.servlet.provider.ApiResponse;
import com.luck.report.infra.modules.servlet.utils.HttpUtils;
import com.luck.report.web.modules.report.service.impl.ResourceLoaderService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * 资源加载控制器
 * 替代原有的ResourceLoaderServletAction，提供静态资源访问功能
 */
@RestController("bean.resourceLoaderController")
@RequestMapping("${luck-report.servletPrefix:}/res")
public class ResourceLoaderController {


    @Autowired
    private ResourceLoaderService resourceLoaderService;

    /**
     * 工具配置接口
     */
    @RequestMapping("/tools")
    public Map<String, Object> tools() {
        return resourceLoaderService.buildToolsConfig();
    }
}
