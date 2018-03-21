package com.infoclinika.mssharing.platform.model.impl.entities.restorable;

import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.model.impl.entities.*;

import javax.persistence.Entity;

/**
 * @author Herman Zamula
 */
@Entity
public class ExperimentDefault extends ExperimentTemplate<UserDefault, LabDefault, ProjectDefault, InstrumentDefault, FactorDefault, ExperimentFileDefault> {
}
