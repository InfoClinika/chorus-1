package com.infoclinika.mssharing.model.helper.items;

/**
 * @author Herman Zamula
 */
public class LockMzData {

    public double value;
    public int charge;

    public LockMzData(double value, int charge) {
        this.value = value;
        this.charge = charge;
    }

    @Override
    public String toString() {
        return value + " (" + charge + ")";
    }
}
