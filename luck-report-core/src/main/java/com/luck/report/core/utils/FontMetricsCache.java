package com.luck.report.core.utils;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 按字体名、样式、字号缓存 FontMetrics，避免折行时反复 new JLabel。
 */
public final class FontMetricsCache {

	private static final Map<String, FontMetrics> CACHE = new ConcurrentHashMap<String, FontMetrics>();
	private static final BufferedImage IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

	private FontMetricsCache() {
	}

	/**
	 * 取字体度量；同键复用
	 *
	 * @param font 字体，可空（空则用宋体 12）
	 * @return FontMetrics，非空
	 */
	public static FontMetrics get(Font font) {
		if (font == null) {
			font = new Font("宋体", Font.PLAIN, 12);
		}
		String key = font.getName() + '|' + font.getStyle() + '|' + font.getSize();
		FontMetrics cached = CACHE.get(key);
		if (cached != null) {
			return cached;
		}
		Graphics2D g = IMAGE.createGraphics();
		try {
			FontMetrics metrics = g.getFontMetrics(font);
			CACHE.put(key, metrics);
			return metrics;
		} finally {
			g.dispose();
		}
	}
}
