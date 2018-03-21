package com.infoclinika.mssharing.platform.model.common.items;

/**
 * @author Herman Zamula
 */
public class NamedItem {

    public final long id;
    public final String name;

    public NamedItem(long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NamedItem namedItem = (NamedItem) o;

        if (id != namedItem.id) return false;
        if (name != null ? !name.equals(namedItem.name) : namedItem.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
