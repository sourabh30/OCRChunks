package com.org.aiml.ocr.service.ocrummary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.org.aiml.ocr.service.dto.DocumentIndex;
import com.org.aiml.ocr.service.dto.Input;
import com.org.aiml.ocr.service.dto.PageRange;

import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class OCRChunks_nested_for_time_3_WORKING {

    private static final Logger logger = Logger.getLogger(OCRChunks_nested_for_time_3_WORKING.class.getName());


    public static void main(String[] args) throws Exception {
        long programStartTime = System.nanoTime(); // Start time for the entire program

        long startTime, endTime;

        // Load index.json from resources folder
        startTime = System.nanoTime();
        InputStream indexStream = getResourceAsStream("OCR/index.json");
        DocumentIndex documentIndex = DocumentIndex.loadFromStream(indexStream);
        endTime = System.nanoTime();
        System.out.println("Loading index.json took: " + (endTime - startTime) / 1_000_000 + " ms");

        // Load input.json from resources folder
        startTime = System.nanoTime();
        InputStream inputStream = getResourceAsStream("OCR/input.json");
        ObjectMapper mapper = new ObjectMapper();
        Input input = mapper.readValue(inputStream, Input.class);
        endTime = System.nanoTime();
        System.out.println("Loading input.json took: " + (endTime - startTime) / 1_000_000 + " ms");

        // Sort the page ranges based on startPage using StreamSupport
        startTime = System.nanoTime();
        List<PageRange> sortedPageRanges = StreamSupport.stream(input.getPageRange().spliterator(), false)
                .filter(OCRChunks_stream_time5_wip::isValidPageRange)
                .sorted(Comparator.comparingInt(PageRange::getStartPage))
                .collect(Collectors.toList());
        endTime = System.nanoTime();
        System.out.println("Sorting page ranges took: " + (endTime - startTime) / 1_000_000 + " ms");

        // Filter and collect the results using nested for loops
        startTime = System.nanoTime();
        Map<String, Map<String, String>> result = getFilteredDocumentsNested(documentIndex, sortedPageRanges);
        endTime = System.nanoTime();
        System.out.println("Filtering and collecting results took: " + (endTime - startTime) / 1_000_000 + " ms");

        // Sort the result map based on the first value of the key
        startTime = System.nanoTime();
        Map<String, Map<String, String>> sortedResult = new LinkedHashMap<>();
        result.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> Integer.parseInt(e.getKey().split("-")[0])))
                .forEachOrdered(e -> sortedResult.put(e.getKey(), e.getValue()));
        endTime = System.nanoTime();
        System.out.println("Sorting the result map took: " + (endTime - startTime) / 1_000_000 + " ms");

        // Print the result in pretty JSON format
//        startTime = System.nanoTime();
//        ObjectMapper prettyMapper = new ObjectMapper();
//        prettyMapper.enable(SerializationFeature.INDENT_OUTPUT);
//        String prettyJson = prettyMapper.writeValueAsString(sortedResult);
//        System.out.println(prettyJson);
//        endTime = System.nanoTime();
//        System.out.println("Pretty printing the result took: " + (endTime - startTime) / 1_000_000 + " ms");

        long programEndTime = System.nanoTime(); // End time for the entire program
        System.out.println("Total time to run the program: " + (programEndTime - programStartTime) / 1_000_000 + " ms");
    }

    public static InputStream getResourceAsStream(String resource) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader.getResourceAsStream(resource);
    }

    public static Map<String, Map<String, String>> getFilteredDocumentsNested(DocumentIndex documentIndex, List<PageRange> pageRanges) {
        Map<String, Map<String, String>> result = new LinkedHashMap<>();

        for (PageRange pageRange : pageRanges) {
            Map<String, String> urls = new LinkedHashMap<>();
            int sequenceNumber = 1;
            for (DocumentIndex.Split split : documentIndex.splits) {
                if (split.startPage > pageRange.getEndPage()) {
                    break; // Stopping criterion
                }
                if (isOverlap(pageRange, split)) {
                    urls.put(String.valueOf(sequenceNumber), split.ocrStandard);  // or any other field you need
                    sequenceNumber++;
                }
            }
            String key = pageRange.getStartPage() + "-" + pageRange.getEndPage();
            result.put(key, urls);
        }

        return result;
    }

    public static boolean isValidPageRange(PageRange range) {
        boolean isValid = true;
        if (range.getStartPage() <= 0 || range.getEndPage() <= 0) {
            logger.log(Level.WARNING, "Invalid page range: " + range.getStartPage() + "-" + range.getEndPage() + " (Negative page number)");
            isValid = false;
        }
        if (range.getEndPage() < range.getStartPage()) {
            logger.log(Level.WARNING, "Invalid page range: " + range.getStartPage() + "-" + range.getEndPage() + " (endPage is less than startPage)");
            isValid = false;
        }
        return isValid;
    }

    public static boolean isOverlap(PageRange range, DocumentIndex.Split split) {
        return !(range.getEndPage() < split.startPage || range.getStartPage() > split.endPage);
    }
}
