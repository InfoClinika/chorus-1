package com.infoclinika.mssharing.model.internal.migration;

import com.infoclinika.mssharing.model.internal.entity.CopyProjectRequest;
import com.infoclinika.mssharing.model.internal.entity.Instrument;
import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.entity.payment.LabPaymentAccount;
import com.infoclinika.mssharing.model.internal.entity.restorable.*;
import com.infoclinika.mssharing.model.internal.repository.*;
import com.infoclinika.mssharing.model.write.ExperimentInfo;
import com.infoclinika.mssharing.platform.entity.UserLabMembership;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate;
import com.infoclinika.mssharing.platform.repository.UserLabMembershipRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.UserProjectAccessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author : Alexander Serebriyan
 */
@Service
public class MigrationHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationHelper.class);

    private List<Long> usersToLeave;
    private List<Long> labsToLeave;

    private final ProjectRepository projectRepository;
    private final ExperimentRepository experimentRepository;
    private final DeletedExperimentRepository deletedExperimentRepository;
    private final FileMetaDataRepository fileMetaDataRepository;
    private final RawFilesRepository experimentFileRepository;
    private final InstrumentRepository instrumentRepository;
    private final LabRepository labRepository;
    private final UserRepository userRepository;
    private final UserLabMembershipRepositoryTemplate<User, Lab> userLabMembershipRepository;
    private final UserProjectAccessRepository userProjectAccessRepository;
    private final LabPaymentAccountRepository labPaymentAccountRepository;
    private final ExperimentManagementTemplate<ExperimentInfo> experimentManagement;
    private final CopyProjectRequestRepository copyProjectRequestRepository;


    @Inject
    public MigrationHelper(
        ProjectRepository projectRepository,
        ExperimentRepository experimentRepository,
        DeletedExperimentRepository deletedExperimentRepository,
        FileMetaDataRepository fileMetaDataRepository,
        RawFilesRepository experimentFileRepository,
        InstrumentRepository instrumentRepository,
        LabRepository labRepository,
        UserRepository userRepository,
        UserLabMembershipRepositoryTemplate<User, Lab> userLabMembershipRepositoryTemplate,
        UserProjectAccessRepository userProjectAccessRepository,
        LabPaymentAccountRepository labPaymentAccountRepository,
        @Named("experimentManagementImpl")
            ExperimentManagementTemplate<ExperimentInfo> experimentManagement,
        CopyProjectRequestRepository copyProjectRequestRepository
    ) {
        this.projectRepository = projectRepository;
        this.experimentRepository = experimentRepository;
        this.deletedExperimentRepository = deletedExperimentRepository;
        this.fileMetaDataRepository = fileMetaDataRepository;
        this.experimentFileRepository = experimentFileRepository;
        this.instrumentRepository = instrumentRepository;
        this.labRepository = labRepository;
        this.userRepository = userRepository;
        this.userLabMembershipRepository = userLabMembershipRepositoryTemplate;
        this.userProjectAccessRepository = userProjectAccessRepository;
        this.labPaymentAccountRepository = labPaymentAccountRepository;
        this.experimentManagement = experimentManagement;
        this.copyProjectRequestRepository = copyProjectRequestRepository;
    }


    public void removeData() {
        // removeProject(projectRepository.findOne(126L));
        removeDeletedExperiments();
        removeProjects();
        removeFiles();
        removeInstruments();
        removeLabs();
        // removeUsers();
    }

    private List<Long> labsToLeave() {

        if (labsToLeave != null) {
            return labsToLeave;
        }

        labsToLeave = newArrayList(1L, 2L, 5L, 6L, 8L);
        return labsToLeave;
    }

    public long getAdmin() {
        return 1L;
    }

    private List<Long> usersToLeave() {

        if (usersToLeave != null) {
            return usersToLeave;
        }

        final HashSet<Long> leave = new HashSet<>();
        for (Long lab : labsToLeave()) {
            final List<UserLabMembership<User, Lab>> memberships = userLabMembershipRepository.findByLab(lab);
            for (UserLabMembership<User, Lab> membership : memberships) {
                leave.add(membership.getUser().getId());
            }
        }

        this.usersToLeave = new LinkedList<>(leave);
        return this.usersToLeave;
    }

    private void removeDeletedExperiments() {
        final List<DeletedExperiment> all = deletedExperimentRepository.findAll();
        for (DeletedExperiment deletedExperiment : all) {
            removeExperimentFiles(deletedExperiment);
            deletedExperimentRepository.delete(deletedExperiment);
        }
    }

    private void removeExperimentFiles(AbstractExperiment e) {
        LOGGER.info("Removing experiment files.");
        experimentFileRepository.deleteByExperimentId(e.getId());
        LOGGER.info("Experiment files removed.");
    }

    private void removeProjects() {
        final List<ActiveProject> allProjects = projectRepository.findAll();
        final List<ActiveProject> projectsToRemove = allProjects.stream()
            .filter(project -> {
                final Lab lab = project.getLab();
                final User creator = project.getCreator();
                return !isInWhiteList(lab, creator);
            })
            .collect(Collectors.toList());

        int number = 1;
        for (ActiveProject project : projectsToRemove) {
            LOGGER.info("Removing project #{} out of {}", number++, projectsToRemove.size());
            removeProject(project);
        }
    }

    // check if specified lab and user belong to list of items that shouldn't be removed
    private boolean isInWhiteList(Lab lab, User user) {
        final boolean belongToLab = lab != null && labsToLeave().contains(lab.getId());
        final boolean belongToUser = usersToLeave().contains(user.getId());
        return belongToLab || belongToUser;
    }


    @Transactional
    public void removeProject(ActiveProject project) {

        LOGGER.info("Removing project: {}", project.getId());
        boolean hasExperimentToLeave = false;
        final List<ActiveExperiment> experiments =
            experimentRepository.findByProjectAndDoNotFetchFiles(project.getId());

        for (ActiveExperiment e : experiments) {
            final Lab lab = e.getLab();
            final User creator = e.getCreator();
            if (isInWhiteList(lab, creator)) {
                hasExperimentToLeave = true;
                continue;
            }

            removeExperimentFiles(e);
            LOGGER.info("Removing experiment: {}", e.getId());

            experimentManagement.deleteExperiment(e.getCreator().getId(), e.getId());
            LOGGER.info("Experiment removed.");
        }

        // don't delete project if it has at least one experiment that shouldn't be removed
        if (hasExperimentToLeave) {
            return;
        }

        LOGGER.info("Removing user's access to project...");
        userProjectAccessRepository.deleteByProjectId(project.getId());
        LOGGER.info("Removing copy project requests...");
        final List<CopyProjectRequest> requests = copyProjectRequestRepository.findByProject(project);
        copyProjectRequestRepository.delete(requests);
        LOGGER.info("Removing project.");
        projectRepository.delete(project);
        LOGGER.info("Project removed.");
    }


    @Transactional
    public void removeFiles() {
        LOGGER.info("Removing files...");
        int pageNumber = 0;
        Page<ActiveFileMetaData> page = fileMetaDataRepository.findAll(new PageRequest(pageNumber, 500));
        while (page.hasContent()) {
            LOGGER.info("Removing page {} out of {}", pageNumber, page.getTotalPages());
            final List<ActiveFileMetaData> files = page.getContent();

            final LinkedList<Long> idsToDelete = new LinkedList<>();

            for (ActiveFileMetaData file : files) {
                if (!isInWhiteList(file.getInstrument().getLab(), file.getOwner())) {
                    experimentFileRepository.deleteByFileMetadataId(file.getId());
                    idsToDelete.add(file.getId());
                }
            }
            LOGGER.info("Removing {} files: ", idsToDelete.size());

            if (!idsToDelete.isEmpty()) {
                fileMetaDataRepository.deleteByIds(idsToDelete);
            }

            page = fileMetaDataRepository.findAll(new PageRequest(++pageNumber, 500));
        }
    }


    @Transactional
    public void removeInstruments() {
        LOGGER.info("Removing instruments...");
        final List<Instrument> allInstruments = instrumentRepository.findAll();

        final List<Instrument> instrumentsToRemove = allInstruments.stream()
            .filter(i -> !isInWhiteList(i.getLab(), i.getCreator()) &&
                fileMetaDataRepository.countByInstrument(i.getId()) == 0 &&
                experimentRepository.countByInstrument(i.getId()) == 0)
            .collect(Collectors.toList());

        LOGGER.info("Removing {} instruments...", instrumentsToRemove.size());
        instrumentRepository.delete(instrumentsToRemove);

        LOGGER.info("Instruments removed.");
    }

    @Transactional
    public void removeLabs() {

        LOGGER.info("Removing labs...");
        final List<Lab> allLabs = labRepository.findAll();
        final List<Lab> labsToRemove = allLabs.stream()
            .filter(l -> !labsToLeave().contains(l.getId()) &&
                instrumentRepository.findByLab(l.getId()).isEmpty() &&
                experimentRepository.countByLabId(l.getId()) == 0 &&
                experimentRepository.countByBillLabId(l.getId()) == 0 &&
                projectRepository.countByLabId(l.getId()) == 0)
            .collect(Collectors.toList());

        LOGGER.info("Removing {} labs...", labsToRemove.size());
        for (Lab lab : labsToRemove) {
            final LabPaymentAccount paymentAccount = labPaymentAccountRepository.findByLab(lab.getId());
            if (paymentAccount != null) {
                labPaymentAccountRepository.delete(paymentAccount);
            }
            userLabMembershipRepository.deleteByLab(lab.getId());
        }

        labRepository.delete(labsToRemove);
        LOGGER.info("Labs removed.");
    }

    @Transactional
    public void removeUsers() {
        LOGGER.info("Remove users...");
        final List<User> allUsers = userRepository.findAll();

        final LinkedList<User> usersToRemove = new LinkedList<>();
        for (User user : allUsers) {
            if (!usersToLeave.contains(user.getId()) &&
                userLabMembershipRepository.findByUser_id(user.getId()).isEmpty()) {
                usersToRemove.add(user);
            }
        }

        LOGGER.info("Removing {} users...", usersToRemove.size());
        LOGGER.info("Removeing user lab memberships...");

        userRepository.delete(usersToRemove);

        LOGGER.info("Users removed.");
    }
}
