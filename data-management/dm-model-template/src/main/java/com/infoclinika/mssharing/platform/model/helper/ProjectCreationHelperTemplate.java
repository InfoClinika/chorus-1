package com.infoclinika.mssharing.platform.model.helper;

import com.infoclinika.mssharing.platform.model.common.items.NamedItem;

import java.util.Set;

/**
 * @author Herman Zamula
 */

public interface ProjectCreationHelperTemplate {

    Set<NamedItem> ownedProjects(long actor);

}
