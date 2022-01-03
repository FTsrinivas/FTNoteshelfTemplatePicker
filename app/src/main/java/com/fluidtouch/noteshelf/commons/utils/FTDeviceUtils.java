package com.fluidtouch.noteshelf.commons.utils;

import android.os.Build;


public class FTDeviceUtils {

    private FTDeviceUtils() {
        throw new IllegalStateException("NumberUtils class");
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    public static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static long getTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }
}
