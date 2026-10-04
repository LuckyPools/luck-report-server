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
package com.luck.report.core.utils;

import com.luck.report.core.exception.ReportComputeException;
import com.luck.report.core.image.ChartImageProcessor;
import com.luck.report.core.image.ImageLoadStats;
import com.luck.report.core.image.ImageOutputSize;
import com.luck.report.core.image.ImageProcessor;
import com.luck.report.core.image.ImageType;
import com.luck.report.core.image.StaticImageProcessor;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Jacky.gao
 * @since 2017年3月20日
 */
public class ImageUtils {
    /**
     * 解码/缩放后长边上限，避免超大图 BufferedImage 占满堆。
     */
    public static final int DEFAULT_MAX_IMAGE_EDGE = 1920;

    private static Map<ImageType, ImageProcessor<?>> imageProcessorMap = new HashMap<ImageType, ImageProcessor<?>>();

    static {
        StaticImageProcessor staticImageProcessor = new StaticImageProcessor();
        imageProcessorMap.put(ImageType.image, staticImageProcessor);
        ChartImageProcessor chartImageProcessor = new ChartImageProcessor();
        imageProcessorMap.put(ImageType.chart, chartImageProcessor);
    }

    public static InputStream base64DataToInputStream(String base64Data) {
        byte[] bytes = Base64.getDecoder().decode(base64Data);
        return new ByteArrayInputStream(bytes);
    }

    /**
     * 根据源图尺寸与请求尺寸计算输出尺寸，并按 maxEdge 限制长边。
     */
    public static ImageOutputSize computeOutputSize(int srcWidth, int srcHeight, int reqWidth, int reqHeight, int maxEdge) {
        int targetWidth = reqWidth > 0 ? reqWidth : srcWidth;
        int targetHeight = reqHeight > 0 ? reqHeight : srcHeight;
        if (targetWidth <= 0) {
            targetWidth = 1;
        }
        if (targetHeight <= 0) {
            targetHeight = 1;
        }
        if (maxEdge > 0) {
            int longEdge = Math.max(targetWidth, targetHeight);
            if (longEdge > maxEdge) {
                double scale = (double) maxEdge / (double) longEdge;
                targetWidth = Math.max(1, (int) Math.round(targetWidth * scale));
                targetHeight = Math.max(1, (int) Math.round(targetHeight * scale));
            }
        }
        return new ImageOutputSize(targetWidth, targetHeight);
    }

    /**
     * 将base64编码的图片按指定宽高缩放后返回新的base64数据。未指定宽高时，若源图超过 {@link #DEFAULT_MAX_IMAGE_EDGE} 仍会等比缩小。
     */
    public static String scaleBase64Image(String base64Data, int width, int height) {
        String cacheKey = "base64|" + width + "x" + height + "|" + Integer.toHexString(base64Data.hashCode())
                + "|" + base64Data.length();
        String cached = getFromCache(cacheKey);
        if (cached != null) {
            return cached;
        }
        try {
            byte[] raw = Base64.getDecoder().decode(base64Data);
            String encoded = encodeImageBytes(raw, width, height);
            putToCache(cacheKey, encoded);
            return encoded;
        } catch (ReportComputeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ReportComputeException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static String getImageBase64Data(ImageType type, Object data, int width, int height) {
        String cacheKey = type + "|" + width + "x" + height + "|" + String.valueOf(data);
        String cached = getFromCache(cacheKey);
        if (cached != null) {
            return cached;
        }
        ImageProcessor<Object> targetProcessor = (ImageProcessor<Object>) imageProcessorMap.get(type);
        if (targetProcessor == null) {
            throw new ReportComputeException("Unknow image type :" + type);
        }
        InputStream inputStream = targetProcessor.getImage(data);
        try {
            byte[] raw = IOUtils.toByteArray(inputStream);
            String encoded = encodeImageBytes(raw, width, height);
            putToCache(cacheKey, encoded);
            return encoded;
        } catch (ReportComputeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ReportComputeException(ex);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private static String getFromCache(String cacheKey) {
        ImageLoadStats stats = ImageLoadStats.current();
        return stats == null ? null : stats.getCachedBase64(cacheKey);
    }

    private static void putToCache(String cacheKey, String base64) {
        ImageLoadStats stats = ImageLoadStats.current();
        if (stats != null) {
            stats.putCachedBase64(cacheKey, base64);
        }
    }

    private static String encodeImageBytes(byte[] raw, int reqWidth, int reqHeight) throws Exception {
        ImageOutputSize dimension = readDimension(raw);
        if (dimension == null) {
            return Base64.getEncoder().encodeToString(raw);
        }
        int srcW = dimension.getWidth();
        int srcH = dimension.getHeight();
        ImageOutputSize outputSize = computeOutputSize(srcW, srcH, reqWidth, reqHeight, DEFAULT_MAX_IMAGE_EDGE);
        int outW = outputSize.getWidth();
        int outH = outputSize.getHeight();
        if (outW == srcW && outH == srcH && reqWidth <= 0 && reqHeight <= 0) {
            return Base64.getEncoder().encodeToString(raw);
        }
        BufferedImage source = readImageWithSubsample(raw, srcW, srcH, outputSize);
        try {
            BufferedImage target = source;
            if (source.getWidth() != outW || source.getHeight() != outH) {
                target = scaleImage(source, outW, outH);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ImageIO.write(target, "png", baos);
                return Base64.getEncoder().encodeToString(baos.toByteArray());
            } finally {
                if (target != source) {
                    target.flush();
                }
            }
        } finally {
            source.flush();
        }
    }

    private static BufferedImage scaleImage(BufferedImage source, int width, int height) {
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = output.createGraphics();
        try {
            g.setComposite(AlphaComposite.SrcOver);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(source, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }
        return output;
    }

    private static ImageOutputSize readDimension(byte[] raw) throws Exception {
        ImageInputStream imageInput = ImageIO.createImageInputStream(new ByteArrayInputStream(raw));
        if (imageInput == null) {
            return null;
        }
        try {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) {
                return null;
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInput, true, true);
                return new ImageOutputSize(reader.getWidth(0), reader.getHeight(0));
            } finally {
                reader.dispose();
            }
        } finally {
            imageInput.close();
        }
    }

    private static BufferedImage readImageWithSubsample(byte[] raw, int srcWidth, int srcHeight, ImageOutputSize outputSize)
            throws Exception {
        ImageInputStream imageInput = ImageIO.createImageInputStream(new ByteArrayInputStream(raw));
        if (imageInput == null) {
            BufferedImage fallback = ImageIO.read(new ByteArrayInputStream(raw));
            if (fallback == null) {
                throw new ReportComputeException("Cannot read image data.");
            }
            return fallback;
        }
        ImageReader reader = null;
        try {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) {
                BufferedImage fallback = ImageIO.read(new ByteArrayInputStream(raw));
                if (fallback == null) {
                    throw new ReportComputeException("Cannot read image data.");
                }
                return fallback;
            }
            reader = readers.next();
            reader.setInput(imageInput, true, true);
            ImageReadParam param = reader.getDefaultReadParam();
            int sample = Math.max(1, (int) Math.ceil(Math.max(
                    (double) srcWidth / (double) outputSize.getWidth(),
                    (double) srcHeight / (double) outputSize.getHeight())));
            if (sample > 1) {
                param.setSourceSubsampling(sample, sample, 0, 0);
            }
            BufferedImage image = reader.read(0, param);
            if (image == null) {
                throw new ReportComputeException("Cannot read image data.");
            }
            return image;
        } finally {
            if (reader != null) {
                reader.dispose();
            }
            imageInput.close();
        }
    }
}
