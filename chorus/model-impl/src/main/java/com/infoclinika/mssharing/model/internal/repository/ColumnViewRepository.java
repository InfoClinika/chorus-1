package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.ColumnsView;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Herman Zamula
 */
public interface ColumnViewRepository extends CrudRepository<ColumnsView, Long> {

    @Query("select cw from ColumnsView cw where cw.type =:type and (cw.user.id =:user or cw.isDefault = true)")
    List<ColumnsView> findAllowed(@Param("user") long user, @Param("type") ColumnsView.Type type);

    @Query("select cw from ColumnsView cw where cw.type =:type and cw.isDefault = true")
    ColumnsView findDefault(@Param("type") ColumnsView.Type type);

    @Query("select cw from ColumnsView cw where cw.type =:type and cw.user.id =:user and cw.isPrimary = true")
    ColumnsView findPrimary(@Param("type") ColumnsView.Type type, @Param("user") long user);
}
