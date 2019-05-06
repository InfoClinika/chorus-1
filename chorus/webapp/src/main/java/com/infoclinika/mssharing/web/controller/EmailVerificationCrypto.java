package com.infoclinika.mssharing.web.controller;

import com.infoclinika.mssharing.propertiesprovider.ChorusPropertiesProvider;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.security.GeneralSecurityException;

/**
 * @author Pavel Kaplin
 */
@Component
class EmailVerificationCrypto {
    private Mac mac;
    private byte[] key;

    @Inject
    public EmailVerificationCrypto(ChorusPropertiesProvider chorusPropertiesProvider) {
        setKey(chorusPropertiesProvider.getEmailVerificationCryptoKey());
    }

    public void setKey(String key) {
        if (key != null) {
            String[] keyStrings = key.split(",");
            this.key = new byte[keyStrings.length];
            for (int i = 0; i < keyStrings.length; i++) {
                this.key[i] = Byte.decode(keyStrings[i].trim());
            }
        }
    }

    @PostConstruct
    public void initMac() {
        SecretKeySpec keySpec = new SecretKeySpec(key, "HmacMD5");
        try {
            mac = Mac.getInstance("HmacMD5");
            mac.init(keySpec);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    String getMac(String value) {
        byte[] bytes = mac.doFinal(value.getBytes());
        return new String(Base64.encode(bytes));
    }

    boolean isMacValid(String value, String mac) {
        return getMac(value).equals(mac);
    }
}
