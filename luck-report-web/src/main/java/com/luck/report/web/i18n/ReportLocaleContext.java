package com.luck.report.web.i18n;

/**
 * 语言上下文（线程隔离）。不使用 Spring 的 LocaleContextHolder，避免与宿主应用冲突。
 * <p>请求结束时必须 {@link #clear()}，否则线程复用会残留上一次请求的语言。
 */
public final class ReportLocaleContext {

    private static final ThreadLocal<ReportLocale> HOLDER = new ThreadLocal<ReportLocale>();

    private ReportLocaleContext() {
    }

    /** 传 null 等效于清除 */
    public static void set(ReportLocale locale) {
        if (locale == null) {
            HOLDER.remove();
            return;
        }
        HOLDER.set(locale);
    }

    /** 未设置时返回 {@link ReportLocale#DEFAULT} */
    public static ReportLocale get() {
        ReportLocale locale = HOLDER.get();
        return locale == null ? ReportLocale.DEFAULT : locale;
    }

    public static void clear() {
        HOLDER.remove();
    }

    /**
     * 包装 Runnable 以传播语言到线程池线程，须在仍持有请求上下文的提交线程上调用。
     * <p>不能用 InheritableThreadLocal 替代：线程池线程只在创建时继承一次，
     * 会导致后续请求复用首次创建线程时的语言，产生跨请求串扰。
     */
    public static Runnable wrap(Runnable task) {
        return wrap(get(), task);
    }

    public static Runnable wrap(ReportLocale locale, Runnable task) {
        if (locale == null || task == null) {
            return task;
        }
        return () -> {
            set(locale);
            try {
                task.run();
            } finally {
                // 线程池线程会被复用，不清理会让下一个任务串到本次请求的语言
                clear();
            }
        };
    }
}
