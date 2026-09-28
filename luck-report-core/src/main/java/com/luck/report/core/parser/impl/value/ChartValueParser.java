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
package com.luck.report.core.parser.impl.value;

import com.luck.report.core.chart.Chart;
import com.luck.report.core.chart.Font;
import com.luck.report.core.chart.axes.BaseAxes;
import com.luck.report.core.chart.axes.ScaleLabel;
import com.luck.report.core.chart.axes.impl.XAxes;
import com.luck.report.core.chart.axes.impl.YAxes;
import com.luck.report.core.chart.dataset.CollectType;
import com.luck.report.core.chart.dataset.Dataset;
import com.luck.report.core.chart.dataset.impl.BubbleDataset;
import com.luck.report.core.chart.dataset.impl.ScatterDataset;
import com.luck.report.core.chart.dataset.impl.category.*;
import com.luck.report.core.chart.dataset.impl.category.*;
import com.luck.report.core.chart.option.Easing;
import com.luck.report.core.chart.option.Labels;
import com.luck.report.core.chart.option.Option;
import com.luck.report.core.chart.option.Position;
import com.luck.report.core.chart.option.impl.AnimationsOption;
import com.luck.report.core.chart.option.impl.ColorsOption;
import com.luck.report.core.chart.option.impl.LegendOption;
import com.luck.report.core.chart.option.impl.TitleOption;
import com.luck.report.core.chart.plugins.DataLabelsPlugin;
import com.luck.report.core.definition.value.ChartValue;
import com.luck.report.core.definition.value.Value;
import com.luck.report.core.exception.ReportParseException;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jacky.gao
 * @since 2017年6月28日
 */
public class ChartValueParser extends ValueParser {

    @Override
    public Value parse(Element element) {
        ChartValue value = new ChartValue();
        Chart chart = new Chart();
        value.setChart(chart);
        for (Object obj : element.elements()) {
            if (obj == null || !(obj instanceof Element)) {
                continue;
            }
            Element ele = (Element) obj;
            String name = ele.getName();
            if (name.equals("dataset")) {
                Dataset dataset = parseDataset(ele);
                chart.setDataset(dataset);
            } else if (name.equals("xaxes")) {
                XAxes xaxes = new XAxes();
                parseAxes(ele, xaxes);
                chart.setXaxes(xaxes);
            } else if (name.equals("yaxes")) {
                YAxes yaxes = new YAxes();
                parseAxes(ele, yaxes);
                chart.setYaxes(yaxes);
            } else if (name.equals("option")) {
                chart.getOptions().add(parseOption(ele));
            } else if (name.equals("plugin")) {
                String pluginName = ele.attributeValue("name");
                if (pluginName.equals("data-labels")) {
                    DataLabelsPlugin plugin = new DataLabelsPlugin();
                    plugin.setDisplay(Boolean.valueOf(ele.attributeValue("display")));
                    plugin.setPosition(ele.attributeValue("position"));
                    plugin.setFormatter(ele.attributeValue("formatter"));
                    Element fontEle = ele.element("font");
                    if (fontEle != null) {
                        plugin.setFont(parseFont(fontEle));
                    }
                    chart.getPlugins().add(plugin);
                }
            }
        }
        return value;
    }

    private Option parseOption(Element element) {
        String type = element.attributeValue("type");
        Option target = null;
        if (type.equals("title")) {
            TitleOption option = new TitleOption();
            String display = element.attributeValue("display");
            if (StringUtils.isNotBlank(display)) {
                option.setDisplay(Boolean.valueOf(display));
            }
            String position = element.attributeValue("position");
            if (StringUtils.isNotBlank(position)) {
                if ("left".equals(position) || "right".equals(position)) {
                    option.setPosition(Position.top);
                    option.setAlign(position);
                } else {
                    option.setPosition(Position.valueOf(position));
                }
            }
            String align = element.attributeValue("align");
            if (StringUtils.isNotBlank(align)) {
                option.setAlign(align);
            }
            String text = element.attributeValue("text");
            option.setText(text);
            target = option;
        } else if (type.equals("legend")) {
            LegendOption option = new LegendOption();
            String display = element.attributeValue("display");
            if (StringUtils.isNotBlank(display)) {
                option.setDisplay(Boolean.valueOf(display));
            }
            String position = element.attributeValue("position");
            if (StringUtils.isNotBlank(position)) {
                if ("left".equals(position) || "right".equals(position)) {
                    option.setPosition(Position.top);
                    option.setAlign(position);
                } else {
                    option.setPosition(Position.valueOf(position));
                }
            }
            String align = element.attributeValue("align");
            if (StringUtils.isNotBlank(align)) {
                option.setAlign(align);
            }
            Element labelsEle = element.element("labels");
            if (labelsEle != null) {
                option.setLabels(parseLabels(labelsEle));
            }
            target = option;
        } else if (type.equals("animation")) {
            AnimationsOption option = new AnimationsOption();
            String duration = element.attributeValue("duration");
            if (StringUtils.isNotBlank(duration)) {
                option.setDuration(Integer.valueOf(duration));
            }
            String easing = element.attributeValue("easing");
            if (StringUtils.isNotBlank(easing)) {
                option.setEasing(Easing.valueOf(easing));
            }
            target = option;
        } else if (type.equals("colors")) {
            ColorsOption option = new ColorsOption();
            List<String> colors = new ArrayList<>();
            for (Object childObj : element.elements()) {
                if (!(childObj instanceof Element)) {
                    continue;
                }
                Element child = (Element) childObj;
                if (!"color".equals(child.getName())) {
                    continue;
                }
                String rgb = child.getTextTrim();
                if (StringUtils.isBlank(rgb)) {
                    rgb = child.attributeValue("rgb");
                }
                if (StringUtils.isBlank(rgb)) {
                    continue;
                }
                String normalized = normalizeRgb(rgb);
                if (normalized != null) {
                    colors.add(normalized);
                }
            }
            option.setColors(colors);
            target = option;
        }
        if (target != null) {
            return target;
        }
        throw new ReportParseException("Unknow option :" + type);
    }

    /**
     * 校验并规范化为 "r,g,b"；非法返回 null
     */
    private String normalizeRgb(String raw) {
        String[] parts = raw.split(",");
        if (parts.length != 3) {
            return null;
        }
        try {
            int r = Integer.parseInt(parts[0].trim());
            int g = Integer.parseInt(parts[1].trim());
            int b = Integer.parseInt(parts[2].trim());
            if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
                return null;
            }
            return r + "," + g + "," + b;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 解析 <labels> 子节点为 Labels 对象
     * 字段缺失时走 Labels 默认值（不影响旧报表）
     * @param element labels XML 元素，不可为空
     * @return Labels 对象
     */
    private Labels parseLabels(Element element) {
        Labels labels = new Labels();
        String boxWidth = element.attributeValue("box-width");
        if (StringUtils.isNotBlank(boxWidth)) {
            labels.setBoxWidth(Integer.valueOf(boxWidth));
        }
        String fontSize = element.attributeValue("font-size");
        if (StringUtils.isNotBlank(fontSize)) {
            labels.setFontSize(Integer.valueOf(fontSize));
        }
        String fontStyle = element.attributeValue("font-style");
        if (StringUtils.isNotBlank(fontStyle)) {
            labels.setFontStyle(com.luck.report.core.chart.FontStyle.valueOf(fontStyle));
        }
        String fontColor = element.attributeValue("font-color");
        if (StringUtils.isNotBlank(fontColor)) {
            labels.setFontColor(fontColor);
        }
        String padding = element.attributeValue("padding");
        if (StringUtils.isNotBlank(padding)) {
            labels.setPadding(Integer.valueOf(padding));
        }
        String itemWidth = element.attributeValue("item-width");
        if (StringUtils.isNotBlank(itemWidth)) {
            labels.setItemWidth(Integer.valueOf(itemWidth));
        }
        String itemHeight = element.attributeValue("item-height");
        if (StringUtils.isNotBlank(itemHeight)) {
            labels.setItemHeight(Integer.valueOf(itemHeight));
        }
        String itemGap = element.attributeValue("item-gap");
        if (StringUtils.isNotBlank(itemGap)) {
            labels.setItemGap(Integer.valueOf(itemGap));
        }
        String fontWeight = element.attributeValue("font-weight");
        if (StringUtils.isNotBlank(fontWeight)) {
            labels.setFontWeight(fontWeight);
        }
        return labels;
    }

    /**
     * 解析 <font> 子节点为 Font 对象（数据标签字体配置）
     * 字段缺失时走 Font 默认值（不影响旧报表）
     * @param element font XML 元素，不可为空
     * @return Font 对象
     */
    private Font parseFont(Element element) {
        Font font = new Font();
        String size = element.attributeValue("size");
        if (StringUtils.isNotBlank(size)) {
            font.setSize(Integer.valueOf(size));
        }
        String color = element.attributeValue("color");
        if (StringUtils.isNotBlank(color)) {
            font.setColor(color);
        }
        String weight = element.attributeValue("weight");
        if (StringUtils.isNotBlank(weight)) {
            font.setWeight(weight);
        }
        return font;
    }

    private void parseAxes(Element element, BaseAxes axes) {
        String rotation = element.attributeValue("rotation");
        if (StringUtils.isNotBlank(rotation)) {
            axes.setRotation(Integer.valueOf(rotation));
        }
        for (Object obj : element.elements()) {
            if (obj == null || !(obj instanceof Element)) {
                continue;
            }
            Element ele = (Element) obj;
            String name = ele.getName();
            if (name.equals("scale-label")) {
                ScaleLabel label = new ScaleLabel();
                String display = ele.attributeValue("display");
                if (StringUtils.isNotBlank(display)) {
                    label.setDisplay(Boolean.valueOf(display));
                }
                String labelString = ele.attributeValue("label-string");
                label.setLabelString(labelString);
                axes.setScaleLabel(label);
                break;
            }
        }
    }

    private Dataset parseDataset(Element element) {
        String type = element.attributeValue("type");
        Dataset dataset = null;
        if (type.equals("area")) {
            AreaDataset ds = new AreaDataset();
            dataset = ds;
        } else if (type.equals("line")) {
            LineDataset ds = new LineDataset();
            dataset = ds;
        } else if (type.equals("bar")) {
            BarDataset ds = new BarDataset();
            dataset = ds;
        } else if (type.equals("doughnut")) {
            DoughnutDataset ds = new DoughnutDataset();
            dataset = ds;
        } else if (type.equals("horizontalBar")) {
            HorizontalBarDataset ds = new HorizontalBarDataset();
            dataset = ds;
        } else if (type.equals("pie")) {
            PieDataset ds = new PieDataset();
            dataset = ds;
        } else if (type.equals("polarArea")) {
            PolarDataset ds = new PolarDataset();
            dataset = ds;
        } else if (type.equals("radar")) {
            RadarDataset ds = new RadarDataset();
            dataset = ds;
        } else if (type.equals("bubble")) {
            BubbleDataset ds = new BubbleDataset();
            String datasetName = element.attributeValue("dataset-name");
            ds.setDatasetName(datasetName);
            ds.setCategoryProperty(element.attributeValue("category-property"));
            ds.setxProperty(element.attributeValue("x-property"));
            ds.setyProperty(element.attributeValue("y-property"));
            ds.setrProperty(element.attributeValue("r-property"));
            dataset = ds;
        } else if (type.equals("scatter")) {
            ScatterDataset ds = new ScatterDataset();
            String datasetName = element.attributeValue("dataset-name");
            ds.setDatasetName(datasetName);
            ds.setCategoryProperty(element.attributeValue("category-property"));
            ds.setxProperty(element.attributeValue("x-property"));
            ds.setyProperty(element.attributeValue("y-property"));
            dataset = ds;
        }
        if (dataset != null && dataset instanceof CategoryDataset) {
            CategoryDataset ds = (CategoryDataset) dataset;
            String datasetName = element.attributeValue("dataset-name");
            ds.setDatasetName(datasetName);
            String format = element.attributeValue("format");
            ds.setFormat(format);
            String categoryProperty = element.attributeValue("category-property");
            ds.setCategoryProperty(categoryProperty);
            String valueProperty = element.attributeValue("value-property");
            ds.setValueProperty(valueProperty);
            String seriesProperty = element.attributeValue("series-property");
            ds.setSeriesProperty(seriesProperty);
            String collectType = element.attributeValue("collect-type");
            if (StringUtils.isNotBlank(collectType)) {
                ds.setCollectType(CollectType.valueOf(collectType));
            }
            String seriesType = element.attributeValue("series-type");
            if (StringUtils.isNotBlank(seriesType)) {
                ds.setSeriesType(SeriesType.valueOf(seriesType));
            }
            ds.setSeriesText(element.attributeValue("series-text"));
        }
        if (dataset != null) {
            return dataset;
        }
        throw new ReportParseException("Unknow chart type : " + type);
    }
}
