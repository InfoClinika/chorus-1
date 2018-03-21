package com.infoclinika.mssharing.model.internal.helper;

import com.infoclinika.mssharing.model.helper.FileMetaInfoHelper;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.entity.FileMetaAnnotations;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkPresence;


/**
 * @author Herman Zamula
 */
@Service
public class FileMetaInfoHelperImpl implements FileMetaInfoHelper {

    public static final Logger LOGGER = Logger.getLogger(FileMetaInfoHelperImpl.class);

    @Inject
    private FileMetaDataRepository fileMetaDataRepository;

    @Override
         public void updateFileMeta(long fileId, MetaInfo metaInfo) {
        //todo: validation?
        final ActiveFileMetaData fileMetaData = checkPresence(fileMetaDataRepository.findOne(fileId));
        final FileMetaAnnotations info = new FileMetaAnnotations(fileMetaData.getId());
        setAnnotations(metaInfo, info);
        fileMetaData.setMetaInfo(info);

        fileMetaDataRepository.save(fileMetaData);
        LOGGER.info("Annotations for file with id '" + fileId + "' has been set");
    }

    @Override
    public void copyFileMetaAnnotation(long to, long from) {
        final ActiveFileMetaData toFile = checkPresence(fileMetaDataRepository.findOne(to));
        final FileMetaAnnotations info = copy(fileMetaDataRepository.findOne(from).getMetaInfo(), to);
        toFile.setMetaInfo(info);
        fileMetaDataRepository.save(toFile);
    }

    private void setAnnotations(MetaInfo metaInfo, FileMetaAnnotations info) {
        info.setComment(metaInfo.comment);
        info.setCreationDate(metaInfo.creationDate);
        info.setFileCondition(metaInfo.fileCondition);
        info.setInstrument(metaInfo.instrument);
        info.setFileName(metaInfo.fileName);
        info.setUserName(metaInfo.userName);
        info.setSampleName(metaInfo.sampleName);
        info.setInstrumentSerialNumber(metaInfo.instrumentSerialNumber);
        info.setPhone(metaInfo.phone);
        info.setInstrumentName(metaInfo.instrumentName);
        info.setUserLabels(metaInfo.userLabels);
        info.setInstrumentMethod(metaInfo.instrumentMethod);
        info.setSeqRowPosition(metaInfo.seqRowPosition);
    }

    private FileMetaAnnotations copy(FileMetaAnnotations metaInfo, long fileId) {
        if(metaInfo == null) return null;
        FileMetaAnnotations copy = new FileMetaAnnotations(fileId);
        copy.setComment(metaInfo.getComment());
        copy.setCreationDate(metaInfo.getCreationDate());
        copy.setFileCondition(metaInfo.getFileCondition());
        copy.setInstrument(metaInfo.getInstrument());
        copy.setFileName(metaInfo.getFileName());
        copy.setUserName(metaInfo.getUserName());
        copy.setSampleName(metaInfo.getSampleName());
        copy.setInstrumentSerialNumber(metaInfo.getInstrumentSerialNumber());
        copy.setPhone(metaInfo.getPhone());
        copy.setInstrumentName(metaInfo.getInstrumentName());
        copy.setUserLabels(metaInfo.getUserLabels());
        copy.setInstrumentMethod(metaInfo.getInstrumentMethod());
        copy.setSeqRowPosition(metaInfo.getSeqRowPosition());
        return copy;
    }
}
