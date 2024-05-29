package com.org.aiml.ocr.service.ocrummary;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.org.aiml.ocr.service.dto.Input;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

public class OCRDataGenerator {

        private static ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger logger = Logger.getLogger(OCRDataGenerator.class.getName());

        public static void main(String[] args) throws IOException {

            long programStartTime = System.nanoTime(); // Start time for the entire program


            InputStream inputStream = OCRDataGenerator.class.getClassLoader().getResourceAsStream("LargeFiles/ocr-data-uri-by-page-range.json");
            if (inputStream == null) {
                throw new IOException("Resource not found: LargeFiles/ocr-data-uri-by-page-range.json");
            }

            Map<String, Object> input = objectMapper.readValue(inputStream, Map.class);
            ArrayNode resultPages = objectMapper.createArrayNode();

            Set<Integer> uniquePages = new HashSet<>();

            input.forEach((key, value) -> {
                Map<String, String> uris = (Map<String, String>) value;
                uris.forEach((subKey, uri) -> {
                    try {
                        byte[] fileContent = readObject(uri);
                        addPagesToResult(fileContent, uniquePages, resultPages);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            });

            // Sort the pages
            List<JsonNode> sortedPages = new ArrayList<>();
            resultPages.forEach(sortedPages::add);
            Collections.sort(sortedPages, (p1, p2) -> Integer.compare(p1.get("page_no").asInt(), p2.get("page_no").asInt()));

            // Create the final JSON object
            ObjectNode finalJson = objectMapper.createObjectNode();
            finalJson.set("pages", objectMapper.valueToTree(sortedPages));

            // Print the final JSON
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(finalJson));

            long programEndTime = System.nanoTime(); // End time for the entire program
            System.out.println("Total time to run the program: " + (programEndTime - programStartTime) / 1_000_000 + " ms");
        }

        private static void addPagesToResult(byte[] fileContent, Set<Integer> uniquePages, ArrayNode resultPages) throws IOException {
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
                            int pageNo = pageNode.get("page_no").asInt();
                            if (uniquePages.add(pageNo)) {
                                resultPages.add(pageNode);
                            }
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
