package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.ApplicationSettings;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * @author Elena Kurilina
 */
public interface ApplicationSettingsRepository extends CrudRepository<ApplicationSettings, Long> {

    public static final String MAX_FILE_SIZE_SETTING = "maxAttachmentSizeInBytes";
    public static final String MAX_PROTEIN_DB_SIZE_SETTING = "maxProteinDBSizeInBytes";
    public static final String HOURS_TO_STORE_IN_TRASH = "hoursToStoreInTrash";

    @Query("SELECT a FROM ApplicationSettings a WHERE a.name='" + MAX_FILE_SIZE_SETTING + "'")
    public ApplicationSettings findMaxSize();

    @Query("SELECT a FROM ApplicationSettings a WHERE a.name='" + HOURS_TO_STORE_IN_TRASH + "'")
    public ApplicationSettings findHoursToStoreInTrash();

    @Query("SELECT a FROM ApplicationSettings a WHERE a.name=:name")
    public ApplicationSettings findByName(@Param("name") String name);

    @Query("SELECT a FROM ApplicationSettings a WHERE a.name='" + MAX_PROTEIN_DB_SIZE_SETTING + "'")
    ApplicationSettings findProteinDBMaxSize();
}
