package com.infoclinika.mssharing.platform.model.impl.read.adapters;

import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.model.impl.read.DefaultFileReader;
import com.infoclinika.mssharing.platform.model.read.FileReaderTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Herman Zamula
 */
@Component
public class DefaultFileReaderAdapter
    extends DefaultFileReader<FileMetaDataTemplate, FileReaderTemplate.FileLineTemplate> {
    @Override
    public FileLineTemplate transform(FileMetaDataTemplate fileMetaDataTemplate) {
        return fileReaderHelper.getDefaultTransformer().apply(fileMetaDataTemplate);
    }
}
