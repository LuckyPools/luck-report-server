package com.luck.report.core.config;

import com.luck.report.core.parser.impl.searchform.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FormParserConfiguration {

    @Bean("bean.formParserUtils")
    public FormParserUtils formParserUtils() {
        return new FormParserUtils();
    }

    @Bean("bean.rowParser")
    public RowParser rowParser() {
        return new RowParser();
    }

    @Bean("bean.colParser")
    public ColParser colParser() {
        return new ColParser();
    }

    @Bean("bean.inputParser")
    public InputParser inputParser() {
        return new InputParser();
    }

    @Bean("bean.buttonParser")
    public ButtonParser buttonParser() {
        return new ButtonParser();
    }

    @Bean("bean.switchParser")
    public SwitchParser switchParser() {
        return new SwitchParser();
    }

    @Bean("bean.selectParser")
    public SelectParser selectParser() {
        return new SelectParser();
    }

    @Bean("bean.cascaderParser")
    public CascaderParser cascaderParser() {
        return new CascaderParser();
    }

    @Bean("bean.treeSelectParser")
    public TreeSelectParser treeSelectParser() {
        return new TreeSelectParser();
    }

    @Bean("bean.checkboxGroupParser")
    public CheckboxGroupParser checkboxGroupParser() {
        return new CheckboxGroupParser();
    }

    @Bean("bean.radioGroupParser")
    public RadioGroupParser radioGroupParser() {
        return new RadioGroupParser();
    }

    @Bean("bean.inputNumberParser")
    public InputNumberParser inputNumberParser() {
        return new InputNumberParser();
    }

    @Bean("bean.datePickerParser")
    public DatePickerParser datePickerParser() {
        return new DatePickerParser();
    }
}
