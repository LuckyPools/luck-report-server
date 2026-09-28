package com.luck.report.web.modules.vector.utils;

import com.luck.report.infra.modules.vector.domain.entity.VectorDocument;

/** RRF 融合累加器：同 id 累加分数并保留文档引用。 */
final class RrfAccumulator {

    VectorDocument document;
    double rrfScore;

    RrfAccumulator(VectorDocument document, double rrfScore) {
        this.document = document;
        this.rrfScore = rrfScore;
    }
}
