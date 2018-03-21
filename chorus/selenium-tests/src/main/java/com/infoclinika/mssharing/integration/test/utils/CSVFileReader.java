package com.infoclinika.mssharing.integration.test.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Alexander Orlov
 */
public class CSVFileReader {

    public static List<String> getDataFromCsvFile(String pathToCsvFile) {
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ";";
        List<String> cellList = new ArrayList<>();
        try {
            File file = new File(CSVFileReader.class.getResource(pathToCsvFile).getFile());
            br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                String[] cell = line.replaceAll("\"", "").split(cvsSplitBy);
                Collections.addAll(cellList, cell);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return cellList;
    }
}
