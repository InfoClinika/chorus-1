package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.Adduct;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdductsRepository extends JpaRepository<Adduct, Long> {

    @Query("select a from Adduct a where a.adductType = 'PEPTIDE'")
    List<Adduct> findAllPeptides(Sort sort);

    @Query("select a from Adduct a where a.adductType = 'COMPOUND'")
    List<Adduct> findAllCompounds(Sort sort);

}
