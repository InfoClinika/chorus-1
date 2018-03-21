package com.infoclinika.mssharing.integration.test.data.experiment;

/**
 * @author Alexander Orlov
 */
public class LockMz {

    private final String lockMass;
    private final String charge;

    private LockMz(Builder builder) {
        this.lockMass = builder.lockMass;
        this.charge = builder.charge;
    }

    public String getLockMass() {
        return lockMass;
    }

    public String getCharge() {
        return charge;
    }

    public String getLockMassWithCharge() {
        return lockMass + "(" + charge + ")";
    }

    public static class Builder {
        private String lockMass;
        private String charge;

        public Builder lockMass(String lockMass) {
            this.lockMass = lockMass;
            return this;
        }

        public Builder charge(String charge) {
            this.charge = charge;
            return this;
        }

        public LockMz build() {
            return new LockMz(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LockMz lockMz = (LockMz) o;

        if (charge != null ? !charge.equals(lockMz.charge) : lockMz.charge != null) return false;
        if (lockMass != null ? !lockMass.equals(lockMz.lockMass) : lockMz.lockMass != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = lockMass != null ? lockMass.hashCode() : 0;
        result = 31 * result + (charge != null ? charge.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LockMz{" +
                "lockMass='" + lockMass + '\'' +
                ", charge='" + charge + '\'' +
                '}';
    }
}
