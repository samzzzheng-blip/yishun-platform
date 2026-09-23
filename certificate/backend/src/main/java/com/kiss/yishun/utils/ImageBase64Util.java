package com.kiss.yishun.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

public class ImageBase64Util {

    /**
     * 将URL路径对应的本地图片转为Base64（带 data:image/...;base64, 前缀）
     * @param imageUrl 图片URL，如 http://xxxx/upload/tmp/20251108/xxx.jpg
     * @param uploadRoot 本地upload根目录路径，如 /data/www/upload
     */
    public static String localTmpImageUrlToBase64(String imageUrl, String uploadRoot) throws IOException {
        // 提取 tmp 后面的路径部分，例如 tmp/20251108/xxx.jpg
        int tmpIndex = imageUrl.indexOf("/tmp/");
        if (tmpIndex == -1) {
            throw new IllegalArgumentException("无法解析文件");
        }

        String relativePath = imageUrl.substring(tmpIndex + 5); // 取 tmp 后面的路径
        File file = new File(uploadRoot + "tmp/" + relativePath); // 拼出本地绝对路径

        if (!file.exists()) {
            throw new IOException("文件不存在：" + file.getAbsolutePath());
        }

        byte[] bytes = Files.readAllBytes(file.toPath());
        String suffix = getSuffix(file.getName());
        String base64 = Base64.getEncoder().encodeToString(bytes);

        // 带 data:image/...;base64, 前缀
        return "data:image/" + suffix + ";base64," + base64;
    }

    /** 根据文件名后缀判断类型 */
    private static String getSuffix(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return "png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "jpeg";
        if (lower.endsWith(".webp")) return "webp";
        return "jpeg"; // 默认
    }
}
