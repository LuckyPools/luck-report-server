package com.luck.report.web.modules.knowledge.mapper;

import com.luck.report.web.modules.knowledge.domain.dto.BusinessKnowledgeQueryDTO;
import com.luck.report.web.modules.knowledge.domain.entity.BusinessKnowledge;
import com.luck.report.jdbc.Param;

import java.util.List;

/**
 * 业务知识Mapper，操作 luck_business_knowledge
 *
 * @author luck
 */
public interface BusinessKnowledgeMapper {

    int insert(BusinessKnowledge knowledge);

    int update(BusinessKnowledge knowledge);

    BusinessKnowledge selectById(@Param("id") String id);

    List<BusinessKnowledge> selectByConditionsWithPage(@Param("queryDTO") BusinessKnowledgeQueryDTO queryDTO,
                                                       @Param("offset") Integer offset,
                                                       @Param("pageSize") Integer pageSize);

    Long countByConditions(@Param("queryDTO") BusinessKnowledgeQueryDTO queryDTO);

    List<String> selectEnabledKnowledgeIds();

    List<BusinessKnowledge> selectByIds(@Param("ids") List<String> ids);
}
