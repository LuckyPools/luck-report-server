package com.luck.report.infra.modules.cache.service;

/**
 * 缓存键隔离策略。
 *
 * @author 24731
 */
public interface ReportCacheKeyResolver {

    boolean disabled();

    String getPrefix();

}
