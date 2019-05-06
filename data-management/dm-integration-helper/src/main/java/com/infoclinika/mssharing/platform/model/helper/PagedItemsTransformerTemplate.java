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
import org.springframework.data.domain.Sort.Order;

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
    private static final String FAILED = "failed";

    private static final Map<Class<?>, Map<String, String>> SORTING = createSortingRulesMap();

    private static Map<Class<?>, Map<String, String>> createSortingRulesMap() {
        final HashMap<Class<?>, Map<String, String>> sortingRules = new HashMap<>();

        final HashMap<String, String> fileSortingRules = new HashMap<>();
        fileSortingRules.put(ID, ID);
        fileSortingRules.put(UPLOAD_DATE, UPLOAD_DATE);
        fileSortingRules.put(NAME, NAME);
        fileSortingRules.put(SIZE_IN_BYTES, SIZE_IN_BYTES);
        fileSortingRules.put(INSTRUMENT, "instrument.name");
        fileSortingRules.put(LABELS, LABELS);
        fileSortingRules.put(LABORATORY, "instrument.lab.name");


        final HashMap<String, String> experimentFileSortingRules = new HashMap<>();

        experimentFileSortingRules.put(ID, ID);
        experimentFileSortingRules.put(UPLOAD_DATE, "fileMetaData.uploadDate");
        experimentFileSortingRules.put(NAME, "fileMetaData.name");
        experimentFileSortingRules.put(SIZE_IN_BYTES, "fileMetaData.sizeInBytes");
        experimentFileSortingRules.put(INSTRUMENT, "fileMetaData.instrument.name");
        experimentFileSortingRules.put(LABELS, "fileMetaData.labels");
        experimentFileSortingRules.put(LABORATORY, "fileMetaData.instrument.lab.name");


        final HashMap<String, String> projectSortingRules = new HashMap<>();

        projectSortingRules.put(ID, ID);
        projectSortingRules.put(NAME, NAME);
        projectSortingRules.put(OWNER, "creator.personData.firstName");
        projectSortingRules.put(LABORATORY, LAB_NAME);
        projectSortingRules.put(AREA, "areaOfResearch");
        projectSortingRules.put(MODIFIED, LAST_MODIFICATION);


        final HashMap<String, String> experimentSortingRules = new HashMap<>();

        experimentSortingRules.put(ID, ID);
        experimentSortingRules.put(NAME, NAME);
        experimentSortingRules.put(OWNER, "creator.personData.firstName");
        experimentSortingRules.put(LABORATORY, LAB_NAME);
        experimentSortingRules.put(PROJECT, "project.name");
        experimentSortingRules.put(MODIFIED, LAST_MODIFICATION);
        experimentSortingRules.put(FAILED, FAILED);


        final HashMap<String, String> instrumentSortingRules = new HashMap<>();

        instrumentSortingRules.put(ID, ID);
        instrumentSortingRules.put(NAME, NAME);
        instrumentSortingRules.put(MODEL, "model.name");
        instrumentSortingRules.put(SERIAL_NUMBER, SERIAL_NUMBER);
        instrumentSortingRules.put(LABORATORY, LAB_NAME);


        final HashMap<String, String> instrumentModelSortingRules = new HashMap<>();

        instrumentModelSortingRules.put(ID, ID);
        instrumentModelSortingRules.put(NAME, NAME);
        instrumentModelSortingRules.put(STUDY_TYPE, "studyType.name");
        instrumentModelSortingRules.put(VENDOR, "vendor.name");
        instrumentModelSortingRules.put(INSTRUMENT_TYPE, "type.name");

        sortingRules.put(FileMetaDataTemplate.class, fileSortingRules);
        sortingRules.put(ProjectTemplate.class, projectSortingRules);
        sortingRules.put(ExperimentFileTemplate.class, experimentFileSortingRules);
        sortingRules.put(ExperimentTemplate.class, experimentSortingRules);
        sortingRules.put(InstrumentTemplate.class, instrumentSortingRules);
        sortingRules.put(InstrumentModel.class, instrumentModelSortingRules);

        return sortingRules;
    }

    public static PageRequest toPageRequest(Class<?> entity, PagedItemInfo pagedInfo) {
        return new PageRequest(
            pagedInfo.page,
            pagedInfo.items,
            new Sort(new Order(pagedInfo.isSortingAsc ? ASC : DESC, resolve(entity, pagedInfo.sortingField)))
        );
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
