package com.kiss.yishun.entity.vo;

import java.util.ArrayList;
import java.util.List;

public class RateImportResult {
    private int total;
    private int imported;
    private int skipped;
    private List<String> errors = new ArrayList<>();

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public int getImported() { return imported; }
    public void setImported(int imported) { this.imported = imported; }
    public int getSkipped() { return skipped; }
    public void setSkipped(int skipped) { this.skipped = skipped; }
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }

    public void addError(String error) {
        skipped++;
        if (errors.size() < 100) {
            errors.add(error);
        }
    }
}
