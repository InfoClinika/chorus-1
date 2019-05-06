package com.infoclinika.mssharing.dto.request;

import java.util.List;

/**
 * Created by Stotskyi Oleksii on 5/22/17.
 */

public class DesktopUploadDoneDTO {
    private String id;
    private long instrumentId;
    private boolean done;
    private List<String> files;

    public DesktopUploadDoneDTO() {
    }

    public DesktopUploadDoneDTO(String id, long instrumentId, boolean done, List<String> files) {
        this.id = id;
        this.instrumentId = instrumentId;
        this.done = done;
        this.files = files;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(long instrumentId) {
        this.instrumentId = instrumentId;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }
}
