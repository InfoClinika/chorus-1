package com.infoclinika.mssharing.model.internal.write.ngs.api;

import com.infoclinika.mssharing.model.internal.write.ngs.api.dto.NgsExperimentCreateRequest;

/**
 * @author timofei.kasianov 8/6/18
 */
public interface NgsExperimentCreator {

    long create(long actor, NgsExperimentCreateRequest request);

}
