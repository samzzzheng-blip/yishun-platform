package com.techtron.onebook.module.app.service.yikoujia;

import javax.imageio.ImageIO;
import javax.imageio.IIOImage;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Optional;

/**
 * Normalizes JPEG EXIF orientation before an image is handed to Goofish.
 *
 * <p>Some Goofish image processors rotate the pixels according to EXIF but still use the raw
 * width/height. That stretches a landscape photo whose raw JPEG is portrait. Baking the
 * orientation into the pixels and writing a metadata-free JPEG avoids that ambiguity.</p>
 */
final class GoofishImageOrientationNormalizer {

    private static final int MAX_PIXELS = 20_000_000;
    private static final int JPEG_SOI = 0xD8;
    private static final int JPEG_APP1 = 0xE1;
    private static final int JPEG_SOS = 0xDA;
    private static final int EXIF_ORIENTATION_TAG = 0x0112;

    private GoofishImageOrientationNormalizer() {
    }

    /**
     * Returns a new metadata-free JPEG only when the source has a non-default EXIF orientation.
     */
    static synchronized Optional<byte[]> normalize(byte[] source) throws IOException {
        int orientation = readExifOrientation(source);
        if (orientation <= 1 || orientation > 8) {
            return Optional.empty();
        }

        BufferedImage input = ImageIO.read(new ByteArrayInputStream(source));
        if (input == null) {
            throw new IOException("Unsupported image data");
        }
        long pixels = (long) input.getWidth() * input.getHeight();
        if (pixels > MAX_PIXELS) {
            throw new IOException("Image is too large to normalize safely");
        }

        BufferedImage output = applyOrientation(input, orientation);
        return Optional.of(writeJpeg(output, source.length));
    }

    static int readExifOrientation(byte[] jpeg) {
        if (jpeg == null || jpeg.length < 4 || unsigned(jpeg[0]) != 0xFF || unsigned(jpeg[1]) != JPEG_SOI) {
            return 1;
        }
        int offset = 2;
        while (offset + 4 <= jpeg.length) {
            if (unsigned(jpeg[offset]) != 0xFF) {
                offset++;
                continue;
            }
            int marker = unsigned(jpeg[offset + 1]);
            offset += 2;
            if (marker == JPEG_SOS || marker == 0xD9) {
                break;
            }
            if (marker == 0x00 || marker == JPEG_SOI || marker >= 0xD0 && marker <= 0xD7) {
                continue;
            }
            if (offset + 2 > jpeg.length) {
                break;
            }
            int segmentLength = readUnsignedShort(jpeg, offset, ByteOrder.BIG_ENDIAN);
            if (segmentLength < 2 || offset + segmentLength > jpeg.length) {
                break;
            }
            if (marker == JPEG_APP1 && segmentLength >= 14
                    && matches(jpeg, offset + 2, new byte[]{'E', 'x', 'i', 'f', 0, 0})) {
                int orientation = readTiffOrientation(jpeg, offset + 8, segmentLength - 8);
                if (orientation >= 1 && orientation <= 8) {
                    return orientation;
                }
            }
            offset += segmentLength;
        }
        return 1;
    }

    private static int readTiffOrientation(byte[] data, int tiffStart, int tiffLength) {
        if (tiffLength < 8 || tiffStart < 0 || tiffStart + tiffLength > data.length) {
            return 1;
        }
        ByteOrder order;
        if (data[tiffStart] == 'I' && data[tiffStart + 1] == 'I') {
            order = ByteOrder.LITTLE_ENDIAN;
        } else if (data[tiffStart] == 'M' && data[tiffStart + 1] == 'M') {
            order = ByteOrder.BIG_ENDIAN;
        } else {
            return 1;
        }
        if (readUnsignedShort(data, tiffStart + 2, order) != 42) {
            return 1;
        }
        long ifdRelative = readUnsignedInt(data, tiffStart + 4, order);
        long ifdLong = tiffStart + ifdRelative;
        if (ifdRelative < 0 || ifdLong < tiffStart || ifdLong + 2 > tiffStart + tiffLength) {
            return 1;
        }
        int ifd = (int) ifdLong;
        int entries = readUnsignedShort(data, ifd, order);
        for (int index = 0; index < entries; index++) {
            long entryLong = (long) ifd + 2L + index * 12L;
            if (entryLong + 12 > tiffStart + tiffLength) {
                return 1;
            }
            int entry = (int) entryLong;
            int tag = readUnsignedShort(data, entry, order);
            if (tag == EXIF_ORIENTATION_TAG) {
                int type = readUnsignedShort(data, entry + 2, order);
                long count = readUnsignedInt(data, entry + 4, order);
                return type == 3 && count >= 1 ? readUnsignedShort(data, entry + 8, order) : 1;
            }
        }
        return 1;
    }

    private static BufferedImage applyOrientation(BufferedImage input, int orientation) {
        int width = input.getWidth();
        int height = input.getHeight();
        boolean swapsDimensions = orientation >= 5;
        BufferedImage output = new BufferedImage(swapsDimensions ? height : width,
                swapsDimensions ? width : height, BufferedImage.TYPE_INT_RGB);

        AffineTransform transform = switch (orientation) {
            case 2 -> new AffineTransform(-1, 0, 0, 1, width, 0);
            case 3 -> new AffineTransform(-1, 0, 0, -1, width, height);
            case 4 -> new AffineTransform(1, 0, 0, -1, 0, height);
            case 5 -> new AffineTransform(0, 1, 1, 0, 0, 0);
            case 6 -> new AffineTransform(0, 1, -1, 0, height, 0);
            case 7 -> new AffineTransform(0, -1, -1, 0, height, width);
            case 8 -> new AffineTransform(0, -1, 1, 0, 0, width);
            default -> new AffineTransform();
        };

        Graphics2D graphics = output.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, output.getWidth(), output.getHeight());
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.drawImage(input, transform, null);
        } finally {
            graphics.dispose();
        }
        return output;
    }

    private static byte[] writeJpeg(BufferedImage image, int expectedSize) throws IOException {
        var writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IOException("JPEG encoder is unavailable");
        }
        ImageWriter writer = writers.next();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(Math.max(expectedSize, 32 * 1024));
        try (ImageOutputStream output = ImageIO.createImageOutputStream(bytes)) {
            writer.setOutput(output);
            ImageWriteParam params = writer.getDefaultWriteParam();
            if (params.canWriteCompressed()) {
                params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                params.setCompressionQuality(0.95f);
            }
            writer.write(null, new IIOImage(image, null, null), params);
        } finally {
            writer.dispose();
        }
        return bytes.toByteArray();
    }

    private static boolean matches(byte[] source, int offset, byte[] expected) {
        if (offset < 0 || offset + expected.length > source.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if (source[offset + i] != expected[i]) {
                return false;
            }
        }
        return true;
    }

    private static int readUnsignedShort(byte[] source, int offset, ByteOrder order) {
        if (offset < 0 || offset + 2 > source.length) {
            return 0;
        }
        return Short.toUnsignedInt(ByteBuffer.wrap(source, offset, 2).order(order).getShort());
    }

    private static long readUnsignedInt(byte[] source, int offset, ByteOrder order) {
        if (offset < 0 || offset + 4 > source.length) {
            return -1;
        }
        return Integer.toUnsignedLong(ByteBuffer.wrap(source, offset, 4).order(order).getInt());
    }

    private static int unsigned(byte value) {
        return Byte.toUnsignedInt(value);
    }
}
