package com.infoclinika.mssharing.model.write.ngs;

import com.infoclinika.mssharing.model.write.ngs.dto.NgsExperimentImportRequest;

/**
 * @author timofei.kasianov 8/3/18
 */
public interface NgsExperimentImporter {

    /**
     * Imports an experiment using experiment template
     *
     * @param actor - actor ID
     * @param request - contains required data for an import
     * @return created experiment ID
     * @throws NgsExperimentImportException thrown exception
     *
     */
    long makeImport(long actor, NgsExperimentImportRequest request) throws NgsExperimentImportException;

}
