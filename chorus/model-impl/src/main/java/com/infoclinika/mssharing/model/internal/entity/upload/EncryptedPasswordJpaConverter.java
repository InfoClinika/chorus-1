package com.infoclinika.mssharing.model.internal.entity.upload;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Base64;

/**
 * @author Vitalii Petkanych
 */
@Converter
public class EncryptedPasswordJpaConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String password) {
        return password == null ? null : new String(Base64.getEncoder().encode(password.getBytes()));
    }

    @Override
    public String convertToEntityAttribute(String encryptedPassword) {
        return encryptedPassword == null ? null : new String(Base64.getDecoder().decode(encryptedPassword));
    }
}
