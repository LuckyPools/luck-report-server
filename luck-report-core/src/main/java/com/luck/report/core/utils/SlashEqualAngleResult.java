package com.luck.report.core.utils;

import java.util.Collections;
import java.util.List;

/**
 * 均分 90° 斜表头布局结果。
 */
public class SlashEqualAngleResult {
    private final List<SlashLineLayout> lines;
    private final List<SlashLabelLayout> labels;

    public SlashEqualAngleResult(List<SlashLineLayout> lines, List<SlashLabelLayout> labels) {
        this.lines = lines == null ? Collections.emptyList() : lines;
        this.labels = labels == null ? Collections.emptyList() : labels;
    }

    public List<SlashLineLayout> getLines() {
        return lines;
    }

    public List<SlashLabelLayout> getLabels() {
        return labels;
    }
}
