package com.infoclinika.mssharing.parser;

//import com.amazonaws.util.json.JSONException;
//import com.sun.jersey.api.client.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.net.URI;
import java.util.Deque;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Created by davemakhervaks on 7/24/17.
 */
public class RestfulAPI {

    public RestfulAPI(String urlName, String sessionID, String zipLocation, String newLocation) throws IOException {

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(urlName);
        httpGet.addHeader("Cookie", sessionID);
        HttpResponse httpResponse = httpClient.execute(httpGet);
        File file = new File(zipLocation);

        InputStream input = httpResponse.getEntity().getContent();
        OutputStream output = new FileOutputStream(file);
        byte[] buffer = new byte[1024]; // Adjust if you want
        int bytesRead;

        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }

        input.close();
        output.close();

        File directory = new File(newLocation);
        unzip(file, directory);

        createDirectoryWithJustTextFiles(directory);

    }


    public static void getColInfoJSONFile(String urlName, String sessionID, String jsonFile) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(urlName);
        httpGet.addHeader("Cookie", sessionID);
        HttpResponse httpResponse = httpClient.execute(httpGet);
        HttpEntity entity = httpResponse.getEntity();

        // A Simple JSON Response Read
        InputStream instream = entity.getContent();
        String result = convertStreamToString(instream);
        // now you have the string representation of the HTML request
        instream.close();

        File file = new File(jsonFile);

        /* This logic is to create the file if the
         * file is not already present
         */
        if (!file.exists()) {
            file.createNewFile();
        }

        //Here true is to append the content to file
        FileWriter fw = new FileWriter(file, true);
        //BufferedWriter writer give better performance
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(result);
        //Closing BufferedWriter Stream
        bw.close();


    }

    private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }


    public static void createDirectoryWithJustTextFiles(File resultingProcessingFiles) throws IOException {
        File[] files = resultingProcessingFiles.listFiles();

        for (File item : files) {
            if (item.getName().equals("samples")) {
                String name = "/";
                String keyWord = item.listFiles()[0].getName();
                if (keyWord.contains("gene")) {
                    name += "genePracticeFiles";
                } else if (keyWord.contains("exon")) {
                    name += "exonPracticeFiles";
                }
                File newDirectory = new File(resultingProcessingFiles.getParent() + name);
                item.renameTo(newDirectory);

            }
        }
    }

    public static void unzip(File zipfile, File directory) throws IOException {
        ZipFile zfile = new ZipFile(zipfile);
        Enumeration<? extends ZipEntry> entries = zfile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            File file = new File(directory, entry.getName());
            if (entry.isDirectory()) {
                file.mkdirs();
            } else {
                file.getParentFile().mkdirs();
                InputStream in = zfile.getInputStream(entry);
                try {
                    copy(in, file);
                } finally {
                    in.close();
                }
            }
        }
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        while (true) {
            int readCount = in.read(buffer);
            if (readCount < 0) {
                break;
            }
            out.write(buffer, 0, readCount);
        }
    }

    private static void copy(File file, OutputStream out) throws IOException {
        InputStream in = new FileInputStream(file);
        try {
            copy(in, out);
        } finally {
            in.close();
        }
    }

    private static void copy(InputStream in, File file) throws IOException {
        OutputStream out = new FileOutputStream(file);
        try {
            copy(in, out);
        } finally {
            out.close();
        }
    }


    public static void zip(File directory, File zipfile) throws IOException {
        URI base = directory.toURI();
        Deque<File> queue = new LinkedList<File>();
        queue.push(directory);
        OutputStream out = new FileOutputStream(zipfile);
        Closeable res = out;
        try {
            ZipOutputStream zout = new ZipOutputStream(out);
            res = zout;
            while (!queue.isEmpty()) {
                directory = queue.pop();
                for (File kid : directory.listFiles()) {
                    String name = base.relativize(kid.toURI()).getPath();
                    if (kid.isDirectory()) {
                        queue.push(kid);
                        name = name.endsWith("/") ? name : name + "/";
                        zout.putNextEntry(new ZipEntry(name));
                    } else {
                        zout.putNextEntry(new ZipEntry(name));
                        copy(kid, zout);
                        zout.closeEntry();
                    }
                }
            }
        } finally {
            res.close();
        }
    }


}

