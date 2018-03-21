package com.infoclinika.mssharing.platform.model.common.items;

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
    @SuppressWarnings("all")
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstrumentItem that = (InstrumentItem) o;

        if (creator != that.creator) return false;
        if (id != that.id) return false;
        if (lab != that.lab) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (serial != null ? !serial.equals(that.serial) : that.serial != null) return false;
        if (vendor != null ? !vendor.equals(that.vendor) : that.vendor != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (vendor != null ? vendor.hashCode() : 0);
        result = 31 * result + (int) (lab ^ (lab >>> 32));
        result = 31 * result + (serial != null ? serial.hashCode() : 0);
        result = 31 * result + (int) (creator ^ (creator >>> 32));
        return result;
    }
}
