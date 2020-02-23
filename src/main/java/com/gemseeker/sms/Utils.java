package com.gemseeker.sms;

import java.text.SimpleDateFormat;
import java.util.Locale;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author gemini1991
 */
public class Utils {

    /**
     * Date format YYYY-MM-dd hh:mm:ss (ex: 2020-02-24 04:34:12).
     */
    public static final SimpleDateFormat MYSQL_DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    
    /**
     * Date format MMM dd, yyyy (ex: Feb 24, 2020).
     */
    public static final SimpleDateFormat TABLE_DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy");
    
    /**
     * Date format MMMM dd, yyyy (ex: February 24, 2020).
     */
    public static final SimpleDateFormat DATE_FORMAT_1 = new SimpleDateFormat("MMMM dd, yyyy");
    
    /**
     * Date format dd/MM/yyyy (ex: 24/02/2020).
     */
    public static final SimpleDateFormat DATE_FORMAT_2 = new SimpleDateFormat("dd/MM/yyyy");
    
    /**
     * Sets the TextField to accept numerical inputs only including the period `.`
     * character.
     * @param textField 
     */
    public static void setAsNumericalTextField(TextField textField) {
        if (textField != null) {
            textField.addEventFilter(KeyEvent.KEY_TYPED, evt -> {
                if (!"0123456789.".contains(evt.getCharacter())) evt.consume();
            });
        }
    }
    
    /**
     * Sets the TextFields to accept numerical inputs only including the period `.`
     * character.
     * @param textFields 
     */
    public static void setAsNumericalTextFields(TextField...textFields) {
        for (TextField tf : textFields) setAsNumericalTextField(tf);
    }
    
    /**
     * Disables the typing in TextField.
     * @param textFields 
     */
    public static void disableTyping(TextField...textFields) {
        for (TextField tf: textFields) {
            tf.addEventFilter(KeyEvent.KEY_TYPED, evt -> evt.consume());
        }
    }

    public static int monthIntegerValue(String month) {
        switch (month.toUpperCase()) {
            case "JANUARY": return 1;
            case "FEBRUARY": return 2;
            case "MARCH": return 3;
            case "APRIL": return 4;
            case "MAY": return 5;
            case "JUNE": return 6;
            case "JULY": return 7;
            case "AUGUST": return 8;
            case "SEPTEMBER": return 9;
            case "OCTOBER": return 10;
            case "NOVEMBER": return 11;
            case "DECEMBER": return 12;
            default: return -1;
        }
    }
}
