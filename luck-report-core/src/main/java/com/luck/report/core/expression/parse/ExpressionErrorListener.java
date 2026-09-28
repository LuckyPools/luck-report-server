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
package com.luck.report.core.expression.parse;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 收集表达式词法/语法错误，不直接拼接给用户看的 ANTLR 原文。
 */
public class ExpressionErrorListener extends BaseErrorListener {

    private final List<ExpressionSyntaxError> errors = new ArrayList<ExpressionSyntaxError>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                            int line, int charPositionInLine,
                            String msg, RecognitionException e) {
        String tokenText = null;
        if (offendingSymbol instanceof Token) {
            tokenText = ((Token) offendingSymbol).getText();
        }
        errors.add(new ExpressionSyntaxError(line, charPositionInLine, tokenText, msg));
    }

    /**
     * 是否已收集到语法错误
     *
     * @return true 表示至少有一条错误
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * 返回已收集的语法错误
     *
     * @return 不可变列表；无错误时为空列表
     */
    public List<ExpressionSyntaxError> getErrors() {
        return Collections.unmodifiableList(errors);
    }
}
