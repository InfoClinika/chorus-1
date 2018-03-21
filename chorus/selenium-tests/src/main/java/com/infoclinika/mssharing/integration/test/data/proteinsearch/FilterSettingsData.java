package com.infoclinika.mssharing.integration.test.data.proteinsearch;

/**
 * @author Alexander Orlov
 */
public class FilterSettingsData {

    private final FilterBy filterBy;
    private final Filter filter;
    private final MatchIf matchIf;
    private final String value;
    private final boolean addOneMoreFilter;
    private final AndOr andOr;

    private FilterSettingsData(Builder builder){
        this.filterBy = builder.filterBy;
        this.filter = builder.filter;
        this.matchIf = builder.matchIf;
        this.value = builder.value;
        this.addOneMoreFilter = builder.addOneMoreFilter;
        this.andOr = builder.andOr;
    }

    public FilterBy getFilterBy() {
        return filterBy;
    }

    public Filter getFilter() {
        return filter;
    }

    public MatchIf getMatchIf() {
        return matchIf;
    }

    public String getValue() {
        return value;
    }

    public boolean isAddOneMoreFilter() {
        return addOneMoreFilter;
    }

    public AndOr getAndOr() {
        return andOr;
    }

    public static class Builder {

        private FilterBy filterBy;
        private Filter filter;
        private MatchIf matchIf;
        private String value;
        private boolean addOneMoreFilter;
        private AndOr andOr;

        public Builder filterBy(FilterBy filterBy){
            this.filterBy = filterBy;
            return this;
        }

        public Builder filter(Filter filter) {
            this.filter = filter;
            return this;
        }

        public Builder matchIf(MatchIf matchIf) {
            this.matchIf = matchIf;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder addOneMoreFilter(boolean isAddOneMoreFilter) {
            this.addOneMoreFilter = addOneMoreFilter;
            return this;
        }

        public Builder andOr(AndOr andOr) {
            this.andOr = andOr;
            return this;
        }

        public FilterSettingsData build(){
            return new FilterSettingsData(this);
        }
    }


}
