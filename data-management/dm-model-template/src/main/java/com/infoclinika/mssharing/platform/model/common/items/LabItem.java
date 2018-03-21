package com.infoclinika.mssharing.platform.model.common.items;

import com.google.common.collect.ImmutableSet;

/**
 * @author Herman Zamula
 */
public class LabItem extends NamedItem {
    public final ImmutableSet<InstrumentItem> instruments;
    public final long labHead;

    public LabItem(long id, String name, long labHead, ImmutableSet<InstrumentItem> instruments) {
        super(id, name);
        this.instruments = instruments;
        this.labHead = labHead;
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LabItem labItem = (LabItem) o;

        if (id != labItem.id) return false;
        if (labHead != labItem.labHead) return false;
        if (instruments != null ? !instruments.equals(labItem.instruments) : labItem.instruments != null)
            return false;
        if (name != null ? !name.equals(labItem.name) : labItem.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (instruments != null ? instruments.hashCode() : 0);
        result = 31 * result + (int) (labHead ^ (labHead >>> 32));
        return result;
    }
}
