package com.infoclinika.mssharing.model.internal.stats;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility class to compose stats.
 *
 * @author Oleksii Tymchenko
 */
public class StatsAggregator {

    private static final String BUCKET = "chorus-production";

    public static final String IMPORT_CSV_PATH = "/Users/armiol/Desktop/chorus-stats-3mar.csv";
    public static final String EXPORT_SERIALIZED_FILE_PATH = "/Users/armiol/stats-serilized.bin";
    public static final String EXPORT_CSV_FILE_PATH = "/Users/armiol/stats-3mar.csv";

//    --- Query to analyze: ---
//    select
//    lab.id as labId,
//    lab.name as labName,
//    fmd.id as fileId,
//    fmd.name as fileName,
//    fmd.sizeInBytes as fileSize,
//    fmd.contentId as rawPath,
//    msf.translatedPath as translatedFunctionPath
//    from FileMetaData fmd
//    inner join Instrument inst on fmd.instrument_id = inst.id
//    inner join Lab lab on lab.id = inst.lab_id
//    left join FileMetaData_MSFunctionItem fmd_to_msf on fmd.id = fmd_to_msf.FileMetaData_id
//    left join MSFunctionItem msf on msf.id =  fmd_to_msf.functions_id



    public static void main(String[] args) throws Exception {
        final long startTime = System.currentTimeMillis();
        final ExecutorService executorService = Executors.newFixedThreadPool(200);

        //todo: fix local paths to provide proper export. CSV delimited with ";"
        final FileOutputStream serFos = new FileOutputStream(EXPORT_SERIALIZED_FILE_PATH);

        final File statsFile = new File(IMPORT_CSV_PATH);
        final Scanner scanner = new Scanner(statsFile);

        //todo: supply credentials before launch
        final BasicAWSCredentials creds = new BasicAWSCredentials("", "");

        final Counter lineCounter = new Counter();
        System.out.println(" Composing jobs...");
        final Map<Long, LabDataStats> statsByLab = Collections.synchronizedMap(new HashMap<Long, LabDataStats>());

        final File outputFile = new File(EXPORT_CSV_FILE_PATH);
        final FileOutputStream fos = new FileOutputStream(outputFile);
        final PrintWriter outWriter = new PrintWriter(fos);
        outWriter.println("Lab ID,Lab name,Total RAW Files,RAW Files Size,Total translated,functions,Total translated function size");
        outWriter.flush();

        final LinkedList<Callable<Object>> callables = new LinkedList<Callable<Object>>();
        while (scanner.hasNextLine()) {
            lineCounter.increment();
            if (lineCounter.get() == 0) {
                scanner.nextLine();
                continue; //skip header
            }

            final String rawData = scanner.nextLine();

            final long currentLine = lineCounter.get();
            callables.add(new Callable<Object>() {
                @Override
                public Object call() {

                    if (currentLine % 100 == 0) {
                        System.out.println(" --- Processing item # " + currentLine);
                    }

                    final AmazonS3Client s3Client = new AmazonS3Client(creds);

                    final long labId;
                    final String labName;
                    final long fileId;
                    final String rawPath;
                    final String translatedFunctionPath;


                    try {
                        final String[] parts = rawData.split(";");
                        labId = Long.parseLong(parts[0]);
                        labName = parts[1];
                        fileId = Long.parseLong(parts[2]);
                        rawPath = parts[5];
                        translatedFunctionPath = parts[6];
                    } catch (NumberFormatException e) {
                        System.err.println("Parse error in line " + currentLine + ". Data: " + rawData);
                        e.printStackTrace();
                        return null;
                    }

                    if (!statsByLab.containsKey(labId)) {
                        statsByLab.put(labId, new LabDataStats(labName));
                    }
                    final LabDataStats labDataStats = statsByLab.get(labId);

                    final boolean usedInTranslation = !"\"null\"".equalsIgnoreCase(translatedFunctionPath);
                    if (usedInTranslation) {
                        long translatedFunctionSize = calculateFolderSize(s3Client, translatedFunctionPath);
                        labDataStats.getTranslatedDataFileSizes().add(translatedFunctionSize);
                    }

                    if (!labDataStats.getFileIds().contains(fileId)) {
                        long rawDataSize = calculateFolderSize(s3Client, rawPath);
                        labDataStats.getFileIds().add(fileId);
                        labDataStats.getTotalRawFileSize().add(rawDataSize);
                        if(usedInTranslation) {
                            labDataStats.getRawFilesTranslatedSize().add(rawDataSize);
                        }
                    }
                    return null;
                }
            });
        }
        System.out.println("Calling all the jobs. Total jobs: " + callables.size());
        executorService.invokeAll(callables);
        System.out.println(" Statistics collected.");


        final List<Long> allRawFileSizes = new LinkedList<Long>();
        final List<Long> rawFileTranslatedSizes = new LinkedList<Long>();
        final List<Long> translatedFunctionsDataSizes = new LinkedList<Long>();
        for (Long labId : statsByLab.keySet()) {
            final LabDataStats labData = statsByLab.get(labId);
            long totalRawDataSize = 0;
            final List<Long> rawDataFileSizes = labData.getTotalRawFileSize();
            for (Long rawDataFileSize : rawDataFileSizes) {
                totalRawDataSize += rawDataFileSize;
            }

            long totalTranslatedDataSize = 0;
            final List<Long> translatedSizes = labData.getTranslatedDataFileSizes();
            for (Long translatedSize : translatedSizes) {
                totalTranslatedDataSize += translatedSize;
            }

            long rawFilesUsedInTranslationSize = 0;
            final List<Long> rawFilesUsedInTranslationSizes = labData.getRawFilesTranslatedSize();
            for (Long rawDataFileSize : rawFilesUsedInTranslationSizes) {
                rawFilesUsedInTranslationSize += rawDataFileSize;
            }


            allRawFileSizes.add(totalRawDataSize);
            rawFileTranslatedSizes.add(totalRawDataSize);
            translatedFunctionsDataSizes.add(totalTranslatedDataSize);

            outWriter.println(Joiner.on(";").join(Lists.newArrayList(labId, labData.getLabName(),
                    labData.getTotalRawFileSize().size(), formatToGB(totalRawDataSize), formatToGB(rawFilesUsedInTranslationSize),
                    labData.getTranslatedDataFileSizes().size(), formatToGB(totalTranslatedDataSize))));

            System.out.println(" + Lab ID: " + labId
                    + " \n ---- Lab name: " + labData.getLabName()
                    + " \n ---- Total RAW files: " + labData.getTotalRawFileSize().size()
                    + " \n ---- Total RAW size: " + formatToGB(totalRawDataSize)
                    + " \n ---- Original RAW size used in translation: " + formatToGB(rawFilesUsedInTranslationSize)
                    + " \n ---- Total functions: " + labData.getTranslatedDataFileSizes().size()
                    + " \n ---- Total function data size: " + formatToGB(totalTranslatedDataSize) + "\n"
            );
        }
        outWriter.flush();
        fos.close();

        System.out.println(" ------- Summary -------");
        System.out.println(" -> Max raw data size: " + formatToGB(Collections.max(allRawFileSizes)));
        System.out.println(" -> Min raw data size: " + formatToGB(Collections.min(allRawFileSizes)));

        System.out.println("------");
        System.out.println(" -> Min raw data used in translation size: " + formatToGB(Collections.min(rawFileTranslatedSizes)));
        System.out.println(" -> Min raw data used in translation size: " + formatToGB(Collections.min(rawFileTranslatedSizes)));

        System.out.println("------");
        System.out.println(" -> Max raw translated data size: " + formatToGB(Collections.max(translatedFunctionsDataSizes)));
        System.out.println(" -> Min raw translated data size: " + formatToGB(Collections.min(translatedFunctionsDataSizes)));


        final ObjectOutputStream oos = new ObjectOutputStream(serFos);
        oos.writeObject(statsByLab);
        serFos.flush();
        serFos.close();

        executorService.shutdownNow();
        final long endTime = System.currentTimeMillis();
        System.out.println("Time taken: " + ((double)(endTime - startTime) / (60 * 1000)) + " min.");
    }

    private static String formatToGB(double totalRawDataSize) {
        final double rawSizeInGb = totalRawDataSize / (1024 * 1024 * 1024);
        return rawSizeInGb + " GBytes";
    }

    private static long calculateFolderSize(AmazonS3Client s3Client, String rawPath) {
        if (rawPath.startsWith("\"")) {
            rawPath = rawPath.substring(1);
        }
        if (rawPath.endsWith("\"")) {
            rawPath = rawPath.substring(0, rawPath.length() - 1);
        }
        final ObjectListing objectListing = s3Client.listObjects(BUCKET, rawPath);
        final List<S3ObjectSummary> summaries = objectListing.getObjectSummaries();
        long totalSize = 0;
        for (S3ObjectSummary summary : summaries) {
            totalSize += summary.getSize();
        }
        return totalSize;
    }

    private static class LabDataStats implements Serializable {
        private final String labName;
        private Set<Long> fileIds = Collections.synchronizedSet(new HashSet<Long>());
        private List<Long> totalRawFileSize = Collections.synchronizedList(new LinkedList<Long>());
        private List<Long> rawFilesTranslatedSize = Collections.synchronizedList(new LinkedList<Long>());
        private List<Long> translatedDataFileSizes = Collections.synchronizedList(new LinkedList<Long>());

        private LabDataStats(String labName) {
            this.labName = labName;
        }

        public List<Long> getTotalRawFileSize() {
            return totalRawFileSize;
        }

        public List<Long> getTranslatedDataFileSizes() {
            return translatedDataFileSizes;
        }

        public Set<Long> getFileIds() {
            return fileIds;
        }


        public List<Long> getRawFilesTranslatedSize() {
            return rawFilesTranslatedSize;
        }

        public String getLabName() {
            return labName;
        }
    }

    private static class Counter {
        private long value = 0;

        synchronized long get() {
            return value;
        }

        synchronized void increment() {
            value++;
        }
    }


}
