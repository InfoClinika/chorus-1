package com.infoclinika.util;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author timofey.kasyanov, Alexander Serebriyan
 *     date:   29.01.14
 */
public final class FilenameUtil {

    private static final String WHITE_SPACE = " ";
    private static final String UNDERSCORE = "_";
    public static final String ALL_FILES_DOT_STAR = ".*";
    public static final String ALL_FILES_STAR_DOT_STAR = "*.*";

    private FilenameUtil() {
        throw new UnsupportedOperationException();
    }

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

    public static String replaceForbiddenPathCharacters(final String str) {
        return str.replaceAll("[/,*?<>|\":\\\\]+", "_");
    }

    public static boolean containExtension(List<String> extensions, String filename) {
        if (extensions.contains(ALL_FILES_DOT_STAR) || extensions.contains(ALL_FILES_STAR_DOT_STAR)) {
            return true;
        }

        for (String extension : extensions) {
            if (filename.endsWith(extension)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isAllFiles(List<String> masks) {
        return masks == null ||
            masks.isEmpty() ||
            masks.contains(FilenameUtil.ALL_FILES_DOT_STAR) ||
            masks.contains(FilenameUtil.ALL_FILES_STAR_DOT_STAR);
    }

}
