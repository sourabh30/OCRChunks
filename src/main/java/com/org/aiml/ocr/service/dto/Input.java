package com.org.aiml.ocr.service.dto;

import java.util.List;

public class Input {
    private String ocrFolder;
    private List<PageRange> pageRange;
    private String responseType;

    // Getters and setters
    public String getOcrFolder() {
        return ocrFolder;
    }

    public void setOcrFolder(String ocrFolder) {
        this.ocrFolder = ocrFolder;
    }

    public List<PageRange> getPageRange() {
        return pageRange;
    }

    public void setPageRange(List<PageRange> pageRange) {
        this.pageRange = pageRange;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }
}
