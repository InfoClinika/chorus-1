package com.infoclinika.mssharing.platform.model.helper.read;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.repository.UserRepositoryTemplate;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;

import static com.infoclinika.mssharing.platform.model.read.UserReaderTemplate.UserLineTemplate;

/**
 * @author : Alexander Serebriyan
 */

@Component
@Scope(value = "prototype")
public class UserReaderHelper<USER extends UserTemplate, USER_LINE extends UserLineTemplate>
    extends AbstractReaderHelper<USER, USER_LINE, UserLineTemplate> {

    @Inject
    private UserRepositoryTemplate<USER> userRepositoryTemplate;

    @Override
    public Function<USER, UserLineTemplate> getDefaultTransformer() {
        return user -> new UserLineTemplate(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName());
    }

    public ResultBuilder<USER, USER_LINE> readUsersByLab(long labId) {
        final List<USER> allUsersByLab = userRepositoryTemplate.findAllUsersByLab(labId);
        return ResultBuilder.builder(allUsersByLab, activeTransformer);
    }
}
