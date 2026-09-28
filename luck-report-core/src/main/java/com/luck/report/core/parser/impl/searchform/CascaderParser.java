package com.luck.report.core.parser.impl.searchform;

import com.luck.report.core.definition.searchform.component.CascaderComponent;
import org.dom4j.Element;

public class CascaderParser implements FormParser<CascaderComponent> {
    @Override
    public CascaderComponent parse(Element element) {
        CascaderComponent cascader = new CascaderComponent();
        FormParserUtils.parseBaseOptionAttributes(cascader, element);

        cascader.setOptions(FormParserUtils.parseOptions(element));
        return cascader;
    }

    @Override
    public boolean support(String name) {
        return name.equals("cascader");
    }
}
