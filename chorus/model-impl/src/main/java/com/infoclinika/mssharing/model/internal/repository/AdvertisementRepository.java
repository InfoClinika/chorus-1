package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.ad.Advertisement;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

/**
 * @author andrii.loboda
 */
public interface AdvertisementRepository extends CrudRepository<Advertisement, Long> {
    @Query("select ad from Advertisement ad where ad.startRollDate <= :dateForAdd and ad.endRollDate >=:dateForAdd and ad.isEnabled=1")
    List<Advertisement> findAdsForDate(@Param("dateForAdd") Date date);
}
