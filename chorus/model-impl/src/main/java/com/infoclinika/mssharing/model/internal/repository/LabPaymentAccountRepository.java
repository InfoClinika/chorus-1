package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.payment.AccountChargeableItemData;
import com.infoclinika.mssharing.model.internal.entity.payment.LabPaymentAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * @author Elena Kurilina
 */
public interface LabPaymentAccountRepository extends CrudRepository<LabPaymentAccount, Long> {

    @Query("SELECT l FROM LabPaymentAccount l WHERE l.lab.id= :lab")
    LabPaymentAccount findByLab(@Param("lab") long labId);

    @Query("SELECT l.id FROM LabPaymentAccount l WHERE l.lab.id= :lab")
    Long accountIdForLab(@Param("lab") long labId);

    @Query("SELECT fd " +
            "FROM LabPaymentAccount l join l.billingData.featuresData fd join fetch fd.chargeableItem WHERE l.lab.id= :lab")
    Set<AccountChargeableItemData> findFeaturesDataByLab(@Param("lab") long labId);

    @Query("SELECT l FROM LabPaymentAccount l join l.lab a WHERE a.id in " +
            "(select m.lab.id from UserLabMembership m where m.user.id = :head and m.head = true)")
    List<LabPaymentAccount> findByLabHeadId(@Param("head") long head);

    @Query("select m from LabPaymentAccount m ")
    Page<LabPaymentAccount> finaPagedAll(Pageable request);

    @Query("select a from LabPaymentAccount a join a.lab lab where a.type = " +
            "com.infoclinika.mssharing.model.internal.entity.payment.LabPaymentAccount$LabPaymentAccountType.ENTERPRISE")
    List<LabPaymentAccount> findEnterprise();
}
