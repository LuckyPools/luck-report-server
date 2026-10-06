/*******************************************************************************
 * Copyright 2017 Bstek
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.luck.report.core.utils;

/**
 * @author Jacky.gao
 * @since 2017年3月16日
 */
public class UnitUtils {
    /**
     * 磅转像素。与前端 table.ts pointToPixel 对齐：Math.round(pt * 1.33)。旧实现 BigDecimal.intValue() 向 0 截断，会导致预览行高系统性偏矮 0~1px/行。
     */
    public static int pointToPixel(double point) {
        double value = point * 1.33;
        return (int) Math.round(value);
    }

    /**
     * 像素转磅。与前端 table.ts pixelToPoint 对齐：Math.round(px * 0.75)。
     */
    public static int pixelToPoint(double pixel) {
        double value = pixel * 0.75;
        return (int) Math.round(value);
    }

    public static final float pointToInche(final float value) {
        return value / 72f;
    }

    public static int pointToTwip(int point) {
        return point * 20;
    }

    /**
     * 像素列宽转 Excel 的 1/256 字符宽
     *
     * @param pixels 列宽像素
     * @return setColumnWidth 参数
     */
    public static int pixelToExcelColumnWidth(int pixels) {
        if (pixels <= 0) {
            return 0;
        }
        int mdw = 7;
        int[] remainderTo256th = {0, 36, 73, 109, 146, 182, 219};
        int excelWidth = 256 * (pixels / mdw) + remainderTo256th[pixels % mdw];
        int maxWidth = 255 * 256;
        return Math.min(excelWidth, maxWidth);
    }
}
