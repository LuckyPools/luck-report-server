package com.luck.report.web.utils;

import com.luck.report.web.common.domain.enums.HttpCodeEnum;
import com.luck.report.web.common.domain.vo.ResultVO;
import com.luck.report.web.i18n.ReportI18n;

/**
 * 多语言响应构造工具。
 */
public  class ResultVOUtils {

    private ResultVOUtils() {
    }

    /**
     * 成功响应，message 按当前请求语言翻译
     */
    public static <T> ResultVO<T> success(String code) {
        return new ResultVO<>(HttpCodeEnum.OK.getCode(), ReportI18n.getMessage(code));
    }

    /**
     * 成功响应，message 按当前请求语言翻译
     */
    public static <T> ResultVO<T> success(String code, T data) {
        return new ResultVO<>(HttpCodeEnum.OK.getCode(), ReportI18n.getMessage(code), data);
    }

    /**
     * 失败响应，message 按当前请求语言翻译，errorCode 原样透出。
     */
    public static <T> ResultVO<T> error(String code, Object... args) {
        ResultVO<T> vo = new ResultVO<>(HttpCodeEnum.UN_KNOW_ERROR.getCode(), ReportI18n.getMessage(code, args));
        vo.setErrorCode(code);
        return vo;
    }
}
