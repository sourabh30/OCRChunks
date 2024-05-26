package com.org.aiml.ocr.service.ocrummary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.aiml.ocr.service.dto.DocumentIndex;
import com.org.aiml.ocr.service.dto.Input;
import com.org.aiml.ocr.service.dto.PageRange;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class OCRChunks {

    public static void main(String[] args) throws Exception {
        // Load index.json from resources folder
        InputStream indexStream = getResourceAsStream("OCR/index.json");
        DocumentIndex documentIndex = DocumentIndex.loadFromStream(indexStream);

        // Load input.json from resources folder
        InputStream inputStream = getResourceAsStream("OCR/input.json");
        ObjectMapper mapper = new ObjectMapper();
        Input input = mapper.readValue(inputStream, Input.class);

        // Sort the page ranges based on startPage using StreamSupport
        List<PageRange> sortedPageRanges = StreamSupport.stream(input.getPageRange().spliterator(), false)
                .sorted(Comparator.comparingInt(PageRange::getStartPage))
                .collect(Collectors.toList());

        Map<String, List<String>> result = getFilteredDocuments(documentIndex, sortedPageRanges);

        result.forEach((range, urls) -> {
            System.out.println(range + ": " + urls);
        });
    }

    public static InputStream getResourceAsStream(String resource) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader.getResourceAsStream(resource);
    }

    public static Map<String, List<String>> getFilteredDocuments(DocumentIndex documentIndex, List<PageRange> pageRanges) {
        Map<String, List<String>> result = new HashMap<>();

        for (PageRange pageRange : pageRanges) {
            List<String> urls = new ArrayList<>();
            for (DocumentIndex.Split split : documentIndex.splits) {
                if (isOverlap(pageRange, split)) {
                    urls.add(split.ocrStandard);  // or any other field you need
                }
            }
            String key = pageRange.getStartPage() + "-" + pageRange.getEndPage();
            result.put(key, urls);
        }

        return result;
    }

    public static boolean isOverlap(PageRange range, DocumentIndex.Split split) {
        return !(range.getEndPage() < split.startPage || range.getStartPage() > split.endPage);
    }
}
