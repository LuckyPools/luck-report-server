package com.luck.report.web.utils;

import org.dom4j.Attribute;
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

import com.luck.report.core.exception.ReportBizException;
import com.luck.report.web.i18n.ReportI18n;

/**
 * 报表转化工具 将 luck-report v1 版本的报表转化为 v2 版本
 *
 * @author luck
 */
public class TransformLuckReportV1Utils {

    /**
     * v1 tag 前缀
     */
    private static final String V1_TAG_PREFIX = "u-";
    /**
     * v2 tag 前缀
     */
    private static final String V2_TAG_PREFIX = "a-";

    /**
     * v1 formRef 值
     */
    private static final String V1_FORM_REF = "uForm";
    /**
     * v2 formRef 值
     */
    private static final String V2_FORM_REF = "aFormRef";

    /**
     * v1 form 默认 gutter
     */
    private static final String V1_FORM_GUTTER = "15";
    /**
     * v2 form 默认 gutter
     */
    private static final String V2_FORM_GUTTER = "0";

    /**
     * v1 row 默认 gutter
     */
    private static final String V1_ROW_GUTTER = "15";
    /**
     * v2 row 默认 gutter
     */
    private static final String V2_ROW_GUTTER = "0";

    /**
     * document URL 映射：v1 路径 → v2 ant-design-vue URL
     */
    private static final Map<String, String> DOCUMENT_URL_MAP = new HashMap<>();
    static {
        DOCUMENT_URL_MAP.put("/component/input", "https://www.antdv.com/components/input-cn");
        DOCUMENT_URL_MAP.put("/component/input-number", "https://www.antdv.com/components/input-number-cn");
        DOCUMENT_URL_MAP.put("/component/select", "https://www.antdv.com/components/select-cn");
        DOCUMENT_URL_MAP.put("/component/radio", "https://www.antdv.com/components/radio-cn");
        DOCUMENT_URL_MAP.put("/component/checkbox", "https://www.antdv.com/components/checkbox-cn");
        DOCUMENT_URL_MAP.put("/component/switch", "https://www.antdv.com/components/switch-cn");
        DOCUMENT_URL_MAP.put("/component/date-picker", "https://www.antdv.com/components/date-picker-cn");
        DOCUMENT_URL_MAP.put("/component/button", "https://www.antdv.com/components/button-cn");
        DOCUMENT_URL_MAP.put("/component/layout", "");
    }

    /**
     * size 需要从 medium 转为 small 的组件 tag
     */
    private static final String[] SIZE_MEDIUM_TO_SMALL_TAGS = {
            "a-radio-group", "a-checkbox-group", "a-button"
    };

    public static void main(String[] args) {
        transformXmlFolder("E:\\IOTest\\luck-report\\v1\\", "E:\\IOTest\\luck-report\\v2\\");
    }

    /**
     * 将 luck-report v1 的报表批量转化为 v2 版本
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
     * @param outputDirPath 输出目录路径
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
     * 将 LuckReport V1 报表 XML 转换为 V2
     *
     * @param xmlContent 原始 XML，非空
     * @return 转化后的 XML 字符串
     */
    public static String transformXmlContent(String xmlContent) {
        try {
            Document doc = DocumentHelper.parseText(xmlContent);
            Element root = doc.getRootElement();

            Element formElement = root.element("form");
            if (formElement != null) {
                transformFormElement(formElement);
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
     * 转换 form 元素
     */
    private static void transformFormElement(Element formElement) {
        Attribute formRefAttr = formElement.attribute("formRef");
        if (formRefAttr != null && V1_FORM_REF.equals(formRefAttr.getValue())) {
            formRefAttr.setValue(V2_FORM_REF);
        }

        updateTagAttribute(formElement);

        Attribute gutterAttr = formElement.attribute("gutter");
        if (gutterAttr != null && V1_FORM_GUTTER.equals(gutterAttr.getValue())) {
            gutterAttr.setValue(V2_FORM_GUTTER);
        }

        @SuppressWarnings("unchecked")
        List<Element> children = formElement.elements();
        for (Element child : children) {
            transformElement(child);
        }
    }

    /**
     * 递归转换元素及其子元素
     */
    private static void transformElement(Element element) {
        String elementName = element.getName();

        updateTagAttribute(element);

        updateDocumentAttribute(element);

        updateSizeAttribute(element);

        if ("row".equals(elementName)) {
            Attribute rowGutterAttr = element.attribute("gutter");
            if (rowGutterAttr != null && V1_ROW_GUTTER.equals(rowGutterAttr.getValue())) {
                rowGutterAttr.setValue(V2_ROW_GUTTER);
            }
        }

        @SuppressWarnings("unchecked")
        List<Element> children = element.elements();
        for (Element child : children) {
            transformElement(child);
        }
    }

    /**
     * 更新 tag 属性
     */
    private static void updateTagAttribute(Element element) {
        Attribute tagAttr = element.attribute("tag");
        if (tagAttr != null) {
            String tagValue = tagAttr.getValue();
            if (tagValue.startsWith(V1_TAG_PREFIX)) {
                tagAttr.setValue(V2_TAG_PREFIX + tagValue.substring(V1_TAG_PREFIX.length()));
            }
        }
    }

    /**
     * 更新 document 属性
     */
    private static void updateDocumentAttribute(Element element) {
        Attribute docAttr = element.attribute("document");
        if (docAttr != null) {
            String docValue = docAttr.getValue();
            String newDocValue = DOCUMENT_URL_MAP.get(docValue);
            if (newDocValue != null) {
                docAttr.setValue(newDocValue);
            }
        }
    }

    /**
     * 更新 size 属性
     */
    private static void updateSizeAttribute(Element element) {
        Attribute tagAttr = element.attribute("tag");
        Attribute sizeAttr = element.attribute("size");
        if (tagAttr == null || sizeAttr == null) {
            return;
        }
        String tagValue = tagAttr.getValue();
        String sizeValue = sizeAttr.getValue();
        if ("medium".equals(sizeValue)) {
            for (String targetTag : SIZE_MEDIUM_TO_SMALL_TAGS) {
                if (targetTag.equals(tagValue)) {
                    sizeAttr.setValue("small");
                    break;
                }
            }
        }
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
