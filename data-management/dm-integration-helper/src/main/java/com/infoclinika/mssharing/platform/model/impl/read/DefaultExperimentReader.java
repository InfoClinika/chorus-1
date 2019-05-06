package com.infoclinika.mssharing.platform.model.impl.read;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.helper.PagedItemsTransformerTemplate;
import com.infoclinika.mssharing.platform.model.helper.read.ExperimentReaderHelper;
import com.infoclinika.mssharing.platform.model.impl.DefaultTransformingTemplate;
import com.infoclinika.mssharing.platform.model.read.ExperimentReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.ExperimentReaderTemplate.ExperimentLineTemplate;
import com.infoclinika.mssharing.platform.model.read.Filter;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Comparator;
import java.util.SortedSet;

/**
 * @author : Alexander Serebriyan, Herman Zamula
 */
@Transactional(readOnly = true)
public abstract class DefaultExperimentReader<EXPERIMENT extends ExperimentTemplate,
    EXPERIMENT_LINE extends ExperimentLineTemplate>
    implements DefaultTransformingTemplate<EXPERIMENT, EXPERIMENT_LINE>,
    ExperimentReaderTemplate<EXPERIMENT_LINE> {

    @Inject
    protected ExperimentReaderHelper<EXPERIMENT, EXPERIMENT_LINE> experimentReaderHelper;
    @Inject
    protected PagedItemsTransformerTemplate pagedItemsTransformer;

    @PostConstruct
    private void setup() {
        experimentReaderHelper.setTransformer(new Function<EXPERIMENT, EXPERIMENT_LINE>() {
            @Nullable
            @Override
            public EXPERIMENT_LINE apply(EXPERIMENT input) {
                return transform(input);
            }
        });
    }

    protected Comparator<EXPERIMENT_LINE> comparator() {
        return Comparator.comparing(o -> o.name);
    }

    @Override
    public SortedSet<EXPERIMENT_LINE> readExperiments(long actor, Filter filter) {

        return experimentReaderHelper
            .byFilter(actor, filter)
            .transform()
            .toSortedSet(comparator());
    }

    @Override
    public PagedItem<EXPERIMENT_LINE> readExperiments(long actor, Filter filter, PagedItemInfo pagedItemInfo) {

        final PageRequest request = toPageRequest(pagedItemInfo);
        final String filterQuery = toFilterQuery(pagedItemInfo);

        return experimentReaderHelper
            .pageableByFilter(actor, filter, request, filterQuery)
            .transform();
    }

    @Override
    public SortedSet<EXPERIMENT_LINE> readExperimentsByProject(long actor, long projectId) {

        return experimentReaderHelper
            .byProject(projectId)
            .transform()
            .toSortedSet(comparator());
    }

    @Override
    public PagedItem<EXPERIMENT_LINE> readExperimentsByLab(long actor, long labId, PagedItemInfo pagedItemInfo) {

        final PageRequest request = toPageRequest(pagedItemInfo);
        final String filterQuery = toFilterQuery(pagedItemInfo);

        return experimentReaderHelper
            .pageableByLab(labId, request, filterQuery)
            .transform();
    }

    @Override
    public PagedItem<EXPERIMENT_LINE> readPagedExperimentsByProject(long actor,
                                                                    long projectId,
                                                                    PagedItemInfo pageInfo) {

        final PageRequest request = toPageRequest(pageInfo);
        final String filterQuery = toFilterQuery(pageInfo);

        return experimentReaderHelper
            .pageableByProject(projectId, request, filterQuery)
            .transform();
    }

    private PageRequest toPageRequest(PagedItemInfo pagedItemInfo) {
        return PagedItemsTransformerTemplate.toPageRequest(ExperimentTemplate.class, pagedItemInfo);
    }

    private String toFilterQuery(PagedItemInfo pagedItemInfo) {
        return pagedItemsTransformer.toFilterQuery(pagedItemInfo);
    }

}
