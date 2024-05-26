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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class OCRChunks_stream3 {


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

        Map<String, Map<String, String>> result = getFilteredDocuments(documentIndex, sortedPageRanges);

        // Sort the result map based on the first value of the key
        Map<String, Map<String, String>> sortedResult = result.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> Integer.parseInt(e.getKey().split("-")[0])))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        // Print the result in pretty JSON format
        ObjectMapper prettyMapper = new ObjectMapper();
        prettyMapper.enable(SerializationFeature.INDENT_OUTPUT);
        String prettyJson = prettyMapper.writeValueAsString(sortedResult);
        System.out.println(prettyJson);
    }

    public static InputStream getResourceAsStream(String resource) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader.getResourceAsStream(resource);
    }

    public static Map<String, Map<String, String>> getFilteredDocuments(DocumentIndex documentIndex, List<PageRange> pageRanges) {
        return pageRanges.stream().collect(Collectors.toMap(
                range -> range.getStartPage() + "-" + range.getEndPage(),
                range -> {
                    Map<String, String> urls = new LinkedHashMap<>();
                    int sequenceNumber = 1;
                    for (DocumentIndex.Split split : documentIndex.splits) {
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
