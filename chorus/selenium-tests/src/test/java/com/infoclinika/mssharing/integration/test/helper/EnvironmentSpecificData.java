package com.infoclinika.mssharing.integration.test.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.infoclinika.mssharing.integration.test.data.UserData;
import com.infoclinika.mssharing.integration.test.exception.FuncTestInfrastructureException;
import com.infoclinika.mssharing.integration.test.logging.Logger;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Scanner;

/**
 * @author Alexander Orlov
 */
public class EnvironmentSpecificData {

    private static final String DATA_ROOT_FOLDER = "selenium-tests\\src\\test\\resources\\data";

    public UserData admin;
    public UserData pavelKaplinAtGmail;
    public UserData pavelKaplinAtTeamdev;
    public UserData geneSimmonsAtGmail;
    public UserData karrenKoe;
    public String project1;
    public String project2;
    public String defaultLaboratory;
    public String defaultInstrument;

    public static void main(String[] args) throws IOException {
        EnvironmentSpecificData environmentSpecificData = new EnvironmentSpecificData();
//        environmentSpecificData.admin = new UserData.Builder().email("chorus.adm@gmail.com").password("pwd").firstName("TeamDev").lastName("Admin").build();
        environmentSpecificData.admin = new UserData.Builder().email("chorusproject.no.reply@gmail.com").password("pwd").firstName("Mark").lastName("Adminovich").build();

        environmentSpecificData.pavelKaplinAtGmail = new UserData.Builder().email("pavel.kaplin@gmail.com").password("pwd").firstName("Pavel").lastName("Kaplin").build();
        environmentSpecificData.pavelKaplinAtTeamdev = new UserData.Builder().email("pavel.kaplin@teamdev.com").password("pwd").firstName("Pavel").lastName("Kaplin").build();
        environmentSpecificData.geneSimmonsAtGmail = new UserData.Builder().email("chorus.tester@gmail.com").password("pwd").firstName("Gene").lastName("Simmons").build();
        environmentSpecificData.karrenKoe = new UserData.Builder().email("karren.koe@gmail.com").password("pwd").firstName("Karren").lastName("Koe").build();
        environmentSpecificData.project1 = "Geno 2.0: The Greatest Journey Ever Told Let's Repeat It Second Time To Check Ellipsizing";
        environmentSpecificData.project2 = "Neanderthal Genome Project";
        environmentSpecificData.defaultLaboratory = "First Chorus Lab Very Long Name For Testing Ellipsize";
        environmentSpecificData.defaultInstrument = "Genome Machine";
        serializeData(environmentSpecificData, "staging.json");
//        deserializeData("staging.json");
    }

    public static void serializeData(EnvironmentSpecificData data, String fileName) throws IOException {
        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
        String json = gson.toJson(data);
        System.out.println(json);
        final File destFile = new File(DATA_ROOT_FOLDER, fileName);
        Logger.log(" - Writing to the file: " + destFile.getAbsolutePath());
        final FileOutputStream fos = new FileOutputStream(destFile);
        IOUtils.write(json, fos);
        fos.flush();
    }

    public static EnvironmentSpecificData deserializeData(String filePath) {
        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
        Type type = new TypeToken<EnvironmentSpecificData>() {
        }.getType();
        Logger.log("---Deserialization in java Object---");
        String json;
        try {
            json = new Scanner(new File(filePath)).useDelimiter("\\Z").next();
        } catch (IOException e) {
            throw new FuncTestInfrastructureException("Json file with data by path " + filePath + " not found");
        }
        EnvironmentSpecificData desData = gson.fromJson(json, type);
        Logger.log(desData);
        return desData;
    }
}
