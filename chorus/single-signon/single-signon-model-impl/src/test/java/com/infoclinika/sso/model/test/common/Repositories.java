package com.infoclinika.sso.model.test.common;

import com.google.common.collect.ImmutableList;
import com.infoclinika.sso.model.internal.repository.UserRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * @author andrii.loboda
 */
@Service
public class Repositories {

    @Inject
    private UserRepository userRepository;

    public List<? extends CrudRepository> get() {
        return ImmutableList.<CrudRepository>builder()
            .add(userRepository)
            .build();
    }
}
