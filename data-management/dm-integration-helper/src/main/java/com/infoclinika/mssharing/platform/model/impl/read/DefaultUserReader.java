package com.infoclinika.mssharing.platform.model.impl.read;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.model.helper.read.UserReaderHelper;
import com.infoclinika.mssharing.platform.model.impl.DefaultTransformingTemplate;
import com.infoclinika.mssharing.platform.model.read.UserReaderTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

/**
 * @author : Alexander Serebriyan
 */
@Transactional(readOnly = true)
public abstract class DefaultUserReader<USER extends UserTemplate,
    USER_LINE extends UserReaderTemplate.UserLineTemplate>
    implements UserReaderTemplate<USER_LINE>, DefaultTransformingTemplate<USER, USER_LINE> {

    @Inject
    protected UserReaderHelper<USER, USER_LINE> userReaderHelper;

    @PostConstruct
    private void setup() {
        userReaderHelper.setTransformer(user -> transform(user));
    }

    @Override
    public List<USER_LINE> readUsersByLab(long actor, long labId) {
        return userReaderHelper.readUsersByLab(labId).transform().toList();
    }
}
