package com.luck.report.web.modules.knowledge.handler.splitter;

import java.util.regex.Pattern;

/**
 * FastGPT commonSplit stepReges 单步规则。
 */
class StructureSplitStep {

    final Pattern pattern;
    final String literal;
    final int maxLen;
    final boolean splitAround;

    StructureSplitStep(Pattern pattern, int maxLen, boolean splitAround) {
        this.pattern = pattern;
        this.literal = null;
        this.maxLen = maxLen;
        this.splitAround = splitAround;
    }

    StructureSplitStep(String literal, int maxLen) {
        this.pattern = null;
        this.literal = literal;
        this.maxLen = maxLen;
        this.splitAround = false;
    }

    boolean isLiteral() {
        return literal != null;
    }
}
