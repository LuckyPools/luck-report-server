package com.luck.report.web.modules.knowledge.handler.splitter;

/**
 * FastGPT getSplitTexts 产出的单段。
 */
class StructureSplitPart {

    final String text;
    final String title;
    final int chunkMaxSize;

    StructureSplitPart(String text, String title, int chunkMaxSize) {
        this.text = text;
        this.title = title;
        this.chunkMaxSize = chunkMaxSize;
    }
}
