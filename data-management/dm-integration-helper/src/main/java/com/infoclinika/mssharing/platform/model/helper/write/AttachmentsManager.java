package com.infoclinika.mssharing.platform.model.helper.write;

import com.google.common.base.Function;
import com.google.common.collect.Sets;
import com.infoclinika.mssharing.platform.entity.AbstractPersistable;
import com.infoclinika.mssharing.platform.entity.Attachment;
import com.infoclinika.mssharing.platform.entity.EntityUtil;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.fileserver.StorageService;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;
import com.infoclinika.mssharing.platform.model.EntityFactories;
import com.infoclinika.mssharing.platform.repository.AttachmentRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.ExperimentRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.ProjectRepositoryTemplate;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

/**
 * @author Herman Zamula
 */
@Component
public class AttachmentsManager<ATTACHMENT extends Attachment> {

    public static final String WHITE_SPACE_REPLACEMENT = "_";

    @Inject
    private AttachmentRepositoryTemplate<ATTACHMENT> attachmentRepository;
    @Inject
    private ExperimentRepositoryTemplate<ExperimentTemplate> experimentRepository;
    @Inject
    private ProjectRepositoryTemplate<ProjectTemplate> projectRepository;
    @Inject
    private StorageService fileStorageService;
    @Inject
    private EntityFactories entityFactories;

    private Function<Long, ATTACHMENT> attachmentFromId = new Function<Long, ATTACHMENT>() {
        @Override
        public ATTACHMENT apply(Long input) {
            return attachmentRepository.findOne(input);
        }
    };


    @SuppressWarnings("unchecked")
    public ATTACHMENT createAttachment(Long actor, String fileName, long sizeInBytes) {

        final UserTemplate creator = entityFactories.userFromId.apply(actor);
        final ATTACHMENT attachment = (ATTACHMENT) entityFactories.attachment.get();
        fileName = fileName.replaceAll("\\s", WHITE_SPACE_REPLACEMENT);

        attachment.setOwner(creator);
        attachment.setName(fileName);
        attachment.setUploadDate(new Date());
        attachment.setSizeInBytes(sizeInBytes);

        return attachmentRepository.save(attachment);

    }

    public void removeAttachment(Long attachment) {

        final ATTACHMENT toDiscard = attachmentRepository.findOne(attachment);

        //todo[tymchenko]: remove the binary contents as well
        attachmentRepository.delete(toDiscard);
    }

    public ATTACHMENT copyAttachment(long originId, long actor, Function<ATTACHMENT, NodePath> nodePathFn) {
        final ATTACHMENT origin = attachmentRepository.findOne(originId);
        final ATTACHMENT copy = createAttachment(actor, origin.getName(), origin.getSizeInBytes());
        copyContentOnStorage(origin, copy, nodePathFn);
        return copy;

    }

    public void updateExperimentAttachments(Long experiment, Iterable<Long> attachments) {

        final ExperimentTemplate entity = checkNotNull(experimentRepository.findOne(experiment));
        //noinspection unchecked
        processAttachmentsUpdate(attachments, entity.attachments, entity, experimentRepository);

    }

    public void updateProjectAttachments(Long project, Iterable<Long> attachments) {

        final ProjectTemplate entity = checkNotNull(projectRepository.findOne(project));
        //noinspection unchecked
        processAttachmentsUpdate(attachments, entity.getAttachments(), entity, projectRepository);

    }

    private <E extends AbstractPersistable> void processAttachmentsUpdate(Iterable<Long> attachmentsIdsToUpdate,
                                                                          List<ATTACHMENT> currentEntityAttachments,
                                                                          E entity,
                                                                          CrudRepository<E, Long> repository) {
        final Sets.SetView<Long> idsToRemove = doUpdateAttachments(attachmentsIdsToUpdate, currentEntityAttachments);
        //explicit save to avoid implicit merge problems on the future transaction commit
        repository.save(entity);
        doRemoveOldAttachments(idsToRemove);
    }


    private void doRemoveOldAttachments(Sets.SetView<Long> idsToRemove) {
        //discard old attachments as well
        for (Long attachmentToRemove : idsToRemove) {
            removeAttachment(attachmentToRemove);
        }
    }

    private Sets.SetView<Long> doUpdateAttachments(Iterable<Long> attachmentsIdsToUpdate,
                                                   List<ATTACHMENT> currentEntityAttachments) {
        final List<Long> existingIds = currentEntityAttachments.stream()
            .map(EntityUtil.ENTITY_TO_ID::apply)
            .collect(Collectors.toList());

        final HashSet<Long> oldIds = Sets.newHashSet(existingIds);
        final HashSet<Long> newIds = Sets.newHashSet(attachmentsIdsToUpdate);

        final Sets.SetView<Long> idsToRemove = Sets.difference(oldIds, newIds);

        final List<ATTACHMENT> newAttachments = newArrayList(attachmentsIdsToUpdate).stream()
            .map(attachmentFromId::apply)
            .collect(Collectors.toList());
        currentEntityAttachments.clear();
        currentEntityAttachments.addAll(newAttachments);
        return idsToRemove;
    }


    private void copyContentOnStorage(ATTACHMENT origin, ATTACHMENT copy, Function<ATTACHMENT, NodePath> nodePathFn) {

        final NodePath originPath = nodePathFn.apply(origin);
        final NodePath copyPath = nodePathFn.apply(copy);

        //noinspection unchecked
        fileStorageService.put(copyPath, fileStorageService.get(originPath));

    }
}
