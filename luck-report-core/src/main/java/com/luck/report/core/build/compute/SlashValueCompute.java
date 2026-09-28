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
package com.luck.report.core.build.compute;

import com.luck.report.core.build.BindData;
import com.luck.report.core.build.Context;
import com.luck.report.core.definition.value.SlashValue;
import com.luck.report.core.definition.value.ValueType;
import com.luck.report.core.model.Cell;
import com.luck.report.core.model.Image;
import com.luck.report.core.parser.SlashBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 斜表头取值：按均分 90° 重绘 PNG，保证预览与设计器一致。
 *
 * @author Jacky.gao
 * @since 2017年3月14日
 */
public class SlashValueCompute implements ValueCompute {
    @Override
    public List<BindData> compute(Cell cell, Context context) {
        List<BindData> list = new ArrayList<BindData>();
        SlashValue v = (SlashValue) cell.getValue();
        String base64 = new SlashBuilder().buildSlashImage(cell, context);
        if (base64 == null || base64.isEmpty()) {
            base64 = v.getBase64Data();
        }
        Image img = new Image(base64, "slash.png", 0, 0);
        BindData bindData = new BindData(img);
        list.add(bindData);
        return list;
    }

    @Override
    public ValueType type() {
        return ValueType.slash;
    }
}
