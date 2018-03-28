package com.infoclinika.mssharing.web.demo;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.model.helper.SecurityHelper;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.read.InstrumentLine;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.common.items.FileItem;
import com.infoclinika.mssharing.platform.model.read.Filter;
import com.infoclinika.mssharing.web.helper.AbstractDataBasedTest;
import org.springframework.beans.factory.annotation.Value;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import static com.google.common.collect.Iterables.find;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.internal.matchers.IsCollectionContaining.hasItem;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Pavel Kaplin
 */
//@RunWith(Theories.class)
public class DemoDataCreatorTest extends AbstractDataBasedTest {

    private static final int EXPERIMENT_LABEL_TYPES_SIZE = 9;
    private static final int EXPERIMENT_LABELS_SIZE = 114;
    private static final String PASSWORD = "pwd";


    @Value("${database.data.admin.email}")
    private String adminEmail;

    @DataProvider
    public static Object[][] getFilterValues() {

        return  Arrays.stream(Filter.values())
                .map(filter -> new Object[]{filter})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "getFilterValues")
    public void theoryHaveProjectsForAnyFilter(Filter filter) {
        assertThat("Have projects for filter " + filter,
                dashboardReader.readProjects(pavelKaplinAtGmail(), filter),
                not(empty()));
    }

    @Test(dataProvider = "getFilterValues")
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
            assertEquals("Thermo Scientific", fileMetaData.getInstrument().getModel().getVendor().getName());
        }
    }

    private void assertCouldLogin(String email) {
        SecurityHelper.UserDetails atGmailCom = securityHelper.getUserDetailsByEmail(email);
        assertTrue("Could login as " + email + ", " + PASSWORD, encoder.matches(PASSWORD, atGmailCom.password));
    }
}
