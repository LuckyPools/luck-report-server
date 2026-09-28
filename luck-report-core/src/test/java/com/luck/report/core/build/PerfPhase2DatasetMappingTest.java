package com.luck.report.core.build;

import com.luck.report.core.definition.ReportDefinition;
import com.luck.report.core.definition.searchform.DatasetOption;
import com.luck.report.core.definition.searchform.SearchForm;
import com.luck.report.core.definition.searchform.component.Component;
import com.luck.report.core.definition.searchform.component.SelectComponent;
import com.luck.report.core.export.ReportRender;
import com.luck.report.core.model.Cell;
import com.luck.report.core.model.Report;
import com.luck.report.core.model.Row;
import com.luck.report.core.parser.ReportParser;
import com.luck.report.core.parser.impl.searchform.FormParserUtils;
import com.luck.report.core.parser.impl.searchform.SelectParser;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 第二期性能优化验收：引用过滤、映射、条件、排序、查询表单绑定。
 */
class PerfPhase2DatasetMappingTest {

	@Test
	void phase2Case_filtersUnusedDatasets_andKeepsResults() throws Exception {
		installFormParsersForUnitTest();

		Path xml = Paths.get("..", "..", "doc", "fixbug", "perf-phase2-dataset-mapping.ureport.xml")
				.toAbsolutePath().normalize();
		ReportDefinition def;
		try (FileInputStream in = new FileInputStream(xml.toFile())) {
			def = new ReportParser().parse(in, xml.toString());
		}
		new ReportRender().rebuildReportDefinition(def);

		assertSearchFormBoundToFormDict(def);

		Set<String> referenced = ReferencedDatasetNames.collect(def);
		assertTrue(referenced.contains("ds_detail"));
		assertTrue(referenced.contains("ds_status_map"));
		assertFalse(referenced.contains("ds_unused_heavy"));
		assertFalse(referenced.contains("ds_form_dict"));

		Report report = new ReportBuilder().buildReport(def, new HashMap<String, Object>());
		assertNotNull(report.getPages());
		assertFalse(report.getPages().isEmpty());

		Map<String, ?> datasetMap = report.getContext().getDatasetMap();
		assertTrue(datasetMap.containsKey("ds_detail"));
		assertTrue(datasetMap.containsKey("ds_status_map"));
		assertFalse(datasetMap.containsKey("ds_unused_heavy"));
		assertFalse(datasetMap.containsKey("ds_form_dict"));

		int detailRows = 0;
		int redAmt = 0;
		Set<Integer> seqs = new HashSet<Integer>();
		for (Row row : report.getRows()) {
			Cell seqCell = findCell(row, "A4");
			if (seqCell == null) {
				continue;
			}
			detailRows++;
			Object seqVal = seqCell.getData();
			assertNotNull(seqVal);
			seqs.add(Integer.valueOf(seqVal.toString()));

			Cell status = findCell(row, "B4");
			Cell statusCopy = findCell(row, "C4");
			assertNotNull(status);
			assertNotNull(statusCopy);
			assertEquals(String.valueOf(status.getFormatData() != null ? status.getFormatData() : status.getData()),
					String.valueOf(statusCopy.getFormatData() != null ? statusCopy.getFormatData() : statusCopy.getData()));
			String statusText = String.valueOf(status.getFormatData() != null ? status.getFormatData() : status.getData());
			assertTrue(statusText.contains("待") || statusText.contains("进行") || statusText.contains("完成")
					|| statusText.contains("关闭") || statusText.contains("驳回")
					|| "待处理".equals(statusText) || "进行中".equals(statusText) || "已完成".equals(statusText)
					|| "已关闭".equals(statusText) || "已驳回".equals(statusText),
					"unexpected status: " + statusText);

			Cell amt = findCell(row, "D4");
			assertNotNull(amt);
			int amtVal = Integer.parseInt(String.valueOf(amt.getData()));
			if (amtVal > 100) {
				assertNotNull(amt.getCustomCellStyle(), "amt>100 should have condition style, amt=" + amtVal);
				assertEquals("255,200,200", amt.getCustomCellStyle().getBgcolor());
				redAmt++;
			}
		}
		assertEquals(40, detailRows);
		assertEquals(40, seqs.size());
		assertTrue(redAmt > 0);
		assertTrue(seqs.contains(1));
		assertTrue(seqs.contains(40));
	}

	/**
	 * 断言案例查询表单含「区域」下拉且绑定 ds_form_dict
	 *
	 * @param def 已解析的报表定义
	 */
	private static void assertSearchFormBoundToFormDict(ReportDefinition def) {
		SearchForm form = def.getSearchForm();
		assertNotNull(form, "search form missing");
		List<Component> fields = form.getFields();
		assertNotNull(fields);
		assertEquals(1, fields.size());
		Component field = fields.get(0);
		assertInstanceOf(SelectComponent.class, field);
		SelectComponent select = (SelectComponent) field;
		assertEquals("区域", select.getLabel());
		assertEquals("dataset", select.getOptionSource());
		assertEquals("colFormItem", select.getLayout());
		DatasetOption option = select.getDatasetOption();
		assertNotNull(option);
		assertEquals("perf_phase2", option.getDatasourceName());
		assertEquals("ds_form_dict", option.getDatasetName());
		assertEquals("name", option.getLabelField());
		assertEquals("code", option.getValueField());
	}

	/**
	 * 单元测试无 Spring 时手动注册 FormParser，便于解析 &lt;form&gt;
	 *
	 * @throws Exception 反射失败时抛出
	 */
	@SuppressWarnings("rawtypes")
	private static void installFormParsersForUnitTest() throws Exception {
		Field parsersField = FormParserUtils.class.getDeclaredField("parsers");
		parsersField.setAccessible(true);
		parsersField.set(null, Collections.singletonList(new SelectParser()));
	}

	private static Cell findCell(Row row, String name) {
		for (Cell cell : row.getCells()) {
			if (name.equals(cell.getName())) {
				return cell;
			}
		}
		return null;
	}
}
