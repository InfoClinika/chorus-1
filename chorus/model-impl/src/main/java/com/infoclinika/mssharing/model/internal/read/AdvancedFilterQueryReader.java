package com.infoclinika.mssharing.model.internal.read;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Map;

/**
 * @author vladislav.kovchug
 */
public interface AdvancedFilterQueryReader {
    <T> Page<T> readQuery(PageRequest request, String requestQuery,
                          String countQuery, Map<String, Object> parameters);
}
