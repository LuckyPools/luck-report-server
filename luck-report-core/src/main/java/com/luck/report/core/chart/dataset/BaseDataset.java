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
package com.luck.report.core.chart.dataset;


import java.io.Serializable;
import java.util.List;

/**
 * @author Jacky.gao
 * @since 2017年6月9日
 */
public abstract class BaseDataset implements Dataset, Serializable {
    private static final long serialVersionUID = 1L;

    /** ECharts 官方默认色板（与前端选择弹窗 / ECHARTS_COLORS 一致） */
    private static final String[] DEFAULT_COLORS = {
            "84, 112, 198", "145, 204, 117", "250, 200, 88", "238, 102, 102", "115, 192, 222", "154, 96, 180"
    };

    /** 用户自定义调色板（r,g,b）；空或 null 时回退 DEFAULT_COLORS */
    private List<String> colorPalette;

    public void setColorPalette(List<String> colorPalette) {
        this.colorPalette = colorPalette;
    }

    public List<String> getColorPalette() {
        return colorPalette;
    }

    protected String getRgbColor(int index) {
        String[] colors;
        if (colorPalette != null && !colorPalette.isEmpty()) {
            colors = colorPalette.toArray(new String[0]);
        } else {
            colors = DEFAULT_COLORS;
        }
        if (index < 0) {
            index = 0;
        }
        index = index % colors.length;
        return colors[index].trim();
    }
}
