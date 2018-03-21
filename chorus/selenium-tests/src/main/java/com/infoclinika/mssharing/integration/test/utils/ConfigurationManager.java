package com.infoclinika.mssharing.integration.test.utils;

import com.infoclinika.mssharing.integration.test.listeners.LoggingEventListener;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author Sergii Moroz
 */
public class ConfigurationManager {

    private static WebDriver driver = null;
    private static final WebDriverEventListener eventListener = new LoggingEventListener();
    private static final String URL = "app.under.test";
    private static final String MAC_DRIVER = "/chromedriver/mac/chromedriver";
    private static final String WINDOWS_DRIVER = "/chromedriver/windows/chromedriver.exe";
    private static final String localUrl = "http://localhost:8080/pages/dashboard.html";
    public static final String OS = System.getProperty("os.name").toLowerCase();
    public static final int DEFAULT_WAIT = 3000;


    public static void stopDriver() {
        driver.quit();
    }

    public static WebDriver getDriver() {
        return driver;
    }

    public static void setImplicitlyWait(int milliSeconds) {
        getDriver().manage().timeouts().implicitlyWait(milliSeconds, TimeUnit.MILLISECONDS);
    }

    /* Method for killing "chromedriver.exe" process. This method was added, because sometimes, if selenium test failed
    unexpectedly the driver remains in the processes. */
    public static void destroyAllRunningChromeDrivers() {
        if (OS.contains("win")) {
            try {
                Runtime.getRuntime().exec("taskkill /F /IM chromedriver.exe");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static WebDriver startDriver(String url) {
        if (OS.contains("win")) {
            System.setProperty("webdriver.chrome.driver", ConfigurationManager.class.getResource(WINDOWS_DRIVER).getFile());
        } else if (OS.contains("mac")) {
            File cDriver = new File(ConfigurationManager.class.getResource(MAC_DRIVER).getFile());
            if (!cDriver.canExecute()) {
                cDriver.setExecutable(true);
            }
            System.setProperty("webdriver.chrome.driver", ConfigurationManager.class.getResource(MAC_DRIVER).getFile());
        }
        ChromeOptions options = new ChromeOptions();
        options.addArguments(Arrays.asList(new String[]{"test-type", "start-maximized"}));
        driver = new EventFiringWebDriver(new ChromeDriver(options)).register(eventListener);
        setImplicitlyWait(DEFAULT_WAIT);
        navigateTo(url);
        driver.manage().window().maximize();
        return driver;
    }

    public static WebDriver startDriver() {
        return startDriver(getDefinedUrl());
    }

    public static void navigateToChorus() {
        driver.get(getDefinedUrl());
    }

    private static String getDefinedUrl() {
        if (System.getProperty(URL) == null) {
            return localUrl;
        } else {
            return System.getProperty(URL);
        }
    }

    public static void navigateTo(String link) {
        getDriver().get(link);
    }

}
