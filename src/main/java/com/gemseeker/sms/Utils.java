package com.gemseeker.sms;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
     * Date format for converting java.util.Date to LocalDate.
     */
    public static final SimpleDateFormat LOCAL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    
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
    
    public static void setAsIntegerTextField(TextField textField) {
        if (textField != null) {
            textField.addEventFilter(KeyEvent.KEY_TYPED, evt -> {
                if (!"0123456789".contains(evt.getCharacter())) evt.consume();
            });
        }
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
    
    public static String monthStringValue(int month) {
        if (month < 0 || month > 12) throw new RuntimeException("Invalid month value.");
        
        switch (month) {
            case 1: return "January";
            case 2: return "February";
            case 3: return "March";
            case 4: return "April";
            case 5: return "May";
            case 6: return "June";
            case 7: return "July";
            case 8: return "August";
            case 9: return "September";
            case 10: return "October";
            case 11: return "November";
            case 12: return "December";
            default: return "Invalid";
        }
    }
    
    public static Date getDateNow() {
        return Calendar.getInstance().getTime();
    }
    
    public static int compare(Calendar cal1, Calendar cal2) {
        return cal1.compareTo(cal2);
    }
    
    public static ObservableList<String> getProvinceList() {
        return FXCollections.observableArrayList(
                "South Cotabato"
        );
    }
    
    public static ObservableList<String> getCityList(String province) {
        if (province.equals("South Cotabato")) {
            return getSouthCotabatoCities();
        } else {
            return FXCollections.observableArrayList();
        }
    }
    
    public static ObservableList<String> getBarangayList(String city) {
        switch (city) {
            case "Banga":
                return getBangaBarangays();
            case "General Santos":
                return getGensanBarangays();
            case "Koronadal":
                return getKoronadalBarangays();
            case "Lake Sebu":
                return getLakeSebuBarangays();
            case "Norala":
                return getNoralaBarangays();
            case "Polomolok":
                return getPolomolokBarangays();
            case "Santo Niño":
                return getSantoNinoBarangays();
            case "Surallah":
                return getSurallahBarangays();
            case "T'Boli":
                return getTboliBarangays();
            case "Tampakan":
                return getTampakanBarangays();
            case "Tantangan":
                return getTantanganBarangays();
            case "Tupi":
                return getTupiBarangays();
            default:
                return FXCollections.observableArrayList();
        }
    }
    
    private static ObservableList<String> getSouthCotabatoCities() {
        return FXCollections.observableArrayList(
                "Banga", "General Santos", "Koronadal", "Lake Sebu", "Norala",
                "Polomolok", "Santo Niño", "Surallah", "T'Boli", "Tampakan",
                "Tantangan", "Tupi"
        );
    }
    
    private static ObservableList<String> getBangaBarangays() {
        return FXCollections.observableArrayList(
                "Benitez (Poblacion)", "Cabudian", "Cabuling", "Cinco (Barrio 5)", "Derilon",
                "El Nonok", "Improgo Village (Poblacion)", "Kusan (Barrio 8)",
                "Lam-Apos", "Lamba", "Lambingi", "Lampari", "Liwanay (Barrio 1)",
                "Malaya (Barrio 9)", "Punong Grande (Barrio 2)", "Rang-ay (Barrio 4)",
                "Reyes (Poblacion)", "Rizal (Barrio 3)", "Rizal Poblacion",
                "San Jose (Barrio 7)", "San Vicente (Barrio 6)", "Yangco Poblacion"
        );
    }
    
    private static ObservableList<String> getGensanBarangays() {
        return FXCollections.observableArrayList(
                "Apopong", "Baluan", "Batomelong", "Buayan", "Bula", "Calumpang",
                "City Heights", "Conel", "Dadiangas East", "Dadiangas North",
                "Dadiangas South", "Dadiangas West", "Fatima", "Katangawan",
                "Labangal", "Lagao", "Ligaya", "Mabuhay", "Olympog", "San Isidro",
                "San Jose", "Siguel", "Sinawal", "Tambler", "Tinagacan", "Upper Labay"
        );
    }
    
    private static ObservableList<String> getKoronadalBarangays() {
        return FXCollections.observableArrayList(
                "Assumption (Bulol)", "Avanceña (Barrio 3)", "Cacub", "Caloocan",
                "Carpenter Hill", "Concepcion (Barrio 6)", "Esperanza", "Mabini",
                "Magsaysay", "Mambucal", "Namnama", "New Pangasinan (Barrio 4)",
                "Paraiso", "Rotonda", "San Isidro", "San Jose (Barrio 5)", "San Roque",
                "Saravia (Barrio 8)", "Topland (Barrio 7)", "General Paulino Santos (Barrio 1)",
                "Morales", "Santa Cruz", "Santo Niño (Barrio 2)", "Zone 1 (Poblacion Zone 1)",
                "Zone 2 (Poblacion Zone 2)", "Zone 3 (Poblacion Zone 3)", "Zone 4 (Poblacion Zone 4)"
        );
    }
    
    private static ObservableList<String> getLakeSebuBarangays() {
        return FXCollections.observableArrayList(
                "Bacdulong", "Denlag", "Halilan", "Hanoon", "Klubi", "Lake Lahit",
                "Lamcade", "Lamdalag", "Lamfugon", "Lamlahak", "Lower Maculan",
                "Luhib", "Ned", "Poblacion", "Lake Seloton", "Talisay", "Takunel",
                "Upper Maculan", "Tasiman", "Koronadal"
        );
    }
    
    private static ObservableList<String> getNoralaBarangays() {
        return FXCollections.observableArrayList(
                "Dumaguil", "Esperanza", "Kibid", "Lapuz", "Liberty", "Lopez Jaena",
                "Matapol", "Poblacion", "Puti", "San Jose", "San Miguel", "Simsiman",
                "Tinago", "Benigno Aquino Jr."
        );
    }
    
    private static ObservableList<String> getPolomolokBarangays() {
        return FXCollections.observableArrayList(
                "Poblacion", "Cannery Site", "Magsaysay", "Bentung", "Crossing Palkan",
                "Glamang", "Kinilis", "Klinan 6", "Koronadal Proper", "Lam Caliaf",
                "Landan", "Lapu", "Lumakil", "Maligo", "Pagalungan", "Palkan",
                "Polo", "Rubber", "Silway 7", "Silway 8", "Sulit", "Sumbakil",
                "Upper Klinan"
        );
    }
    
    private static ObservableList<String> getSantoNinoBarangays() {
        return FXCollections.observableArrayList(
                "Ambalgan", "Guinsang-an (Barrio 4)", "Katipunan (Barrio 11)",
                "Manuel Roxas (Barrio 10)", "New Panay (Barrio 9)", "Poblacion (Barrio 13)",
                "San Isidro (Barrio 12)", "San Vicente (Barrio 5)", "Teresita",
                "Sajaneba"
        );
    }
    
    private static ObservableList<String> getSurallahBarangays() {
        return FXCollections.observableArrayList(
                "Buenavista", "Centrala", "Colongolo", "Dajay", "Duengas",
                "Canahay (Godwino)", "Lambontong", "Lamian", "Lamsugod", "Libertad",
                "Little Baguio", "Moloy", "Naci", "Talahik", "Tubi-Allah", "Upper Sepaka",
                "Veterans"
        );
    }
    
    private static ObservableList<String> getTboliBarangays() {
        return FXCollections.observableArrayList(
                "Aflek", "Afus", "Basag", "Datal Bob", "Desawo", "Dlanag", "Edwards (Poblacion)",
                "Kematu", "Laconon", "Lambangan", "Lambuling", "Lamhako", "Lamsalome",
                "Lemsnolon", "Maan", "Malugong", "Mongocayo", "New Dumangas", "Poblacion",
                "Salacafe", "Sinolon", "Talcon", "Talufo", "T'bolok", "Tudok"
        );
    }
    
    private static ObservableList<String> getTampakanBarangays() {
        return FXCollections.observableArrayList(
                "Albagan", "Buto", "Danlag", "Kipalbig", "Lambayong", "Liberty",
                "Lampitak", "Maltana", "Poblacion", "Palo", "Pula Bato", "San Isidro",
                "Santa Cruz", "Tablu"
        );
    }
    
    private static ObservableList<String> getTantanganBarangays() {
        return FXCollections.observableArrayList(
                "Bukay Pait", "Cabuling", "Dumadalig", "Libas", "Magon Baguilan Gunting",
                "Maibo", "Mangilala", "New Cuyapo", "New Iloilo", "New Lambunao",
                "Poblacion", "San Felipe", "Tinongcop"
        );
    }
    
    private static ObservableList<String> getTupiBarangays() {
        return FXCollections.observableArrayList(
                "Acmonan", "Bololmala", "Bunao", "Cebuano", "Crossing Rubber",
                "Kablon", "Kalkam", "Linan", "Lunen", "Miasong", "Palian", "Poblacion",
                "Polonuling", "Simbo", "Tubeng"
        );
    }
}
