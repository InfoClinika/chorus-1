package com.infoclinika.mssharing.platform.model.helper;

import com.infoclinika.mssharing.platform.entity.ExperimentFileTemplate;
import com.infoclinika.mssharing.platform.entity.InstrumentModel;
import com.infoclinika.mssharing.platform.entity.InstrumentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * @author Herman Zamula
 */
public class PagedItemsTransformerTemplate {
    private static final String ID = "id";
    private static final String UPLOAD_DATE = "uploadDate";
    private static final String NAME = "name";
    private static final String SIZE_IN_BYTES = "sizeInBytes";
    private static final String INSTRUMENT = "instrument";
    private static final String LABELS = "labels";
    private static final String LABORATORY = "laboratory";
    private static final String OWNER = "owner";
    private static final String AREA = "area";
    private static final String MODIFIED = "modified";
    private static final String MODEL = "model";
    private static final String SERIAL_NUMBER = "serialNumber";
    private static final String STUDY_TYPE = "studyType";
    private static final String VENDOR = "vendor";
    private static final String INSTRUMENT_TYPE = "instrumentType";
    private static final String LAB_NAME = "lab.name";
    private static final String LAST_MODIFICATION = "lastModification";
    private static final String PROJECT = "project";
    private static final String PERCENT = "%";

    private static final Map<Class<?>, Map<String, String>> SORTING = new HashMap<Class<?>, Map<String, String>>() {{
        put(FileMetaDataTemplate.class, new HashMap<String, String>() {{
            put(ID, ID);
            put(UPLOAD_DATE, UPLOAD_DATE);
            put(NAME, NAME);
            put(SIZE_IN_BYTES, SIZE_IN_BYTES);
            put(INSTRUMENT, "instrument.name");
            put(LABELS, LABELS);
            put(LABORATORY, "instrument.lab.name");
        }});
        put(ExperimentFileTemplate.class, new HashMap<String, String>() {{
            put(ID, ID);
            put(UPLOAD_DATE, "fileMetaData.uploadDate");
            put(NAME, "fileMetaData.name");
            put(SIZE_IN_BYTES, "fileMetaData.sizeInBytes");
            put(INSTRUMENT, "fileMetaData.instrument.name");
            put(LABELS, "fileMetaData.labels");
            put(LABORATORY, "fileMetaData.instrument.lab.name");
        }});
        put(ProjectTemplate.class, new HashMap<String, String>() {{
            put(ID, ID);
            put(NAME, NAME);
            put(OWNER, "creator.personData.firstName");
            put(LABORATORY, LAB_NAME);
            put(AREA, "areaOfResearch");
            put(MODIFIED, LAST_MODIFICATION);
        }});
        put(ExperimentTemplate.class, new HashMap<String, String>() {{
            put(ID, ID);
            put(NAME, NAME);
            put(OWNER, "creator.personData.firstName");
            put(LABORATORY, LAB_NAME);
            put(PROJECT, "project.name");
            put(MODIFIED, LAST_MODIFICATION);
        }});
        put(InstrumentTemplate.class, new HashMap<String, String>() {{
            put(ID, ID);
            put(NAME, NAME);
            put(MODEL, "model.name");
            put(SERIAL_NUMBER, SERIAL_NUMBER);
            put(LABORATORY, LAB_NAME);
        }});
        put(InstrumentModel.class, new HashMap<String, String>() {{
            put(ID, ID);
            put(NAME, NAME);
            put(STUDY_TYPE, "studyType.name");
            put(VENDOR, "vendor.name");
            put(INSTRUMENT_TYPE, "type.name");
        }});
    }};

    public static PageRequest toPageRequest(Class<?> entity, PagedItemInfo pagedInfo) {
        return new PageRequest(pagedInfo.page, pagedInfo.items, new Sort(
                new Sort.Order(pagedInfo.isSortingAsc ? ASC : DESC, resolve(entity, pagedInfo.sortingField))
        ));
    }

    public static String toFilterQuery(PagedItemInfo pagedInfo) {
        if (StringUtils.isEmpty(pagedInfo.filterQuery)) {
            return PERCENT;
        }

        return PERCENT + pagedInfo.filterQuery + PERCENT;
    }

    public static String resolve(Class<?> entity, String filedName) {
        final Map<String, String> map = SORTING.get(entity);
        if (map == null) {
            throw new IllegalArgumentException("Unknown entity type to sort: " + entity);
        }
        String field = map.get(filedName);
        if (field == null) {
            throw new IllegalArgumentException("Unknown field to sort: " + filedName);
        }
        return field;
    }

    protected void sortingOverride(Map<Class<?>, Map<String, String>> sorting) {
        SORTING.putAll(sorting);
    }
}
