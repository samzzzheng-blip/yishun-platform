package com.kiss.yishun.service;

import com.kiss.yishun.config.UploadConfig;
import com.kiss.yishun.entity.Rate;
import com.kiss.yishun.entity.vo.RateImportResult;
import com.kiss.yishun.entity.vo.RateImportRow;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFShape;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class RateImportService {
    private static final int MAX_FILE_SIZE = 20 * 1024 * 1024;
    private static final int MAX_IMAGE_SIZE = 10 * 1024 * 1024;
    private static final int MAX_ROWS = 5000;
    private static final Pattern WPS_DISPIMG_PATTERN = Pattern.compile("(?i)DISPIMG\\s*\\(\\s*\"([^\"]+)\"");

    private static final String CERT_NUMBER = "certNumber";
    private static final String RATE_NAME = "rateName";
    private static final String SURFACE = "surface";
    private static final String CENTER = "center";
    private static final String EDGE = "edge";
    private static final String CORNER = "corner";
    private static final String SCORE = "score";
    private static final String IMG_URL = "imgUrl";
    private static final String REMARK = "remark";

    private static final Map<String, String> HEADER_ALIASES = new HashMap<>();

    static {
        addAliases(CERT_NUMBER, "编号", "证书编号", "certnumber", "certno");
        addAliases(RATE_NAME, "标签", "名称", "卡片名称", "ratename", "name");
        addAliases(SURFACE, "表面", "surface");
        addAliases(CENTER, "居中", "中心", "center", "centering");
        addAliases(EDGE, "边缘", "edge", "edges");
        addAliases(CORNER, "角落", "corner", "corners");
        addAliases(SCORE, "总分", "总评分", "评分", "score", "grade");
        addAliases(IMG_URL, "照片", "图片", "照片地址", "图片地址", "imgurl", "image", "photo");
        addAliases(REMARK, "备注", "remark", "note");
    }

    @Autowired
    private RateService rateService;

    @Autowired
    private UploadConfig uploadConfig;

    private final AtomicInteger imageSequence = new AtomicInteger();

    public RateImportResult importFile(MultipartFile file, String operator) throws Exception {
        return saveRows(parseFile(file), operator);
    }

    /** Parsing only: temporary imports must never call the formal Rate save path. */
    public List<RateImportRow> parseFile(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择导入文件");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("导入文件不能超过20MB");
        }
        String fileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String lowerName = fileName.toLowerCase(Locale.ROOT);
        List<RateImportRow> rows;
        if (lowerName.endsWith(".xls") || lowerName.endsWith(".xlsx")) {
            rows = parseExcel(file);
        } else if (lowerName.endsWith(".xml")) {
            rows = parseXml(file);
        } else {
            throw new IllegalArgumentException("仅支持.xls、.xlsx或.xml文件");
        }
        if (rows.size() > MAX_ROWS) {
            throw new IllegalArgumentException("单次最多导入5000条数据");
        }
        return rows;
    }

    private RateImportResult saveRows(List<RateImportRow> rows, String operator) {
        RateImportResult result = new RateImportResult();
        result.setTotal(rows.size());
        Set<String> certNumbers = new HashSet<>();
        for (RateImportRow row : rows) {
            String validationError = validate(row);
            if (validationError != null) {
                result.addError(row.getSource() + "：" + validationError);
                continue;
            }
            String certNumber = clean(row.getCertNumber());
            if (!isBlank(certNumber) && !certNumbers.add(certNumber)) {
                result.addError(row.getSource() + "：文件内编号重复（" + certNumber + "）");
                continue;
            }
            if (!isBlank(certNumber) && Integer.valueOf(1).equals(rateService.existSameCertNumber(certNumber))) {
                result.addError(row.getSource() + "：系统中已有编号（" + certNumber + "）");
                continue;
            }
            try {
                Rate rate = new Rate();
                rate.setCertNumber(certNumber);
                rate.setRateName(clean(row.getRateName()));
                rate.setSurface(clean(row.getSurface()));
                rate.setCenter(clean(row.getCenter()));
                rate.setEdge(clean(row.getEdge()));
                rate.setCorner(clean(row.getCorner()));
                rate.setScore(clean(row.getScore()));
                rate.setRemark(clean(row.getRemark()));
                rate.setImgUrl(resolveImage(row, certNumber));
                rate.setOperator(operator);
                long now = System.currentTimeMillis();
                rate.setCreatedate(now);
                rate.setUpdatedate(now);
                rateService.addRate(rate);
                result.setImported(result.getImported() + 1);
            } catch (IllegalArgumentException e) {
                result.addError(row.getSource() + "：" + safeMessage(e));
            } catch (Exception e) {
                result.addError(row.getSource() + "：保存失败，请检查字段长度或数据格式");
            }
        }
        return result;
    }

    private String validate(RateImportRow row) {
        if (clean(row.getCertNumber()).length() > 100 || clean(row.getRateName()).length() > 100) {
            return "编号或名称长度超过100个字符";
        }
        return null;
    }

    private String resolveImage(RateImportRow row, String certNumber) throws Exception {
        String value = clean(row.getImgUrl());
        if (value.startsWith("data:image/")) {
            int comma = value.indexOf(',');
            if (comma < 0 || !value.substring(0, comma).contains(";base64")) {
                throw new IllegalArgumentException("照片Base64格式不正确");
            }
            String media = value.substring(11, value.indexOf(';', 11));
            return saveImage(certNumber, Base64.getDecoder().decode(value.substring(comma + 1)), media);
        }
        if (value.startsWith("http://") || value.startsWith("https://") || value.startsWith("/upload/")) {
            if (value.length() > 200) {
                throw new IllegalArgumentException("照片地址不能超过200个字符");
            }
            return value;
        }
        if (row.getEmbeddedImage() != null) {
            return saveImage(certNumber, row.getEmbeddedImage(), row.getEmbeddedImageExtension());
        }
        if (!isBlank(value)) {
            throw new IllegalArgumentException("照片需填写网址、系统图片路径或嵌入Excel图片");
        }
        return "";
    }

    private String saveImage(String certNumber, byte[] data, String extension) throws Exception {
        if (data == null || data.length == 0 || data.length > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("照片为空或超过10MB");
        }
        String ext = normalizeImageExtension(extension);
        String safeCertNumber = certNumber.replaceAll("[^A-Za-z0-9_-]", "_");
        if (safeCertNumber.isEmpty()) {
            throw new IllegalArgumentException("编号不能用于保存照片");
        }
        Path imageDir = Paths.get(uploadConfig.getDiskPreciousDir(), safeCertNumber, "img");
        Files.createDirectories(imageDir);
        String fileName = "import_" + System.currentTimeMillis() + "_" + imageSequence.incrementAndGet() + "." + ext;
        Files.write(imageDir.resolve(fileName), data);
        String base = uploadConfig.getReturnPreciousDir();
        if (!base.endsWith("/")) {
            base += "/";
        }
        return base + safeCertNumber + "/img/" + fileName;
    }

    private String normalizeImageExtension(String extension) {
        String ext = clean(extension).toLowerCase(Locale.ROOT).replace(".", "");
        if ("jpeg".equals(ext)) {
            return "jpg";
        }
        if ("png".equals(ext) || "jpg".equals(ext) || "gif".equals(ext) || "bmp".equals(ext) || "webp".equals(ext)) {
            return ext;
        }
        throw new IllegalArgumentException("不支持的照片格式：" + ext);
    }

    private List<RateImportRow> parseExcel(MultipartFile file) throws Exception {
        List<RateImportRow> rows = new ArrayList<>();
        byte[] fileBytes = file.getBytes();
        Map<String, ImportedPicture> wpsCellImages = readWpsCellImages(fileBytes);
        try (InputStream input = new ByteArrayInputStream(fileBytes); Workbook workbook = WorkbookFactory.create(input)) {
            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                int headerRowIndex = findHeaderRow(sheet, formatter, evaluator);
                if (headerRowIndex < 0) {
                    continue;
                }
                Map<Integer, String> columns = readHeaders(sheet.getRow(headerRowIndex), formatter, evaluator);
                Map<Integer, ImportedPicture> pictures = readPictures(sheet);
                for (int rowIndex = headerRowIndex + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row excelRow = sheet.getRow(rowIndex);
                    if (excelRow == null) {
                        continue;
                    }
                    RateImportRow row = new RateImportRow();
                    row.setSource(sheet.getSheetName() + " 第" + (rowIndex + 1) + "行");
                    for (Map.Entry<Integer, String> column : columns.entrySet()) {
                        Cell cell = excelRow.getCell(column.getKey());
                        if (IMG_URL.equals(column.getValue())) {
                            ImportedPicture cellImage = wpsCellImages.get(wpsImageId(cell));
                            if (cellImage != null) {
                                row.setEmbeddedImage(cellImage.data);
                                row.setEmbeddedImageExtension(cellImage.extension);
                            }
                        }
                        String value = cellValue(cell, formatter, evaluator, IMG_URL.equals(column.getValue()));
                        setValue(row, column.getValue(), value);
                    }
                    ImportedPicture picture = pictures.get(rowIndex);
                    if (picture != null && row.getEmbeddedImage() == null) {
                        row.setEmbeddedImage(picture.data);
                        row.setEmbeddedImageExtension(picture.extension);
                    }
                    if (!isEmptyRow(row)) {
                        rows.add(row);
                        if (rows.size() > MAX_ROWS) {
                            return rows;
                        }
                    }
                }
            }
        }
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("未找到可导入数据，请检查表头名称和工作表内容");
        }
        return rows;
    }

    private int findHeaderRow(Sheet sheet, DataFormatter formatter, FormulaEvaluator evaluator) {
        int max = Math.min(sheet.getLastRowNum(), 20);
        for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= max; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            int recognized = readHeaders(row, formatter, evaluator).size();
            if (recognized >= 4) {
                return rowIndex;
            }
        }
        return -1;
    }

    private Map<Integer, String> readHeaders(Row row, DataFormatter formatter, FormulaEvaluator evaluator) {
        Map<Integer, String> headers = new HashMap<>();
        if (row == null) {
            return headers;
        }
        for (Cell cell : row) {
            String field = HEADER_ALIASES.get(normalize(formatter.formatCellValue(cell, evaluator)));
            if (field != null) {
                headers.put(cell.getColumnIndex(), field);
            }
        }
        return headers;
    }

    private String cellValue(Cell cell, DataFormatter formatter, FormulaEvaluator evaluator, boolean preferHyperlink) {
        if (cell == null) {
            return "";
        }
        if (preferHyperlink && cell.getHyperlink() != null && !isBlank(cell.getHyperlink().getAddress())) {
            return cell.getHyperlink().getAddress();
        }
        try {
            return clean(formatter.formatCellValue(cell, evaluator));
        } catch (RuntimeException e) {
            if (preferHyperlink && !isBlank(wpsImageId(cell))) {
                return "";
            }
            throw e;
        }
    }

    private String wpsImageId(Cell cell) {
        if (cell == null || cell.getCellTypeEnum() != org.apache.poi.ss.usermodel.CellType.FORMULA) {
            return "";
        }
        Matcher matcher = WPS_DISPIMG_PATTERN.matcher(cell.getCellFormula());
        return matcher.find() ? matcher.group(1) : "";
    }

    private Map<String, ImportedPicture> readWpsCellImages(byte[] fileBytes) {
        Map<String, ImportedPicture> images = new HashMap<>();
        try {
            Map<String, byte[]> entries = readEmbeddedZipEntries(fileBytes);
            byte[] imageDefinitions = entries.get("xl/cellImages.xml");
            byte[] relationships = entries.get("xl/_rels/cellImages.xml.rels");
            if (imageDefinitions == null || relationships == null) {
                return images;
            }

            Map<String, String> targets = new HashMap<>();
            Document relationshipDocument = parseSafeXml(relationships);
            NodeList relationshipNodes = relationshipDocument.getElementsByTagNameNS("*", "Relationship");
            for (int i = 0; i < relationshipNodes.getLength(); i++) {
                Element relationship = (Element) relationshipNodes.item(i);
                String id = relationship.getAttribute("Id");
                String target = relationship.getAttribute("Target").replace('\\', '/');
                while (target.startsWith("../")) {
                    target = target.substring(3);
                }
                if (target.startsWith("/")) {
                    target = target.substring(1);
                }
                targets.put(id, target.startsWith("xl/") ? target : "xl/" + target);
            }

            Document imageDocument = parseSafeXml(imageDefinitions);
            NodeList cellImages = imageDocument.getElementsByTagNameNS("*", "cellImage");
            for (int i = 0; i < cellImages.getLength(); i++) {
                Element cellImage = (Element) cellImages.item(i);
                Element properties = firstDescendant(cellImage, "cNvPr");
                Element blip = firstDescendant(cellImage, "blip");
                if (properties == null || blip == null) {
                    continue;
                }
                String imageId = properties.getAttribute("name");
                String relationshipId = blip.getAttributeNS("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "embed");
                if (isBlank(relationshipId)) {
                    relationshipId = blip.getAttribute("r:embed");
                }
                String target = targets.get(relationshipId);
                byte[] data = entries.get(target);
                if (!isBlank(imageId) && data != null) {
                    images.put(imageId, new ImportedPicture(data, extensionOf(target)));
                }
            }
        } catch (Exception ignored) {
            // Not every WPS .xls contains the optional embedded OOXML image package.
        }
        return images;
    }

    private Map<String, byte[]> readEmbeddedZipEntries(byte[] fileBytes) throws Exception {
        int searchFrom = 0;
        while (searchFrom < fileBytes.length) {
            int zipStart = findZipStart(fileBytes, searchFrom);
            if (zipStart < 0) {
                break;
            }
            Map<String, byte[]> entries = new HashMap<>();
            try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(fileBytes, zipStart, fileBytes.length - zipStart))) {
                ZipEntry entry;
                byte[] buffer = new byte[8192];
                while ((entry = zip.getNextEntry()) != null) {
                    String name = entry.getName().replace('\\', '/');
                    if (!entry.isDirectory() && ("xl/cellImages.xml".equals(name)
                            || "xl/_rels/cellImages.xml.rels".equals(name)
                            || name.startsWith("xl/media/"))) {
                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        int count;
                        while ((count = zip.read(buffer)) != -1) {
                            output.write(buffer, 0, count);
                        }
                        entries.put(name, output.toByteArray());
                    }
                    zip.closeEntry();
                }
            } catch (Exception ignored) {
                // An OLE2 .xls may contain unrelated byte sequences that look like ZIP headers.
            }
            if (entries.containsKey("xl/cellImages.xml")
                    && entries.containsKey("xl/_rels/cellImages.xml.rels")) {
                return entries;
            }
            searchFrom = zipStart + 4;
        }
        return new HashMap<>();
    }

    private int findZipStart(byte[] bytes, int searchFrom) {
        for (int i = Math.max(0, searchFrom); i <= bytes.length - 4; i++) {
            if (bytes[i] == 'P' && bytes[i + 1] == 'K' && bytes[i + 2] == 3 && bytes[i + 3] == 4) {
                return i;
            }
        }
        return -1;
    }

    private Document parseSafeXml(byte[] data) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(data));
    }

    private Element firstDescendant(Element parent, String localName) {
        NodeList nodes = parent.getElementsByTagNameNS("*", localName);
        return nodes.getLength() == 0 ? null : (Element) nodes.item(0);
    }

    private String extensionOf(String path) {
        int dot = path == null ? -1 : path.lastIndexOf('.');
        return dot < 0 ? "png" : path.substring(dot + 1);
    }

    private Map<Integer, ImportedPicture> readPictures(Sheet sheet) {
        Map<Integer, ImportedPicture> result = new HashMap<>();
        if (sheet instanceof XSSFSheet) {
            XSSFDrawing drawing = ((XSSFSheet) sheet).getDrawingPatriarch();
            if (drawing != null) {
                for (XSSFShape shape : drawing.getShapes()) {
                    if (shape instanceof XSSFPicture) {
                        XSSFPicture picture = (XSSFPicture) shape;
                        addPicture(result, picture.getClientAnchor().getRow1(), picture.getPictureData());
                    }
                }
            }
        } else if (sheet instanceof HSSFSheet) {
            HSSFPatriarch drawing = ((HSSFSheet) sheet).getDrawingPatriarch();
            if (drawing != null) {
                for (HSSFShape shape : drawing.getChildren()) {
                    if (shape instanceof HSSFPicture) {
                        HSSFPicture picture = (HSSFPicture) shape;
                        addPicture(result, picture.getClientAnchor().getRow1(), picture.getPictureData());
                    }
                }
            }
        }
        return result;
    }

    private void addPicture(Map<Integer, ImportedPicture> result, int rowIndex, PictureData pictureData) {
        if (!result.containsKey(rowIndex) && pictureData != null) {
            result.put(rowIndex, new ImportedPicture(pictureData.getData(), pictureData.suggestFileExtension()));
        }
    }

    private List<RateImportRow> parseXml(MultipartFile file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        Document document;
        try (InputStream input = file.getInputStream()) {
            document = factory.newDocumentBuilder().parse(input);
        }
        Element root = document.getDocumentElement();
        List<Element> rootChildren = directChildElements(root);
        List<Element> records = new ArrayList<>();
        if (hasRecognizedFields(root, rootChildren)) {
            records.add(root);
        } else {
            records.addAll(rootChildren);
        }
        List<RateImportRow> rows = new ArrayList<>();
        int rowNumber = 1;
        for (Element record : records) {
            RateImportRow row = new RateImportRow();
            row.setSource("XML 第" + rowNumber++ + "条");
            NamedNodeMap attributes = record.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                setAliasedValue(row, attribute.getNodeName(), attribute.getNodeValue());
            }
            for (Element field : directChildElements(record)) {
                setAliasedValue(row, field.getTagName(), field.getTextContent());
            }
            if (!isEmptyRow(row)) {
                rows.add(row);
            }
            if (rows.size() > MAX_ROWS) {
                break;
            }
        }
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("XML中未找到可导入数据");
        }
        return rows;
    }

    private boolean hasRecognizedFields(Element element, List<Element> children) {
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            if (HEADER_ALIASES.containsKey(normalize(attributes.item(i).getNodeName()))) {
                return true;
            }
        }
        for (Element child : children) {
            if (HEADER_ALIASES.containsKey(normalize(child.getTagName()))) {
                return true;
            }
        }
        return false;
    }

    private List<Element> directChildElements(Element parent) {
        List<Element> elements = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                elements.add((Element) child);
            }
        }
        return elements;
    }

    private void setAliasedValue(RateImportRow row, String header, String value) {
        String field = HEADER_ALIASES.get(normalize(header));
        if (field != null) {
            setValue(row, field, clean(value));
        }
    }

    private void setValue(RateImportRow row, String field, String value) {
        if (CERT_NUMBER.equals(field)) row.setCertNumber(value);
        else if (RATE_NAME.equals(field)) { row.setLabelText(value); row.setRateName(secondNonEmptyLineOrOriginal(value)); }
        else if (SURFACE.equals(field)) row.setSurface(value);
        else if (CENTER.equals(field)) row.setCenter(value);
        else if (EDGE.equals(field)) row.setEdge(value);
        else if (CORNER.equals(field)) row.setCorner(value);
        else if (SCORE.equals(field)) row.setScore(value);
        else if (IMG_URL.equals(field)) row.setImgUrl(value);
        else if (REMARK.equals(field)) row.setRemark(value);
    }

    private boolean isEmptyRow(RateImportRow row) {
        return isBlank(row.getCertNumber()) && isBlank(row.getRateName()) && isBlank(row.getSurface())
                && isBlank(row.getCenter()) && isBlank(row.getEdge()) && isBlank(row.getCorner())
                && isBlank(row.getScore()) && isBlank(row.getImgUrl()) && row.getEmbeddedImage() == null;
    }

    private static void addAliases(String field, String... aliases) {
        for (String alias : aliases) {
            HEADER_ALIASES.put(normalize(alias), field);
        }
    }

    private static String normalize(String value) {
        return clean(value).toLowerCase(Locale.ROOT).replaceAll("[\\s_\\-:/：]", "");
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static String secondNonEmptyLineOrOriginal(String value) {
        String cleaned = clean(value);
        if (cleaned.isEmpty()) {
            return cleaned;
        }
        String[] lines = cleaned.split("\\R");
        int nonEmptyLine = 0;
        for (String line : lines) {
            String candidate = clean(line);
            if (candidate.isEmpty()) {
                continue;
            }
            nonEmptyLine++;
            if (nonEmptyLine == 2) {
                return candidate;
            }
        }
        return cleaned;
    }

    private static String safeMessage(Exception e) {
        String message = e.getMessage();
        return isBlank(message) ? "未知错误" : message;
    }

    private static class ImportedPicture {
        private final byte[] data;
        private final String extension;

        private ImportedPicture(byte[] data, String extension) {
            this.data = data;
            this.extension = extension;
        }
    }
}
