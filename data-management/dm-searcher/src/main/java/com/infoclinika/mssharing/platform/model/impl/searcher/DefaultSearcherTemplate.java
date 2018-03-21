package com.infoclinika.mssharing.platform.model.impl.searcher;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.infoclinika.mssharing.platform.entity.InstrumentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.SearcherTemplate;
import com.infoclinika.mssharing.platform.model.helper.PagedItemsTransformerTemplate;
import com.infoclinika.mssharing.platform.model.read.ExperimentReaderTemplate.ExperimentLineTemplate;
import com.infoclinika.mssharing.platform.model.read.FileReaderTemplate.FileLineTemplate;
import com.infoclinika.mssharing.platform.model.read.InstrumentReaderTemplate.InstrumentLineTemplate;
import com.infoclinika.mssharing.platform.model.read.ProjectReaderTemplate.ProjectLineTemplate;
import com.infoclinika.mssharing.platform.repository.ExperimentRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.FileRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.InstrumentRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.InstrumentRepositoryTemplate.AccessedInstrument;
import com.infoclinika.mssharing.platform.repository.ProjectRepositoryTemplate;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

import static com.google.common.collect.FluentIterable.from;
import static com.infoclinika.mssharing.platform.model.DefaultTransformers.toPagedItem;

/**
 * @author Herman Zamula
 */
@Transactional(readOnly = true)
public abstract class DefaultSearcherTemplate<
        PROJECT extends ProjectTemplate,
        EXPERIMENT extends ExperimentTemplate,
        FILE extends FileMetaDataTemplate,
        INSTRUMENT extends InstrumentTemplate,
        PROJECT_LINE extends ProjectLineTemplate,
        EXPERIMENT_LINE extends ExperimentLineTemplate,
        FILE_LINE extends FileLineTemplate,
        INSTRUMENT_LINE extends InstrumentLineTemplate
        > implements SearcherTemplate<PROJECT_LINE, EXPERIMENT_LINE, FILE_LINE, INSTRUMENT_LINE> {

    private final Function<PROJECT, PROJECT_LINE> projectTransformer = new Function<PROJECT, PROJECT_LINE>() {
        @Override
        public PROJECT_LINE apply(PROJECT input) {
            return transformProject(input);
        }
    };
    private final Function<AccessedInstrument<INSTRUMENT>, INSTRUMENT_LINE> instrumentTransformer = new Function<AccessedInstrument<INSTRUMENT>, INSTRUMENT_LINE>() {
        @Override
        public INSTRUMENT_LINE apply(AccessedInstrument<INSTRUMENT> input) {
            return transformInstrument(input);
        }
    };
    private final Function<EXPERIMENT, EXPERIMENT_LINE> experimentTransformer = new Function<EXPERIMENT, EXPERIMENT_LINE>() {
        @Override
        public EXPERIMENT_LINE apply(EXPERIMENT input) {
            return transformExperiment(input);
        }
    };
    @Inject
    protected ProjectRepositoryTemplate<PROJECT> projectRepository;
    @Inject
    protected FileRepositoryTemplate<FILE> fileMetaDataRepository;
    @Inject
    protected ExperimentRepositoryTemplate<EXPERIMENT> experimentRepository;
    @Inject
    protected InstrumentRepositoryTemplate<INSTRUMENT> instrumentRepository;
    @Inject
    protected PagedItemsTransformerTemplate pagedItemsTransformer;
    private Function<FILE, FILE_LINE> fileTransformer = new Function<FILE, FILE_LINE>() {
        @Override
        public FILE_LINE apply(FILE input) {
            return transformFile(input);
        }
    };

    @Override
    public ImmutableList<PROJECT_LINE> projects(long actor, String query) {

        final List<PROJECT> projects = projectRepository.searchProjects(actor, transformQueryString(query));

        return afterReadProjects(projects, actor, query);

    }

    @Override
    public ImmutableList<EXPERIMENT_LINE> experiments(long actor, String query) {

        final List<EXPERIMENT> experiments = experimentRepository.searchExperiments(actor, transformQueryString(query));

        return afterReadExperiments(experiments, actor, query);

    }

    @Override
    public ImmutableList<INSTRUMENT_LINE> instruments(long actor, String query) {

        final List<AccessedInstrument<INSTRUMENT>> instruments = instrumentRepository.searchInstrumentsAccessed(actor, transformQueryString(query));

        return afterReadInstruments(instruments, actor, query);

    }

    @Override
    public ImmutableList<FILE_LINE> files(long actor, String query) {

        final List<FILE> files = fileMetaDataRepository.searchFiles(actor, transformQueryString(query));

        return afterReadFiles(files, actor, query);

    }

    @Override
    public PagedItem<PROJECT_LINE> pagedProjects(long actor, PagedItemInfo pagedItemInfo) {

        final Page<PROJECT> itemsPage = projectRepository.searchPagedProjects(
                actor,
                transformQueryString(pagedItemInfo.filterQuery),
                pagedItemsTransformer.toPageRequest(ProjectTemplate.class, pagedItemInfo)
        );

        return afterReadProjectsPage(itemsPage, actor, pagedItemInfo);

    }

    @Override
    public PagedItem<PROJECT_LINE> pagedProjectsWithId(long actor, PagedItemInfo pagedItemInfo) {

        final Page<PROJECT> itemsPage = projectRepository.searchPagedProjectsWithId(
                actor,
                transformQueryString(pagedItemInfo.filterQuery),
                pagedItemsTransformer.toPageRequest(ProjectTemplate.class, pagedItemInfo)
        );

        return afterReadProjectsPage(itemsPage, actor, pagedItemInfo);

    }

    @Override
    public PagedItem<EXPERIMENT_LINE> pagedExperiments(long actor, PagedItemInfo pagedItemInfo) {

        final Page<EXPERIMENT> itemsPage = experimentRepository.searchPagedExperiments(
                actor,
                transformQueryString(pagedItemInfo.filterQuery),
                pagedItemsTransformer.toPageRequest(ExperimentTemplate.class, pagedItemInfo)
        );

        return afterReadExperimentsPage(itemsPage, actor, pagedItemInfo);

    }

    @Override
    public PagedItem<FILE_LINE> pagedFiles(long actor, PagedItemInfo pagedItemInfo) {

        final Page<FILE> itemsPage = fileMetaDataRepository.searchPagedFiles(
                actor,
                transformQueryString(pagedItemInfo.filterQuery),
                pagedItemsTransformer.toPageRequest(FileMetaDataTemplate.class, pagedItemInfo)
        );

        return afterReadFilesPage(itemsPage, actor, pagedItemInfo);

    }

    @Override
    public PagedItem<INSTRUMENT_LINE> pagedInstruments(long actor, PagedItemInfo pagedItemInfo) {

        final Page<AccessedInstrument<INSTRUMENT>> itemsPage = instrumentRepository.searchPagedInstrumentsAccessed(
                actor,
                transformQueryString(pagedItemInfo.filterQuery),
                pagedItemsTransformer.toPageRequest(InstrumentTemplate.class, pagedItemInfo)
        );

        return afterReadInstrumentsPage(itemsPage, actor, pagedItemInfo);

    }

    @SuppressWarnings("unused")
    protected ImmutableList<PROJECT_LINE> afterReadProjects(List<PROJECT> projects, long actor, String query) {

        return from(projects)
                .transform(projectTransformer)
                .toList();

    }

    @SuppressWarnings("unused")
    protected ImmutableList<EXPERIMENT_LINE> afterReadExperiments(List<EXPERIMENT> experiments, long actor, String query) {

        return from(experiments)
                .transform(experimentTransformer)
                .toList();

    }

    @SuppressWarnings("unused")
    protected ImmutableList<FILE_LINE> afterReadFiles(List<FILE> files, long actor, String query) {

        return from(files)
                .transform(fileTransformer)
                .toList();

    }

    @SuppressWarnings("unused")
    protected ImmutableList<INSTRUMENT_LINE> afterReadInstruments(List<AccessedInstrument<INSTRUMENT>> instruments, long actor, String query) {

        return from(instruments)
                .transform(instrumentTransformer)
                .toList();
    }

    @SuppressWarnings("unused")
    protected PagedItem<PROJECT_LINE> afterReadProjectsPage(Page<PROJECT> itemsPage, long actor, PagedItemInfo pagedItemInfo) {

        return toPagedItem(itemsPage, projectTransformer);

    }

    @SuppressWarnings("unused")
    protected PagedItem<EXPERIMENT_LINE> afterReadExperimentsPage(Page<EXPERIMENT> itemsPage, long actor, PagedItemInfo pagedItemInfo) {

        return toPagedItem(itemsPage, experimentTransformer);

    }

    @SuppressWarnings("unused")
    protected PagedItem<FILE_LINE> afterReadFilesPage(Page<FILE> itemsPage, long actor, PagedItemInfo pagedItemInfo) {

        return toPagedItem(itemsPage, fileTransformer);

    }

    @SuppressWarnings("unused")
    protected PagedItem<INSTRUMENT_LINE> afterReadInstrumentsPage(Page<AccessedInstrument<INSTRUMENT>> itemsPage, long actor, PagedItemInfo pagedItemInfo) {

        return toPagedItem(itemsPage, instrumentTransformer);

    }

    @Override
    public Count getItemsCount(PagedItemInfo pagedItemInfo, long actor) {

        final String queryString = transformQueryString(pagedItemInfo.filterQuery);

        final long instruments = instrumentRepository.searchInstrumentsCount(actor, queryString);
        final long experiments = experimentRepository.searchExperimentsCount(actor, queryString);
        final long files = fileMetaDataRepository.searchFilesCount(actor, queryString);
        final long projects = projectRepository.searchProjectsCount(actor, queryString);

        return new Count(instruments, projects, files, experiments);

    }

    protected String transformQueryString(String query) {

        return "%" + query.toLowerCase() + "%";

    }

    protected abstract PROJECT_LINE transformProject(PROJECT project);

    protected abstract FILE_LINE transformFile(FILE file);

    protected abstract EXPERIMENT_LINE transformExperiment(EXPERIMENT experiment);

    protected abstract INSTRUMENT_LINE transformInstrument(AccessedInstrument<INSTRUMENT> instrument);
}
