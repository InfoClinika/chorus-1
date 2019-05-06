package com.infoclinika.mssharing.model.internal.write;

import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.write.FileMetaDataInfo;
import com.infoclinika.mssharing.platform.model.impl.write.DefaultFileManagement;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author Herman Zamula
 */
@Component("fileManagement")
public class FileManagementImpl extends DefaultFileManagement<ActiveFileMetaData, FileMetaDataInfo> {


    @Override
    protected void beforeCreateFile(long actor, long instrument, FileMetaDataInfo fileMetaDataInfo) {
        super.beforeCreateFile(actor, instrument, fileMetaDataInfo);
    }

    @Override
    protected void afterCreateFile(ActiveFileMetaData updated, FileMetaDataInfo fileMetaDataInfo) {
        super.afterCreateFile(updated, fileMetaDataInfo);
        final Date current = new Date();
        updated.setLastAccess(current);
        updated.setArchive(fileMetaDataInfo.archive);
        updated.setLastPingDate(current);
    }
}
