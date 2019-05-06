package com.infoclinika.mssharing.model.internal;

/**
 * @author timofei.kasianov 6/21/16
 */
public class FileNameSpotter {

    private static final String INVALID_SYMBOLS_REGEX = "\\\\|/|:|\\*|\\?|\"|<|>|\\|| ";
    private static final String VALID_SYMBOL = "_";

    public static String replaceInvalidSymbols(String filename) {
        return filename.replaceAll(INVALID_SYMBOLS_REGEX, VALID_SYMBOL);
    }

}
