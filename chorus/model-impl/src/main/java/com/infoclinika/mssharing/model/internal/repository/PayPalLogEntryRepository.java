package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.payment.PayPalLogEntry;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Elena Kurilina
 */
public interface PayPalLogEntryRepository extends CrudRepository<PayPalLogEntry, Long> {
    @Query("select u from PayPalLogEntry u where u.lab = :lab")
    List<PayPalLogEntry> findByLab(@Param("lab") Long lab);


    @Query("select u from PayPalLogEntry u where u.transactionId = :transactionId")
    @Nullable
    PayPalLogEntry findByTransaction(@Param("transactionId") String transactionId);

}
