package com.infoclinika.mssharing.wizard.upload.service.util;

import com.google.common.base.Function;
import com.infoclinika.mssharing.dto.response.FileDTO;

import java.io.File;

/**
 * @author timofey.kasyanov
 *         date:   28.01.14
 */
public class Transformers {

    public static Function<FileDTO, String> FILE_DTO_TO_STRING = new Function<FileDTO, String>() {
        @Override
        public String apply(FileDTO input) {
            return input.getName();
        }
    };

    public static Function<File, String> FILE_TO_FILENAME = new Function<File, String>() {
        @Override
        public String apply(File input) {
            return input.getName();
        }
    };

}
