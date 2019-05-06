package com.infoclinika.mssharing.platform.model.helper.read;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.model.DefaultTransformers;
import com.infoclinika.mssharing.platform.model.read.ExperimentReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.ExperimentReaderTemplate.ExperimentLineTemplate;
import com.infoclinika.mssharing.platform.model.read.Filter;
import com.infoclinika.mssharing.platform.repository.ExperimentRepositoryTemplate;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

/**
 * @author : Alexander Serebriyan
 */
@Component
@Scope(value = "prototype")
public class ExperimentReaderHelper<EXPERIMENT extends ExperimentTemplate,
    EXPERIMENT_LINE extends ExperimentReaderTemplate.ExperimentLineTemplate>
    extends AbstractReaderHelper<EXPERIMENT, EXPERIMENT_LINE, ExperimentLineTemplate> {

    @Inject
    private ExperimentRepositoryTemplate<EXPERIMENT> experimentRepository;

    public ExperimentReaderHelper() {
    }

    public ResultBuilder<EXPERIMENT, EXPERIMENT_LINE> byFilter(long actor, Filter filter) {

        final List<EXPERIMENT> experiments = getExperiments(actor, filter);
        return ResultBuilder.builder(experiments, activeTransformer);

    }

    public ResultBuilder<EXPERIMENT, EXPERIMENT_LINE> byProject(long projectId) {

        List<EXPERIMENT> experiments = experimentRepository.findByProject(projectId);
        return ResultBuilder.builder(experiments, activeTransformer);

    }

    public PagedResultBuilder<EXPERIMENT, EXPERIMENT_LINE> pageableByFilter(long actor,
                                                                            Filter filter,
                                                                            PageRequest request,
                                                                            String filterQuery) {

        final Page<EXPERIMENT> experiments = getExperimentsPage(actor, filter, request, filterQuery);
        return PagedResultBuilder.builder(experiments, activeTransformer);
    }

    public PagedResultBuilder<EXPERIMENT, EXPERIMENT_LINE> pageableByLab(long labId,
                                                                         PageRequest request,
                                                                         String filterQuery) {

        final Page<EXPERIMENT> experiments = experimentRepository.findAllByLab(labId, filterQuery, request);
        return PagedResultBuilder.builder(experiments, activeTransformer);

    }

    public PagedResultBuilder<EXPERIMENT, EXPERIMENT_LINE> pageableByProject(long projectId,
                                                                             PageRequest request,
                                                                             String filterQuery) {

        final Page<EXPERIMENT> experiments = experimentRepository.findByProject(projectId, filterQuery, request);
        return PagedResultBuilder.builder(experiments, activeTransformer);

    }

    @Override
    public Function<EXPERIMENT, ExperimentLineTemplate> getDefaultTransformer() {
        return input -> {
            UserTemplate creator = input.getCreator();
            return new ExperimentLineTemplate(
                input.getId(),
                input.getName(),
                input.getProject().getName(),
                input.getRawFiles().numberOfFiles(),
                input.getLastModification(),
                DefaultTransformers.labLineTemplateTransformer().apply(input.getLab()),
                input.getDownloadToken(),
                creator.getEmail(),
                DefaultTransformers.fromSharingType(input.getProject().getSharing().getType()),
                creator.getId(),
                input.isFailed()
            );
        };
    }

    private List<EXPERIMENT> getExperiments(long actor, Filter filter) {
        switch (filter) {
            case ALL:
                return experimentRepository.findAllAvailable(actor);
            case MY:
                return experimentRepository.findMy(actor);
            case SHARED_WITH_ME:
                return experimentRepository.findShared(actor);
            case PUBLIC:
                return experimentRepository.findPublic();
            default:
                throw new AssertionError(filter);
        }
    }

    private Page<EXPERIMENT> getExperimentsPage(long actor, Filter filter, PageRequest request, String filterQuery) {
        switch (filter) {
            case ALL:
                return experimentRepository.findAllAvailable(actor, filterQuery, request);
            case MY:
                return experimentRepository.findMy(actor, filterQuery, request);
            case SHARED_WITH_ME:
                return experimentRepository.findShared(actor, filterQuery, request);
            case PUBLIC:
                return experimentRepository.findPublic(filterQuery, request);
            default:
                throw new AssertionError(filter);
        }
    }

}
