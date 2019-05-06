/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika,
 * Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use,
 * duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal.read;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.infoclinika.mssharing.model.PaginationItems;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.entity.*;
import com.infoclinika.mssharing.model.internal.entity.restorable.*;
import com.infoclinika.mssharing.model.internal.entity.view.ExperimentDashboardRecord;
import com.infoclinika.mssharing.model.internal.repository.*;
import com.infoclinika.mssharing.model.read.*;
import com.infoclinika.mssharing.platform.entity.ExperimentFileTemplate;
import com.infoclinika.mssharing.platform.entity.ProjectSharingRequestTemplate;
import com.infoclinika.mssharing.platform.entity.Sharing;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.common.items.FileItem;
import com.infoclinika.mssharing.platform.model.common.items.InstrumentItem;
import com.infoclinika.mssharing.platform.model.common.items.LabItem;
import com.infoclinika.mssharing.platform.model.helper.write.ExperimentManager;
import com.infoclinika.mssharing.platform.model.read.*;
import com.infoclinika.mssharing.platform.repository.ProjectSharingRequestRepositoryTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.*;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Sets.newHashSet;
import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkPresence;
import static com.infoclinika.mssharing.platform.model.impl.ValidatorPredicates.*;

/**
 * @author Stanislav Kurilin
 */
@Component
@Transactional(readOnly = true)
public class DashboardReaderImpl implements DashboardReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardReaderImpl.class);


    @Inject
    private ProjectRepository projectRepository;
    @Inject
    private ExperimentRepository experimentRepository;
    @Inject
    private ExperimentManager experimentManager;
    @Inject
    private FileMetaDataRepository fileMetaDataRepository;
    @Inject
    private LabRepository labRepository;
    @Inject
    private InstrumentRepository instrumentRepository;
    @Inject
    private RuleValidator ruleValidator;
    @Inject
    private Transformers transformers;
    @Inject
    private UserRepository userRepository;
    @Inject
    private ProjectSharingRequestRepositoryTemplate<ProjectSharingRequestTemplate> projectSharingRequestRepository;

    @PersistenceContext(unitName = "mssharing")
    protected EntityManager em;

    @Inject
    private FeaturesRepository featuresRepository;

    @Inject
    @Named("instrumentReaderImpl")
    private InstrumentReaderTemplate<InstrumentLine> instrumentReader;
    @Inject
    @Named("defaultLabReaderAdapter")
    private LabReaderTemplate<LabReaderTemplate.LabLineTemplate> labReader;
    @Inject
    @Named("projectReaderImpl")
    private ProjectReaderTemplate<ProjectLine> projectReader;
    @Inject
    @Named("fileReaderImpl")
    private FileReader<FileLine> fileReader;
    @Inject
    private ExperimentReaderTemplate<ExperimentLine> experimentReader;
    @Inject
    private GroupsReaderTemplate<GroupLine> groupsReader;
    @Inject
    @Named("defaultInstrumentModelsReaderAdapter")
    private InstrumentModelReaderTemplate<InstrumentModelLineTemplate> instrumentModelReader;

    public static final Comparator<ExperimentLine> EXPERIMENT_LINE_COMPARATOR = new Comparator<ExperimentLine>() {
        @Override
        public int compare(ExperimentLine o1, ExperimentLine o2) {
            return o1.name.compareTo(o2.name);
        }
    };
    public static final Comparator<ShortExperimentDashboardRecord> SHORT_EXPERIMENT_RECORD_COMPARATOR =
        new Comparator<ShortExperimentDashboardRecord>() {
            @Override
            public int compare(ShortExperimentDashboardRecord o1, ShortExperimentDashboardRecord o2) {
                return o1.name.compareTo(o2.name);
            }
        };

    public static final Function<ExperimentFileTemplate, UploadedFile> UPLOADED_FILE_ITEM_FROM_RAW =
        new Function<ExperimentFileTemplate, UploadedFile>() {
            @Override
            public UploadedFile apply(ExperimentFileTemplate input) {
                AbstractFileMetaData data = (AbstractFileMetaData) input.getFileMetaData();
                return toUploadedFile(data);
            }
        };

    public static final Function<ActiveFileMetaData, UploadedFile> UPLOADED_FILE_ITEM_FROM_META_DATA =
        new Function<ActiveFileMetaData, UploadedFile>() {
            @Override
            public UploadedFile apply(ActiveFileMetaData data) {
                return toUploadedFile(data);
            }
        };

    private static UploadedFile toUploadedFile(AbstractFileMetaData data) {
        final Instrument instrument = data.getInstrument();
        final FileMetaAnnotations metaInfo = data.getMetaInfo();
        final Date acquisitionDate = metaInfo == null ? null : metaInfo.getCreationDate();

        return new UploadedFile(data.getId(), data.getName(), instrument.getName(),
            Transformers.toFullInstrumentModel(instrument.getModel()),
            data.getUploadDate(), acquisitionDate, data.getSizeInBytes(),
            data.getOwner().getId()
        );
    }

    @Override
    @Transactional
    public void setExperimentFailed(long actor, long experiment, boolean failed) {
        final ActiveExperiment activeExperiment = checkNotNull(experimentRepository.findOne(experiment));
        activeExperiment.setFailed(failed);

        experimentManager.saveExperiment(activeExperiment);
    }

    @Override
    public ImmutableSet<InstrumentItem> instrumentsWithAvailableFiles(long actor) {
        return from(fileMetaDataRepository.instrumentsWithAvailableFiles(actor))
            .transform(transformers.instrumentItemTransformer()).toSet();
    }

    @Override
    public InstrumentItem readInstrument(long instrument) {
        final Instrument one = instrumentRepository.findOne(instrument);
        return transformers.instrumentItemTransformer().apply(one);
    }

    /**
     * *********************************** ProjectReader methods ****************************************************
     */

    @Override
    public SortedSet<ProjectLine> readProjects(long userId, Filter filter) {
        return projectReader.readProjects(userId, filter);
    }

    @Override
    public PagedItem<ProjectLine> readProjects(long user, Filter filter, PagedItemInfo pageInfo) {
        return projectReader.readProjects(user, filter, pageInfo);
    }

    @Override
    public PagedItem<ProjectLine> readProjectsByLab(long actor, Long lab, PagedItemInfo pagedItemInfo) {
        return projectReader.readProjectsByLab(actor, lab, pagedItemInfo);
    }

    @Override
    public ProjectLine readProject(long user, long projectID) {
        return projectReader.readProject(user, projectID);
    }

    @Override
    public SortedSet<ProjectLine> readProjectsAllowedForWriting(long user) {
        return projectReader.readProjectsAllowedForWriting(user);
    }

    /**
     * ************************************* GroupReader methods ****************************************************
     */

    @Override
    public ImmutableSet<GroupLine> readGroups(long actor, boolean includeAllUsers) {
        return groupsReader.readGroups(actor, includeAllUsers);
    }

    /**
     * ************************************* FilesReader methods ****************************************************
     */

    @Override
    public Set<FileLine> readFiles(long user, Filter filter) {
        return fileReader.readFiles(user, filter);
    }

    @Override
    public PagedItem<FileLine> readFiles(long user, Filter filter, PagedItemInfo pagedInfo) {
        return fileReader.readFiles(user, filter, pagedInfo);
    }

    @Override
    public PagedItem<FileLine> readFilesByLab(long userId, long labId, PagedItemInfo pagedInfo) {
        return fileReader.readFilesByLab(userId, labId, pagedInfo);
    }

    @Override
    public Set<FileLine> readFilesByLab(long userId, long labId) {
        return fileReader.readFilesByLab(userId, labId);
    }

    @Override
    public PagedItem<FileLine> readFilesByInstrument(long actor, long instrument, PagedItemInfo pagedInfo) {
        return fileReader.readFilesByInstrument(actor, instrument, pagedInfo);
    }

    @Override
    public Set<FileLine> readFilesByInstrument(long actor, long instrument) {
        return fileReader.readFilesByInstrument(actor, instrument);
    }

    @Override
    public Set<FileLine> readByNameForInstrument(long actor, long instrument, String fileName) {
        return fileReader.readByNameForInstrument(actor, instrument, fileName);
    }

    @Override
    public PagedItem<FileLine> readFilesByExperiment(long actor, long experiment, PagedItemInfo pagedInfo) {
        return fileReader.readFilesByExperiment(actor, experiment, pagedInfo);
    }

    @Override
    public Set<FileLine> readFilesByExperiment(long actor, long experiment) {
        return fileReader.readFilesByExperiment(actor, experiment);
    }

    @Override
    public Set<FileLine> readUnfinishedFiles(long user) {
        return fileReader.readUnfinishedFiles(user);
    }

    @Override
    public SortedSet<FileItem> readFileItemsByExperiment(long actor, long experimentId) {
        return fileReader.readFileItemsByExperiment(actor, experimentId);
    }

    /**
     * ************************************ ExperimentsReader methods ***********************************************
     */

    @Override
    public PagedItem<ExperimentLine> readExperiments(long actor, Filter filter, PagedItemInfo pageInfo) {
        return experimentReader.readExperiments(actor, filter, pageInfo);
    }

    @Override
    public SortedSet<ExperimentLine> readExperiments(long actor, Filter filter) {
        return experimentReader.readExperiments(actor, filter);
    }

    @Override
    public PagedItem<ExperimentLine> readExperimentsByLab(long actor, long labId, PagedItemInfo pagedItemInfo) {
        return experimentReader.readExperimentsByLab(actor, labId, pagedItemInfo);
    }

    @Override
    public SortedSet<ExperimentLine> readExperimentsByProject(long user, long project) {
        return experimentReader.readExperimentsByProject(user, project);
    }

    /**
     * ************************************ InstrumentsReader methods ***********************************************
     */

    @Override
    public Set<InstrumentLine> readInstrumentsByLab(long userId, long labId) {
        return instrumentReader.readInstrumentsByLab(userId, labId);
    }

    @Override
    public PagedItem<InstrumentLine> readInstrumentsByLab(long actor, long lab, PagedItemInfo pagedInfo) {
        return instrumentReader.readInstrumentsByLab(actor, lab, pagedInfo);
    }

    @Override
    public PagedItem<InstrumentLine> readInstruments(long actor, PagedItemInfo pagedInfo) {
        return instrumentReader.readInstruments(actor, pagedInfo);
    }

    @Override
    public Set<InstrumentLine> readInstruments(long userId) {
        return instrumentReader.readInstruments(userId);
    }

    @Override
    public Set<InstrumentLine> readInstrumentsByLabAndStudyType(long actor, long lab, long studyType) {
        return instrumentReader.readInstrumentsByLabAndStudyType(actor, lab, studyType);
    }

    /**
     * ************************************ LabReader methods *******************************************************
     */

    @Override
    public ImmutableSet<LabReaderTemplate.LabLineTemplate> readUserLabs(long actor) {
        return labReader.readUserLabs(actor);
    }

    @Override
    public LabReaderTemplate.LabLineTemplate readLab(long id) {
        return labReader.readLab(id);
    }

    @Override
    public LabReaderTemplate.LabLineTemplate readLabByName(String name) {
        return labReader.readLabByName(name);
    }

    @Override
    public SortedSet<LabItem> readLabItems(long actor) {
        return labReader.readLabItems(actor);
    }

    @Override
    public ImmutableSet<LabLineTemplate> readAllLabs(long actor) {
        return labReader.readAllLabs(actor);
    }

    /**
     * ************************************ Charts ******************************************************************
     */

    @Override
    public PagedItem<ExperimentLine> readPagedExperimentsByProject(long actor, long projectId, PagedItemInfo
        pagedItemInfo) {

        return experimentReader.readPagedExperimentsByProject(actor, projectId, pagedItemInfo);
    }

    /**
     * ********************************** Skyline methods ***********************************************************
     */

    @Override
    public FullFolderStructure readFolderStructure(long user) {

        final Set<ProjectLine> myProjects = readProjects(user, Filter.MY);
        final ImmutableSortedSet<ExperimentLine> myExperiments = readExperimentsForFolderStructure(user, Filter.MY);
        final Set<UploadedFile> myFiles = readDetailedFiles(user, Filter.MY);

        final Set<ProjectLine> sharedProjects = readProjects(user, Filter.SHARED_WITH_ME);
        final ImmutableSortedSet<ExperimentLine> sharedExperiments =
            readExperimentsForFolderStructure(user, Filter.SHARED_WITH_ME);
        final Set<UploadedFile> sharedFiles = readDetailedFiles(user, Filter.SHARED_WITH_ME);

        final Set<ProjectLine> publicProjects = readProjects(user, Filter.PUBLIC);
        final ImmutableSortedSet<ExperimentLine> publicExperiments =
            readExperimentsForFolderStructure(user, Filter.PUBLIC);
        final Set<UploadedFile> publicFiles = readDetailedFiles(user, Filter.PUBLIC);

        final FullFolderStructure result = new FullFolderStructure();
        result.myProjects.addAll(toProjectStructure(user, myProjects, true));
        result.myExperiments.addAll(toExperimentStructure(user, myExperiments, true));
        result.myFiles.addAll(myFiles);

        result.sharedProjects.addAll(toProjectStructure(user, sharedProjects, true));
        result.sharedExperiments.addAll(toExperimentStructure(user, sharedExperiments, true));
        result.sharedFiles.addAll(sharedFiles);

        result.publicProjects.addAll(toProjectStructure(user, publicProjects, true));
        result.publicExperiments.addAll(toExperimentStructure(user, publicExperiments, true));
        result.publicFiles.addAll(publicFiles);

        return result;
    }

    @Override
    public FolderStructure readFolderStructure(long user, Filter filter) {
        final FolderStructure result;
        if (Filter.PUBLIC.equals(filter)) {
            result = readPublicFolderStructure(user, filter);
        } else if (Filter.SHARED_WITH_ME.equals(filter)) {
            result = readSharedFolderStructure(user, filter);
        } else {
            final Set<ProjectLine> projects = readProjects(user, filter);
            final ImmutableSortedSet<ShortExperimentDashboardRecord> experiments =
                readShortExperimentsForFolderStructure(user, projects);
            final Set<UploadedFile> files = readDetailedFiles(user, filter);
            result = new FolderStructure();
            result.projects.addAll(toProjectStructure(user, projects, true));
            result.experiments.addAll(shortRecordsToExperimentStructure(user, experiments, true));
            result.files.addAll(files);
        }
        LOGGER.info(
            " + Total \n + projects: {} \n + experiments: {} \n + files: {}" + result.projects.size(),
            result.experiments.size(),
            result.files.size()
        );
        return result;
    }

    @Override
    public SortedSet<ProjectStructure> readProjectsOnlyStructure(long userId, Filter filter) {
        final Set<ProjectLine> projects = readProjects(userId, filter);
        LOGGER.info(" + Total projects: {}", projects.size());
        return ImmutableSortedSet.copyOf(toProjectStructure(userId, projects, false));
    }

    @Override
    public SortedSet<ExperimentStructure> readExperimentsOnlyStructureByProject(long userId, long projectId) {
        final SortedSet<ExperimentLine> experimentLines = readExperimentsByProject(userId, projectId);
        LOGGER.info(" + Total experiments for project ID = {}: {}", projectId, experimentLines.size());
        return ImmutableSortedSet.copyOf(toExperimentStructure(userId, experimentLines, false));
    }

    @Override
    public SortedSet<UploadedFile> readFilesStructureByExperiment(long userId, long experimentId) {
        final Collection<UploadedFile> fileItems = readExperimentDetailedFiles(userId, experimentId);
        LOGGER.info(" + Total files for experiment ID = {}: {}", experimentId, fileItems.size());
        return ImmutableSortedSet.copyOf(fileItems);
    }

    @Override
    public SortedSet<ExperimentStructure> readExperimentsOnlyStructure(long userId, Filter filter) {
        final ImmutableSortedSet<ExperimentLine> experiments = readExperimentsForFolderStructure(userId, filter);
        LOGGER.info(" + Total experiments: {}", experiments.size());
        return ImmutableSortedSet.copyOf(toExperimentStructure(userId, experiments, false));
    }

    @Override
    public SortedSet<UploadedFile> readFilesOnlyStructure(long userId, Filter filter) {
        final Set<UploadedFile> files = readDetailedFiles(userId, filter);
        LOGGER.info(" + Total files: {}", files.size());
        return ImmutableSortedSet.copyOf(files);
    }

    private FolderStructure readSharedFolderStructure(final long user, Filter filter) {
        final FolderStructure folderStructure = readProjectsTreeForFilter(user, filter);
        final Set<ExperimentStructure> nonOwnedExperiments =
            new HashSet<>(Collections2.filter(folderStructure.experiments, new Predicate<ExperimentStructure>() {
                @Override
                public boolean apply(ExperimentStructure exp) {
                    return !exp.currentUserOwner;
                }
            }));
        folderStructure.experiments.clear();
        folderStructure.experiments.addAll(nonOwnedExperiments);

        final Set<Long> sharedFileIds = new HashSet<>(fileMetaDataRepository.findAllSharedIds(user));
        final Set<UploadedFile> filteredSharedFiles =
            new HashSet<>(Sets.filter(folderStructure.files, new Predicate<UploadedFile>() {
                public boolean apply(UploadedFile input) {
                    return sharedFileIds.contains(input.id);
                }
            }));

        folderStructure.files.clear();
        folderStructure.files.addAll(filteredSharedFiles);
        return folderStructure;
    }

    private FolderStructure readPublicFolderStructure(long user, Filter filter) {
        return readProjectsTreeForFilter(user, filter);
    }

    private FolderStructure readProjectsTreeForFilter(long user, Filter filter) {
        final FolderStructure result = new FolderStructure();

        final Set<ProjectLine> projects = readProjects(user, filter);
        final Collection<ProjectStructure> convertedProjects = new TreeSet<>(toProjectStructure(user, projects, true));
        result.projects.addAll(convertedProjects);

        final Set<ExperimentStructure> alreadyConvertedExperiments = new HashSet<>();
        final Set<UploadedFile> alreadyConvertedFiles = new HashSet<>();
        for (ProjectStructure convertedProject : convertedProjects) {
            final SortedSet<ExperimentStructure> convertedExpsForProject = convertedProject.experiments;
            for (ExperimentStructure convertedExp : convertedExpsForProject) {
                alreadyConvertedExperiments.add(convertedExp);
                for (UploadedFile convertedFile : convertedExp.files) {
                    alreadyConvertedFiles.add(convertedFile);
                }
            }
        }
        result.experiments.addAll(alreadyConvertedExperiments);
        result.files.addAll(alreadyConvertedFiles);
        return result;
    }

    private ImmutableSortedSet<ExperimentLine> readExperimentsForFolderStructure(long user, Filter filter) {

        final ImmutableSortedSet.Builder<ExperimentLine> builder =
            ImmutableSortedSet.orderedBy(EXPERIMENT_LINE_COMPARATOR);
        final User actor = userRepository.findOne(user);
        final FluentIterable<ActiveProject> projects = from(projectRepository.findAllAvailable(user));

        for (ActiveProject project : projects) {
            final List<ExperimentDashboardRecord> dashboardItemsByProject =
                experimentRepository.findRecordsByProject(project.getId());
            final FluentIterable<ExperimentDashboardRecord> filteredExperiments = from(dashboardItemsByProject)
                .filter(getFilteredExperimentDashboardRecords(project, actor, filter));
            final List<ExperimentLine> list =
                transformers.transformExperimentRecords(user, filteredExperiments).toList();
            builder.addAll(Iterables.transform(list, transformers.experimentFolderStructureTransformer));
        }
        return builder.build();
    }


    private ImmutableSortedSet<ShortExperimentDashboardRecord> readShortExperimentsForFolderStructure(
        long user,
        Set<ProjectLine> projects
    ) {
        final ImmutableSortedSet.Builder<ShortExperimentDashboardRecord> builder =
            ImmutableSortedSet.orderedBy(SHORT_EXPERIMENT_RECORD_COMPARATOR);

        for (ProjectLine project : projects) {
            final List<ShortExperimentDashboardRecord> dashboardItemsByProject =
                experimentRepository.findShortRecordsByProject(project.id);
            builder.addAll(dashboardItemsByProject);
        }
        return builder.build();
    }

    private Set<UploadedFile> readDetailedFiles(long user, Filter filter) {
        FluentIterable<ActiveFileMetaData> filtered = readFilteredFiles(user, filter);
        return newHashSet(Iterables.transform(filtered, UPLOADED_FILE_ITEM_FROM_META_DATA));
    }

    private FluentIterable<ActiveFileMetaData> readFilteredFiles(long user, Filter filter) {
        final Iterable<ActiveFileMetaData> filesToFilter;
        switch (filter) {
            case MY:
                filesToFilter = fileMetaDataRepository.findAllMy(user);
                break;
            case SHARED_WITH_ME:
                filesToFilter = fileMetaDataRepository.findAllShared(user);
                break;
            case PUBLIC:
                //todo[tymchenko]: refactor code to make code more usable
                return FluentIterable.from(fileMetaDataRepository.findAllPublic());
            default:
                filesToFilter = fileMetaDataRepository.findAll();
        }
        return readFilteredFiles(user, filter, filesToFilter);
    }

    private FluentIterable<ActiveFileMetaData> readFilteredFiles(long user,
                                                                 Filter filter,
                                                                 Iterable<ActiveFileMetaData> toFilter) {
        return from(toFilter)
            .filter(filterForFile(user, filter));
    }

    private Collection<ProjectStructure> toProjectStructure(final long user,
                                                            Set<ProjectLine> projects,
                                                            final boolean readExperimentData) {

        return Collections2.transform(projects, new Function<ProjectLine, ProjectStructure>() {
            @Override
            public ProjectStructure apply(ProjectLine input) {
                final String labName = input.lab == null ? "<No Lab>" : input.lab.name;
                final ProjectStructure projectStructure =
                    new ProjectStructure(input.id, input.name, labName, input.creator, input.modified);
                if (readExperimentData) {
                    final SortedSet<ShortExperimentDashboardRecord> experimentLines =
                        readShortExperimentsForFolderStructure(user, Sets.newHashSet(input));
                    projectStructure.experiments.addAll(shortRecordsToExperimentStructure(user, experimentLines, true));
                }
                return projectStructure;
            }
        });
    }

    private Collection<ExperimentStructure> toExperimentStructure(final long user,
                                                                  SortedSet<ExperimentLine> experiments,
                                                                  final boolean readFiles) {
        return Collections2.transform(experiments, new Function<ExperimentLine, ExperimentStructure>() {
            @Override
            public ExperimentStructure apply(ExperimentLine input) {
                final ExperimentStructure experimentStructure = new ExperimentStructure(input.id,
                    input.name, input.creator, input.isOwner, input.modified, input.analyzesCount
                );
                if (readFiles) {
                    final Collection<UploadedFile> fileItems = readExperimentDetailedFiles(user, input.id);
                    experimentStructure.files.addAll(fileItems);
                }
                return experimentStructure;
            }
        });
    }

    private Collection<ExperimentStructure> shortRecordsToExperimentStructure(
        final long user,
        SortedSet<ShortExperimentDashboardRecord> experiments,
        final boolean readFiles
    ) {
        return Collections2.transform(experiments, new Function<ShortExperimentDashboardRecord, ExperimentStructure>() {
            @Override
            public ExperimentStructure apply(ShortExperimentDashboardRecord input) {
                final ExperimentStructure experimentStructure = new ExperimentStructure(input.id,
                    input.name, input.creatorEmail, input.owner == user, input.modified, input.analysisRunCount
                );
                if (readFiles) {
                    final Collection<UploadedFile> fileItems = readExperimentDetailedFiles(user, input.id);
                    experimentStructure.files.addAll(fileItems);
                }
                return experimentStructure;
            }
        });
    }

    @Override
    public Set<ExperimentLevelItem> readExperimentLevels(long userId, long experiment) {
        final ActiveExperiment ex = experimentRepository.findOne(experiment);
        final List<Factor> factors = ex.rawFiles.getFilteredFactors();
        final Set<ExperimentLevelItem> results = newHashSet();
        for (Factor f : factors) {
            final ExperimentLevelItem.ExperimentFactorItem factorItem =
                new ExperimentLevelItem.ExperimentFactorItem(f.getId(), f.getName());
            for (Level l : f.getLevels()) {
                results.add(new ExperimentLevelItem(
                    l.getId(),
                    l.getName(),
                    factorItem,
                    newHashSet(Collections2.transform(l.getSampleConditions(), new Function<SampleCondition, Long>() {
                        @Override
                        public Long apply(SampleCondition c) {
                            return c.getId();
                        }
                    }))
                ));
            }
        }
        return results;
    }


    @Override
    public ProjectSharingRequestInfo readProjectSharingRequest(long user, long accessExperimentId) {
        final ActiveExperiment requestedExperiment = checkPresence(experimentRepository.findOne(accessExperimentId));
        ProjectSharingRequestTemplate request =
            projectSharingRequestRepository.findByRequesterAndProject(user, requestedExperiment.getProject().getId());
        List<String> downloadExperimentLinks =
            request != null ? request.getDownloadExperimentLinks() : new ArrayList<String>();
        return new ProjectSharingRequestInfo(downloadExperimentLinks);
    }

    @Override
    public SortedSet<InstrumentItem> readInstrumentsWhereUserIsOperator(final long actor) {
        return instrumentReader.readInstrumentsWhereUserIsOperator(actor);
    }

    private Predicate<ActiveFileMetaData> owner(final long actor) {
        return new Predicate<ActiveFileMetaData>() {
            @Override
            public boolean apply(ActiveFileMetaData input) {
                return input.getOwner().equals(Util.USER_FROM_ID.apply(actor));
            }
        };
    }

    private Predicate<ActiveFileMetaData> filterForFile(final long actor, final Filter filter) {
        final User user = Util.USER_FROM_ID.apply(actor);
        //TODO: [stanislav.kurilin] it should be rewritten
        switch (filter) {
            case ALL:
                return ruleValidator.userHasReadPermissionsOnFilePredicate(actor);
            case MY:
                return owner(actor);
            case SHARED_WITH_ME:
                final Predicate<AbstractProject> or =
                    or(isProjectShared(user), isOwnerInProject(user));
                return and(
                    not(owner(actor)),
                    ruleValidator.filesFromMatchedProjectsPredicate(or),
                    not(filterForFile(actor, Filter.PUBLIC))
                );
            case PUBLIC:
                return ruleValidator.filesFromMatchedProjectsPredicate(isPublicProject());
            default:
                throw new AssertionError(filter);
        }
    }


    @Override
    public ImmutableSet<UserLine> readUsersByLab(long labHead, long labId) {
        if (!ruleValidator.canReadUsersInLab(labHead, labId)) {
            throw new AccessDenied("User should be LabHead to read users in lab");
        }
        final Lab lab = labRepository.findOne(labId);
        return from(userRepository.findAllUsersByLab(lab.getId()))
            .transform(transformToUserLineFunction(labId)).toSet();
    }

    private Collection<UploadedFile> readExperimentDetailedFiles(long actor, long experimentId) {
        ActiveExperiment experiment = checkPresence(experimentRepository.findOne(experimentId));
        if (!ruleValidator.isUserCanReadExperimentPredicate(actor).apply(experiment)) {
            throw new AccessDenied("User cannot read experiment files");
        }
        return from(experiment.getRawFiles().getData()).transform(UPLOADED_FILE_ITEM_FROM_RAW).toSet();
    }

    private Predicate<ExperimentDashboardRecord> getFilteredExperimentDashboardRecords(final ActiveProject project,
                                                                                       final User actor,
                                                                                       final Filter filter) {
        return new Predicate<ExperimentDashboardRecord>() {
            @Override
            public boolean apply(ExperimentDashboardRecord input) {
                switch (filter) {
                    case ALL:
                        return true;
                    case SHARED_WITH_ME:
                        return (project.getSharing().getType() == Sharing.Type.SHARED &&
                            !input.getCreator().equals(actor)) &&
                            (project.getSharing().getAllCollaborators().keySet().contains(actor) ||
                                project.getCreator().equals(actor));
                    case PUBLIC:
                        return !input.getCreator().equals(actor) &&
                            project.getSharing().getType() == Sharing.Type.PUBLIC;
                    case MY:
                        return input.getCreator().equals(actor);
                    default:
                        throw new AssertionError(filter);
                }
            }
        };
    }


    @Override
    public InstrumentModelLineTemplate readById(long actor, long modelId) {
        return instrumentModelReader.readById(actor, modelId);
    }

    @Override
    public Set<InstrumentModelLineTemplate> readByVendor(long actor, long vendorId) {
        return instrumentModelReader.readByVendor(actor, vendorId);
    }

    @Override
    public Set<InstrumentModelLineTemplate> readByStudyType(long actor, long typeId) {
        return instrumentModelReader.readByStudyType(actor, typeId);
    }

    @Override
    public Set<InstrumentModelLineTemplate> readByStudyTypeAndVendor(long actor, long typeId, long vendorId) {
        return instrumentModelReader.readByStudyTypeAndVendor(actor, typeId, vendorId);
    }

    @Override
    public PagedItem<InstrumentModelLineTemplate> readInstrumentModels(long actor, PagedItemInfo pagedItem) {
        return instrumentModelReader.readInstrumentModels(actor, pagedItem);
    }

    @Override
    public StringBuffer readFileCompoundsAsCSV(long actor, Set<Long> files) {
        final List<ActiveFileMetaData> filesMetadata = fileMetaDataRepository.findAll(files);
        return readCompounds(actor, filesMetadata);
    }

    @Override
    public StringBuffer readRawFileCompoundsAsCSV(long actor, Set<Long> rawFiles) {
        if (rawFiles.isEmpty()) {
            return new StringBuffer();
        }

        final List<ActiveFileMetaData> files = fileMetaDataRepository.findAllByRawFileId(rawFiles);
        return readCompounds(actor, files);
    }

    private StringBuffer readCompounds(long actor, List<ActiveFileMetaData> filesMetadata) {
        StringBuffer stringBuffer = new StringBuffer();
        for (ActiveFileMetaData file : filesMetadata) {
            if (!ruleValidator.userHasReadPermissionsOnFile(actor, file.getId())) {
                throw new RuntimeException("No permissions for file " + file);
            }

            final Set<com.infoclinika.mssharing.model.internal.FileCompound> fileCompounds =
                file.getExternalMetadata().getFileCompounds();
            fileCompounds.forEach(compound -> {
                stringBuffer.append(compound.getCompoundId());
                stringBuffer.append(",");
                stringBuffer.append(compound.getFormula());
                stringBuffer.append(",");
                stringBuffer.append(compound.getWeight());
                stringBuffer.append("\n");
            });
        }

        return stringBuffer;
    }

    private static Function<User, UserLine> transformToUserLineFunction(final long lab) {
        return new Function<User, UserLine>() {
            @Override
            public UserLine apply(User input) {
                boolean labHead = false;
                for (Lab lab1 : input.getLabs()) {
                    if (lab1.getId().equals(lab) && lab1.getHead().equals(input)) {
                        labHead = true;
                    }
                }
                return new UserLine(
                    input.getId(),
                    input.getEmail(),
                    input.getFirstName(),
                    input.getLastName(),
                    labHead
                );
            }
        };
    }

    @Override
    public PagedItem<FileLine> filterPageableFile(long actor, Filter filter, PaginationItems.PagedItemInfo pagedInfo) {
        return fileReader.filterPageableFile(actor, filter, pagedInfo);
    }
}
