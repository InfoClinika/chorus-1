package com.infoclinika.mssharing.web.controller.v2.service;

import com.infoclinika.mssharing.model.internal.entity.upload.FileDetails;
import com.infoclinika.util.FilenameUtil;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Vitalii Petkanych
 */
public interface FileImportService {

    default Predicate<String> buildFilter(List<String> masks) {
        if (FilenameUtil.isAllFiles(masks)) {
            return f -> true;
        } else {
            final String regExp = masks.stream()
                .map(mask -> mask.toLowerCase().replace(".", "\\.").replace("*", ".*"))
                .collect(Collectors.joining("|", "^", "$"));
            final Pattern pattern = Pattern.compile(regExp, Pattern.CASE_INSENSITIVE);
            return fileName -> pattern.matcher(fileName.toLowerCase()).find();
        }
    }

    List<FileDetails> listFiles(String user, String pass, String url, boolean recursive, Predicate<String> filter);

    void copyFiles(String user, String pass, String url, List<String> files, Function<String, String> dstKeyGenerator);
}
