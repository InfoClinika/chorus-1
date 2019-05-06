package com.infoclinika.mssharing.web.controller.request;

/**
 * @author Nikita Matrosov
 */
public class ProteinDatabaseRequest {
    public long databaseId;
    public long typeId;
    public String name;
    public long accessionParseRule;
    public long descriptionParseRule;

    public ProteinDatabaseRequest() {
    }

    public ProteinDatabaseRequest(
        long databaseId,
        long typeId,
        String name,
        long accessionParseRule,
        long descriptionParseRule
    ) {
        this.databaseId = databaseId;
        this.typeId = typeId;
        this.name = name;
        this.accessionParseRule = accessionParseRule;
        this.descriptionParseRule = descriptionParseRule;
    }
}
