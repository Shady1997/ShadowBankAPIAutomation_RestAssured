/*
 * Author: Shady Ahmed
 * Date: 2025-09-27
 * Project: Mobile Banking API Testing using RestAssured (E2E)
 * My Linked-in: https://www.linkedin.com/in/shady-ahmed97/.
 */
package org.banking.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelDataReader {

    private static final Logger logger = LogManager.getLogger(ExcelDataReader.class);

    public static Object[][] readExcelData(String fileName, String sheetName) {
        try {
            InputStream inputStream = ExcelDataReader.class.getClassLoader()
                    .getResourceAsStream("testdata/" + fileName);

            if (inputStream == null) {
                throw new RuntimeException("Excel file not found: " + fileName);
            }

            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheet(sheetName);

            if (sheet == null) {
                throw new RuntimeException("Sheet not found: " + sheetName);
            }

            int rowCount = sheet.getLastRowNum();
            int colCount = sheet.getRow(0).getLastCellNum();

            Object[][] data = new Object[rowCount][1];

            // Get headers from first row
            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            for (int col = 0; col < colCount; col++) {
                headers.add(getCellValueAsString(headerRow.getCell(col)));
            }

            // Read data rows
            for (int row = 1; row <= rowCount; row++) {
                Row dataRow = sheet.getRow(row);
                Map<String, String> rowData = new HashMap<>();

                for (int col = 0; col < colCount; col++) {
                    String header = headers.get(col);
                    String cellValue = getCellValueAsString(dataRow.getCell(col));
                    rowData.put(header, cellValue);
                }

                data[row - 1][0] = rowData;
            }

            workbook.close();
            inputStream.close();

            logger.info("Excel data loaded successfully from: " + fileName + ", Sheet: " + sheetName);
            return data;

        } catch (IOException e) {
            logger.error("Error reading Excel data: " + e.getMessage());
            throw new RuntimeException("Failed to read Excel data", e);
        }
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}