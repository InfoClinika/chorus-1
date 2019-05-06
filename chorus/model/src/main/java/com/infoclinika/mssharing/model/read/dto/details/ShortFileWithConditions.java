package com.infoclinika.mssharing.model.read.dto.details;

/**
 * @author Oleksii Tymchenko
 */
public class ShortFileWithConditions {
    public final Long id;
    public final String filename;
    public final String joinedCoditions;

    public ShortFileWithConditions(Long id, String filename, String joinedCoditions) {
        this.id = id;
        this.filename = filename;
        this.joinedCoditions = joinedCoditions;
    }
}
