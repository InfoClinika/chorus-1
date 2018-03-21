package com.infoclinika.mssharing.services.jobs;

import com.infoclinika.mssharing.model.internal.repository.*;
import com.infoclinika.mssharing.model.write.InstrumentManagement;
import com.infoclinika.mssharing.model.write.StudyManagement;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Elena Kurilina
 */
@Service
public class CleanTrashJob implements DisposableBean{

    @Inject
    private DeletedFileMetaDataRepository deletedFileMetaDataRepository;
    @Inject
    private DeletedProjectRepository deletedProjectRepository;
    @Inject
    private DeletedExperimentRepository deletedExperimentRepository;
    @Inject
    private ApplicationSettingsRepository applicationSettingsRepository;
    @Inject
    private StudyManagement studyManagement;
    @Inject
    private InstrumentManagement instrumentManagement;

    private static final Logger LOGGER = Logger.getLogger(CleanTrashJob.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);


    @PostConstruct
    public void checkJobs() {
        scheduler.scheduleAtFixedRate(new OldTrashChecker(), 0, 1L, TimeUnit.DAYS);
    }

    @Override
    public void destroy() throws Exception {
        scheduler.shutdownNow();
    }

    private class OldTrashChecker implements Runnable {
        @Override
        public void run() {
            Collection<DeletedItem> projects = deletedProjectRepository.findAllDeleted();
            deleteProjects(projects);
            Collection<DeletedItem> experiments = deletedExperimentRepository.findAllDeleted();
            deleteExperiments(experiments);
            Collection<DeletedItem> files = deletedFileMetaDataRepository.findAllDeleted();
            deleteFiles(files);

        }

        private void deleteProjects(Collection<DeletedItem> items){
            final Date current = new Date();
            final long hours = applicationSettingsRepository.findHoursToStoreInTrash().value;
            for(DeletedItem item : items){
                if(current.getTime() - item.deletionDate.getTime() >= hours * 60 * 1000){
                    try {
                        studyManagement.removeProject(item.id);
                        LOGGER.debug("Item was deleted, id: " + item.id);
                    } catch (Exception e) {
                        LOGGER.error("Clean trash is failed for entity: " + item.id, e);
                    }
                }
            }
        }

        private void deleteFiles(Collection<DeletedItem> items){
            final Date current = new Date();
            final long hours = applicationSettingsRepository.findHoursToStoreInTrash().value;
            for(DeletedItem item : items){
                if(current.getTime() - item.deletionDate.getTime() >= hours * 60 * 1000){
                    try {
                        instrumentManagement.deleteFile(item.id);
                        LOGGER.debug("Item was deleted, id: " + item.id);
                    } catch (Exception e) {
                        LOGGER.error("Clean trash is failed for entity: " + item.id, e);
                    }
                }
            }
        }

        private void deleteExperiments(Collection<DeletedItem> items){
            final Date current = new Date();
            final long hours = applicationSettingsRepository.findHoursToStoreInTrash().value;
            for(DeletedItem item : items){
                if(current.getTime() - item.deletionDate.getTime() >= hours * 60 * 1000){
                    try {
                        studyManagement.removeExperiment(item.id);
                        LOGGER.debug("Item was deleted, id: " + item.id);
                    } catch (Exception e) {
                        LOGGER.error("Clean trash is failed for entity: " + item.id, e);
                    }
                }
            }
        }
    }


}
