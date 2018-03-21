package com.infoclinika.mssharing.utils.logging;

import com.google.common.base.Optional;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Elena Kurilina
 */
public class LogBuffer extends AppenderSkeleton {

    private int size;
    private List<String> buffer = new ArrayList<String>();
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    protected void append(LoggingEvent event) {
        final String message = this.layout.format(event);
        List<String> newLines = Arrays.asList(message.split("\r\n|\r|\n"));
        if (!isBufferCanEat(newLines.size())) {
            cleanSpaceFor(newLines.size());
        }
        buffer.addAll(newLines);

    }

    public String getLastLog() {
        final StringBuilder result = new StringBuilder();
        for (String line : buffer) {
            result.append(line).append("\n");
        }
        return result.toString();

    }

    public Optional<File> getLasLogFile() throws IOException {
        File file = null;
        try {
            file = File.createTempFile("tomcatLog" + dateFormat.format(new Date()), ".txt");
            file.deleteOnExit();

            Writer writer = new OutputStreamWriter(new FileOutputStream(file));
            writer.write(getLastLog());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.of(file);
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }

    @Override
    public void close() {
        buffer.clear();
    }

    private boolean isBufferCanEat(int forAdd) {
        return (buffer.size() + forAdd) < size;
    }

    private void cleanSpaceFor(int forAdd) {
        final int forDelete = buffer.size() + forAdd - size;
        List<String> oldLines = buffer.subList(0, forDelete);
        oldLines.clear();
    }

}
