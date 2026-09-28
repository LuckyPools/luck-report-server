package com.luck.report.jdbc;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Mapper 参数绑定单测
 */
public class LuckMapperProxyFactoryTest {

    public interface DemoMapper {
        Object findById(@Param("id") String id);

        Object findByIdNoAnno(String id);

        Object insert(DemoEntity entity);
    }

    public static class DemoEntity {
        private String id;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void shouldBindAnnotatedSimpleParamAsId() throws Exception {
        Method method = DemoMapper.class.getMethod("findById", String.class);
        Map<String, Object> params = LuckMapperProxyFactory.buildParams(method, new Object[]{"abc"});
        assertEquals("abc", params.get("id"));
    }

    @Test
    public void shouldNotFlattenStringWithoutParamAnnotation() throws Exception {
        Method method = DemoMapper.class.getMethod("findByIdNoAnno", String.class);
        Map<String, Object> params = LuckMapperProxyFactory.buildParams(method, new Object[]{"abc"});
        assertFalse(params.containsKey("empty"));
        assertFalse(params.containsKey("bytes"));
        // 有 -parameters 时为 id；否则至少有 param1
        Object value = params.containsKey("id") ? params.get("id") : params.get("param1");
        assertEquals("abc", value);
    }

    @Test
    public void shouldFlattenEntityWithoutParamAnnotation() throws Exception {
        Method method = DemoMapper.class.getMethod("insert", DemoEntity.class);
        DemoEntity entity = new DemoEntity();
        entity.setId("1");
        entity.setName("n");
        Map<String, Object> params = LuckMapperProxyFactory.buildParams(method, new Object[]{entity});
        assertEquals("1", params.get("id"));
        assertEquals("n", params.get("name"));
    }
}
