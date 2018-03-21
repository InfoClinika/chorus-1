package com.infoclinika.mssharing.integration.test.data.instrument;

import com.google.common.collect.ImmutableList;
import com.infoclinika.mssharing.integration.test.data.experiment.LockMz;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Orlov
 */
public class OptionalFields {

    private final boolean isAutoTranslate;
    private final boolean isUseDefaultLockMass1;
    private final boolean isUseDefaultLockMass2;
    private final List<LockMz> lockMasses;
    private final String hLPC;
    private final String peripherals;

    private OptionalFields(Builder builder) {
        this.isAutoTranslate = builder.isAutoTranslate;
        this.isUseDefaultLockMass1 = builder.isUseDefaultLockMass1;
        this.isUseDefaultLockMass2 = builder.isUseDefaultLockMass2;
        this.lockMasses = ImmutableList.copyOf(builder.lockMasses);
        this.hLPC = builder.hLPC;
        this.peripherals = builder.peripherals;
    }

    public boolean isAutoTranslate() {
        return isAutoTranslate;
    }

    public boolean isUseDefaultLockMass1() {
        return isUseDefaultLockMass1;
    }

    public boolean isUseDefaultLockMass2() {
        return isUseDefaultLockMass2;
    }

    public List<LockMz> getLockMasses() {
        return lockMasses;
    }

    public String gethLPC() {
        return hLPC;
    }

    public String getPeripherals() {
        return peripherals;
    }

    public static class Builder {
        private boolean isAutoTranslate = true;
        private boolean isUseDefaultLockMass1;
        private boolean isUseDefaultLockMass2;
        private List<LockMz> lockMasses = new ArrayList<>();
        private String hLPC;
        private String peripherals;


        public Builder isAutoTranslate(boolean isAutoTranslate) {
            this.isAutoTranslate = isAutoTranslate;
            return this;
        }

        public Builder isUseDefaultLockMass1(boolean isUseDefaultLockMass1) {
            this.isUseDefaultLockMass1 = isUseDefaultLockMass1;
            return this;
        }

        public Builder isUseDefaultLockMass2(boolean isUseDefaultLockMass2) {
            this.isUseDefaultLockMass2 = isUseDefaultLockMass2;
            return this;
        }

        public Builder lockMasses(List<LockMz> lockMasses) {
            this.lockMasses = lockMasses;
            return this;
        }

        public Builder hLPC(String hLPC) {
            this.hLPC = hLPC;
            return this;
        }

        public Builder peripherals(String peripherals) {
            this.peripherals = peripherals;
            return this;
        }

        public OptionalFields build() {
            return new OptionalFields(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OptionalFields that = (OptionalFields) o;

        if (isAutoTranslate != that.isAutoTranslate) return false;
        if (isUseDefaultLockMass1 != that.isUseDefaultLockMass1) return false;
        if (isUseDefaultLockMass2 != that.isUseDefaultLockMass2) return false;
        if (hLPC != null ? !hLPC.equals(that.hLPC) : that.hLPC != null) return false;
        if (lockMasses != null ? !lockMasses.containsAll(that.lockMasses) : that.lockMasses != null)
            return false;
        if (peripherals != null ? !peripherals.equals(that.peripherals) : that.peripherals != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (isAutoTranslate ? 1 : 0);
        result = 31 * result + (isUseDefaultLockMass1 ? 1 : 0);
        result = 31 * result + (isUseDefaultLockMass2 ? 1 : 0);
        result = 31 * result + (lockMasses != null ? lockMasses.hashCode() : 0);
        result = 31 * result + (hLPC != null ? hLPC.hashCode() : 0);
        result = 31 * result + (peripherals != null ? peripherals.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "OptionalFields{" +
                "isAutoTranslate=" + isAutoTranslate +
                ", isUseDefaultLockMass1=" + isUseDefaultLockMass1 +
                ", isUseDefaultLockMass2=" + isUseDefaultLockMass2 +
                ", lockMasses=" + lockMasses +
                ", hLPC='" + hLPC + '\'' +
                ", peripherals='" + peripherals + '\'' +
                '}';
    }
}
