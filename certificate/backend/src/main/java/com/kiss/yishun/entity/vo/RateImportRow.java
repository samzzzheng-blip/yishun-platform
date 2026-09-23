package com.kiss.yishun.entity.vo;

public class RateImportRow {
    private String source;
    private String certNumber;
    private String rateName;
    private String labelText;
    public String getLabelText() { return labelText; }
    public void setLabelText(String labelText) { this.labelText = labelText; }
    private String surface;
    private String center;
    private String edge;
    private String corner;
    private String score;
    private String imgUrl;
    private String remark;
    private byte[] embeddedImage;
    private String embeddedImageExtension;

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getCertNumber() { return certNumber; }
    public void setCertNumber(String certNumber) { this.certNumber = certNumber; }
    public String getRateName() { return rateName; }
    public void setRateName(String rateName) { this.rateName = rateName; }
    public String getSurface() { return surface; }
    public void setSurface(String surface) { this.surface = surface; }
    public String getCenter() { return center; }
    public void setCenter(String center) { this.center = center; }
    public String getEdge() { return edge; }
    public void setEdge(String edge) { this.edge = edge; }
    public String getCorner() { return corner; }
    public void setCorner(String corner) { this.corner = corner; }
    public String getScore() { return score; }
    public void setScore(String score) { this.score = score; }
    public String getImgUrl() { return imgUrl; }
    public void setImgUrl(String imgUrl) { this.imgUrl = imgUrl; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public byte[] getEmbeddedImage() { return embeddedImage; }
    public void setEmbeddedImage(byte[] embeddedImage) { this.embeddedImage = embeddedImage; }
    public String getEmbeddedImageExtension() { return embeddedImageExtension; }
    public void setEmbeddedImageExtension(String embeddedImageExtension) { this.embeddedImageExtension = embeddedImageExtension; }
}
