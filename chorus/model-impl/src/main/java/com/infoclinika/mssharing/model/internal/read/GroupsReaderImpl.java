package com.infoclinika.mssharing.model.internal.read;

import com.infoclinika.mssharing.model.internal.entity.Group;
import com.infoclinika.mssharing.platform.model.impl.read.DefaultGroupReader;
import com.infoclinika.mssharing.platform.model.read.GroupsReaderTemplate.GroupLine;
import org.springframework.stereotype.Service;

/**
 * @author Herman Zamula
 */
@Service("groupsReader")
public class GroupsReaderImpl extends DefaultGroupReader<Group, GroupLine> {

    @Override
    public GroupLine transform(Group group) {
        return groupReaderHelper.getDefaultTransformer().apply(group);
    }

}
