package com.infoclinika.mssharing.model.test.sso;

/**
 * @author Andrii Loboda
 */

import com.infoclinika.mssharing.model.helper.AbstractTest;
import com.infoclinika.mssharing.model.internal.write.SecretTokenGenerator;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static org.testng.Assert.assertFalse;

/**
 * @author Andrii Loboda
 */
@SuppressWarnings("InstanceMethodNamingConvention")
public class SecretTokenGeneratorShouldTest extends AbstractTest {
    @Inject
    private SecretTokenGenerator secretTokenGenerator;


    @Test
    public void generate_unique_token() {
        final String generatedSequence = secretTokenGenerator.generate();
        for (int i = 0; i < 10_000; i++) {
            assertFalse(secretTokenGenerator.generate().equals(generatedSequence));
        }
    }

}
