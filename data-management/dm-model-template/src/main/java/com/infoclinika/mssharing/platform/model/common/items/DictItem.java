package com.infoclinika.mssharing.platform.model.common.items;

import java.util.Objects;

/**
 * @author timofei.kasianov 8/8/18
 */
public class DictItem<ID, NAME> {

    private final ID id;
    private final NAME name;

    public DictItem(ID id, NAME name) {
        this.id = id;
        this.name = name;
    }

    public ID getId() {
        return id;
    }

    public NAME getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DictItem)) {
            return false;
        }
        DictItem<?, ?> dictItem = (DictItem<?, ?>) o;
        return Objects.equals(id, dictItem.id) &&
            Objects.equals(name, dictItem.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
