package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.ApplicationSettings;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * @author Elena Kurilina
 */
public interface ApplicationSettingsRepository extends CrudRepository<ApplicationSettings, Long> {

    String MAX_FILE_SIZE_SETTING = "maxAttachmentSizeInBytes";
    String MAX_PROTEIN_DB_SIZE_SETTING = "maxProteinDBSizeInBytes";
    String HOURS_TO_STORE_IN_TRASH = "hoursToStoreInTrash";
    String IS_DEMO_DATA_CREATED = "isDemoDataCreated";

    @Query("SELECT a FROM ApplicationSettings a WHERE a.name='" + MAX_FILE_SIZE_SETTING + "'")
    ApplicationSettings findMaxSize();

    @Query("SELECT a FROM ApplicationSettings a WHERE a.name='" + HOURS_TO_STORE_IN_TRASH + "'")
    ApplicationSettings findHoursToStoreInTrash();

    @Query("SELECT a FROM ApplicationSettings a WHERE a.name=:name")
    ApplicationSettings findByName(@Param("name") String name);

    @Query("SELECT a FROM ApplicationSettings a WHERE a.name='" + MAX_PROTEIN_DB_SIZE_SETTING + "'")
    ApplicationSettings findProteinDBMaxSize();

    @Query("SELECT a FROM ApplicationSettings a WHERE a.name='" + IS_DEMO_DATA_CREATED + "'")
    ApplicationSettings findIsDemoDataCreated();

}
