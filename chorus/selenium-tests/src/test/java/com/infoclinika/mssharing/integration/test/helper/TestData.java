package com.infoclinika.mssharing.integration.test.helper;

import com.infoclinika.mssharing.integration.test.data.proteinsearch.ShotGunSearchData;
import com.infoclinika.mssharing.integration.test.data.UserData;


public abstract class TestData {

    protected String productionUrl = "https://chorusproject.org/pages/dashboard.html";

    protected UserData admin = new UserData.Builder().email("chorusproject.no.reply@gmail.com").password("pwd").firstName("Mark").lastName("Adminovich").build();
    protected UserData pavelKaplinAtGmail = new UserData.Builder().email("pavel.kaplin@gmail.com").password("pwd").firstName("Pavel").lastName("Kaplin").build();
    protected UserData pavelKaplinAtTeamdev = new UserData.Builder().email("pavel.kaplin@teamdev.com").password("pwd").build();
    protected UserData geneSimmonsAtGmail = new UserData.Builder().email("chorus.tester@gmail.com").password("pwd").firstName("Gene").lastName("Simmons").build();

//    protected ExperimentData experimentWithSearches = new ExperimentData("Albumin (Ecoli) Test");

    protected ShotGunSearchData shotGunSearchData = new ShotGunSearchData.Builder()
            .name("ShotGun for automated testing")
            .build();
}
