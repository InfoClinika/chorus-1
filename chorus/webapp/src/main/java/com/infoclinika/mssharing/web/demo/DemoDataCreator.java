package com.infoclinika.mssharing.web.demo;

import com.amazonaws.services.s3.AmazonS3;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.infoclinika.analysis.storage.cloud.CloudStorageItemReference;
import com.infoclinika.analysis.storage.cloud.CloudStorageService;
import com.infoclinika.mssharing.model.DemoDataCreatorHelper;
import com.infoclinika.mssharing.model.Notifier;
import com.infoclinika.mssharing.model.PredefinedDataCreator;
import com.infoclinika.mssharing.model.api.*;
import com.infoclinika.mssharing.model.helper.*;
import com.infoclinika.mssharing.model.internal.cloud.CloudStorageClientsProvider;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.write.*;
import com.infoclinika.mssharing.model.write.billing.BillingManagement;
import com.infoclinika.mssharing.platform.model.InstrumentsDefaults;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.common.items.FileExtensionItem;
import com.infoclinika.mssharing.platform.model.helper.InstrumentCreationHelperTemplate;
import com.infoclinika.mssharing.platform.model.helper.InstrumentCreationHelperTemplate.PotentialOperator;
import com.infoclinika.mssharing.platform.model.read.LabReaderTemplate;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate.MetaFactorTemplate;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate.Restriction;
import com.infoclinika.mssharing.platform.model.write.LabManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.SharingManagementTemplate;
import com.infoclinika.mssharing.propertiesprovider.AmazonPropertiesProvider;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingChargeType;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.infoclinika.mssharing.model.helper.ExperimentSampleTypeItem.LIGHT;
import static com.infoclinika.mssharing.model.write.InstrumentManagement.MSFunctionDTO;
import static com.infoclinika.mssharing.services.billing.rest.api.model.BillingChargeType.PER_GB;

/**
 * @author Pavel Kaplin
 */
@Component
@Singleton
@DependsOn("seedDataCreator")
public class DemoDataCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoDataCreator.class);

    public static final String MAIN_ACTOR_EMAIL = "demo.user@infoclinika.com";
    public static final String PASSWORD = "pwd";
    public static final String AB_FIRST_NAME = "Andrey";
    public static final String AB_LAST_NAME = "Bondarenko";
    public static final String AB_EMAIL = "abondarenko@celgene.com";
    public static final String MT_FIRST_NAME = "Matthew";
    public static final String MT_LAST_NAME = "Trotter";
    public static final String MT_EMAIL = "mtrotter@celgene.com";
    private static final Function<String, String> SAMPLE_RAW_FILE_1_FN = namePrefix -> String.format(
        "raw-files/sample-data/%s",
        namePrefix
    );
    private static final Function<String, String> SAMPLE_RAW_FILE_2_FN = input -> String.format(
        "raw-files/sample-data/%s",
        input
    );
    private static final Function<String, String> SAMPLE_RAW_FILE_3_FN = namePrefix -> String.format(
        "raw-files/sample-data/%s",
        namePrefix
    );
    private static final Function<String, String> SAMPLE_RAW_FILE_4_FN = namePrefix -> String.format(
        "raw-files/sample-data/%s",
        namePrefix
    );
    private static final Function<String, String> SAMPLE_RAW_FILE_5_FN = namePrefix -> String.format(
        "raw-files/sample-data/%s",
        namePrefix
    );
    private static final Function<String, String> SAMPLE_RAW_FILE_6_FN = namePrefix -> String.format(
        "raw-files/sample-data/%s",
        namePrefix
    );
    private static final int RESOLUTION = 100000;
    private static final int MAX_SCAN = 4440;
    private static final int MAX_PACKETS = 9052;
    private static final HashSet<MSFunctionDTO> DEFAULT_MS_FUNCTIONS = Sets.newHashSet(
        new MSFunctionDTO(
            "FTMS + p NSI Full ms [400.00-1800.00]",
            MSFunctionDataType.PROFILE,
            MSFunctionImageType.INT_IMAGE,
            MSFunctionType.MS,
            MSFunctionScanType.FULL,
            MSFunctionFragmentationType.CAD,
            MSFunctionMassAnalyzerType.FTMS,
            MSResolutionType.HIGH,
            "LTQ FT",
            "LTQ FTSN06106F",
            true,
            true,
            false,
            RESOLUTION,
            0,
            6677,
            96720,
            0,
            Integer.MAX_VALUE
        ),
        new MSFunctionDTO(
            "ITMS + c NSI d Full ms2 445.13@cid35.00 [110.00-460.00]",
            MSFunctionDataType.CENTROID,
            MSFunctionImageType.INT_IMAGE,
            MSFunctionType.MS2,
            MSFunctionScanType.FULL,
            MSFunctionFragmentationType.CAD,
            MSFunctionMassAnalyzerType.ITMS,
            MSResolutionType.LOW,
            "LTQ FT", "LTQ FTSN06106F",
            false,
            true,
            false,
            RESOLUTION,
            0,
            MAX_SCAN,
            MAX_PACKETS,
            0,
            Integer.MAX_VALUE
        )
    );
    private static final String NEXT_GEN_SEQUENCING = "Roche";
    private static final String PERMANENT_SAMPLE_RAW_FILE_S3_1 = "raw-files/permanent-data/50_02.RAW";
    private static final String PERMANENT_SAMPLE_RAW_FILE_S3_2 = "raw-files/permanent-data/50_03.RAW";
    private static final String PERMANENT_SAMPLE_RAW_FILE_S3_3 = "raw-files/permanent-data/50f_01.RAW";
    private static final String PERMANENT_SAMPLE_RAW_FILE_S3_4 = "raw-files/permanent-data/500f_01.RAW";
    private static final String PERMANENT_SAMPLE_RAW_FILE_S3_5 = "raw-files/permanent-data/500f_02.RAW";
    private static final String PERMANENT_SAMPLE_RAW_FILE_S3_6 = "raw-files/permanent-data/500f_03.RAW";
    private static final String THERMO_DIA_MS1 =
        "skyline-translated/translated-per-file/20130311_DIA_Pit01.raw/FTMS + p NSI Full ms [500.00-900.00]";
    private static final String THERMO_DIA_MS2_1 =
        "skyline-translated/translated-per-file/20130311_DIA_Pit01.raw/FTMS + p NSI Full ms2 510.48@hcd20.00 [72" +
            ".00-1080.00]";
    private static final String THERMO_DIA_MS2_2 =
        "skyline-translated/translated-per-file/20130311_DIA_Pit01.raw/FTMS + p NSI Full ms2 530.49@hcd20.00 [74" +
            ".67-1120.00]";
    private static final String THERMO_DIA_MS2_3 =
        "skyline-translated/translated-per-file/20130311_DIA_Pit01.raw/FTMS + p NSI Full ms2 550.50@hcd20.00 [77" +
            ".33-1160.00]";
    private static final String THERMO_DIA_MS2_4 =
        "skyline-translated/translated-per-file/20130311_DIA_Pit01.raw/FTMS + p NSI Full ms2 570.51@hcd20.00 [80" +
            ".00-1200.00]";
    private static final String THERMO_DIA_MS2_5 =
        "skyline-translated/translated-per-file/20130311_DIA_Pit01.raw/FTMS + p NSI Full ms2 590.52@hcd20.00 [83" +
            ".00-1245.00]";
    private static final String THERMO_DIA_MS2_6 =
        "skyline-translated/translated-per-file/20130311_DIA_Pit01.raw/FTMS + p NSI Full ms2 610.53@hcd20.00 [85" +
            ".67-1285.00]";
    private static final String THERMO_DIA_MS2_7 =
        "skyline-translated/translated-per-file/20130311_DIA_Pit01.raw/FTMS + p NSI Full ms2 630.54@hcd20.00 [88" +
            ".33-1325.00]";
    private static final String THERMO_DIA_MS2_8 =
        "skyline-translated/translated-per-file/20130311_DIA_Pit01.raw/FTMS + p NSI Full ms2 650.55@hcd20.00 [91" +
            ".00-1365.00]";
    private static final String THERMO_DIA_MS2_9 =
        "skyline-translated/translated-per-file/20130311_DIA_Pit01.raw/FTMS + p NSI Full ms2 670.55@hcd20.00 [93" +
            ".67-1405.00]";
    private static final String THERMO_DIA_MS2_10 =
        "skyline-translated/translated-per-file/20130311_DIA_Pit01.raw/FTMS + p NSI Full ms2 690.56@hcd20.00 [96" +
            ".33-1445.00]";
    private static final String THERMO_DIA_MS2_11 =
        "skyline-translated/translated-per-file/20130311_DIA_Pit01.raw/FTMS + p NSI Full ms2 710.57@hcd20.00 [99" +
            ".00-1485.00]";
    private static final String THERMO_DIA_MS2_12 =
        "skyline-translated/translated-per-file/20130311_DIA_Pit01.raw/FTMS + p NSI Full ms2 730.58@hcd20.00 [102" +
            ".00-1530.00]";
    private static final String THERMO_DIA_MS2_13 =
        "skyline-translated/translated-per-file/20130311_DIA_Pit01.raw/FTMS + p NSI Full ms2 750.59@hcd20.00 [104" +
            ".67-1570.00]";
    private static final String THERMO_DIA_MS2_14 =
        "skyline-translated/translated-per-file/20130311_DIA_Pit01.raw/FTMS + p NSI Full ms2 770.60@hcd20.00 [107" +
            ".33-1610.00]";
    private static final String THERMO_DIA_MS2_15 =
        "skyline-translated/translated-per-file/20130311_DIA_Pit01.raw/FTMS + p NSI Full ms2 790.61@hcd20.00 [110" +
            ".00-1650.00]";
    private static final String THERMO_DIA_MS2_16 =
        "skyline-translated/translated-per-file/20130311_DIA_Pit01.raw/FTMS + p NSI Full ms2 810.62@hcd20.00 [112" +
            ".67-1690.00]";
    private static final String THERMO_DIA_MS2_17 =
        "skyline-translated/translated-per-file/20130311_DIA_Pit01.raw/FTMS + p NSI Full ms2 830.63@hcd20.00 [115" +
            ".33-1730.00]";
    private static final String THERMO_DIA_MS2_18 =
        "skyline-translated/translated-per-file/20130311_DIA_Pit01.raw/FTMS + p NSI Full ms2 850.64@hcd20.00 [118" +
            ".33-1775.00]";
    private static final String THERMO_DIA_MS2_19 =
        "skyline-translated/translated-per-file/20130311_DIA_Pit01.raw/FTMS + p NSI Full ms2 870.65@hcd20.00 [121" +
            ".00-1815.00]";
    private static final String THERMO_DIA_MS2_20 =
        "skyline-translated/translated-per-file/20130311_DIA_Pit01.raw/FTMS + p NSI Full ms2 890.65@hcd20.00 [123" +
            ".67-1855.00]";
    private static final String MS_STUDY_TYPE = "Mass Spectrometry";
    private static final String NEWS_INTRODUCTION_1 =
        "Stratus Biosciences releases beta version of the Chorus Project, a community based solution for the storage," +
            " organization, analysis and sharing of mass spectrometry data.";
    private static final String NEWS_BODY_1 =
        "Stratus Biosciences to Introduce The CHORUS Project - A Community Based Solution for the Storage, Analysis, " +
            "and Exchange of Mass Spectrometry Data and Information at ASMS 2013\n" +
            "\n" +
            "MINNEAPOLIS, MN/June 13, 13 — Stratus Biosciences, Inc., provider of cloud-based solutions for mass " +
            "spectral data analysis and storage, will be introducing their CHORUS Project at the 61st Annual American" +
            " Society for Mass Spectrometry and Allied Topics Conference on June 9 −13, 2013 in Minneapolis, MN.\n" +
            "\n" +
            "The CHORUS Project is a new, highly integrated cloud application that provides the ability to securely " +
            "store, analyze and share mass spectral data regardless of the original raw file format. CHORUS is " +
            "operated under a not-for-profit public/private partnership.\n" +
            "\n" +
            "CHORUS provides custom built open-source data analysis tools that enable fast processing of large data " +
            "sets with parallel and distributed algorithms on the cloud. The initial data analysis tools include a " +
            "chromatographic and mass spectral viewers, as well as a database search engine for protein sequence " +
            "identification.\n" +
            "\n" +
            "While CHORUS’s capabilities have been initially designed to meet the data-intense needs of the " +
            "proteomics community, CHORUS’s chromatographic and spectral analysis tools can be used by anyone " +
            "conducting mass spectrometric analyses.\n" +
            "\n" +
            "A distinguishing feature of the CHORUS Project is that data can be kept entirely private, shared with " +
            "select groups of reviewers, or placed in the public domain. Many of the data sharing concepts that were " +
            "used to develop CHORUS are based on the academic research process where investigators: 1) privately " +
            "analyze data with a small number of co-investigators; 2) share these findings with a limited number of " +
            "editors and reviewers; and 3) publish the final work making the results and data available to the " +
            "scientific community and the public.\n" +
            "\n" +
            "The goal of the CHORUS Project is to advance science by allowing mass spectrometrists to use and " +
            "contribute to a scalable and sustainable digital repository that is designed to store the world’s mass " +
            "spectrometric data. All of the public data are freely accessible.\n" +
            "\n" +
            "To help fulfill this goal, Stratus Biosciences has established an external Advisory Board. Mike Lee, " +
            "President of Milestone Development Services, Chris Tirpak, Vice President of Information Technology at " +
            "Echostar, and Ruedi Aebersold, Professor at the Institute of Molecular Systems Biology, are members of " +
            "the Board and Arthur Moseley, Associate Research Professor at Duke University and Director of the Duke " +
            "Proteomics Core Facility is now a CHORUS Project collaborator.\n" +
            "\n" +
            "“It is very important to note that the CHORUS Project is not intended to make money, only cover our " +
            "costs,” states Michael MacCoss, University of Washington Associate Professor. “Money that is raised to " +
            "support the CHORUS Project will be used to expand the capabilities and further reduce the costs to the " +
            "users.”\n" +
            "\n" +
            "“Progress has been fantastic and the software engineering team we have assembled is phenomenal,” states " +
            "Andrey Bondarenko, President of InfoClinika. The CHORUS Project has already been beta tested. “In five " +
            "short months, we have built a scalable system that is operating on the cloud and managing data from " +
            "multiple institutions around the world.”\n" +
            "\n" +
            "Future plans for CHORUS include the implementation of easy-to-use, open-source proteomic database " +
            "searching tools, label-free differential mass spectrometry capabilities, and the incorporation of " +
            "powerful third party tools such as “Preview” and “Bionic™” from Protein Metrics.\n" +
            "\n" +
            "“The opportunities for distributed cloud based analysis of mass spectrometry data are tremendously " +
            "exciting,” states Nathan Yates, Associate Professor at the University of Pittsburgh. Stratus Biosciences" +
            " believes that raw mass spectrometry data holds an ocean of unstructured information that will only be " +
            "revealed as scientists develop new approaches and technologies for analyzing the data. “Re-analysis of " +
            "raw data on the cloud is fundamental to this exploration and CHORUS is the first solution that enables " +
            "storage, preservation, and analysis of every single bit of data.”\n" +
            "\n" +
            "Stratus Biosciences will be introducing the CHORUS Project and its capabilities at this year’s ASMS " +
            "conference in Minneapolis in a Workshop at 5:30 p.m. Wednesday June 12, 2013. During the Workshop, " +
            "participants can access CHORUS on their laptops and mobile devices and provide feedback on what they " +
            "feel are the essential features of this community platform.\n" +
            "\n" +
            "In addition, Stratus Bioscience’s partner Agilent Technologies, Inc. will host a terminal featuring " +
            "CHORUS in their 2013 ASMS hospitality suite.";
    private static final String NEWS_INTRODUCTION_2 =
        "Stratus Biosciences will be introducing the CHORUS Project and its capabilities at this year’s ASMS " +
            "conference in Minneapolis in a Workshop at 5:30 p.m. Wednesday June 12, 2013. Participants will be able " +
            "to access CHORUS on their laptops and mobile devices and provide feedback on what they feel are the " +
            "essential features of such a community platform.";
    private static final String NEWS_BODY_2 =
        "Stratus Biosciences will be introducing the CHORUS Project and its capabilities at this year’s ASMS " +
            "conference in Minneapolis in a Workshop at 5:30 p.m. Wednesday June 12, 2013. Participants will be able " +
            "to access CHORUS on their laptops and mobile devices and provide feedback on what they feel are the " +
            "essential features of such a community platform. In addition, Stratus Bioscience’s partner Agilent " +
            "Technologies, Inc. will host a terminal featuring CHORUS in their 2013 ASMS hospitality suite.";
    private static final int RAW_FILE_1_SIZE = 63981338;
    private static final int RAW_FILE_2_SIZE = 64273726;
    private static final int RAW_FILE_3_SIZE = 63034614;
    private static final int RAW_FILE_4_SIZE = 63495560;
    private static final int RAW_FILE_5_SIZE = 63611244;
    private static final int RAW_FILE_6_SIZE = 62996602;
    private static final String MY_PROJECT = "My Project";
    private static final String LIVER = "liver";
    private static final String HEART = "heart";
    private static boolean isInit;

    //For tests to skip additional checks
    private boolean testMode = false;

    private AmazonS3 s3client;

    @Inject
    private PredefinedDataCreator initiator;

    @Inject
    private StudyManagement studyManagement;

    @Inject
    private SharingManagement sharingManagement;

    @Inject
    private InstrumentManagement instrumentManagement;

    @Inject
    private UserManagement userManagement;

    @Inject
    private LabManagement labManagement;

    @Inject
    private PasswordEncoder passwordEncoder;

    @Inject
    private InstrumentCreationHelperTemplate<PotentialOperator> instrumentCreationHelper;

    @Inject
    private ExperimentCreationHelper experimentCreationHelper;

    @Inject
    private Notifier notifier;

    @Inject
    private SecurityHelper securityHelper;

    @Inject
    private StoredObjectPaths storedObjectPaths;

    @Inject
    private FileMetaInfoHelper fileMetaInfoHelper;

    @Inject
    private NewsManagement newsManagement;

    @Inject
    private BillingManagement billingManagement;

    @Inject
    private DashboardReader dashboardReader;

    @Inject
    private FeaturesHelper featuresHelper;

    @Inject
    private SeedDataCreator seedDataCreator;

    @Inject
    private DemoDataCreatorHelper demoDataCreatorHelper;

    @Inject
    private CloudDataInitializer cloudDataInitializer;

    @Inject
    private DemoDataPropertiesProvider propertiesProvider;

    @Inject
    private CloudStorageClientsProvider cloudStorageClientsProvider;

    @Inject
    private AmazonPropertiesProvider amazonPropertiesProvider;

    public DemoDataCreator() {
    }

    public DemoDataCreator(boolean testMode) {
        this.testMode = testMode;
    }

    private static ExperimentPreparedSampleItem sampleWithFactors(long id, List<String> factorValues) {
        final ImmutableSet<ExperimentSampleItem> bioSamples =
            ImmutableSet.of(new ExperimentSampleItem(String.valueOf(id), LIGHT, factorValues));
        return new ExperimentPreparedSampleItem(String.valueOf(id), bioSamples);
    }

    private static ExperimentPreparedSampleItem sampleWithoutFactors(long id) {
        final ImmutableSet<ExperimentSampleItem> bioSamples =
            ImmutableSet.of(new ExperimentSampleItem(String.valueOf(id), LIGHT, newArrayList()));
        return new ExperimentPreparedSampleItem(String.valueOf(id), bioSamples);
    }

    @PostConstruct
    public void postConstruct() {
        if (isInit) {
            LOGGER.warn("Initialization goes second time");
            return;
        }

        isInit = true;

        seedDataCreator.createSeedData();
        // additional configuration is needed for open-chorus
        // cloudDataInitializer.initializeCloudData();
        createDemoData();

        demoDataCreatorHelper.markDemoDataCreationAsComplete();
    }

    private void createDemoData() {
        final long admin = createAdmin();

        if (!propertiesProvider.isCreateDemoData()) {
            LOGGER.info("Administrator with " + propertiesProvider.getAdminEmail() +
                            " has been created. Skipping demo data creation.");
            return;
        }

        createHelsinkiDemoData();

        s3client = cloudStorageClientsProvider.getAmazonS3Client();
        notifier.setEnabled(false);

        initiator.billingProperties();

        long archiveStorage = billingManagement.createChargeableItem(10, BillingFeature.ARCHIVE_STORAGE, 1, PER_GB);
        long analyzeStorage = billingManagement.createChargeableItem(90, BillingFeature.ANALYSE_STORAGE, 1, PER_GB);
        long download = billingManagement.createChargeableItem(0, BillingFeature.DOWNLOAD, 1, PER_GB);
        long publicDownload = billingManagement.createChargeableItem(0, BillingFeature.PUBLIC_DOWNLOAD, 1, PER_GB);
        long storageVolumes =
            billingManagement.createChargeableItem(4000, BillingFeature.STORAGE_VOLUMES, 1, BillingChargeType.PER_GB);
        long archiveStorageVolumes = billingManagement.createChargeableItem(
            4000,
            BillingFeature.ARCHIVE_STORAGE_VOLUMES,
            1,
            BillingChargeType.PER_GB
        );

        long lab = createLab(admin, "First Chorus Lab Very Long Name For Testing Ellipsize");
        long lab2 = createLab(admin, "Second Chorus Lab Very Long Name For Testing Ellipsize");
        long demoUser = createActor(lab, "Demo", "User", MAIN_ACTOR_EMAIL);

        long anotherUser = createActor(lab, "John", "Doe", "john.doe@infoclinika.com");

        long testUser = createActor(lab, "Karren", "Koe", "karren.koe@infoclinika.com");

        long anotherTestUser = createActor(lab, "Gene", "Simmons", "gene.simmons@infoclinika.com");

        Long loadTestingProject = createProject(anotherUser, lab, "Load Testing Project", "Chorus load testing", "");
        Long sharedProject = createProject(
            anotherUser,
            lab,
            "Neanderthal Genome Project",
            "Ancient DNA, Human Genome",
            "The Neanderthal genome project is a collaboration of scientists " +
                "coordinated by the " +
                "Max Planck Institute for Evolutionary Anthropology in Germany and 454" +
                " Life Sciences " +
                "in the United States to sequence the Neanderthal genome."
        );

        for (int i = 0; i < 100; i++) {
            createProject(anotherUser, lab, "Test project " + i, "Test Project Research", "");
        }


        sharingManagement.updateSharingPolicy(
            anotherUser,
            sharedProject,
            Collections.singletonMap(demoUser, SharingManagementTemplate.Access.WRITE),
            Collections.<Long, SharingManagementTemplate.Access>emptyMap(),
            false
        );

        final long model = someInstrumentModel();
        Optional<Long> instrument =
            createInstrumentAndApproveIfNeeded(demoUser, lab, "Genome Machine", "ABC123456", "HLPC", "", model);
        createInstrumentAndApproveIfNeeded(
            demoUser,
            lab,
            "CLC Genomics Machine",
            "DD",
            "HLPC",
            "",
            someInstrumentModel()
        );
        final Long cellMachine = createInstrumentAndApproveIfNeeded(
            anotherUser,
            lab,
            "Cell Machine",
            "1ABC123456",
            "HLPC",
            "",
            someInstrumentModel()
        ).get();

        attachFiles(demoUser, instrument.get());
        attachAnotherFiles(anotherUser, cellMachine);

        Long project = createProject(
            demoUser,
            lab,
            "Geno 2.0: The Greatest Journey Ever Told Let's Repeat It Second Time To Check Ellipsizing",
            "Your Story. Our Story. The Human Story.",
            "Since its launch in 2005, National Geographic’s Genographic Project " +
                "has used advanced DNA analysis and worked with indigenous communities to " +
                "help answer fundamental questions about where humans originated and how we came to " +
                "populate the Earth."
        );
        createExperiment(demoUser, project, lab, MY_PROJECT);

        for (int i = 0; i < 100; i++) {
            createExperiment(anotherUser, loadTestingProject, lab, MY_PROJECT, "Experiment " + i);
        }

        Long publicProject = createSeveralPublicProjects(anotherUser, lab);
        createExperiment(anotherUser, publicProject, lab, "Public Project");
        createExperiment(anotherUser, sharedProject, lab, "Shared Project");

        sharingManagement.createGroup(demoUser, "First Group", ImmutableSet.of(anotherUser));
        sharingManagement.createGroup(demoUser, "Second Group", ImmutableSet.of(anotherUser));
        sharingManagement.createGroup(demoUser, "Third Group", ImmutableSet.of(anotherUser));

        createDataForAndreyDemo(admin);
        notifier.setEnabled(true);

        //creating NextGen instrumen
        final long nextGenInstrumentModel = nextGenInstrumentModel();
        createInstrumentAndApproveIfNeeded(
            demoUser,
            lab,
            NEXT_GEN_SEQUENCING + " instrument",
            "NGS0001",
            "HLPC",
            "",
            nextGenInstrumentModel
        );
    }

    private void createHelsinkiDemoData() {
        final long labHead =
            initiator.admin(AB_FIRST_NAME, AB_LAST_NAME, AB_EMAIL, propertiesProvider.getAdminPassword());
        final long labId = createHelsinkiLab(labHead);

        initiator.admin(
            MT_FIRST_NAME,
            MT_LAST_NAME,
            MT_EMAIL,
            propertiesProvider.getAdminPassword(),
            newHashSet(labId)
        );

        initiator.createInstrumentWithModel(labHead, labId, VendorNames.ILLUMINA_VENDOR, "NextSeq 500");
        initiator.createInstrumentWithModel(labHead, labId, VendorNames.ILLUMINA_VENDOR, "NextSeq 550");
        initiator.createInstrumentWithModel(labHead, labId, VendorNames.ILLUMINA_VENDOR, "HiSeq 2500");
        initiator.createInstrumentWithModel(labHead, labId, VendorNames.ILLUMINA_VENDOR, "HiSeq 3000");
        initiator.createInstrumentWithModel(labHead, labId, VendorNames.ILLUMINA_VENDOR, "HiSeq 4000");
        initiator.createInstrumentWithModel(labHead, labId, VendorNames.ILLUMINA_VENDOR, "HiSeq X");
        initiator.createInstrumentWithModel(labHead, labId, VendorNames.ILLUMINA_VENDOR, "NovaSeq 5000");
        initiator.createInstrumentWithModel(labHead, labId, VendorNames.ILLUMINA_VENDOR, "NovaSeq 6000");
        initiator.createInstrumentWithModel(labHead, labId, VendorNames.ILLUMINA_VENDOR, "MiSeqDx");
        initiator.createInstrumentWithModel(labHead, labId, VendorNames.ILLUMINA_VENDOR, "MiSeq FGx");
        initiator.createInstrumentWithModel(labHead, labId, VendorNames.ILLUMINA_VENDOR, "MiniSeq");

        initiator.createInstrument(labHead, labId, InstrumentsDefaults.CL_TECHNOLOGY_TYPE, VendorNames.DEFAULT_VENDOR);
    }

    private void copyFileOnS3IfNeeded(String fileName, String permanentFileName) {
        final CloudStorageService cloudStorageService = cloudStorageClientsProvider.getCloudStorageService();
        final String activeBucket = amazonPropertiesProvider.getActiveBucket();
        final CloudStorageItemReference s3Path = new CloudStorageItemReference(activeBucket, fileName);
        int retries = 0;
        final int maxRetriesCount = 10;
        try {
            while (!cloudStorageService.existsAtCloud(s3Path) && retries++ < maxRetriesCount) {
                LOGGER.info(
                    "Copy file from '" + activeBucket + "|" + permanentFileName + "' to '" +
                        s3Path.asDelimitedPath() + "'..."
                );
                s3client.copyObject(activeBucket, permanentFileName, activeBucket, fileName);
            }
        } catch (RuntimeException ex) {
            LOGGER.debug(ex.getMessage(), ex);
        }
    }

    private void attachAnotherFiles(long user, Long instrument) {
        final int[] fileSizes =
            {RAW_FILE_1_SIZE, RAW_FILE_2_SIZE, RAW_FILE_3_SIZE, RAW_FILE_4_SIZE, RAW_FILE_5_SIZE, RAW_FILE_6_SIZE};
        final List<Function<String, String>> fileNames = sampleFileNames();
        final String[] permanentFileNames = permanentSampleFileNames();

        attachFile(
            user,
            instrument,
            "c15092005_007.RAW",
            RAW_FILE_1_SIZE,
            anySpecie(),
            "mscloud, charts, gaucher",
            SAMPLE_RAW_FILE_1_FN.apply("c15092005_007.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_1
        );
        attachFile(
            user,
            instrument,
            "c15092005_008.RAW",
            RAW_FILE_2_SIZE,
            anySpecie(),
            "cloud, demo, gaucher",
            SAMPLE_RAW_FILE_2_FN.apply("c15092005_008.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_2
        );
        attachFile(
            user,
            instrument,
            "c15092005_009.RAW",
            RAW_FILE_3_SIZE,
            anySpecie(),
            "cloud, demo, gaucher",
            SAMPLE_RAW_FILE_3_FN.apply("c15092005_009.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_3
        );
        attachFile(
            user,
            instrument,
            "c15092005_010.RAW",
            RAW_FILE_4_SIZE,
            anySpecie(),
            "cloud, demo, gaucher",
            SAMPLE_RAW_FILE_4_FN.apply("c15092005_010.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_4
        );
        attachFile(
            user,
            instrument,
            "c15092005_011.RAW",
            RAW_FILE_5_SIZE,
            anySpecie(),
            "cloud, demo, gaucher",
            SAMPLE_RAW_FILE_5_FN.apply("c15092005_011.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_5
        );

        for (int i = 0; i < 100; i++) {
            final int index = i % 6;
            final String contentId = fileNames.get(index).apply("load-testing-" + i + ".RAW");
            attachFile(
                user,
                instrument,
                "load-testing-" + i + ".RAW",
                fileSizes[index],
                anySpecie(),
                "load testing",
                contentId,
                permanentFileNames[index]
            );
        }
    }

    private List<Function<String, String>> sampleFileNames() {
        return ImmutableList.of(
            SAMPLE_RAW_FILE_1_FN,
            SAMPLE_RAW_FILE_2_FN,
            SAMPLE_RAW_FILE_3_FN,
            SAMPLE_RAW_FILE_4_FN,
            SAMPLE_RAW_FILE_5_FN,
            SAMPLE_RAW_FILE_6_FN
        );
    }

    private String[] permanentSampleFileNames() {
        return new String[] {PERMANENT_SAMPLE_RAW_FILE_S3_1, PERMANENT_SAMPLE_RAW_FILE_S3_2,
            PERMANENT_SAMPLE_RAW_FILE_S3_3, PERMANENT_SAMPLE_RAW_FILE_S3_4, PERMANENT_SAMPLE_RAW_FILE_S3_5,
            PERMANENT_SAMPLE_RAW_FILE_S3_6};
    }

    private Optional<Long> createInstrumentAndApproveIfNeeded(
        long user,
        long lab,
        String instrumentName,
        String serialNumber,
        String hplc,
        String peripherals,
        long model
    ) {

        final boolean labHead = labManagement.isLabHead(user, lab);
        final InstrumentDetails details = new InstrumentDetails(
            instrumentName,
            serialNumber,
            peripherals,
            hplc,
            Collections.<LockMzItem>emptyList()
        );

        if (labHead) {
            return Optional.of(instrumentManagement.createInstrument(user, lab, model, details));
        } else {
            final Optional<Long> instrumentRequest =
                instrumentManagement.newInstrumentRequest(user, lab, model, details);
            final LabReaderTemplate.LabLineTemplate labLine = dashboardReader.readLab(lab);
            return Optional.of(instrumentManagement.approveInstrumentCreation(
                labLine.labHead,
                instrumentRequest.get()
            ));
        }

    }

    private void createDataForAndreyDemo(long admin) {
        final UserManagement.PersonInfo mikeHead =
            new UserManagement.PersonInfo("Michael", "MacCoss", "maccoss@uw.edu");
        final UserManagement.PersonInfo nateHead = new UserManagement.PersonInfo("Nathan", "Yates", "yatesn@upmc.edu");
        final UserManagement.PersonInfo andreyHead =
            new UserManagement.PersonInfo("Andrey", "Bondarenko", "abond380@comcast.net");

        final String[] permanentFileNames = permanentSampleFileNames();
        final Random randomizer = new Random();
        final int randomMaxIndex = permanentFileNames.length - 1;
        final long uwLab =
            createLab(admin, "maccoss@uw.edu", "http://www.washington.edu/", "University of Washington", mikeHead);
        final long upLab =
            createLab(admin, "yatesn@upmc.edu", "http://pitt.edu/", "University of Pittsburgh", nateHead);
        final long infoLab =
            createLab(admin, "abond380@comcast.net", "http://www.infoclinika.com/", "InfoClinika", andreyHead);
        final long mike = createUserAndVerifyEmail(mikeHead, uwLab);
        final long nate = createUserAndVerifyEmail(nateHead, upLab);
        final long andrey = createUserAndVerifyEmail(andreyHead, infoLab);

        final long ftmsModel = initiator.instrumentModel(
            "Thermo Scientific", "FTMS", MS_STUDY_TYPE, "FTMS",
            false, false,
            newHashSet(new FileExtensionItem(
                ".raw",
                "",
                Collections.emptyMap()
            ))
        );
        final long lqtotModel = initiator.instrumentModel(
            "Thermo Scientific",
            "LTQ-OT",
            MS_STUDY_TYPE,
            "LTQ-OT",
            false,
            false,
            newHashSet(new FileExtensionItem(
                ".raw",
                "",
                Collections.emptyMap()
            ))
        );

        final long ftmsInstrument =
            createInstrumentAndApproveIfNeeded(andrey, infoLab, "FTMS - 01", "1", "", "", ftmsModel).get();
        final long lqtotInstrument =
            createInstrumentAndApproveIfNeeded(andrey, infoLab, "LTQ-OT - 01", "2", "", "", lqtotModel).get();

        final long specie = anySpecie();

        //liver
        final long liver1 = attachFile(
            andrey,
            ftmsInstrument,
            "101208_liver_mito_runon_2.RAW",
            RAW_FILE_1_SIZE,
            specie,
            "liver, 3 days, mouse, protein kinetics",
            SAMPLE_RAW_FILE_1_FN.apply("101208_liver_mito_runon_2.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_1
        );

        final long liver2 = attachFile(
            andrey,
            ftmsInstrument,
            "101208_liver_mito_runon_3.RAW",
            RAW_FILE_2_SIZE,
            specie,
            "liver, 3 days, mouse, protein kinetics",
            SAMPLE_RAW_FILE_2_FN.apply("101208_liver_mito_runon_3.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_2
        );
        final long liver3 = attachFile(
            andrey,
            ftmsInstrument,
            "101208_liver_mito_runon_4.RAW",
            RAW_FILE_3_SIZE,
            specie,
            "liver, 3 days, mouse, protein kinetics",
            SAMPLE_RAW_FILE_3_FN.apply("101208_liver_mito_runon_4.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_3
        );

        final long liver4 = attachFile(
            andrey,
            ftmsInstrument,
            "101208_liver_mito_runon_16.RAW",
            RAW_FILE_4_SIZE,
            specie,
            "liver, 6 days, mouse, protein kinetics",
            SAMPLE_RAW_FILE_4_FN.apply("101208_liver_mito_runon_16.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_4
        );
        final long liver5 = attachFile(
            andrey,
            ftmsInstrument,
            "101208_liver_mito_runon_17.RAW",
            RAW_FILE_5_SIZE,
            specie,
            "liver, 6 days, mouse, protein kinetics",
            SAMPLE_RAW_FILE_5_FN.apply("101208_liver_mito_runon_17.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_5
        );

        final long liver6 = attachFile(
            andrey,
            ftmsInstrument,
            "101208_liver_mito_runon_25.RAW",
            RAW_FILE_6_SIZE,
            specie,
            "liver, 10 days, mouse, protein kinetics",
            SAMPLE_RAW_FILE_6_FN.apply("101208_liver_mito_runon_25.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_6
        );
        final long liver7 = attachFile(
            andrey,
            ftmsInstrument,
            "101208_liver_mito_runon_26.RAW",
            RAW_FILE_1_SIZE,
            specie,
            "liver, 10 days, mouse, protein kinetics",
            SAMPLE_RAW_FILE_1_FN.apply("101208_liver_mito_runon_26.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_1
        );
        final long liver8 = attachFile(
            andrey,
            ftmsInstrument,
            "101208_liver_mito_runon_27.RAW",
            RAW_FILE_2_SIZE,
            specie,
            "liver, 10 days, mouse, protein kinetics",
            SAMPLE_RAW_FILE_2_FN.apply("101208_liver_mito_runon_27.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_2
        );

        final long liver9 = attachFile(
            andrey,
            ftmsInstrument,
            "101208_liver_mito_runon_37.RAW",
            RAW_FILE_3_SIZE,
            specie,
            "liver, 17 days, mouse, protein kinetics",
            SAMPLE_RAW_FILE_3_FN.apply("101208_liver_mito_runon_37.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_3
        );
        final long liver10 = attachFile(
            andrey,
            ftmsInstrument,
            "101208_liver_mito_runon_38.RAW",
            RAW_FILE_4_SIZE,
            specie,
            "liver, 17 days, mouse, protein kinetics",
            SAMPLE_RAW_FILE_4_FN.apply("101208_liver_mito_runon_38.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_4
        );
        final long liver11 = attachFile(
            andrey,
            ftmsInstrument,
            "101208_liver_mito_runon_39.RAW",
            RAW_FILE_5_SIZE,
            specie,
            "liver, 17 days, mouse, protein kinetics",
            SAMPLE_RAW_FILE_5_FN.apply("101208_liver_mito_runon_39.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_5
        );

        //heart
        final long heart1 = attachFile(
            andrey,
            ftmsInstrument,
            "110101_heart_mito_runon_2.RAW",
            RAW_FILE_1_SIZE,
            specie,
            "heart, 3 days, mouse, protein kinetics",
            SAMPLE_RAW_FILE_1_FN.apply("110101_heart_mito_runon_2.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_1
        );
        final long heart2 = attachFile(
            andrey,
            ftmsInstrument,
            "110101_heart_mito_runon_3.RAW",
            RAW_FILE_2_SIZE,
            specie,
            "heart, 3 days, mouse, protein kinetics",
            SAMPLE_RAW_FILE_2_FN.apply("110101_heart_mito_runon_3.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_2
        );
        final long heart3 = attachFile(
            andrey,
            ftmsInstrument,
            "110101_heart_mito_runon_4.RAW",
            RAW_FILE_3_SIZE,
            specie,
            "heart, 3 days, mouse, protein kinetics",
            SAMPLE_RAW_FILE_3_FN.apply("110101_heart_mito_runon_4.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_3
        );

        final long heart4 = attachFile(
            andrey,
            ftmsInstrument,
            "110101_heart_mito_runon_16.RAW",
            RAW_FILE_4_SIZE,
            specie,
            "heart, 6 days, mouse, protein kinetics",
            SAMPLE_RAW_FILE_4_FN.apply("110101_heart_mito_runon_16.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_4
        );
        final long heart5 = attachFile(
            andrey,
            ftmsInstrument,
            "110101_heart_mito_runon_17.RAW",
            RAW_FILE_5_SIZE,
            specie,
            "heart, 6 days, mouse, protein kinetics",
            SAMPLE_RAW_FILE_5_FN.apply("110101_heart_mito_runon_17.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_5
        );

        final long heart6 = attachFile(
            andrey,
            ftmsInstrument,
            "110101_heart_mito_runon_25.RAW",
            RAW_FILE_6_SIZE,
            specie,
            "heart, 10 days, mouse, protein kinetics",
            SAMPLE_RAW_FILE_6_FN.apply("110101_heart_mito_runon_25.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_6
        );
        final long heart7 = attachFile(
            andrey,
            ftmsInstrument,
            "110101_heart_mito_runon_26.RAW",
            RAW_FILE_1_SIZE,
            specie,
            "heart, 10 days, mouse, protein kinetics",
            SAMPLE_RAW_FILE_1_FN.apply("110101_heart_mito_runon_26.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_1
        );
        final long heart8 = attachFile(
            andrey,
            ftmsInstrument,
            "110101_heart_mito_runon_27.RAW",
            RAW_FILE_2_SIZE,
            specie,
            "heart, 10 days, mouse, protein kinetics",
            SAMPLE_RAW_FILE_2_FN.apply("110101_heart_mito_runon_27.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_2
        );

        final long heart9 = attachFile(
            andrey,
            ftmsInstrument,
            "110101_heart_mito_runon_37.RAW",
            RAW_FILE_3_SIZE,
            specie,
            "heart, 17 days, mouse, protein kinetics",
            SAMPLE_RAW_FILE_3_FN.apply("110101_heart_mito_runon_37.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_3
        );
        final long heart10 = attachFile(
            andrey,
            ftmsInstrument,
            "110101_heart_mito_runon_38.RAW",
            RAW_FILE_4_SIZE,
            specie,
            "heart, 17 days, mouse, protein kinetics",
            SAMPLE_RAW_FILE_4_FN.apply("110101_heart_mito_runon_38.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_4
        );
        final long heart11 = attachFile(
            andrey,
            ftmsInstrument,
            "110101_heart_mito_runon_39.RAW",
            RAW_FILE_5_SIZE,
            specie,
            "heart, 17 days, mouse, protein kinetics",
            SAMPLE_RAW_FILE_5_FN.apply("110101_heart_mito_runon_39.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_5
        );

        final long proKineticsProject =
            createProject(andrey, infoLab, "Protein Kinetics in Mouse", "Protein Kinetics", "");

        //liver experiment
        liverExperiment(
            andrey,
            infoLab,
            ftmsModel,
            ftmsInstrument,
            specie,
            liver1,
            liver2,
            liver3,
            liver4,
            liver5,
            liver6,
            liver7,
            liver8,
            liver9,
            liver10,
            liver11,
            proKineticsProject
        );

        //heart experiment
        heartExperiment(
            andrey,
            infoLab,
            ftmsModel,
            ftmsInstrument,
            specie,
            heart1,
            heart2,
            heart3,
            heart4,
            heart5,
            heart6,
            heart7,
            heart8,
            heart9,
            heart10,
            heart11,
            proKineticsProject
        );

        //combined experiment
        combinedExperiment(
            andrey,
            infoLab,
            ftmsModel,
            ftmsInstrument,
            specie,
            liver1,
            liver2,
            liver3,
            liver4,
            liver5,
            liver6,
            liver7,
            liver8,
            liver9,
            liver10,
            liver11,
            heart1,
            heart2,
            heart3,
            heart4,
            heart5,
            heart6,
            heart7,
            heart8,
            heart9,
            heart10,
            heart11,
            proKineticsProject
        );

        final SimpleDateFormat format = new SimpleDateFormat("MMM:dd:yyyy");
        try {
            newsManagement.createNews(
                admin,
                new NewsManagement.NewsInfo(
                    "Beta Version Release",
                    propertiesProvider.getAdminEmail(),
                    NEWS_INTRODUCTION_1,
                    NEWS_BODY_1,
                    format.parse("Jun:03:2013")
                )
            );
            newsManagement.createNews(
                admin,
                new NewsManagement.NewsInfo(
                    "Introducing the Chorus Project",
                    propertiesProvider.getAdminEmail(),
                    NEWS_INTRODUCTION_2,
                    NEWS_BODY_2,
                    format.parse("Jun:10:2013")
                )
            );
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void skylineExtractionFiles(long andrey, long ftmsInstrument, long specie) {

        final long fileMetaDataId = instrumentManagement.createFile(
            andrey,
            ftmsInstrument,
            new FileMetaDataInfo("20130311_DIA_Pit01.raw", RAW_FILE_1_SIZE, "", null, specie, false)
        );
        instrumentManagement.setContentID(andrey, fileMetaDataId, SAMPLE_RAW_FILE_1_FN.apply("20130311_DIA_Pit01.raw"));
        if (!testMode) {
            copyFileOnS3IfNeeded(SAMPLE_RAW_FILE_1_FN.apply("20130311_DIA_Pit01.raw"), PERMANENT_SAMPLE_RAW_FILE_S3_1);
        }

        final FileMetaInfoHelper.MetaInfo metaInfo = new FileMetaInfoHelper.MetaInfo();
        metaInfo.creationDate = new Date();
        final Random random = new Random();
        metaInfo.comment = "Comment " + random.nextLong();
        metaInfo.startRt = Float.toString(random.nextFloat());
        metaInfo.endRt = Float.toString(random.nextFloat());
        metaInfo.instrumentMethod = "Instrument method " + random.nextLong();

        fileMetaInfoHelper.updateFileMeta(fileMetaDataId, metaInfo);

        final Set<MSFunctionDTO> functions = new HashSet<>();
        functions.add(new MSFunctionDTO(
            "FTMS + p NSI Full ms [500.00-900.00]",
            MSFunctionDataType.PROFILE,
            MSFunctionImageType.INT_IMAGE,
            MSFunctionType.MS,
            MSFunctionScanType.FULL,
            MSFunctionFragmentationType.CAD,
            MSFunctionMassAnalyzerType.FTMS,
            MSResolutionType.HIGH,
            "LTQ FT",
            "LTQ FTSN06106F",
            true,
            true,
            false,
            RESOLUTION,
            0,
            6677,
            96720,
            0,
            Integer.MAX_VALUE
        ));


        final Set<String> ms2Functions = Sets.newHashSet(
            THERMO_DIA_MS2_1,
            THERMO_DIA_MS2_2,
            THERMO_DIA_MS2_3,
            THERMO_DIA_MS2_4,
            THERMO_DIA_MS2_5,
            THERMO_DIA_MS2_6,
            THERMO_DIA_MS2_7,
            THERMO_DIA_MS2_8,
            THERMO_DIA_MS2_9,
            THERMO_DIA_MS2_10,
            THERMO_DIA_MS2_11,
            THERMO_DIA_MS2_12,
            THERMO_DIA_MS2_13,
            THERMO_DIA_MS2_14,
            THERMO_DIA_MS2_15,
            THERMO_DIA_MS2_16,
            THERMO_DIA_MS2_17,
            THERMO_DIA_MS2_18,
            THERMO_DIA_MS2_19,
            THERMO_DIA_MS2_20
        );

        for (String ms2Function : ms2Functions) {
            functions.add(new MSFunctionDTO(
                ms2Function.substring(ms2Function.lastIndexOf("/") + 1),
                MSFunctionDataType.CENTROID,
                MSFunctionImageType.INT_IMAGE,
                MSFunctionType.MS2,
                MSFunctionScanType.FULL,
                MSFunctionFragmentationType.CAD,
                MSFunctionMassAnalyzerType.ITMS,
                MSResolutionType.LOW,
                "LTQ FT", "LTQ FTSN06106F",
                false,
                true,
                false,
                RESOLUTION,
                0,
                MAX_SCAN,
                MAX_PACKETS,
                0,
                Integer.MAX_VALUE
            ));
        }
    }

    private long createUserAndVerifyEmail(UserManagement.PersonInfo mikeHead, long uwLab) {
        final long userId = userManagement.createPersonAndApproveMembership(mikeHead, PASSWORD, uwLab, null);
        userManagement.verifyEmail(userId);
        return userId;
    }

    private void combinedExperiment(
        long andrey,
        long lab,
        long ftmsModel,
        long ftmsInstrument,
        long specie,
        long liver1,
        long liver2,
        long liver3,
        long liver4,
        long liver5,
        long liver6,
        long liver7,
        long liver8,
        long liver9,
        long liver10,
        long liver11,
        long heart1,
        long heart2,
        long heart3,
        long heart4,
        long heart5,
        long heart6,
        long heart7,
        long heart8,
        long heart9,
        long heart10,
        long heart11,
        long proKineticsProject
    ) {
        ExperimentInfo.Builder combinedExInfo = new ExperimentInfo.Builder().name("Combined Kinetics")
            .description("Mouse heart and liver combined protein kinetics analysis")
            .experimentType(anyExperimentType())
            .specie(specie);

        final FileItem liver1FileItem =
            new FileItem(liver1, false, 0, sampleWithFactors(
                liver1,
                of("3", LIVER, "liver : 3 days")
            ));
        final FileItem liver2FileItem =
            new FileItem(liver2,
                         false, 0, sampleWithFactors(liver2, of("3", LIVER, "liver : 3 days"))
            );
        final FileItem liver3FileItem =
            new FileItem(liver3, false, 0, sampleWithFactors(liver3, of("3", LIVER, "liver : 3 days")));

        final FileItem liver4FileItem =

            new FileItem(liver4, false, 0, sampleWithFactors(liver4, of("6", LIVER, "liver : 6 days")));
        final FileItem liver5FileItem =
            new FileItem(liver5, false, 0, sampleWithFactors(liver5, of("6", LIVER, "liver : 6 days")));

        final FileItem liver6FileItem =
            new FileItem(liver6, false, 0, sampleWithFactors(liver6, of("10", LIVER, "liver : 10 days")));
        final FileItem liver7FileItem =
            new FileItem(liver7, false, 0, sampleWithFactors(liver7, of("10", LIVER, "liver : 10 days")));
        final FileItem liver8FileItem =
            new FileItem(liver8, false, 0, sampleWithFactors(liver8, of("10", LIVER, "liver : 10 days")));

        final FileItem liver9FileItem =
            new FileItem(liver9, false, 0, sampleWithFactors(liver9, of("17", LIVER, "liver : 17 days")));
        final FileItem liver10FileItem =
            new FileItem(liver10, false, 0, sampleWithFactors(liver10, of("17", LIVER, "liver : 17 days")));
        final FileItem liver11FileItem =
            new FileItem(liver11, false, 0, sampleWithFactors(liver11, of("17", LIVER, "liver : 17 days")));

        final FileItem heart1FileItem =
            new FileItem(heart1, false, 0, sampleWithFactors(heart1, of("3", HEART, "heart : 3 days")));
        final FileItem heart2FileItem =
            new FileItem(heart2, false, 0, sampleWithFactors(heart2, of("3", HEART, "heart : 3 days")));
        final FileItem heart3FileItem =
            new FileItem(heart3, false, 0, sampleWithFactors(heart3, of("3", HEART, "heart : 3 days")));

        final FileItem heart4FileItem =

            new FileItem(heart4, false, 0, sampleWithFactors(heart4, of("6", HEART, "heart : 6 days")));
        final FileItem heart5FileItem =
            new FileItem(heart5, false, 0, sampleWithFactors(heart5, of("6", HEART, "heart : 6 days")));

        final FileItem heart6FileItem =
            new FileItem(heart6, false, 0, sampleWithFactors(heart6, of("10", HEART, "heart : 10 days")));
        final FileItem heart7FileItem =
            new FileItem(heart7, false, 0, sampleWithFactors(heart7, of("10", HEART, "heart : 10 days")));
        final FileItem heart8FileItem =
            new FileItem(heart8, false, 0, sampleWithFactors(heart8, of("10", HEART, "heart : 10 days")));


        final FileItem heart9FileItem =
            new FileItem(heart9, false, 0, sampleWithFactors(heart9, of("17", HEART, "heart : 17 days")));
        final FileItem heart10FileItem =
            new FileItem(heart10, false, 0, sampleWithFactors(heart10, of("17", HEART, "heart : 17 days")));
        final FileItem heart11FileItem =
            new FileItem(heart11, false, 0, sampleWithFactors(heart11, of("17", HEART, "heart : 17 days")));

        final List<FileItem> combinedExperimentFiles = of(
            liver1FileItem, liver2FileItem, liver3FileItem, liver4FileItem,
            liver5FileItem, liver6FileItem, liver7FileItem, liver8FileItem,
            liver9FileItem, liver10FileItem, liver11FileItem,
            heart1FileItem, heart2FileItem, heart3FileItem, heart4FileItem,
            heart5FileItem, heart6FileItem, heart7FileItem, heart8FileItem,
            heart9FileItem, heart10FileItem, heart11FileItem
        );

        final FileItem seedFile = new FileItem(heart11, false, 0, sampleWithoutFactors(heart11));

        combinedExInfo.project(proKineticsProject).lab(lab).billLab(lab).is2dLc(false)
            .restriction(restrictionFromModelAndInstrument(ftmsModel, ftmsInstrument))
            .factors(Collections.<MetaFactorTemplate>emptyList())
            .files(of(seedFile))
            .bounds(new AnalysisBounds())
            .lockMasses(new ArrayList<>())
            .experimentLabels(new ExperimentLabelsInfo());

        final long experiment = studyManagement.createExperiment(andrey, combinedExInfo.build());

        final ImmutableList<MetaFactorTemplate> metaFactors = of(
            new MetaFactorTemplate("Factor - Time", "Days", true, experiment),
            new MetaFactorTemplate("Factor Tissue", "", false, experiment),
            new MetaFactorTemplate("Condition", "", false, experiment)
        );

        combinedExInfo.factors(metaFactors).files(combinedExperimentFiles);

        studyManagement.updateExperiment(andrey, experiment, combinedExInfo.build());

    }

    private Restriction restrictionFromModel(long model) {
        return new Restriction(model, Optional.absent());
    }

    private Restriction restrictionFromModelAndInstrument(long model, long instrument) {
        return new Restriction(model, Optional.of(instrument));
    }

    private void heartExperiment(
        long andrey,
        long lab,
        long ftmsModel,
        long ftmsInstrument,
        long specie,
        long heart1,
        long heart2,
        long heart3,
        long heart4,
        long heart5,
        long heart6,
        long heart7,
        long heart8,
        long heart9,
        long heart10,
        long heart11,
        long proKineticsProject
    ) {
        ExperimentInfo.Builder heartExInfo = new ExperimentInfo.Builder().name("Heart Kinetics")
            .description("Mouse heart protein kinetics analysis")
            .experimentType(anyExperimentType())
            .specie(specie);

        final FileItem heart1FileItem = new FileItem(heart1, false, 0, sampleWithFactors(heart1, of("3", "3 days")));
        final FileItem heart2FileItem = new FileItem(heart2, false, 0, sampleWithFactors(heart2, of("3", "3 days")));
        final FileItem heart3FileItem = new FileItem(heart3, false, 0, sampleWithFactors(heart3, of("3", "3 days")));

        final FileItem heart4FileItem = new FileItem(heart4, false, 0, sampleWithFactors(heart4, of("6", "6 days")));
        final FileItem heart5FileItem = new FileItem(heart5, false, 0, sampleWithFactors(heart5, of("6", "6 days")));

        final FileItem heart6FileItem = new FileItem(heart6, false, 0, sampleWithFactors(heart6, of("10", "10 days")));
        final FileItem heart7FileItem = new FileItem(heart7, false, 0, sampleWithFactors(heart7, of("10", "10 days")));
        final FileItem heart8FileItem = new FileItem(heart8, false, 0, sampleWithFactors(heart8, of("10", "10 days")));

        final FileItem heart9FileItem = new FileItem(heart9, false, 0, sampleWithFactors(heart9, of("17", "17 days")));
        final FileItem heart10FileItem =
            new FileItem(heart10, false, 0, sampleWithFactors(heart10, of("17", "17 days")));
        final FileItem heart11FileItem =
            new FileItem(heart11, false, 0, sampleWithFactors(heart11, of("17", "17 days")));


        final List<FileItem> heartExperimentFiles = of(
            heart1FileItem, heart2FileItem, heart3FileItem, heart4FileItem,
            heart5FileItem, heart6FileItem, heart7FileItem, heart8FileItem,
            heart9FileItem, heart10FileItem, heart11FileItem
        );

        final FileItem seedFile = new FileItem(heart1, false, 0, sampleWithoutFactors(heart1));

        heartExInfo.project(proKineticsProject).lab(lab).billLab(lab).is2dLc(false)
            .restriction(restrictionFromModelAndInstrument(ftmsModel, ftmsInstrument))
            .factors(Collections.<MetaFactorTemplate>emptyList())
            .files(of(seedFile)).bounds(new AnalysisBounds())
            .lockMasses(new ArrayList<>())
            .experimentLabels(new ExperimentLabelsInfo());

        final long heartExperiment = studyManagement.createExperiment(andrey, heartExInfo.build());

        final ImmutableList<MetaFactorTemplate> metaFactors = of(
            new MetaFactorTemplate("Factor - Time", "Days", true, heartExperiment),
            new MetaFactorTemplate("Condition", "", false, heartExperiment)
        );

        heartExInfo.factors(metaFactors).files(heartExperimentFiles);

        studyManagement.updateExperiment(andrey, heartExperiment, heartExInfo.build());
    }

    private void liverExperiment(
        long andrey,
        long lab,
        long ftmsModel,
        long ftmsInstrument,
        long specie,
        long liver1,
        long liver2,
        long liver3,
        long liver4,
        long liver5,
        long liver6,
        long liver7,
        long liver8,
        long liver9,
        long liver10,
        long liver11,
        long proKineticsProject
    ) {
        ExperimentInfo.Builder liverExInfo = new ExperimentInfo.Builder().name("Liver Kinetics")
            .description("Mouse liver protein kinetics analysis")
            .experimentType(anyExperimentType())
            .specie(specie);

        final FileItem liver1FileItem = new FileItem(liver1, false, 0, sampleWithFactors(liver1, of("3", "3 days")));
        final FileItem liver2FileItem = new FileItem(liver2, false, 0, sampleWithFactors(liver2, of("3", "3 days")));
        final FileItem liver3FileItem = new FileItem(liver3, false, 0, sampleWithFactors(liver3, of("3", "3 days")));

        final FileItem liver4FileItem = new FileItem(liver4, false, 0, sampleWithFactors(liver4, of("6", "6 days")));
        final FileItem liver5FileItem = new FileItem(liver5, false, 0, sampleWithFactors(liver5, of("6", "6 days")));

        final FileItem liver6FileItem = new FileItem(liver6, false, 0, sampleWithFactors(liver6, of("10", "10 days")));
        final FileItem liver7FileItem = new FileItem(liver7, false, 0, sampleWithFactors(liver7, of("10", "10 days")));
        final FileItem liver8FileItem = new FileItem(liver8, false, 0, sampleWithFactors(liver8, of("10", "10 days")));

        final FileItem liver9FileItem = new FileItem(liver9, false, 0, sampleWithFactors(liver9, of("17", "17 days")));
        final FileItem liver10FileItem =
            new FileItem(liver10, false, 0, sampleWithFactors(liver10, of("17", "17 days")));
        final FileItem liver11FileItem =
            new FileItem(liver11, false, 0, sampleWithFactors(liver11, of("17", "17 days")));


        final List<FileItem> liverExperimentFiles = of(
            liver1FileItem, liver2FileItem, liver3FileItem, liver4FileItem,
            liver5FileItem, liver6FileItem, liver7FileItem, liver8FileItem,
            liver9FileItem, liver10FileItem, liver11FileItem
        );

        final FileItem seedFile = new FileItem(liver11, false, 0, sampleWithoutFactors(liver11));

        liverExInfo.project(proKineticsProject).lab(lab).billLab(lab)
            .is2dLc(false)
            .restriction(restrictionFromModelAndInstrument(ftmsModel, ftmsInstrument))
            .experimentLabels(new ExperimentLabelsInfo())
            .factors(Collections.<MetaFactorTemplate>emptyList())
            .files(of(seedFile))
            .bounds(new AnalysisBounds())
            .lockMasses(new ArrayList<>());

        final long liverExperiment = studyManagement.createExperiment(andrey, liverExInfo.build());

        liverExInfo.factors(of(
            new MetaFactorTemplate("Factor - Time", "Days", true, liverExperiment),
            new MetaFactorTemplate("Condition", "", false, liverExperiment)
        )).files(liverExperimentFiles);

        studyManagement.updateExperiment(andrey, liverExperiment, liverExInfo.build());
    }

    private List<String> emptyFactors() {
        return Collections.<String>emptyList();
    }

    private Long createSeveralPublicProjects(long anotherUser, long lab) {
        Long publicProject = createProject(
            anotherUser,
            lab,
            "Personal Genome Project",
            "Technology, Science, ELSI, Health Care",
            "The PGP hopes to make personal genome sequencing more affordable, accessible, and useful for humankind."
        );
        sharingManagement.makeProjectPublic(anotherUser, publicProject);
        Long publicProjectB = createProject(
            anotherUser,
            lab,
            "Drive for biomass to feed synthetic organisms project",
            "Synthetic biology and many others very interesting areas enough for long area of research",
            "Synthetic biology is an extreme form of genetic engineering, an emerging technology that is developing " +
                "rapidly yet is largely unregulated. With synthetic biology, instead of swapping genes from one " +
                "species to another"
        );
        sharingManagement.makeProjectPublic(anotherUser, publicProjectB);
        Long publicProjectC = createProject(
            anotherUser,
            lab,
            "Small molecule screen",
            "Technology, Science, ELSI, Health Care",
            "An automated small molecule screening method for Chlamydomonas reinhardtii, and its application to " +
                "identify bioactive compounds for agriculture, biofuels and therapeutics."
        );
        sharingManagement.makeProjectPublic(anotherUser, publicProjectC);
        return publicProject;
    }

    private long createActor(long lab, String firstName, String lastName, String email) {
        UserManagement.PersonInfo anotherPersonInfo =
            new UserManagement.PersonInfo(firstName, lastName, email);
        long userId = userManagement.createPersonAndApproveMembership(anotherPersonInfo, PASSWORD, lab, null);
        userManagement.verifyEmail(userId);
        userManagement.resetPassword(userId, passwordEncoder.encode(PASSWORD));
        return userId;
    }

    private long createLab(long admin, String labName) {
        final String labHeadFirstName = "Demo ";
        final String labHeadLastName = "User";
        final String institutionUrl = "http://teamdev.com";
        return createLab(
            admin,
            MAIN_ACTOR_EMAIL,
            institutionUrl,
            labName,
            new UserManagement.PersonInfo(labHeadFirstName, labHeadLastName, MAIN_ACTOR_EMAIL)
        );
    }

    private long createLab(
        long admin,
        String labHeadMail,
        String institutionUrl,
        String labName,
        UserManagement.PersonInfo personInfoLab
    ) {
        long lab = labManagement.requestLabCreation(new LabManagementTemplate.LabInfoTemplate(
            institutionUrl,
            personInfoLab,
            labName
        ), labHeadMail);
        labManagement.confirmLabCreation(admin, lab);
        return lab;
    }

    private long createHelsinkiLab(long admin) {
        final String institutionUrl = "https://www.celgene.com/";
        final String labName = "New University of Helsinki";
        return createLab(
            admin,
            AB_EMAIL,
            institutionUrl,
            labName,
            new UserManagement.PersonInfo(AB_FIRST_NAME, AB_LAST_NAME, AB_EMAIL)
        );
    }

    private long createAdmin() {
        return initiator.admin(
            "Mark",
            "Adminovich",
            propertiesProvider.getAdminEmail(),
            propertiesProvider.getAdminPassword()
        );
    }

    private void attachFiles(long user, long instrument) {
        final long specie = anySpecie();
        attachFile(
            user,
            instrument,
            "c15092005_000.RAW",
            RAW_FILE_1_SIZE,
            specie,
            "cloud, demo, gaucher",
            SAMPLE_RAW_FILE_1_FN.apply("c15092005_000.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_1
        );
        attachFile(
            user,
            instrument,
            "c15092005_001.RAW",
            RAW_FILE_2_SIZE,
            specie,
            "cloud, demo, gaucher",
            SAMPLE_RAW_FILE_2_FN.apply("c15092005_001.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_2
        );
        attachFile(
            user,
            instrument,
            "c15092005_002_here_we_have_long_name_to_check_ellipsizing.RAW",
            RAW_FILE_3_SIZE,
            specie,
            "mscloud, demo, gaucher, other, labels, to, check, ellipsizing",
            SAMPLE_RAW_FILE_3_FN.apply("c15092005_002_here_we_have_long_name_to_check_ellipsizing.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_3
        );
        attachFile(
            user,
            instrument,
            "c15092005_003.RAW",
            RAW_FILE_4_SIZE,
            specie,
            "cloud, demo, gaucher",
            SAMPLE_RAW_FILE_4_FN.apply("c15092005_003.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_4
        );
        attachFile(
            user,
            instrument,
            "c15092005_004.RAW",
            RAW_FILE_5_SIZE,
            specie,
            "cloud, old, gaucher",
            SAMPLE_RAW_FILE_5_FN.apply("c15092005_004.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_5
        );
        attachFile(
            user,
            instrument,
            "c15092005_006.RAW",
            RAW_FILE_6_SIZE,
            specie,
            "mscloud, another, gaucher",
            SAMPLE_RAW_FILE_6_FN.apply("c15092005_006.RAW"),
            PERMANENT_SAMPLE_RAW_FILE_S3_6
        );

        skylineExtractionFiles(user, instrument, specie);
    }

    private long attachFile(
        long user,
        long instrument,
        String fileName,
        int sizeInBytes,
        long specie,
        String labels,
        String contentId,
        String realFileContent
    ) {
        final long fileMetaDataId = instrumentManagement.createFile(
            user,
            instrument,
            new FileMetaDataInfo(fileName, sizeInBytes, labels, null, specie, false)
        );
        instrumentManagement.setContentID(user, fileMetaDataId, contentId);

        if (!testMode) {
            copyFileOnS3IfNeeded(contentId, realFileContent);
        }


        final FileMetaInfoHelper.MetaInfo metaInfo = new FileMetaInfoHelper.MetaInfo();
        metaInfo.creationDate = new Date();
        final Random random = new Random();
        metaInfo.comment = "Comment " + random.nextLong();
        metaInfo.startRt = Float.toString(random.nextFloat());
        metaInfo.endRt = Float.toString(random.nextFloat());
        metaInfo.instrumentMethod = "Instrument method " + random.nextLong();

        fileMetaInfoHelper.updateFileMeta(fileMetaDataId, metaInfo);

        return fileMetaDataId;
    }

    private void createExperiment(long user, long project, long lab, String projectName) {
        final String experimentName = " Experiment With Very Long Name Enough For Testing Ellipsize";
        createExperiment(user, project, lab, projectName, experimentName);
    }

    private void createExperiment(long user, long project, long lab, String projectName, String experimentName) {
        final long instrumentModel = experimentCreationHelper.availableInstrumentModels(user, lab).get(0).id;
        final long specie = anySpecie();
        ExperimentInfo.Builder experimentInfo = new ExperimentInfo.Builder().name(projectName + experimentName)
            .description("Experiment for project " + projectName)
            .experimentType(anyExperimentType())
            .specie(specie);


        final List<com.infoclinika.mssharing.platform.model.common.items.FileItem> fileItems =
            experimentCreationHelper.availableFilesByInstrumentModel(user, specie, instrumentModel, null);
        final LinkedList<com.infoclinika.mssharing.platform.model.common.items.FileItem> fileList =
            new LinkedList<com.infoclinika.mssharing.platform.model.common.items.FileItem>(fileItems);
        final List<com.infoclinika.mssharing.platform.model.common.items.FileItem> filesForThisExperiment =
            fileList.subList(0, 10);

        final List<FileItem> experimentFileList = Lists.transform(
            filesForThisExperiment,
            new Function<com.infoclinika.mssharing.platform.model.common.items.FileItem, FileItem>() {
                @Override
                public FileItem apply(com.infoclinika.mssharing.platform.model.common.items.FileItem fileItem) {
                    final ImmutableSet<ExperimentSampleItem> bioSamples =
                        ImmutableSet.of(new ExperimentSampleItem(String.valueOf(fileItem.id), LIGHT, emptyFactors()));
                    return new FileItem(
                        fileItem.id,
                        false,
                        0,
                        new ExperimentPreparedSampleItem(String.valueOf(fileItem.id), bioSamples)
                    );
                }
            }
        );

        experimentInfo.project(project).lab(lab).billLab(lab).is2dLc(false)
            .restriction(restrictionFromModel(instrumentModel))
            .factors(Collections.<MetaFactorTemplate>emptyList())
            .files(experimentFileList)
            .bounds(new AnalysisBounds())
            .lockMasses(new ArrayList<>())
            .experimentLabels(new ExperimentLabelsInfo());

        studyManagement.createExperiment(user, experimentInfo.build());
    }

    private long anySpecie() {
        return experimentCreationHelper.species().iterator().next().id;
    }


    private long createProject(long userId, long lab, String name, String areaOfResearch, String description) {
        ProjectInfo projectInfo = new ProjectInfo(name, areaOfResearch, description, lab);
        return studyManagement.createProject(userId, projectInfo);
    }

    private long someInstrumentModel() {
        final DictionaryItem vendor =
            Collections2.filter(instrumentCreationHelper.vendors(), new Predicate<DictionaryItem>() {
                @Override
                public boolean apply(DictionaryItem input) {
                    return "Thermo Scientific".equals(input.name);
                }
            }).iterator().next();
        return instrumentCreationHelper.models(vendor.id).first().id;
    }

    private long nextGenInstrumentModel() {
        final DictionaryItem vendor =
            Collections2.filter(instrumentCreationHelper.vendors(), new Predicate<DictionaryItem>() {
                @Override
                public boolean apply(DictionaryItem input) {
                    return NEXT_GEN_SEQUENCING.equals(input.name);
                }
            }).iterator().next();
        return instrumentCreationHelper.models(vendor.id).first().id;
    }

    public long anyExperimentType() {
        return experimentCreationHelper.experimentTypes()
            .stream()
            .filter(type -> SeedDataCreator.BOTTOM_UP_PROTEOMICS_EXPERIMENT_TYPE.equals(type.name))
            .findFirst()
            .get().id;
    }


}
