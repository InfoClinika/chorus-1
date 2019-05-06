package com.infoclinika.mssharing.platform.model.impl;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.model.EntityFactories;
import com.infoclinika.mssharing.platform.repository.FileRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.FileUsage;
import com.infoclinika.mssharing.platform.repository.ProjectRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.UserRepositoryTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public final class ValidatorPredicates {

    @Inject
    private ProjectRepositoryTemplate<ProjectTemplate> projectRepository;
    @Inject
    private FileRepositoryTemplate<FileMetaDataTemplate> fileMetaDataRepository;
    @Inject
    private UserRepositoryTemplate<UserTemplate> userRepository;
    @Inject
    private EntityFactories factories;

    public static <PROJECT extends ProjectTemplate> Predicate<PROJECT> isPublicProject() {
        return project -> project.getSharing().getType() == Sharing.Type.PUBLIC;
    }

    public static Predicate<ProjectTemplate> isOwnerInProject(final UserTemplate user) {
        return project -> project.getCreator().equals(user);
    }

    public static Predicate<ProjectTemplate> isProjectShared(final UserTemplate user) {
        return project -> project.getSharing().getCollaborators().keySet().contains(user) || Iterables.any(
            project.getSharing().getGroupsOfCollaborators().keySet(),
            (Predicate<GroupTemplate>) input -> input.getCollaborators().contains(user)
        );
    }

    public static Predicate<ProjectTemplate> isProjectLabHead(final UserTemplate user) {
        return project -> project.getLab() == null ? project.getCreator().equals(user) :
            project.getLab().getHead().getId().equals(user.getId());
    }

    public <E extends ExperimentTemplate> Predicate<E> isUserCanReadExperiment(final long actor) {

        final FluentIterable<ProjectTemplate> projects = FluentIterable.from(projectRepository.findAllAvailable(actor));

        return Predicates.or(
            isExperimentLabHead(actor),
            input -> isAdmin(actor) || projects.contains(input.getProject())
        );
    }

    private boolean isAdmin(long actor) {
        return findUser(actor).isAdmin();
    }

    private UserTemplate findUser(long actor) {
        return checkNotNull(userRepository.findOne(actor), "User not found. Id=" + actor);
    }

    public <E extends ExperimentTemplate> Predicate<E> isExperimentLabHead(final long actor) {
        return input -> input.getLab() == null ? input.getCreator().getId().equals(actor) :
            input.getLab().getHead().getId().equals(actor);
    }

    public Predicate<FileMetaDataTemplate> hasSameInstrumentModel(final InstrumentModel model) {
        return input -> input.getInstrument().getModel().equals(model);
    }

    /**
     * User can use file if he is owner or this file is used in any project with user can access
     * or the user is an operator of the instrument used to upload this file. File not private.
     */
    public <FILE extends FileMetaDataTemplate> Predicate<FILE> userHasReadPermissionsOnFile(final long userId) {
        //noinspection unchecked
        return input -> fileMetaDataRepository.isUserCanReadFile(userId, input.getId());
    }

    protected FileMetaDataTemplate loadedFile(FileMetaDataTemplate file) {
        if (file.getOwner() == null) {
            return fileMetaDataRepository.findOne(file.getId());
        }
        return file;
    }

    public Predicate<FileMetaDataTemplate> userCanAccessFileThroughProject(long userId) {
        return filesFromMatchedProjects(isUserCanReadProject(factories.userFromId.apply(userId)));
    }

    @SuppressWarnings("unchecked")
    public <
        FILE extends FileMetaDataTemplate,
        PROJECT extends ProjectTemplate> Predicate<FILE> filesFromMatchedProjects(
            final Predicate<PROJECT> projectPredicate) {

        List<FileUsage> fileUsages = projectRepository.whereFileIsUsed();
        ImmutableListMultimap.Builder<Long, PROJECT> builder = ImmutableListMultimap.builder();
        for (FileUsage fileUsage : fileUsages) {
            builder.put(fileUsage.file, (PROJECT) fileUsage.project);
        }
        final ImmutableListMultimap<Long, PROJECT> filesToProjects = builder.build();
        return input -> Iterables.any(filesToProjects.get(input.getId()), projectPredicate);
    }

    public Predicate<ProjectTemplate> isUserCanReadProject(final UserTemplate user) {
        //noinspection unchecked
        return project -> projectRepository.isUserCanReadProject(user.getId(), project.getId());
    }

    public <INSTRUMENT extends InstrumentTemplate> Predicate<INSTRUMENT> isUserCanReadInstrument(final long actor) {
        final Optional<UserTemplate> user = Optional.fromNullable(userRepository.findOne(actor));
        return Predicates.or(
            (Predicate<InstrumentTemplate>) input -> user.isPresent() && user.get().getLabs().contains(input.getLab()),
            (Predicate<InstrumentTemplate>) input -> {
                if (!user.isPresent()) {
                    return false;
                }
                final Long count =
                    fileMetaDataRepository.countAvailableFilesByInstrument(user.get().getId(), input.getId());
                return count != null && count > 0;
            }
        );
    }


    public Predicate<FileMetaDataTemplate> hasSameSpecies(final Species specie) {
        return input -> {
            final Species fileSpecies = input.getSpecie();
            return fileSpecies.equals(specie) || fileSpecies.isUnspecified();
        };
    }

    public Predicate<FileMetaDataTemplate> isFileInvalid() {
        return input -> input.isInvalid();
    }

    public Predicate<FileMetaDataTemplate> userIsOwnerOfFile(final long userId) {
        return input -> loadedFile(input).getOwner().getId().equals(userId);
    }
}
