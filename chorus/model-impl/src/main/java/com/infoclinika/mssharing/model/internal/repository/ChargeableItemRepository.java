package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem;
import com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem.Feature;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * @author Elena Kurilina
 */
public interface ChargeableItemRepository extends CrudRepository<ChargeableItem, Long> {

    @Query("SELECT l FROM ChargeableItem l WHERE l.feature = :feature")
    ChargeableItem findByFeature(@Param("feature") Feature feature);

    @Query("select i from ChargeableItem i where i.feature in (" +
            "com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem$Feature.ANALYSE_STORAGE," +
            "com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem$Feature.DOWNLOAD," +
            "com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem$Feature.ANALYSIS," +
            "com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem$Feature.PUBLIC_DOWNLOAD)")
    Set<ChargeableItem> findEnabledByDefault();
}
