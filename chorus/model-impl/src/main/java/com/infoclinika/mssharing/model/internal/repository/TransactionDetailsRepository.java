package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.payment.TransactionDetails;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * @author Elena Kurilina
 */
public interface TransactionDetailsRepository extends CrudRepository<TransactionDetails, Long>{

    @Query("SELECT t FROM TransactionDetails t JOIN t.parameters p WHERE KEY(p) IN (':trans') ")
    public TransactionDetails findByTransaction(@Param("trans") String transaction);
}
