package com.luck.report.core.config;

import com.luck.report.core.expression.function.*;
import com.luck.report.core.expression.function.date.*;
import com.luck.report.core.expression.function.math.*;
import com.luck.report.core.expression.function.page.*;
import com.luck.report.core.expression.function.string.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FunctionConfiguration {

    // 统计函数
    @Bean("bean.countFunction")
    public CountFunction countFunction() {
        return new CountFunction();
    }

    @Bean("bean.sumFunction")
    public SumFunction sumFunction() {
        return new SumFunction();
    }

    @Bean("bean.maxFunction")
    public MaxFunction maxFunction() {
        return new MaxFunction();
    }

    @Bean("bean.minFunction")
    public MinFunction minFunction() {
        return new MinFunction();
    }

    @Bean("bean.listFunction")
    public ListFunction listFunction() {
        return new ListFunction();
    }

    @Bean("bean.avgFunction")
    public AvgFunction avgFunction() {
        return new AvgFunction();
    }

    @Bean("bean.orderFunction")
    public OrderFunction orderFunction() {
        return new OrderFunction();
    }

    @Bean("bean.ifnFunction")
    public IfnFunction ifnFunction() {
        return new IfnFunction();
    }

    // 日期函数
    @Bean("bean.weekFunction")
    public WeekFunction weekFunction() {
        return new WeekFunction();
    }

    @Bean("bean.dayFunction")
    public DayFunction dayFunction() {
        return new DayFunction();
    }

    @Bean("bean.monthFunction")
    public MonthFunction monthFunction() {
        return new MonthFunction();
    }

    @Bean("bean.yearFunction")
    public YearFunction yearFunction() {
        return new YearFunction();
    }

    @Bean("bean.dateFunction")
    public DateFunction dateFunction() {
        return new DateFunction();
    }

    @Bean("bean.formatDateFunction")
    public FormatDateFunction formatDateFunction() {
        return new FormatDateFunction();
    }

    // 数学函数
    @Bean("bean.absFunction")
    public AbsFunction absFunction() {
        return new AbsFunction();
    }

    @Bean("bean.ceilFunction")
    public CeilFunction ceilFunction() {
        return new CeilFunction();
    }

    @Bean("bean.chnFunction")
    public ChnFunction chnFunction() {
        return new ChnFunction();
    }

    @Bean("bean.chnMoneyFunction")
    public ChnMoneyFunction chnMoneyFunction() {
        return new ChnMoneyFunction();
    }

    @Bean("bean.cosFunction")
    public CosFunction cosFunction() {
        return new CosFunction();
    }

    @Bean("bean.expFunction")
    public ExpFunction expFunction() {
        return new ExpFunction();
    }

    @Bean("bean.floorFunction")
    public FloorFunction floorFunction() {
        return new FloorFunction();
    }

    @Bean("bean.log10Function")
    public Log10Function log10Function() {
        return new Log10Function();
    }

    @Bean("bean.logFunction")
    public LogFunction logFunction() {
        return new LogFunction();
    }

    @Bean("bean.powFunction")
    public PowFunction powFunction() {
        return new PowFunction();
    }

    @Bean("bean.randomFunction")
    public RandomFunction randomFunction() {
        return new RandomFunction();
    }

    @Bean("bean.roundFunction")
    public RoundFunction roundFunction() {
        return new RoundFunction();
    }

    @Bean("bean.sinFunction")
    public SinFunction sinFunction() {
        return new SinFunction();
    }

    @Bean("bean.sqrtFunction")
    public SqrtFunction sqrtFunction() {
        return new SqrtFunction();
    }

    @Bean("bean.tanFunction")
    public TanFunction tanFunction() {
        return new TanFunction();
    }

    @Bean("bean.stdevpFunction")
    public StdevpFunction stdevpFunction() {
        return new StdevpFunction();
    }

    @Bean("bean.varaFunction")
    public VaraFunction varaFunction() {
        return new VaraFunction();
    }

    @Bean("bean.modeFunction")
    public ModeFunction modeFunction() {
        return new ModeFunction();
    }

    @Bean("bean.medianFunction")
    public MedianFunction medianFunction() {
        return new MedianFunction();
    }

    // 字符串函数
    @Bean("bean.lengthFunction")
    public LengthFunction lengthFunction() {
        return new LengthFunction();
    }

    @Bean("bean.lowerFunction")
    public LowerFunction lowerFunction() {
        return new LowerFunction();
    }

    @Bean("bean.indexOfFunction")
    public IndexOfFunction indexOfFunction() {
        return new IndexOfFunction();
    }

    @Bean("bean.replaceFunction")
    public ReplaceFunction replaceFunction() {
        return new ReplaceFunction();
    }

    @Bean("bean.substringFunction")
    public SubstringFunction substringFunction() {
        return new SubstringFunction();
    }

    @Bean("bean.trimFunction")
    public TrimFunction trimFunction() {
        return new TrimFunction();
    }

    @Bean("bean.upperFunction")
    public UpperFunction upperFunction() {
        return new UpperFunction();
    }

    @Bean("bean.toNumberFunction")
    public ToNumberFunction toNumberFunction() {
        return new ToNumberFunction();
    }

    @Bean("bean.leftFunction")
    public LeftFunction leftFunction() {
        return new LeftFunction();
    }

    @Bean("bean.rightFunction")
    public RightFunction rightFunction() {
        return new RightFunction();
    }

    @Bean("bean.splitFunction")
    public SplitFunction splitFunction() {
        return new SplitFunction();
    }

    // 页面函数
    @Bean("bean.pageTotalFunction")
    public PageTotalFunction pageTotalFunction() {
        return new PageTotalFunction();
    }

    @Bean("bean.pageNumberFunction")
    public PageNumberFunction pageNumberFunction() {
        return new PageNumberFunction();
    }

    @Bean("bean.pageAvgFunction")
    public PageAvgFunction pageAvgFunction() {
        return new PageAvgFunction();
    }

    @Bean("bean.pageCountFunction")
    public PageCountFunction pageCountFunction() {
        return new PageCountFunction();
    }

    @Bean("bean.pageMaxFunction")
    public PageMaxFunction pageMaxFunction() {
        return new PageMaxFunction();
    }

    @Bean("bean.pageMinFunction")
    public PageMinFunction pageMinFunction() {
        return new PageMinFunction();
    }

    @Bean("bean.pageRowsFunction")
    public PageRowsFunction pageRowsFunction() {
        return new PageRowsFunction();
    }

    @Bean("bean.pageSumFunction")
    public PageSumFunction pageSumFunction() {
        return new PageSumFunction();
    }

    // 其他函数
    @Bean("bean.formatNumberFunction")
    public FormatNumberFunction formatNumberFunction() {
        return new FormatNumberFunction();
    }

    @Bean("bean.getFunction")
    public GetFunction getFunction() {
        return new GetFunction();
    }

    @Bean("bean.parameterFunction")
    public ParameterFunction parameterFunction() {
        return new ParameterFunction();
    }

    @Bean("bean.parameterIsEmptyFunction")
    public ParameterIsEmptyFunction parameterIsEmptyFunction() {
        return new ParameterIsEmptyFunction();
    }

    @Bean("bean.jsonFunction")
    public JsonFunction jsonFunction() {
        return new JsonFunction();
    }

    @Bean("bean.rowFunction")
    public RowFunction rowFunction() {
        return new RowFunction();
    }

    @Bean("bean.columnFunction")
    public ColumnFunction columnFunction() {
        return new ColumnFunction();
    }

    @Bean("bean.dataRowFunction")
    public DataRowFunction dataRowFunction() {
        return new DataRowFunction();
    }

    @Bean("bean.dataSeqFunction")
    public DataSeqFunction dataSeqFunction() {
        return new DataSeqFunction();
    }
}
