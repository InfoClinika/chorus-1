package com.infoclinika.mssharing.model.internal.stats;

import java.io.Serializable;
import java.util.*;

public class LabDataStatistics implements Serializable {
    private String labName;
    private Set<Long> fileIds = Collections.synchronizedSet(new HashSet<>());
    private List<Long> totalRawFileSizes = Collections.synchronizedList(new LinkedList<>());
    private List<Long> otherFilesSizes = Collections.synchronizedList(new LinkedList<>());

    public LabDataStatistics(String labName) {
        this.labName = labName;
    }

    public String getLabName() {
        return labName;
    }

    public void setLabName(String labName) {
        this.labName = labName;
    }

    public Set<Long> getFileIds() {
        return fileIds;
    }

    public void setFileIds(Set<Long> fileIds) {
        this.fileIds = fileIds;
    }

    public List<Long> getTotalRawFileSizes() {
        return totalRawFileSizes;
    }

    public void setTotalRawFileSizes(List<Long> totalRawFileSizes) {
        this.totalRawFileSizes = totalRawFileSizes;
    }

    public List<Long> getOtherFilesSizes() {
        return otherFilesSizes;
    }

    public void setOtherFilesSizes(List<Long> otherFilesSizes) {
        this.otherFilesSizes = otherFilesSizes;
    }
}
