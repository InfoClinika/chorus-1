package com.infoclinika.mssharing.platform.model.common.items;

import java.util.List;

/**
 * @author andrey.lavrov
 */
public class InstrumentModelItem {
    public final long id;
    public final String name;
    public final String technologyType;
    public final String vendor;
    public final String instrumentType;
    public final List<String> extensions;

    private InstrumentModelItem(Builder builder) {
        this.id = builder.getId();
        this.name = builder.getName();
        this.technologyType = builder.getTechnologyType();
        this.vendor = builder.getVendor();
        this.instrumentType = builder.getInstrumentType();
        this.extensions = builder.getExtensions();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private long id;
        private String name;
        private String technologyType;
        private String vendor;
        private String instrumentType;
        private List<String> extensions;

        public long getId() {
            return id;
        }

        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        public String getName() {
            return name;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public String getTechnologyType() {
            return technologyType;
        }

        public Builder setTechnologyType(String technologyType) {
            this.technologyType = technologyType;
            return this;
        }

        public String getVendor() {
            return vendor;
        }

        public Builder setVendor(String vendor) {
            this.vendor = vendor;
            return this;
        }

        public String getInstrumentType() {
            return instrumentType;
        }

        public Builder setInstrumentType(String instrumentType) {
            this.instrumentType = instrumentType;
            return this;
        }

        public List<String> getExtensions() {
            return extensions;
        }

        public Builder setExtensions(List<String> extensions) {
            this.extensions = extensions;
            return this;
        }

        public InstrumentModelItem build() {
            return new InstrumentModelItem(this);
        }
    }
}
