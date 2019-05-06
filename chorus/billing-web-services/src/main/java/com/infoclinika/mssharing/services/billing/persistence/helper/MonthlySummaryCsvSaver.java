package com.infoclinika.mssharing.services.billing.persistence.helper;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.infoclinika.mssharing.model.internal.entity.payment.LabPaymentAccount;
import com.infoclinika.mssharing.model.internal.repository.LabPaymentAccountRepository;
import com.infoclinika.mssharing.services.billing.persistence.enity.*;
import com.infoclinika.mssharing.services.billing.persistence.enity.storage.AnalyzableStorageUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.storage.ArchiveStorageUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.storage.DailyAnalyzableStorageUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.storage.DailyArchiveStorageUsage;
import com.infoclinika.mssharing.services.billing.persistence.repository.FeatureUsageRepository;
import com.infoclinika.mssharing.services.billing.persistence.repository.MonthlySummaryRepository;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.infoclinika.mssharing.model.internal.read.Transformers.transformFeature;
import static java.io.File.separator;
import static java.lang.String.join;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.iterate;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.joda.time.DateTimeZone.forTimeZone;

/**
 * @author Herman Zamula
 */
@Component
public class MonthlySummaryCsvSaver {

    public static final String SUMMARY_CSV = "summary.csv";
    private static final Logger LOG = LoggerFactory.getLogger(MonthlySummaryCsvSaver.class);
    private static final String COLUMN_DELIMETER = ",";
    private static final String ROW_DELIMETER = "\n";
    private static final String HOURS = "hours";
    private static final String EXPERIMENT_ID = "experiment id";
    private static final String EXPERIMENT_NAME = "experiment name";
    private static final String ARCHIVE = "archive";
    private static final String UNKNOWN = "unknown";
    private static final String TOTAL = "Total";
    private static final String BALANCE = "Balance";
    private static final String UTF_8 = "UTF-8";

    public static final SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
    private final TimeZone timeZone;
    @Inject
    private LabPaymentAccountRepository accountRepository;
    @Inject
    private MonthlySummaryRepository monthlySummaryRepository;
    @Inject
    private Collection<FeatureUsageRepository<?>> featureUsageRepositories = newArrayList();

    private final TransactionTemplate tt;

    private final Function<ChargeableItemUsage, String> defaultUsageLineParser;

    private final Map<Class<?>, Function<ChargeableItemUsage, String>> usageLineParsers;

    private String defaultUsageLineHeader = join(
        COLUMN_DELIMETER,
        "id",
        "balance",
        "bytes",
        "charge",
        "day",
        "file",
        "file name",
        "instrument",
        "lab",
        "scaled to pay value",
        "timestamp",
        "user",
        "user id"
    );

    private final Map<Class<?>, String> usageLineHeaders = ImmutableMap.of(

        DailyAnalyzableStorageUsage.class, join(
            COLUMN_DELIMETER,
            defaultUsageLineHeader,
            HOURS,
            "total price",
            "translated bytes",
            "translated charge"
        ),

        DailyArchiveStorageUsage.class, join(COLUMN_DELIMETER, defaultUsageLineHeader, HOURS),

        PublicDownloadUsage.class, join(COLUMN_DELIMETER, defaultUsageLineHeader, ARCHIVE)

    );


    private final Map<Class<?>, String> nameMap = ImmutableMap.<Class<?>, String>builder()
        .put(DailyAnalyzableStorageUsage.class, "analyzable_storage.csv")
        .put(DailyArchiveStorageUsage.class, "archive_storage.csv")
        .put(DownloadUsage.class, "download.csv")
        .put(PublicDownloadUsage.class, "public_download.csv")
        .build();

    @Inject
    protected MonthlySummaryCsvSaver(TimeZone timeZone,
                                     PlatformTransactionManager ptm) {
        this.timeZone = timeZone;
        this.tt = new TransactionTemplate(ptm);
        this.tt.setReadOnly(true);

        DateTimeZone dateTimeZone = forTimeZone(timeZone);

        DateTimeFormatter pattern = DateTimeFormat.forPattern("yyyy-MM-dd");

        this.defaultUsageLineParser = (itemUsage) -> on(COLUMN_DELIMETER).join(
            ofNullable(itemUsage.getId()).orElse(0L),
            ofNullable(itemUsage.getBalance()).orElse(0L),
            ofNullable(itemUsage.getBytes()).orElse(0L),
            ofNullable(itemUsage.getCharge()).orElse(0L),
            ofNullable(itemUsage.getDay()).orElse(0L),
            ofNullable(itemUsage.getFile()).orElse(0L),
            ofNullable(itemUsage.getFileName()).orElse(UNKNOWN),
            ofNullable(itemUsage.getInstrument()).orElse(UNKNOWN),
            ofNullable(itemUsage.getLab()).orElse(0L),
            ofNullable(itemUsage.getScaledToPayValue()).orElse(0L),
            ofNullable(itemUsage.getTimestampDate()).map(date -> pattern.print(new DateTime(date, dateTimeZone)))
                .orElse(UNKNOWN),
            ofNullable(itemUsage.getUsedBy()).orElse(UNKNOWN),
            ofNullable(itemUsage.getUser()).orElse(0L)
        );

        this.usageLineParsers = ImmutableMap.of(

            DailyAnalyzableStorageUsage.class, itemUsage -> {
                final AnalyzableStorageUsage storageUsage = (AnalyzableStorageUsage) itemUsage;
                return on(COLUMN_DELIMETER).join(
                    defaultUsageLineParser.apply(storageUsage),
                    ofNullable(storageUsage.getHours()).orElse(0),
                    ofNullable(storageUsage.getTotalPrice()).orElse(0L)
                );
            },

            DailyArchiveStorageUsage.class, itemUsage -> {
                final ArchiveStorageUsage storageUsage = (ArchiveStorageUsage) itemUsage;
                return on(COLUMN_DELIMETER)
                    .join(defaultUsageLineParser.apply(storageUsage), ofNullable(storageUsage.hours).orElse(0));
            },

            PublicDownloadUsage.class, itemUsage -> {
                final PublicDownloadUsage storageUsage = (PublicDownloadUsage) itemUsage;
                return on(COLUMN_DELIMETER)
                    .join(defaultUsageLineParser.apply(storageUsage), storageUsage.isOnArchive());
            }

        );
    }

    public File saveUsagesToCsv(Date month) {

        LOG.debug("Start saving usages as csv for the date month: {}", month);

        final DateTime dateTime = new DateTime(month, forTimeZone(timeZone));
        final DateTime startOfMonth = dateTime.dayOfMonth().withMinimumValue().millisOfDay().withMinimumValue();
        final DateTime endOfMonth = dateTime.dayOfMonth().withMaximumValue().minuteOfDay().withMaximumValue();

        LOG.debug("Start of month: {}, end of month: {}", startOfMonth, endOfMonth);

        final File contentDir = Files.createTempDir();

        saveSummary(startOfMonth.toDate(), contentDir);

        saveDetailedUsages(startOfMonth, endOfMonth, contentDir);

        LOG.debug("Saving usages as csv has been completed for the date month: {}", month);

        return contentDir;

    }

    private void saveDetailedUsages(DateTime startOfMonth, DateTime endOfMonth, File contentDir) {

        LOG.debug("Saving detailed statistics for month: {}", startOfMonth);

        final List<LabPaymentAccount> labs = newArrayList(accountRepository.findAll());

        LOG.debug("Found {} accounts", labs.size());

        iterate(startOfMonth, dateTime -> dateTime.plusDays(1))
            .limit(endOfMonth.getDayOfMonth())
            .forEach((day -> {

                final DateTime nextDay = day.plusDays(1);

                labs.stream()
                    .flatMap(account -> featureUsageRepositories.stream()
                        .map(repo -> repo.findByLab(account.getLab().getId(), day.getMillis(), nextDay.getMillis())))
                    .forEach(usages -> saveUsagesAsCsv(usages, day, contentDir));

            }));

    }

    private void saveUsagesAsCsv(List<? extends ChargeableItemUsage> usages, DateTime currentDate, File contentDir) {

        usages.stream().findFirst().ifPresent(itemUsage -> {

            LOG.info("Saving usages for lab: {}. Day: {}", itemUsage.getLab(), currentDate);

            final Class<? extends ChargeableItemUsage> itemUsageClass = itemUsage.getClass();

            final Stream<String> stringStream = usages.stream().map(usage ->
                usageLineParsers.getOrDefault(itemUsageClass, defaultUsageLineParser).apply(usage));

            final String name = currentDate.getDayOfMonth() + "_" + nameMap.get(itemUsageClass);
            final File file = createFile(contentDir, itemUsage.getLab(), currentDate, name);

            final BufferedWriter writer = newWriter(file);

            write(writer, join(
                ROW_DELIMETER,
                usageLineHeaders.getOrDefault(itemUsageClass, defaultUsageLineHeader),
                stringStream.collect(joining(ROW_DELIMETER))
                )
            );

            closeQuietly(writer);

        });

    }

    protected void saveSummary(Date startOfMonth, File contentDir) {

        LOG.debug("Saving summary statistics for month: {}", startOfMonth);

        tt.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                final List<MonthlySummary> monthRecords = monthlySummaryRepository.findForMonth(startOfMonth);
                final Map<File, MonthlySummary> fileToMonthSummary =
                    asFileToMonthRecordMap(monthRecords, contentDir, startOfMonth);

                fileToMonthSummary.entrySet()
                    .stream()
                    .forEach(summaryEntry -> {
                        final BufferedWriter bufferedWriter = newWriter(summaryEntry.getKey());
                        final String featuresContent = parseMonthlySummaryContent(summaryEntry.getValue());
                        write(bufferedWriter, featuresContent);
                        closeQuietly(bufferedWriter);
                    });
            }
        });

    }

    private static Map<File, MonthlySummary> asFileToMonthRecordMap(Iterable<MonthlySummary> monthRecords,
                                                                    File contentDir, Date startOfMonth) {
        final Map<File, MonthlySummary> fileToMonthSummary = newHashMap();
        for (MonthlySummary monthRecord : monthRecords) {
            final File key = createFile(contentDir, monthRecord.getLabId(), new DateTime(startOfMonth), SUMMARY_CSV);
            if (fileToMonthSummary.containsKey(key)) {
                throw new IllegalStateException("Duplicated record in the map, file: " + key);
            }
            fileToMonthSummary.put(key, monthRecord);

        }
        return fileToMonthSummary;
    }

    private void write(BufferedWriter bufferedWriter, String featuresContent) {
        try {
            bufferedWriter.write(featuresContent);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String parseMonthlySummaryContent(MonthlySummary value) {

        return join(
            ROW_DELIMETER,
            getMonthlySummaryContentByFeature(value),
            join(COLUMN_DELIMETER, TOTAL, Long.toString(value.getMonthlyTotal())),
            join(COLUMN_DELIMETER, BALANCE, Long.toString(value.getEndMonthBalance()))
        );
    }

    private String getMonthlySummaryContentByFeature(MonthlySummary value) {

        return value.getTotalByFeature().entrySet().stream()
            .sorted(Comparator.comparing(Map.Entry::getKey))
            .map((entry) ->
                on(COLUMN_DELIMETER).join(transformFeature(entry.getKey()), entry.getValue()))
            .collect(joining(ROW_DELIMETER));

    }

    private BufferedWriter newWriter(File lab) {
        try {
            return Files.newWriter(lab, Charset.forName(UTF_8));
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    private static File createFile(File tempDir, long labId, DateTime month, String name) {
        final File file =
            new File(tempDir, on(separator).join(Long.toString(labId), monthFormat.format(month.toDate()), name));
        try {
            Files.createParentDirs(file);
            file.createNewFile();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return file;
    }


}
