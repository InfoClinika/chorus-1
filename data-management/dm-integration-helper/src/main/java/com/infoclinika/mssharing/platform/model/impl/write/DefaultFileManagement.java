package com.infoclinika.mssharing.platform.model.impl.write;

import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.model.RuleValidator;
import com.infoclinika.mssharing.platform.model.helper.write.FileManager;
import com.infoclinika.mssharing.platform.model.write.FileManagementTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkAccess;

/**
 * @author Herman Zamula
 */
@Transactional
@Component
public class DefaultFileManagement<
        FILE extends FileMetaDataTemplate,
        FILE_META_DATA_INFO extends FileManagementTemplate.FileMetaDataInfoTemplate> implements FileManagementTemplate<FILE_META_DATA_INFO> {

    @Inject
    private FileManager<FILE> helper;

    @Inject
    private RuleValidator ruleValidator;

    @Override
    public long createFile(long actor, long instrument, FILE_META_DATA_INFO fileMetaDataInfo) {

        beforeCreateFile(actor, instrument, fileMetaDataInfo);

        final FILE toUpdate = helper.newFile();
        final FILE updated = helper.setCreationValues(toUpdate, actor, instrument, fileMetaDataInfo);

        afterCreateFile(updated, fileMetaDataInfo);

        return helper.saveFile(updated).getId();
    }

    protected void beforeCreateFile(long actor, long instrument, FILE_META_DATA_INFO fileMetaDataInfo) {
        checkAccess(ruleValidator.canFileBeUploadedByInstrument(fileMetaDataInfo.archive, instrument),
                "Vendor of this instrument doesn't provide an ability to upload archives");

    }

    protected void afterCreateFile(FILE updated, FILE_META_DATA_INFO fileMetaDataInfo) {
    }

    @Override
    public void updateFile(long actor, long file, FILE_META_DATA_INFO fileMetaDataInfo) {

        beforeUpdateFile(actor, file, fileMetaDataInfo);

        final FILE metaData = helper.fromId(file);

        helper.setTemplateProperties(metaData, fileMetaDataInfo);

        afterUpdateFile(metaData, fileMetaDataInfo);

    }

    protected void beforeUpdateFile(long actor, long file, FILE_META_DATA_INFO fileMetaDataInfo) {
    }

    protected void afterUpdateFile(FILE updated, FILE_META_DATA_INFO fileMetaDataInfo) {
    }

    @Override
    public void deleteFile(long actor, long file, boolean permanently) {

        beforeDeleteFile(actor, file, permanently);

        final FILE metaData = helper.fromId(file);
        if (permanently) {
            helper.remove(file);
        } else {
            metaData.setDeleted(true);
            helper.saveFile(metaData);
        }
    }

    protected void beforeDeleteFile(long actor, long file, boolean permanently) {

        checkAccess(ruleValidator.canRemoveFile(actor, file), "Cannot move file to trash");

    }
}
