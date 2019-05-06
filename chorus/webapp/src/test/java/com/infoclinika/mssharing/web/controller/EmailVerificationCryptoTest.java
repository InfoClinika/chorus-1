package com.infoclinika.mssharing.web.controller;

import com.infoclinika.mssharing.propertiesprovider.ChorusPropertiesProvider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Pavel Kaplin
 */
public class EmailVerificationCryptoTest {

    private EmailVerificationCrypto crypto;

    @BeforeMethod
    public void initCrypto() {
        crypto = new EmailVerificationCrypto(new ChorusPropertiesProvider());
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
