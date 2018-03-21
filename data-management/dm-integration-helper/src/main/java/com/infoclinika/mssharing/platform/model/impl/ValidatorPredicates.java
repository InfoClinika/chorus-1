package com.infoclinika.mssharing.platform.model.impl;

import com.google.common.base.Function;
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
import com.infoclinika.mssharing.platform.repository.*;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public final class ValidatorPredicates {

    @Inject
    private InstrumentRepositoryTemplate<InstrumentTemplate> instrumentRepository;
    @Inject
    private ProjectRepositoryTemplate<ProjectTemplate> projectRepository;
    @Inject
    private FileRepositoryTemplate<FileMetaDataTemplate> fileMetaDataRepository;
    @Inject
    private UserRepositoryTemplate<UserTemplate> userRepository;
    @Inject
    private EntityFactories factories;

    public static <PROJECT extends ProjectTemplate> Predicate<PROJECT> isPublicProject() {
        return new Predicate<PROJECT>() {
            @Override
            public boolean apply(ProjectTemplate project) {
                return project.getSharing().getType() == Sharing.Type.PUBLIC;
            }
        };
    }

    public static Predicate<ProjectTemplate> isOwnerInProject(final UserTemplate user) {
        return new Predicate<ProjectTemplate>() {
            @Override
            public boolean apply(ProjectTemplate project) {
                return project.getCreator().equals(user);
            }
        };
    }

    public static Predicate<ProjectTemplate> isProjectShared(final UserTemplate user) {
        return new Predicate<ProjectTemplate>() {
            @Override
            public boolean apply(ProjectTemplate project) {
                return project.getSharing().getCollaborators().keySet().contains(user)
                        || Iterables.any(project.getSharing().getGroupsOfCollaborators().keySet(), new Predicate<GroupTemplate>() {
                    @Override
                    public boolean apply(GroupTemplate input) {
                        return input.getCollaborators().contains(user);
                    }
                });
            }
        };
    }

    public static Predicate<ProjectTemplate> isProjectLabHead(final UserTemplate user) {
        return new Predicate<ProjectTemplate>() {
            @Override
            public boolean apply(ProjectTemplate project) {
                return project.getLab() == null ? project.getCreator().equals(user) : project.getLab().getHead().getId().equals(user.getId());
            }
        };
    }

    public <E extends ExperimentTemplate> Predicate<E> isUserCanReadExperiment(final long actor) {

        final FluentIterable<ProjectTemplate> projects = FluentIterable.from(projectRepository.findAllAvailable(actor));

        return Predicates.or(isExperimentLabHead(actor), new Predicate<E>() {
            @Override
            public boolean apply(ExperimentTemplate input) {
                return isAdmin(actor) || projects.contains(input.getProject());
            }
        });
    }

    private boolean isAdmin(long actor) {
        return findUser(actor).isAdmin();
    }

    private UserTemplate findUser(long actor) {
        return checkNotNull(userRepository.findOne(actor), "User not found. Id=" + actor);
    }

    public <E extends ExperimentTemplate> Predicate<E> isExperimentLabHead(final long actor) {
        return new Predicate<E>() {
            @Override
            public boolean apply(E input) {
                return input.getLab() == null ? input.getCreator().getId().equals(actor) : input.getLab().getHead().getId().equals(actor);
            }
        };
    }

    public Predicate<FileMetaDataTemplate> hasSameInstrumentModel(final InstrumentModel model) {
        return new Predicate<FileMetaDataTemplate>() {
            @Override
            public boolean apply(FileMetaDataTemplate input) {
                return input.getInstrument().getModel().equals(model);
            }
        };
    }

    /**
     * User can use file if he is owner or this file is used in any project with user can access
     * or the user is an operator of the instrument used to upload this file. File not private.
     */
    public <FILE extends FileMetaDataTemplate> Predicate<FILE> userHasReadPermissionsOnFile(final long userId) {
        //noinspection unchecked
        return new Predicate<FILE>() {
            @Override
            public boolean apply(FILE input) {
                return fileMetaDataRepository.isUserCanReadFile(userId, input.getId());
            }
        };
    }

    /**
     * User is one of the operators of the instruments which was used to upload this file
     *
     * @param userId the user ID
     * @return true if user is one of the operators, false otherwise
     */
    public Predicate<FileMetaDataTemplate> userIsOperatorOfInstrumentForFile(final long userId) {
        // todo[rduboveckij]: add equals to all entity objects because this function always return false;
        return Predicates.compose(Predicates.in(instrumentRepository.findWhereOperatorIs(userId)),
                new Function<FileMetaDataTemplate, InstrumentTemplate>() {
                    @Override
                    public InstrumentTemplate apply(FileMetaDataTemplate input) {
                        return loadedFile(input).getInstrument();
                    }
                }
        );
    }

    protected FileMetaDataTemplate loadedFile(FileMetaDataTemplate file) {
        if (file.getOwner() == null) return fileMetaDataRepository.findOne(file.getId());
        return file;
    }

    public Predicate<FileMetaDataTemplate> userCanAccessFileThroughProject(long userId) {
        return filesFromMatchedProjects(isUserCanReadProject(factories.userFromId.apply(userId)));
    }

    @SuppressWarnings("unchecked")
    public <FILE extends FileMetaDataTemplate, PROJECT extends ProjectTemplate> Predicate<FILE> filesFromMatchedProjects(final Predicate<PROJECT> projectPredicate) {
        List<FileUsage> fileUsages = projectRepository.whereFileIsUsed();
        ImmutableListMultimap.Builder<Long, PROJECT> builder = ImmutableListMultimap.builder();
        for (FileUsage fileUsage : fileUsages) {
            builder.put(fileUsage.file, (PROJECT) fileUsage.project);
        }
        final ImmutableListMultimap<Long, PROJECT> filesToProjects = builder.build();
        return (Predicate<FILE>) new Predicate<FileMetaDataTemplate>() {
            @Override
            public boolean apply(FileMetaDataTemplate input) {
                return Iterables.any(filesToProjects.get(input.getId()), projectPredicate);
            }
        };
    }

    public Predicate<ProjectTemplate> isUserCanReadProject(final UserTemplate user) {
        //noinspection unchecked
        return new Predicate<ProjectTemplate>() {
            @Override
            public boolean apply(ProjectTemplate project) {
                return projectRepository.isUserCanReadProject(user.getId(), project.getId());
            }
        };
    }

    public <INSTRUMENT extends InstrumentTemplate> Predicate<INSTRUMENT> isUserCanReadInstrument(final long actor) {
        final Optional<UserTemplate> user = Optional.fromNullable(userRepository.findOne(actor));
        return Predicates.or(new Predicate<InstrumentTemplate>() {
            @Override
            public boolean apply(InstrumentTemplate input) {
                return user.isPresent() && user.get().getLabs().contains(input.getLab());
            }
        }, new Predicate<InstrumentTemplate>() {
            @Override
            public boolean apply(InstrumentTemplate input) {
                if (!user.isPresent()) {
                    return false;
                }
                final Long count = fileMetaDataRepository.countAvailableFilesByInstrument(user.get().getId(), input.getId());
                return count != null && count > 0;
            }
        });
    }


    public Predicate<FileMetaDataTemplate> hasSameSpecies(final Species specie) {
        return new Predicate<FileMetaDataTemplate>() {
            @Override
            public boolean apply(FileMetaDataTemplate input) {
                final Species fileSpecies = input.getSpecie();
                return fileSpecies.equals(specie) || fileSpecies.isUnspecified();
            }
        };
    }

    public Predicate<FileMetaDataTemplate> isFileInvalid() {
        return new Predicate<FileMetaDataTemplate>() {
            @Override
            public boolean apply(FileMetaDataTemplate input) {
                return input.isInvalid();
            }
        };
    }

    public Predicate<FileMetaDataTemplate> userIsOwnerOfFile(final long userId) {
        return new Predicate<FileMetaDataTemplate>() {
            @Override
            public boolean apply(@Nullable FileMetaDataTemplate input) {
                return loadedFile(input).getOwner().getId().equals(userId);
            }
        };
    }
}
