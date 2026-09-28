package com.luck.report.core.parser.impl.searchform;

import com.luck.report.core.definition.searchform.component.SelectComponent;
import org.dom4j.Element;

public class SelectParser implements FormParser<SelectComponent> {
    @Override
    public SelectComponent parse(Element element) {
        SelectComponent select = new SelectComponent();
        FormParserUtils.parseBaseOptionAttributes(select, element);
        select.setOptions(FormParserUtils.parseOptions(element));
        return select;
    }

    @Override
    public boolean support(String name) {
        return name.equals("select");
    }
}
