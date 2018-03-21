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
public abstract class DefaultRegistrationHelperTemplate<LAB_ITEM extends LabItem> implements RegistrationHelperTemplate<LAB_ITEM> {

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
                .transform(new Function<LabTemplate, LAB_ITEM>() {
                    @Override
                    public LAB_ITEM apply(LabTemplate input) {
                        return transformLabItem(input);
                    }
                }).toSortedSet(labItemComparator());
    }

    protected Comparator<LAB_ITEM> labItemComparator() {

        return new Comparator<LAB_ITEM>() {
            @Override
            public int compare(LAB_ITEM o1, LAB_ITEM o2) {
                return o1.name.compareTo(o2.name);
            }
        };

    }

    protected abstract LAB_ITEM transformLabItem(LabTemplate input);
}
