package com.infoclinika.mssharing.platform.model.impl.read;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.platform.entity.GroupTemplate;
import com.infoclinika.mssharing.platform.model.helper.read.GroupReaderHelper;
import com.infoclinika.mssharing.platform.model.impl.DefaultTransformingTemplate;
import com.infoclinika.mssharing.platform.model.read.GroupsReaderTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Comparator;

/**
 * @author Herman Zamula
 */
@Transactional(readOnly = true)
public abstract class DefaultGroupReader<GROUP extends GroupTemplate, LINE extends GroupsReaderTemplate.GroupLine>
        implements GroupsReaderTemplate<LINE>, DefaultTransformingTemplate<GROUP, LINE> {

    @Inject
    protected GroupReaderHelper<GROUP, LINE> groupReaderHelper;

    @PostConstruct
    private void init() {
        groupReaderHelper.setTransformer(new Function<GROUP, LINE>() {
            @Override
            public LINE apply(GROUP input) {
                return transform(input);
            }
        });
    }

    @Override
    public ImmutableSet<LINE> readGroups(long actor, boolean includeAllUsers) {
        return groupReaderHelper.readGroups(actor, includeAllUsers)
                .transform()
                .toSortedSet(new Comparator<LINE>() {
                    @Override
                    public int compare(LINE o1, LINE o2) {
                        return o1.name.compareTo(o2.name);
                    }
                });
    }
}
