package com.infoclinika.mssharing.model.internal.write;

import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Generates Secret Token for User to integrate with SSO server
 *
 * @author andrii.loboda
 */
@Service
public class SecretTokenGenerator {
    private static final int RADIX = 32;
    public static final int NUM_BITS = 130;
    private final SecureRandom random = new SecureRandom();

    public String generate() {
        return new BigInteger(NUM_BITS, random).toString(RADIX);
    }
}
