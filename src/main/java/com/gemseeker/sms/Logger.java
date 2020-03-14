package com.gemseeker.sms;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author gemini1991
 */
public final class Logger {

    public Logger(boolean logToFile) {
        if (logToFile) {
            setupLogToFile();
        }
    }
    
    private void setupLogToFile() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy_h-m-s");
        String filename = dateFormat.format(cal.getTime()).toLowerCase() + ".txt";
        File file = new File("log/" + filename);
        if (!file.exists()) {
            boolean created = file.mkdirs();
            if (created) {
                try {
                    System.setOut(new PrintStream(file));
                    System.setErr(new PrintStream(file));
                } catch (FileNotFoundException e) {
                    System.err.println("Failed to create log file.\n" + e);
                }
            }
        }
    }
    
    public void log(String debugName, String message) {
        System.out.println(String.format("%s [VERBOSE]: %s", debugName, message));
    }
    
    public void logErr(String debugName, String message) {
        System.err.println(String.format("%s [ERROR]: %s", debugName, message));
    }
    
    public void logErr(String debugName, String message, Exception e) {
        System.err.println(String.format("%s [ERROR]: %s {%s}", debugName, message, e.getLocalizedMessage()));
    }
}
