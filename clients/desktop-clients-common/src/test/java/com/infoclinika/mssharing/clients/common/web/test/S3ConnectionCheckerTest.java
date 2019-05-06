package com.infoclinika.mssharing.clients.common.web.test;

import com.infoclinika.mssharing.clients.common.util.S3ConnectionChecker;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Vladislav Kovchug
 */

public class S3ConnectionCheckerTest {

    @Test
    public void connectToS3() {
        final boolean canAccess = S3ConnectionChecker.checkCanAccessToS3();
        Assert.assertTrue(canAccess);
    }

}
