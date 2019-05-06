package com.infoclinika.mssharing.autoimporter.service.util;

/*
 *@author Elena Kurilina
 */

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;


public class CipherUtil {
    public static final String CHARSET_NAME = "UTF8";
    private static Cipher dcipher;
    private static Cipher ecipher;
    private static final String KEY = "M:|5$n:BR^AU77N7&1?/T8[z3";
    private static final String ALGORITHM = "PBEWithMD5AndDES";

    static {
        byte[] salt = {(byte) 0xA1, (byte) 0x91, (byte) 0xC8, (byte) 0x33, (byte) 0x56, (byte) 0x44, (byte) 0xE3,
            (byte) 0x63};
        int iterationCount = 19;

        try {
            KeySpec keySpec = new PBEKeySpec(KEY.toCharArray(), salt, iterationCount);
            SecretKey key = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(keySpec);

            ecipher = Cipher.getInstance(key.getAlgorithm());
            dcipher = Cipher.getInstance(key.getAlgorithm());

            AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);

            ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
            dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static String encrypt(String str) {
        try {
            byte[] utf8 = str.getBytes(CHARSET_NAME);
            byte[] enc = ecipher.doFinal(utf8);
            return new sun.misc.BASE64Encoder().encode(enc);

        } catch (Exception e) {
            System.out.println("Error during encrypting: " + e);
        }
        return null;
    }

    public static String decrypt(String str) {
        try {
            byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(str);
            byte[] utf8 = dcipher.doFinal(dec);
            return new String(utf8, CHARSET_NAME);
        } catch (Exception e) {
            System.out.println("Error during decrypting: " + e);
        }
        return null;
    }
}
