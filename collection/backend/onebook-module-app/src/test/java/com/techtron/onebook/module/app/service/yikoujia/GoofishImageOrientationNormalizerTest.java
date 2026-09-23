package com.techtron.onebook.module.app.service.yikoujia;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoofishImageOrientationNormalizerTest {

    @Test
    void orientationEightIsBakedIntoLandscapePixelsAndMetadataIsRemoved() throws Exception {
        BufferedImage rawPortrait = new BufferedImage(2, 4, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < rawPortrait.getHeight(); y++) {
            for (int x = 0; x < rawPortrait.getWidth(); x++) {
                rawPortrait.setRGB(x, y, y < 2 ? Color.RED.getRGB() : Color.BLUE.getRGB());
            }
        }
        ByteArrayOutputStream jpeg = new ByteArrayOutputStream();
        ImageIO.write(rawPortrait, "jpg", jpeg);
        byte[] oriented = insertExifOrientation(jpeg.toByteArray(), 8);

        assertEquals(8, GoofishImageOrientationNormalizer.readExifOrientation(oriented));
        byte[] normalized = GoofishImageOrientationNormalizer.normalize(oriented).orElseThrow();
        BufferedImage result = ImageIO.read(new ByteArrayInputStream(normalized));

        assertEquals(4, result.getWidth());
        assertEquals(2, result.getHeight());
        assertEquals(1, GoofishImageOrientationNormalizer.readExifOrientation(normalized));
        assertTrue(red(result.getRGB(0, 0)) > blue(result.getRGB(0, 0)));
        assertTrue(blue(result.getRGB(3, 0)) > red(result.getRGB(3, 0)));
    }

    @Test
    void imageWithoutExifOrientationIsNotReencoded() throws Exception {
        BufferedImage image = new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream jpeg = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", jpeg);

        assertTrue(GoofishImageOrientationNormalizer.normalize(jpeg.toByteArray()).isEmpty());
    }

    private static byte[] insertExifOrientation(byte[] jpeg, int orientation) {
        ByteBuffer exif = ByteBuffer.allocate(36).order(ByteOrder.BIG_ENDIAN);
        exif.put((byte) 0xFF).put((byte) 0xE1).putShort((short) 34);
        exif.put(new byte[]{'E', 'x', 'i', 'f', 0, 0});
        exif.put(new byte[]{'M', 'M'}).putShort((short) 42).putInt(8);
        exif.putShort((short) 1);
        exif.putShort((short) 0x0112).putShort((short) 3).putInt(1);
        exif.putShort((short) orientation).putShort((short) 0);
        exif.putInt(0);

        byte[] output = new byte[jpeg.length + exif.array().length];
        System.arraycopy(jpeg, 0, output, 0, 2);
        System.arraycopy(exif.array(), 0, output, 2, exif.array().length);
        System.arraycopy(jpeg, 2, output, 2 + exif.array().length, jpeg.length - 2);
        return output;
    }

    private static int red(int rgb) {
        return rgb >> 16 & 0xFF;
    }

    private static int blue(int rgb) {
        return rgb & 0xFF;
    }
}
