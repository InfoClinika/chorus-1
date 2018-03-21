package com.infoclinika.mssharing.platform.model.helper.read;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.LabTemplate;
import com.infoclinika.mssharing.platform.model.DefaultTransformers;
import com.infoclinika.mssharing.platform.model.read.LabReaderTemplate.LabLineTemplate;
import com.infoclinika.mssharing.platform.repository.LabRepositoryTemplate;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

import static com.infoclinika.mssharing.platform.model.helper.read.SingleResultBuilder.builder;

/**
 * @author : Alexander Serebriyan
 */
@Component
@Scope(value = "prototype")
public class LabReaderHelper<LAB extends LabTemplate, LAB_LINE extends LabLineTemplate>
        extends AbstractReaderHelper<LAB, LAB_LINE, LabLineTemplate> {

    @Inject
    private LabRepositoryTemplate<LAB> labRepository;

    @Override
    public Function<LAB, LabLineTemplate> getDefaultTransformer() {
        return DefaultTransformers.labLineTemplateTransformer();
    }

    public ResultBuilder<LAB, LAB_LINE> readAllLabs() {
        List<LAB> labs = labRepository.findAll();
        return ResultBuilder.builder(labs, activeTransformer);
    }

    public ResultBuilder<LAB, LAB_LINE> readUserLabs(long actor) {
        List<LAB> userLabs = labRepository.findForUser(actor);
        return ResultBuilder.builder(userLabs, activeTransformer);
    }

    public SingleResultBuilder<LAB, LAB_LINE> readLab(long labId) {
        LAB lab = labRepository.findOne(labId);
        return builder(lab, activeTransformer);
    }

    public SingleResultBuilder<LAB, LAB_LINE> readLabByName(String labName) {
        LAB lab = labRepository.findByName(labName);
        return builder(lab, activeTransformer);
    }

    public ResultBuilder<LAB, LAB_LINE> readLabsByHeadEmail(String headEmail) {
        List<LAB> labs = labRepository.findByHeadEmail(headEmail);
        return ResultBuilder.builder(labs, activeTransformer);
    }
}
