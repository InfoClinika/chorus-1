package com.infoclinika.mssharing.platform.repository;

import com.infoclinika.mssharing.platform.entity.VendorExtension;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author timofei.kasianov 12/6/16
 */
public interface VendorExtensionRepositoryTemplate<T extends VendorExtension> extends CrudRepository<T, Long> {

    @Query("select ve from #{#entityName} ve " +
        "where ve.extension = :extension and (ve.zipExtension is null or ve.zipExtension = '')")
    List<T> findByExtensionWithEmptyZipExtension(@Param("extension") String extension);

}
