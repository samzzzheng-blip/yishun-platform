package com.kiss.yishun.service;

import com.kiss.yishun.config.UploadConfig;
import com.kiss.yishun.entity.Precious;
import com.kiss.yishun.entity.vo.RateImportResult;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.*;
import java.util.*;

/** 宝贝管理专用导入；不调用编辑/删除照片的逻辑。 */
@Service
public class PreciousImportService {
    public static final String[] HEADERS = {"证书编号", "照片", "签名人", "载体", "卡品", "活动地址",
            "活动时间", "亲笔签名", "证据图片", "证据视频", "评级", "备注"};
    private static final int[] LIMITS = {100, 200, 100, 50, 200, 100, 50, 100, 200, 200, 50, 500};
    @Autowired private PreciousService preciousService;
    @Autowired private UploadConfig uploadConfig;

    public RateImportResult importFile(MultipartFile file, String operator) throws Exception {
        return importFile(file, operator, Collections.emptyMap());
    }

    public List<String> attachmentReferences(MultipartFile file) throws Exception {
        checkExcel(file);
        Set<String> refs = new LinkedHashSet<>();
        try (InputStream in = file.getInputStream(); Workbook book = WorkbookFactory.create(in)) {
            Sheet sheet = book.getSheetAt(0);
            DataFormatter formatter = new DataFormatter(Locale.CHINA);
            Map<Integer, Integer> cols = columns(sheet.getRow(sheet.getFirstRowNum()), formatter);
            if (sheet.getLastRowNum() - sheet.getFirstRowNum() > 5000) throw new IllegalArgumentException("每次最多导入 5000 行");
            PreciousCellImages.read(book, sheet, cols);
            FormulaEvaluator evaluator = book.getCreationHelper().createFormulaEvaluator();
            for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                for (Map.Entry<Integer, Integer> col : cols.entrySet()) {
                    if (!isMedia(col.getValue())) continue;
                    Cell cell = row.getCell(col.getKey());
                    if (cell == null) continue;
                    if (PreciousCellImages.isImage(cell)) continue;
                    String value = cell.getHyperlink() == null ? formatter.formatCellValue(cell, evaluator) : cell.getHyperlink().getAddress();
                    value = value == null ? "" : value.trim();
                    if (!value.isEmpty() && !remoteMedia(value)) refs.add(value);
                }
            }
        }
        return new ArrayList<>(refs);
    }

    private void checkExcel(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (file.isEmpty() || file.getSize() > 20 * 1024 * 1024 || name == null
                || !name.toLowerCase(Locale.ROOT).matches(".*\\.xlsx?")) {
            throw new IllegalArgumentException("请选择不超过 20MB 的 .xls 或 .xlsx 文件");
        }
    }

    private boolean remoteMedia(String value) {
        return value.matches("(?i)^(https?://[^\\s]+|/upload/[^\\s]+)$");
    }

    public RateImportResult importFile(MultipartFile file, String operator, Map<String, MultipartFile> attachments) throws Exception {
        checkExcel(file);
        long bytes = file.getSize();
        for (MultipartFile attachment : attachments.values()) bytes += attachment.getSize();
        if (bytes > 90L * 1024 * 1024) throw new IllegalArgumentException("Excel 与附件合计不能超过 90MB，请分批导入");
        RateImportResult result = new RateImportResult();
        Set<String> seen = new HashSet<>();
        try (InputStream in = file.getInputStream(); Workbook book = WorkbookFactory.create(in)) {
            // 与旧导入一致，只读取第一个工作表，避免将说明页作为商品写入。
            Sheet sheet = book.getSheetAt(0);
            DataFormatter formatter = new DataFormatter(Locale.CHINA);
            FormulaEvaluator evaluator = book.getCreationHelper().createFormulaEvaluator();
            Row header = sheet.getRow(sheet.getFirstRowNum());
            Map<Integer, Integer> columns = columns(header, formatter);
            Map<String, PictureData> pictures = pictures(sheet);
            for (Map.Entry<String, PictureData> image : PreciousCellImages.read(book, sheet, columns).entrySet()) {
                if (pictures.put(image.getKey(), image.getValue()) != null)
                    throw new IllegalArgumentException("同一单元格同时存在浮动图片和 WPS 图片，请只保留一种");
            }
            if (sheet.getLastRowNum() - header.getRowNum() > 5000) {
                throw new IllegalArgumentException("每次最多导入 5000 行");
            }
            for (int r = header.getRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                String[] values = new String[HEADERS.length];
                Arrays.fill(values, "");
                boolean hasData = false;
                for (Integer col : columns.keySet()) {
                    Cell cell = row == null ? null : row.getCell(col);
                    if ((cell != null && cell.getCellTypeEnum() != CellType.BLANK
                            && !cell.toString().trim().isEmpty()) || pictures.containsKey(r + ":" + col)) hasData = true;
                }
                if (!hasData) continue;
                result.setTotal(result.getTotal() + 1);
                try {
                    for (Map.Entry<Integer, Integer> col : columns.entrySet()) {
                        Cell cell = row == null ? null : row.getCell(col.getKey());
                        if (cell == null) continue;
                        if (PreciousCellImages.isImage(cell)) continue;
                        String value = cell.getHyperlink() != null && isMedia(col.getValue())
                                ? cell.getHyperlink().getAddress() : formatter.formatCellValue(cell, evaluator);
                        values[col.getValue()] = value == null ? "" : value.trim();
                    }
                    if (values[0].isEmpty()) throw new IllegalArgumentException("证书编号不能为空");
                    // 编号还用于系统其他操作的目录名，禁止路径穿越及 Windows 特殊路径。
                    if (values[0].matches(".*[\\\\/:*?\"<>|\\p{Cntrl}].*") || values[0].equals(".")
                            || values[0].contains("..") || values[0].endsWith(".")
                            || values[0].matches("(?i)(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(\\..*)?")) {
                        throw new IllegalArgumentException("证书编号包含不安全的路径字符");
                    }
                    for (int i = 0; i < values.length; i++) {
                        if (!(isMedia(i) && attachments.containsKey(values[i])) && values[i].length() > LIMITS[i]) throw new IllegalArgumentException(HEADERS[i] + "超过 " + LIMITS[i] + " 字符");
                        if (isMedia(i) && !values[i].isEmpty() && !remoteMedia(values[i])) {
                            MultipartFile attachment = attachments.get(values[i]);
                            if (attachment == null) throw new IllegalArgumentException(HEADERS[i] + "未匹配到本地附件：" + values[i]);
                            attachmentExtension(attachment, i == 9);
                        }
                    }
                    String key = values[0].toLowerCase(Locale.ROOT);
                    if (!seen.add(key) || Integer.valueOf(1).equals(preciousService.existSameCertNumber(values[0]))) {
                        throw new IllegalArgumentException("证书编号重复，未覆盖已有记录");
                    }
                    // 验证两列图片后再写入；非照片列的图片不会被误配。
                    for (Map.Entry<Integer, Integer> col : columns.entrySet()) {
                        if (col.getValue() != 1 && col.getValue() != 8) continue;
                        PictureData pic = pictures.get(r + ":" + col.getKey());
                        if (pic != null) {
                            if (!values[col.getValue()].isEmpty()) throw new IllegalArgumentException(HEADERS[col.getValue()] + "同时存在地址和嵌入图片，请只保留一种");
                            validatePicture(pic);
                        }
                    }
                    for (Map.Entry<Integer, Integer> col : columns.entrySet()) {
                        if (col.getValue() != 1 && col.getValue() != 8) continue;
                        PictureData pic = pictures.get(r + ":" + col.getKey());
                        if (pic != null) values[col.getValue()] = storePicture(values[0], col.getValue() == 1 ? "img" : "evidenceImg", pic);
                    }
                    for (int i : new int[]{1, 8, 9}) {
                        if (!values[i].isEmpty() && !remoteMedia(values[i])) {
                            MultipartFile attachment = attachments.get(values[i]);
                            String ext = attachmentExtension(attachment, i == 9);
                            Path target = pictureTarget(values[0], i == 1 ? "img" : i == 8 ? "evidenceImg" : "evidenceVideo", ext);
                            try (InputStream input = attachment.getInputStream()) { Files.copy(input, target); }
                            values[i] = pictureUrl(target);
                        }
                    }
                    Precious p = new Precious();
                    p.setCertNumber(values[0]); p.setImgUrl(values[1]); p.setSigner(values[2]);
                    p.setItemType(values[3]); p.setPublishActivity(values[4]); p.setPublishCity(values[5]);
                    p.setPublishTime(values[6]); p.setPublishSign(values[7]); p.setEvidenceImg(values[8]);
                    p.setEvidenceVideo(values[9]); p.setScore(values[10]); p.setRemark(values[11]);
                    p.setEvidenceVideoImgUrl(""); p.setOperator(operator); p.setStatus(0);
                    p.setCreatedate(System.currentTimeMillis()); p.setUpdatedate(p.getCreatedate());
                    preciousService.addPrecious(p);
                    result.setImported(result.getImported() + 1);
                } catch (IllegalArgumentException e) {
                    result.addError("第 " + (r + 1) + " 行（" + values[0] + "）：" + e.getMessage());
                } catch (Exception e) {
                    result.addError("第 " + (r + 1) + " 行（" + values[0] + "）：保存失败，未计入成功，请检查服务日志");
                    org.slf4j.LoggerFactory.getLogger(getClass()).error("Precious import row {} failed", r + 1, e);
                }
            }
        }
        if (result.getTotal() == 0) throw new IllegalArgumentException("没有可导入的数据行");
        return result;
    }

    private boolean isMedia(int index) { return index == 1 || index == 8 || index == 9; }

    private Map<Integer, Integer> columns(Row row, DataFormatter formatter) {
        if (row == null) throw new IllegalArgumentException("第一行必须为列名");
        Map<String, Integer> names = new HashMap<>();
        for (int i = 0; i < HEADERS.length; i++) names.put(HEADERS[i], i);
        names.put("编号", 0); names.put("姓名", 2); names.put("活动", 4); names.put("卡号", 4);
        names.put("地点", 5); names.put("时间", 6); names.put("证据-图片", 8); names.put("证据-视频", 9);
        Map<Integer, Integer> columns = new LinkedHashMap<>();
        for (Cell cell : row) {
            Integer field = names.get(formatter.formatCellValue(cell).trim());
            if (field != null) {
                if (columns.containsValue(field)) throw new IllegalArgumentException("重复的表头：" + HEADERS[field]);
                columns.put(cell.getColumnIndex(), field);
            }
        }
        if (!columns.containsValue(0)) throw new IllegalArgumentException("第一行必须包含“证书编号”列");
        return columns;
    }

    private Map<String, PictureData> pictures(Sheet sheet) {
        Map<String, PictureData> result = new HashMap<>();
        if (sheet instanceof XSSFSheet) {
            XSSFDrawing drawing = ((XSSFSheet) sheet).getDrawingPatriarch();
            if (drawing != null) for (XSSFShape shape : drawing.getShapes()) {
                if (shape instanceof XSSFPicture) {
                    XSSFPicture picture = (XSSFPicture) shape;
                    ClientAnchor anchor = picture.getClientAnchor();
                    if (anchor != null) putPicture(result, anchor, picture.getPictureData());
                }
            }
        } else if (sheet instanceof HSSFSheet) {
            HSSFPatriarch drawing = ((HSSFSheet) sheet).getDrawingPatriarch();
            if (drawing != null) for (HSSFShape shape : drawing.getChildren()) {
                if (shape instanceof HSSFPicture && shape.getAnchor() instanceof ClientAnchor)
                    putPicture(result, (ClientAnchor) shape.getAnchor(), ((HSSFPicture) shape).getPictureData());
            }
        }
        return result;
    }

    private void putPicture(Map<String, PictureData> result, ClientAnchor anchor, PictureData data) {
        if (result.put(anchor.getRow1() + ":" + anchor.getCol1(), data) != null)
            throw new IllegalArgumentException("第 " + (anchor.getRow1() + 1) + " 行同一单元格存在多张图片，请只保留一张");
    }

    private void validatePicture(PictureData pic) {
        if (pic.getData().length > 10 * 1024 * 1024 || pic.getData().length == 0
                || !pic.suggestFileExtension().matches("(?i)png|jpe?g|gif"))
            throw new IllegalArgumentException("嵌入图片须为不超过 10MB 的 PNG/JPG/GIF");
    }

    private String storePicture(String cert, String kind, PictureData pic) throws Exception {
        Path target = pictureTarget(cert, kind, pic.suggestFileExtension());
        Files.write(target, pic.getData(), StandardOpenOption.CREATE_NEW);
        return pictureUrl(target);
    }

    private String attachmentExtension(MultipartFile file, boolean video) {
        String name = file.getOriginalFilename();
        String ext = name == null || !name.contains(".") ? "" : name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (file.isEmpty() || file.getSize() > (video ? 80L : 10L) * 1024 * 1024
                || !ext.matches(video ? "mp4|mov|webm|m4v" : "jpg|jpeg|png|gif"))
            throw new IllegalArgumentException(video ? "视频须为 MP4/MOV/WebM/M4V 且不超过 80MB" : "照片须为 JPG/PNG/GIF 且不超过 10MB");
        return ext;
    }

    private String pictureUrl(Path target) throws Exception {
        Path root = Paths.get(uploadConfig.getDiskPreciousDir()).toRealPath();
        return uploadConfig.getReturnPreciousDir().replaceAll("/+$", "") + "/" + root.relativize(target).toString().replace('\\', '/');
    }

    private Path pictureTarget(String cert, String kind, String extension) throws Exception {
        Path root = Paths.get(uploadConfig.getDiskPreciousDir()).toRealPath();
        Path dir = root.resolve(cert).resolve(kind).normalize();
        if (!dir.startsWith(root) || dir.equals(root)) throw new IllegalArgumentException("无效图片目录");
        for (Path p = dir; p != null && !p.equals(root); p = p.getParent()) {
            if (Files.isSymbolicLink(p)) throw new IllegalArgumentException("图片目录不能是符号链接");
        }
        String filename = "excel-" + UUID.randomUUID() + "." + extension;
        String prefix = uploadConfig.getReturnPreciousDir().replaceAll("/+$", "");
        String url = prefix + "/" + cert + "/" + kind + "/" + filename;
        if (url.length() > 200) throw new IllegalArgumentException("图片地址过长");
        Files.createDirectories(dir);
        return dir.resolve(filename);
    }
}
