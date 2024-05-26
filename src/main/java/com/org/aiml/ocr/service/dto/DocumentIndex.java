package com.org.aiml.ocr.service.dto;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;

public class DocumentIndex {
    public String document;
    public String ocrData;
    public List<Split> splits;

    public static class Split {
        public int startPage;
        public int endPage;
        public String documentChunk;
        public String ocrRaw;
        public String ocrTextOnly;
        public String ocrStandard;
        public String ocrTable;
        public String ocrForm;
    }

    public static DocumentIndex loadFromStream(InputStream inputStream) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(inputStream, DocumentIndex.class);
    }
}
