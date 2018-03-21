package com.infoclinika.mssharing.model.internal.read;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.infoclinika.mssharing.model.PaginationItems;
import com.infoclinika.mssharing.model.PaginationItems.AdvancedFilterQueryParams.AdvancedFilterPredicateItem;
import com.infoclinika.mssharing.model.helper.BillingFeaturesHelper;
import com.infoclinika.mssharing.model.helper.ColumnViewHelper;
import com.infoclinika.mssharing.model.helper.ExperimentSampleItem;
import com.infoclinika.mssharing.model.helper.ExperimentSampleTypeItem;
import com.infoclinika.mssharing.model.helper.LockMzItem;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.entity.ColumnDefinition;
import com.infoclinika.mssharing.model.internal.entity.ExperimentSampleType;
import com.infoclinika.mssharing.model.internal.entity.FileAccessLog;
import com.infoclinika.mssharing.model.internal.entity.FileMetaAnnotations;
import com.infoclinika.mssharing.model.internal.entity.Instrument;
import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.entity.LockMz;
import com.infoclinika.mssharing.model.internal.entity.NewsItem;
import com.infoclinika.mssharing.model.internal.entity.PrepToExperimentSample;
import com.infoclinika.mssharing.model.internal.entity.ProteinDatabase;
import com.infoclinika.mssharing.model.internal.entity.UploadAppConfiguration;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.entity.ViewColumn;
import com.infoclinika.mssharing.model.internal.entity.payment.AccountChargeableItemData;
import com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem;
import com.infoclinika.mssharing.model.internal.entity.payment.FeatureLog;
import com.infoclinika.mssharing.model.internal.entity.payment.LabPaymentAccount;
import com.infoclinika.mssharing.model.internal.entity.payment.PayPalLogEntry;
import com.infoclinika.mssharing.model.internal.entity.payment.StoreLogEntry;
import com.infoclinika.mssharing.model.internal.entity.restorable.AbstractExperiment;
import com.infoclinika.mssharing.model.internal.entity.restorable.AbstractFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveExperiment;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveProject;
import com.infoclinika.mssharing.model.internal.entity.restorable.StorageData;
import com.infoclinika.mssharing.model.internal.entity.view.ExperimentDashboardRecord;
import com.infoclinika.mssharing.model.internal.entity.view.ProjectDashboardRecord;
import com.infoclinika.mssharing.model.internal.repository.ExperimentAdditionalInfoRecord;
import com.infoclinika.mssharing.model.internal.repository.ExperimentRepository;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.model.internal.repository.RawFilesRepository;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import com.infoclinika.mssharing.model.read.AdministrationToolsReader;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.ExperimentLine;
import com.infoclinika.mssharing.model.read.FileLine;
import com.infoclinika.mssharing.model.read.PaymentHistoryReader;
import com.infoclinika.mssharing.model.read.ProjectLine;
import com.infoclinika.mssharing.model.read.ProteinDatabaseReader.ProteinDBItem;
import com.infoclinika.mssharing.model.write.ExperimentCategory;
import com.infoclinika.mssharing.model.write.UploadAppManagement;
import com.infoclinika.mssharing.platform.entity.EntityUtil;
import com.infoclinika.mssharing.platform.entity.ExperimentFileTemplate;
import com.infoclinika.mssharing.platform.entity.InstrumentModel;
import com.infoclinika.mssharing.platform.entity.Sharing;
import com.infoclinika.mssharing.platform.model.DefaultTransformers;
import com.infoclinika.mssharing.platform.model.helper.PagedItemsTransformerTemplate;
import com.infoclinika.mssharing.platform.model.helper.read.ExperimentReaderHelper;
import com.infoclinika.mssharing.platform.model.helper.read.FileReaderHelper;
import com.infoclinika.mssharing.platform.model.helper.read.ProjectReaderHelper;
import com.infoclinika.mssharing.platform.model.read.AccessLevel;
import com.infoclinika.mssharing.platform.model.read.ExperimentReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.FileReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.LabReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.ProjectReaderTemplate;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingChargeType;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Sets.newHashSet;
import static com.infoclinika.mssharing.model.read.PaymentHistoryReader.HistoryItemType.FEATURE;
import static com.infoclinika.mssharing.model.read.PaymentHistoryReader.HistoryItemType.PAYPAL;
import static com.infoclinika.mssharing.model.read.PaymentHistoryReader.HistoryItemType.STORE;

/**
 * @author Pavel Kaplin
 */
@Component
public class Transformers extends DefaultTransformers {

    private static final Logger LOGGER = LoggerFactory.getLogger(Transformers.class);

    public static final String CHARTS_HOME_SUFFIX = "/charts/home";
    public final TimeZone serverTimezone;
    public final SimpleDateFormat historyLineDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
    public final SimpleDateFormat extendedHistoryDateFormat = new SimpleDateFormat("MMM dd, YYYY hh:mm:ss z", Locale.ENGLISH);

    public static final Function<ChargeableItem, BillingFeature> BILLING_FEATURE_TRANSFORMER = item -> transformFeature(item.getFeature());

    public static final Function<AccountChargeableItemData, ChargeableItem> CHARGEABLE_ITEM_FROM_ACCOUNT_TRANSFORMER = new Function<AccountChargeableItemData, ChargeableItem>() {
        @Override
        public ChargeableItem apply(AccountChargeableItemData input) {
            return input.getChargeableItem();
        }
    };

    public static BillingChargeType transformChargeType(ChargeableItem.ChargeType chargeType) {
        switch (chargeType) {
            case GB:
                return BillingChargeType.PER_GB;
        }
        throw new AssertionError("Unknown charge type: " + chargeType);
    }

    public static final Function<ExperimentFileTemplate, AbstractFileMetaData> RAW_META_DATA_TRANSFORMER = new Function<ExperimentFileTemplate, AbstractFileMetaData>() {
        @Override
        public AbstractFileMetaData apply(ExperimentFileTemplate input) {
            return (AbstractFileMetaData) input.getFileMetaData();
        }
    };
    public static final Function<ExperimentFileTemplate, Long> RAW_FILES_META_ID_TRANSFORMER = new Function<ExperimentFileTemplate, Long>() {
        @Override
        @SuppressWarnings("all")
        public Long apply(ExperimentFileTemplate input) {
            return RAW_META_DATA_TRANSFORMER.apply(input).getId();
        }
    };


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
    @Value("${base.url}")
    private String baseUrl;
    @Inject
    private ProjectReaderHelper<ActiveProject, ProjectLine> projectReaderHelper;
    @Inject
    private FileReaderHelper<ActiveFileMetaData, FileLine> fileReaderHelper;
    @Inject
    private ExperimentReaderHelper<ActiveExperiment, ExperimentLine> experimentReaderHelper;

    @Inject
    public Transformers(TimeZone serverTimezone) {
        this.serverTimezone = serverTimezone;
        historyLineDateFormat.setTimeZone(serverTimezone);
        extendedHistoryDateFormat.setTimeZone(serverTimezone);
    }

    public Function<ActiveExperiment, ExperimentReaderTemplate.ExperimentLineTemplate> defaultExperimentTransformer() {
        return experimentReaderHelper.getDefaultTransformer();
    }

    public Function<ActiveProject, ProjectReaderTemplate.ProjectLineTemplate> defaultProjectTransformer() {
        return projectReaderHelper.getDefaultTransformer();
    }

    public Function<ActiveFileMetaData, FileReaderTemplate.FileLineTemplate> defaultFileTransformer() {
        return fileReaderHelper.getDefaultTransformer();
    }

    public static final String FILE_PARAM = "file";
    public static final String EXPERIMENT_PARAM = "experiment";
    public static final Function<NewsItem, AdministrationToolsReader.NewsLine> TO_NEWS_LINE = new Function<NewsItem, AdministrationToolsReader.NewsLine>() {
        @Override
        public AdministrationToolsReader.NewsLine apply(NewsItem input) {
            return new AdministrationToolsReader.NewsLine(input.getId(), input.getTitle(), input.getAuthor(), input.getCreationDate());
        }
    };
    public static final Comparator<AdministrationToolsReader.NewsLine> NEWS_BY_DATE = new Comparator<AdministrationToolsReader.NewsLine>() {
        @Override
        public int compare(AdministrationToolsReader.NewsLine o1, AdministrationToolsReader.NewsLine o2) {
            if (o1.dateCreated.equals(o2.dateCreated)) {
                return o1.hashCode() - o2.hashCode();
            }
            return o1.dateCreated.compareTo(o2.dateCreated);
        }
    };
    public static final Comparator<FileLine> FILE_BY_NAME_CMP =
            new Comparator<FileLine>() {
                @Override
                public int compare(FileLine o1, FileLine o2) {
                    final int i = o1.columns.name.compareTo(o2.columns.name);
                    if (i == 0) {
                        return o1.hashCode() - o2.hashCode();
                    }
                    return i;
                }
            };

    public static final Comparator<PaymentHistoryReader.PaymentHistoryLine> HISTORY_LINES_BY_DATE =
            new Comparator<PaymentHistoryReader.PaymentHistoryLine>() {
                @Override
                public int compare(PaymentHistoryReader.PaymentHistoryLine o1, PaymentHistoryReader.PaymentHistoryLine o2) {
                    final int i = o1.date.compareTo(o2.date);
                    if (i == 0) {
                        return o1.hashCode() - o2.hashCode();
                    }
                    return i;
                }
            };

    public static final Comparator<PaymentHistoryReader.PaymentHistoryLine> HISTORY_LINES_BY_DATE_REVERSED = Ordering.from(HISTORY_LINES_BY_DATE).reverse();

    public static final Function<ActiveFileMetaData, Instrument> INSTRUMENT_FROM_FILE = new Function<ActiveFileMetaData, Instrument>() {
        @Override
        public Instrument apply(ActiveFileMetaData input) {
            return input.getInstrument();
        }
    };

    private static LabReaderTemplate.LabLineTemplate getLabLineTemplate(Lab input) {
        return new LabReaderTemplate.LabLineTemplate(input.getId(),
                input.getName(), input.getHead().getId(),
                input.getInstitutionUrl(),
                input.getHead().getFullName(),
                input.getLastModification());
    }

    public final Function<PayPalLogEntry, PaymentHistoryReader.PaymentHistoryLine> paypalHistoryLineTransformFunction =
            new Function<PayPalLogEntry, PaymentHistoryReader.PaymentHistoryLine>() {
                @Override
                public PaymentHistoryReader.PaymentHistoryLine apply(PayPalLogEntry input) {
                    return new PaymentHistoryReader.PaymentHistoryLine(
                            input.getTimestamp(),
                            null, extendedHistoryDateFormat.format(input.getTimestamp()), PaymentHistoryReader.creditCharge,
                            input.getAmount(), input.getStoreBalance(),
                            PAYPAL, serverTimezone.getID(), true);

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
                            STORE, serverTimezone.getID(), true);

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
                            FEATURE, serverTimezone.getID(), true);
                }
            };


    public static final Function<UploadAppConfiguration, UploadAppManagement.Configuration> TO_UPLOAD_APP_CONFIGURATION_DTO =
            new Function<UploadAppConfiguration, UploadAppManagement.Configuration>() {
                @Override
                public UploadAppManagement.Configuration apply(UploadAppConfiguration input) {
                    return new UploadAppManagement.Configuration(
                            input.getId(),
                            input.getName(),
                            input.getFolder(),
                            input.isStarted(),
                            input.getLabels(),
                            input.getInstrument().getId(),
                            input.getSpecie().getId(),
                            input.getCreated(),
                            UploadAppManagement.CompleteAction.valueOf(input.getCompleteAction().name()),
                            input.getFolderToMoveFiles()
                    );
                }
            };

    public static final Function<LockMz, LockMzItem> LOCK_MZ_ITEM_FUNCTION = new Function<LockMz, LockMzItem>() {
        @Override
        public LockMzItem apply(LockMz input) {
            return new LockMzItem(input.getMass(), input.getCharge());
        }
    };
    public static final Function<LockMzItem, LockMz> LOCK_MZ_FUNCTION = new Function<LockMzItem, LockMz>() {
        @Override
        public LockMz apply(LockMzItem input) {
            final LockMz lockMz = new LockMz();
            lockMz.setCharge(input.charge);
            lockMz.setMass(input.lockMass);
            return lockMz;
        }
    };
    public static Function<Lab, LabReaderTemplate.LabLineTemplate> LAB_LINE_FUNCTION = new Function<Lab, LabReaderTemplate.LabLineTemplate>() {
        @Override
        public LabReaderTemplate.LabLineTemplate apply(@Nullable Lab input) {
            if (input == null) {
                return null;
            }
            return getLabLineTemplate(input);
        }
    };
    //    create transformer
    public static final Function<ExperimentSampleType, ExperimentSampleTypeItem> AS_SAMPLE_TYPE_ITEM = new Function<ExperimentSampleType, ExperimentSampleTypeItem>() {
        @Override
        public ExperimentSampleTypeItem apply(ExperimentSampleType type) {
            switch (type) {
                case LIGHT:
                    return ExperimentSampleTypeItem.LIGHT;
                case MEDIUM:
                    return ExperimentSampleTypeItem.MEDIUM;
                case HEAVY:
                    return ExperimentSampleTypeItem.HEAVY;
                case CHANNEL_1:
                    return ExperimentSampleTypeItem.CHANNEL_1;
                case CHANNEL_2:
                    return ExperimentSampleTypeItem.CHANNEL_2;
                case CHANNEL_3:
                    return ExperimentSampleTypeItem.CHANNEL_3;
                case CHANNEL_4:
                    return ExperimentSampleTypeItem.CHANNEL_4;
                case CHANNEL_5:
                    return ExperimentSampleTypeItem.CHANNEL_5;
                case CHANNEL_6:
                    return ExperimentSampleTypeItem.CHANNEL_6;
                case CHANNEL_7:
                    return ExperimentSampleTypeItem.CHANNEL_7;
                case CHANNEL_8:
                    return ExperimentSampleTypeItem.CHANNEL_8;
                case CHANNEL_9:
                    return ExperimentSampleTypeItem.CHANNEL_9;
                case CHANNEL_10:
                    return ExperimentSampleTypeItem.CHANNEL_10;
                default:
                    throw new IllegalStateException("Undefined experiment sample type: " + type);
            }
        }
    };

    public static final Function<ExperimentSampleTypeItem, ExperimentSampleType> AS_SAMPLE_TYPE = new Function<ExperimentSampleTypeItem, ExperimentSampleType>() {
        @Override
        public ExperimentSampleType apply(ExperimentSampleTypeItem type) {
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
        }
    };

    public static final Function AS_SAMPLE_ITEM = new Function<PrepToExperimentSample, ExperimentSampleItem>() {
        @Override
        public ExperimentSampleItem apply(PrepToExperimentSample sample) {
            return new ExperimentSampleItem(sample.getExperimentSample().getName(), AS_SAMPLE_TYPE_ITEM.apply(sample.getType()), sample.getExperimentSample().getFactorValues());
        }
    };

    public final Function<ActiveProject, ProjectLine> projectTransformer = new Function<ActiveProject, ProjectLine>() {
        @Override
        public ProjectLine apply(ActiveProject input) {
            return new ProjectLine(projectReaderHelper.getDefaultTransformer().apply(input), input.isBlogEnabled(),
                    Transformers.transformProjectColumns(input));
        }
    };

    @Deprecated
    public final Function<ProjectDashboardRecord, ProjectLine> projectDashboardRecordTransformer = new Function<ProjectDashboardRecord, ProjectLine>() {
        @Nullable
        @Override
        public ProjectLine apply(ProjectDashboardRecord input) {
            return new ProjectLine(input.getId(),
                    input.getName(),
                    input.getLastModification(),
                    input.getAreaOfResearch(),
                    input.getCreator().getEmail(),
                    fromSharingType(input.getProject().getSharing().getType()),
                    (input.getLab() == null) ? null : LAB_LINE_FUNCTION.apply(input.getLab()),
                    input.getCreator().getFullName(),
                    input.getProject().isBlogEnabled());

        }
    };

    private List<ExperimentAdditionalInfoRecord> sortAccordingToIds(List<ExperimentAdditionalInfoRecord> list, List<Long> ids) {

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

    public FluentIterable<ExperimentLine> transformExperimentRecords(final long actor, Iterable<ExperimentDashboardRecord> experiments) {

        return from(experiments).transform(experimentLineTransformerFn(actor, experiments));

    }

    public Function<ExperimentDashboardRecord, ExperimentLine> experimentLineTransformerFn(final long actor, Iterable<ExperimentDashboardRecord> experiments) {

        final List<Long> ids = newLinkedList();

        for (ExperimentDashboardRecord experiment : experiments) {
            ids.add(experiment.getId());
        }
        final List<Long> userLabIds = newArrayList(transform(newArrayList(userRepository.findOne(actor).getLabs()), new Function<Lab, Long>() {
            @Override
            public Long apply(Lab input) {
                return input.getId();
            }
        }));
        if (userLabIds.isEmpty()) {
            userLabIds.add(-1L);// add fake lab Id to make getAdditionalInfo work
        }
        final List<ExperimentAdditionalInfoRecord> additionalInfo = ids.size() > 0 ? experimentRepository.getAdditionalInfo(actor, ids) : Collections.<ExperimentAdditionalInfoRecord>emptyList();
        final Iterator<ExperimentAdditionalInfoRecord> sortedInfoIterator = sortAccordingToIds(additionalInfo, ids).iterator();

        return new Function<ExperimentDashboardRecord, ExperimentLine>() {
            @Override
            public ExperimentLine apply(ExperimentDashboardRecord input) {
                final ExperimentAdditionalInfoRecord info = sortedInfoIterator.next();
                return getExperimentLine(input, info, actor);
            }
        };

    }

    private ExperimentLine getExperimentLine(ExperimentDashboardRecord record, ExperimentAdditionalInfoRecord info, Long actor) {

        final java.util.Optional<Lab> labOpt = java.util.Optional.ofNullable(record.getLab() != null ? record.getLab() : record.getBillLab());
        final boolean bDownloadAvailable = info.countFilesReadyToDownload == record.getNumberOfFiles();
        final boolean hasUnArchiveRequest = !bDownloadAvailable && info.countArchivedFilesRequestedForUnArchiving > 0;
        final boolean hasUnArchiveDownloadOnlyRequest = !bDownloadAvailable && info.countArchivedFilesRequestedForDownloadOnly > 0;
        final boolean canArchive = labOpt.isPresent() && info.canArchiveExperiment > 0 && billingFeaturesHelper.isFeatureEnabled(labOpt.get().getId(), BillingFeature.ARCHIVE_STORAGE);
        final boolean canUnarchive = labOpt.isPresent() && info.canUnarchiveExperiment > 0 && billingFeaturesHelper.isFeatureEnabled(labOpt.get().getId(), BillingFeature.ANALYSE_STORAGE);
        final Long labToUseForSearch = labOpt.isPresent() ? labOpt.get().getId() : null;
        // define whether Processing is possible for an experiment

        return new ExperimentLine(record.getId(),
                DefaultTransformers.labLineTemplateTransformer().apply(record.getLab()),
                record.getName(),
                record.getCreator().getFullName(),
                record.getProject().getName(),
                record.getNumberOfFiles(),
                record.getLastModification(),
                Transformers.fromSharingType(record.getProject().getSharing().getType()),
                getChartsLink(record),
                getDownloadLink(record.getDownloadToken()),
                actor.equals(record.getCreator().getId()),
                info.userCanCreateExperimentsInProject > 0,
                bDownloadAvailable, hasUnArchiveRequest, hasUnArchiveDownloadOnlyRequest,
                canArchive,
                canUnarchive,
                record.getAnalyzesCount(),
                labOpt.map(EntityUtil.ENTITY_TO_ID::apply).orElse(null),
                record.getCreator().getId(),
                transformExperimentColumns(record)
        );
    }

    public final Function<ExperimentLine, ExperimentLine> experimentFolderStructureTransformer = new Function<ExperimentLine, ExperimentLine>() {
        @Override
        public ExperimentLine apply(ExperimentLine experiment) {
            return new ExperimentLine(
                    experiment.id,
                    experiment.lab,
                    experiment.name,
                    experiment.creator,
                    experiment.project,
                    experiment.files,
                    experiment.modified,
                    experiment.accessLevel,
                    "",
                    "",
                    experiment.isOwner,
                    false,
                    false,false,false,
                    experiment.canArchive,
                    experiment.canUnarchive,
                    experiment.analyzesCount,
                    experiment.billLab,
                    experiment.owner,
                    new DashboardReader.ExperimentColumns(experiment.name, experiment.creator, experiment.lab.name,
                            experiment.project, experiment.files, experiment.modified));
        }
    };

    public final Function<ProteinDatabase, ProteinDBItem> proteinDBItemTransformer = new Function<ProteinDatabase, ProteinDBItem>() {
        @Override
        public ProteinDBItem apply(ProteinDatabase db) {
            return new ProteinDBItem(db.getId(), db.getName(), db.getSpecie().getId(), db.getSpecie().getName());
        }
    };

    public final Function<ViewColumn, ColumnViewHelper.ColumnInfo> viewToColumnTransformer = new Function<ViewColumn, ColumnViewHelper.ColumnInfo>() {
        @Override
        public ColumnViewHelper.ColumnInfo apply(ViewColumn input) {
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
                    columnDefinition.getUnits());
        }
    };

    private Map<String, String> columnModelViewNameMap = new HashMap<String, String>() {{
        put("id", "id");
        put("name", "name");
        put("size", "sizeInBytes");
        put("instrument", "instrument");
        put("laboratory", "laboratory");
        put("upload date", "uploadDate");
        put("labels", "labels");
        put("creation date", "creationDate");
        put("comment", "comment");
        put("instrument method", "instrumentMethod");
        put("end time", "endRt");
        put("start time", "startRt");
        put("start mz", "startMz");
        put("end mz", "endMz");
        put("file name", "fileName");
        put("position", "seqRowPosition");
        put("sample name", "sampleName");
        put("annotation instrument", "annotationInstrument");
        put("user name", "userName");
        put("user labels", "userLabels");
        put("file condition", "fileCondition");
        put("translate flag", "translateFlag");
        put("instrument serial", "instrumentSerialNumber");
        put("phone", "phone");
        put("instrument name", "instrumentName");
    }};

    private Map<String, String> projectColumnModelViewNameMap = new HashMap<String, String>() {{
        put("id", "id");
        put("project name", "name");
        put("owner", "owner");
        put("laboratory", "laboratory");
        put("area of research", "area");
        put("modified", "modified");
    }};

    private Map<String, String> experimentColumnModelViewNameMap = new HashMap<String, String>() {{
        put("id", "id");
        put("experiment name", "name");
        put("owner", "owner");
        put("laboratory", "laboratory");
        put("project", "project");
        put("files", "files");
        put("modified", "modified");
    }};

    public static DashboardReader.ProjectColumns transformProjectColumns(ActiveProject input) {
        String labName = input.getLab() == null ? "" : input.getLab().getName();
        return new DashboardReader.ProjectColumns(input.getName(), input.getCreator().getFullName(), labName,
                input.getAreaOfResearch(), input.getLastModification());
    }

    public static DashboardReader.ExperimentColumns transformExperimentColumns(ExperimentDashboardRecord input) {
        String labName = input.getLab() == null ? "" : input.getLab().getName();
        return new DashboardReader.ExperimentColumns(input.getName(), input.getCreator().getFullName(), labName,
                input.getProject().getName(), input.getNumberOfFiles(), input.getLastModification());
    }

    public static DashboardReader.FileColumns transformColumns(AbstractFileMetaData input) {

        final Instrument instrument = input.getInstrument();
        final FileMetaAnnotations metaInfo = input.getMetaInfo();
        DashboardReader.FileColumns fileColumns = new DashboardReader.FileColumns(input.getName(), input.getSizeInBytes(), instrument.getName(), instrument.getLab().getName(), input.getUploadDate(), input.getLabels());
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

    private static String getChartsLink(ActiveExperiment experiment, String chartsRenderPageUrl) {
        return chartsRenderPageUrl + "?" + EXPERIMENT_PARAM + "=" + experiment.getId();
    }

    private static String getChartsLink(ExperimentDashboardRecord experiment, String chartsRenderPageUrl) {
        return chartsRenderPageUrl + "?" + EXPERIMENT_PARAM + "=" + experiment.getId();
    }

    private static String getChartsLink(List<Long> processedDataIds, String chartsRenderPageUrl) {
        if (processedDataIds.contains(null)) {
            return null;
        }
        StringBuilder msChartsLinkBuilder = new StringBuilder();
        msChartsLinkBuilder.append(chartsRenderPageUrl);
        for (int i = 0; i < processedDataIds.size(); i++) {
            msChartsLinkBuilder.append(i == 0 ? "?" : "&");
            msChartsLinkBuilder.append(FILE_PARAM + "=").append(processedDataIds.get(i));
        }
        return msChartsLinkBuilder.toString();
    }

    public static String getDownloadLink(ActiveExperiment experiment, String baseUrl, boolean bPublic) {
        if (!bPublic) {
            return baseUrl + "/download/bulk?experiment=" + experiment.getId();
        }
        final String downloadToken = experiment.getDownloadToken();
        if (downloadToken == null) {
            return null;
        }
        return baseUrl + "/anonymous/download/experiment/" + downloadToken;
    }

    public static String getPublicDownloadLink(String experimentDownloadToken, String baseUrl) {
        if (experimentDownloadToken == null) {
            return null;
        }
        return baseUrl + "/anonymous/download/experiment/" + experimentDownloadToken;
    }

    public static AccessLevel fromSharingType(Sharing.Type type) {
        AccessLevel accessLevel;
        switch (type) {
            case PUBLIC:
                accessLevel = AccessLevel.PUBLIC;
                break;
            case PRIVATE:
                accessLevel = AccessLevel.PRIVATE;
                break;
            case SHARED:
                accessLevel = AccessLevel.SHARED;
                break;
            default:
                throw new IllegalStateException("Illegal sharing type: " + type);
        }
        return accessLevel;
    }

    public static ChargeableItem.Feature transformFeature(BillingFeature feature) {
        switch (feature) {
            case ANALYSE_STORAGE:
                return ChargeableItem.Feature.ANALYSE_STORAGE;
            case ARCHIVE_STORAGE:
                return ChargeableItem.Feature.ARCHIVE_STORAGE;
            case DOWNLOAD:
                return ChargeableItem.Feature.DOWNLOAD;
            case PROTEIN_ID_SEARCH:
                return ChargeableItem.Feature.ANALYSIS;
            case PUBLIC_DOWNLOAD:
                return ChargeableItem.Feature.PUBLIC_DOWNLOAD;
            case PROCESSING:
                return ChargeableItem.Feature.PROCESSING;
            case STORAGE_VOLUMES:
                return ChargeableItem.Feature.STORAGE_VOLUMES;
            case ARCHIVE_STORAGE_VOLUMES:
                return ChargeableItem.Feature.ARCHIVE_STORAGE_VOLUMES;
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
            case ANALYSIS:
                return BillingFeature.PROTEIN_ID_SEARCH;
            case PUBLIC_DOWNLOAD:
                return BillingFeature.PUBLIC_DOWNLOAD;
            case PROCESSING:
                return BillingFeature.PROCESSING;
            case STORAGE_VOLUMES:
                return BillingFeature.STORAGE_VOLUMES;
            case ARCHIVE_STORAGE_VOLUMES:
                return BillingFeature.ARCHIVE_STORAGE_VOLUMES;
            default:
                throw new AssertionError("Unknown billing feature: " + feature);
        }
    }


    public Function<ActiveFileMetaData, FileLine> transformFilesFn(final User actor, Iterable<ActiveFileMetaData> filtered) {
        LOGGER.trace("*** Getting statistics to transform file line");
        ImmutableList<Long> fileIds = from(filtered).transform(EntityUtil.ENTITY_TO_ID).toList();
        final List<Long> usedInExperiments = fileIds.isEmpty() ? Collections.<Long>emptyList() : fileRepository.usedInExperiments(fileIds);
        LOGGER.trace("*** Statistics retrieved");
        return new Function<ActiveFileMetaData, FileLine>() {
            @Override
            public FileLine apply(ActiveFileMetaData input) {
                AccessLevel level = ruleValidator.getAccessLevel(input);
                final Instrument instrument = input.getInstrument();
                final DashboardReader.FileColumns columns = transformColumns(input);
                final InstrumentModel model = instrument.getModel();

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
                        getChartsLink(Collections.singletonList(input.getId())),
                        usedInExperiments.contains(input.getId()),
                        input.getOwner().getId(),
                        input.getLastPingDate(),
                        columns,
                        input.isInvalid(),
                        model.getVendor().getName(),
                        Collections2.transform(instrument.getOperators(), EntityUtil.ENTITY_TO_ID),
                        transformStorageStatus(input.getStorageData().getStorageStatus(), input.getStorageData().isArchivedDownloadOnly()),
                        input.isSizeConsistent(),
                        input.isToReplace(), model.getStudyType().getName());
            }
        };
    }

    public static DashboardReader.StorageStatus transformStorageStatus(StorageData.Status storageStatus, boolean unArchivingForDownload) {
        switch (storageStatus) {
            case ARCHIVING_REQUESTED:
                //return DashboardReader.StorageStatus.ARCHIVING_IN_PROCESS;
            case ARCHIVED:
                return DashboardReader.StorageStatus.ARCHIVED;
            case UNARCHIVING_REQUESTED:
                return (unArchivingForDownload) ? DashboardReader.StorageStatus.UN_ARCHIVING_FOR_DOWNLOAD_IN_PROCESS : DashboardReader.StorageStatus.UN_ARCHIVING_IN_PROCESS;
            case UNARCHIVED:
            default:
                return DashboardReader.StorageStatus.UNARCHIVED;
        }
    }

    public static String toFullInstrumentModel(InstrumentModel model) {
        return Joiner.on(" ").join(new String[]{model.getVendor().getName(), model.getType().getName(), model.getName()});
    }

    public final String getChartsLink(ActiveExperiment experiment) {
        return getChartsLink(experiment, baseUrl + CHARTS_HOME_SUFFIX);
    }

    public final String getChartsLink(ExperimentDashboardRecord experiment) {
        return getChartsLink(experiment, baseUrl + CHARTS_HOME_SUFFIX);
    }

    public final String getChartsLink(List<Long> processedRawFileDataIds) {
        return getChartsLink(processedRawFileDataIds, baseUrl + CHARTS_HOME_SUFFIX);
    }

    public final String getPublicDownloadLink(ActiveExperiment experiment) {
        return getDownloadLink(experiment, baseUrl, true);
    }

    public final String getPrivateDownloadLink(ActiveExperiment experiment) {
        return getDownloadLink(experiment, baseUrl, false);
    }

    public final String getDownloadLink(String experimentDownloadToken) {
        return getPublicDownloadLink(experimentDownloadToken, baseUrl);
    }

    private Set<Long> getExperimentsByFile(ActiveFileMetaData input) {
        final List<ActiveExperiment> experiments = experimentRepository.findByFile(input);
        return from(experiments).transform(new Function<ActiveExperiment, Long>() {
            @Override
            public Long apply(ActiveExperiment input) {
                return input.getId();
            }
        }).toSet();
    }

    private String transformColumnNameToView(ColumnDefinition columnDefinition) {
        switch (columnDefinition.getType()) {
            case FILE_META:
                return columnModelViewNameMap.get(columnDefinition.getName().toLowerCase());
            case PROJECT_META:
                return projectColumnModelViewNameMap.get(columnDefinition.getName().toLowerCase());
            case EXPERIMENT_META:
                return experimentColumnModelViewNameMap.get(columnDefinition.getName().toLowerCase());
            default:
                throw new AssertionError(columnDefinition.getType());
        }
    }

    public Function<ActiveFileMetaData, FileLine> transformToFileLineFunction(long actor) {
        final User actorEntity = userRepository.findOne(actor);
        return new Function<ActiveFileMetaData, FileLine>() {
            @Override
            public FileLine apply(ActiveFileMetaData input) {
                return transformToFileLine(actorEntity, input);
            }
        };
    }

    public FileLine transformToFileLine(User actor, ActiveFileMetaData input) {
        final FileReaderTemplate.FileLineTemplate lineTemplate = fileReaderHelper.getDefaultTransformer().apply(input);
        final Instrument instrument = input.getInstrument();
        final DashboardReader.FileColumns columns = Transformers.transformColumns(input);

        return new FileLine(lineTemplate,
                input.getArchiveId(),
                input.getLastPingDate(),
                input.isArchive(),
                Collections2.transform(instrument.getOperators(), EntityUtil.ENTITY_TO_ID),
                getChartsLink(Collections.singletonList(input.getId())),
                transformStorageStatus(input.getStorageData().getStorageStatus(), input.getStorageData().isArchivedDownloadOnly()),
                input.isSizeConsistent(), columns,
                input.isToReplace(), instrument.getModel().getStudyType().getName());
    }


    @Component("pagedItemsTransformer")
    public static class PagedItemsTransformer extends PagedItemsTransformerTemplate {
        public static class FieldInStorageDescription {
            public final String name;
            public final Class aClass;

            public FieldInStorageDescription(String name, Class aClass) {
                this.name = name;
                this.aClass = aClass;
            }
        }

        public PagedItemsTransformer() {
            this.sortingOverride(new HashMap<Class<?>, Map<String, String>>() {{
                put(ActiveFileMetaData.class, new HashMap<String, String>() {{
                    put("id", "id");
                    put("uploadDate", "uploadDate");
                    put("name", "name");
                    put("sizeInBytes", "sizeInBytes");
                    put("instrument", "instrument.name");
                    put("labels", "labels");
                    put("laboratory", "instrument.lab.name");
                }});
                put(ExperimentFileTemplate.class, new HashMap<String, String>() {{
                    put("id", "id");
                    put("uploadDate", "fileMetaData.uploadDate");
                    put("name", "fileMetaData.name");
                    put("sizeInBytes", "fileMetaData.sizeInBytes");
                    put("instrument", "fileMetaData.instrument.name");
                    put("labels", "fileMetaData.labels");
                    put("laboratory", "fileMetaData.instrument.lab.name");
                }});
                put(ProjectDashboardRecord.class, new HashMap<String, String>() {{
                    put("id", "id");
                    put("name", "name");
                    put("owner", "creator.personData.firstName");
                    put("laboratory", "labName");
                    put("area", "areaOfResearch");
                    put("modified", "lastModification");
                }});
                put(ActiveProject.class, new HashMap<String, String>() {{
                    put("id", "id");
                    put("name", "name");
                    put("owner", "creator.personData.firstName");
                    put("laboratory", "lab.name");
                    put("area", "areaOfResearch");
                    put("modified", "lastModification");
                }});
                put(ActiveExperiment.class, new HashMap<String, String>() {{
                    put("id", "id");
                    put("name", "name");
                    put("owner", "creator.personData.firstName");
                    put("laboratory", "lab.name");
                    put("project", "project.name");
                    put("modified", "lastModification");
                }});
                put(ExperimentDashboardRecord.class, new HashMap<String, String>() {{
                    put("id", "id");
                    put("name", "name");
                    put("owner", "creator.personData.firstName");
                    put("laboratory", "labName");
                    put("project", "p.name");
                    put("modified", "lastModification");
                    put("files", "numberOfFiles");
                }});
                put(Instrument.class, new HashMap<String, String>() {{
                    put("id", "id");
                    put("name", "name");
                    put("model", "model.name");
                    put("serialNumber", "serialNumber");
                    put("laboratory", "lab.name");
                }});
                put(LabPaymentAccount.class, new HashMap<String, String>() {{
                    put("id", "id");
                    put("name", "lab.name");
                }});

                put(FileAccessLog.class, new HashMap<String, String>() {{
                    put("id", "id");
                    put("fileName", "fileName");
                    put("userEmail", "userEmail");
                    put("fileSize", "fileSize");
                    put("operationType", "operationType");
                    put("operationDate", "operationDate");
                }});
            }});
        }

        public static String toFilterQuery(PaginationItems.PagedItemInfo pagedInfo) {
            if (StringUtils.isEmpty(pagedInfo.filterQuery))
                return "%";
            return "%" + pagedInfo.filterQuery + "%";
        }

        public static String resolvePredicateForAdvancedSearch(Class<?> entity, AdvancedFilterPredicateItem predicateItem) {
            final Map<String, FieldInStorageDescription> map = ADVANCED_FILTER_FIELDS_MAPPING.get(entity);
            if (map == null) throw new IllegalArgumentException("Unknown entity type to make an advanced search");
            FieldInStorageDescription fieldDescription = map.get(predicateItem.prop);
            if (fieldDescription == null)
                throw new IllegalArgumentException("Unknown field to make an advanced search");
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
                        return "(" + field + ">'" + dateFormatToSql.format(getCalendarDateWithStartTime(dateField)) + "' AND " + field + "<'" + dateFormatToSql.format(getCalendarDateWithEndTime(dateField)) + "')";
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
                        return "(" + field + ">'" + dateFormatToSql.format(getCalendarDateWithStartTime(currentDate)) + "' AND " + field + "<'" + dateFormatToSql.format(getCalendarDateWithEndTime(currentDate)) + "')";
                    case IS_YESTERDAY:
                        Calendar yesterdayCal = Calendar.getInstance();
                        yesterdayCal.add(Calendar.DATE, -1);
                        final Date yesterday = yesterdayCal.getTime();
                        return "(" + field + ">'" + dateFormatToSql.format(getCalendarDateWithStartTime(yesterday)) + "' AND " + field + "<'" + dateFormatToSql.format(getCalendarDateWithEndTime(yesterday)) + "')";
                    case IS_IN_WEEK:
                        Calendar inWeek = Calendar.getInstance();
                        inWeek.add(Calendar.DATE, -7);
                        return field + ">='" + dateFormatToSql.format(getCalendarDateWithStartTime(inWeek.getTime())) + "'";
                }
            } catch (ParseException e) {
                LOGGER.error("Parse exception during query creation: " + e.getMessage(), e);
                throw new RuntimeException("Wrong date format: " + predicateItem.value);
            }
            throw new IllegalStateException("Operator is undefined:" + predicateItem.operator);
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

        private static final Map<Class<?>, Map<String, FieldInStorageDescription>> ADVANCED_FILTER_FIELDS_MAPPING = new HashMap<Class<?>, Map<String, FieldInStorageDescription>>() {{
            put(ExperimentDashboardRecord.class, new HashMap<String, FieldInStorageDescription>() {{
                put("id", new FieldInStorageDescription("e.id", Number.class));
                put("name", new FieldInStorageDescription("e.name", String.class));
                put("owner", new FieldInStorageDescription("e.creator.personData.firstName", String.class));
                put("laboratory", new FieldInStorageDescription("e.labName", String.class));
                put("project", new FieldInStorageDescription("e.project.name", String.class));
                put("filesCount", new FieldInStorageDescription("e.numberOfFiles", Number.class));
                put("modified", new FieldInStorageDescription("e.lastModification", Date.class));
                put("description", new FieldInStorageDescription("e.description", String.class));
            }});
            put(ActiveFileMetaData.class, new HashMap<String, FieldInStorageDescription>() {{
                put("id", new FieldInStorageDescription("f.id", Number.class));
                put("name", new FieldInStorageDescription("f.name", String.class));
                put("instrument", new FieldInStorageDescription("instrument.name", String.class));
                put("laboratory", new FieldInStorageDescription("lab.name", String.class));
                put("uploadDate", new FieldInStorageDescription("f.uploadDate", Date.class));
                put("labels", new FieldInStorageDescription("f.labels", String.class));
                put("sizeInBytes", new FieldInStorageDescription("f.sizeInBytes", Number.class));
                //meta info for file
                put("annotationInstrument", new FieldInStorageDescription("metaInfo.instrument", String.class));
                put("userName", new FieldInStorageDescription("metaInfo.userName", String.class));
                put("userLabels", new FieldInStorageDescription("metaInfo.metaInfo", String.class));
                put("fileCondition", new FieldInStorageDescription("metaInfo.fileCondition", String.class));
                put("instrumentMethod", new FieldInStorageDescription("metaInfo.instrumentMethod", String.class));
//                put("endRt", new FieldInStorageDescription("e.labName", String.class));
//                put("startRt", new FieldInStorageDescription("e.labName", String.class));
                put("creationDate", new FieldInStorageDescription("metaInfo.creationDate", Date.class));
                put("comment", new FieldInStorageDescription("metaInfo.metaInfo", String.class));
//                put("startMz", new FieldInStorageDescription("e.labName", String.class));
//                put("endMz", new FieldInStorageDescription("e.labName", String.class));
                put("fileName", new FieldInStorageDescription("metaInfo.fileName", String.class));
                put("seqRowPosition", new FieldInStorageDescription("metaInfo.seqRowPosition", String.class));
                put("sampleName", new FieldInStorageDescription("metaInfo.sampleName", String.class));
                put("translateFlag", new FieldInStorageDescription("metaInfo.translateFlag", String.class));
                put("instrumentSerialNumber", new FieldInStorageDescription("metaInfo.instrumentSerialNumber", String.class));
                put("phone", new FieldInStorageDescription("metaInfo.phone", String.class));
                put("instrumentName", new FieldInStorageDescription("metaInfo.instrumentName", String.class));
            }});
            put(ExperimentFileTemplate.class, new HashMap<String, FieldInStorageDescription>() {{
                put("id", new FieldInStorageDescription("rawFile.fileMetaData.id", Number.class));
                put("name", new FieldInStorageDescription("rawFile.fileMetaData.name", String.class));
                put("instrument", new FieldInStorageDescription("instrument.name", String.class));
                put("laboratory", new FieldInStorageDescription("lab.name", String.class));
                put("uploadDate", new FieldInStorageDescription("rawFile.fileMetaData.uploadDate", Date.class));
                put("labels", new FieldInStorageDescription("rawFile.fileMetaData.labels", String.class));
                put("sizeInBytes", new FieldInStorageDescription("rawFile.fileMetaData.sizeInBytes", Number.class));
                //meta info for file
                put("annotationInstrument", new FieldInStorageDescription("metaInfo.instrument", String.class));
                put("userName", new FieldInStorageDescription("metaInfo.userName", String.class));
                put("userLabels", new FieldInStorageDescription("metaInfo.metaInfo", String.class));
                put("fileCondition", new FieldInStorageDescription("metaInfo.fileCondition", String.class));
                put("instrumentMethod", new FieldInStorageDescription("metaInfo.instrumentMethod", String.class));
//                put("endRt", new FieldInStorageDescription("e.labName", String.class));
//                put("startRt", new FieldInStorageDescription("e.labName", String.class));
                put("creationDate", new FieldInStorageDescription("metaInfo.creationDate", Date.class));
                put("comment", new FieldInStorageDescription("metaInfo.metaInfo", String.class));
//                put("startMz", new FieldInStorageDescription("e.labName", String.class));
//                put("endMz", new FieldInStorageDescription("e.labName", String.class));
                put("fileName", new FieldInStorageDescription("metaInfo.fileName", String.class));
                put("seqRowPosition", new FieldInStorageDescription("metaInfo.seqRowPosition", String.class));
                put("sampleName", new FieldInStorageDescription("metaInfo.sampleName", String.class));
                put("translateFlag", new FieldInStorageDescription("metaInfo.translateFlag", String.class));
                put("instrumentSerialNumber", new FieldInStorageDescription("metaInfo.instrumentSerialNumber", String.class));
                put("phone", new FieldInStorageDescription("metaInfo.phone", String.class));
                put("instrumentName", new FieldInStorageDescription("metaInfo.instrumentName", String.class));
            }});
        }};
    }

}
