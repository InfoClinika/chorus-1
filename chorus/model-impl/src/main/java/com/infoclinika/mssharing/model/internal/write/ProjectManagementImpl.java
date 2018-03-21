package com.infoclinika.mssharing.model.internal.write;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.model.helper.FileMetaInfoHelper;
import com.infoclinika.mssharing.model.internal.FileNameSpotter;
import com.infoclinika.mssharing.model.internal.entity.*;
import com.infoclinika.mssharing.model.internal.entity.restorable.*;
import com.infoclinika.mssharing.model.internal.entity.PrepToExperimentSample;
import com.infoclinika.mssharing.model.internal.repository.FactorRepository;
import com.infoclinika.mssharing.model.internal.repository.LabRepository;
import com.infoclinika.mssharing.model.write.ProjectInfo;
import com.infoclinika.mssharing.model.write.StudyManagement;
import com.infoclinika.mssharing.platform.entity.ExperimentFileTemplate;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.model.impl.write.DefaultProjectManagement;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

/**
 * @author : Alexander Serebriyan
 */
@Component
public class ProjectManagementImpl extends DefaultProjectManagement<ActiveProject, ProjectInfo> {

    @Inject
    private FileMetaInfoHelper fileMetaInfoHelper;
    @Inject
    private FactorRepository factorRepository;
    @Inject
    private LabRepository labRepository;

    @Override
    protected void beforeCopyProject(long actor, CopyProjectInfoTemplate copyInfo) {
        super.beforeCopyProject(actor, copyInfo);
        checkNotNull(labRepository.findOne(toChorusInfo(copyInfo).getBillLab()));
    }

    private StudyManagement.CopyProjectInfo toChorusInfo(CopyProjectInfoTemplate copyProjectInfo) {
        return (StudyManagement.CopyProjectInfo) copyProjectInfo;
    }


    private ActiveExperiment saveExperiment(ActiveExperiment activeExperiment) {
        activeExperiment.setLastModification(new Date());
        return experimentRepository.save(activeExperiment);
    }


    @Override
    protected void onCopyExperimentRawFiles(UserTemplate newOwner, ExperimentTemplate originalExperiment, ExperimentTemplate experimentCopy, CopyProjectInfoTemplate copyInfo) {
        ActiveExperiment from = (ActiveExperiment) originalExperiment;
        ActiveExperiment to = (ActiveExperiment) experimentCopy;
        List<Factor> copiedFactors = new ArrayList<>();
        final StudyManagement.CopyProjectInfo copyProjectInfo = toChorusInfo(copyInfo);
        final Function<AbstractFileMetaData, AbstractFileMetaData> copyMetaDataFn = createCopyMetaDataFn(newOwner, labRepository.findOne(copyProjectInfo.getBillLab()));
        final Collection<RawFile> copiedFiles = transform(from.rawFiles.getData(), new Function<ExperimentFileTemplate, RawFile>() {
            @Override
            public RawFile apply(ExperimentFileTemplate from) {
                //noinspection unchecked
                final RawFile rawFile = (RawFile) from;
                RawFile copyFile = new RawFile(copyMetaDataFn.apply((AbstractFileMetaData) from.getFileMetaData()), rawFile.getFractionNumber(), rawFile.getPreparedSample());
                copyFile.setCopy(true);
                return copyFile;
            }
        });
        for (Factor factor : from.rawFiles.getFactors()) {
            copiedFactors.add(factorRepository.save(Factor.createCopy(factor, to)));
        }
        to.rawFiles.getData().clear();
        to.rawFiles.getData().addAll(copiedFiles);
        to.rawFiles.getFactors().clear();
        to.rawFiles.getFactors().addAll(copiedFactors);

        addConditionsToLevelsAndRawFiles(to);
    }

    @Override
    protected ExperimentTemplate onCopyExperiment(ActiveProject copyOfProject, UserTemplate newOwner, ExperimentTemplate origin, CopyProjectInfoTemplate copyInfo) {
        final boolean isNameUsed = experimentRepository.findOneByName(newOwner.getId(), origin.getName()) != null;
        final Lab billLab = labRepository.findOne(toChorusInfo(copyInfo).getBillLab());
        final String copyName = createCopyName(origin.getName(), isNameUsed);
        final ActiveExperiment experimentCopy = copyExperimentFor(copyOfProject, (ActiveExperiment) origin, billLab, copyName);
        projectManager.setDownloadToken(copyOfProject.getSharing().getType(), experimentCopy);
        return saveExperiment(experimentCopy);

    }

    private Function<AbstractFileMetaData, AbstractFileMetaData> createCopyMetaDataFn(final UserTemplate newOwner, final Lab newBillLab) {

        final Map<AbstractFileMetaData, AbstractFileMetaData> originalCopyMap = newHashMap();

        return new Function<AbstractFileMetaData, AbstractFileMetaData>() {
            @Override
            public AbstractFileMetaData apply(AbstractFileMetaData originalMeta) {
                if (originalCopyMap.containsKey(originalMeta)) {
                    return originalCopyMap.get(originalMeta);
                }
                final AbstractFileMetaData copied = copyFileMetadata(originalMeta, newOwner, newBillLab);
                originalCopyMap.put(originalMeta, copied);
                return copied;
            }
        };
    }

    private void addConditionsToLevelsAndRawFiles(ExperimentTemplate ex) {
        final Set<ExperimentSample> samples = newHashSet();
        //noinspection unchecked
        final List<RawFile> rawFiles = ex.getRawFiles().getData();
        for (RawFile rawFile : rawFiles) {
            for (PrepToExperimentSample prepToExperimentSample : rawFile.getPreparedSample().getSamples()) {
                samples.add(prepToExperimentSample.getExperimentSample());
            }
        }
        new SampleConditionsFactory((AbstractExperiment) ex, ex.rawFiles.getFactors(), samples).create();
    }

    private String createCopyName(String oldName, boolean isNameUsed) {
        if (oldName.lastIndexOf(".") != -1) {
            final String extension = oldName.substring(oldName.lastIndexOf("."), oldName.length());
            final String clearOldName = oldName.substring(0, oldName.lastIndexOf("."));
            return isNameUsed ? "Copy of " + clearOldName + " " + currentDateFormatted() + extension : oldName;
        }
        //For files without extension
        return isNameUsed ? "Copy of " + oldName + " " + currentDateFormatted() : oldName;
    }

    private String currentDateFormatted() {
        final SimpleDateFormat format = new SimpleDateFormat("MMM dd yyyy hh_mm_ss", Locale.ENGLISH);
        return format.format(new Date());
    }

    private ActiveExperiment copyExperimentFor(ActiveProject project, AbstractExperiment experiment, Lab newBillLab, String copyName) {

        ActiveExperiment copy = (ActiveExperiment) projectManager.copyExperimentData(project, experiment, copyName);

        copy.setBillLaboratory(newBillLab);
        copy.setExperimentCategory(experiment.getExperimentCategory());
        return copy;
    }

    private AbstractFileMetaData copyFileMetadata(AbstractFileMetaData from, UserTemplate owner, Lab newBillLab) {
        final String copyName = FileNameSpotter.replaceInvalidSymbols(createCopyName(from.getName(), true));

        final ActiveFileMetaData toSave = (ActiveFileMetaData) from.copy(copyName, owner);
        toSave.setBillLab(newBillLab);
        ActiveFileMetaData copy = (ActiveFileMetaData) fileMetaDataRepository.save(toSave);
        fileMetaInfoHelper.copyFileMetaAnnotation(copy.getId(), from.getId());
        //noinspection unchecked
        return (AbstractFileMetaData) fileMetaDataRepository.findOne(copy.getId());
    }

}
