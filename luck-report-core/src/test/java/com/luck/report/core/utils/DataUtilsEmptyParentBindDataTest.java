package com.luck.report.core.utils;

import com.luck.report.core.definition.Expand;
import com.luck.report.core.definition.value.DatasetValue;
import com.luck.report.core.model.Cell;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** 同数据集左父格 bindData 为 null 时，应按空约束处理，不能回退全量数据集 */
class DataUtilsEmptyParentBindDataTest {

    @Test
    void leftParentWithNullBindData_returnsEmpty() {
        Cell parent = new Cell();
        parent.setName("B3");
        parent.setExpand(Expand.Down);
        parent.setValue(dataset("t_shop", "name"));
        parent.setBindData(null);

        Cell child = new Cell();
        child.setName("C3");
        child.setLeftParentCell(parent);

        // 父格已约束时不应再访问 Context 全量数据；传 null 即可验证不会 NPE 回退
        List<?> data = DataUtils.fetchData(child, null, "t_shop");

        assertTrue(data.isEmpty());
    }

    private static DatasetValue dataset(String name, String property) {
        DatasetValue value = new DatasetValue();
        value.setDatasetName(name);
        value.setProperty(property);
        return value;
    }
}
