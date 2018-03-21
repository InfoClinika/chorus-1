package com.infoclinika.mssharing.integration.test.listeners;

import com.infoclinika.mssharing.integration.test.exception.FuncTestInfrastructureException;
import com.infoclinika.mssharing.integration.test.logging.Logger;
import com.infoclinika.mssharing.integration.test.preconditions.AfterTestComplete;
import com.infoclinika.mssharing.integration.test.preconditions.PrepareTest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.TestListenerAdapter;
import org.testng.internal.ConstructorOrMethod;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.getDriver;

/**
 * @author Alexander Orlov
 */
public class ChorusListener extends TestListenerAdapter {

    private static final Log LOG = LogFactory.getLog(ChorusListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        try{
        Object testCase = result.getInstance();
        ConstructorOrMethod testMethod = result.getMethod().getConstructorOrMethod();
        invokeAnnotatedMethods(testCase, testMethod, PrepareTest.class);
        } catch (Throwable e) {
            LOG.info(e.getMessage(), e);
            throw new FuncTestInfrastructureException(e);
        }
    }

    @Override
    public void onTestSuccess(ITestResult tr) {
        try{
            Object testCase = tr.getInstance();
            ConstructorOrMethod testMethod = tr.getMethod().getConstructorOrMethod();
            invokeAnnotatedMethods(testCase, testMethod, AfterTestComplete.class);
        } catch (Throwable e) {
            LOG.info(e.getMessage(), e);
            throw new FuncTestInfrastructureException(e);
        }
    }

    @Override
    public void onTestFailure(ITestResult tr) {
        try{
            Object testCase = tr.getInstance();
            ConstructorOrMethod testMethod = tr.getMethod().getConstructorOrMethod();
            makeScreenshot(tr);
            invokeAnnotatedMethods(testCase, testMethod, AfterTestComplete.class);
        } catch (Throwable e) {
            LOG.info(e.getMessage(), e);
            throw new FuncTestInfrastructureException(e);
        } finally {
            getDriver().quit();
        }
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
        try{
            Object testCase = tr.getInstance();
            ConstructorOrMethod testMethod = tr.getMethod().getConstructorOrMethod();
            invokeAnnotatedMethods(testCase, testMethod, AfterTestComplete.class);
        } catch (Throwable e) {
            LOG.info(e.getMessage(), e);
            throw new FuncTestInfrastructureException(e);
        }
    }

    private static void invokeAnnotatedMethods(Object testCase, ConstructorOrMethod testMethod, Class<? extends Annotation> annotation) throws InvocationTargetException, IllegalAccessException {
        Method[] methods = testCase.getClass().getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(annotation)) {
                method.setAccessible(true);
                method.invoke(testCase, testMethod.getMethod());
            }
        }
    }

    private void makeScreenshot(ITestResult result) {
        File file = new File("");
        Calendar calendar = GregorianCalendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SSS", Locale.ENGLISH);
        Reporter.setCurrentTestResult(result);
        String path = file.getAbsolutePath();
        String reportsFolder = path + "\\target\\surefire-reports\\html\\";
        String screenShotsFolder = "failure-screenshots\\";
        String screenShotName = result.getMethod().getMethodName() + "(" + result.getTestClass().getName() + ")" + "-"
                + formatter.format(calendar.getTime()) + ".png";
        // Create the filename for the screen shots
        String fileName = reportsFolder
                + screenShotsFolder
                + screenShotName;
        //Put the path, written with red font and link to the screen shot into ReportNG output.
        Logger.log("<font color='red'>Fail! Screenshot with test failure saved at " + fileName + "</font>");
        Logger.log("<a href='" + screenShotsFolder + screenShotName + "'>" + screenShotName + "</a>");
        try {
            File scrFile = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.FILE);
            try {
                FileUtils.copyFile(scrFile, new File(fileName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (UnreachableBrowserException e) {
            Logger.log("Cannot capture the screenshot. Error communicating with the remote browser. It may have died.");
        }
        Reporter.setCurrentTestResult(null);
    }
}
