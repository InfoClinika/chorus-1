package com.infoclinika.ngs;

import com.infoclinika.mssharing.parser.RestfulAPI;
import org.apache.commons.io.FileUtils;
import org.json.simple.parser.ParseException;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static com.infoclinika.mssharing.parser.RestfulAPI.getColInfoJSONFile;

/**
 * Created by davemakhervaks on 7/25/17.
 */
public class RESTfulAPITest {

    private static String sessionID = "JSESSIONID=FB84A2825C3AA52C31D0CBF6E988202F; Path=/; HttpOnly";

    @Test(enabled = false) // invalid test
    public void testRetrievalOfColInfoInformation() throws ParseException, IOException {
        String json = "src/test/resources/SampleCol.json";
        File jsonFile = new File(json);
        if (jsonFile.exists()) {
            jsonFile.delete();
        }
        getColInfoJSONFile("https://crdm.celgene.com/v2/experiments/48", sessionID, json);
    }

    @Test(enabled = false) // invalid test
    public void testRESTfulGeneRetrievalOfFiles() throws IOException, ParseException {
        // Delete all previous files for testing purposes
        File zipFile = new File("src/test/resources/resultingGeneProcessingFiles.zip");
        if (zipFile.exists()) {
            zipFile.delete();
        }
        File directory = new File("src/test/resources/resultingGeneProcessingFiles");
        if (directory.exists() && directory.isDirectory()) {
            FileUtils.deleteDirectory(directory);
        }
        File newDirectory = new File("src/test/resources/genePracticeFiles");
        if (newDirectory.exists()) {
            FileUtils.deleteDirectory(newDirectory);

        }
        RestfulAPI test = new RestfulAPI("https://crdm.celgene.com/v2/experiment/48/processing-runs-export/10", sessionID, "src/test/resources/resultingGeneProcessingFiles.zip", "src/test/resources/resultingGeneProcessingFiles");
    }

    @Test(enabled = false) // invalid test
    public void testRESTfulExonRetrievalOfFiles() throws IOException, ParseException {
        // Delete all previous files for testing purposes
        File zipFile = new File("src/test/resources/resultingExonProcessingFiles.zip");
        if (zipFile.exists()) {
            zipFile.delete();
        }
        File directory = new File("src/test/resources/resultingExonProcessingFiles");
        if (directory.exists() && directory.isDirectory()) {
            FileUtils.deleteDirectory(directory);
        }
        File newDirectory = new File("src/test/resources/exonPracticeFiles");
        if (newDirectory.exists()) {
            FileUtils.deleteDirectory(newDirectory);

        }
        RestfulAPI test = new RestfulAPI("https://crdm.celgene.com/v2/experiment/48/processing-runs-export/9", sessionID, "src/test/resources/resultingExonProcessingFiles.zip", "src/test/resources/resultingExonProcessingFiles");
    }

}



