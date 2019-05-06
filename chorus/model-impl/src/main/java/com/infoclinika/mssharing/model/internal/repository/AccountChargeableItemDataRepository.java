package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.payment.AccountChargeableItemData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

/**
 * @author : Alexander Serebriyan
 */
public interface AccountChargeableItemDataRepository extends CrudRepository<AccountChargeableItemData, Long> {
}
