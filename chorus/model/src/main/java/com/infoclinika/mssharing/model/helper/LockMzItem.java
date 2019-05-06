package com.infoclinika.mssharing.model.helper;

/**
 * @author Herman Zamula
 */
public class LockMzItem {

    public double lockMass;
    public int charge;

    public LockMzItem() {
    }

    public LockMzItem(double lockMass, int charge) {
        this.lockMass = lockMass;
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

        LockMzItem that = (LockMzItem) o;

        if (charge != that.charge) {
            return false;
        }
        if (Double.compare(that.lockMass, lockMass) != 0) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(lockMass);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + charge;
        return result;
    }

    @Override
    public String toString() {
        return "LockMzItem{" +
            "lockMass=" + lockMass +
            ", charge=" + charge +
            '}';
    }
}


