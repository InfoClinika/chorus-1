package com.infoclinika.mssharing.web.util;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author timofey.kasyanov
 *         date:   29.01.14
 */
public abstract class FilenameUtil {
    private static final String WHITE_SPACE = " ";
    private static final String UNDERSCORE = "_";

    public static String getPartBefore(String filename, String endPart) {
        int indexOf = filename.lastIndexOf(endPart);
        if (indexOf <= 0) {
            return filename;
        }

        return filename.substring(0, indexOf);

    }

    public static String getBaseName(String filename) {
        int indexOf = filename.indexOf(".");
        if (indexOf <= 0) {
            return filename;
        }

        return filename.substring(0, indexOf);
    }

    public static String getExtension(String filename) {
        int indexOf = filename.indexOf(".");
        if (indexOf <= 0) {
            return "";
        }

        return filename.substring(indexOf);
    }

    public static String replaceWhiteSpacesWithUnderscores(String filename) {
        return filename.replaceAll(WHITE_SPACE, UNDERSCORE);
    }

    public static List<String> expandNameWithSuffixes(String fileName, List<String> suffixList) {
        final List<String> expandedNames = newArrayList();
        final String baseName = FilenameUtil.getBaseName(fileName);
        for (String suffix : suffixList) {
            expandedNames.add(baseName + suffix);
        }

        return expandedNames;
    }

}
