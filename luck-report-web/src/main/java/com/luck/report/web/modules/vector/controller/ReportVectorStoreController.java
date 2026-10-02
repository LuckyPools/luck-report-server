package com.luck.report.web.modules.vector.controller;

import com.luck.report.infra.modules.vector.domain.entity.VectorDocument;
import com.luck.report.web.common.domain.vo.ResultVO;
import com.luck.report.web.modules.chat.domain.vo.ComponentDocAddRequest;
import com.luck.report.web.modules.vector.domain.vo.VectorAddRequest;
import com.luck.report.web.modules.vector.domain.vo.VectorSearchRequest;
import com.luck.report.web.modules.vector.domain.vo.VectorSearchResult;
import com.luck.report.web.modules.vector.service.ReportVectorSearchService;
import com.luck.report.web.modules.vector.service.impl.AgentVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 向量库 HTTP 入口；检索业务在 {@link ReportVectorSearchService}。
 */
@RestController("bean.reportVectorStoreController")
@RequestMapping("${luck-report.servletPrefix:}/vector")
public class ReportVectorStoreController {

    @Autowired
    @Qualifier("bean.agentVectorStore")
    private AgentVectorStore reportAgentVectorStore;

    @Autowired
    @Qualifier("bean.reportVectorSearchService")
    private ReportVectorSearchService reportVectorSearchService;

    @PostMapping("/search")
    public ResultVO<List<VectorSearchResult>> search(@RequestBody VectorSearchRequest request) {
        if (request.getQuery() == null || request.getQuery().isEmpty()) {
            return ResultVO.error(400, "Query text cannot be empty");
        }
        if (request.getVectorType() == null || request.getVectorType().isEmpty()) {
            return ResultVO.error(400, "Knowledge type cannot be empty");
        }
        return ResultVO.success(reportVectorSearchService.search(request));
    }

    @PostMapping("/create")
    public ResultVO<Boolean> addDocument(@RequestBody VectorAddRequest request) {
        if (request.getContent() == null || request.getContent().isEmpty()) {
            return ResultVO.error(400, "Document content cannot be empty");
        }
        if (request.getVectorType() == null || request.getVectorType().isEmpty()) {
            return ResultVO.error(400, "Knowledge type cannot be empty");
        }

        Map<String, Object> metadata = request.getMetadata() != null
                ? new HashMap<>(request.getMetadata())
                : new HashMap<>();
        metadata.put("vectorType", request.getVectorType());

        VectorDocument doc = new VectorDocument(request.getContent(), metadata);
        reportAgentVectorStore.addDocuments(Collections.singletonList(doc));

        return ResultVO.success(true);
    }

    @PostMapping("/batch/create")
    public ResultVO<Boolean> addDocuments(@RequestBody List<VectorAddRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return ResultVO.error(400, "Document list cannot be empty");
        }

        List<VectorDocument> documents = new ArrayList<>();
        for (VectorAddRequest request : requests) {
            if (request.getContent() == null || request.getContent().isEmpty()
                    || request.getVectorType() == null || request.getVectorType().isEmpty()) {
                continue;
            }
            Map<String, Object> metadata = request.getMetadata() != null
                    ? new HashMap<>(request.getMetadata())
                    : new HashMap<>();
            metadata.put("vectorType", request.getVectorType());
            documents.add(new VectorDocument(request.getContent(), metadata));
        }

        if (!documents.isEmpty()) {
            reportAgentVectorStore.addDocuments(documents);
        }

        return ResultVO.success(true);
    }

    @DeleteMapping("/delete/{vectorType}")
    public ResultVO<Boolean> deleteByVectorType(@PathVariable String vectorType) {
        boolean result = reportAgentVectorStore.deleteByVectorType(vectorType);
        return ResultVO.success(result);
    }

    @PostMapping("/create_component_doc")
    public ResultVO<Boolean> addComponentDoc(@RequestBody ComponentDocAddRequest request) {
        if (request.getName() == null || request.getName().isEmpty()) {
            return ResultVO.error(400, "Component name cannot be empty");
        }
        if (request.getDescription() == null || request.getDescription().isEmpty()) {
            return ResultVO.error(400, "Component description cannot be empty");
        }
        if (request.getComponentType() == null || request.getComponentType().isEmpty()) {
            return ResultVO.error(400, "Component type cannot be empty");
        }

        reportAgentVectorStore.addComponentDoc(
                request.getName(),
                request.getDescription(),
                request.getComponentType(),
                request.getExtraMetadata()
        );

        return ResultVO.success(true);
    }
}
