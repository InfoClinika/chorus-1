package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.platform.entity.InstrumentModel;
import com.infoclinika.mssharing.platform.repository.InstrumentModelRepositoryTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

/**
 * @author timofei.kasianov 12/8/16
 */
public interface InstrumentModelRepository extends InstrumentModelRepositoryTemplate<InstrumentModel> {

    @Query("select im from InstrumentModel im " +
            " where cast(im.id as string) like :query or im.name like :query or im.studyType.name like :query or im.vendor.name like :query or im.type.name like :query")
    Page<InstrumentModel> findPage(@Param("query") String query, Pageable request);

    @Query("select new map(im.id as id, (select count(*) from Instrument i left join i.model m where m.id = im.id) as count) from InstrumentModel im where im.id in(:ids)")
    List<Map<String, Long>> findInstrumentCounts(@Param("ids") List<Long> modelIds);

}
