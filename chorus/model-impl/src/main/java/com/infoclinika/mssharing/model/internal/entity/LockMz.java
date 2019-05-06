package com.infoclinika.mssharing.model.internal.entity;

import org.hibernate.validator.constraints.Range;

import javax.persistence.Embeddable;

/**
 * @author Herman Zamula
 */
@Embeddable
public class LockMz {

    private double mass;

    /**
     * Charge is value [-3, -1] or [+1, +3]
     */
    @Range.List({@Range(min = -3, max = -1), @Range(min = 1, max = 3)})
    private int charge;

    public double getMass() {
        return mass;
    }

    public void setMass(double mass) {
        this.mass = mass;
    }

    public int getCharge() {
        return charge;
    }

    public void setCharge(int charge) {
        this.charge = charge;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LockMz lockMz = (LockMz) o;

        if (charge != lockMz.charge) {
            return false;
        }
        if (Double.compare(lockMz.mass, mass) != 0) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(mass);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + charge;
        return result;
    }
}