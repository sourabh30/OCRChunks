package com.org.aiml.ocr.service.ocrummary;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

public class OCRDataGenerator2 {
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = Logger.getLogger(OCRDataGenerator2.class.getName());


    public static void main(String[] args) throws IOException {

        long programStartTime = System.nanoTime(); // Start time for the entire program


        InputStream inputStream = OCRDataGenerator2.class.getClassLoader().getResourceAsStream("LargeFiles/ocr-data-uri-by-page-range.json");
        if (inputStream == null) {
            throw new IOException("Resource not found: LargeFiles/ocr-data-uri-by-page-range.json");
        }

        Map<String, Object> input = objectMapper.readValue(inputStream, Map.class);
        Set<JsonNode> sortedPages = new TreeSet<>(Comparator.comparingInt(page -> page.get("page_no").asInt()));

        input.forEach((key, value) -> {
            Map<String, String> uris = (Map<String, String>) value;
            uris.forEach((subKey, uri) -> {
                try {
                    byte[] fileContent = readObject(uri);
                    addPagesToResult(fileContent, sortedPages);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });

        // Create the final JSON object
        ArrayNode resultPages = objectMapper.createArrayNode();
        sortedPages.forEach(resultPages::add);

        ObjectNode finalJson = objectMapper.createObjectNode();
        finalJson.set("pages", resultPages);

        // Print the final JSON
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(finalJson));

        long programEndTime = System.nanoTime(); // End time for the entire program
        System.out.println("Total time to run the program: " + (programEndTime - programStartTime) / 1_000_000 + " ms");
    }

    private static void addPagesToResult(byte[] fileContent, Set<JsonNode> sortedPages) throws IOException {
        try (JsonParser parser = new JsonFactory().createParser(new ByteArrayInputStream(fileContent))) {
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw new IOException("Expected data to start with an Object");
            }

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.getCurrentName();
                if ("pages".equals(fieldName)) {
                    parser.nextToken(); // start array
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        JsonNode pageNode = objectMapper.readTree(parser);
                        sortedPages.add(pageNode);
                    }
                } else {
                    parser.skipChildren();
                }
            }
        }
    }

    private static byte[] readObject(String uri) throws IOException {
        File file = new File(uri);
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            return data;
        }
    }
}
