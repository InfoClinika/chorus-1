package com.infoclinika.mssharing.model.internal.write;

import com.infoclinika.mssharing.model.DemoDataCreatorHelper;
import com.infoclinika.mssharing.model.internal.entity.ApplicationSettings;
import com.infoclinika.mssharing.model.internal.repository.ApplicationSettingsRepository;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import static com.infoclinika.mssharing.model.internal.repository.ApplicationSettingsRepository.IS_DEMO_DATA_CREATED;

/**
 * @author Vladislav Kovchug
 */
@Service
@Transactional
public class DemoDataCreatorHelperImpl implements DemoDataCreatorHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoDataCreatorHelperImpl.class);

    private final UserRepository userRepository;
    private final ApplicationSettingsRepository applicationSettingsRepository;

    @Inject
    public DemoDataCreatorHelperImpl(UserRepository userRepository,
                                     ApplicationSettingsRepository applicationSettingsRepository) {
        this.userRepository = userRepository;
        this.applicationSettingsRepository = applicationSettingsRepository;
    }

    @Override
    public boolean isDemoDataCreated() {
        LOGGER.info("Read demo data creator status");

        final ApplicationSettings isDemoDataCreated = applicationSettingsRepository.findIsDemoDataCreated();
        if (isDemoDataCreated == null) {
            return userRepository.count() > 0; // If users are persisted then demo data is created.
        }

        return isDemoDataCreated.getValue() > 0;
    }

    @Override
    public void markDemoDataCreationAsComplete() {
        LOGGER.info("Mark demo data creation as complete");
        final int demoDataAlreadyCreatedValue = 1;
        ApplicationSettings isDemoDataCreated = applicationSettingsRepository.findIsDemoDataCreated();
        if (isDemoDataCreated == null) {
            isDemoDataCreated = new ApplicationSettings(demoDataAlreadyCreatedValue, IS_DEMO_DATA_CREATED);
        }

        isDemoDataCreated.setValue(demoDataAlreadyCreatedValue);
        applicationSettingsRepository.save(isDemoDataCreated);
    }
}
