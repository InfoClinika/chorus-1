package com.infoclinika.mssharing.platform.model.impl.entities;

import com.infoclinika.mssharing.platform.entity.ExperimentFileTemplate;
import com.infoclinika.mssharing.platform.model.impl.entities.restorable.ExperimentDefault;
import com.infoclinika.mssharing.platform.model.impl.entities.restorable.FileMetaDataDefault;

import javax.persistence.Entity;

/**
 * @author Herman Zamula
 */
@Entity
public class ExperimentFileDefault
    extends ExperimentFileTemplate<FileMetaDataDefault, ExperimentDefault, AnnotationDefault> {
}
