package com.infoclinika.mssharing.platform.repository;

import com.infoclinika.mssharing.platform.entity.Dictionary;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @author Pavel Kaplin
 */
@NoRepositoryBean
public interface DictionaryRepository<T extends Dictionary> extends CrudRepository<T, Long> {

    @Query("select d from #{#entityName} d where d.name=?1")
    T findByName(String name);
}
