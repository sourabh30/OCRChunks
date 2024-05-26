package com.org.aiml.ocr.service.ocrummary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.org.aiml.ocr.service.dto.DocumentIndex;
import com.org.aiml.ocr.service.dto.Input;
import com.org.aiml.ocr.service.dto.PageRange;

import java.io.InputStream;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class OCRChunks_stream_time5_wip {

    private static final Logger logger = Logger.getLogger(OCRChunks_stream_time5_wip.class.getName());

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

        // Validate input
        // validateInput(input);

        // Validate, sort, and filter the page ranges based on startPage using StreamSupport
        startTime = System.nanoTime();
        List<PageRange> sortedPageRanges = StreamSupport.stream(input.getPageRange().spliterator(), false)
                .filter(OCRChunks_stream_time5_wip::isValidPageRange)
                .sorted(Comparator.comparingInt(PageRange::getStartPage))
                .collect(Collectors.toList());
        endTime = System.nanoTime();
        System.out.println("Sorting page ranges took: " + (endTime - startTime) / 1_000_000 + " ms");

        // Filter and collect the results
        startTime = System.nanoTime();
        Map<String, Map<String, String>> result = getFilteredDocuments(documentIndex, sortedPageRanges);
        endTime = System.nanoTime();
        System.out.println("Filtering and collecting results took: " + (endTime - startTime) / 1_000_000 + " ms");

        // Sort the result map based on the first value of the key
        startTime = System.nanoTime();
        Map<String, Map<String, String>> sortedResult = result.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> Integer.parseInt(e.getKey().split("-")[0])))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
        endTime = System.nanoTime();
        System.out.println("Sorting the result map took: " + (endTime - startTime) / 1_000_000 + " ms");

        // Print the result in pretty JSON format
        startTime = System.nanoTime();
        ObjectMapper prettyMapper = new ObjectMapper();
        prettyMapper.enable(SerializationFeature.INDENT_OUTPUT);
        String prettyJson = prettyMapper.writeValueAsString(sortedResult);
        System.out.println(prettyJson);
        endTime = System.nanoTime();
        System.out.println("Pretty printing the result took: " + (endTime - startTime) / 1_000_000 + " ms");

        long programEndTime = System.nanoTime(); // End time for the entire program
        System.out.println("Total time to run the program: " + (programEndTime - programStartTime) / 1_000_000 + " ms");
    }


    public static InputStream getResourceAsStream(String resource) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader.getResourceAsStream(resource);
    }

    public static void validateInput(Input input) {
        if (input.getPageRange() == null || input.getPageRange().isEmpty()) {
            throw new IllegalArgumentException("Page range list cannot be null or empty.");
        }
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

    public static Map<String, Map<String, String>> getFilteredDocuments(DocumentIndex documentIndex, List<PageRange> pageRanges) {
//        return pageRanges.stream().collect(Collectors.toMap(
        return pageRanges.parallelStream().collect(Collectors.toMap(
                range -> range.getStartPage() + "-" + range.getEndPage(),
                range -> {
                    Map<String, String> urls = new LinkedHashMap<>();
                    int sequenceNumber = 1;
                    for (DocumentIndex.Split split : documentIndex.splits) {
                        if (split.startPage > range.getEndPage()) {
                            break; // Stopping criterion
                        }
                        if (isOverlap(range, split)) {
                            urls.put(String.valueOf(sequenceNumber), split.ocrStandard);  // or any other field you need
                            sequenceNumber++;
                        }

                    }
                    return urls;
                },
                (e1, e2) -> e1,
                LinkedHashMap::new
        ));
    }

    public static boolean isOverlap(PageRange range, DocumentIndex.Split split) {
        return !(range.getEndPage() < split.startPage || range.getStartPage() > split.endPage);
    }
}
