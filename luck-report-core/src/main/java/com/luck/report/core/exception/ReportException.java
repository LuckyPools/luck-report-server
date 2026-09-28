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
package com.luck.report.core.exception;

/**
 * @author Jacky.gao
 * @since 2016年11月1日
 */
public class ReportException extends RuntimeException implements ErrorCodeAware {
    private static final long serialVersionUID = 1L;

    private final String errorCode;

    private final Object[] errorArgs;

    public ReportException(String msg) {
        super(msg);
        this.errorCode = null;
        this.errorArgs = null;
    }

    public ReportException(Exception ex) {
        super(ex);
        ex.printStackTrace();
        this.errorCode = null;
        this.errorArgs = null;
    }

    /**
     * 携带文案编码的构造，errorCode 同时作为 message，由出口按请求语言翻译。
     * <p>无参调用会命中 {@link #ReportException(String)}，此时出口退化为把 message 当编码翻译。
     */
    public ReportException(String errorCode, Object... errorArgs) {
        super(errorCode);
        this.errorCode = errorCode;
        this.errorArgs = errorArgs;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public Object[] getErrorArgs() {
        return errorArgs;
    }
}
