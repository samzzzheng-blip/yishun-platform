package com.kiss.yishun.service;

import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.regex.*;

/** WPS DISPIMG reader. Reads embedded package bytes only, never external links. */
final class PreciousCellImages {
    private static final Pattern FORMULA = Pattern.compile("(?i)^(?:_xlfn\\.)?DISPIMG\\s*\\(\\s*\"([^\"]+)\"\\s*,\\s*1\\s*\\)$");
    static boolean isImage(Cell cell) {
        return cell != null && cell.getCellTypeEnum() == CellType.FORMULA
                && cell.getCellFormula().toUpperCase(Locale.ROOT).contains("DISPIMG");
    }
    static Map<String, PictureData> read(Workbook book, Sheet sheet, Map<Integer,Integer> columns) throws Exception {
        Map<String, String> references = new LinkedHashMap<>();
        for (Row row : sheet) for (Map.Entry<Integer,Integer> col : columns.entrySet()) {
            Cell cell = row.getCell(col.getKey());
            if (!isImage(cell)) continue;
            if (col.getValue() != 1 && col.getValue() != 8)
                throw new IllegalArgumentException("第 " + (row.getRowNum()+1) + " 行单元格图片只能放在照片或证据图片列");
            Matcher match = FORMULA.matcher(cell.getCellFormula().trim());
            if (!match.matches()) throw new IllegalArgumentException("第 " + (row.getRowNum()+1) + " 行 DISPIMG 公式格式不支持");
            references.put(row.getRowNum()+":"+col.getKey(), match.group(1));
        }
        Map<String,PictureData> result = new HashMap<>();
        if (references.isEmpty()) return result;
        if (!(book instanceof XSSFWorkbook)) throw new IllegalArgumentException("WPS 单元格图片请另存为 .xlsx 后导入");
        Map<String,PackagePart> parts = new HashMap<>();
        for (PackagePart part : ((XSSFWorkbook)book).getPackage().getParts()) {
            String key = part.getPartName().getName().toLowerCase(Locale.ROOT);
            if (parts.put(key, part) != null) throw new IllegalArgumentException("Excel 内存在重复资源路径");
        }
        Document defs = xml(readBytes(parts.get("/xl/cellimages.xml"), 2*1024*1024));
        Document rels = xml(readBytes(parts.get("/xl/_rels/cellimages.xml.rels"), 2*1024*1024));
        Map<String,String> targets = new HashMap<>();
        NodeList relationships = rels.getElementsByTagNameNS("*","Relationship");
        for (int i=0;i<relationships.getLength();i++) {
            Element rel=(Element)relationships.item(i);
            if ("External".equalsIgnoreCase(rel.getAttribute("TargetMode"))) continue;
            URI target = URI.create("/xl/").resolve(rel.getAttribute("Target")).normalize();
            if (target.isAbsolute() || target.getAuthority()!=null || target.getQuery()!=null || target.getFragment()!=null
                    || !target.getPath().startsWith("/xl/media/")) continue;
            if (targets.put(rel.getAttribute("Id"),target.getPath().toLowerCase(Locale.ROOT))!=null)
                throw new IllegalArgumentException("WPS 图片关系编号重复");
        }
        Map<String,String> imageTargets = new HashMap<>();
        NodeList pics=defs.getElementsByTagNameNS("*","cellImage");
        for(int i=0;i<pics.getLength();i++){
            Element element=(Element)pics.item(i);
            NodeList props=element.getElementsByTagNameNS("*","cNvPr"),blips=element.getElementsByTagNameNS("*","blip");
            if(props.getLength()!=1 || blips.getLength()!=1) throw new IllegalArgumentException("WPS 图片定义不完整");
            String id=((Element)props.item(0)).getAttribute("name");
            String rel=((Element)blips.item(0)).getAttributeNS("http://schemas.openxmlformats.org/officeDocument/2006/relationships","embed");
            if(imageTargets.containsKey(id)) throw new IllegalArgumentException("WPS 图片编号重复");
            imageTargets.put(id,targets.get(rel));
        }
        Map<String,PictureData> cached=new HashMap<>();
        long total=0;
        for(Map.Entry<String,String> reference:references.entrySet()){
            String id=reference.getValue(),target=imageTargets.get(id);
            if(target==null) throw new IllegalArgumentException("第 "+(Integer.parseInt(reference.getKey().split(":")[0])+1)+" 行 WPS 图片未嵌入或资源缺失："+id);
            PictureData image=cached.get(target);
            if(image==null){
                byte[] bytes=readBytes(parts.get(target),10*1024*1024);
                total+=bytes.length;
                if(total>50L*1024*1024) throw new IllegalArgumentException("WPS 图片解压后合计超过 50MB，请分批导入");
                String ext=target.substring(target.lastIndexOf('.')+1);
                if(!ext.matches("png|jpe?g|gif")) throw new IllegalArgumentException("WPS 图片只支持 PNG/JPG/GIF");
                image=new EmbeddedPicture(bytes,ext);cached.put(target,image);
            }
            result.put(reference.getKey(),image);
        }
        return result;
    }
    private static byte[] readBytes(PackagePart part,int limit)throws Exception{
        if(part==null)throw new IllegalArgumentException("WPS 单元格图片资源缺失");
        try(InputStream in=part.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] buffer=new byte[8192];int n;
            while((n=in.read(buffer))!=-1){if(out.size()+n>limit)throw new IllegalArgumentException("WPS 图片资源超过大小限制");out.write(buffer,0,n);}
            if(out.size()==0)throw new IllegalArgumentException("WPS 图片资源为空");
            return out.toByteArray();
        }
    }
    private static Document xml(byte[] bytes)throws Exception{
        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);factory.setXIncludeAware(false);factory.setExpandEntityReferences(false);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities",false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities",false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(bytes));
    }
    private static final class EmbeddedPicture implements PictureData{
        private final byte[] data;private final String ext;
        EmbeddedPicture(byte[] data,String ext){this.data=data;this.ext=ext;}
        public byte[] getData(){return data;}
        public String suggestFileExtension(){return ext;}
        public String getMimeType(){return "image/"+(ext.matches("jpe?g")?"jpeg":ext);}
        public int getPictureType(){return ext.equals("png")?Workbook.PICTURE_TYPE_PNG:ext.matches("jpe?g")?Workbook.PICTURE_TYPE_JPEG:0;}
    }
}
