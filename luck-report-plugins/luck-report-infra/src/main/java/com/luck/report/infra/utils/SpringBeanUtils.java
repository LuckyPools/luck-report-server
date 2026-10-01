package com.luck.report.infra.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Spring Bean 静态访问工具。
 *
 * @author luck
 * @Date 2024/7/18
 */
@Component("bean.springBeanUtils")
public class SpringBeanUtils implements ApplicationContextAware, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(SpringBeanUtils.class);

    private static ApplicationContext applicationContext;

    @Override
    public  void setApplicationContext(ApplicationContext applicationContext){
        SpringBeanUtils.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext(){
        return applicationContext;
    }

    public static <T> T getBean(String name) {
        return (T) applicationContext.getBean(name);
    }

    public static <T> T getBean(Class<T> requiredType) {
        return applicationContext.getBean(requiredType);
    }

    public static <T> List<T> getBeans(Class<T> requiredType) {
        List<T> beanList = new ArrayList<>();
        Map<String, T> beanMap = applicationContext.getBeansOfType(requiredType);
        Set<Map.Entry<String, T>> set = beanMap.entrySet();
        Iterator<Map.Entry<String, T>> it = set.iterator();
        Map<String, T> data = new HashMap<>();
        while (it.hasNext()) {
            Map.Entry<String, T> entry = it.next();
            beanList.add(entry.getValue());
        }
        return beanList;
    }

    @Override
    public void destroy() throws Exception {
        log.debug("清除SpringContextHolder中的ApplicationContext:" + applicationContext);
        applicationContext = null;
    }
}
