package com.infoclinika.mssharing.platform.model.impl.read;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.ExperimentFileTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.RuleValidator;
import com.infoclinika.mssharing.platform.model.TransformersTemplate;
import com.infoclinika.mssharing.platform.model.common.items.FileItem;
import com.infoclinika.mssharing.platform.model.helper.PagedItemsTransformerTemplate;
import com.infoclinika.mssharing.platform.model.helper.read.FileReaderHelper;
import com.infoclinika.mssharing.platform.model.impl.DefaultTransformingTemplate;
import com.infoclinika.mssharing.platform.model.read.FileReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.Filter;
import com.infoclinika.mssharing.platform.repository.ExperimentRepositoryTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;

import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkAccess;
import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkPresence;
import static java.lang.String.format;

/**
 * @author Herman Zamula
 */
@Transactional(readOnly = true)
public abstract class DefaultFileReader<FILE extends FileMetaDataTemplate, LINE extends FileReaderTemplate.FileLineTemplate>
        implements FileReaderTemplate<LINE>, DefaultTransformingTemplate<FILE, LINE> {

    @Inject
    protected FileReaderHelper<FILE, LINE> fileReaderHelper;
    @Inject
    protected ExperimentRepositoryTemplate<?> experimentRepositoryTemplate;
    @Inject
    protected PagedItemsTransformerTemplate pagedItemsTransformer;
    @Inject
    protected RuleValidator ruleValidator;
    @Inject
    protected TransformersTemplate transformers;

    @PostConstruct
    private void init() {
        fileReaderHelper.setTransformer(new Function<FILE, LINE>() {
            @Override
            public LINE apply(FILE input) {
                return transform(input);
            }
        });
    }

    @Override
    public Set<LINE> readFiles(long actor, Filter genericFilter) {

        return fileReaderHelper.filesByFilter(actor, genericFilter)
                .transform()
                .toSortedSet(fileComparator());
    }

    @Override
    public PagedItem<LINE> readFiles(long actor, Filter filter, PagedItemInfo pagedItemInfo) {

        PageRequest pageRequest = PagedItemsTransformerTemplate.toPageRequest(FileMetaDataTemplate.class, pagedItemInfo);

        return fileReaderHelper
                .filesByFilter(actor, filter, pageRequest, pagedItemInfo.toFilterQuery())
                .transform();
    }

    @Override
    public PagedItem<LINE> readFilesByLab(long userId, long labId, PagedItemInfo pagedItemInfo) {

        PageRequest pageRequest = PagedItemsTransformerTemplate.toPageRequest(FileMetaDataTemplate.class, pagedItemInfo);

        return fileReaderHelper
                .filesByLab(userId, labId, pageRequest, pagedItemInfo.toFilterQuery())
                .transform();
    }

    @Override
    public PagedItem<LINE> readFilesByInstrument(long actor, long instrument, PagedItemInfo pagedItemInfo) {

        PageRequest pageRequest = PagedItemsTransformerTemplate.toPageRequest(FileMetaDataTemplate.class, pagedItemInfo);

        return fileReaderHelper
                .filesByInstrument(actor, instrument, pageRequest, pagedItemInfo.toFilterQuery())
                .transform();
    }

    @Override
    public PagedItem<LINE> readFilesByExperiment(long actor, long experiment, PagedItemInfo pagedInfo) {

        beforeReadFilesByExperiment(actor, experiment);

        Pageable request = PagedItemsTransformerTemplate.toPageRequest(ExperimentFileTemplate.class, pagedInfo);

        return fileReaderHelper
                .filesByExperiment(actor, experiment, request, pagedInfo.toFilterQuery())
                .transform();
    }

    @Override
    public Set<LINE> readUnfinishedFiles(long user) {

        return fileReaderHelper
                .readUnfinishedFilesByUser(user)
                .transform()
                .toSortedSet(fileComparator());
    }

    @Override
    public Set<LINE> readFilesByInstrument(final long actor, long instrument) {

        return fileReaderHelper
                .readFilesByInstrument(actor, instrument)
                .transform()
                .toSortedSet(fileComparator());
    }

    @Override
    public Set<LINE> readByNameForInstrument(final long actor, long instrument, String fileName) {
        return fileReaderHelper
                .readByNameForInstrument(actor, instrument, fileName)
                .transform()
                .toSortedSet(fileComparator());
    }

    @Override
    public Set<LINE> readFilesByLab(final long userId, long labId) {

        return fileReaderHelper
                .readFilesByLab(userId, labId)
                .transform()
                .toSortedSet(fileComparator());
    }

    @Override
    public Set<LINE> readFilesByExperiment(long actor, long experiment) {

        beforeReadFilesByExperiment(actor, experiment);

        return fileReaderHelper
                .filesByExperiment(actor, experiment)
                .transform()
                .toSortedSet(fileComparator());
    }

    @Override
    public SortedSet<FileItem> readFileItemsByExperiment(long actor, long experiment) {

        beforeReadFilesByExperiment(actor, experiment);

        return fileReaderHelper
                .filesByExperiment(actor, experiment)
                .transform(transformers.<FILE>fileTransformer())
                .toSortedSet(transformers.dictionaryItemComparator());
    }

    protected void beforeReadFilesByExperiment(long actor, long experiment) {
        checkPresence(experimentRepositoryTemplate.findOne(experiment));
        checkAccess(ruleValidator.isUserCanReadExperiment(actor, experiment),
                format("User {%d} has no access to read experiment {%d}", actor, experiment));
    }

    protected Comparator<LINE> fileComparator() {
        return new Comparator<LINE>() {
            @Override
            public int compare(LINE o1, LINE o2) {
                if (o1.name.equals(o2.name)) {
                    return o1.hashCode() - o2.hashCode();
                }
                return o1.name.compareTo(o2.name);
            }
        };
    }

}
