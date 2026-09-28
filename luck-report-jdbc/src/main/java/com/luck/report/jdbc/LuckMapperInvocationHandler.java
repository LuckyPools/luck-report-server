package com.luck.report.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Mapper 方法调用处理器
 */
public class LuckMapperInvocationHandler implements InvocationHandler {

    private final String namespace;
    private final LuckSqlExecutor executor;

    /**
     * 构造调用处理器
     *
     * @param namespace Mapper 接口全名
     * @param executor SQL 执行器
     */
    public LuckMapperInvocationHandler(String namespace, LuckSqlExecutor executor) {
        this.namespace = namespace;
        this.executor = executor;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }
        String statementId = namespace + "." + method.getName();
        Map<String, Object> params = LuckMapperProxyFactory.buildParams(method, args);
        Class<?> returnType = method.getReturnType();
        SqlStatement statement = executor.getStatement(statementId);

        if (void.class.equals(returnType)) {
            executor.update(statementId, params);
            return null;
        }
        if (List.class.isAssignableFrom(returnType)) {
            Class<?> elemType = LuckMapperProxyFactory.resolveListElementType(method);
            return executor.selectList(statementId, params, elemType);
        }
        if (returnType == int.class || returnType == Integer.class
                || returnType == long.class || returnType == Long.class) {
            if (statement.getType() == SqlStatement.StatementType.SELECT) {
                Number number = executor.selectScalar(statementId, params, Number.class);
                if (number == null) {
                    return returnType == long.class || returnType == Long.class ? Long.valueOf(0L) : Integer.valueOf(0);
                }
                if (returnType == long.class || returnType == Long.class) {
                    return Long.valueOf(number.longValue());
                }
                return Integer.valueOf(number.intValue());
            }
            int updated = executor.update(statementId, params);
            if (returnType == long.class || returnType == Long.class) {
                return Long.valueOf(updated);
            }
            return Integer.valueOf(updated);
        }
        return executor.selectOne(statementId, params, returnType);
    }
}
