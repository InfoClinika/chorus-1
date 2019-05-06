package com.infoclinika.mssharing.web.demo;

import com.google.common.collect.Sets;
import com.infoclinika.mssharing.model.Notifier;
import com.infoclinika.mssharing.model.PredefinedDataCreator;
import com.infoclinika.mssharing.model.helper.ColumnViewHelper;
import com.infoclinika.mssharing.model.helper.ExperimentCreationHelper;
import com.infoclinika.mssharing.model.internal.features.FeaturesInitializer;
import com.infoclinika.mssharing.platform.model.InstrumentsDefaults;
import com.infoclinika.mssharing.platform.model.common.items.AdditionalExtensionImportance;
import com.infoclinika.mssharing.platform.model.common.items.FileExtensionItem;
import com.infoclinika.util.FilenameUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Sets.newHashSet;
import static com.infoclinika.mssharing.web.demo.VendorNames.*;

/**
 * @author Pavel Kaplin
 */
@Component
public class SeedDataCreator {
    public static final String BOTTOM_UP_PROTEOMICS_EXPERIMENT_TYPE = "Bottom Up Proteomics";
    private static final String THERMO_VENDOR = "Thermo Scientific";
    private static final String WATERS_VENDOR = "Waters";
    private static final String AGILENT_VENDOR = "Agilent";
    private static final String BRUKER_VENDOR = "Bruker";
    private static final String AB_SCIEX_VENDOR = "Sciex";
    private static final String NEXT_GEN_SEQUENCING_VENDOR = "Roche";
    private static final String METABOLOMICS_EXPERIMENT_TYPE = "Metabolomics";
    private static final String DMPK_EXPERIMENT_TYPE = "DMPK";
    private static final String ALIS_SCREENING = "ALIS Screening";
    private static final String OTHER_EXPERIMENT_TYPE = "Other";
    private static final String TOP_DOWN_PROTEOMICS_EXPERIMENT_TYPE = "Top Down Proteomics";
    private static final String HOMO_SAPIENS = "Homo sapiens";
    private static final String NUMBER_DATA_TYPE = "number";
    private static final String STRING_DATA_TYPE = "string";
    private static final String DATE_DATA_TYPE = "date";
    private static final String BOOLEAN_DATA_TYPE = "boolean";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String SIZE = "size";
    private static final String INSTRUMENT = "instrument";
    private static final String LABORATORY = "laboratory";
    private static final String UPLOAD_DATE = "upload date";
    private static final String LABELS = "labels";
    private static final String CREATION_DATE = "creation date";
    private static final String COMMENT = "comment";
    private static final String INSTRUMENT_METHOD = "instrument method";
    private static final String END_TIME = "end time";
    private static final String START_TIME = "start time";
    private static final String START_MZ = "start mz";
    private static final String END_MZ = "end mz";
    private static final String FILE_NAME = "file name";
    private static final String POSITION = "position";
    private static final String SAMPLE_NAME = "sample name";
    private static final String ANNOTATION_INSTRUMENT = "annotation instrument";
    private static final String USER_NAME = "user name";
    private static final String USER_LABELS = "user labels";
    private static final String FILE_CONDITION = "file condition";
    private static final String TRANSLATE_FLAG = "translate flag";
    private static final String INSTRUMENT_SERIAL = "instrument serial";
    private static final String DETECTED_ISOTOPE_GROUPS = "detected isotope groups";
    private static final String ANNOTATED_ISOTOPE_GROUPS = "annotated isotope groups";
    private static final String PHONE = "phone";
    private static final String INSTRUMENT_NAME = "instrument name";
    private static final String EXPERIMENT_NAME = "experiment name";
    private static final String OWNER = "owner";
    private static final String PROJECT = "project";
    private static final String FILES = "files";
    private static final String FAILED = "failed";
    private static final String MODIFIED = "modified";
    private static final String PROJECT_NAME = "project name";
    private static final String AREA_OF_RESEARCH = "area of research";
    private static final String OT = "OT";
    private static final String LTQ = "LTQ";
    private static final String FT = "FT";
    private static final String QQQ = "QQQ";
    private static final String Q_TOF = "Q-TOF";
    private static final String SINGLE_QUAD = "single quad";
    private static final String TANDEM_QUAD = "tandem quad";
    private static final String LC_TOF = "LC-TOF";
    private static final String MALDI_TOF = "MALDI-TOF";
    private static final String IT = "IT";
    private static final String TOF = "TOF";
    private static final String O_TOF = "O-TOF";
    private static final String TOF_TOF = "TOF/TOF";
    private static final String NTER = "Nter";
    private static final String R_AMINO_ACID = "R";
    private static final String K_AMINO_ACID = "K";
    private static final String C_AMINO_ACID = "C";
    private static final String EMPTY_AMINO_ACID = "";
    private static final int LABELS_COLUMN_INDEX = 6;
    private static final int UNITS_6 = LABELS_COLUMN_INDEX;
    private static final int UNITS_8 = 8;
    private static final int UNITS_10 = 10;
    private static final int UNITS_12 = 12;
    private static final int UNITS_14 = 14;
    private static final int UNITS_17 = 17;
    private static final int UNITS_22 = 22;
    private static final int UNITS_30 = 30;
    private static final String RAW = ".raw";
    private static final int ID_COLUMN_INDEX = 0;
    private static final int NAME_COLUMN_INDEX = 1;
    private static final int SIZE_COLUMN_INDEX = 2;
    private static final int INSTRUMENT_COLUMN_INDEX = 3;
    private static final int LAB_COLUMN_INDEX = 4;
    private static final int UPLOAD_DATY_COLUMN_INDEX = 5;
    private static final int FAILED_COLUMN_INDEX = 7;
    // Instriment NGS Model Type
    private static final String NEXTSEQ_MODEL_TYPE = "NextSeq";
    private static final String HiSEQ_MODEL_TYPE = "HiSeq";
    private static final String NOVASEQ_MODEL_TYPE = "NovaSeq";
    private static final String MINISEQ_MODEL_TYPE = "MiniSeq";
    private static final String MISEQ_MODEL_TYPE = "MiSeq";
    private static final String DEFAULT_NAME = "Default";

    // Technolgies
    private static final String MS_INSTRUMENT_STUDY_TYPE = InstrumentsDefaults.MS_TECHNOLOGY_TYPE;
    private static final String MA_INSTRUMENT_STUDY_TYPE = InstrumentsDefaults.MA_TECHNOLOGY_TYPE;
    private static final String NG_INSTRUMENT_STUDY_TYPE = InstrumentsDefaults.NG_TECHNOLOGY_TYPE;
    private static final String CL_INSTRUMENT_STUDY_TYPE = InstrumentsDefaults.CL_TECHNOLOGY_TYPE;
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Inject
    private PredefinedDataCreator initiator;
    @Inject
    private Notifier notifier;
    @Inject
    private FeaturesInitializer featuresInitializer;
    @Inject
    private ExperimentCreationHelper experimentCreationHelper;

    public SeedDataCreator() {
    }

    public void createSeedData() {
        featuresInitializer.initializeFeatures();

        if (isSeedDataAlreadyHere()) {
            log.info("Seed data is already here. Skipping");
            return;
        }

        experimentTypes();
        studyTypes();
        vendors();
        species();
        fileColumns();
        experimentColumns();
        projectColumns();
        experimentLabels();

        initiator.allUsersGroup();
    }

    private void fileColumns() {

        List<ColumnViewHelper.Column> columnSet = new ArrayList<ColumnViewHelper.Column>() {
            {
                add(new ColumnViewHelper.Column(0L, ID, NUMBER_DATA_TYPE, false, true, UNITS_6));
                add(new ColumnViewHelper.Column(0L, NAME, STRING_DATA_TYPE, false, true, UNITS_22));
                add(new ColumnViewHelper.Column(0L, SIZE, SIZE, true, true, UNITS_12));
                add(new ColumnViewHelper.Column(0L, INSTRUMENT, STRING_DATA_TYPE, true, true, UNITS_17));
                add(new ColumnViewHelper.Column(0L, LABORATORY, STRING_DATA_TYPE, true, true, UNITS_14));
                add(new ColumnViewHelper.Column(0L, UPLOAD_DATE, DATE_DATA_TYPE, true, true, UNITS_12));
                add(new ColumnViewHelper.Column(0L, LABELS, STRING_DATA_TYPE, true, true, UNITS_17));

                add(new ColumnViewHelper.Column(0L, CREATION_DATE, DATE_DATA_TYPE, true, false, UNITS_12));
                add(new ColumnViewHelper.Column(0L, COMMENT, STRING_DATA_TYPE, true, false, UNITS_17));
                add(new ColumnViewHelper.Column(0L, INSTRUMENT_METHOD, STRING_DATA_TYPE, true, false, UNITS_17));
                add(new ColumnViewHelper.Column(0L, END_TIME, DATE_DATA_TYPE, true, false, UNITS_12));
                add(new ColumnViewHelper.Column(0L, START_TIME, DATE_DATA_TYPE, true, false, UNITS_12));
                add(new ColumnViewHelper.Column(0L, START_MZ, NUMBER_DATA_TYPE, true, false, UNITS_12));
                add(new ColumnViewHelper.Column(0L, END_MZ, NUMBER_DATA_TYPE, true, false, UNITS_12));
                add(new ColumnViewHelper.Column(0L, FILE_NAME, STRING_DATA_TYPE, true, false, UNITS_17));
                add(new ColumnViewHelper.Column(0L, POSITION, NUMBER_DATA_TYPE, true, false, UNITS_12));
                add(new ColumnViewHelper.Column(0L, SAMPLE_NAME, STRING_DATA_TYPE, true, false, UNITS_17));
                add(new ColumnViewHelper.Column(0L, ANNOTATION_INSTRUMENT, STRING_DATA_TYPE, true, false, UNITS_17));
                add(new ColumnViewHelper.Column(0L, USER_NAME, STRING_DATA_TYPE, true, false, UNITS_17));
                add(new ColumnViewHelper.Column(0L, USER_LABELS, STRING_DATA_TYPE, true, false, UNITS_17));
                add(new ColumnViewHelper.Column(0L, FILE_CONDITION, STRING_DATA_TYPE, true, false, UNITS_17));
                add(new ColumnViewHelper.Column(0L, TRANSLATE_FLAG, STRING_DATA_TYPE, true, false, UNITS_17));
                add(new ColumnViewHelper.Column(0L, INSTRUMENT_SERIAL, STRING_DATA_TYPE, true, false, UNITS_17));
                add(new ColumnViewHelper.Column(0L, PHONE, STRING_DATA_TYPE, true, false, UNITS_17));
                add(new ColumnViewHelper.Column(0L, INSTRUMENT_NAME, STRING_DATA_TYPE, true, false, UNITS_17));
                add(new ColumnViewHelper.Column(0L, DETECTED_ISOTOPE_GROUPS, NUMBER_DATA_TYPE, true, false, UNITS_17));
                add(new ColumnViewHelper.Column(0L, ANNOTATED_ISOTOPE_GROUPS, NUMBER_DATA_TYPE, true, false, UNITS_17));
            }
        };

        final List<Long> ids = initiator.createColumnsDefinitions(columnSet, ColumnViewHelper.ColumnViewType.FILE);

        Set<ColumnViewHelper.ColumnInfo> columns = newHashSet(
            new ColumnViewHelper.ColumnInfo(ID, ID_COLUMN_INDEX, false, true, ids.get(ID_COLUMN_INDEX)),
            new ColumnViewHelper.ColumnInfo(NAME, NAME_COLUMN_INDEX, false, true, ids.get(NAME_COLUMN_INDEX)),
            new ColumnViewHelper.ColumnInfo(SIZE, SIZE_COLUMN_INDEX, true, true, ids.get(SIZE_COLUMN_INDEX)),
            new ColumnViewHelper.ColumnInfo(
                INSTRUMENT,
                INSTRUMENT_COLUMN_INDEX,
                true,
                true,
                ids.get(INSTRUMENT_COLUMN_INDEX)
            ),
            new ColumnViewHelper.ColumnInfo(LABORATORY, LAB_COLUMN_INDEX, true, true, ids.get(LAB_COLUMN_INDEX)),
            new ColumnViewHelper.ColumnInfo(
                UPLOAD_DATE,
                UPLOAD_DATY_COLUMN_INDEX,
                true,
                true,
                ids.get(UPLOAD_DATY_COLUMN_INDEX)
            ),
            new ColumnViewHelper.ColumnInfo(LABELS, LABELS_COLUMN_INDEX, true, true, ids.get(LABELS_COLUMN_INDEX))
        );

        initiator.defaultColumnsView(columns, ColumnViewHelper.ColumnViewType.FILE);
    }

    private void experimentColumns() {

        List<ColumnViewHelper.Column> columnSet = new ArrayList<ColumnViewHelper.Column>() {
            {
                add(new ColumnViewHelper.Column(0L, ID, NUMBER_DATA_TYPE, false, true, UNITS_8));
                add(new ColumnViewHelper.Column(0L, EXPERIMENT_NAME, STRING_DATA_TYPE, false, true, UNITS_30));
                add(new ColumnViewHelper.Column(0L, OWNER, STRING_DATA_TYPE, true, true, UNITS_10));
                add(new ColumnViewHelper.Column(0L, LABORATORY, STRING_DATA_TYPE, true, true, UNITS_12));
                add(new ColumnViewHelper.Column(0L, PROJECT, STRING_DATA_TYPE, true, true, UNITS_12));
                add(new ColumnViewHelper.Column(0L, FILES, NUMBER_DATA_TYPE, true, true, UNITS_8));
                add(new ColumnViewHelper.Column(0L, MODIFIED, DATE_DATA_TYPE, true, true, UNITS_10));
                add(new ColumnViewHelper.Column(0L, FAILED, BOOLEAN_DATA_TYPE, true, true, UNITS_10));
            }
        };

        final List<Long> ids =
            initiator.createColumnsDefinitions(columnSet, ColumnViewHelper.ColumnViewType.EXPERIMENT);

        Set<ColumnViewHelper.ColumnInfo> columns = newHashSet(
            new ColumnViewHelper.ColumnInfo(ID, ID_COLUMN_INDEX, false, true, ids.get(ID_COLUMN_INDEX)),
            new ColumnViewHelper.ColumnInfo(
                EXPERIMENT_NAME,
                NAME_COLUMN_INDEX,
                false,
                true,
                ids.get(NAME_COLUMN_INDEX)
            ),
            new ColumnViewHelper.ColumnInfo(OWNER, SIZE_COLUMN_INDEX, true, true, ids.get(SIZE_COLUMN_INDEX)),
            new ColumnViewHelper.ColumnInfo(
                LABORATORY,
                INSTRUMENT_COLUMN_INDEX,
                true,
                true,
                ids.get(INSTRUMENT_COLUMN_INDEX)
            ),
            new ColumnViewHelper.ColumnInfo(PROJECT, LAB_COLUMN_INDEX, true, true, ids.get(LAB_COLUMN_INDEX)),
            new ColumnViewHelper.ColumnInfo(
                FILES,
                UPLOAD_DATY_COLUMN_INDEX,
                true,
                true,
                ids.get(UPLOAD_DATY_COLUMN_INDEX)
            ),
            new ColumnViewHelper.ColumnInfo(MODIFIED, LABELS_COLUMN_INDEX, true, true, ids.get(LABELS_COLUMN_INDEX)),
            new ColumnViewHelper.ColumnInfo(FAILED, FAILED_COLUMN_INDEX, true, true, ids.get(FAILED_COLUMN_INDEX))
        );

        initiator.defaultColumnsView(columns, ColumnViewHelper.ColumnViewType.EXPERIMENT);
    }

    private void projectColumns() {

        List<ColumnViewHelper.Column> columnSet = new ArrayList<ColumnViewHelper.Column>() {
            {
                add(new ColumnViewHelper.Column(0L, ID, NUMBER_DATA_TYPE, false, true, UNITS_8));
                add(new ColumnViewHelper.Column(0L, PROJECT_NAME, STRING_DATA_TYPE, false, true, UNITS_30));
                add(new ColumnViewHelper.Column(0L, OWNER, STRING_DATA_TYPE, true, true, UNITS_12));
                add(new ColumnViewHelper.Column(0L, LABORATORY, STRING_DATA_TYPE, true, true, UNITS_12));
                add(new ColumnViewHelper.Column(0L, AREA_OF_RESEARCH, STRING_DATA_TYPE, true, true, UNITS_12));
                add(new ColumnViewHelper.Column(0L, MODIFIED, DATE_DATA_TYPE, true, true, UNITS_12));

            }
        };

        final List<Long> ids = initiator.createColumnsDefinitions(columnSet, ColumnViewHelper.ColumnViewType.PROJECT);

        Set<ColumnViewHelper.ColumnInfo> columns = newHashSet(
            new ColumnViewHelper.ColumnInfo(ID, ID_COLUMN_INDEX, false, true, ids.get(ID_COLUMN_INDEX)),
            new ColumnViewHelper.ColumnInfo(PROJECT_NAME, NAME_COLUMN_INDEX, false, true, ids.get(NAME_COLUMN_INDEX)),
            new ColumnViewHelper.ColumnInfo(OWNER, SIZE_COLUMN_INDEX, true, true, ids.get(SIZE_COLUMN_INDEX)),
            new ColumnViewHelper.ColumnInfo(
                LABORATORY,
                INSTRUMENT_COLUMN_INDEX,
                true,
                true,
                ids.get(INSTRUMENT_COLUMN_INDEX)
            ),
            new ColumnViewHelper.ColumnInfo(AREA_OF_RESEARCH, LAB_COLUMN_INDEX, true, true, ids.get(LAB_COLUMN_INDEX)),
            new ColumnViewHelper.ColumnInfo(
                MODIFIED,
                UPLOAD_DATY_COLUMN_INDEX,
                true,
                true,
                ids.get(UPLOAD_DATY_COLUMN_INDEX)
            )
        );

        initiator.defaultColumnsView(columns, ColumnViewHelper.ColumnViewType.PROJECT);
    }


    private void experimentTypes() {
        notifier.setEnabled(false);
        initiator.experimentType(BOTTOM_UP_PROTEOMICS_EXPERIMENT_TYPE, true, true);
        initiator.experimentType(METABOLOMICS_EXPERIMENT_TYPE, false, false);
        initiator.experimentType(DMPK_EXPERIMENT_TYPE, false, false);
        initiator.experimentType(OTHER_EXPERIMENT_TYPE, false, false);
        initiator.experimentType(TOP_DOWN_PROTEOMICS_EXPERIMENT_TYPE, false, false);
        initiator.experimentType(ALIS_SCREENING, false, false);
        notifier.setEnabled(true);
    }

    private void studyTypes() {
        initiator.createInstrumentStudyType(MS_INSTRUMENT_STUDY_TYPE);
        initiator.createInstrumentStudyType(MA_INSTRUMENT_STUDY_TYPE);
        initiator.createInstrumentStudyType(NG_INSTRUMENT_STUDY_TYPE);
        initiator.createInstrumentStudyType(CL_INSTRUMENT_STUDY_TYPE);
    }

    private void vendors() {
        notifier.setEnabled(false);

        final HashSet<FileExtensionItem> raws = newHashSet(new FileExtensionItem(
            RAW,
            EMPTY_AMINO_ACID,
            Collections.<String, AdditionalExtensionImportance>emptyMap()
        ));
        initiator.instrumentModel(THERMO_VENDOR, LTQ, MS_INSTRUMENT_STUDY_TYPE, LTQ, false, false, raws);
        initiator.instrumentModel(THERMO_VENDOR, OT, MS_INSTRUMENT_STUDY_TYPE, "LTQ Orbitrap XL", false, false, raws);
        initiator.instrumentModel(THERMO_VENDOR, OT, MS_INSTRUMENT_STUDY_TYPE, "Exactive Plus", false, false, raws);
        initiator.instrumentModel(
            THERMO_VENDOR,
            OT,
            MS_INSTRUMENT_STUDY_TYPE,
            "LTQ Orbitrap XL ETD",
            false,
            false,
            raws
        );
        initiator.instrumentModel(THERMO_VENDOR, OT, MS_INSTRUMENT_STUDY_TYPE, "Orbitrap Elite", false, false, raws);
        initiator.instrumentModel(THERMO_VENDOR, OT, MS_INSTRUMENT_STUDY_TYPE, "Orbitrap Velos", false, false, raws);
        initiator.instrumentModel(THERMO_VENDOR, LTQ, MS_INSTRUMENT_STUDY_TYPE, "LTQ Velos", false, false, raws);
        initiator.instrumentModel(THERMO_VENDOR, LTQ, MS_INSTRUMENT_STUDY_TYPE, "LTQ Velos Pro", false, false, raws);
        initiator.instrumentModel(THERMO_VENDOR, OT, MS_INSTRUMENT_STUDY_TYPE, "Orbitrap Fusion", false, false, raws);
        initiator.instrumentModel(THERMO_VENDOR, FT, MS_INSTRUMENT_STUDY_TYPE, "LTQ-FT", false, false, raws);
        initiator.instrumentModel(THERMO_VENDOR, OT, MS_INSTRUMENT_STUDY_TYPE, "Q Exactive", false, false, raws);
        initiator.instrumentModel(THERMO_VENDOR, QQQ, MS_INSTRUMENT_STUDY_TYPE, "TSQ Quantum", false, false, raws);
        initiator.instrumentModel(
            THERMO_VENDOR,
            QQQ,
            MS_INSTRUMENT_STUDY_TYPE,
            "TSQ Quantum Ultra",
            false,
            false,
            raws
        );
        initiator.instrumentModel(THERMO_VENDOR, QQQ, MS_INSTRUMENT_STUDY_TYPE, "TSQ Vantage", false, false, raws);
        initiator.instrumentModel(THERMO_VENDOR, QQQ, MS_INSTRUMENT_STUDY_TYPE, "TSQ Quantiva", false, false, raws);

        final HashSet<FileExtensionItem> waters =
            newHashSet(new FileExtensionItem(RAW, RAW, Collections.<String, AdditionalExtensionImportance>emptyMap()));
        initiator.instrumentModel(WATERS_VENDOR, Q_TOF, MS_INSTRUMENT_STUDY_TYPE, "API-US", true, false, waters);
        initiator.instrumentModel(WATERS_VENDOR, Q_TOF, MS_INSTRUMENT_STUDY_TYPE, "Micro", true, false, waters);
        initiator.instrumentModel(WATERS_VENDOR, Q_TOF, MS_INSTRUMENT_STUDY_TYPE, "Premier", true, false, waters);

        initiator.instrumentModel(WATERS_VENDOR, Q_TOF, MS_INSTRUMENT_STUDY_TYPE, "Synapt HDMS", true, false, waters);
        initiator.instrumentModel(
            WATERS_VENDOR,
            Q_TOF,
            MS_INSTRUMENT_STUDY_TYPE,
            "MALDI Synapt MS",
            true,
            false,
            waters
        );
        initiator.instrumentModel(
            WATERS_VENDOR,
            Q_TOF,
            MS_INSTRUMENT_STUDY_TYPE,
            "MALDI Synapt HDMS",
            true,
            false,
            waters
        );
        initiator.instrumentModel(WATERS_VENDOR, Q_TOF, MS_INSTRUMENT_STUDY_TYPE, "Synapt G2 MS", true, false, waters);
        initiator.instrumentModel(
            WATERS_VENDOR,
            Q_TOF,
            MS_INSTRUMENT_STUDY_TYPE,
            "Synapt G2 HDMS",
            true,
            false,
            waters
        );
        initiator.instrumentModel(
            WATERS_VENDOR,
            Q_TOF,
            MS_INSTRUMENT_STUDY_TYPE,
            "MALDI Synapt G2 MS",
            true,
            false,
            waters
        );
        initiator.instrumentModel(
            WATERS_VENDOR,
            Q_TOF,
            MS_INSTRUMENT_STUDY_TYPE,
            "MALDI Synapt G2 HDMS",
            true,
            false,
            waters
        );
        initiator.instrumentModel(
            WATERS_VENDOR,
            Q_TOF,
            MS_INSTRUMENT_STUDY_TYPE,
            "Synapt G2-S MS",
            true,
            false,
            waters
        );
        initiator.instrumentModel(
            WATERS_VENDOR,
            Q_TOF,
            MS_INSTRUMENT_STUDY_TYPE,
            "Synapt G2-S MS",
            true,
            false,
            waters
        );
        initiator.instrumentModel(
            WATERS_VENDOR,
            Q_TOF,
            MS_INSTRUMENT_STUDY_TYPE,
            "Synapt G2-S HDMS",
            true,
            false,
            waters
        );
        initiator.instrumentModel(
            WATERS_VENDOR,
            Q_TOF,
            MS_INSTRUMENT_STUDY_TYPE,
            "MALDI Synapt G2-S MS",
            true,
            false,
            waters
        );
        initiator.instrumentModel(
            WATERS_VENDOR,
            Q_TOF,
            MS_INSTRUMENT_STUDY_TYPE,
            "MALDI Synapt G2-S HDMS",
            true,
            false,
            waters
        );
        initiator.instrumentModel(WATERS_VENDOR, Q_TOF, MS_INSTRUMENT_STUDY_TYPE, "Xevo QTof", true, false, waters);
        initiator.instrumentModel(WATERS_VENDOR, Q_TOF, MS_INSTRUMENT_STUDY_TYPE, "Xevo G2 QTof", true, false, waters);
        initiator.instrumentModel(
            WATERS_VENDOR,
            Q_TOF,
            MS_INSTRUMENT_STUDY_TYPE,
            "Xevo G2-S QTof",
            true,
            false,
            waters
        );

        initiator.instrumentModel(
            WATERS_VENDOR,
            SINGLE_QUAD,
            MS_INSTRUMENT_STUDY_TYPE,
            "Platform",
            true,
            false,
            waters
        );
        initiator.instrumentModel(
            WATERS_VENDOR,
            SINGLE_QUAD,
            MS_INSTRUMENT_STUDY_TYPE,
            "Platform II",
            true,
            false,
            waters
        );
        initiator.instrumentModel(WATERS_VENDOR, SINGLE_QUAD, MS_INSTRUMENT_STUDY_TYPE, "PLC", true, false, waters);
        initiator.instrumentModel(
            WATERS_VENDOR,
            SINGLE_QUAD,
            MS_INSTRUMENT_STUDY_TYPE,
            "Platform LCZ",
            true,
            false,
            waters
        );
        initiator.instrumentModel(WATERS_VENDOR, SINGLE_QUAD, MS_INSTRUMENT_STUDY_TYPE, "ZMD", true, false, waters);
        initiator.instrumentModel(WATERS_VENDOR, SINGLE_QUAD, MS_INSTRUMENT_STUDY_TYPE, "ZQ", true, false, waters);
        initiator.instrumentModel(
            WATERS_VENDOR,
            SINGLE_QUAD,
            MS_INSTRUMENT_STUDY_TYPE,
            "EMD 1000",
            true,
            false,
            waters
        );
        initiator.instrumentModel(WATERS_VENDOR, SINGLE_QUAD, MS_INSTRUMENT_STUDY_TYPE, "3100", true, false, waters);
        initiator.instrumentModel(
            WATERS_VENDOR,
            SINGLE_QUAD,
            MS_INSTRUMENT_STUDY_TYPE,
            "ACQUITY SQD",
            true,
            false,
            waters
        );
        initiator.instrumentModel(
            WATERS_VENDOR,
            SINGLE_QUAD,
            MS_INSTRUMENT_STUDY_TYPE,
            "SQ Detector 2",
            true,
            false,
            waters
        );

        initiator.instrumentModel(WATERS_VENDOR, TANDEM_QUAD, MS_INSTRUMENT_STUDY_TYPE, "Bio-Q", true, false, waters);
        initiator.instrumentModel(WATERS_VENDOR, TANDEM_QUAD, MS_INSTRUMENT_STUDY_TYPE, "Quattro", true, false, waters);
        initiator.instrumentModel(
            WATERS_VENDOR,
            TANDEM_QUAD,
            MS_INSTRUMENT_STUDY_TYPE,
            "Quattro II",
            true,
            false,
            waters
        );
        initiator.instrumentModel(
            WATERS_VENDOR,
            TANDEM_QUAD,
            MS_INSTRUMENT_STUDY_TYPE,
            "Quattro LC",
            true,
            false,
            waters
        );
        initiator.instrumentModel(
            WATERS_VENDOR,
            TANDEM_QUAD,
            MS_INSTRUMENT_STUDY_TYPE,
            "Quattro Ultima",
            true,
            false,
            waters
        );
        initiator.instrumentModel(
            WATERS_VENDOR,
            TANDEM_QUAD,
            MS_INSTRUMENT_STUDY_TYPE,
            "Quattro Ultima Pt",
            true,
            false,
            waters
        );
        initiator.instrumentModel(
            WATERS_VENDOR,
            TANDEM_QUAD,
            MS_INSTRUMENT_STUDY_TYPE,
            "Quattro micro",
            true,
            false,
            waters
        );
        initiator.instrumentModel(
            WATERS_VENDOR,
            TANDEM_QUAD,
            MS_INSTRUMENT_STUDY_TYPE,
            "Quattro Premier",
            true,
            false,
            waters
        );
        initiator.instrumentModel(
            WATERS_VENDOR,
            TANDEM_QUAD,
            MS_INSTRUMENT_STUDY_TYPE,
            "Quattro Premier XE",
            true,
            false,
            waters
        );
        initiator.instrumentModel(
            WATERS_VENDOR,
            TANDEM_QUAD,
            MS_INSTRUMENT_STUDY_TYPE,
            "ACQUITY TQD",
            true,
            false,
            waters
        );
        initiator.instrumentModel(
            WATERS_VENDOR,
            TANDEM_QUAD,
            MS_INSTRUMENT_STUDY_TYPE,
            "Xevo TQ MS",
            true,
            false,
            waters
        );
        initiator.instrumentModel(
            WATERS_VENDOR,
            TANDEM_QUAD,
            MS_INSTRUMENT_STUDY_TYPE,
            "Xevo TQ-S",
            true,
            false,
            waters
        );
        initiator.instrumentModel(
            WATERS_VENDOR,
            TANDEM_QUAD,
            MS_INSTRUMENT_STUDY_TYPE,
            "Xevo TQD",
            true,
            false,
            waters
        );

        initiator.instrumentModel(WATERS_VENDOR, LC_TOF, MS_INSTRUMENT_STUDY_TYPE, "LCT", true, false, waters);
        initiator.instrumentModel(WATERS_VENDOR, LC_TOF, MS_INSTRUMENT_STUDY_TYPE, "LCT Premier", true, false, waters);
        initiator.instrumentModel(
            WATERS_VENDOR,
            LC_TOF,
            MS_INSTRUMENT_STUDY_TYPE,
            "LCT Premier XE",
            true,
            false,
            waters
        );
        initiator.instrumentModel(WATERS_VENDOR, LC_TOF, MS_INSTRUMENT_STUDY_TYPE, "Xevo G2 Tof", true, false, waters);
        initiator.instrumentModel(
            WATERS_VENDOR,
            LC_TOF,
            MS_INSTRUMENT_STUDY_TYPE,
            "Xevo G2-S Tof",
            true,
            false,
            waters
        );

        initiator.instrumentModel(WATERS_VENDOR, MALDI_TOF, MS_INSTRUMENT_STUDY_TYPE, "MALDI L", true, false, waters);
        initiator.instrumentModel(WATERS_VENDOR, MALDI_TOF, MS_INSTRUMENT_STUDY_TYPE, "MALDI LR", true, false, waters);
        initiator.instrumentModel(WATERS_VENDOR, MALDI_TOF, MS_INSTRUMENT_STUDY_TYPE, "MALDI R", true, false, waters);
        initiator.instrumentModel(
            WATERS_VENDOR,
            MALDI_TOF,
            MS_INSTRUMENT_STUDY_TYPE,
            "MALDI micro MX",
            true,
            false,
            waters
        );
        initiator.instrumentModel(WATERS_VENDOR, MALDI_TOF, MS_INSTRUMENT_STUDY_TYPE, "TofSpec", true, false, waters);
        initiator.instrumentModel(
            WATERS_VENDOR,
            MALDI_TOF,
            MS_INSTRUMENT_STUDY_TYPE,
            "TofSpec 2E",
            true,
            false,
            waters
        );
        initiator.instrumentModel(WATERS_VENDOR, MALDI_TOF, MS_INSTRUMENT_STUDY_TYPE, "TofSpec E", true, false, waters);
        initiator.instrumentModel(
            WATERS_VENDOR,
            MALDI_TOF,
            MS_INSTRUMENT_STUDY_TYPE,
            "TofSpec SE",
            true,
            false,
            waters
        );


        final HashSet<FileExtensionItem> ds = newHashSet(new FileExtensionItem(
            ".d",
            ".d",
            Collections.<String, AdditionalExtensionImportance>emptyMap()
        ));
        initiator.instrumentModel(
            AGILENT_VENDOR,
            IT,
            MS_INSTRUMENT_STUDY_TYPE,
            "220 GC/MS Ion Trap System",
            true,
            false,
            ds
        );
        initiator.instrumentModel(
            AGILENT_VENDOR,
            IT,
            MS_INSTRUMENT_STUDY_TYPE,
            "240 GC/MS Ion Trap System",
            true,
            false,
            ds
        );
        initiator.instrumentModel(
            AGILENT_VENDOR,
            QQQ,
            MS_INSTRUMENT_STUDY_TYPE,
            "7000 Triple Quadrupole GC/MS System",
            true,
            false,
            ds
        );
        initiator.instrumentModel(AGILENT_VENDOR, Q_TOF, MS_INSTRUMENT_STUDY_TYPE, "7200 GC/Q-TOF", true, false, ds);
        initiator.instrumentModel(
            AGILENT_VENDOR,
            TOF,
            MS_INSTRUMENT_STUDY_TYPE,
            "6200 Series Accurate-Mass Time-of-Flight (TOF) LC/MS",
            true,
            false,
            ds
        );
        initiator.instrumentModel(
            AGILENT_VENDOR,
            QQQ,
            MS_INSTRUMENT_STUDY_TYPE,
            "6400 Series Triple Quadrupole LC/MS",
            true,
            false,
            ds
        );

        final HashSet<FileExtensionItem> bruker = newHashSet(new FileExtensionItem(".d", ".d",
            Collections.<String, AdditionalExtensionImportance>emptyMap()
        ));
        initiator.instrumentModel(
            BRUKER_VENDOR,
            O_TOF,
            MS_INSTRUMENT_STUDY_TYPE,
            "micrOTOF focus II",
            true,
            true,
            bruker
        );
        initiator.instrumentModel(BRUKER_VENDOR, O_TOF, MS_INSTRUMENT_STUDY_TYPE, "micrOTOF-Q II", true, true, bruker);
        initiator.instrumentModel(BRUKER_VENDOR, TOF, MS_INSTRUMENT_STUDY_TYPE, "maXis impact", true, true, bruker);
        initiator.instrumentModel(BRUKER_VENDOR, TOF, MS_INSTRUMENT_STUDY_TYPE, "maXis 4G", true, true, bruker);
        initiator.instrumentModel(BRUKER_VENDOR, FT, MS_INSTRUMENT_STUDY_TYPE, "solariX", true, true, bruker);
        initiator.instrumentModel(BRUKER_VENDOR, QQQ, MS_INSTRUMENT_STUDY_TYPE, "EVOQ Elite", true, true, bruker);
        initiator.instrumentModel(BRUKER_VENDOR, QQQ, MS_INSTRUMENT_STUDY_TYPE, "EVOQ Qube", true, true, bruker);

        final HashMap<String, AdditionalExtensionImportance> additionalExtensionsWiff =
            new HashMap<String, AdditionalExtensionImportance>() {
                {
                    put(".wiff.scan", AdditionalExtensionImportance.NOT_REQUIRED);
                    put(".wiff.mtd", AdditionalExtensionImportance.NOT_REQUIRED);
                }
            };

        final HashSet<FileExtensionItem> wiff =
            newHashSet(new FileExtensionItem(".wiff", ".wiff", additionalExtensionsWiff));
        initiator.instrumentModel(AB_SCIEX_VENDOR, TOF, MS_INSTRUMENT_STUDY_TYPE, "TripleTOF 5600", false, true, wiff);
        initiator.instrumentModel(AB_SCIEX_VENDOR, TOF, MS_INSTRUMENT_STUDY_TYPE, "TripleTOF 6600", false, true, wiff);
        initiator.instrumentModel(AB_SCIEX_VENDOR, TOF_TOF, MS_INSTRUMENT_STUDY_TYPE, "MALDI 4800", false, true, wiff);
        initiator.instrumentModel(AB_SCIEX_VENDOR, QQQ, MS_INSTRUMENT_STUDY_TYPE, "4000 QTRAP", false, true, wiff);
        initiator.instrumentModel(AB_SCIEX_VENDOR, QQQ, MS_INSTRUMENT_STUDY_TYPE, "QTRAP 5500", false, true, wiff);

        /*NextGen Sequencing instrument models*/
        final Set<FileExtensionItem> nextGenExtensions =
            makeFileExtensionsSet(".fastq", ".fastq.gz", ".bam", ".sam", ".txt");

        initiator.instrumentModel(
            ILLUMINA_VENDOR,
            HiSEQ_MODEL_TYPE,
            NG_INSTRUMENT_STUDY_TYPE,
            "HiSeq 2500",
            false,
            false,
            nextGenExtensions
        );
        initiator.instrumentModel(
            ILLUMINA_VENDOR,
            HiSEQ_MODEL_TYPE,
            NG_INSTRUMENT_STUDY_TYPE,
            "HiSeq 3000",
            false,
            false,
            nextGenExtensions
        );
        initiator.instrumentModel(
            ILLUMINA_VENDOR,
            HiSEQ_MODEL_TYPE,
            NG_INSTRUMENT_STUDY_TYPE,
            "HiSeq 4000",
            false,
            false,
            nextGenExtensions
        );
        initiator.instrumentModel(
            ILLUMINA_VENDOR,
            HiSEQ_MODEL_TYPE,
            NG_INSTRUMENT_STUDY_TYPE,
            "HiSeq X",
            false,
            false,
            nextGenExtensions
        );

        initiator.instrumentModel(
            ILLUMINA_VENDOR,
            NEXTSEQ_MODEL_TYPE,
            NG_INSTRUMENT_STUDY_TYPE,
            "NextSeq 500",
            false,
            false,
            nextGenExtensions
        );
        initiator.instrumentModel(
            ILLUMINA_VENDOR,
            NEXTSEQ_MODEL_TYPE,
            NG_INSTRUMENT_STUDY_TYPE,
            "NextSeq 550",
            false,
            false,
            nextGenExtensions
        );


        initiator.instrumentModel(
            ILLUMINA_VENDOR,
            NOVASEQ_MODEL_TYPE,
            NG_INSTRUMENT_STUDY_TYPE,
            "NovaSeq 5000",
            false,
            false,
            nextGenExtensions
        );
        initiator.instrumentModel(
            ILLUMINA_VENDOR,
            NOVASEQ_MODEL_TYPE,
            NG_INSTRUMENT_STUDY_TYPE,
            "NovaSeq 6000",
            false,
            false,
            nextGenExtensions
        );

        initiator.instrumentModel(
            ILLUMINA_VENDOR,
            MISEQ_MODEL_TYPE,
            NG_INSTRUMENT_STUDY_TYPE,
            "MiSeqDx",
            false,
            false,
            nextGenExtensions
        );
        initiator.instrumentModel(
            ILLUMINA_VENDOR,
            MISEQ_MODEL_TYPE,
            NG_INSTRUMENT_STUDY_TYPE,
            "MiSeq FGx",
            false,
            false,
            nextGenExtensions
        );

        initiator.instrumentModel(
            ILLUMINA_VENDOR,
            MINISEQ_MODEL_TYPE,
            NG_INSTRUMENT_STUDY_TYPE,
            "MiniSeq",
            false,
            false,
            nextGenExtensions
        );

        initiator.instrumentModel(
            PACBIO_VENDOR,
            PACBIO_VENDOR,
            NG_INSTRUMENT_STUDY_TYPE,
            PACBIO_VENDOR,
            false,
            false,
            nextGenExtensions
        );
        initiator.instrumentModel(
            ROCHE_VENDOR,
            ROCHE_VENDOR,
            NG_INSTRUMENT_STUDY_TYPE,
            ROCHE_VENDOR,
            false,
            false,
            nextGenExtensions
        );

        final Set<FileExtensionItem> defaultExtensions = makeFileExtensionsSet(FilenameUtil.ALL_FILES_DOT_STAR);
        createDefaultVendorForTechnology(CL_INSTRUMENT_STUDY_TYPE, defaultExtensions);
        createDefaultVendorForTechnology(NG_INSTRUMENT_STUDY_TYPE, defaultExtensions);
        createDefaultVendorForTechnology(MA_INSTRUMENT_STUDY_TYPE, defaultExtensions);
        createDefaultVendorForTechnology(MS_INSTRUMENT_STUDY_TYPE, defaultExtensions);

        notifier.setEnabled(true);
    }

    private void createDefaultVendorForTechnology(String nameTechnology, Set<FileExtensionItem> fileExtensions) {
        initiator.instrumentModel(
            DEFAULT_VENDOR,
            DEFAULT_NAME,
            nameTechnology,
            DEFAULT_NAME,
            false,
            false,
            fileExtensions
        );
    }

    private void species() {
        notifier.setEnabled(false);
        final Set<String> species = Sets.newHashSet(
            "Unspecified",
            "Arabidopsis thaliana",
            "Escherichia coli",
            "Pneumocystis carinii",
            "Bos taurus",
            "Hepatitis C virus",
            "Rattus norvegicus",
            "Caenorhabditis elegans",
            HOMO_SAPIENS,
            "Saccharomyces cerevisiae",
            "Chlamydomonas reinhardtii",
            "Mus musculus",
            "Schizosaccharomyces pombe",
            "Danio rerio (zebrafish)",
            "Mycoplasma pneumoniae",
            "Takifugu rubripes",
            "Dictyostelium discoideum",
            "Oryza sativa",
            "Xenopus laevis",
            "Drosophila melanogaster",
            "Plasmodium falciparum",
            "Zea mays",
            "Bemisia tabaci (Gennadius)",
            "Mucata mulata",
            "Canis familiaris",
            "Gallus gallus",
            "Sus scrofa",
            "Anopheles gambiae",
            "Caenorhabditis briggsae",
            "Caenorhabditis remanei",
            "Haliotis rufescens",
            "Pseudomonas aeruginosa",
            "Staphylococcus aureus",
            "Gasterosteus aculeatus (fish)",
            "Trichomonas vaginalis"
        );
        initiator.species(species.toArray(new String[species.size()]));


        notifier.setEnabled(true);
    }

    public boolean isSeedDataAlreadyHere() {
        return !experimentCreationHelper.experimentTypes().isEmpty();
    }

    private Set<FileExtensionItem> makeFileExtensionsSet(String... extensions) {
        return Stream.of(extensions)
            .map(ext -> new FileExtensionItem(ext, EMPTY_AMINO_ACID, Collections.emptyMap()))
            .collect(Collectors.toSet());
    }

    private void experimentLabels() {
        long id1 = initiator.createExperimentLabelType("SILAC", 3);
        initiator.createExperimentLabel(id1, R_AMINO_ACID, "Arg6");
        initiator.createExperimentLabel(id1, R_AMINO_ACID, "Arg10");
        initiator.createExperimentLabel(id1, K_AMINO_ACID, "Lys4");
        initiator.createExperimentLabel(id1, K_AMINO_ACID, "Lys6");
        initiator.createExperimentLabel(id1, K_AMINO_ACID, "Lys8");

        long id2 = initiator.createExperimentLabelType("ICAT", 2);
        initiator.createExperimentLabel(id2, C_AMINO_ACID, "ICAT-9");
        initiator.createExperimentLabel(id2, C_AMINO_ACID, "ICAT-0");

        long id3 = initiator.createExperimentLabelType("O18-K", 1);
        initiator.createExperimentLabel(id3, K_AMINO_ACID, "O18");

        long id4 = initiator.createExperimentLabelType("Dimethyl Labeling", 3);
        initiator.createExperimentLabel(id4, K_AMINO_ACID, "DymethLys0");
        initiator.createExperimentLabel(id4, K_AMINO_ACID, "DymethLys2");
        initiator.createExperimentLabel(id4, K_AMINO_ACID, "DymethLys4");
        initiator.createExperimentLabel(id4, K_AMINO_ACID, "DymethLys6");
        initiator.createExperimentLabel(id4, K_AMINO_ACID, "DymethLys8");
        initiator.createExperimentLabel(id4, NTER, "DymethNter0");
        initiator.createExperimentLabel(id4, NTER, "DymethNter2");
        initiator.createExperimentLabel(id4, NTER, "DymethNter4");
        initiator.createExperimentLabel(id4, NTER, "DymethNter6");
        initiator.createExperimentLabel(id4, NTER, "DymethNter8");

        long id5 = initiator.createExperimentLabelType("mTRAQ", 10);
        initiator.createExperimentLabel(id5, K_AMINO_ACID, "mTRAQ-Lys0");
        initiator.createExperimentLabel(id5, K_AMINO_ACID, "mTRAQ-Lys4");
        initiator.createExperimentLabel(id5, K_AMINO_ACID, "mTRAQ-Lys8");
        initiator.createExperimentLabel(id5, NTER, "mTRAQ-Nter0");
        initiator.createExperimentLabel(id5, NTER, "mTRAQ-Nter0");
        initiator.createExperimentLabel(id5, NTER, "mTRAQ-Nter0");

        long id6 = initiator.createExperimentLabelType("iTRAQ", 10);
        initiator.createExperimentLabel(id6, EMPTY_AMINO_ACID, "iTRAQ4plex-Lys114");
        initiator.createExperimentLabel(id6, EMPTY_AMINO_ACID, "iTRAQ4plex-Lys115");
        initiator.createExperimentLabel(id6, EMPTY_AMINO_ACID, "iTRAQ4plex-Lys116");
        initiator.createExperimentLabel(id6, EMPTY_AMINO_ACID, "iTRAQ4plex-Lys117");
        initiator.createExperimentLabel(id6, EMPTY_AMINO_ACID, "iTRAQ4plex-Nter114");
        initiator.createExperimentLabel(id6, EMPTY_AMINO_ACID, "iTRAQ4plex-Nter115");
        initiator.createExperimentLabel(id6, EMPTY_AMINO_ACID, "iTRAQ4plex-Nter116");
        initiator.createExperimentLabel(id6, EMPTY_AMINO_ACID, "iTRAQ4plex-Nter117");
        initiator.createExperimentLabel(id6, EMPTY_AMINO_ACID, "iTRAQ8plex-Lys113");
        initiator.createExperimentLabel(id6, EMPTY_AMINO_ACID, "iTRAQ8plex-Lys114");
        initiator.createExperimentLabel(id6, EMPTY_AMINO_ACID, "iTRAQ8plex-Lys115");
        initiator.createExperimentLabel(id6, EMPTY_AMINO_ACID, "iTRAQ8plex-Lys116");
        initiator.createExperimentLabel(id6, EMPTY_AMINO_ACID, "iTRAQ8plex-Lys117");
        initiator.createExperimentLabel(id6, EMPTY_AMINO_ACID, "iTRAQ8plex-Lys118");
        initiator.createExperimentLabel(id6, EMPTY_AMINO_ACID, "iTRAQ8plex-Lys119");
        initiator.createExperimentLabel(id6, EMPTY_AMINO_ACID, "iTRAQ8plex-Lys121");
        initiator.createExperimentLabel(id6, EMPTY_AMINO_ACID, "iTRAQ8plex-Nter113");
        initiator.createExperimentLabel(id6, EMPTY_AMINO_ACID, "iTRAQ8plex-Nter114");
        initiator.createExperimentLabel(id6, EMPTY_AMINO_ACID, "iTRAQ8plex-Nter115");
        initiator.createExperimentLabel(id6, EMPTY_AMINO_ACID, "iTRAQ8plex-Nter116");
        initiator.createExperimentLabel(id6, EMPTY_AMINO_ACID, "iTRAQ8plex-Nter117");
        initiator.createExperimentLabel(id6, EMPTY_AMINO_ACID, "iTRAQ8plex-Nter118");
        initiator.createExperimentLabel(id6, EMPTY_AMINO_ACID, "iTRAQ8plex-Nter119");
        initiator.createExperimentLabel(id6, EMPTY_AMINO_ACID, "iTRAQ8plex-Nter121");

        long id7 = initiator.createExperimentLabelType("ICPL", 10);
        initiator.createExperimentLabel(id7, K_AMINO_ACID, "ICPL-Lys0");
        initiator.createExperimentLabel(id7, K_AMINO_ACID, "ICPL-Lys4");
        initiator.createExperimentLabel(id7, K_AMINO_ACID, "ICPL-Lys6");
        initiator.createExperimentLabel(id7, K_AMINO_ACID, "ICPL-Lys10");
        initiator.createExperimentLabel(id7, NTER, "ICPL-Nter0");
        initiator.createExperimentLabel(id7, NTER, "ICPL-Nter4");
        initiator.createExperimentLabel(id7, NTER, "ICPL-Nter6");
        initiator.createExperimentLabel(id7, NTER, "ICPL-Nter10");

        long id8 = initiator.createExperimentLabelType("TMT", 10);
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT10plex-Lys126C");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT10plex-Lys127C");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT10plex-Lys127N");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT10plex-Lys128C");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT10plex-Lys128N");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT10plex-Lys129C");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT10plex-Lys129N");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT10plex-Lys130C");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT10plex-Lys130N");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT10plex-Lys131N");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT10plex-Nter126C");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT10plex-Nter127C");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT10plex-Nter127N");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT10plex-Nter128C");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT10plex-Nter128N");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT10plex-Nter129C");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT10plex-Nter129N");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT10plex-Nter130C");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT10plex-Nter130N");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT10plex-Nter131N");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT2plex-Lys126");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT2plex-Lys127");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT2plex-Nter126");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT2plex-Nter127");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT6plex-Lys126");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT6plex-Lys127");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT6plex-Lys128");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT6plex-Lys129");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT6plex-Lys130");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT6plex-Lys131");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT6plex-Nter126");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT6plex-Nter127");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT6plex-Nter128");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT6plex-Nter129");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT6plex-Nter130");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT6plex-Nter131");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT8plex-Lys126C");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT8plex-Lys127C");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT8plex-Lys127N");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT8plex-Lys128C");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT8plex-Lys129C");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT8plex-Lys129N");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT8plex-Lys130C");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT8plex-Lys131N");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT8plex-Nter126C");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT8plex-Nter127C");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT8plex-Nter127N");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT8plex-Nter128C");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT8plex-Nter129C");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT8plex-Nter129N");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT8plex-Nter130C");
        initiator.createExperimentLabel(id8, EMPTY_AMINO_ACID, "TMT8plex-Nter131N");

        long id9 = initiator.createExperimentLabelType("iodoTMT", 6);
        initiator.createExperimentLabel(id9, EMPTY_AMINO_ACID, "iodoTMT6plex-Cys126");
        initiator.createExperimentLabel(id9, EMPTY_AMINO_ACID, "iodoTMT6plex-Cys127");
        initiator.createExperimentLabel(id9, EMPTY_AMINO_ACID, "iodoTMT6plex-Cys128");
        initiator.createExperimentLabel(id9, EMPTY_AMINO_ACID, "iodoTMT6plex-Cys129");
        initiator.createExperimentLabel(id9, EMPTY_AMINO_ACID, "iodoTMT6plex-Cys130");
        initiator.createExperimentLabel(id9, EMPTY_AMINO_ACID, "iodoTMT6plex-Cys131");
    }
}
