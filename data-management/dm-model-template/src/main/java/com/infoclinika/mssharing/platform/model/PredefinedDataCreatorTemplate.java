package com.infoclinika.mssharing.platform.model;

import com.infoclinika.mssharing.platform.model.common.items.FileExtensionItem;

import java.util.Set;

/**
 * @author Herman Zamula
 */
public interface PredefinedDataCreatorTemplate {

    long admin(String firstName, String lastName, String email, String password);

    void allUsersGroup();

    void species(String... names);

    void experimentType(String name, boolean allowed2DLC, boolean allowLabels);

    long instrumentModel(String vendor, String type, String studyType, String name, boolean isFolderArchiveSupport,
                         boolean isMultipleFiles, Set<FileExtensionItem> extensions);

}
