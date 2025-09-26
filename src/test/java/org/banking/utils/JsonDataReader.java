package org.banking.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class JsonDataReader {

    private static final Logger logger = LogManager.getLogger(JsonDataReader.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T readTestData(String fileName, String testCase, Class<T> clazz) {
        try {
            InputStream inputStream = JsonDataReader.class.getClassLoader()
                    .getResourceAsStream("testdata/" + fileName);

            if (inputStream == null) {
                throw new RuntimeException("Test data file not found: " + fileName);
            }

            JsonNode rootNode = objectMapper.readTree(inputStream);
            JsonNode testCaseNode = rootNode.get(testCase);

            if (testCaseNode == null) {
                throw new RuntimeException("Test case not found in JSON: " + testCase);
            }

            return objectMapper.treeToValue(testCaseNode, clazz);

        } catch (IOException e) {
            logger.error("Error reading JSON test data: " + e.getMessage());
            throw new RuntimeException("Failed to read JSON test data", e);
        }
    }

    public static <T> List<T> readTestDataList(String fileName, String testCase, Class<T> clazz) {
        try {
            InputStream inputStream = JsonDataReader.class.getClassLoader()
                    .getResourceAsStream("testdata/" + fileName);

            if (inputStream == null) {
                throw new RuntimeException("Test data file not found: " + fileName);
            }

            JsonNode rootNode = objectMapper.readTree(inputStream);
            JsonNode testCaseNode = rootNode.get(testCase);

            if (testCaseNode == null || !testCaseNode.isArray()) {
                throw new RuntimeException("Test case array not found in JSON: " + testCase);
            }

            List<T> resultList = new ArrayList<>();
            for (JsonNode node : testCaseNode) {
                resultList.add(objectMapper.treeToValue(node, clazz));
            }

            return resultList;

        } catch (IOException e) {
            logger.error("Error reading JSON test data list: " + e.getMessage());
            throw new RuntimeException("Failed to read JSON test data list", e);
        }
    }
}
