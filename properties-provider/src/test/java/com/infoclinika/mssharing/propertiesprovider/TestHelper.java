package com.infoclinika.mssharing.propertiesprovider;

import java.lang.reflect.Method;

public class TestHelper {

    //assertEquals(amazonPropertiesProvider.getAccessKey(), "<amazon key>")
    public static void printFields(Class clazz) {
        final Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            System.out.println("assertEquals(rabbitPropertiesProvider." + method.getName() + "(), \"\");");
        }
    }

    public static void main(String[] args) {
        TestHelper.printFields(RabbitPropertiesProvider.class);
    }
}
