package com.infoclinika.mssharing.integration.test.data.experiment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Orlov
 */
public class FileToPrepInfo {

    private List<String> prepsList = new ArrayList<>();

    public FileToPrepInfo(Builder builder){
        this.prepsList = builder.prepsList;

    }

    public List<String> getPrepsList() {
        return prepsList;
    }

    public static class Builder {

        private List<String> prepsList = new ArrayList<>();

        public Builder prepsList(List<String> prepsList){
            this.prepsList = prepsList;
            return this;
        }

        public FileToPrepInfo build(){
            return new FileToPrepInfo(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileToPrepInfo that = (FileToPrepInfo) o;

        return prepsList != null ? prepsList.equals(that.prepsList) : that.prepsList == null;

    }

    @Override
    public int hashCode() {
        return prepsList != null ? prepsList.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "FileToPrepInfo{" +
                "prepsList=" + prepsList +
                '}';
    }
}
