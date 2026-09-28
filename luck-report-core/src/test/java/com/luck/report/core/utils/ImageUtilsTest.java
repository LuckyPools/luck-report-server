package com.luck.report.core.utils;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageUtilsTest {

    @Test
    void computeOutputSize_underMax_keepsSourceWhenNoRequest() {
        ImageOutputSize size = ImageUtils.computeOutputSize(800, 600, 0, 0, 1920);
        assertEquals(800, size.getWidth());
        assertEquals(600, size.getHeight());
    }

    @Test
    void computeOutputSize_overMax_capsLongEdgeAndKeepsAspect() {
        ImageOutputSize size = ImageUtils.computeOutputSize(4000, 3000, 0, 0, 1920);
        assertEquals(1920, size.getWidth());
        assertEquals(1440, size.getHeight());
    }

    @Test
    void computeOutputSize_requestedSizeOverMax_isCapped() {
        ImageOutputSize size = ImageUtils.computeOutputSize(4000, 3000, 3000, 2000, 1920);
        assertEquals(1920, size.getWidth());
        assertEquals(1280, size.getHeight());
    }

    @Test
    void computeOutputSize_smallRequest_unchanged() {
        ImageOutputSize size = ImageUtils.computeOutputSize(4000, 3000, 120, 80, 1920);
        assertEquals(120, size.getWidth());
        assertEquals(80, size.getHeight());
    }

    @Test
    void scaleBase64Image_oversizedWithoutTarget_isDownscaledToMaxEdge() throws Exception {
        String base64 = createPngBase64(3000, 2000);
        String scaled = ImageUtils.scaleBase64Image(base64, 0, 0);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(scaled)));
        assertTrue(Math.max(image.getWidth(), image.getHeight()) <= ImageUtils.DEFAULT_MAX_IMAGE_EDGE);
        assertEquals(1920, image.getWidth());
        assertEquals(1280, image.getHeight());
    }

    @Test
    void scaleBase64Image_hugeTarget_isCappedToMaxEdge() throws Exception {
        String base64 = createPngBase64(800, 600);
        String scaled = ImageUtils.scaleBase64Image(base64, 5000, 4000);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(scaled)));
        assertTrue(Math.max(image.getWidth(), image.getHeight()) <= ImageUtils.DEFAULT_MAX_IMAGE_EDGE);
    }

    @Test
    void scaleBase64Image_sameInput_reusesCachedString() throws Exception {
        String base64 = createPngBase64(80, 60);
        ImageLoadStats.begin();
        try {
            String first = ImageUtils.scaleBase64Image(base64, 40, 30);
            String second = ImageUtils.scaleBase64Image(base64, 40, 30);
            assertTrue(first == second);
        } finally {
            ImageLoadStats.end();
        }
    }

    private static String createPngBase64(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, width, height);
        g.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}
