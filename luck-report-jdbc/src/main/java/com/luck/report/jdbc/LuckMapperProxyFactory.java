package com.luck.report.jdbc;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapper 接口 JDK 代理工厂：方法名映射为 namespace.methodName
 */
public final class LuckMapperProxyFactory {

    private LuckMapperProxyFactory() {
    }

    /**
     * 为 Mapper 接口创建代理实例
     *
     * @param mapperInterface Mapper 接口
     * @param executor        SQL 执行器
     * @param <T>             接口类型
     * @return 代理对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> mapperInterface, LuckSqlExecutor executor) {
        LuckMapperInvocationHandler handler =
                new LuckMapperInvocationHandler(mapperInterface.getName(), executor);
        return (T) Proxy.newProxyInstance(
                mapperInterface.getClassLoader(),
                new Class<?>[]{mapperInterface},
                handler);
    }

    /**
     * 从方法参数构建 SQL 参数 Map
     *
     * @param method 方法
     * @param args   实参
     * @return 参数 Map
     */
    public static Map<String, Object> buildParams(Method method, Object[] args) {
        Map<String, Object> params = new HashMap<String, Object>();
        if (args == null || args.length == 0) {
            return params;
        }
        Parameter[] parameters = method.getParameters();
        boolean anyParamAnno = false;
        for (Parameter parameter : parameters) {
            if (parameter.getAnnotation(Param.class) != null
                    || readMyBatisParamValue(parameter) != null) {
                anyParamAnno = true;
                break;
            }
        }
        if (!anyParamAnno && args.length == 1) {
            Object arg = args[0];
            Class<?> paramType = parameters[0].getType();
            // 简单类型 / 集合：按参数名绑定，禁止把 String 等误拆成 Bean 属性
            if (arg == null || isSimpleType(paramType) || isCollectionOrArray(arg)) {
                bindSingleNamedParam(params, parameters[0], 0, arg);
                return params;
            }
            flattenBean(arg, params);
            return params;
        }
        for (int i = 0; i < parameters.length; i++) {
            params.put(resolveParamName(parameters[i], i), args[i]);
        }
        return params;
    }

    private static void bindSingleNamedParam(Map<String, Object> params, Parameter parameter,
                                            int index, Object arg) {
        String name = resolveParamName(parameter, index);
        params.put(name, arg);
        params.put("param1", arg);
        if (isCollectionOrArray(arg)) {
            params.put("list", arg);
            params.put("collection", arg);
        }
    }

    private static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive()
                || CharSequence.class.isAssignableFrom(type)
                || Number.class.isAssignableFrom(type)
                || Boolean.class.isAssignableFrom(type)
                || Character.class.isAssignableFrom(type)
                || java.util.Date.class.isAssignableFrom(type)
                || java.time.temporal.Temporal.class.isAssignableFrom(type)
                || type.isEnum();
    }

    private static boolean isCollectionOrArray(Object arg) {
        return arg instanceof Collection || (arg != null && arg.getClass().isArray());
    }

    private static String resolveParamName(Parameter parameter, int index) {
        Param param = parameter.getAnnotation(Param.class);
        if (param != null) {
            return param.value();
        }
        String mybatis = readMyBatisParamValue(parameter);
        if (mybatis != null) {
            return mybatis;
        }
        if (parameter.isNamePresent()) {
            return parameter.getName();
        }
        return "param" + (index + 1);
    }

    private static String readMyBatisParamValue(Parameter parameter) {
        for (java.lang.annotation.Annotation annotation : parameter.getAnnotations()) {
            if ("org.apache.ibatis.annotations.Param".equals(annotation.annotationType().getName())) {
                try {
                    Method value = annotation.annotationType().getMethod("value");
                    return (String) value.invoke(annotation);
                } catch (Exception ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private static void flattenBean(Object bean, Map<String, Object> params) {
        if (bean == null) {
            return;
        }
        if (bean instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) bean;
            params.putAll(map);
            return;
        }
        if (bean instanceof Collection || bean.getClass().isArray()) {
            params.put("list", bean);
            params.put("collection", bean);
            return;
        }
        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(bean);
        for (java.beans.PropertyDescriptor pd : wrapper.getPropertyDescriptors()) {
            String name = pd.getName();
            if ("class".equals(name) || !wrapper.isReadableProperty(name)) {
                continue;
            }
            params.put(name, wrapper.getPropertyValue(name));
        }
    }

    static Class<?> resolveListElementType(Method method) {
        Type generic = method.getGenericReturnType();
        if (generic instanceof ParameterizedType) {
            Type arg = ((ParameterizedType) generic).getActualTypeArguments()[0];
            if (arg instanceof Class) {
                return (Class<?>) arg;
            }
        }
        return Map.class;
    }
}
