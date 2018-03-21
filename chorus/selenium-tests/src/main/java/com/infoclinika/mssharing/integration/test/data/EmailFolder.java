package com.infoclinika.mssharing.integration.test.data;

/**
 * @author Alexander Orlov
 */
public enum EmailFolder {

    VERIFY_EMAIL ("VerifyEmail"),
    INSTRUMENT_AVAILABLE("Instrument/Available"),
    INSTRUMENT_APPROVED("Instrument/Approved"),
    RESET_PASSWORD("ResetPassword"),
    LAB_CREATION_APPROVED("LabCreation/Approved"),
    LAB_CREATION_PASSWORD("LabCreation/Password"),
    LAB_CREATION_REFUSED("LabCreation/Refused"),
    LAB_MEMBERSHIP_APPROVED("LabMembership/Approved"),
    INVITATION("Invitation");



    private String value;

    private EmailFolder(String value) {
        this.value = value;
    }

    public String getName() {
        return value;
    }
}
