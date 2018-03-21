package com.infoclinika.mssharing.platform.model.testing.helper;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.platform.entity.LabTemplate;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate;
import com.infoclinika.mssharing.platform.repository.UserRepositoryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Herman Zamula
 */
@Component
@Transactional(readOnly = true)
@SuppressWarnings("unchecked")
public class UserTestHelper {
    @Inject
    private UserRepositoryTemplate<?> repository;

    private static ImmutableSet<String> getLabNames(UserTemplate user) {
        return FluentIterable.from(user.getLabs()).transform(new Function<LabTemplate, String>() {
            @Override
            public String apply(LabTemplate input) {
                return input.getName();
            }
        }).toSet();
    }

    public UserShortForm shortForm(long actor) {
        final UserTemplate user = find(actor);
        return new UserShortForm(user.getId(),
                user.getFullName(),
                user.getEmail(),
                getLabNames(user));
    }

    public AccountSettingsForm accountSettingsForm(long actor) {
        final UserTemplate user = find(actor);
        return new AccountSettingsForm(
                user.getPersonData().getFirstName(),
                user.getPersonData().getLastName(),
                getLabNames(user),
                user.getEmail());
    }

    public UserManagementTemplate.PersonInfo readPersonInfo(long actor) {
        final UserTemplate user = find(actor);
        return new UserManagementTemplate.PersonInfo(user.getFirstName(), user.getLastName(), user.getEmail());
    }

    private UserTemplate find(long id) {
        return checkNotNull(repository.findOne(id), "Couldn't find user with id %s", id);
    }

    public static class UserShortForm {
        public final long id;
        public final String name;
        public final String email;
        public final ImmutableSet<String> units;

        public UserShortForm(long id, String name, String email, Set<String> units) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.units = ImmutableSet.copyOf(units);
        }

        public UserManagementTemplate.PersonInfo toPersonInfo() {
            return new UserManagementTemplate.PersonInfo(name, name, email);
        }
    }

    public static class AccountSettingsForm {
        public final String firstName;
        public final String lastName;
        public final ImmutableSet<String> laboratories;
        public final String email;

        public AccountSettingsForm(String firstName, String lastName, Set<String> laboratories, String email) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.laboratories = ImmutableSet.copyOf(laboratories);
            this.email = email;
        }
    }
}
