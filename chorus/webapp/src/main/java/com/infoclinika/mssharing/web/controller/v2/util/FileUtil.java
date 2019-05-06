package com.infoclinika.mssharing.web.controller.v2.util;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Vitalii Petkanych
 */
public final class FileUtil {

    public static String extractName(String fullName) {
        return fullName.substring(fullName.lastIndexOf("/") + 1);
    }

    public static String formatDuration(Duration duration) {
        return DateTimeFormatter.ISO_LOCAL_TIME.format(LocalTime.ofNanoOfDay(duration.toNanos()));
    }

    public static String formatSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[] {"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
