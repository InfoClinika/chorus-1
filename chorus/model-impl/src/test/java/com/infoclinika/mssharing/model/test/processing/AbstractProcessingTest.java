package com.infoclinika.mssharing.model.test.processing;

import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.model.helper.AbstractTest;
import com.infoclinika.mssharing.model.helper.ExperimentSampleItem;
import com.infoclinika.mssharing.model.internal.read.ProcessingRunReader;
import com.infoclinika.mssharing.model.internal.read.ProcessingFileReader;
import com.infoclinika.mssharing.model.read.dto.details.ExperimentItem;
import com.infoclinika.mssharing.model.read.dto.details.FileItem;
import com.infoclinika.mssharing.model.write.ProcessingFileManagement;
import com.infoclinika.mssharing.model.write.ProcessingRunManagement;
import com.infoclinika.mssharing.model.write.ProjectInfo;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;

import javax.inject.Inject;
import java.util.*;

import static com.infoclinika.mssharing.model.helper.Data.PROJECT_TITLE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class AbstractProcessingTest extends AbstractTest{


    @Inject
    protected ProcessingFileReader processingFileReader;
    @Inject
    protected ProcessingFileManagement processingFileManagement;
    @Inject
    protected ProcessingRunManagement processingRunManagement;
    @Inject
    protected ProcessingRunReader processingRunReader;



    protected long createProcessingFile(long experiment, FileItem fileItem){
        ProcessingFileManagement.ProcessingFileShortInfo processingFileShortInfo = new ProcessingFileManagement.ProcessingFileShortInfo(fileItem.name, "processed-file/" + experiment + "/" + fileItem.name);
        return processingFileManagement.createProcessingFile(experiment, processingFileShortInfo);
    }

    protected long createProcessingRun(long experiment, String name){
        return processingRunManagement.create(experiment, name);
    }

    protected Map<String, Collection<String>> createFileToFileMap(FileItem fileItem, long processingFileId){
        ProcessingFileReader.ProcessingFileInfo processingFileInfo = processingFileReader.readProcessingFileInfo(processingFileId);

        Map<String, Collection<String>> map = new HashMap<>();
        List<String> collection = new ArrayList<>();
        collection.add(fileItem.name);
        map.put(processingFileInfo.name, collection);

        return map;
    }

    protected void assertProcessingFilesIsAssociateExperimentFile(long processingFileId, long fileId){

        ProcessingFileReader.ProcessingFileInfo processingFileInfo = processingFileReader.readProcessingFileInfo(processingFileId);
        assertTrue(Iterables.any(processingFileInfo.fileMetaDataTemplateList, new Predicate<FileMetaDataTemplate>() {
            @Override
            public boolean apply(FileMetaDataTemplate input) {
                return input.getId() == fileId;
            }
        }));
        assertTrue(processingFileInfo.processingRuns.size() > 0);
        assertTrue(processingFileInfo.fileMetaDataTemplateList.size() == 1);
    }


    protected void assertMultipartProcessingFilesIsAssociateExperimentFile(long processingFileId, long fileId, long experiment){

        ProcessingFileReader.ProcessingFileInfo processingFileInfo = processingFileReader.readProcessingFileInfo(processingFileId);
        assertTrue(Iterables.any(processingFileInfo.fileMetaDataTemplateList, new Predicate<FileMetaDataTemplate>() {
            @Override
            public boolean apply(FileMetaDataTemplate input) {
                return input.getId() == fileId;
            }
        }));

        assertTrue(processingFileInfo.processingRuns.size() > 0);
        assertEquals(Optional.ofNullable(experiment).get(), processingFileInfo.abstractExperiment.getId());
    }


    protected List<Long> createMultiProcessingFiles(ExperimentItem experimentItem){

        List<Long> list = new ArrayList<>();
        for(int i = 0; i < experimentItem.files.size(); i++){
            String file = "file-test"+UUID.randomUUID().toString()+".RAW";
            ProcessingFileManagement.ProcessingFileShortInfo processingFileShortInfo = new ProcessingFileManagement.ProcessingFileShortInfo(file, "processed-file/" + experimentItem.id +"/"+file);
            long id = processingFileManagement.createProcessingFile(experimentItem.id, processingFileShortInfo);
            list.add(id);
        }
        return list;
    }

    protected Map<String, Collection<String>> createFileToFileMap(ExperimentItem experimentItem, List<Long> list){
        Map<String, Collection<String>> map = new HashMap<>();
        List<String> collection = new ArrayList<>();
        for(int i = 0; i<list.size(); i++){
            ProcessingFileReader.ProcessingFileInfo processingFileInfo = processingFileReader.readProcessingFileInfo(list.get(i));
            collection.add(experimentItem.files.get(i).name);
            map.put(processingFileInfo.name, collection);
        }

        return map;
    }

    private void createSamplesToFileMap(ExperimentItem experimentItem, Map<String, Collection<String>> fileToFileMap){

        Collection<String> collection = new ArrayList();
        Map<String, Collection<String>> sampleFilesMap = new HashMap();

        ImmutableList<DetailsReaderTemplate.FileItemTemplate> files = experimentItem.files;
        for(DetailsReaderTemplate.FileItemTemplate fileItemTemplate : files){
//            fileItemTemplate.
        }
    }


    protected long createExperimentWithOneRawFile(long user, long lab) {
        final long project = studyManagement.createProject(user, new ProjectInfo(PROJECT_TITLE, "DNA", "Some proj", lab));
        return createInstrumentAndExperimentWithOneFile(user, lab, project);
    }


}
