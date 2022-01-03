package com.fluidtouch.noteshelf.commons.utils;

import android.text.format.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil {
    public static SimpleDateFormat getDateFormat(String format) {
        return new SimpleDateFormat(format, Locale.getDefault());
    }

    public static Date getDate(String format, String parseString, Date defaultDate) {
        try {
            return getDateFormat(format).parse(parseString);
        } catch (ParseException e) {
            return defaultDate;
        }
    }

    public static String getDateAndTime(String outputFormat, long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time * 1000);
        return DateFormat.format(outputFormat, cal).toString();
    }

    public static int getDateDifference(Date firstDate, Date secondDate, boolean isYear) {
        Calendar firstCalendar = Calendar.getInstance();
        Calendar secondCalendar = Calendar.getInstance();
        firstCalendar.setTime(firstDate);
        secondCalendar.setTime(secondDate);
        if (isYear) {
            return secondCalendar.get(Calendar.YEAR) - firstCalendar.get(Calendar.YEAR);
        } else {
            int yearDifference = secondCalendar.get(Calendar.YEAR) - firstCalendar.get(Calendar.YEAR);
            return (yearDifference * 12) + secondCalendar.get(Calendar.MONTH) - firstCalendar.get(Calendar.MONTH);
        }
    }
}
