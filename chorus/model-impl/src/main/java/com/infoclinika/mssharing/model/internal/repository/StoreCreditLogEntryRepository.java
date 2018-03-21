package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.payment.StoreLogEntry;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;

/**
 * @author Elena Kurilina
 */

public interface StoreCreditLogEntryRepository extends CrudRepository<StoreLogEntry, Long>{

    @Query("SELECT s FROM StoreLogEntry s WHERE s.lab = :lab AND s.direction = 0")
    public List<StoreLogEntry> findInByLab(@Param("lab") long lab);


    @Query("SELECT s FROM StoreLogEntry s WHERE s.lab = :lab AND s.direction = 1")
    public List<StoreLogEntry> findOutByLab(@Param("lab") long lab);

    @Query("select u from StoreLogEntry u where u.transactionId = :transactionId")
    @Nullable
    StoreLogEntry findByTransaction(@Param("transactionId") String transactionId);

    @Query("select u.storeBalance from StoreLogEntry u where u.lab=:lab and u.timestamp between :dateFrom and :dateTo group by 1 order by u.timestamp desc")
    Long storedBalanceInDateRange(@Param("lab") long lab, @Param("dateFrom") Date from, @Param("dateTo") Date to);

    @Query("select u from StoreLogEntry u where u.lab=:lab and u.timestamp between :dateFrom and :dateTo order by u.timestamp desc")
    List<StoreLogEntry> findInByLab(@Param("lab") long lab, @Param("dateFrom") Date from, @Param("dateTo") Date to);
}
