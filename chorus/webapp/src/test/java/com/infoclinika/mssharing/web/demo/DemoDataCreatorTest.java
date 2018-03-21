package com.infoclinika.mssharing.web.demo;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.model.helper.ExperimentCreationHelper;
import com.infoclinika.mssharing.model.helper.SecurityHelper;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.repository.ExperimentLabelRepository;
import com.infoclinika.mssharing.model.internal.repository.ExperimentLabelTypeRepository;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.model.read.AdministrationToolsReader;
import com.infoclinika.mssharing.model.read.InstrumentLine;
import com.infoclinika.mssharing.model.read.RequestsReader;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.common.items.FileItem;
import com.infoclinika.mssharing.platform.model.read.Filter;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Iterables.find;
import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.*;
import static org.junit.internal.matchers.IsCollectionContaining.hasItem;

/**
 * @author Pavel Kaplin
 */
@RunWith(Theories.class)
public class DemoDataCreatorTest extends DemoDataBasedTest {

    private static final int EXPERIMENT_LABEL_TYPES_SIZE = 9;
    private static final int EXPERIMENT_LABELS_SIZE = 114;
    private static final String PASSWORD = "pwd";
    @Inject
    private AdministrationToolsReader administrationToolsReader;

    @Inject
    private ExperimentCreationHelper experimentCreationHelper;

    @Inject
    private RequestsReader requestsReader;

    @Inject
    private PasswordEncoder encoder;

    @Inject
    private SecurityHelper securityHelper;

    @Inject
    private FileMetaDataRepository fileMetaDataRepository;

    @Inject
    private ExperimentLabelTypeRepository experimentLabelTypeRepository;

    @Inject
    private ExperimentLabelRepository experimentLabelRepository;

    @Value("${database.data.admin.email}")
    private String adminEmail;

    public DemoDataCreatorTest() {
    }

    @DataPoints
    public static Filter[] getFilterValues() {
        return Filter.values();
    }

    @Theory
    public void theoryHaveProjectsForAnyFilter(Filter filter) {
        assertThat("Have projects for filter " + filter,
                dashboardReader.readProjects(pavelKaplinAtGmail(), filter),
                not(empty()));
    }

    @Theory
    public void theoryHaveExperimentsForAnyFilter(Filter filter) {
        assertThat("Have experiments for filter " + filter,
                dashboardReader.readExperiments(pavelKaplinAtGmail(), filter),
                not(empty()));
    }

    @Test
    public void testHaveInstruments() {
        assertThat(dashboardReader.readInstruments(pavelKaplinAtGmail()), not(empty()));
    }

    @Test
    public void testHaveFiles() {
        final Set<InstrumentLine> instrumentItems = dashboardReader.readInstruments(pavelKaplinAtGmail());
        final InstrumentLine filled = Iterables.find(instrumentItems, new Predicate<InstrumentLine>() {
            @Override
            public boolean apply(InstrumentLine input) {
                return input.files > 0;
            }
        }, Iterables.get(instrumentItems, 0));
        ImmutableSet<DictionaryItem> species = experimentCreationHelper.species();
        DictionaryItem specie = find(species, DictionaryItem.UNSPECIFIED);
        List<FileItem> files = experimentCreationHelper.availableFilesByInstrument(pavelKaplinAtGmail(), specie.id, filled.id);
        assertThat(files, hasItem(any(FileItem.class)));
        assertThat(files, hasSize(greaterThan(0)));
    }

    @Test
    public void testPavelKaplinAtTeamdevComDoesNotHavePendingLabMembership() {
        assertTrue(requestsReader.myLabMembershipOutbox(pavelKaplinAtTeamdev()).isEmpty());
    }

    @Test
    public void testAllExperimentLabelTypesExist() {
        assertTrue(experimentLabelTypeRepository.findAll().size() == EXPERIMENT_LABEL_TYPES_SIZE);
    }

    @Test
    public void testAllExperimentLabelsExist() {
        assertTrue(experimentLabelRepository.findAll().size() == EXPERIMENT_LABELS_SIZE);
    }

    @Test
    public void testCouldLogin() {
        assertCouldLogin("pavel.kaplin@gmail.com");
        assertCouldLogin("pavel.kaplin@teamdev.com");
        assertCouldLogin(adminEmail);
    }

    @Test
    public void testAllFilesAreOfThermoVendor() {
        Iterable<ActiveFileMetaData> all = fileMetaDataRepository.findAll();
        for (ActiveFileMetaData fileMetaData : all) {
            assertEquals("File " + fileMetaData + " is of Thermo vendor", "Thermo Scientific", fileMetaData.getInstrument().getModel().getVendor().getName());
        }
    }

    private void assertCouldLogin(String email) {
        SecurityHelper.UserDetails atGmailCom = securityHelper.getUserDetailsByEmail(email);
        assertTrue("Could login as " + email + ", " + PASSWORD, encoder.matches(PASSWORD, atGmailCom.password));
    }
}
