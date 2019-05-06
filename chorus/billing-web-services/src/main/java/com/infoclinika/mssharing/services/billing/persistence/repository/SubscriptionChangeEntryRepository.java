package com.infoclinika.mssharing.services.billing.persistence.repository;

import com.infoclinika.mssharing.services.billing.persistence.enity.SubscriptionChangeEntry;
import org.springframework.data.repository.CrudRepository;

/**
 * @author timofei.kasianov 1/11/17
 */
public interface SubscriptionChangeEntryRepository extends CrudRepository<SubscriptionChangeEntry, Long> {
}
