package com.infoclinika.mssharing.platform.model.common.items;

import com.google.common.collect.ImmutableSet;

import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LabItem)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        LabItem labItem = (LabItem) o;
        return labHead == labItem.labHead &&
            Objects.equals(instruments, labItem.instruments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), instruments, labHead);
    }
}
