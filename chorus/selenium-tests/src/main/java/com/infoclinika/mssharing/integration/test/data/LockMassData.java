package com.infoclinika.mssharing.integration.test.data;

/**
 * @author Sergii Moroz
 */
public class LockMassData {
    private double lockMassValue;
    private String lockMassCharge;

    public LockMassData(){
        this.lockMassValue = 1.1;
        this.lockMassCharge = "+2";
    }

    public LockMassData(String lockMassValue, String lockMassCharge){
        this.lockMassValue = Double.parseDouble(lockMassValue);
        this.lockMassCharge = lockMassCharge;
    }

    public LockMassData(double lockMassValue, String lockMassCharge){
        this.lockMassValue = lockMassValue;
        this.lockMassCharge = lockMassCharge;
    }

    public String getLockMassCharge(){
        return lockMassCharge;
    }

    public double getLockMassValue(){
        return lockMassValue;
    }

    public void setLockMasses(double lockMassValue, String lockMassCharge){
        this.lockMassValue = lockMassValue;
        this.lockMassCharge = lockMassCharge;
    }

    public void setLockMassCharge(String lockMassCharge){
        this.lockMassCharge = lockMassCharge;
    }
}
