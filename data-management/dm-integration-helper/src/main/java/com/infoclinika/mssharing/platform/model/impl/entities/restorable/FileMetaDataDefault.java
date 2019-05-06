package com.infoclinika.mssharing.platform.model.impl.entities.restorable;

import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.model.impl.entities.InstrumentDefault;
import com.infoclinika.mssharing.platform.model.impl.entities.UserDefault;

import javax.persistence.Entity;

/**
 * @author Herman Zamula
 */
@Entity
public class FileMetaDataDefault extends FileMetaDataTemplate<UserDefault, InstrumentDefault> {
    @Override
    public FileMetaDataTemplate copy(String copyName, UserTemplate owner) {
        final FileMetaDataDefault fmd = new FileMetaDataDefault();
        fmd.setDeleted(this.isDeleted());
        fmd.setName(copyName);
        fmd.setContentId(this.getContentId());
        fmd.setInstrument(this.getInstrument());
        fmd.setInvalid(this.isInvalid());
        fmd.setCopy(true);
        fmd.setOwner((UserDefault) owner);
        fmd.setLabels(this.getLabels());
        fmd.setDestinationPath(this.getDestinationPath());
        fmd.setSizeInBytes(this.getSizeInBytes());
        fmd.setLastModification(this.getLastModification());
        fmd.setUploadDate(this.getUploadDate());
        fmd.setUploadId(this.getUploadId());
        fmd.setSpecie(this.getSpecie());
        return fmd;
    }
}
