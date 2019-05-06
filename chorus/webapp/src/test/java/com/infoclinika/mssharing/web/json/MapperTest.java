package com.infoclinika.mssharing.web.json;

import com.google.common.io.Resources;
import com.infoclinika.mssharing.model.helper.ExperimentSampleItem;
import com.infoclinika.mssharing.model.helper.ExperimentSampleTypeItem;
import com.infoclinika.mssharing.model.write.AnnotationItem;
import com.infoclinika.mssharing.model.write.ExperimentInfo;
import com.infoclinika.mssharing.model.write.FileItem;
import com.infoclinika.mssharing.model.write.ProjectInfo;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate;
import com.infoclinika.mssharing.web.controller.SecurityController;
import com.infoclinika.mssharing.web.controller.request.CreateInstrumentRequest;
import com.infoclinika.mssharing.web.controller.request.ExperimentDetails;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.AssertJUnit.*;

/**
 * @author Pavel Kaplin
 */
public class MapperTest {

    private static final String EMAIL = "pavel@example.com";
    private static final String PASSWORD = "pwd";
    private static final String FIRST_NAME = "Pavel";
    private static final String LAST_NAME = "Kaplin";
    private MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();

    public MapperTest() {
    }

    @BeforeMethod
    public void setUpConverter() {
        converter.setObjectMapper(new Mapper());
    }

    @Test
    public void testCouldBeDeserialized() {
        assertThat(converter.canRead(ProjectInfo.class, MediaType.APPLICATION_JSON), is(true));
        assertThat(converter.canRead(SecurityController.AccountDetails.class, MediaType.APPLICATION_JSON), is(true));
        assertThat(converter.canRead(ExperimentInfo.class, MediaType.APPLICATION_JSON), is(true));
    }

    @Test
    public void testCreateAccount() throws IOException {
        SecurityController.AccountDetails result =
            readJson("createAccount.json", SecurityController.AccountDetails.class);
        assertNotNull(result);
        assertEquals(FIRST_NAME, result.firstName);
        assertEquals(LAST_NAME, result.lastName);
        assertTrue(result.laboratories.contains(1L));
        assertEquals(EMAIL, result.email);
        assertEquals(PASSWORD, result.password);
    }

    private <T> T readJson(String resourceName, Class<T> clazz) throws IOException {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        byte[] json = Resources.toByteArray(Resources.getResource(MapperTest.class, resourceName));
        servletRequest.setContent(json);
        return (T) converter.read(
            clazz,
            new ServletServerHttpRequest(servletRequest)
        );
    }

    @Test
    public void testCreateExperiment() throws Exception {
        ExperimentDetails experiment = readJson("createExperiment.json", ExperimentDetails.class);
        assertThat(experiment.info.name, is("Global Experiment"));
        assertThat(experiment.info.description, is("It is about all the people on Earth"));
        assertThat(experiment.restriction.instrumentModel, is(7L));
        assertThat(experiment.restriction.instrument.get(), is(1L));
        assertThat(experiment.project, is(3L));
        assertThat(experiment.factors, notNullValue());
        assertThat(experiment.info.experimentType, is(17L));
        assertThat(experiment.info.specie, is(19L));
        assertThat(experiment.ownerEmail, is(EMAIL));
    }

    @Test
    public void testCreateExperimentWithFactors() throws Exception {
        ExperimentDetails experiment = readJson("createExperimentWithFactors.json", ExperimentDetails.class);
        assertThat(experiment.factors.size(), is(2));

        ExperimentManagementTemplate.MetaFactorTemplate firstFactor = experiment.factors.get(0);
        assertThat(firstFactor.name, is("Max Factor"));
        assertThat(firstFactor.units, is("Meters"));
        assertThat(firstFactor.isNumeric, is(true));

        ExperimentManagementTemplate.MetaFactorTemplate secondFactor = experiment.factors.get(1);
        assertThat(secondFactor.name, is("Non Numeric"));
        assertThat(secondFactor.units, is(""));
        assertThat(secondFactor.isNumeric, is(false));

        assertThat(experiment.files.size(), is(2));

        FileItem firstFile = experiment.files.get(0);
        assertThat(firstFile.id, is(7L));
        assertThat(firstFile.factorValues, is(Arrays.asList("1", "One")));

        FileItem secondFile = experiment.files.get(1);
        assertThat(secondFile.id, is(12L));
        assertThat(secondFile.factorValues, is(Arrays.asList("2", "Two")));
    }

    @Test
    public void testCreateExperimentWithSamplesAndFactor() throws Exception {
        ExperimentDetails experiment = readJson("createExperimentWithSamplesAndFactors.json", ExperimentDetails.class);
        assertThat(experiment.files.size(), is(2));

        FileItem first = experiment.files.get(0);
        assertThat(first.preparedSample.samples.size(), is(1));
        assertThat(first.preparedSample.name, is("prepared_sample_for_file_7"));
        ExperimentSampleItem sample1 = first.preparedSample.samples.iterator().next();
        assertThat(sample1.type, is(ExperimentSampleTypeItem.LIGHT));
        assertThat(sample1.name, is("sample_1"));
        assertEquals(sample1.factorValues.get(0), "healthy");
        assertEquals(sample1.factorValues.get(1), "50");

        FileItem secondFile = experiment.files.get(1);
        assertThat(secondFile.preparedSample.samples.size(), is(1));
        assertThat(secondFile.preparedSample.name, is("prepared_sample_for_file_8"));
        ExperimentSampleItem sample2 = secondFile.preparedSample.samples.iterator().next();
        assertThat(sample2.type, is(ExperimentSampleTypeItem.LIGHT));
        assertEquals(sample2.factorValues.get(0), "sick");
        assertEquals(sample2.factorValues.get(1), "500");
        assertThat(sample2.name, is("sample_2"));
    }

    @Test
    public void testCreateExperimentWithSamplesAndAnnotations() throws Exception {
        ExperimentDetails experiment =
            readJson("createExperimentWithSamplesAndAnnotations.json", ExperimentDetails.class);
        assertThat(experiment.files.size(), is(2));

        FileItem first = experiment.files.get(0);
        assertThat(first.preparedSample.samples.size(), is(1));
        assertThat(first.preparedSample.name, is("prepared_sample_for_file_7"));
        ExperimentSampleItem sample1 = first.preparedSample.samples.iterator().next();
        assertThat(sample1.type, is(ExperimentSampleTypeItem.LIGHT));
        assertThat(sample1.name, is("sample_1"));

        AnnotationItem annotation;
        annotation = sample1.annotationValues.get(0);
        assertEquals(annotation.name, "AT");
        assertEquals(annotation.value, "a10");
        assertEquals(annotation.isNumeric, false);
        assertNull(annotation.units);

        annotation = sample1.annotationValues.get(1);
        assertEquals(annotation.name, "AN");
        assertEquals(annotation.value, "10");
        assertEquals(annotation.isNumeric, true);
        assertEquals(annotation.units, "au");

        FileItem secondFile = experiment.files.get(1);
        assertThat(secondFile.preparedSample.samples.size(), is(1));
        assertThat(secondFile.preparedSample.name, is("prepared_sample_for_file_8"));
        ExperimentSampleItem sample2 = secondFile.preparedSample.samples.iterator().next();
        assertThat(sample2.type, is(ExperimentSampleTypeItem.LIGHT));
        assertThat(sample2.name, is("sample_2"));

        annotation = sample2.annotationValues.get(0);
        assertEquals(annotation.name, "AT");
        assertEquals(annotation.value, "a20");
        assertEquals(annotation.isNumeric, false);
        assertNull(annotation.units);

        annotation = sample2.annotationValues.get(1);
        assertEquals(annotation.name, "AN");
        assertEquals(annotation.value, "20");
        assertEquals(annotation.isNumeric, true);
        assertEquals(annotation.units, "au");
    }

    @Test
    public void testCreateInstrument() throws IOException {
        CreateInstrumentRequest request = readJson("createInstrument.json", CreateInstrumentRequest.class);
        assertThat(request.model, is(254L));
        assertThat(request.details.name, is("New instrument"));
        assertThat(request.details.serialNumber, is("ABC123456"));
        assertThat(request.details.hplc, is("hplc"));
        assertThat(request.details.peripherals, is("printer, scanner"));
    }
}
