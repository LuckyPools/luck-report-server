package com.luck.report.web.handler;

import com.luck.report.core.exception.ErrorCodeAware;
import com.luck.report.web.common.domain.vo.ResultVO;
import com.luck.report.web.i18n.ReportI18n;
import com.luck.report.web.i18n.ReportLocale;
import com.luck.report.web.i18n.ReportLocaleContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 全局异常处理器
 *
 * @author luck
 */
@ControllerAdvice(basePackages = "com.luck.report")
public class ReportExceptionHandler {

    /**
     * 未取到异常信息时使用的文案编码
     */
    private static final String UNKNOWN_ERROR_CODE = "error.unknown";

    /**
     * 防止异常自引用造成死循环
     */
    private static final int MAX_CAUSE_DEPTH = 10;

    private static final Logger logger = LoggerFactory.getLogger(ReportExceptionHandler.class);
    private static final Random random = new Random();

    /**
     * 处理 RuntimeException 及其子类异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public ResultVO<Object> handleException(RuntimeException ex) {
        ReportLocale locale = ReportLocaleContext.get();
        String auxCode = generateAuxCode();

        ErrorCodeAware aware = findErrorCodeAware(ex);
        String code = aware == null ? null : aware.getErrorCode();
        Object[] args = aware == null ? null : aware.getErrorArgs();
        if (StringUtils.isBlank(code)) {
            code = getRootErrorMessage(ex);
        }
        if (StringUtils.isBlank(code)) {
            code = UNKNOWN_ERROR_CODE;
        }

        String errorMessage = ReportI18n.getMessage(locale, code, args);
        logger.error("Report Exception [auxCode={}][lang={}]: {}", auxCode, locale.getCode(),
                ReportI18n.getMessage(ReportLocale.DEFAULT, code, args), ex);

        Map<String, Object> data = new HashMap<>();
        data.put("auxCode", auxCode);
        return ResultVO.error(500, errorMessage).setData(data);
    }

    private ErrorCodeAware findErrorCodeAware(Throwable throwable) {
        Throwable current = throwable;
        for (int depth = 0; current != null && depth < MAX_CAUSE_DEPTH; depth++) {
            if (current instanceof ErrorCodeAware) {
                ErrorCodeAware aware = (ErrorCodeAware) current;
                if (StringUtils.isNotBlank(aware.getErrorCode())) {
                    return aware;
                }
            }
            current = current.getCause();
        }
        return null;
    }

    /**
     * 生成10位辅助编码
     *
     * @return 10位辅助编码
     */
    private String generateAuxCode() {
        long timestamp = System.currentTimeMillis();
        String timestampPart = String.valueOf(timestamp % 1000000);
        while (timestampPart.length() < 6) {
            timestampPart = "0" + timestampPart;
        }
        int randomNum = random.nextInt(10000);
        String randomPart = String.format("%04d", randomNum);
        return timestampPart + randomPart;
    }

    /**
     * 获取根异常信息
     *
     * @param throwable 异常对象
     * @return 根异常的错误信息
     */
    private String getRootErrorMessage(Throwable throwable) {
        Throwable root = throwable;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root.getMessage();
    }
}
