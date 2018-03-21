package com.infoclinika.mssharing.integration.test.data.proteinsearch;

/**
 * @author Alexander Orlov
 */
public class SortingSettingsData {

    private final FilterBy dimension;
    private final Filter sortBy;
    private final Direction direction;

    private SortingSettingsData(Builder builder) {
        this.dimension = builder.dimension;
        this.sortBy = builder.sortBy;
        this.direction = builder.direction;
    }

    public FilterBy getDimension() {
        return dimension;
    }

    public Filter getSortBy() {
        return sortBy;
    }

    public Direction getDirection() {
        return direction;
    }

    public static class Builder {

        private FilterBy dimension;
        private Filter sortBy;
        private Direction direction;

        public Builder dimension(FilterBy dimension) {
            this.dimension = dimension;
            return this;
        }

        public Builder sortBy(Filter sortBy) {
            this.sortBy = sortBy;
            return this;
        }

        public Builder direction(Direction direction) {
            this.direction = direction;
            return this;
        }

        public SortingSettingsData build() {
            return new SortingSettingsData(this);
        }
    }
}
