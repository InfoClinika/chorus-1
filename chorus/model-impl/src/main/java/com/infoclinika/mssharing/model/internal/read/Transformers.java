package com.infoclinika.mssharing.model.internal.read;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.*;
import com.infoclinika.mssharing.model.PaginationItems;
import com.infoclinika.mssharing.model.PaginationItems.AdvancedFilterQueryParams.AdvancedFilterPredicateItem;
import com.infoclinika.mssharing.model.helper.*;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.entity.*;
import com.infoclinika.mssharing.model.internal.entity.payment.*;
import com.infoclinika.mssharing.model.internal.entity.restorable.*;
import com.infoclinika.mssharing.model.internal.entity.view.ExperimentDashboardRecord;
import com.infoclinika.mssharing.model.internal.entity.view.ProjectDashboardRecord;
import com.infoclinika.mssharing.model.internal.entity.workflow.PrepToExperimentSample;
import com.infoclinika.mssharing.model.internal.repository.*;
import com.infoclinika.mssharing.model.read.*;
import com.infoclinika.mssharing.model.write.AnnotationItem;
import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.model.DefaultTransformers;
import com.infoclinika.mssharing.platform.model.helper.PagedItemsTransformerTemplate;
import com.infoclinika.mssharing.platform.model.helper.read.ExperimentReaderHelper;
import com.infoclinika.mssharing.platform.model.helper.read.FileReaderHelper;
import com.infoclinika.mssharing.platform.model.helper.read.ProjectReaderHelper;
import com.infoclinika.mssharing.platform.model.read.AccessLevel;
import com.infoclinika.mssharing.platform.model.read.ExperimentReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.FileReaderTemplate;
import com.infoclinika.mssharing.propertiesprovider.ChorusPropertiesProvider;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingChargeType;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Sets.newHashSet;
import static com.infoclinika.mssharing.model.helper.ExperimentSampleTypeItem.*;
import static com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem.ChargeType;
import static com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem.Feature.*;
import static com.infoclinika.mssharing.model.read.AdministrationToolsReader.*;
import static com.infoclinika.mssharing.model.read.PaymentHistoryReader.HistoryItemType.*;
import static com.infoclinika.mssharing.model.write.UploadAppManagement.CompleteAction;
import static com.infoclinika.mssharing.model.write.UploadAppManagement.Configuration;
import static com.infoclinika.mssharing.platform.entity.AnnotationTemplate.Type.INTEGER;
import static com.infoclinika.mssharing.platform.model.read.AccessLevel.*;
import static com.infoclinika.mssharing.platform.model.read.LabReaderTemplate.LabLineTemplate;
import static com.infoclinika.mssharing.services.billing.rest.api.model.BillingChargeType.PER_GB;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;


/**
 * @author Pavel Kaplin
 */
@Component
public class Transformers extends DefaultTransformers {
    private static final Logger LOGGER = LoggerFactory.getLogger(Transformers.class);

    public static final String SPECIE_ID_FIELD = "specieId";
    public static final String INSTRUMENT_MODEL_ID_FIELD = "instrumentModelId";
    public static final String INSTRUMENT_ID_FIELD = "instrumentId";
    public static final String LABORATORY_ID_FIELD = "laboratoryId";

    @Inject
    private BillingFeaturesHelper billingFeaturesHelper;
    @Inject
    private ExperimentRepository experimentRepository;
    @Inject
    private FileMetaDataRepository fileRepository;
    @Inject
    private UserRepository userRepository;
    @Inject
    private RawFilesRepository experimentFileRepository;
    @Inject
    private RuleValidator ruleValidator;
    @Inject
    private ProjectReaderHelper<ActiveProject, ProjectLine> projectReaderHelper;
    @Inject
    private FileReaderHelper<ActiveFileMetaData, FileLine> fileReaderHelper;
    @Inject
    private ExperimentReaderHelper<ActiveExperiment, ExperimentLine> experimentReaderHelper;
    @Inject
    private ChorusPropertiesProvider chorusPropertiesProvider;

    public static final Function<String, String> STRIP_TO_EMPTY_FN = input -> StringUtils.stripToEmpty(input);

    public final TimeZone serverTimezone;
    public final SimpleDateFormat historyLineDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
    public final SimpleDateFormat extendedHistoryDateFormat =
        new SimpleDateFormat("MMM dd, yyyy hh:mm:ss z", Locale.ENGLISH);


    public static final Function<ChargeableItem, BillingFeature> BILLING_FEATURE_TRANSFORMER =
        item -> transformFeature(item.getFeature());

    public static final Function<AccountChargeableItemData, ChargeableItem> CHARGEABLE_ITEM_FROM_ACCOUNT_TRANSFORMER =
        input -> input.getChargeableItem();

    public static BillingChargeType transformChargeType(ChargeType chargeType) {
        switch (chargeType) {
            case GB:
                return PER_GB;
            default:
                throw new AssertionError("Unknown charge type: " + chargeType);
        }
    }

    public static final Function<ExperimentFileTemplate, AbstractFileMetaData> RAW_META_DATA_TRANSFORMER =
        input -> (AbstractFileMetaData) input.getFileMetaData();

    public static final Function<ExperimentFileTemplate, Long> RAW_FILES_META_ID_TRANSFORMER =
        input -> RAW_META_DATA_TRANSFORMER.apply(input).getId();

    @Inject
    public Transformers(TimeZone serverTimezone) {
        this.serverTimezone = serverTimezone;
        historyLineDateFormat.setTimeZone(serverTimezone);
        extendedHistoryDateFormat.setTimeZone(serverTimezone);
    }

    public Function<ActiveExperiment, ExperimentReaderTemplate.ExperimentLineTemplate> defaultExperimentTransformer() {
        return experimentReaderHelper.getDefaultTransformer();
    }

    public static final String FILE_PARAM = "file";
    public static final String EXPERIMENT_PARAM = "experiment";
    public static final Function<NewsItem, NewsLine> TO_NEWS_LINE =
        input -> new NewsLine(
            input.getId(),
            input.getTitle(),
            input.getAuthor(),
            input.getCreationDate()
        );

    public static final Comparator<NewsLine> NEWS_BY_DATE =
        (o1, o2) -> {
            if (o1.dateCreated.equals(o2.dateCreated)) {
                return o1.hashCode() - o2.hashCode();
            }
            return o1.dateCreated.compareTo(o2.dateCreated);
        };

    public static final Comparator<PaymentHistoryReader.PaymentHistoryLine> HISTORY_LINES_BY_DATE =
        (o1, o2) -> {
            final int i = o1.date.compareTo(o2.date);
            if (i == 0) {
                return o1.hashCode() - o2.hashCode();
            }
            return i;
        };

    public static final Comparator<PaymentHistoryReader.PaymentHistoryLine> HISTORY_LINES_BY_DATE_REVERSED =
        Ordering.from(HISTORY_LINES_BY_DATE).reverse();

    private static LabLineTemplate getLabLineTemplate(Lab input) {
        return new LabLineTemplate(
            input.getId(),
            input.getName(), input.getHead().getId(),
            input.getInstitutionUrl(),
            input.getHead().getFullName(),
            input.getLastModification()
        );
    }

    public final Function<PayPalLogEntry, PaymentHistoryReader.PaymentHistoryLine> paypalHistoryLineTransformFunction =
        new Function<PayPalLogEntry, PaymentHistoryReader.PaymentHistoryLine>() {
            @Override
            public PaymentHistoryReader.PaymentHistoryLine apply(PayPalLogEntry input) {
                return new PaymentHistoryReader.PaymentHistoryLine(
                    input.getTimestamp(),
                    null, extendedHistoryDateFormat.format(input.getTimestamp()), PaymentHistoryReader.creditCharge,
                    input.getAmount(), input.getStoreBalance(),
                    PAYPAL, serverTimezone.getID(), true
                );

            }
        };

    public final Function<StoreLogEntry, PaymentHistoryReader.PaymentHistoryLine> storeHistoryLineTransformFunction =
        new Function<StoreLogEntry, PaymentHistoryReader.PaymentHistoryLine>() {
            @Override
            public PaymentHistoryReader.PaymentHistoryLine apply(StoreLogEntry input) {
                return new PaymentHistoryReader.PaymentHistoryLine(
                    input.getTimestamp(),
                    null, extendedHistoryDateFormat.format(input.getTimestamp()),
                    input.getDirection() == StoreLogEntry.Direction.IN ? PaymentHistoryReader.TOP_UP_BALANCE :
                        PaymentHistoryReader.storageCharge,
                    input.getAmount(), input.getStoreBalance(),
                    STORE, serverTimezone.getID(), true
                );

            }
        };
    public final Function<FeatureLog, PaymentHistoryReader.PaymentHistoryLine> featureHistoryLineTransformFunction =
        new Function<FeatureLog, PaymentHistoryReader.PaymentHistoryLine>() {
            @Override
            public PaymentHistoryReader.PaymentHistoryLine apply(FeatureLog input) {
                return new PaymentHistoryReader.PaymentHistoryLine(
                    input.getTimestamp(),
                    null,
                    extendedHistoryDateFormat.format(input.getTimestamp()),
                    input.action.value + " " + input.message,
                    0L, input.getStoreBalance(),
                    FEATURE, serverTimezone.getID(), true
                );
            }
        };

    public static final Function<UploadAppConfiguration, Configuration>
        TO_UPLOAD_APP_CONFIGURATION_DTO = input -> new Configuration(
        input.getId(),
        input.getName(),
        input.getFolder(),
        input.isStarted(),
        input.getLabels(),
        input.getInstrument().getId(),
        input.getSpecie().getId(),
        input.getCreated(),
        CompleteAction.valueOf(input.getCompleteAction().name()),
        input.getFolderToMoveFiles()
    );

    public static final Function<LockMz, LockMzItem> LOCK_MZ_ITEM_FUNCTION =
        input -> new LockMzItem(input.getMass(), input.getCharge());

    public static final Function<LockMzItem, LockMz> LOCK_MZ_FUNCTION = input -> {
        final LockMz lockMz = new LockMz();
        lockMz.setCharge(input.charge);
        lockMz.setMass(input.lockMass);
        return lockMz;
    };

    public static Function<Lab, LabLineTemplate> LAB_LINE_FUNCTION =
        input -> {
            if (input == null) {
                return null;
            }
            return getLabLineTemplate(input);
        };

    //    create transformer
    public static final Function<ExperimentSampleType, ExperimentSampleTypeItem> AS_SAMPLE_TYPE_ITEM =
        type -> {
            switch (type) {
                case LIGHT:
                    return LIGHT;
                case MEDIUM:
                    return MEDIUM;
                case HEAVY:
                    return HEAVY;
                case CHANNEL_1:
                    return CHANNEL_1;
                case CHANNEL_2:
                    return CHANNEL_2;
                case CHANNEL_3:
                    return CHANNEL_3;
                case CHANNEL_4:
                    return CHANNEL_4;
                case CHANNEL_5:
                    return CHANNEL_5;
                case CHANNEL_6:
                    return CHANNEL_6;
                case CHANNEL_7:
                    return CHANNEL_7;
                case CHANNEL_8:
                    return CHANNEL_8;
                case CHANNEL_9:
                    return CHANNEL_9;
                case CHANNEL_10:
                    return CHANNEL_10;
                default:
                    throw new IllegalStateException("Undefined experiment sample type: " + type);
            }
        };

    public static final Function<ExperimentSampleTypeItem, ExperimentSampleType> AS_SAMPLE_TYPE =
        type -> {
            switch (type) {
                case LIGHT:
                    return ExperimentSampleType.LIGHT;
                case MEDIUM:
                    return ExperimentSampleType.MEDIUM;
                case HEAVY:
                    return ExperimentSampleType.HEAVY;
                case CHANNEL_1:
                    return ExperimentSampleType.CHANNEL_1;
                case CHANNEL_2:
                    return ExperimentSampleType.CHANNEL_2;
                case CHANNEL_3:
                    return ExperimentSampleType.CHANNEL_3;
                case CHANNEL_4:
                    return ExperimentSampleType.CHANNEL_4;
                case CHANNEL_5:
                    return ExperimentSampleType.CHANNEL_5;
                case CHANNEL_6:
                    return ExperimentSampleType.CHANNEL_6;
                case CHANNEL_7:
                    return ExperimentSampleType.CHANNEL_7;
                case CHANNEL_8:
                    return ExperimentSampleType.CHANNEL_8;
                case CHANNEL_9:
                    return ExperimentSampleType.CHANNEL_9;
                case CHANNEL_10:
                    return ExperimentSampleType.CHANNEL_10;
                default:
                    throw new IllegalStateException("Undefined experiment sample type: " + type);
            }
        };

    public static final Function AS_SAMPLE_ITEM = (Function<PrepToExperimentSample, ExperimentSampleItem>) sample -> {
        final ExperimentSample es = sample.getExperimentSample();
        final ExperimentSampleTypeItem sampleTypeItem = AS_SAMPLE_TYPE_ITEM.apply(sample.getType());
        final List<AnnotationItem> annotationValues = annotationTemplatesToItems(es.getAnnotationValues());

        return new ExperimentSampleItem(es.getName(), sampleTypeItem, es.getFactorValues(), annotationValues);
    };

    private static List<AnnotationItem> annotationTemplatesToItems(Set<AnnotationTemplate> annotationTemplates) {
        return annotationTemplates
            .stream()
            .sorted(Comparator.comparing(AnnotationTemplate::getName))
            .map(a -> new AnnotationItem(a.getName(), a.getValue(), a.getUnits(), a.getType() == INTEGER))
            .collect(toList());
    }

    public final Function<ActiveProject, ProjectLine> projectTransformer = new Function<ActiveProject, ProjectLine>() {
        @Override
        public ProjectLine apply(ActiveProject input) {
            return new ProjectLine(projectReaderHelper.getDefaultTransformer().apply(input), input.isBlogEnabled(),
                                   Transformers.transformProjectColumns(input)
            );
        }
    };

    @Deprecated
    public final Function<ProjectDashboardRecord, ProjectLine> projectDashboardRecordTransformer =
        input -> new ProjectLine(
            input.getId(),
            input.getName(),
            input.getLastModification(),
            input.getAreaOfResearch(),
            input.getCreator().getEmail(),
            fromSharingType(input.getProject().getSharing().getType()),
            (input.getLab() == null) ? null : LAB_LINE_FUNCTION.apply(input.getLab()),
            input.getCreator().getFullName(),
            input.getProject().isBlogEnabled()
        );

    private List<ExperimentAdditionalInfoRecord> sortAccordingToIds(
        List<ExperimentAdditionalInfoRecord> list,
        List<Long> ids
    ) {

        final List<ExperimentAdditionalInfoRecord> sorted = newArrayList();

        for (Long id : ids) {
            final ExperimentAdditionalInfoRecord item = getInfoById(id, list);
            sorted.add(item);
        }

        return sorted;
    }

    private ExperimentAdditionalInfoRecord getInfoById(Long id, List<ExperimentAdditionalInfoRecord> info) {
        for (ExperimentAdditionalInfoRecord record : info) {
            if (record.experiment == id) {
                return record;
            }
        }
        throw new RuntimeException("Can't find ExperimentAdditionalInfoRecord by id: " + id);
    }

    public FluentIterable<ExperimentLine> transformExperimentRecords(
        final long actor,
        Iterable<ExperimentDashboardRecord> experiments
    ) {

        return from(experiments).transform(experimentLineTransformerFn(actor, experiments));

    }

    public Function<ExperimentDashboardRecord, ExperimentLine> experimentLineTransformerFn(
        final long actor,
        Iterable<ExperimentDashboardRecord> experiments
    ) {

        final List<Long> ids = newLinkedList();

        for (ExperimentDashboardRecord experiment : experiments) {
            ids.add(experiment.getId());
        }
        final List<ExperimentAdditionalInfoRecord> additionalInfo =
            ids.size() > 0 ? experimentRepository.getAdditionalInfo(actor, ids) : emptyList();
        final Iterator<ExperimentAdditionalInfoRecord> sortedInfoIterator =
            sortAccordingToIds(additionalInfo, ids).iterator();

        return input -> {
            final ExperimentAdditionalInfoRecord info = sortedInfoIterator.next();
            return getExperimentLine(input, info, actor);
        };
    }

    public final Function<ExperimentLine, ExperimentLine> experimentFolderStructureTransformer =
        experiment -> new ExperimentLine(
            experiment.id,
            experiment.lab,
            experiment.name,
            experiment.creator,
            experiment.project,
            experiment.files,
            experiment.modified,
            experiment.accessLevel,
            "",
            experiment.isOwner,
            false,
            false,
            false,
            false,
            experiment.canArchive,
            experiment.canUnarchive,
            experiment.analyzesCount,
            experiment.billLab,
            experiment.owner,
            new DashboardReader.ExperimentColumns(
                experiment.name,
                experiment.creator,
                experiment.lab.name,
                experiment.project,
                experiment.files,
                experiment.modified,
                experiment.failed
            ),
            experiment.experimentType,
            experiment.failed
        );

    public final Function<ViewColumn, ColumnViewHelper.ColumnInfo> viewToColumnTransformer =
        input -> {
            final ColumnDefinition columnDefinition = input.getColumnDefinition();
            columnDefinition.getType();

            return new ColumnViewHelper.ColumnInfo(
                columnDefinition.getName(),
                transformColumnNameToView(columnDefinition),
                input.getOrder(),
                columnDefinition.isHideable(),
                columnDefinition.isSortable(),
                columnDefinition.getId(),
                columnDefinition.getDataType(),
                columnDefinition.getUnits()
            );
        };

    private Map<String, String> fileColumnNameMapping = getFileColumnMapping();
    private Map<String, String> projectColumnNameMapping = createProjectColumnMapping();
    private Map<String, String> experimentColumnNameMapping = getExperimentColumnMapping();

    public static DashboardReader.ExperimentColumns transformExperimentColumns(ExperimentDashboardRecord input) {
        String labName = input.getLab() == null ? "" : input.getLab().getName();

        return new DashboardReader.ExperimentColumns(
            input.getName(),
            input.getCreator().getFullName(),
            labName,
            input.getProject().getName(),
            input.getNumberOfFiles(),
            input.getLastModification(),
            input.isFailed()
        );
    }

    public static DashboardReader.ProjectColumns transformProjectColumns(ActiveProject input) {
        String labName = input.getLab() == null ? "" : input.getLab().getName();

        return new DashboardReader.ProjectColumns(
            input.getName(),
            input.getCreator().getFullName(),
            labName,
            input.getAreaOfResearch(),
            input.getLastModification()
        );
    }

    public static DashboardReader.FileColumns transformColumns(AbstractFileMetaData input) {

        final Instrument instrument = input.getInstrument();

        DashboardReader.FileColumns fileColumns = new DashboardReader.FileColumns(
            input.getName(),
            input.getSizeInBytes(),
            instrument.getName(),
            instrument.getLab().getName(),
            input.getUploadDate(),
            input.getLabels()
        );

        // metadata
        final FileMetaAnnotations metaInfo = input.getMetaInfo();
        if (metaInfo == null) {
            return fileColumns;
        }
        //todo: remove startMz, endMz, startRt, endRt columns from the UI
        fileColumns.annotationInstrument = metaInfo.getInstrument();
        fileColumns.comment = metaInfo.getComment();
        fileColumns.creationDate = metaInfo.getCreationDate();
        fileColumns.endMz = "";
        fileColumns.endRt = "";
        fileColumns.fileCondition = metaInfo.getFileCondition();
        fileColumns.instrumentMethod = metaInfo.getInstrumentMethod();
        fileColumns.fileName = metaInfo.getFileName();
        fileColumns.instrumentSerialNumber = metaInfo.getInstrumentSerialNumber();
        fileColumns.startMz = "";
        fileColumns.userLabels = metaInfo.getUserLabels();
        fileColumns.phone = metaInfo.getPhone();
        fileColumns.seqRowPosition = metaInfo.getSeqRowPosition();
        fileColumns.sampleName = metaInfo.getSampleName();
        fileColumns.startRt = "";
        fileColumns.userName = metaInfo.getUserName();
        fileColumns.instrumentName = metaInfo.getInstrumentName();

        return fileColumns;
    }

    private ExperimentLine getExperimentLine(
        ExperimentDashboardRecord record,
        ExperimentAdditionalInfoRecord info,
        Long actor
    ) {

        final java.util.Optional<Lab> labOpt =
            java.util.Optional.ofNullable(record.getLab() != null ? record.getLab() : record.getBillLab());
        final boolean bDownloadAvailable = info.countFilesReadyToDownload == record.getNumberOfFiles();
        final boolean hasUnArchiveRequest = !bDownloadAvailable && info.countArchivedFilesRequestedForUnArchiving > 0;
        final boolean hasUnArchiveDownloadOnlyRequest =
            !bDownloadAvailable && info.countArchivedFilesRequestedForDownloadOnly > 0;
        final boolean canArchive = labOpt.isPresent() && info.canArchiveExperiment > 0 &&
            billingFeaturesHelper.isFeatureEnabled(labOpt.get().getId(), BillingFeature.ARCHIVE_STORAGE);
        final boolean canUnarchive = labOpt.isPresent() && info.canUnarchiveExperiment > 0 &&
            billingFeaturesHelper.isFeatureEnabled(labOpt.get().getId(), BillingFeature.ANALYSE_STORAGE);

        return new ExperimentLine(
            record.getId(),
            DefaultTransformers.labLineTemplateTransformer().apply(record.getLab()),
            record.getName(),
            record.getCreator().getFullName(),
            record.getProject().getName(),
            record.getNumberOfFiles(),
            record.getLastModification(),
            Transformers.fromSharingType(record.getProject().getSharing().getType()),
            getDownloadLink(record.getDownloadToken()),
            actor.equals(record.getCreator().getId()),
            info.userCanCreateExperimentsInProject > 0,
            bDownloadAvailable, hasUnArchiveRequest, hasUnArchiveDownloadOnlyRequest,
            canArchive,
            canUnarchive,
            record.getAnalyzesCount(),
            labOpt.map(EntityUtil.ENTITY_TO_ID::apply).orElse(null),
            record.getCreator().getId(),
            transformExperimentColumns(record),
            record.getExperimentType().getId(),
            record.isFailed()
        );
    }

    public static String getDownloadLink(ActiveExperiment experiment, String baseUrl, boolean isPublic) {
        if (!isPublic) {
            return baseUrl + "/download/bulk?experiment=" + experiment.getId();
        }
        final String downloadToken = experiment.getDownloadToken();
        if (downloadToken == null) {
            return null;
        }
        return baseUrl + "/anonymous/download/experiment/" + downloadToken;
    }

    public final String getDownloadLink(String experimentDownloadToken) {
        return getPublicDownloadLink(experimentDownloadToken, chorusPropertiesProvider.getBaseUrl());
    }

    public static String getPublicDownloadLink(String experimentDownloadToken, String baseUrl) {
        if (experimentDownloadToken == null) {
            return null;
        }
        return baseUrl + "/anonymous/download/experiment/" + experimentDownloadToken;
    }

    public final String getPublicDownloadLink(ActiveExperiment experiment) {
        return getDownloadLink(experiment, chorusPropertiesProvider.getBaseUrl(), true);
    }

    public static AccessLevel fromSharingType(Sharing.Type type) {
        AccessLevel accessLevel;
        switch (type) {
            case PUBLIC:
                accessLevel = PUBLIC;
                break;
            case PRIVATE:
                accessLevel = PRIVATE;
                break;
            case SHARED:
                accessLevel = SHARED;
                break;
            default:
                throw new IllegalStateException("Illegal sharing type: " + type);
        }
        return accessLevel;
    }


    public static ChargeableItem.Feature transformFeature(BillingFeature feature) {
        switch (feature) {
            case ANALYSE_STORAGE:
                return ANALYSE_STORAGE;
            case ARCHIVE_STORAGE:
                return ARCHIVE_STORAGE;
            case DOWNLOAD:
                return DOWNLOAD;
            case PUBLIC_DOWNLOAD:
                return PUBLIC_DOWNLOAD;
            case STORAGE_VOLUMES:
                return STORAGE_VOLUMES;
            case ARCHIVE_STORAGE_VOLUMES:
                return ARCHIVE_STORAGE_VOLUMES;
            default:
                throw new AssertionError("Unknown billing feature: " + feature);
        }
    }

    public static BillingFeature transformFeature(ChargeableItem.Feature feature) {
        switch (feature) {
            case ANALYSE_STORAGE:
                return BillingFeature.ANALYSE_STORAGE;
            case ARCHIVE_STORAGE:
                return BillingFeature.ARCHIVE_STORAGE;
            case DOWNLOAD:
                return BillingFeature.DOWNLOAD;
            case PUBLIC_DOWNLOAD:
                return BillingFeature.PUBLIC_DOWNLOAD;
            case STORAGE_VOLUMES:
                return BillingFeature.STORAGE_VOLUMES;
            case ARCHIVE_STORAGE_VOLUMES:
                return BillingFeature.ARCHIVE_STORAGE_VOLUMES;
            default:
                throw new AssertionError("Unknown billing feature: " + feature);
        }
    }

    public Function<ActiveFileMetaData, FileLine> transformFilesFn(
        final User actor,
        Iterable<ActiveFileMetaData> filtered
    ) {
        LOGGER.trace("*** Getting statistics to transform file line");
        ImmutableList<Long> fileIds = from(filtered).transform(EntityUtil.ENTITY_TO_ID).toList();
        final Set<Long> usedInExperiments =
            fileIds.isEmpty() ? emptySet() : newHashSet(fileRepository.usedInExperiments(fileIds));
        LOGGER.trace("*** Statistics retrieved");
        return input -> {
            AccessLevel level = ruleValidator.getAccessLevel(input);
            final Instrument instrument = input.getInstrument();
            final DashboardReader.FileColumns columns = transformColumns(input);
            final InstrumentModel model = instrument.getModel();

            final Set<DashboardReader.FileCompound> fileCompounds = getFileCompounds(input.getExternalMetadata());

            return new FileLine(input.getId(),
                                input.getName(), instrument.getId(),
                                instrument.getName(),
                                model.getId(),
                                toFullInstrumentModel(model),
                                instrument.getLab().getId(),
                                instrument.getLab().getHead().getId(),
                                input.getSpecie() == null ? null : input.getSpecie().getId(),
                                input.getContentId(),
                                input.getArchiveId(), input.getUploadId(),
                                input.getDestinationPath(),
                                input.isArchive(),
                                level,
                                usedInExperiments.contains(input.getId()),
                                input.getOwner().getId(),
                                input.getLastPingDate(),
                                columns,
                                input.isInvalid(),
                                model.getVendor().getName(),
                                transform(instrument.getLab().getUsers(), EntityUtil.ENTITY_TO_ID),
                                transformStorageStatus(
                                    input.getStorageData().getStorageStatus(),
                                    input.getStorageData().isArchivedDownloadOnly()
                                ),
                                input.isSizeConsistent(), input.isToReplace(), input.isCorrupted(),
                                model.getStudyType().getName(), fileCompounds
            );
        };
    }

    private Set<DashboardReader.FileCompound> getFileCompounds(ExternalFileMetadata externalFileMetadata) {
        if (externalFileMetadata == null || externalFileMetadata.getFileCompounds() == null) {
            return emptySet();
        }
        return Sets.newHashSet(transform(
            externalFileMetadata.getFileCompounds(),
            input -> new DashboardReader.FileCompound(
                input.getCompoundId(),
                input.getFormula(),
                input.getWeight()
            )
        ));
    }

    public static DashboardReader.StorageStatus transformStorageStatus(
        StorageData.Status storageStatus,
        boolean unArchivingForDownload
    ) {
        switch (storageStatus) {
            case ARCHIVING_REQUESTED:
                //return DashboardReader.StorageStatus.ARCHIVING_IN_PROCESS;
            case ARCHIVED:
                return DashboardReader.StorageStatus.ARCHIVED;
            case UNARCHIVING_REQUESTED:
                return (unArchivingForDownload) ? DashboardReader.StorageStatus.UN_ARCHIVING_FOR_DOWNLOAD_IN_PROCESS
                    : DashboardReader.StorageStatus.UN_ARCHIVING_IN_PROCESS;
            case UNARCHIVED:
            default:
                return DashboardReader.StorageStatus.UNARCHIVED;
        }
    }

    public static String toFullInstrumentModel(InstrumentModel model) {
        return Joiner.on(" ")
            .join(new String[] {model.getVendor().getName(), model.getType().getName(), model.getName()});
    }

    public final String getPrivateDownloadLink(ActiveExperiment experiment) {
        return getDownloadLink(experiment, chorusPropertiesProvider.getBaseUrl(), false);
    }

    private String transformColumnNameToView(ColumnDefinition columnDefinition) {
        switch (columnDefinition.getType()) {
            case FILE_META:
                return fileColumnNameMapping.get(columnDefinition.getName().toLowerCase());
            case PROJECT_META:
                return projectColumnNameMapping.get(columnDefinition.getName().toLowerCase());
            case EXPERIMENT_META:
                return experimentColumnNameMapping.get(columnDefinition.getName().toLowerCase());
            default:
                throw new AssertionError(columnDefinition.getType());
        }
    }

    public Function<ActiveFileMetaData, FileLine> transformToFileLineFunction(long actor) {
        final User actorEntity = userRepository.findOne(actor);
        return input -> transformToFileLine(actorEntity, input);
    }

    public FileLine transformToFileLine(User actor, ActiveFileMetaData input) {

        FileReaderTemplate.FileLineTemplate lineTemplate = fileReaderHelper.getDefaultTransformer().apply(input);

        if (!lineTemplate.usedInExperiments) {
            final boolean usedInExperiments = true;
            lineTemplate = new FileReaderTemplate.FileLineTemplate(
                lineTemplate.id, lineTemplate.name, lineTemplate.contentId, lineTemplate.uploadId,
                lineTemplate.destinationPath, lineTemplate.instrumentId, lineTemplate.labId,
                lineTemplate.instrumentName, lineTemplate.modelId, lineTemplate.labName, lineTemplate.owner,
                lineTemplate.labHead, lineTemplate.invalid, lineTemplate.vendorName, lineTemplate.instrumentModel,
                lineTemplate.specieId, lineTemplate.accessLevel, usedInExperiments, lineTemplate.labels,
                lineTemplate.sizeInBytes, lineTemplate.uploadDate
            );
        }

        final Instrument instrument = input.getInstrument();
        final DashboardReader.FileColumns columns = Transformers.transformColumns(input);

        final Set<DashboardReader.FileCompound> fileCompounds = getFileCompounds(input.getExternalMetadata());

        return new FileLine(
            lineTemplate,
            input.getArchiveId(),
            input.getLastPingDate(),
            input.isArchive(),
            transform(instrument.getLab().getUsers(), EntityUtil.ENTITY_TO_ID),
            transformStorageStatus(
                input.getStorageData().getStorageStatus(),
                input.getStorageData().isArchivedDownloadOnly()
            ),
            input.isSizeConsistent(), columns,
            input.isToReplace(), input.isCorrupted(), instrument.getModel().getStudyType().getName(),
            fileCompounds
        );
    }


    @Component("pagedItemsTransformer")
    public static class PagedItemsTransformer extends PagedItemsTransformerTemplate {
        public static class FieldInStorageDescription {
            public final String name;
            public final Class aClass;

            public FieldInStorageDescription(String name, Class clazz) {
                this.name = name;
                this.aClass = clazz;
            }
        }

        public PagedItemsTransformer() {
            this.sortingOverride(new HashMap<Class<?>, Map<String, String>>() {
                {
                    put(ActiveFileMetaData.class, new HashMap<String, String>() {
                        {
                            put("id", "id");
                            put("uploadDate", "uploadDate");
                            put("name", "name");
                            put("sizeInBytes", "sizeInBytes");
                            put("instrument", "instrument.name");
                            put("labels", "labels");
                            put("laboratory", "instrument.lab.name");
                            put("modified", "lastModification");
                        }
                    });
                    put(ExperimentFileTemplate.class, new HashMap<String, String>() {
                        {
                            put("id", "fileMetaData.id");
                            put("uploadDate", "fileMetaData.uploadDate");
                            put("name", "fileMetaData.name");
                            put("sizeInBytes", "fileMetaData.sizeInBytes");
                            put("instrument", "fileMetaData.instrument.name");
                            put("labels", "fileMetaData.labels");
                            put("laboratory", "fileMetaData.instrument.lab.name");
                        }
                    });
                    put(ProjectDashboardRecord.class, new HashMap<String, String>() {
                        {
                            put("id", "id");
                            put("name", "name");
                            put("owner", "creator.personData.firstName");
                            put("laboratory", "labName");
                            put("area", "areaOfResearch");
                            put("modified", "lastModification");
                        }
                    });
                    put(ActiveProject.class, new HashMap<String, String>() {
                        {
                            put("id", "id");
                            put("name", "name");
                            put("owner", "creator.personData.firstName");
                            put("laboratory", "lab.name");
                            put("area", "areaOfResearch");
                            put("modified", "lastModification");
                        }
                    });
                    put(ActiveExperiment.class, new HashMap<String, String>() {
                        {
                            put("id", "id");
                            put("name", "name");
                            put("owner", "creator.personData.firstName");
                            put("laboratory", "lab.name");
                            put("project", "project.name");
                            put("modified", "lastModification");
                            put("failed", "failed");
                        }
                    });
                    put(ExperimentDashboardRecord.class, new HashMap<String, String>() {
                        {
                            put("id", "id");
                            put("name", "name");
                            put("owner", "creator.personData.firstName");
                            put("laboratory", "labName");
                            put("project", "p.name");
                            put("modified", "lastModification");
                            put("files", "numberOfFiles");
                            put("failed", "failed");
                        }
                    });
                    put(Instrument.class, new HashMap<String, String>() {
                        {
                            put("id", "id");
                            put("name", "name");
                            put("model", "model.name");
                            put("serialNumber", "serialNumber");
                            put("laboratory", "lab.name");
                        }
                    });
                    put(LabPaymentAccount.class, new HashMap<String, String>() {
                        {
                            put("id", "id");
                            put("name", "lab.name");
                        }
                    });
                    put(FileAccessLog.class, new HashMap<String, String>() {
                        {
                            put("id", "id");
                            put("fileName", "fileName");
                            put("userEmail", "userEmail");
                            put("fileSize", "fileSize");
                            put("operationType", "operationType");
                            put("operationDate", "operationDate");
                        }
                    });
                }
            });
        }

        public static String toFilterQuery(PaginationItems.PagedItemInfo pagedInfo) {
            if (StringUtils.isEmpty(pagedInfo.filterQuery)) {
                return "%";
            }
            return "%" + pagedInfo.filterQuery + "%";
        }

        public static String resolvePredicateForAdvancedSearch(
            Class<?> entity,
            AdvancedFilterPredicateItem predicateItem
        ) {
            final Map<String, FieldInStorageDescription> map = ADVANCED_FILTER_FIELDS_MAPPING.get(entity);
            if (map == null) {
                throw new IllegalArgumentException("Unknown entity type to make an advanced search");
            }
            FieldInStorageDescription fieldDescription = map.get(predicateItem.prop);
            if (fieldDescription == null) {
                throw new IllegalArgumentException("Unknown field to make an advanced search");
            }
            final String field = fieldDescription.name;
            final SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
            final SimpleDateFormat dateFormatToSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dateField;
            try {
                switch (predicateItem.operator) {
                    case EQUAL:
                        if (!Number.class.isAssignableFrom(fieldDescription.aClass)) {
                            return field + "='" + escapeSql(predicateItem.value) + "'";
                        } else {
                            return field + "=" + predicateItem.value;
                        }
                    case NOT_EQUAL:
                        if (!Number.class.isAssignableFrom(fieldDescription.aClass)) {
                            return field + "!='" + predicateItem.value + "'";
                        } else {
                            return field + "!=" + predicateItem.value;
                        }

                    case GREATER_THAN:
                        return field + ">" + predicateItem.value;
                    case LESS_THAN:

                        return field + "<" + predicateItem.value;
                    case IS_IN:
                        final String[] wantedOccurencies = predicateItem.value.split("\n");
                        if (!Number.class.isAssignableFrom(fieldDescription.aClass)) {
                            final List<String> conditions = newLinkedList();
                            for (String notOperatedValue : wantedOccurencies) {
                                conditions.add(field + " like '%" + escapeSql(notOperatedValue) + "%'");
                            }
                            return Joiner.on(" or ").join(conditions);
                        } else {
                            final List<Number> wantedOccurenciesOperated = newArrayList();
                            for (String notOperatedValue : wantedOccurencies) {
                                wantedOccurenciesOperated.add(Double.parseDouble(notOperatedValue));
                            }
                            return field + " in (" + Joiner.on(",").join(wantedOccurenciesOperated) + ")";
                        }

                    case IS_NOT_IN:
                        final String[] notWantedOccurencies = predicateItem.value.split("\n");
                        if (!Number.class.isAssignableFrom(fieldDescription.aClass)) {
                            final List<String> conditions = newLinkedList();
                            for (String notOperatedValue : notWantedOccurencies) {
                                conditions.add(field + " not like '%" + escapeSql(notOperatedValue) + "%'");
                            }
                            return Joiner.on(" and ").join(conditions);
                        } else {
                            final List<Number> notWantedOccurenciesOperated = newArrayList();
                            for (String notOperatedValue : notWantedOccurencies) {
                                notWantedOccurenciesOperated.add(Double.parseDouble(notOperatedValue));
                            }
                            return field + " not in (" + Joiner.on(",").join(notWantedOccurenciesOperated) + ")";
                        }

                    case BEGINS_WITH:
                        return field + " like '" + escapeSql(predicateItem.value) + "%'";
                    case ENDS_WITH:
                        return field + " like '%" + escapeSql(predicateItem.value) + "'";
                    case CONTAINS:
                        return field + " like '%" + escapeSql(predicateItem.value) + "%'";
                    case NOT_CONTAINS:
                        return field + " not like '%" + escapeSql(predicateItem.value) + "%'";
                    case IS_EMPTY:
                        return " (" + field + " is null or " + field + "='')";
                    case IS_NOT_EMPTY:
                        return " not (" + field + " is null or " + field + "='')";
                    case TRUE:
                        return field + " is true";
                    case FALSE:
                        return field + " is false";
                    case IS_ON:
                        dateField = dateFormatter.parse(predicateItem.value);
                        return "(" + field + ">'" + dateFormatToSql.format(getCalendarDateWithStartTime(dateField)) +
                            "' AND " + field + "<'" + dateFormatToSql.format(getCalendarDateWithEndTime(dateField)) +
                            "')";
                    case IS_AFTER:
                        dateField = dateFormatter.parse(predicateItem.value);
                        return field + ">'" + dateFormatToSql.format(getCalendarDateWithEndTime(dateField)) + "'";
                    case IS_ON_AND_AFTER:
                        dateField = dateFormatter.parse(predicateItem.value);
                        return field + ">='" + dateFormatToSql.format(getCalendarDateWithStartTime(dateField)) + "'";
                    case IS_ON_OR_BEFORE:
                        dateField = dateFormatter.parse(predicateItem.value);
                        return field + "<='" + dateFormatToSql.format(getCalendarDateWithEndTime(dateField)) + "'";
                    case IS_BEFORE:
                        dateField = dateFormatter.parse(predicateItem.value);
                        return field + "<'" + dateFormatToSql.format(getCalendarDateWithStartTime(dateField)) + "'";
                    case IS_TODAY:
                        final Date currentDate = new Date();
                        return "(" + field + ">'" + dateFormatToSql.format(getCalendarDateWithStartTime(currentDate)) +
                            "' AND " + field + "<'" + dateFormatToSql.format(getCalendarDateWithEndTime(currentDate)) +
                            "')";
                    case IS_YESTERDAY:
                        Calendar yesterdayCal = Calendar.getInstance();
                        yesterdayCal.add(Calendar.DATE, -1);
                        final Date yesterday = yesterdayCal.getTime();
                        return "(" + field + ">'" + dateFormatToSql.format(getCalendarDateWithStartTime(yesterday)) +
                            "' AND " + field + "<'" + dateFormatToSql.format(getCalendarDateWithEndTime(yesterday)) +
                            "')";
                    case IS_IN_WEEK:
                        Calendar inWeek = Calendar.getInstance();
                        inWeek.add(Calendar.DATE, -7);
                        return field + ">='" + dateFormatToSql.format(getCalendarDateWithStartTime(inWeek.getTime())) +
                            "'";
                    default:
                        throw new IllegalStateException("Operator is undefined:" + predicateItem.operator);
                }
            } catch (ParseException e) {
                LOGGER.error("Parse exception during query creation: " + e.getMessage(), e);
                throw new RuntimeException("Wrong date format: " + predicateItem.value);
            }
        }

        private static String escapeSql(String str) {
            if (str == null) {
                return null;
            }
            return StringUtils.replace(str, "'", "''");
        }

        private static Date getCalendarDateWithEndTime(Date dateField) throws ParseException {
            Calendar calendarDateEnd = Calendar.getInstance();
            calendarDateEnd.setTime(dateField);
            calendarDateEnd.set(Calendar.HOUR_OF_DAY, 23);
            calendarDateEnd.set(Calendar.MINUTE, 59);
            calendarDateEnd.set(Calendar.SECOND, 59);
            return calendarDateEnd.getTime();
        }

        private static Date getCalendarDateWithStartTime(Date dateField) throws ParseException {
            Calendar calendarDateEnd = Calendar.getInstance();
            calendarDateEnd.setTime(dateField);
            calendarDateEnd.set(Calendar.HOUR_OF_DAY, 0);
            calendarDateEnd.set(Calendar.MINUTE, 0);
            calendarDateEnd.set(Calendar.SECOND, 0);
            return calendarDateEnd.getTime();
        }

        private static final Map<Class<?>, Map<String, FieldInStorageDescription>> ADVANCED_FILTER_FIELDS_MAPPING =
            new HashMap<Class<?>, Map<String, FieldInStorageDescription>>() {
                {
                    put(ExperimentDashboardRecord.class, new HashMap<String, FieldInStorageDescription>() {
                        {
                            put("id", new FieldInStorageDescription("e.id", Number.class));
                            put("name", new FieldInStorageDescription("e.name", String.class));
                            put("owner", new FieldInStorageDescription("e.creator.personData.firstName", String.class));
                            put("laboratory", new FieldInStorageDescription("e.labName", String.class));
                            put("project", new FieldInStorageDescription("e.project.name", String.class));
                            put("filesCount", new FieldInStorageDescription("e.numberOfFiles", Number.class));
                            put("modified", new FieldInStorageDescription("e.lastModification", Date.class));
                            put("description", new FieldInStorageDescription("e.description", String.class));
                        }
                    });
                    put(ActiveFileMetaData.class, new HashMap<String, FieldInStorageDescription>() {
                        {
                            put("id", new FieldInStorageDescription("f.id", Number.class));
                            put("name", new FieldInStorageDescription("f.name", String.class));
                            put("instrument", new FieldInStorageDescription("instrument.name", String.class));
                            put("laboratory", new FieldInStorageDescription("lab.name", String.class));
                            put("uploadDate", new FieldInStorageDescription("f.uploadDate", Date.class));
                            put("labels", new FieldInStorageDescription("f.labels", String.class));
                            put("sizeInBytes", new FieldInStorageDescription("f.sizeInBytes", Number.class));
                            //meta info for file
                            put(
                                "annotationInstrument",
                                new FieldInStorageDescription("metaInfo.instrument", String.class)
                            );
                            put("userName", new FieldInStorageDescription("metaInfo.userName", String.class));
                            put("userLabels", new FieldInStorageDescription("metaInfo.metaInfo", String.class));
                            put("fileCondition", new FieldInStorageDescription("metaInfo.fileCondition", String.class));
                            put(
                                "instrumentMethod",
                                new FieldInStorageDescription("metaInfo.instrumentMethod", String.class)
                            );
                            // put("endRt", new FieldInStorageDescription("e.labName", String.class));
                            // put("startRt", new FieldInStorageDescription("e.labName", String.class));
                            put("creationDate", new FieldInStorageDescription("metaInfo.creationDate", Date.class));
                            put("comment", new FieldInStorageDescription("metaInfo.metaInfo", String.class));
                            // put("startMz", new FieldInStorageDescription("e.labName", String.class));
                            // put("endMz", new FieldInStorageDescription("e.labName", String.class));
                            put("fileName", new FieldInStorageDescription("metaInfo.fileName", String.class));
                            put(
                                "seqRowPosition",
                                new FieldInStorageDescription("metaInfo.seqRowPosition", String.class)
                            );
                            put("sampleName", new FieldInStorageDescription("metaInfo.sampleName", String.class));
                            put("translateFlag", new FieldInStorageDescription("metaInfo.translateFlag", String.class));
                            put(
                                "instrumentSerialNumber",
                                new FieldInStorageDescription("metaInfo.instrumentSerialNumber", String.class)
                            );
                            put("phone", new FieldInStorageDescription("metaInfo.phone", String.class));
                            put(
                                "instrumentName",
                                new FieldInStorageDescription("metaInfo.instrumentName", String.class)
                            );
                            put(
                                "studyType",
                                new FieldInStorageDescription("instrument.model.studyType.id", Number.class)
                            );
                            put(LABORATORY_ID_FIELD, new FieldInStorageDescription("lab.id", Number.class));
                            put(INSTRUMENT_ID_FIELD, new FieldInStorageDescription("instrument.id", Number.class));
                            put(
                                INSTRUMENT_MODEL_ID_FIELD,
                                new FieldInStorageDescription("instrument.model.id", Number.class)
                            );
                            put(SPECIE_ID_FIELD, new FieldInStorageDescription("f.specie.id", Number.class));
                        }
                    });
                    put(ExperimentFileTemplate.class, new HashMap<String, FieldInStorageDescription>() {
                        {
                            put("id", new FieldInStorageDescription("rawFile.fileMetaData.id", Number.class));
                            put("name", new FieldInStorageDescription("rawFile.fileMetaData.name", String.class));
                            put("instrument", new FieldInStorageDescription("instrument.name", String.class));
                            put("laboratory", new FieldInStorageDescription("lab.name", String.class));
                            put(
                                "uploadDate",
                                new FieldInStorageDescription("rawFile.fileMetaData.uploadDate", Date.class)
                            );
                            put("labels", new FieldInStorageDescription("rawFile.fileMetaData.labels", String.class));
                            put(
                                "sizeInBytes",
                                new FieldInStorageDescription("rawFile.fileMetaData.sizeInBytes", Number.class)
                            );
                            //meta info for file
                            put(
                                "annotationInstrument",
                                new FieldInStorageDescription("metaInfo.instrument", String.class)
                            );
                            put("userName", new FieldInStorageDescription("metaInfo.userName", String.class));
                            put("userLabels", new FieldInStorageDescription("metaInfo.metaInfo", String.class));
                            put("fileCondition", new FieldInStorageDescription("metaInfo.fileCondition", String.class));
                            put(
                                "instrumentMethod",
                                new FieldInStorageDescription("metaInfo.instrumentMethod", String.class)
                            );
                            // put("endRt", new FieldInStorageDescription("e.labName", String.class));
                            // put("startRt", new FieldInStorageDescription("e.labName", String.class));
                            put("creationDate", new FieldInStorageDescription("metaInfo.creationDate", Date.class));
                            put("comment", new FieldInStorageDescription("metaInfo.metaInfo", String.class));
                            // put("startMz", new FieldInStorageDescription("e.labName", String.class));
                            // put("endMz", new FieldInStorageDescription("e.labName", String.class));
                            put("fileName", new FieldInStorageDescription("metaInfo.fileName", String.class));
                            put(
                                "seqRowPosition",
                                new FieldInStorageDescription("metaInfo.seqRowPosition", String.class)
                            );
                            put("sampleName", new FieldInStorageDescription("metaInfo.sampleName", String.class));
                            put("translateFlag", new FieldInStorageDescription("metaInfo.translateFlag", String.class));
                            put(
                                "instrumentSerialNumber",
                                new FieldInStorageDescription("metaInfo.instrumentSerialNumber", String.class)
                            );
                            put("phone", new FieldInStorageDescription("metaInfo.phone", String.class));
                            put(
                                "instrumentName",
                                new FieldInStorageDescription("metaInfo.instrumentName", String.class)
                            );
                        }
                    });
                }
            };
    }

    private static HashMap<String, String> getExperimentColumnMapping() {

        final HashMap<String, String> mapping = new HashMap<>();

        mapping.put("id", "id");
        mapping.put("experiment name", "name");
        mapping.put("owner", "owner");
        mapping.put("laboratory", "laboratory");
        mapping.put("project", "project");
        mapping.put("files", "files");
        mapping.put("modified", "modified");
        mapping.put("failed", "failed");

        return mapping;
    }


    private static HashMap<String, String> createProjectColumnMapping() {
        final HashMap<String, String> mapping = new HashMap<>();

        mapping.put("id", "id");
        mapping.put("project name", "name");
        mapping.put("owner", "owner");
        mapping.put("laboratory", "laboratory");
        mapping.put("area of research", "area");
        mapping.put("modified", "modified");

        return mapping;
    }

    private static HashMap<String, String> getFileColumnMapping() {
        final HashMap<String, String> mapping = new HashMap<>();
        mapping.put("id", "id");
        mapping.put("name", "name");
        mapping.put("size", "sizeInBytes");
        mapping.put("instrument", "instrument");
        mapping.put("laboratory", "laboratory");
        mapping.put("upload date", "uploadDate");
        mapping.put("labels", "labels");
        mapping.put("creation date", "creationDate");
        mapping.put("comment", "comment");
        mapping.put("instrument method", "instrumentMethod");
        mapping.put("end time", "endRt");
        mapping.put("start time", "startRt");
        mapping.put("start mz", "startMz");
        mapping.put("end mz", "endMz");
        mapping.put("file name", "fileName");
        mapping.put("position", "seqRowPosition");
        mapping.put("sample name", "sampleName");
        mapping.put("annotation instrument", "annotationInstrument");
        mapping.put("user name", "userName");
        mapping.put("user labels", "userLabels");
        mapping.put("file condition", "fileCondition");
        mapping.put("translate flag", "translateFlag");
        mapping.put("instrument serial", "instrumentSerialNumber");
        mapping.put("phone", "phone");
        mapping.put("instrument name", "instrumentName");
        mapping.put("detected isotope groups", "detectedIsotopeGroups");
        mapping.put("annotated isotope groups", "annotatedIsotopeGroups");

        return mapping;
    }
}
