package com.luck.report.core.parser.impl.searchform;

import com.luck.report.core.definition.searchform.component.TreeSelectComponent;
import org.dom4j.Element;

public class TreeSelectParser implements FormParser<TreeSelectComponent> {
    @Override
    public TreeSelectComponent parse(Element element) {
        TreeSelectComponent treeSelect = new TreeSelectComponent();
        FormParserUtils.parseBaseOptionAttributes(treeSelect, element);

        Boolean treeDefaultExpandAll = FormParserUtils.parseBooleanAttribute(element.attributeValue("treeDefaultExpandAll"));
        if (treeDefaultExpandAll != null) {
            treeSelect.setTreeDefaultExpandAll(treeDefaultExpandAll);
        }

        treeSelect.setOptions(FormParserUtils.parseOptions(element));
        return treeSelect;
    }

    @Override
    public boolean support(String name) {
        return name.equals("tree-select");
    }
}
