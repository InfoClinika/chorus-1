package com.infoclinika.mssharing.integration.test.data.experiment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Orlov
 */
public class TranslationRange {

    private final String minRt;
    private final String maxRt;
    private final String minMz;
    private final String maxMz;
    private List<LockMz> lockMzList = new ArrayList<>();

    public TranslationRange(Builder builder) {
        this.minRt = builder.minRt;
        this.maxRt = builder.maxRt;
        this.minMz = builder.minMz;
        this.maxMz = builder.maxMz;
        this.lockMzList = builder.lockMzList;
    }

    public String getMinRt() {
        return minRt;
    }

    public String getMaxRt() {
        return maxRt;
    }

    public String getMinMz() {
        return minMz;
    }

    public String getMaxMz() {
        return maxMz;
    }

    public List<LockMz> getLockMzList() {
        return lockMzList;
    }

    public static class Builder {
        private String minRt;
        private String maxRt;
        private String minMz;
        private String maxMz;
        private List<LockMz> lockMzList = new ArrayList<>();

        public Builder minRt(String minRt) {
            this.minRt = minRt;
            return this;
        }

        public Builder maxRt(String maxRt) {
            this.maxRt = maxRt;
            return this;
        }

        public Builder minMz(String minMz) {
            this.minMz = minMz;
            return this;
        }

        public Builder maxMz(String maxMz) {
            this.maxMz = maxMz;
            return this;
        }

        public Builder lockMzList(List<LockMz> lockMzList) {
            this.lockMzList = lockMzList;
            return this;
        }

        public TranslationRange build() {
            return new TranslationRange(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TranslationRange that = (TranslationRange) o;

        if (lockMzList != null ? !lockMzList.equals(that.lockMzList) : that.lockMzList != null) return false;
        if (maxMz != null ? !maxMz.equals(that.maxMz) : that.maxMz != null) return false;
        if (maxRt != null ? !maxRt.equals(that.maxRt) : that.maxRt != null) return false;
        if (minMz != null ? !minMz.equals(that.minMz) : that.minMz != null) return false;
        if (minRt != null ? !minRt.equals(that.minRt) : that.minRt != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = minRt != null ? minRt.hashCode() : 0;
        result = 31 * result + (maxRt != null ? maxRt.hashCode() : 0);
        result = 31 * result + (minMz != null ? minMz.hashCode() : 0);
        result = 31 * result + (maxMz != null ? maxMz.hashCode() : 0);
        result = 31 * result + (lockMzList != null ? lockMzList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TranslationRange{" +
                "minRt='" + minRt + '\'' +
                ", maxRt='" + maxRt + '\'' +
                ", minMz='" + minMz + '\'' +
                ", maxMz='" + maxMz + '\'' +
                ", lockMzList=" + lockMzList +
                '}';
    }
}
