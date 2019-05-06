package com.infoclinika.mssharing.model.internal.read;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Map;

import static com.infoclinika.mssharing.model.internal.read.AdvancedFilterCreationHelper.getPageOfItemsByQuery;

/**
 * @author vladislav.kovchug
 */

@Service
public class AdvancedFilterQueryReaderImpl implements AdvancedFilterQueryReader {

    @PersistenceContext(unitName = "mssharing")
    private EntityManager em;

    @Override
    public <T> Page<T> readQuery(
        PageRequest request,
        String requestQuery,
        String countQuery,
        Map<String, Object> parameters
    ) {
        final Query query = em.createQuery(requestQuery);
        final Query count = em.createQuery(countQuery);

        for (String name : parameters.keySet()) {
            query.setParameter(name, parameters.get(name));
            count.setParameter(name, parameters.get(name));
        }
        return getPageOfItemsByQuery(request, query, count);
    }
}
