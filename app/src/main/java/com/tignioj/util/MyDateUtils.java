package com.tignioj.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MyDateUtils {
//    public static SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    public static SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");

    //https://stackoverflow.com/questions/20165564/calculating-days-between-two-dates-with-java
    public static String getDiffDay(Date date1, Date date2) {
        long diff = date2.getTime() - date1.getTime();
        long convert = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        return String.valueOf(convert);
    }

    public static String format(Date date) {
        return myFormat.format(date);
    }

    public static Date parse(String s) {
        try {
            return myFormat.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}
