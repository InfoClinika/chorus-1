package com.infoclinika.mssharing.platform.repository;

import com.infoclinika.mssharing.platform.entity.InstrumentModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Pavel Kaplin
 */
public interface InstrumentModelRepositoryTemplate<T extends InstrumentModel> extends JpaRepository<T, Long> {

    @Query("select m from #{#entityName} m where m.vendor.id = :vendor")
    List<T> findByVendor(@Param("vendor") long vendor);

    @Query("select m from #{#entityName} m where m.folderArchiveSupport = true")
    List<T> findWithFolderArchiveUploadSupport();

    @Query("select m from #{#entityName} m where m.studyType.id = :studyType")
    List<T> findByStudyType(@Param("studyType") long studyType);

    @Query("select m from #{#entityName} m where m.studyType.id = :studyType and m.vendor.id = :vendor")
    List<T> findByStudyTypeAndVendor(@Param("studyType") long studyType, @Param("vendor") long vendor);

    @Query("select m from #{#entityName} m")
    Page<T> findAllAvailable(Pageable request);

    @Query("select m from #{#entityName} m where m.name = :name and m.vendor.name = :vendorName")
    T findByNameAndVendorName(@Param("name") String name, @Param("vendorName") String vendorName);
}
