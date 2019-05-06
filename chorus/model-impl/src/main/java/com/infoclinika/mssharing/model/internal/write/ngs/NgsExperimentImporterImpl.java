package com.infoclinika.mssharing.model.internal.write.ngs;

import com.infoclinika.mssharing.model.internal.write.ngs.api.NgsExperimentCreator;
import com.infoclinika.mssharing.model.internal.write.ngs.api.NgsExperimentTemplateDataValidator;
import com.infoclinika.mssharing.model.internal.write.ngs.api.NgsExperimentTemplateParser;
import com.infoclinika.mssharing.model.internal.write.ngs.api.dto.NgsExperimentCreateRequest;
import com.infoclinika.mssharing.model.internal.write.ngs.api.dto.NgsExperimentTemplateData;
import com.infoclinika.mssharing.model.internal.write.ngs.api.dto.NgsExperimentTemplateValidationResult;
import com.infoclinika.mssharing.model.internal.write.ngs.api.dto.NgsExperimentTemplateValidationResult.ValidationError;
import com.infoclinika.mssharing.model.write.ngs.NgsExperimentImportException;
import com.infoclinika.mssharing.model.write.ngs.NgsExperimentImporter;
import com.infoclinika.mssharing.model.write.ngs.dto.NgsExperimentImportRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * @author timofei.kasianov 8/3/18
 */
@Service
public class NgsExperimentImporterImpl implements NgsExperimentImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(NgsExperimentImporterImpl.class);

    @Inject
    private NgsExperimentTemplateParser ngsExperimentTemplateParser;
    @Inject
    private NgsExperimentTemplateDataValidator ngsExperimentTemplateDataValidator;
    @Inject
    private NgsExperimentCreator ngsExperimentCreator;

    @Override
    public long makeImport(long actor, NgsExperimentImportRequest request) {

        final NgsExperimentTemplateData experimentTemplateData =
                ngsExperimentTemplateParser.parse(request.getExperimentTemplateFileInBytes());

        final NgsExperimentTemplateValidationResult validationResult =
                ngsExperimentTemplateDataValidator.validate(experimentTemplateData);

        if (!validationResult.successful()) {
            final String errorMessage = composeErrorMessageFromValidationErrors(validationResult.getErrors());
            throw new NgsExperimentImportException(errorMessage);
        }

        final NgsExperimentCreateRequest createRequest = new NgsExperimentCreateRequest(
                request.getExperimentName(),
                request.getProjectId(),
                request.getLabId(),
                request.getInstrumentId(),
                experimentTemplateData
        );
        final long experimentId = ngsExperimentCreator.create(actor, createRequest);

        LOGGER.info("NGS Experiment has been created. Experiment ID: {}", experimentId);

        return experimentId;
    }

    private String composeErrorMessageFromValidationErrors(List<ValidationError> validationErrors) {
        final StringBuilder errorMessage = new StringBuilder("Experiment template validation error occurred: \n");
        for (ValidationError validationError : validationErrors) {
            errorMessage.append("Sample: ");
            errorMessage.append(validationError.getSampleOrder());
            errorMessage.append(", Message: ");
            errorMessage.append(validationError.getErrorMessage());
            errorMessage.append(".\n");
        }
        return errorMessage.toString();
    }
}
