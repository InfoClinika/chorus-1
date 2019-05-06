package com.infoclinika.mssharing.model.internal.entity.upload;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * @author Vitalii Petkanych
 */
@Converter
public class StringArrayJpaConverter implements AttributeConverter<String[], String> {

    private static final String SEPARATOR = "\n";

    @Override
    public String convertToDatabaseColumn(String[] strings) {
        if (strings != null && strings.length > 0) {
            return String.join(SEPARATOR, strings);
        }

        return null;
    }

    @Override
    public String[] convertToEntityAttribute(String value) {
        return StringUtils.isNotEmpty(value) ? value.split(SEPARATOR) : null;
    }
}
