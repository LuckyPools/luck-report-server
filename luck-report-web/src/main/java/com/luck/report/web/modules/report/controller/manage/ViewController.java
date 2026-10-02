package com.luck.report.web.modules.report.controller.manage;

import com.luck.report.web.modules.report.service.ViewRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;

import java.io.IOException;

/**
 * 报表前端页面入口控制器
 */
@Controller("bean.viewController")
@RequestMapping("${luck-report.servletPrefix:}")
public class ViewController {

    /**
     * 后台 servlet 前缀
     */
    @Value("${luck-report.servletPrefix:}")
    private String servletPrefix;

    /**
     * 视图渲染器
     */
    @Autowired
    @Qualifier("bean.viewRenderer")
    private ViewRenderer viewRenderer;

    /**
     * 报表设计器入口
     */
    @GetMapping({"/designer", "/designer/**"})
    public void designer(NativeWebRequest webRequest) throws IOException {
        viewRenderer.render("designer", webRequest, servletPrefix);
    }

    /**
     * 报表预览入口
     */
    @GetMapping({"/preview", "/preview/**"})
    public void preview(NativeWebRequest webRequest) throws IOException {
        viewRenderer.render("preview", webRequest, servletPrefix);
    }

    /**
     * 工作台首页入口
     */
    @GetMapping({
        "/manage", "/manage/**",
        "/datasource", "/datasource/**",
        "/model_config", "/model_config/**",
        "/business_knowledge", "/business_knowledge/**",
        "/agent_knowledge", "/agent_knowledge/**"
    })
    public void manage(NativeWebRequest webRequest) throws IOException {
        viewRenderer.render("index", webRequest, servletPrefix);
    }
}
