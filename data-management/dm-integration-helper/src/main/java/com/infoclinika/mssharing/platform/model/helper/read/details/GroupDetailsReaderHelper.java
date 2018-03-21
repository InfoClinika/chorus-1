package com.infoclinika.mssharing.platform.model.helper.read.details;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.platform.entity.GroupTemplate;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.model.TransformersTemplate;
import com.infoclinika.mssharing.platform.model.helper.read.AbstractReaderHelper;
import com.infoclinika.mssharing.platform.model.helper.read.SingleResultBuilder;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.GroupItemTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.MemberItemTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.SharedProjectItemTemplate;
import com.infoclinika.mssharing.platform.repository.GroupRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.ProjectRepositoryTemplate;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Comparator;

import static com.google.common.collect.FluentIterable.from;

/**
 * @author Herman Zamula
 */
@Component
@Scope(value = "prototype")
public class GroupDetailsReaderHelper<GROUP extends GroupTemplate, GROUP_ITEM extends GroupItemTemplate>
        extends AbstractReaderHelper<GROUP, GROUP_ITEM, GroupItemTemplate> {

    private static final Function<UserTemplate, MemberItemTemplate> MEMBER_TRANSFORMER = new Function<UserTemplate, MemberItemTemplate>() {
        @Override
        public MemberItemTemplate apply(UserTemplate input) {
            return new MemberItemTemplate(input.getId(), input.getEmail(), input.getFullName());
        }
    };
    @Inject
    private GroupRepositoryTemplate<GROUP> groupRepository;
    @Inject
    private ProjectRepositoryTemplate<ProjectTemplate> projectRepository;
    @Inject
    private TransformersTemplate transformers;

    @Override
    public Function<GROUP, GroupItemTemplate> getDefaultTransformer() {
        return new Function<GROUP, GroupItemTemplate>() {
            @Nullable
            @Override
            public GroupItemTemplate apply(GROUP input) {
                //noinspection unchecked
                return new GroupItemTemplate(input.getId(), input.getName(), input.getLastModification(),
                        from(input.getCollaborators())
                                .transform(MEMBER_TRANSFORMER)
                                .toSortedSet(transformers.namedItemComparator()),
                        projectsSharedWithGroup(input.getId())
                );
            }
        };
    }

    public SingleResultBuilder<GROUP, GROUP_ITEM> readGroup(long id) {
        return SingleResultBuilder.builder(groupRepository.findOne(id), activeTransformer);
    }

    private ImmutableSortedSet<SharedProjectItemTemplate> projectsSharedWithGroup(long group) {
        return from(projectRepository.findBySharedGroup(group)).transform(new Function<ProjectTemplate, SharedProjectItemTemplate>() {
            @Override
            public SharedProjectItemTemplate apply(ProjectTemplate input) {
                return new SharedProjectItemTemplate(input.getId(), input.getName());
            }
        }).toSortedSet(new Comparator<SharedProjectItemTemplate>() {
            @Override
            public int compare(SharedProjectItemTemplate o1, SharedProjectItemTemplate o2) {
                return o1.title.compareTo(o2.title);
            }
        });
    }

}
