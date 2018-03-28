package com.infoclinika.mssharing.web.helper;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.infoclinika.auth.ChorusAuthenticationService;
import com.infoclinika.mssharing.model.PredefinedDataCreator;
import com.infoclinika.mssharing.model.helper.ExperimentCreationHelper;
import com.infoclinika.mssharing.model.helper.LockMzItem;
import com.infoclinika.mssharing.model.helper.RestHelper;
import com.infoclinika.mssharing.model.helper.SecurityHelper;
import com.infoclinika.mssharing.model.internal.repository.ExperimentLabelRepository;
import com.infoclinika.mssharing.model.internal.repository.ExperimentLabelTypeRepository;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import com.infoclinika.mssharing.model.read.*;
import com.infoclinika.mssharing.model.write.*;
import com.infoclinika.mssharing.platform.model.helper.InstrumentCreationHelperTemplate;
import com.infoclinika.mssharing.platform.model.read.LabReaderTemplate;
import com.infoclinika.mssharing.web.controller.EmailVerificationCrypto;
import com.infoclinika.mssharing.web.controller.SecurityController;
import com.infoclinika.mssharing.web.downloader.ChorusSingleFileDownloadHelper;
import com.infoclinika.mssharing.web.rest.UploaderRestServiceImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import javax.inject.Inject;

import java.util.*;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;

/**
 * @author Pavel Kaplin
 */
@ContextConfiguration(classes = SpringSupportTest.class)
public class AbstractDataBasedTest extends AbstractTestNGSpringContextTests{

    public final List<LockMzItem> lockMasses = emptyList();


    @Inject
    protected DashboardReader dashboardReader;
    @Inject
    protected SecurityHelper securityHelper;
    @Inject
    protected UserManagement userManagement;
    @Inject
    protected ChorusAuthenticationService chorusAuthenticationService;
    @Inject
    protected SecurityController securityController;
    @Inject
    protected EmailVerificationCrypto crypto;
    @Inject
    protected UserRepository userRepository;

    @Inject
    protected ExperimentCreationHelper helper;

    @Inject
    protected AdministrationToolsReader administrationToolsReader;

    @Inject
    protected ExperimentCreationHelper experimentCreationHelper;

    @Inject
    protected RequestsReader requestsReader;

    @Inject
    protected PasswordEncoder encoder;

    @Inject
    protected FileMetaDataRepository fileMetaDataRepository;

    @Inject
    protected ExperimentLabelTypeRepository experimentLabelTypeRepository;

    @Inject
    protected ExperimentLabelRepository experimentLabelRepository;

    @Inject
    protected LabHeadManagement labHeadManagement;
    @Inject
    protected FileAccessLogReader fileAccessLogReader;

    @Inject
    protected UserReader userReader;

    @Inject
    protected ChorusSingleFileDownloadHelper chorusSingleFileDownloadHelper;

    @Inject
    protected PredefinedDataCreator initiator;
    @Inject
    protected LabManagement labManagement;
    @Inject
    protected RestHelper restHelper;
    @Inject
    protected UploaderRestServiceImpl uploaderRestService;

    @Inject
    protected InstrumentManagement instrumentManagement;

    @Inject
    private InstrumentCreationHelperTemplate<InstrumentCreationHelperTemplate.PotentialOperator> instrumentCreationHelper;


    protected Long pavelKaplinAtGmail() {
        return securityHelper.getUserDetailsByEmail("pavel.kaplin@gmail.com").id;
    }

    protected long pavelKaplinAtTeamdev() {
        return securityHelper.getUserDetailsByEmail("pavel.kaplin@teamdev.com").id;
    }

    protected long firstLab() {
        ImmutableSet<LabReaderTemplate.LabLineTemplate> labLines = dashboardReader.readUserLabs(pavelKaplinAtGmail());
        LabReaderTemplate.LabLineTemplate lab = Collections2.filter(labLines, new Predicate<LabReaderTemplate.LabLineTemplate>() {
            @Override
            public boolean apply(LabReaderTemplate.LabLineTemplate input) {
                return input.name.equals("First Chorus Lab Very Long Name For Testing Ellipsize");
            }
        }).iterator().next();
        return lab.id;
    }


    protected InstrumentDetails instrumentDetails() {
        return new InstrumentDetails(generateInstrumentValue(), generateInstrumentValue(), generateInstrumentValue(), generateInstrumentValue(), lockMasses);
    }

    protected String generateInstrumentValue(){
        return UUID.randomUUID().toString();
    }

    protected long anyInstrumentModel() {
        return randElement(instrumentCreationHelper.models(anyVendor())).id;
    }

    protected long anyVendor() {
        return instrumentCreationHelper.vendors().first().id;
    }

    protected <T> T randElement(Collection<T> all) {
        final Random random = new Random();
        return Iterables.get(all, random.nextInt(all.size()));
    }
}
