package com.fluidtouch.noteshelf.commons.utils;

import android.graphics.Color;

public class ColorUtil {

    public static int iOSColor(int intColor) {
        String strColor = ColorUtil.hexStringFromInt(intColor);
        if (strColor.length() < 8) {
            strColor = strColor.replace("#", "#00");
        }
        return Color.parseColor(strColor);
    }

    public static int androidColor(int intColor) {
        String strColor = ColorUtil.hexStringFromInt(intColor);
        if (strColor.length() > 8) {
            strColor = "#" + strColor.substring(3);
        }
        return Color.parseColor(strColor);
    }

    public static String hexStringFromInt(int intColor) {
        return String.format("#%06X", 0xFFFFFF & intColor);
    }

    public static int intFromHexString(String hexColor) {
        return Color.parseColor(hexColor);
    }

    public static boolean isLightColor(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness < 0.3;
    }

    public static String changeColorHSB(String color) {
        float[] hsv = new float[3];
        int brandColor = Color.parseColor(color);
        Color.colorToHSV(brandColor, hsv);
        hsv[1] = hsv[1] + 0.1f;
        hsv[2] = hsv[2] - 0.1f;
        int argbColor = Color.HSVToColor(hsv);
        String hexColor = String.format("#%08X", argbColor);
        return hexColor;
    }

    private static int hexToDecimal(String string) {
        return Integer.parseInt(string, 16);
    }
}
