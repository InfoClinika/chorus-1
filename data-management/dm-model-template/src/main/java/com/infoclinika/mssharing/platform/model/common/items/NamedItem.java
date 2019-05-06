package com.infoclinika.mssharing.platform.model.common.items;

import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NamedItem)) {
            return false;
        }
        NamedItem namedItem = (NamedItem) o;
        return id == namedItem.id &&
            Objects.equals(name, namedItem.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
