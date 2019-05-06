package com.infoclinika.mssharing.model.test.read;

import com.infoclinika.mssharing.model.helper.AbstractTest;
import com.infoclinika.mssharing.model.read.DictionaryReader;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.List;

/**
 * @author timofei.kasianov 2/20/18
 */
public class DictionaryReaderTest extends AbstractTest {

    @Inject
    private DictionaryReader dictionaryReader;

    @Test
    public void testFindExperimentPrepMethodByNgsExperimentType() {
        final int typeId = ngsExperimentTypeWithPrepMethods();
        final List<DictionaryReader.ExperimentPrepMethodDTO> prepMethods =
            dictionaryReader.findExperimentPrepMethodByNgsExperimentType(typeId);
        Assert.assertFalse(prepMethods.isEmpty());
    }
}
