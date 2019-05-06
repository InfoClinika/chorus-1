package com.infoclinika.mssharing.platform.model.helper.write;

import com.infoclinika.mssharing.platform.entity.Species;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.model.EntityFactories;
import com.infoclinika.mssharing.platform.model.write.FileManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.FileManagementTemplate.FileMetaDataInfoTemplate;
import com.infoclinika.mssharing.platform.repository.FileRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.SpeciesRepositoryTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Date;

/**
 * @author Herman Zamula
 */
@Component
public class FileManager<FILE extends FileMetaDataTemplate> {

    @Inject
    private EntityFactories factories;
    @Inject
    private FileRepositoryTemplate<FILE> fileMetaDataRepository;
    @Inject
    private SpeciesRepositoryTemplate<Species> speciesRepository;

    @SuppressWarnings("unchecked")
    public FILE setCreationValues(FILE toUpdate, long actor, long instrument, FileMetaDataInfoTemplate file) {

        toUpdate.setInstrument(factories.instrumentFromId.apply(instrument));
        toUpdate.setOwner(factories.userFromId.apply(actor));
        toUpdate.setUploadDate(new Date());
        setTemplateProperties(toUpdate, file);

        return toUpdate;
    }

    public FILE newFile() {
        //noinspection unchecked
        return (FILE) factories.fileMetaData.get();
    }

    public FILE saveFile(FILE file) {
        file.setLastModification(new Date());
        return fileMetaDataRepository.save(file);
    }

    public FILE fromId(long id) {
        return fileMetaDataRepository.findOne(id);
    }

    public void remove(long id) {
        fileMetaDataRepository.delete(id);
    }

    public void setTemplateProperties(FileMetaDataTemplate metaDataTemplate,
                                      FileMetaDataInfoTemplate file) {
        metaDataTemplate.setName(file.fileName);
        metaDataTemplate.setSpecie(speciesRepository.findOne(file.species));
        metaDataTemplate.setLabels(file.labels);
        metaDataTemplate.setDestinationPath(file.destinationPath);
        metaDataTemplate.setSizeInBytes(file.sizeInBytes);
        metaDataTemplate.setBucket(file.bucket);
        metaDataTemplate.setContentId(file.storedKey);
        metaDataTemplate.setReadOnly(file.readOnly);
    }
}
