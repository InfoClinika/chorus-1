package com.infoclinika.mssharing.dto.response;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

/**
 * author: Ruslan Duboveckij
 */
public class InstrumentDTO implements Serializable {
    private long id;
    private String name;
    private VendorDTO vendor;
    private long lab;
    private String serial;
    private long creator;

    public InstrumentDTO() {
    }

    public InstrumentDTO(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public InstrumentDTO(long id,
                         String name,
                         VendorDTO vendor,
                         long lab,
                         String serial,
                         long creator) {
        this.id = id;
        this.name = name;
        this.vendor = vendor;
        this.lab = lab;
        this.serial = serial;
        this.creator = creator;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VendorDTO getVendor() {
        return vendor;
    }

    public void setVendor(VendorDTO vendor) {
        this.vendor = vendor;
    }

    public long getLab() {
        return lab;
    }

    public void setLab(long lab) {
        this.lab = lab;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public long getCreator() {
        return creator;
    }

    public void setCreator(long creator) {
        this.creator = creator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstrumentDTO that = (InstrumentDTO) o;

        if (id != that.id) return false;
        if (vendor != null ? !vendor.equals(that.vendor) : that.vendor != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (vendor != null ? vendor.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("vendor", vendor)
                .add("lab", lab)
                .add("serial", serial)
                .add("creator", creator)
                .toString();
    }
}
