package com.luck.report.core.expression;

import com.luck.report.core.exception.ReportParseException;
import com.luck.report.core.expression.parse.ExpressionParseErrorFormatter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpressionParseErrorMessageTest {

    @Test
    void string_plus_paren_uses_concat_hint() {
        ReportParseException ex = assertThrows(ReportParseException.class,
                () -> ExpressionUtils.parseExpression("var subSql = \" limit \" + (pageNum - 1)"));
        assertEquals(ExpressionParseErrorFormatter.CODE_CONCAT_PAREN, ex.getErrorCode());
        assertEquals(1, ((Number) ex.getErrorArgs()[0]).intValue());
        String snippet = String.valueOf(ex.getErrorArgs()[2]);
        assertTrue(snippet.contains("+"));
        assertTrue(snippet.contains("("));
        assertFalse(ex.getMessage().contains("expecting"));
    }

    @Test
    void salary_sample_points_at_concat_line() {
        String text = "\n"
                + "var sql = \" select * from luck_data_show.year_salary \"\n"
                + "    if(param(\"_i\") != null && param(\"_i\") == ''){\n"
                + "        var pageNum = toNumber(param(\"_i\"))\n"
                + "        var subSql = \" limit \" + (pageNum - 1) + \" \" + (pageNum * 10)\n"
                + "        sql = sql + subSql;\n"
                + "    }\n";
        ReportParseException ex = assertThrows(ReportParseException.class,
                () -> ExpressionUtils.parseExpression(text));
        assertEquals(ExpressionParseErrorFormatter.CODE_CONCAT_PAREN, ex.getErrorCode());
        assertEquals(5, ((Number) ex.getErrorArgs()[0]).intValue());
    }

    @Test
    void variable_concat_still_parses() {
        assertNotNull(ExpressionUtils.parseExpression(
                "var subSql = \" limit \" + offset + \",\" + size; sql = sql + subSql; sql"));
    }

    @Test
    void wrapped_paren_concat_still_parses() {
        assertNotNull(ExpressionUtils.parseExpression(
                "var subSql = (\" limit \" + offset); sql"));
    }

    @Test
    void if_return_still_parses() {
        assertNotNull(ExpressionUtils.parseExpression("if (1==1) { return 1; }"));
    }

    @Test
    void unknown_token_uses_default_without_antlr_dump() {
        ReportParseException ex = assertThrows(ReportParseException.class,
                () -> ExpressionUtils.parseExpression("@@"));
        assertEquals(ExpressionParseErrorFormatter.CODE_DEFAULT, ex.getErrorCode());
        assertFalse(String.valueOf(ex.getMessage()).contains("mismatched"));
        assertFalse(String.valueOf(ex.getMessage()).contains("expecting"));
    }
}
