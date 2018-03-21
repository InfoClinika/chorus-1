package com.infoclinika.mssharing.platform.repository;

import com.infoclinika.mssharing.platform.entity.Species;
import org.springframework.data.jpa.repository.Query;

/**
 * @author Pavel Kaplin
 */
public interface SpeciesRepositoryTemplate<S extends Species> extends DictionaryRepository<S> {

    @Query("select s from #{#entityName} s where s.name = 'Unspecified'")
    S getUnspecified();

}
