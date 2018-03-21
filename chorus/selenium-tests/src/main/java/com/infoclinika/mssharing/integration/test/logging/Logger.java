package com.infoclinika.mssharing.integration.test.logging;


import org.testng.Reporter;

import java.lang.reflect.Method;

/**
 * @author Sergii Moroz
 *
 */
public class Logger {

    public static void log(String message) {
        Reporter.log(message + "<br>");
    }

    public static void log(int i) {
        log("" + i);
    }

    public static void log(boolean b) {
        log("" + b);
    }

    public static void log(Object o){
        log(o.toString());
    }

    public static void title(String message) {
        log("<h3>" + message + "</h3>");
    }

    public static void logMethodName(Method method) {
        title("Test method: " + method.getName());
    }

}
