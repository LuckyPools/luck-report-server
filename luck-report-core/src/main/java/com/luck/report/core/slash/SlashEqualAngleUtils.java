package com.luck.report.core.slash;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 斜表头几何：从左上角引出分割线（与前端 slash-geometry 对齐）；三标签时一条到「底边中点」、一条到「右边中点」，其它数量在底边/右边均分落点
 */
public final class SlashEqualAngleUtils {
    private SlashEqualAngleUtils() {}

    /**
     * 从 (0,0) 沿 angleRad（水平向右为 0、顺时针向下）发射，与矩形右边或底边求交
     *
     * @param width 单元格宽，像素，&gt;0
     * @param height 单元格高，像素，&gt;0
     * @param angleRad 倾角（弧度），(0, π/2)
     * @return 交点坐标；非法时退回右下角
     */
    public static double[] rayHitBorder(double width, double height, double angleRad) {
        double dx = Math.cos(angleRad);
        double dy = Math.sin(angleRad);
        double t = Double.POSITIVE_INFINITY;
        if (dx > 1e-8) {
            t = Math.min(t, width / dx);
        }
        if (dy > 1e-8) {
            t = Math.min(t, height / dy);
        }
        if (!Double.isFinite(t) || t <= 0) {
            return new double[] {width, height};
        }
        return new double[] {dx * t, dy * t};
    }

    /**
     * 按底边中点 / 右边中点策略生成分割线与文字落点
     *
     * @param width 合并区宽度，像素
     * @param height 合并区高度，像素
     * @param labels 斜表头文案；空则按单空串处理
     * @return 线与标签布局；标签不足 2 个时无线
     */
    public static SlashEqualAngleResult compute(int width, int height, List<String> labels) {
        int w = Math.max(1, width);
        int h = Math.max(1, height);
        List<String> texts;
        if (labels == null || labels.isEmpty()) {
            texts = Collections.singletonList("");
        } else {
            texts = labels;
        }
        int n = texts.size();
        List<double[]> ends = buildDividerEnds(w, h, Math.max(0, n - 1));

        List<SlashLineLayout> lines = new ArrayList<>();
        for (double[] p : ends) {
            double angleRad = Math.atan2(p[1], p[0]);
            lines.add(new SlashLineLayout(
                    (int) Math.round(p[0]),
                    (int) Math.round(p[1]),
                    (angleRad * 180) / Math.PI));
        }

        double[] angles = new double[ends.size() + 2];
        angles[0] = 0;
        for (int i = 0; i < ends.size(); i++) {
            angles[i + 1] = Math.atan2(ends.get(i)[1], ends.get(i)[0]);
        }
        angles[angles.length - 1] = Math.PI / 2;

        List<SlashLabelLayout> labelLayouts = new ArrayList<>();
        for (int k = 0; k < n; k++) {
            double midRad = (angles[n - k - 1] + angles[n - k]) / 2;
            double[] hit = rayHitBorder(w, h, midRad);
            int x = (int) Math.round(hit[0] * 0.55);
            int y = (int) Math.round(hit[1] * 0.55);
            double midDeg = (midRad * 180) / Math.PI;
            int degree = (int) Math.round(Math.min(midDeg, 50) * 0.35);
            labelLayouts.add(new SlashLabelLayout(
                    texts.get(k),
                    Math.max(4, x),
                    Math.max(10, y),
                    degree));
        }
        return new SlashEqualAngleResult(lines, labelLayouts);
    }

    /**
     * 计算分割线终点：三标签用底边中点 + 右边中点；其余在底边/右边均分
     *
     * @param w 宽
     * @param h 高
     * @param lineCount 分割线数量（= 标签数 - 1）
     * @return 按倾角升序的终点列表，每项为 {x,y}
     */
    static List<double[]> buildDividerEnds(int w, int h, int lineCount) {
        List<double[]> ends = new ArrayList<>();
        if (lineCount <= 0) {
            return ends;
        }
        if (lineCount == 2) {
            ends.add(new double[] {w / 2.0, h});
            ends.add(new double[] {w, h / 2.0});
            return sortEndsByAngle(ends);
        }
        if (lineCount == 1) {
            ends.add(new double[] {w, h});
            return ends;
        }
        int rightCount = lineCount / 2;
        int bottomCount = lineCount - rightCount;
        for (int i = 1; i <= rightCount; i++) {
            ends.add(new double[] {w, (h * 1.0 * i) / (rightCount + 1)});
        }
        for (int i = 1; i <= bottomCount; i++) {
            ends.add(new double[] {(w * 1.0 * i) / (bottomCount + 1), h});
        }
        return sortEndsByAngle(ends);
    }

    private static List<double[]> sortEndsByAngle(List<double[]> ends) {
        List<double[]> sorted = new ArrayList<>(ends);
        Collections.sort(sorted, new Comparator<double[]>() {
            @Override
            public int compare(double[] a, double[] b) {
                return Double.compare(Math.atan2(a[1], a[0]), Math.atan2(b[1], b[0]));
            }
        });
        return sorted;
    }
}
