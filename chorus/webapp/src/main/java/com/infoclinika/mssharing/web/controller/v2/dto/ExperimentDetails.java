package com.infoclinika.mssharing.web.controller.v2.dto;

import com.infoclinika.mssharing.model.read.dto.details.ExperimentItem;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExperimentDetails {

    private ExperimentItem experimentItem;
    private DetailsReaderTemplate.ExperimentShortInfo experimentShortInfo;
}
