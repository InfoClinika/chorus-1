package com.infoclinika.mssharing.web.controller;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Pavel Kaplin
 */
public class EmailVerificationCryptoTest {

    private EmailVerificationCrypto crypto;

    @BeforeMethod
    public void initCrypto() {
        crypto = new EmailVerificationCrypto();
        crypto.setKey("1,2,3");
        crypto.initMac();
    }

    @Test
    public void testGetMacAndVerify() {
        String mac = crypto.getMac("pavel@example.com");
        assertTrue(crypto.isMacValid("pavel@example.com", mac));
        assertFalse(crypto.isMacValid("pavel@another.com", mac));
        assertFalse(crypto.isMacValid("pavel@example.com", mac.substring(1)));
    }
}
