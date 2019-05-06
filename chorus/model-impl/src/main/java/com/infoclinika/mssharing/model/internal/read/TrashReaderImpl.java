package com.infoclinika.mssharing.model.internal.read;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.entity.restorable.DeletedExperiment;
import com.infoclinika.mssharing.model.internal.entity.restorable.DeletedFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.restorable.DeletedProject;
import com.infoclinika.mssharing.model.internal.repository.DeletedExperimentRepository;
import com.infoclinika.mssharing.model.internal.repository.DeletedFileMetaDataRepository;
import com.infoclinika.mssharing.model.internal.repository.DeletedItem;
import com.infoclinika.mssharing.model.internal.repository.DeletedProjectRepository;
import com.infoclinika.mssharing.model.read.TrashReader;
import com.infoclinika.mssharing.model.write.LabHeadManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Elena Kurilina
 */
@Service
public class TrashReaderImpl implements TrashReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrashReaderImpl.class);

    @Inject
    private DeletedFileMetaDataRepository deletedFileRepository;
    @Inject
    private DeletedExperimentRepository deletedExperimentRepository;
    @Inject
    private DeletedProjectRepository deletedProjectRepository;
    @Inject
    private LabHeadManagement labHeadManagement;
    @Inject
    private RuleValidator ruleValidator;

    @Override
    public ImmutableSet<TrashLine> readByOwner(long actor) {
        Set<TrashLine> deletedFileMetaDatas = convertToTrashLine(deletedFileRepository.findByOwner(actor));
        Set<TrashLine> deletedExperiments = convertToTrashLine(deletedExperimentRepository.findByOwner(actor));
        Set<TrashLine> deletedProjects = convertToTrashLine(deletedProjectRepository.findByOwner(actor));
        Set<TrashLine> all = getTrashLines(deletedFileMetaDatas, deletedExperiments, deletedProjects);

        return ImmutableSet.copyOf(all);
    }

    private Set<TrashLine> getTrashLines(
        Set<TrashLine> deletedFileMetaDatas,
        Set<TrashLine> deletedExperiments,
        Set<TrashLine> deletedProjects
    ) {
        Set<TrashLine> all = new HashSet<TrashLine>();
        all.addAll(deletedFileMetaDatas);
        all.addAll(deletedExperiments);
        all.addAll(deletedProjects);
        return all;
    }

    @Override
    public ImmutableSet<TrashLine> readByOwnerOrLabHead(long actor) {
        final Collection<Long> labs = labHeadManagement.findLabsForLabHead(actor);
        Set<TrashLine> all;

        if (!labs.isEmpty()) {
            LOGGER.debug("Trash was read for lab head: {}", actor);
            Set<TrashLine> deletedFileMetaDatas = convertToTrashLine(deletedFileRepository.findByLabs(labs));
            Set<TrashLine> deletedExperiments = convertToTrashLine(deletedExperimentRepository.findByLabs(labs));
            Set<TrashLine> deletedProjects = convertToTrashLine(deletedProjectRepository.findByLabs(labs));
            all = getTrashLines(deletedFileMetaDatas, deletedExperiments, deletedProjects);
        } else {
            LOGGER.debug("Trash was read for user {}", actor);
            all = readByOwner(actor);
        }

        return ImmutableSet.copyOf(all);
    }

    @Override
    public ImmutableSet<TrashLineShort> readNotRestorableProjects(long actor, List<Long> projectIds) {
        Set<TrashLineShort> notRestorableProjects = new HashSet<>();
        for (Long projectId : projectIds) {
            DeletedProject project = deletedProjectRepository.findOne(projectId);
            if (ruleValidator.projectHasDuplicateNames(actor, project)) {
                notRestorableProjects.add(new TrashLineShort(project.getId(), project.getName()));
            }
        }
        return ImmutableSet.copyOf(notRestorableProjects);
    }

    @Override
    public ImmutableSet<TrashLineShort> readNotRestorableExperiments(long actor, List<Long> experimentIds) {
        Set<TrashLineShort> notRestorableExperiments = new HashSet<>();
        for (Long experimentId : experimentIds) {
            DeletedExperiment experiment = deletedExperimentRepository.findOne(experimentId);
            if (ruleValidator.experimentHasDuplicateNames(actor, experiment)) {
                notRestorableExperiments.add(new TrashLineShort(experiment.getId(), experiment.getName()));
            }
        }
        return ImmutableSet.copyOf(notRestorableExperiments);
    }

    @Override
    public ImmutableSet<TrashLineShort> readNotRestorableFiles(long actor, List<Long> fileIds) {
        Set<TrashLineShort> notRestorableFiles = new HashSet<>();
        for (Long fileId : fileIds) {
            DeletedFileMetaData file = deletedFileRepository.findOne(fileId);
            if (ruleValidator.fileHasDuplicateName(file)) {
                notRestorableFiles.add(new TrashLineShort(file.getId(), file.getName()));
            }
        }
        return ImmutableSet.copyOf(notRestorableFiles);
    }

    private Set<TrashLine> convertToTrashLine(Collection<DeletedItem> deletedItems) {
        Set<TrashLine> trashLineSet = new HashSet<>();
        for (DeletedItem data : deletedItems) {
            trashLineSet.add(trashLineTransformer.apply(data));
        }
        return trashLineSet;
    }

    private final Function<DeletedItem, TrashLine> trashLineTransformer =
        new Function<DeletedItem, TrashLine>() {
            @Override
            public TrashLine apply(DeletedItem item) {
                return new TrashLine(item.deletionDate, item.labName, item.title, item.id, item.type);
            }
        };

}
