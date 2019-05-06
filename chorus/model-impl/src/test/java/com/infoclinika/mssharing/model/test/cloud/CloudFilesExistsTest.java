package com.infoclinika.mssharing.model.test.cloud;

import com.infoclinika.analysis.storage.cloud.CloudStorageItemReference;
import com.infoclinika.analysis.storage.cloud.CloudStorageService;
import com.infoclinika.mssharing.model.helper.AbstractTest;
import com.infoclinika.mssharing.model.internal.cloud.CloudStorageClientsProvider;
import org.testng.annotations.Test;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * @author vladislav.kovchug
 */

//@Configuration
//@ImportResource({"test.cfg.xml", "persistence.cfg.xml", "../../../../../../../../main/resources/mysql.cfg.xml",
// "workflow.cfg.xml"})
//@ComponentScan(basePackages = {"com.infoclinika.mssharing.model", "com.infoclinika.mssharing.platform"},
// excludeFilters = {@ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.*DefaultRuleValidator*")})

public class CloudFilesExistsTest extends AbstractTest {

    private static final String SOURCE_CSV = "D:\\database\\raw_files.csv";
    private static final String DEST_CSV = "D:\\database\\report.csv";

    private static final String CSV_DELIMETER = ";";

    @Inject
    private CloudStorageClientsProvider cloudStorageClientsProvider;

    private String removeQuotes(String text) {
        if (text != null && text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
            return text.substring(1, text.length() - 1);
        } else {
            return text;
        }
    }

    @Test(enabled = false)
    @PostConstruct
    public void check_files_for_exists() throws Exception {
        File fileMetaDataTemplateFile = new File(SOURCE_CSV);
        File reportFile = new File(DEST_CSV);
        reportFile.createNewFile();
        final Scanner scanner = new Scanner(fileMetaDataTemplateFile);
        PrintWriter writer = new PrintWriter(reportFile);


        writer.println("\"id\";\"MetadataId\";\"contentId\";\"Exists\"");
        Integer counter = -1;
        while (scanner.hasNextLine()) {
            final String rawData = scanner.nextLine();
            counter++;
            if (counter == 0) { //ignore headers
                continue;
            }
            final String[] parts = rawData.split(CSV_DELIMETER);
            if (parts.length < 3) {
                continue;
            }
            String id = removeQuotes(parts[0]);
            String contentId = removeQuotes(parts[2]);

            Boolean fileExists = false;
            if (!contentId.isEmpty()) {
                CloudStorageService service = cloudStorageClientsProvider.getCloudStorageService();
                CloudStorageItemReference reference = new CloudStorageItemReference(
                    "chorus-production",
                    contentId
                );
                fileExists = service.existsAtCloud(reference);
            }


            String csvData = counter.toString() + CSV_DELIMETER + id + CSV_DELIMETER + "\"" + contentId + "\"" +
                CSV_DELIMETER + fileExists.toString();
            writer.println(csvData);
            System.out.println(csvData);
        }

        writer.close();
    }

}
