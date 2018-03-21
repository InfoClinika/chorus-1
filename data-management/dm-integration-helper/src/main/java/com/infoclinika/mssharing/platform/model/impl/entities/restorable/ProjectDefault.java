package com.infoclinika.mssharing.platform.model.impl.entities.restorable;

import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.model.impl.entities.GroupDefault;
import com.infoclinika.mssharing.platform.model.impl.entities.LabDefault;
import com.infoclinika.mssharing.platform.model.impl.entities.UserDefault;

import javax.persistence.Entity;

/**
 * @author Herman Zamula
 */
@Entity
public class ProjectDefault extends ProjectTemplate<UserDefault, LabDefault, GroupDefault, ProjectDefault> {
}
