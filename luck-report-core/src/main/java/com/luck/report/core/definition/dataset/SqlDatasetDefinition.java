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
package com.luck.report.core.definition.dataset;

import com.luck.report.core.Utils;
import com.luck.report.core.build.Context;
import com.luck.report.core.build.Dataset;
import com.luck.report.core.definition.datasource.DataType;
import com.luck.report.core.expression.ExpressionUtils;
import com.luck.report.core.expression.model.Expression;
import com.luck.report.core.expression.model.data.ExpressionData;
import com.luck.report.core.expression.model.data.ObjectExpressionData;
import com.luck.report.core.expression.utils.ExpressionReturns;
import com.luck.report.core.utils.ProcedureUtils;
import com.luck.report.core.utils.SqlParamUtils;
import com.luck.report.core.utils.SqlSecurityUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author Jacky.gao
 * @since 2016年12月27日
 */
public class SqlDatasetDefinition implements DatasetDefinition {
    private static final long serialVersionUID = 1L;
    private String name;
    private String sql;
    private List<Parameter> parameters;
    private List<Field> fields;
    private Expression sqlExpression;

    /**
     * 默认无参构造器
     */
    public SqlDatasetDefinition() {}

    public Dataset buildDataset(Map<String, Object> parameterMap, Connection conn) {
        String sqlForUse = sql;
        sqlForUse = SqlParamUtils.convertToNamedParam(sqlForUse);
        Map<String, Object> pmap = buildParameters(parameterMap);
        // 补充 SQL 中引用但未在 Parameter[] 中定义的参数（如内置参数 luck_user_id）
        supplementSqlParams(pmap, parameterMap, sql);
        Context context = new Context(null, pmap);
        if (sqlExpression != null) {
            sqlForUse = executeSqlExpr(sqlExpression, context);
        } else {
            Pattern pattern = Pattern.compile("\\$\\{.*?\\}");
            Matcher matcher = pattern.matcher(sqlForUse);
            while (matcher.find()) {
                String substr = matcher.group();
                String sqlExpr = substr.substring(2, substr.length() - 1);
                Expression expr = ExpressionUtils.parseExpression(sqlExpr);
                String result = executeSqlExpr(expr, context);
                sqlForUse = sqlForUse.replace(substr, result);
            }
        }
        SqlSecurityUtils.validate(sqlForUse);
        long start = System.currentTimeMillis();
        Utils.logToConsole("START [" + start + "] RUNTIME SQL:" + sqlForUse);
        List<Map<String, Object>> list;
        if (ProcedureUtils.isProcedure(sqlForUse)) {
            list = ProcedureUtils.procedureQuery(sqlForUse, pmap, conn);
        } else {
            SingleConnectionDataSource datasource = new SingleConnectionDataSource(conn, false);
            NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(datasource);
            list = jdbcTemplate.queryForList(sqlForUse, pmap);
        }
        long end = System.currentTimeMillis();
        String msg = "END   [" + start + "] SQL EXECUTION TIME:" + (end - start) + "ms";
        Utils.logToConsole(msg);
        return new Dataset(name, list);
    }

    private String executeSqlExpr(Expression sqlExpr, Context context) {
        String sqlForUse = null;
        ExpressionData<?> exprData = sqlExpr.execute(null, null, context);
        exprData = ExpressionReturns.unwrap(exprData);
        if (exprData instanceof ObjectExpressionData) {
            ObjectExpressionData data = (ObjectExpressionData) exprData;
            Object obj = data.getData();
            if (obj != null) {
                String s = obj.toString();
                s = s.replaceAll("\\\\", "");
                sqlForUse = s;
            }
        }
        return sqlForUse;
    }


    private Map<String, Object> buildParameters(Map<String, Object> params) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (Parameter param : parameters) {
            String name = param.getName();
            DataType datatype = param.getType();
            Object value = param.getDefaultValue();
            if (params != null && params.containsKey(name)) {
                value = params.get(name);
            }
            map.put(name, datatype.parse(value));
        }
        return map;
    }

    /**
     * 从原始参数 Map 中补充 SQL 引用但未在 Parameter[] 中定义的参数
     * <p>内置参数（如 luck_user_id）不在报表 Parameter[] 定义中，
     * 但已由 web 层注入到 parameterMap 中，此处将其补充到 pmap，
     * 保证 NamedParameterJdbcTemplate 执行时参数齐全。
     *
     * @param pmap 已构建的参数 Map（来自 Parameter[] 定义），不可为空
     * @param sourceMap 原始参数 Map（含内置参数），可为空
     * @param originalSql 原始 SQL（#{xxx} 格式），可为空
     */
    private void supplementSqlParams(Map<String, Object> pmap, Map<String, Object> sourceMap, String originalSql) {
        if (sourceMap == null || originalSql == null) {
            return;
        }
        Set<String> sqlParamNames = new HashSet<>();
        SqlParamUtils.extractParamNames(originalSql, sqlParamNames);
        for (String name : sqlParamNames) {
            if (!pmap.containsKey(name) && sourceMap.containsKey(name)) {
                pmap.put(name, sourceMap.get(name));
            }
        }
    }

    @Override
    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public void setSqlExpression(Expression sqlExpression) {
        this.sqlExpression = sqlExpression;
    }

    public Expression getSqlExpression() {
        return sqlExpression;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
