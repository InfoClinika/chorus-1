package com.infoclinika.mssharing.platform.model.impl.read;

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
        groupReaderHelper.setTransformer(input -> transform(input));
    }

    @Override
    public ImmutableSet<LINE> readGroups(long actor, boolean includeAllUsers) {
        return groupReaderHelper.readGroups(actor, includeAllUsers)
            .transform()
            .toSortedSet(Comparator.comparing(o -> o.name));
    }
}
