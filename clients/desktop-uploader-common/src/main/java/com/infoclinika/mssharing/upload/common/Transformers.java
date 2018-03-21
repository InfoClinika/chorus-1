package com.infoclinika.mssharing.upload.common;

import com.google.common.base.Function;
import com.infoclinika.mssharing.dto.FileDescription;
import com.infoclinika.mssharing.dto.response.DictionaryDTO;
import com.infoclinika.mssharing.dto.response.InstrumentDTO;
import com.infoclinika.mssharing.upload.common.dto.DictionaryWrapper;
import com.infoclinika.mssharing.upload.common.dto.InstrumentWrapper;

import javax.annotation.Nullable;
import java.io.File;

/**
 * @author timofey.kasyanov
 *         date:   28.01.14
 */
public class Transformers {

    public static final Function<InstrumentDTO, InstrumentWrapper> TO_INSTRUMENT_WRAPPER = new Function<InstrumentDTO, InstrumentWrapper>() {
        @Nullable
        @Override
        public InstrumentWrapper apply(@Nullable InstrumentDTO input) {
            return new InstrumentWrapper(input);
        }
    };

    public static final Function<DictionaryDTO, DictionaryWrapper> TO_DICTIONARY_WRAPPER = new Function<DictionaryDTO, DictionaryWrapper>() {
        @Nullable
        @Override
        public DictionaryWrapper apply(@Nullable DictionaryDTO input) {
            return new DictionaryWrapper(input);
        }
    };

    public static final Function<File, FileDescription> FILE_TO_FILE_DESCRIPTION = new Function<File, FileDescription>() {
        @Override
        public FileDescription apply(File input) {
            return new FileDescription(
                    input.getName(),
                    input.isDirectory(),
                    false
            );
        }
    };
}
