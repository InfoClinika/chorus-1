package com.infoclinika.mssharing.platform.model.helper.read;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.platform.entity.InstrumentModel;
import com.infoclinika.mssharing.platform.entity.InstrumentTemplate;
import com.infoclinika.mssharing.platform.entity.LabTemplate;
import com.infoclinika.mssharing.platform.entity.Sharing;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.model.read.AccessLevel;
import com.infoclinika.mssharing.platform.model.read.Filter;
import com.infoclinika.mssharing.platform.repository.FileProjectUsage;
import com.infoclinika.mssharing.platform.repository.FileRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.InstrumentRepositoryTemplate;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.FluentIterable.from;
import static com.infoclinika.mssharing.platform.entity.EntityUtil.ENTITY_TO_ID;
import static com.infoclinika.mssharing.platform.model.read.FileReaderTemplate.FileLineTemplate;


/**
 * @author Herman Zamula
 */
@Scope("prototype")
@Component
public class FileReaderHelper<FILE extends FileMetaDataTemplate, FILE_LINE extends FileLineTemplate> extends AbstractReaderHelper<FILE, FILE_LINE, FileLineTemplate> {

    @Inject
    private FileRepositoryTemplate<FILE> fileMetaDataRepository;
    @Inject
    private InstrumentRepositoryTemplate<InstrumentTemplate> instrumentRepository;

    public FileReaderHelper() {
    }

    public static String toFullInstrumentModel(InstrumentModel model) {
        return Joiner.on(" ").join(new String[]{model.getVendor().getName(), model.getType().getName(), model.getName()});
    }

    @Override
    public Function<FILE, FileLineTemplate> getDefaultTransformer() {
        return new Function<FILE, FileLineTemplate>() {
            @Override
            public FileLineTemplate apply(FILE input) {
                final InstrumentTemplate instrument = input.getInstrument();
                final LabTemplate lab = instrument.getLab();
                final InstrumentModel model = instrument.getModel();

                return new FileLineTemplate(input.getId(), input.getName(),
                        input.getContentId(),
                        input.getUploadId(),
                        input.getDestinationPath(),
                        instrument.getId(),
                        lab.getId(),
                        instrument.getName(),
                        model.getId(),
                        lab.getName(),
                        input.getOwner().getId(),
                        lab.getHead().getId(),
                        input.isInvalid(),
                        model.getVendor().getName(),
                        toFullInstrumentModel(model),
                        fromNullable(input.getSpecie()).transform(ENTITY_TO_ID).orNull(),
                        transformFileAccessLevel(input),
                        !fileMetaDataRepository.findFileExperimentUsage(input.getId()).isEmpty(),
                        input.getLabels(),
                        input.getSizeInBytes(),
                        input.getUploadDate());
            }
        };
    }

    public PagedResultBuilder<FILE, FILE_LINE> filesByFilter(long user, Filter filter, Pageable pageable, String query) {
        Page<FILE> result;
        switch (filter) {
            case ALL:
                result = fileMetaDataRepository.findAllAvailable(user, query, pageable);
                break;
            case SHARED_WITH_ME:
                result = fileMetaDataRepository.findShared(user, query, pageable);
                break;
            case MY:
                result = fileMetaDataRepository.findMy(user, query, pageable);
                break;
            case PUBLIC:
                result = fileMetaDataRepository.findPublic(query, pageable);
                break;
            default:
                throw new AssertionError(filter);
        }
        return new PagedResultBuilder<>(result, activeTransformer);
    }

    public PagedResultBuilder<FILE, FILE_LINE> filesByInstrument(long actor, long instrument, Pageable pageable, String query) {
        return PagedResultBuilder.builder(fileMetaDataRepository.findByInstrument(instrument,
                actor, pageable, query), activeTransformer);

    }

    public PagedResultBuilder<FILE, FILE_LINE> filesByLab(long actor, long labId, Pageable pageable, String query) {
        final Page<FILE> byLab = fileMetaDataRepository.findByLab(labId, actor, pageable, query);
        return PagedResultBuilder.builder(byLab, activeTransformer);

    }

    public ResultBuilder<FILE, FILE_LINE> filesByFilter(long user, Filter filter, Predicate<FILE> predicate) {
        final Iterable<FILE> filesToFilter;
        switch (filter) {
            case ALL:
                filesToFilter = fileMetaDataRepository.findAllAvailable(user);
                break;
            case MY:
                filesToFilter = fileMetaDataRepository.findAllMy(user);
                break;
            case SHARED_WITH_ME:
                filesToFilter = fileMetaDataRepository.findAllShared(user);
                break;
            case PUBLIC:
                return ResultBuilder.builder(fileMetaDataRepository.findAllPublic(), activeTransformer);
            default:
                filesToFilter = fileMetaDataRepository.findAll();
        }
        return ResultBuilder.builder(from(filesToFilter).filter(predicate), activeTransformer);
    }

    public ResultBuilder<FILE, FILE_LINE> filesByFilter(long user, Filter filter) {
        final Iterable<FILE> filesToFilter;
        switch (filter) {
            case ALL:
                filesToFilter = fileMetaDataRepository.findAllAvailable(user);
                break;
            case MY:
                filesToFilter = fileMetaDataRepository.findAllMy(user);
                break;
            case SHARED_WITH_ME:
                filesToFilter = fileMetaDataRepository.findAllShared(user);
                break;
            case PUBLIC:
                return ResultBuilder.builder(fileMetaDataRepository.findAllPublic(), activeTransformer);
            default:
                filesToFilter = fileMetaDataRepository.findAll();
        }
        return ResultBuilder.builder(filesToFilter, activeTransformer);
    }

    /**
     * @see #readFilesByInstrument(long, long)
     */
    @Deprecated
    public ResultBuilder<FILE, FILE_LINE> readFilesByInstrument(long instrumentId) {
        List<FILE> files = filesByInstrument(instrumentId);
        return ResultBuilder.builder(files, activeTransformer);
    }

    public ResultBuilder<FILE, FILE_LINE> readFilesByInstrument(long actor, long instrumentId) {
        List<FILE> files = filesByInstrument(actor, instrumentId);
        return ResultBuilder.builder(files, activeTransformer);
    }

    @Deprecated
    private List<FILE> filesByInstrument(long instrumentId) {
        return fileMetaDataRepository.byInstrument(instrumentId);
    }

    private List<FILE> filesByInstrument(long actor, long instrumentId) {
        return fileMetaDataRepository.findByInstrument(actor, instrumentId);
    }

    public ResultBuilder<FILE, FILE_LINE> readByNameForInstrument(long actor, long instrumentId, String fileName) {
        List<FILE> files = fileMetaDataRepository.findByNameForInstrument(actor, instrumentId, fileName);
        return ResultBuilder.builder(files, activeTransformer);
    }

    /**
     * @see #readFilesByLab(long, long)
     */
    @Deprecated
    public ResultBuilder<FILE, FILE_LINE> readFilesByLab(long labId) {
        ImmutableSet.Builder<FILE> builder = ImmutableSet.builder();
        List<InstrumentTemplate> instruments = instrumentRepository.findByLab(labId);
        for (InstrumentTemplate instrument : instruments) {
            builder.addAll(filesByInstrument(instrument.getId()));
        }
        return ResultBuilder.builder(builder.build(), activeTransformer);
    }

    public ResultBuilder<FILE, FILE_LINE> readFilesByLab(long actor, long labId) {
        ImmutableSet.Builder<FILE> builder = ImmutableSet.builder();
        List<InstrumentTemplate> instruments = instrumentRepository.findByLab(labId);
        for (InstrumentTemplate instrument : instruments) {
            builder.addAll(filesByInstrument(actor, instrument.getId()));
        }
        return ResultBuilder.builder(builder.build(), activeTransformer);
    }

    public ResultBuilder<FILE, FILE_LINE> readUnfinishedFilesByUser(long userId) {
        List<FILE> files = fileMetaDataRepository.unfinishedByUser(userId);
        return ResultBuilder.builder(files, activeTransformer);
    }

    public PagedResultBuilder<FILE, FILE_LINE> filesByExperiment(long actor, long experiment, Pageable request, String filter) {
        Page<FILE> byExperiment = fileMetaDataRepository.findByExperiment(experiment, request, filter);
        return PagedResultBuilder.builder(byExperiment, activeTransformer);
    }

    public ResultBuilder<FILE, FILE_LINE> filesByExperiment(long actor, long experiment) {
        List<FILE> byExperiment = fileMetaDataRepository.findByExperiment(experiment);
        return ResultBuilder.builder(byExperiment, activeTransformer);
    }

    public AccessLevel transformFileAccessLevel(FILE input) {

        final List<FileProjectUsage> projects = fileMetaDataRepository.findFileProjectUsage(input.getId());

        if (includedToProjectsOfType(projects, Sharing.Type.PUBLIC)) {
            return AccessLevel.PUBLIC;
        }
        if (includedToProjectsOfType(projects, Sharing.Type.SHARED)) {
            return AccessLevel.SHARED;
        }
        return AccessLevel.PRIVATE;
    }

    private boolean includedToProjectsOfType(List<FileProjectUsage> projectsSharing, final Sharing.Type type) {

        return from(projectsSharing)
                .firstMatch(new Predicate<FileProjectUsage>() {
                    @Override
                    public boolean apply(FileProjectUsage input) {
                        return input.sharingType.equals(type);
                    }
                })
                .isPresent();
    }
}
