package com.infoclinika.mssharing.propertiesprovider;

import java.io.*;
import java.util.Properties;

public class PropertiesCopier {

    public static void copyProperties(File to, File... from) throws IOException {
        final Properties properties = new Properties();

        for (File file : from) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                properties.load(br);
            }
        }

        try (BufferedReader br = new BufferedReader(new FileReader(to));
             PrintWriter pw = new PrintWriter(new FileWriter(new File(to.getParent(), "temp.properties"), false))) {

            String str;
            while ((str = br.readLine()) != null) {
                if (str.contains("=")) {
                    final String[] values = str.split("=");
                    final String key = values[0];

                    if (properties.containsKey(key)) {
                        pw.println(key + "=" + properties.get(key));
                    } else {
                        pw.println(str);
                        System.out.println("Property does not exist: " + key);
                    }
                } else {
                    pw.println(str);
                }
            }
        }
    }
}
