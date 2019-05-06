package com.infoclinika.mssharing.platform.repository;

import com.infoclinika.mssharing.platform.entity.FactorTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author : Alexander Serebriyan
 */
public interface FactorRepositoryTemplate<T extends FactorTemplate> extends JpaRepository<T, Long> {
}
