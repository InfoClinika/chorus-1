package com.infoclinika.mssharing.web.controller.request;

/**
 * @author Nikita Matrosov
 */
public class ProteinDatabaseRequest {

    public long databaseId;
    public long typeId;
    public String name;

    public ProteinDatabaseRequest() {
    }

    public ProteinDatabaseRequest(long databaseId, long typeId, String name) {
        this.databaseId = databaseId;
        this.typeId = typeId;
        this.name = name;
    }
}
