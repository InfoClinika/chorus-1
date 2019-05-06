package com.infoclinika.mssharing.web.controller.v2.service;

import com.infoclinika.mssharing.model.internal.entity.upload.FileDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Vitalii Petkanych
 */
@Service
public class FtpFileImportService implements FileImportService {

    @Override
    public List<FileDetails> listFiles(
        String user,
        String pass,
        String url,
        boolean recursive,
        Predicate<String> filter
    ) {
        return null;
    }

    @Override
    public void copyFiles(
        String user,
        String pass,
        String url,
        List<String> files,
        Function<String, String> dstKeyGenerator
    ) {

    }
}
