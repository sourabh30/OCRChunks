package com.org.aiml.ocr.service.dto;

public class PageRange {
    private int startPage;
    private int endPage;

    // Default constructor
    public PageRange() {
    }

    // Parameterized constructor
    public PageRange(int startPage, int endPage) {
        this.startPage = startPage;
        this.endPage = endPage;
    }

    // Getters and setters
    public int getStartPage() {
        return startPage;
    }

    public void setStartPage(int startPage) {
        this.startPage = startPage;
    }

    public int getEndPage() {
        return endPage;
    }

    public void setEndPage(int endPage) {
        this.endPage = endPage;
    }
}
