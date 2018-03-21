package com.infoclinika.mssharing.integration.test.utils;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

/**
 * @author Alexander Orlov
 */
public class Strings {

    public static String randomizeName(String name) {
        return name + "-" + RandomStringUtils.randomAlphanumeric(5);
    }

    public static String randomInt(){
        return RandomStringUtils.randomNumeric(5);
    }

    public static int getRandomNumberFrom(int min, int max) {
        Random random = new Random();
        return random.nextInt((max + 1) - min) + min;
    }

    public static String randomizeFileName(String fileName){
        return fileName.replace(".", "-" + RandomStringUtils.randomAlphanumeric(3) + ".");
    }
}
