package com.infoclinika.mssharing.platform.web.json;

import com.infoclinika.mssharing.platform.model.read.Filter;
import org.springframework.core.convert.converter.Converter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Pavel Kaplin
 */
public class FilterConverter implements Converter<String, Filter> {
    private static final Map<String, Filter> MAPPING;

    static {
        Map<String, Filter> mapping = new HashMap<String, Filter>();
        mapping.put("all", Filter.ALL);
        mapping.put("my", Filter.MY);
        mapping.put("public", Filter.PUBLIC);
        mapping.put("shared", Filter.SHARED_WITH_ME);
        MAPPING = Collections.unmodifiableMap(mapping);
    }

    @Override
    public Filter convert(String string) {
        return MAPPING.get(string);
    }
}
