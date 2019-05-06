package com.infoclinika.mssharing.model.internal.entity.upload;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author Vitalii Petkanych
 */
@Converter(autoApply = true)
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, Timestamp> {

    @Override
    public Timestamp convertToDatabaseColumn(LocalDateTime date) {
        return date != null
            ? Timestamp.from(date.toInstant(ZoneOffset.ofTotalSeconds(0)))
            : null;
    }

    @Override
    public LocalDateTime convertToEntityAttribute(Timestamp value) {
        return value != null
            ? value.toLocalDateTime()
            : null;
    }
}
