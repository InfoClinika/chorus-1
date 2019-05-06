package com.infoclinika.mssharing.platform.model.impl.read.adapters;

import com.infoclinika.mssharing.platform.entity.GroupTemplate;
import com.infoclinika.mssharing.platform.model.impl.read.DefaultGroupReader;
import com.infoclinika.mssharing.platform.model.read.GroupsReaderTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Herman Zamula
 */
@Component
public class DefaultGroupReaderAdapter extends DefaultGroupReader<GroupTemplate, GroupsReaderTemplate.GroupLine> {
    @Override
    public GroupLine transform(GroupTemplate groupTemplate) {
        return groupReaderHelper.getDefaultTransformer().apply(groupTemplate);
    }
}
