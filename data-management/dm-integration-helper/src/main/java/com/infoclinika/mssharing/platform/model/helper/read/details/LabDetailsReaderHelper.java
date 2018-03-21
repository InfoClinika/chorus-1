package com.infoclinika.mssharing.platform.model.helper.read.details;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.LabTemplate;
import com.infoclinika.mssharing.platform.model.helper.read.AbstractReaderHelper;
import com.infoclinika.mssharing.platform.model.helper.read.SingleResultBuilder;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.LabItemTemplateDetailed;
import com.infoclinika.mssharing.platform.repository.LabRepositoryTemplate;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static com.infoclinika.mssharing.platform.model.helper.read.SingleResultBuilder.builder;

/**
 * @author Herman Zamula
 */
@Component
@Scope(value = "prototype")
public class LabDetailsReaderHelper<LAB extends LabTemplate, LAB_ITEM extends LabItemTemplateDetailed>
        extends AbstractReaderHelper<LAB, LAB_ITEM, LabItemTemplateDetailed> {

    @Inject
    private LabRepositoryTemplate<LAB> labRepository;

    public SingleResultBuilder<LAB, LAB_ITEM> readLab(long lab) {
        return builder(labRepository.findOne(lab), activeTransformer);
    }

    @Override
    public Function<LAB, LabItemTemplateDetailed> getDefaultTransformer() {
        return new Function<LAB, LabItemTemplateDetailed>() {
            @Override
            public LabItemTemplateDetailed apply(LAB lab) {

                return new LabItemTemplateDetailed(lab.getId(),
                        lab.getName(),
                        lab.getInstitutionUrl(),
                        lab.getHead().getFirstName(),
                        lab.getHead().getLastName(),
                        lab.getHead().getEmail(),
                        lab.getContactEmail(),
                        lab.getLastModification(),
                        labRepository.membersCount(lab.getId())
                );
            }
        };
    }
}
