package com.infoclinika.mssharing.platform.model.common.items;

import com.google.common.base.Predicate;

import javax.annotation.Nullable;

/**
 * @author Pavel Kaplin
 */
public class DictionaryItem {
    /**
     * Name for special "Unspecified" dictionary items
     */
    public static final String UNSPECIFIED_NAME = "Unspecified";
    public static final Predicate<DictionaryItem> UNSPECIFIED = input -> input != null && input.isUnspecified();
    public final long id;
    public final String name;


    public DictionaryItem(long pk, String name) {
        this.id = pk;
        this.name = name;
    }

    public boolean isUnspecified() {
        return UNSPECIFIED_NAME.equals(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DictionaryItem that = (DictionaryItem) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
