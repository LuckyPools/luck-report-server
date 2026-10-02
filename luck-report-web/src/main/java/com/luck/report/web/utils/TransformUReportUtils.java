package com.luck.report.web.utils;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.luck.report.core.exception.ReportBizException;
import com.luck.report.web.i18n.ReportI18n;

/**
 * 报表转化工具 将 UReport2 的报表转化为适配当前设计器的版本（v2，基于 ant-design-vue）
 * @author luck
 */
public class TransformUReportUtils {

    private static final AtomicInteger FIELD_COUNTER = new AtomicInteger(100);

    public static void main(String[] args) {
        transformXmlFolder("E:\\IOTest\\luck-report\\ureport\\","E:\\IOTest\\luck-report\\v2\\");
    }

    /**
     * 将 UReport2 的报表转化为适配当前设计器的版本
     * @param inputFolderPath 原文件夹路径
     * @param outputFolderPath 目标文件夹路径
     */
    public static void transformXmlFolder(String inputFolderPath, String outputFolderPath) {
        File inputFolder = new File(inputFolderPath);
        if (!inputFolder.exists() || !inputFolder.isDirectory()) {
            throw new ReportBizException("error.transform.inputDirInvalid", inputFolderPath);
        }

        File outputFolder = new File(outputFolderPath);
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }

        processFolder(inputFolder, outputFolder);
    }

    /**
     * 转化文件夹中的xml
     * @param inputFolder
     * @param outputFolder
     */
    private static void processFolder(File inputFolder, File outputFolder) {
        File[] files = inputFolder.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                File newOutputFolder = new File(outputFolder, file.getName());
                if (!newOutputFolder.exists()) {
                    newOutputFolder.mkdirs();
                }
                processFolder(file, newOutputFolder);
            } else if (file.isFile() && file.getName().toLowerCase().endsWith(".xml")) {
                try {
                    transformXmlFile(file.getAbsolutePath(), outputFolder.getAbsolutePath());
                } catch (Exception e) {
                    System.err.println("转换文件失败: " + file.getAbsolutePath() + ", 错误: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 转化xml文件
     * @param inputFilePath 输入XML文件的完整路径
     * @param outputDirPath 输出目录路径，如果目录不存在会自动创建
     */
    public static void transformXmlFile(String inputFilePath, String outputDirPath) {
        try {
            File inputFile = new File(inputFilePath);
            if (!inputFile.exists()) {
                throw new ReportBizException("error.transform.inputFileInvalid", inputFilePath);
            }

            String content = new String(Files.readAllBytes(inputFile.toPath()), StandardCharsets.UTF_8);
            String transformed = transformXmlContent(content);

            File outputDir = new File(outputDirPath);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            File outputFile = new File(outputDir, inputFile.getName());
            Files.write(outputFile.toPath(), transformed.getBytes(StandardCharsets.UTF_8));

            System.out.println("转换成功，文件已保存到: " + outputFile.getAbsolutePath());
        } catch (ReportBizException e) {
            throw e;
        } catch (Exception e) {
            throw new ReportBizException("error.transform.xmlFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 将 UReport2 报表 XML 转换为 V2
     *
     * @param xmlContent 原始 XML，非空
     * @return 转化后的 XML 字符串
     */
    public static String transformXmlContent(String xmlContent) {
        try {
            Document doc = DocumentHelper.parseText(xmlContent);
            Element root = doc.getRootElement();

            Element searchFormElement = root.element("search-form");
            if (searchFormElement != null) {
                Element formElement = DocumentHelper.createElement("form");
                transformSearchForm(searchFormElement, formElement);

                int index = root.indexOf(searchFormElement);
                root.remove(searchFormElement);
                root.elements().add(index, formElement);
            }

            transformSqlElements(root);
            return documentToString(doc);
        } catch (ReportBizException e) {
            throw e;
        } catch (Exception e) {
            throw new ReportBizException("error.transform.xmlFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 将 Document 序列化为 XML 字符串
     *
     * @param doc 文档，非空
     * @return XML 文本
     */
    private static String documentToString(Document doc) throws Exception {
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8");
        StringWriter sw = new StringWriter();
        XMLWriter xmlWriter = new XMLWriter(sw, format);
        xmlWriter.write(doc);
        xmlWriter.close();
        return sw.toString();
    }

    /**
     * 转换search-form元素为form元素
     *
     * @param oldForm 原版search-form元素
     * @param newForm 新版form元素
     */
    private static void transformSearchForm(Element oldForm, Element newForm) {
        newForm.addAttribute("formRef", "aFormRef");
        newForm.addAttribute("tag", "a-form");
        newForm.addAttribute("formModel", "formData");
        newForm.addAttribute("size", "medium");
        newForm.addAttribute("labelPosition", "right");
        newForm.addAttribute("labelWidth", "100");
        newForm.addAttribute("formRules", "rules");
        newForm.addAttribute("gutter", "0");
        newForm.addAttribute("disabled", "false");
        newForm.addAttribute("span", "24");
        newForm.addAttribute("formBtns", "true");

        List<Element> oldGrids = oldForm.elements("grid");
        for (Element oldGrid : oldGrids) {
            Element newRow = newForm.addElement("row");
            transformGrid(oldGrid, newRow);
        }
    }

    /**
     * 转换grid元素为row元素
     *
     * @param oldGrid 原版grid元素
     * @param newRow 新版row元素
     */
    private static void transformGrid(Element oldGrid, Element newRow) {
        newRow.addAttribute("layout", "rowFormItem");
        newRow.addAttribute("tagIcon", "row");
        newRow.addAttribute("type", "default");
        newRow.addAttribute("tag", "a-row");
        newRow.addAttribute("justify", "start");
        newRow.addAttribute("align", "top");
        newRow.addAttribute("layoutTree", "true");
        newRow.addAttribute("span", "24");
        newRow.addAttribute("gutter", "0");

        String formId = generateFormId();
        newRow.addAttribute("formId", formId);
        newRow.addAttribute("renderKey", generateRenderKey());
        newRow.addAttribute("componentName", "row" + formId);

        String borderWidth = oldGrid.attributeValue("border-width");
        if (borderWidth != null && !borderWidth.isEmpty()) {
            try {
                int gutter = Integer.parseInt(borderWidth);
                newRow.addAttribute("gutter", String.valueOf(gutter));
            } catch (NumberFormatException e) {
            }
        }

        List<Element> oldCols = oldGrid.elements("col");
        for (Element oldCol : oldCols) {
            transformCol(oldCol, newRow);
        }
    }

    /**
     * 转换col元素
     *
     * @param oldCol 原版col元素
     * @param parentRow 父级row元素，新组件将添加到此元素中
     */
    private static void transformCol(Element oldCol, Element parentRow) {
        String size = oldCol.attributeValue("size");
        List<Element> children = oldCol.elements();

        for (Element child : children) {
            String tagName = child.getName();

            if (tagName.equals("grid")) {
                Element nestedRow = parentRow.addElement("row");
                transformGrid(child, nestedRow);
            } else {
                String newTagName = mapComponentTag(tagName);

                if (newTagName != null) {
                    Element newComponent = parentRow.addElement(newTagName);
                    transformComponent(child, newComponent, size);
                }
            }
        }
    }

    /**
     * 转换组件元素
     *
     * @param oldComponent 原版组件元素
     * @param newComponent 新版组件元素
     * @param colSize col的size属性值，用于设置组件的span
     */
    private static void transformComponent(Element oldComponent, Element newComponent, String colSize) {
        String tagName = oldComponent.getName();

        if (tagName.equals("button-submit") || tagName.equals("button-reset")) {
            return;
        }

        Map<String, String> commonAttributes = new HashMap<>();
        commonAttributes.put("label", oldComponent.attributeValue("label"));
        commonAttributes.put("span", colSize != null ? colSize : "24");
        commonAttributes.put("labelWidth", "null");
        commonAttributes.put("disabled", "false");
        commonAttributes.put("required", "true");
        commonAttributes.put("regList", "[]");
        commonAttributes.put("changeTag", "true");
        commonAttributes.put("layout", "colFormItem");

        String formId = generateFormId();
        commonAttributes.put("formId", formId);
        commonAttributes.put("renderKey", generateRenderKey());

        String vModel = "field" + FIELD_COUNTER.getAndIncrement();
        commonAttributes.put("vModel", vModel);

        switch (tagName) {
            case "input-text":
                transformInputText(oldComponent, newComponent, commonAttributes);
                break;
            case "input-datetime":
                transformInputDatetime(oldComponent, newComponent, commonAttributes);
                break;
            case "input-radio":
                transformInputRadio(oldComponent, newComponent, commonAttributes);
                break;
            case "input-checkbox":
                transformInputCheckbox(oldComponent, newComponent, commonAttributes);
                break;
            case "input-select":
                transformInputSelect(oldComponent, newComponent, commonAttributes);
                break;
            default:
                transformGeneric(oldComponent, newComponent, commonAttributes);
        }
    }

    /**
     * 转换input-text组件为input组件
     *
     * @param oldComponent 原版input-text元素
     * @param newComponent 新版input元素
     * @param commonAttributes 通用属性集合
     */
    private static void transformInputText(Element oldComponent, Element newComponent, Map<String, String> commonAttributes) {
        newComponent.addAttribute("tag", "a-input");
        newComponent.addAttribute("tagIcon", "input");
        newComponent.addAttribute("placeholder", "请输入" + (oldComponent.attributeValue("label") != null ? oldComponent.attributeValue("label") : ""));
        newComponent.addAttribute("style", "{\"width\":\"100%\"}");
        newComponent.addAttribute("clearable", "true");
        newComponent.addAttribute("prepend", "");
        newComponent.addAttribute("append", "");
        newComponent.addAttribute("prefixIcon", "");
        newComponent.addAttribute("suffixIcon", "");
        newComponent.addAttribute("maxlength", "null");
        newComponent.addAttribute("showWordLimit", "false");
        newComponent.addAttribute("readonly", "false");
        newComponent.addAttribute("defaultValue", "null");
        newComponent.addAttribute("document", "https://www.antdv.com/components/input-cn");

        for (Map.Entry<String, String> entry : commonAttributes.entrySet()) {
            newComponent.addAttribute(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 转换input-datetime组件为date-picker组件
     *
     * @param oldComponent 原版input-datetime元素
     * @param newComponent 新版date-picker元素
     * @param commonAttributes 通用属性集合
     */
    private static void transformInputDatetime(Element oldComponent, Element newComponent, Map<String, String> commonAttributes) {
        newComponent.addAttribute("tag", "a-date-picker");
        newComponent.addAttribute("tagIcon", "date");
        newComponent.addAttribute("placeholder", "请选择" + (oldComponent.attributeValue("label") != null ? oldComponent.attributeValue("label") : ""));
        newComponent.addAttribute("defaultValue", "null");
        newComponent.addAttribute("type", "date");
        newComponent.addAttribute("style", "{\"width\":\"100%\"}");
        newComponent.addAttribute("clearable", "true");
        newComponent.addAttribute("readonly", "false");
        newComponent.addAttribute("format", "YYYY-MM-DD");
        newComponent.addAttribute("valueFormat", "format");
        newComponent.addAttribute("document", "https://www.antdv.com/components/date-picker-cn");

        String oldFormat = oldComponent.attributeValue("format");
        if (oldFormat != null && !oldFormat.isEmpty()) {
            newComponent.addAttribute("format", convertDateFormat(oldFormat));
        }

        for (Map.Entry<String, String> entry : commonAttributes.entrySet()) {
            newComponent.addAttribute(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 转换input-radio组件为radio-group组件
     *
     * @param oldComponent 原版input-radio元素
     * @param newComponent 新版radio-group元素
     * @param commonAttributes 通用属性集合
     */
    private static void transformInputRadio(Element oldComponent, Element newComponent, Map<String, String> commonAttributes) {
        newComponent.addAttribute("tag", "a-radio-group");
        newComponent.addAttribute("tagIcon", "radio");
        newComponent.addAttribute("style", "{}");
        newComponent.addAttribute("optionType", "default");
        newComponent.addAttribute("border", "false");
        newComponent.addAttribute("size", "small");
        newComponent.addAttribute("defaultValue", "false");
        newComponent.addAttribute("document", "https://www.antdv.com/components/radio-cn");

        for (Map.Entry<String, String> entry : commonAttributes.entrySet()) {
            newComponent.addAttribute(entry.getKey(), entry.getValue());
        }

        List<Element> options = oldComponent.elements("option");
        for (Element option : options) {
            Element newOption = newComponent.addElement("option");
            newOption.addAttribute("label", option.attributeValue("label"));
            newOption.addAttribute("value", option.attributeValue("value"));
        }
    }

    /**
     * 转换input-checkbox组件为checkbox-group组件
     *
     * @param oldComponent 原版input-checkbox元素
     * @param newComponent 新版checkbox-group元素
     * @param commonAttributes 通用属性集合
     */
    private static void transformInputCheckbox(Element oldComponent, Element newComponent, Map<String, String> commonAttributes) {
        newComponent.addAttribute("tag", "a-checkbox-group");
        newComponent.addAttribute("tagIcon", "checkbox");
        newComponent.addAttribute("defaultValue", "[]");
        newComponent.addAttribute("style", "{}");
        newComponent.addAttribute("optionType", "default");
        newComponent.addAttribute("border", "false");
        newComponent.addAttribute("size", "small");
        newComponent.addAttribute("document", "https://www.antdv.com/components/checkbox-cn");

        for (Map.Entry<String, String> entry : commonAttributes.entrySet()) {
            newComponent.addAttribute(entry.getKey(), entry.getValue());
        }

        List<Element> options = oldComponent.elements("option");
        for (Element option : options) {
            Element newOption = newComponent.addElement("option");
            newOption.addAttribute("label", option.attributeValue("label"));
            newOption.addAttribute("value", option.attributeValue("value"));
        }
    }

    /**
     * 转换input-select组件为select组件
     *
     * @param oldComponent 原版input-select元素
     * @param newComponent 新版select元素
     * @param commonAttributes 通用属性集合
     */
    private static void transformInputSelect(Element oldComponent, Element newComponent, Map<String, String> commonAttributes) {
        newComponent.addAttribute("tag", "a-select");
        newComponent.addAttribute("tagIcon", "select");
        newComponent.addAttribute("placeholder", "请选择" + (oldComponent.attributeValue("label") != null ? oldComponent.attributeValue("label") : ""));
        newComponent.addAttribute("style", "{}");
        newComponent.addAttribute("clearable", "true");
        newComponent.addAttribute("filterable", "false");
        newComponent.addAttribute("multiple", "false");
        newComponent.addAttribute("defaultValue", "null");
        newComponent.addAttribute("document", "https://www.antdv.com/components/select-cn");

        for (Map.Entry<String, String> entry : commonAttributes.entrySet()) {
            newComponent.addAttribute(entry.getKey(), entry.getValue());
        }

        List<Element> options = oldComponent.elements("option");
        for (Element option : options) {
            Element newOption = newComponent.addElement("option");
            newOption.addAttribute("label", option.attributeValue("label"));
            newOption.addAttribute("value", option.attributeValue("value"));
        }
    }

    /**
     * 通用组件转换方法
     *
     * @param oldComponent 原版组件元素
     * @param newComponent 新版组件元素
     * @param commonAttributes 通用属性集合
     */
    private static void transformGeneric(Element oldComponent, Element newComponent, Map<String, String> commonAttributes) {
        newComponent.addAttribute("tagIcon", "input");
        for (Map.Entry<String, String> entry : commonAttributes.entrySet()) {
            newComponent.addAttribute(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 映射原版组件标签名为新版组件标签名
     *
     * @param oldTag 原版组件标签名
     * @return 新版组件标签名，如果不支持则返回null
     */
    private static String mapComponentTag(String oldTag) {
        switch (oldTag) {
            case "input-text":
                return "input";
            case "input-datetime":
                return "date-picker";
            case "input-radio":
                return "radio-group";
            case "input-checkbox":
                return "checkbox-group";
            case "input-select":
                return "select";
            default:
                return null;
        }
    }

    /**
     * 转换日期格式
     *
     * @param oldFormat 原版日期格式字符串
     * @return 新版日期格式字符串
     */
    private static String convertDateFormat(String oldFormat) {
        if (oldFormat == null) {
            return "YYYY-MM-DD";
        }
        switch (oldFormat.toLowerCase()) {
            case "yyyy-mm-dd":
                return "YYYY-MM-DD";
            case "yyyy-mm-dd hh:mm:ss":
                return "YYYY-MM-DD HH:mm:ss";
            case "hh:mm:ss":
                return "HH:mm:ss";
            default:
                return "YYYY-MM-DD";
        }
    }

    /**
     * 生成随机的formId
     *
     * @return 3位随机数字字符串
     */
    private static String generateFormId() {
        return String.valueOf(100 + (int)(Math.random() * 900));
    }

    /**
     * 生成唯一的renderKey
     *
     * @return 基于时间戳和随机数的唯一字符串
     */
    private static String generateRenderKey() {
        return String.valueOf(System.currentTimeMillis() + (int)(Math.random() * 1000));
    }

    /**
     * 递归转换XML中所有 <sql> 元素的参数占位符
     *
     * @param element 当前XML元素
     */
    private static void transformSqlElements(Element element) {
        @SuppressWarnings("unchecked")
        List<Element> children = element.elements();
        for (Element child : children) {
            if ("sql".equals(child.getName())) {
                String text = child.getText();
                if (text != null && text.contains(":")) {
                    String converted = text.replaceAll(
                            "(?<![a-zA-Z0-9_\\p{L}]):([a-zA-Z_\\p{L}][a-zA-Z0-9_\\p{L}]*)",
                            "#{$1}"
                    );
                    child.clearContent();
                    child.addCDATA(converted);
                }
            } else {
                transformSqlElements(child);
            }
        }
    }

}
