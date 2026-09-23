package com.kiss.yishun.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class QrcodeUtils {
    /**
     * 黑色
     */
    private static final int BLACK = 0xFF000000;
    /**
     * 白色
     */
    private static final int WHITE = 0xFFFFFFFF;
    /**
     * 二维码的宽
     */
    private static final int WIDTH = 360;
    /**
     * 二维码的高
     */
    private static final int HEIGHT = 360;

    /**
     * 二维码传图片为正方形
     *
     * @param matrix
     * @return
     */
    public static BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();

        BufferedImage image = new BufferedImage(width, HEIGHT, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                image.setRGB(x, y,WHITE);
            }
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
            }
        }
        return image;
    }

    /**
     * 生成二维码,生成的是二维码图片
     * @author 谈政扬
     * @date 2018/6/13 11:31
     * @param [content]
     * @return java.awt.image.BufferedImage
     */
    @SuppressWarnings("unchecked")
    public static BufferedImage createQrCode(String content) throws Exception {

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        @SuppressWarnings("rawtypes")
        Map hints = new HashMap();
        // 设置UTF-8， 防止中文乱码
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        // 设置二维码四周白色区域的大小
        hints.put(EncodeHintType.MARGIN, 1);
        // 设置二维码的容错性
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        // 画二维码
        BitMatrix bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hints);
        BufferedImage image = toBufferedImage(bitMatrix);
        return image;
    }

    /**
     * 把二维码图片添加到背景图
     *
     * @param barCodeImage
     * @param logoPic
     */
    public static BufferedImage addLogo_QRCode(BufferedImage erweima, InputStream bgp)throws Exception {

        //读取二维码图片，并构建绘图对象360*360
        //保持二维码是正方形的
        int widtherweima = erweima.getWidth();
        int heighterweima = erweima.getHeight();
        /**
         * 读取背景图片600*900
         */
        BufferedImage bgpi = ImageIO.read(bgp);
        int widthbgpi = bgpi.getWidth();
        int heightbgpi = bgpi.getHeight();
        // 计算图片放置位置
        int x = 120;
        int y = 236;
        //构建背景的图片
        Graphics2D bgpiGraphics = bgpi.createGraphics();
        bgpiGraphics.drawImage(erweima, x, y, widtherweima, heighterweima, null);
        bgpiGraphics.dispose();
        return bgpi;
    }

    /**
         * 将文字写入背景图片中， 生成的图片以流的行式返回
     *
     * @param qrcFile 路径
     * @param qrCodeContent 二维码内容
     * @param pressText 增加的文字
     * @throws Exception
     */
    public static BufferedImage generateQrCode(BufferedImage image, String qrCodeContent, String pressText) throws Exception {

        Graphics g = image.getGraphics();
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        //设置字体，下面商户名称
        Font font=new Font("宋体", Font.PLAIN, 40);
        g.setFont(font);
        g.setColor(Color.white);
        FontMetrics metrics = g.getFontMetrics(font);
        // 文字在图片中的坐标 这里设置在中间
        int startX = (600 - metrics.stringWidth(pressText)) / 2;
        int startY=650;
        g.drawString(pressText, startX, startY);
        g.dispose();
        return image;
    }


}