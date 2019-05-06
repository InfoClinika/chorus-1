package com.infoclinika.mssharing.platform.model.common.items;

import java.util.Objects;

/**
 * @author Herman Zamula
 */
public class InstrumentItem {
    public final long id;
    public final String name;
    public final VendorItem vendor;
    public final long lab;
    public final String serial;
    public final long creator;

    public InstrumentItem(long id, String name, VendorItem vendorItem, long lab, String serial, long creator) {
        this.id = id;
        this.name = name;
        this.vendor = vendorItem;
        this.lab = lab;
        this.serial = serial;
        this.creator = creator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InstrumentItem)) {
            return false;
        }
        InstrumentItem that = (InstrumentItem) o;
        return id == that.id &&
            lab == that.lab &&
            creator == that.creator &&
            Objects.equals(name, that.name) &&
            Objects.equals(vendor, that.vendor) &&
            Objects.equals(serial, that.serial);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, vendor, lab, serial, creator);
    }
}
