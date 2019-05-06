package com.infoclinika.mssharing.platform.model.impl.helper;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.platform.entity.LabTemplate;
import com.infoclinika.mssharing.platform.model.helper.RegistrationHelperTemplate;
import com.infoclinika.mssharing.platform.model.helper.RegistrationHelperTemplate.LabItem;
import com.infoclinika.mssharing.platform.repository.LabRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.UserRepositoryTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Comparator;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.FluentIterable.from;

/**
 * @author Herman Zamula
 */
@Transactional(readOnly = true)
public abstract class DefaultRegistrationHelperTemplate<LAB_ITEM extends LabItem>
    implements RegistrationHelperTemplate<LAB_ITEM> {

    @Inject
    private UserRepositoryTemplate<?> userRepository;
    @Inject
    private LabRepositoryTemplate<?> labRepository;


    @Override
    public boolean isEmailAvailable(String email) {

        return userRepository.findByEmail(email) == null;
    }

    @Override
    public boolean isEmailActivated(String email) {

        return checkNotNull(userRepository.findByEmail(email)).isEmailVerified();

    }

    @Override
    public ImmutableSortedSet<LAB_ITEM> availableLabs() {

        return from(labRepository.findAll())
            .transform((Function<LabTemplate, LAB_ITEM>) input -> transformLabItem(input))
            .toSortedSet(labItemComparator());
    }

    protected Comparator<LAB_ITEM> labItemComparator() {

        return Comparator.comparing(o -> o.name);

    }

    protected abstract LAB_ITEM transformLabItem(LabTemplate input);
}
