package com.luck.report.core.build;

import com.luck.report.core.Utils;

/**
 * 报表计算四段耗时：取数、格子计算（含展开与补空白行）、条件后置、分页。
 */
public class ReportComputeTiming {

	private long datasetMs;
	private long computeMs;
	private long lazyMs;
	private long pagingMs;

	private long markNanos;

	/**
	 * 标记当前阶段起点
	 */
	public void markStart() {
		markNanos = System.nanoTime();
	}

	/**
	 * 结束取数阶段并累加毫秒
	 */
	public void endDataset() {
		datasetMs += elapsedMsAndReset();
	}

	/**
	 * 结束格子计算阶段并累加毫秒
	 */
	public void endCompute() {
		computeMs += elapsedMsAndReset();
	}

	/**
	 * 结束条件后置阶段并累加毫秒
	 */
	public void endLazy() {
		lazyMs += elapsedMsAndReset();
	}

	/**
	 * 结束分页阶段并累加毫秒
	 */
	public void endPaging() {
		pagingMs += elapsedMsAndReset();
	}

	/**
	 * 打一条四段耗时日志
	 */
	public void log() {
		long total = datasetMs + computeMs + lazyMs + pagingMs;
		Utils.logToConsole("Report compute timing: dataset=" + datasetMs + "ms, compute=" + computeMs
				+ "ms, lazy=" + lazyMs + "ms, paging=" + pagingMs + "ms, total=" + total + "ms");
	}

	public long getDatasetMs() {
		return datasetMs;
	}

	public long getComputeMs() {
		return computeMs;
	}

	public long getLazyMs() {
		return lazyMs;
	}

	public long getPagingMs() {
		return pagingMs;
	}

	private long elapsedMsAndReset() {
		long now = System.nanoTime();
		long ms = (now - markNanos) / 1_000_000L;
		markNanos = now;
		return ms;
	}
}
