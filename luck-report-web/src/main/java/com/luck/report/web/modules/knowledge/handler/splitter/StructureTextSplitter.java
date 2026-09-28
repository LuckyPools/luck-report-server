package com.luck.report.web.modules.knowledge.handler.splitter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FastGPT splitText2Chunks / commonSplit 移植（仅 char 模式，不含 token）。
 */
public class StructureTextSplitter implements TextSplitter {

    private static final String CUSTOM_SPLIT_SIGN = "-----CUSTOM_SPLIT_SIGN-----";
    private static final String SPLIT_MARKER = "SPLIT_HERE_SPLIT_HERE";
    private static final String CODE_BLOCK_LINE_MARKER = "CODE_BLOCK_LINE_MARKER";
    private static final int DEFAULT_MAX_CHUNK_SIZE = 8000;
    private static final int PARAGRAPH_CHUNK_DEEP = 5;
    private static final int PARAGRAPH_CHUNK_MIN_SIZE = 100;

    private static final Pattern CODE_FENCE_BLOCK =
            Pattern.compile("(```[\\s\\S]*?```|~~~[\\s\\S]*?~~~)");
    private static final Pattern MULTI_NEWLINE = Pattern.compile("(\\r?\\n|\\r){3,}");
    private static final Pattern CODE_BLOCK_WHOLE =
            Pattern.compile("^(```[\\s\\S]*```|~~~[\\s\\S]*~~~)$");

    private final int chunkSize;
    private final double overlapRatio;
    private final int maxSize;
    private final int paragraphChunkDeep;
    private final int paragraphChunkMinSize;
    private final List<String> customReg;

    private List<StructureSplitStep> stepReges;
    private int customRegLen;
    private int markdownIndex;
    private int forbidOverlapIndex;
    private int overlapLen;

    public StructureTextSplitter(int chunkSize, int overlapSize) {
        if (!Double.isFinite(chunkSize) || chunkSize <= 0) {
            throw new IllegalArgumentException("Chunk size must be a positive finite number");
        }
        this.chunkSize = chunkSize;
        double ratio = overlapSize / (double) chunkSize;
        if (ratio < 0) {
            ratio = 0;
        }
        if (ratio >= 1) {
            ratio = Math.nextDown(1.0);
        }
        this.overlapRatio = ratio;
        this.maxSize = DEFAULT_MAX_CHUNK_SIZE;
        this.paragraphChunkDeep = PARAGRAPH_CHUNK_DEEP;
        this.paragraphChunkMinSize = PARAGRAPH_CHUNK_MIN_SIZE;
        this.customReg = new ArrayList<String>();
    }

    @Override
    public List<String> split(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<String>();
        }
        String[] parts = text.split(Pattern.quote(CUSTOM_SPLIT_SIGN), -1);
        List<String> chunks = new ArrayList<String>();
        for (String item : parts) {
            if (TableTextSplitter.strIsMdTable(item)) {
                chunks.addAll(new TableTextSplitter(chunkSize, maxSize).markdownTableSplit(item));
            } else {
                chunks.addAll(commonSplit(item));
            }
        }
        return chunks;
    }

    /** FastGPT commonSplit（char） */
    List<String> commonSplit(String rawText) {
        if (!Double.isFinite(overlapRatio) || overlapRatio < 0 || overlapRatio >= 1) {
            throw new IllegalArgumentException(
                    "Overlap ratio must be greater than or equal to 0 and less than 1");
        }

        String text = rawText == null ? "" : rawText;
        overlapLen = (int) Math.round(chunkSize * overlapRatio);
        int maxCodeBlockChunks = 4;
        int codeBlockMaxLen = Math.min(maxSize, chunkSize * maxCodeBlockChunks);

        text = replaceCodeBlockNewlines(text);
        text = MULTI_NEWLINE.matcher(text).replaceAll("\n\n\n");

        customRegLen = customReg.size();
        markdownIndex = paragraphChunkDeep - 1;
        forbidOverlapIndex = customRegLen + markdownIndex + 4;
        stepReges = buildStepReges(codeBlockMaxLen);

        List<String> chunks =
                splitTextRecursively(text, 0, "", "");
        List<String> restored = new ArrayList<String>(chunks.size());
        for (String chunk : chunks) {
            if (chunk == null) {
                restored.add("");
                continue;
            }
            restored.add(chunk.replace(CODE_BLOCK_LINE_MARKER, "\n").trim());
        }
        return restored;
    }

    private List<StructureSplitStep> buildStepReges(int codeBlockMaxLen) {
        List<StructureSplitStep> steps = new ArrayList<StructureSplitStep>();
        for (String reg : customReg) {
            steps.add(new StructureSplitStep(reg.replace("\\n", "\n"), maxSize));
        }
        int maxDeep = Math.min(paragraphChunkDeep, 8);
        for (int i = 1; i <= maxDeep; i++) {
            String hashes = repeatChar('#', i);
            Pattern reg =
                    Pattern.compile(
                            "^(" + Pattern.quote(hashes) + "\\s[^\\n]+\\n)", Pattern.MULTILINE);
            steps.add(new StructureSplitStep(reg, chunkSize, false));
        }
        steps.add(
                new StructureSplitStep(
                        Pattern.compile("(^|\\n)(```[\\s\\S]*?```|~~~[\\s\\S]*?~~~)"),
                        codeBlockMaxLen,
                        true));
        steps.add(
                new StructureSplitStep(
                        Pattern.compile(
                                "(\\n\\|(?:[^\\n|]*\\|)+\\n\\|(?:[:\\-\\s]*\\|)+\\n(?:\\|(?:[^\\n|]*\\|)*\\n)*)"),
                        chunkSize,
                        false));
        steps.add(new StructureSplitStep(Pattern.compile("(\\n{2,})"), chunkSize, false));
        steps.add(new StructureSplitStep(Pattern.compile("([\\n])"), chunkSize, false));
        steps.add(
                new StructureSplitStep(
                        Pattern.compile("([。]|([a-zA-Z])\\.\\s)"), chunkSize, false));
        steps.add(new StructureSplitStep(Pattern.compile("([！]|!\\s)"), chunkSize, false));
        steps.add(new StructureSplitStep(Pattern.compile("([？]|\\?\\s)"), chunkSize, false));
        steps.add(new StructureSplitStep(Pattern.compile("([；]|;\\s)"), chunkSize, false));
        steps.add(new StructureSplitStep(Pattern.compile("([，]|,\\s)"), chunkSize, false));
        return steps;
    }

    private static String replaceCodeBlockNewlines(String text) {
        Matcher matcher = CODE_FENCE_BLOCK.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String replaced = matcher.group().replace("\n", CODE_BLOCK_LINE_MARKER);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replaced));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private boolean checkIsCustomStep(int step) {
        return step < customRegLen;
    }

    private boolean checkIsMarkdownSplit(int step) {
        return step >= customRegLen && step <= markdownIndex + customRegLen;
    }

    private boolean checkForbidOverlap(int step) {
        return step <= forbidOverlapIndex;
    }

    private List<StructureSplitPart> getSplitTexts(String text, int step) {
        if (step >= stepReges.size()) {
            List<StructureSplitPart> one = new ArrayList<StructureSplitPart>();
            one.add(new StructureSplitPart(text, "", chunkSize));
            return one;
        }

        boolean isCustomStep = checkIsCustomStep(step);
        boolean isMarkdownSplit = checkIsMarkdownSplit(step);
        StructureSplitStep rule = stepReges.get(step);

        String replaceText;
        if (rule.isLiteral()) {
            replaceText = text;
            String[] items = rule.literal.split("\\|");
            for (String itemReg : items) {
                String replacement;
                if (isCustomStep) {
                    replacement = SPLIT_MARKER;
                } else if (isMarkdownSplit) {
                    replacement = SPLIT_MARKER + itemReg;
                } else {
                    replacement = itemReg + SPLIT_MARKER;
                }
                replaceText = replaceLiteral(replaceText, itemReg, replacement);
            }
        } else {
            Matcher matcher = rule.pattern.matcher(text);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String replacement;
                if (isCustomStep) {
                    replacement = SPLIT_MARKER;
                } else if (isMarkdownSplit) {
                    replacement = SPLIT_MARKER + matcher.group(1);
                } else if (rule.splitAround) {
                    replacement = SPLIT_MARKER + matcher.group() + SPLIT_MARKER;
                } else {
                    replacement = matcher.group(1) + SPLIT_MARKER;
                }
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(sb);
            replaceText = sb.toString();
        }

        String[] splitTexts = replaceText.split(Pattern.quote(SPLIT_MARKER), -1);
        List<StructureSplitPart> result = new ArrayList<StructureSplitPart>();
        for (String part : splitTexts) {
            if (part.trim().isEmpty()) {
                continue;
            }
            String matchTitle = "";
            if (isMarkdownSplit) {
                Matcher titleMatcher = rule.pattern.matcher(part);
                if (titleMatcher.find()) {
                    matchTitle = titleMatcher.group(0);
                }
            }
            int chunkMaxSize;
            if (isCustomStep) {
                chunkMaxSize = rule.maxLen;
            } else {
                Matcher sizeMatcher = rule.pattern.matcher(part);
                chunkMaxSize = sizeMatcher.find() ? rule.maxLen : chunkSize;
            }
            String body = isMarkdownSplit ? part.replace(matchTitle, "") : part;
            if (matchTitle.isEmpty() && (body == null || body.trim().isEmpty())) {
                continue;
            }
            result.add(new StructureSplitPart(body, matchTitle, chunkMaxSize));
        }
        return result;
    }

    private String getOneTextOverlapText(String text, int step) {
        if (checkForbidOverlap(step) || overlapLen == 0 || step >= stepReges.size()) {
            return "";
        }
        double maxOverlapLen = chunkSize * 0.4;
        List<StructureSplitPart> splitTexts = getSplitTexts(text, step);
        String overlayText = "";
        for (int i = splitTexts.size() - 1; i >= 0; i--) {
            String currentText = splitTexts.get(i).text;
            String newText = currentText + overlayText;
            int newTextLen = newText.length();
            if (newTextLen > overlapLen) {
                if (newTextLen > maxOverlapLen) {
                    String deeper = getOneTextOverlapText(newText, step + 1);
                    return deeper.isEmpty() ? overlayText : deeper;
                }
                return newText;
            }
            overlayText = newText;
        }
        return overlayText;
    }

    private List<String> splitTextRecursively(
            String text, int step, String lastText, String parentTitle) {
        boolean isMarkdownStep = checkIsMarkdownSplit(step);
        boolean isCustomStep = checkIsCustomStep(step);
        boolean forbidConcat = isCustomStep;

        if (step >= stepReges.size()) {
            String combinedText = lastText + text;
            if (combinedText.length() < maxSize) {
                List<String> one = new ArrayList<String>();
                one.add(combinedText);
                return one;
            }
            return splitTextByCharLengthLimit(
                    combinedText, chunkSize, chunkSize - overlapLen);
        }

        List<StructureSplitPart> splitTexts = getSplitTexts(text, step);
        List<String> chunks = new ArrayList<String>();
        String mutableLast = lastText;

        for (int i = 0; i < splitTexts.size(); i++) {
            StructureSplitPart item = splitTexts.get(i);
            int maxLen = item.chunkMaxSize;
            int lastTextLen = mutableLast.length();
            String currentText = item.text;
            String newText = mutableLast + currentText;
            int newTextLen = newText.length();

            if (strIsCodeBlock(currentText)) {
                if (lastTextLen > 0) {
                    chunks.add(mutableLast);
                    mutableLast = "";
                }
                if (currentText.length() > maxLen) {
                    String restored = currentText.replace(CODE_BLOCK_LINE_MARKER, "\n");
                    chunks.addAll(splitTextByCharLengthLimit(restored, chunkSize, chunkSize));
                } else {
                    chunks.add(currentText);
                }
                continue;
            }

            if (TableTextSplitter.strIsMdTable(currentText) && newTextLen > maxLen) {
                if (lastTextLen > 0) {
                    chunks.add(mutableLast);
                    mutableLast = "";
                }
                int tableChunkSize = (int) (chunkSize * 1.2);
                chunks.addAll(
                        new TableTextSplitter(tableChunkSize, maxSize)
                                .markdownTableSplit(currentText));
                continue;
            }

            if (isMarkdownStep) {
                List<String> innerChunks =
                        splitTextRecursively(
                                newText, step + 1, "", parentTitle + item.title);
                if (innerChunks.isEmpty()) {
                    chunks.add(parentTitle + item.title);
                    continue;
                }
                if (step == markdownIndex + customRegLen) {
                    for (String chunk : innerChunks) {
                        chunks.add(parentTitle + item.title + chunk);
                    }
                } else {
                    chunks.addAll(innerChunks);
                }
                continue;
            }

            if (newTextLen > maxLen) {
                double minChunkLen = maxLen * 0.8;
                double maxChunkLen = maxLen * 1.2;

                if (newTextLen < maxChunkLen) {
                    chunks.add(newText);
                    mutableLast = getOneTextOverlapText(newText, step);
                    continue;
                }
                if (lastTextLen > minChunkLen) {
                    chunks.add(mutableLast);
                    mutableLast = getOneTextOverlapText(mutableLast, step);
                    i--;
                    continue;
                }

                List<String> innerChunks =
                        splitTextRecursively(
                                currentText, step + 1, mutableLast, parentTitle + item.title);
                if (innerChunks.isEmpty()) {
                    continue;
                }
                String lastChunk = innerChunks.get(innerChunks.size() - 1);
                if (lastChunk.length() < minChunkLen) {
                    chunks.addAll(innerChunks.subList(0, innerChunks.size() - 1));
                    mutableLast = lastChunk;
                    continue;
                }
                chunks.addAll(innerChunks);
                mutableLast = getOneTextOverlapText(lastChunk, step);
                continue;
            }

            if (forbidConcat) {
                chunks.add(currentText);
                continue;
            }

            mutableLast = newText;
        }

        String lastChunk = chunks.isEmpty() ? null : chunks.get(chunks.size() - 1);
        boolean shouldPushLastText =
                lastChunk == null || !lastChunk.endsWith(mutableLast);

        if (mutableLast.length() > 0 && lastChunk != null && shouldPushLastText) {
            if (mutableLast.length() < chunkSize * 0.4 && !strIsCodeBlock(lastChunk)) {
                chunks.set(chunks.size() - 1, lastChunk + mutableLast);
            } else {
                chunks.add(mutableLast);
            }
        } else if (mutableLast.length() > 0 && chunks.isEmpty()) {
            chunks.add(mutableLast);
        }

        return chunks;
    }

    private static boolean strIsCodeBlock(String str) {
        if (str == null) {
            return false;
        }
        return CODE_BLOCK_WHOLE.matcher(str.trim()).matches();
    }

    /** FastGPT splitTextByCharLengthLimit */
    private static List<String> splitTextByCharLengthLimit(
            String text, int maxLength, int stepLength) {
        List<String> chunks = new ArrayList<String>();
        int chunkLength = Math.max(1, maxLength);
        int chunkStep = Math.max(1, stepLength);
        for (int i = 0; i < text.length(); i += chunkStep) {
            int end = Math.min(i + chunkLength, text.length());
            chunks.add(text.substring(i, end));
        }
        return chunks;
    }

    private static String replaceLiteral(String text, String search, String replacement) {
        if (search.isEmpty()) {
            return text;
        }
        StringBuilder sb = new StringBuilder();
        int from = 0;
        int idx;
        while ((idx = text.indexOf(search, from)) >= 0) {
            sb.append(text, from, idx);
            sb.append(replacement);
            from = idx + search.length();
        }
        sb.append(text, from, text.length());
        return sb.toString();
    }

    private static String repeatChar(char c, int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}
