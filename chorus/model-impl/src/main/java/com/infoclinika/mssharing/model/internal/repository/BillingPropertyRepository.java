package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.payment.BillingProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author : Alexander Serebriyan
 */
public interface BillingPropertyRepository extends JpaRepository<BillingProperty, Long> {

    @Query("select bp from BillingProperty bp where bp.name=:name")
    BillingProperty findByName(@Param("name") BillingProperty.BillingPropertyName name);
}
