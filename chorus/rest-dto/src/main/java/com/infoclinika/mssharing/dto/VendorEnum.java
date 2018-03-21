package com.infoclinika.mssharing.dto;

import java.io.Serializable;

/**
 * author Ruslan Duboveckij
 */
public enum VendorEnum implements Serializable {
    THERMO("Thermo Scientific"),
    WATERS("Waters"),
    AGILENT("Agilent"),
    BRUKER("Bruker"),
    AB_SCIEX("Sciex"),
    MA_AFFYMETRIX("Affymetrix"),
    MA_AGILENT("Agilent"),
    MA_ILLIMUNA("Illumina"),
    WYATT("Wyatt"),
    NGS("Roche"),
    SOLID("SOLiD"),
    UNKNOWN("Unknown");

    public String name;

    VendorEnum(String name) {
        this.name = name;
    }

    public static VendorEnum getVendorEnum(String label) {
        for (VendorEnum vendorEnum : values()) {
            if (vendorEnum.name.equalsIgnoreCase(label)) {
                return vendorEnum;
            }
        }

        return UNKNOWN;
    }
}
