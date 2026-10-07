package com.luck.report.web.modules.report.service.impl;

import com.luck.report.core.Utils;
import com.luck.report.infra.modules.vector.service.VectorStore;
import com.luck.report.web.config.properties.AgentTraceProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 资源加载服务
 */
@Service("bean.resourceLoaderService")
@RequiredArgsConstructor
public class ResourceLoaderService {

    /**
     * agent 链路日志总开关，随工具配置一并返回，供前端决定是否上报
     */
    @Qualifier("bean.agentTraceProperties")
    private final AgentTraceProperties agentTraceProperties;

    /**
     * 向量存储实现；EmptyVectorStore 时 vectorEnabled=false
     */
    private final VectorStore vectorStore;

    /**
     * 配置的向量库类型（postgresql/milvus/chroma），未配置时为空
     */
    @Value("${luck-report.vector.type:}")
    private String vectorType;

    /**
     * 默认语言（可选）。未在配置文件声明或为空时回落 zh_CN。
     */
    @Value("${luck-report.locale:zh_CN}")
    private String locale;

    /**
     * 构建工具配置信息
     */
    public Map<String, Object> buildToolsConfig() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("version", readVersion());
        result.put("encoding", "UTF-8");
        result.put("debug", Utils.isDebug());
        result.put("agentTraceEnabled", agentTraceProperties.isEnabled());

        boolean vectorEnabled = vectorStore != null && vectorStore.isAvailable();
        result.put("vectorEnabled", vectorEnabled);
        result.put("vectorType", vectorEnabled && StringUtils.hasText(vectorType) ? vectorType : null);

        addLocaleInfo(result);
        addTimezoneInfo(result);
        addExportInfo(result);
        addRenderInfo(result);
        addCacheInfo(result);
        addFormatInfo(result);

        result.put("features", new String[]{"pdf", "excel", "word", "html", "chart"});
        return result;
    }

    /**
     * 添加本地化信息。未配置或空串时默认 zh_CN。
     */
    private void addLocaleInfo(Map<String, Object> result) {
        String value = StringUtils.hasText(locale) ? locale.trim() : "zh_CN";
        result.put("locale", value);
    }

    /**
     * 添加时区信息
     */
    private void addTimezoneInfo(Map<String, Object> result) {
        result.put("timezone", "Asia/Shanghai");
    }

    /**
     * 添加导出配置
     */
    private void addExportInfo(Map<String, Object> result) {
        Map<String, Object> export = new LinkedHashMap<>();
        export.put("pdfEnabled", true);
        export.put("excelEnabled", true);
        export.put("wordEnabled", true);
        export.put("maxRows", 100000);
        export.put("maxColumns", 256);
        result.put("export", export);
    }

    /**
     * 从 MANIFEST.MF 读取 Implementation-Version
     *
     * @return 版本号；读取失败时返回 "0.0.0"
     */
    private String readVersion() {
        Package pkg = ResourceLoaderService.class.getPackage();
        String version = (pkg != null) ? pkg.getImplementationVersion() : null;
        return (version != null) ? version : "2.0.8";
    }

    /**
     * 添加渲染配置
     */
    private void addRenderInfo(Map<String, Object> result) {
        Map<String, Object> render = new LinkedHashMap<>();
        render.put("engine", "canvas");
        render.put("dpi", 96);
        render.put("scaleMode", "adaptive");
        result.put("render", render);
    }

    /**
     * 添加缓存配置
     */
    private void addCacheInfo(Map<String, Object> result) {
        Map<String, Object> cache = new LinkedHashMap<>();
        cache.put("enabled", true);
        cache.put("ttl", 3600);
        cache.put("maxSize", 1000);
        result.put("cache", cache);
    }

    /**
     * 添加格式化配置
     */
    private void addFormatInfo(Map<String, Object> result) {
        Map<String, Object> format = new LinkedHashMap<>();
        format.put("date", "yyyy-MM-dd");
        format.put("time", "HH:mm:ss");
        format.put("datetime", "yyyy-MM-dd HH:mm:ss");
        result.put("format", format);
    }
}
