package com.fluidtouch.noteshelf.templatepicker.common.util;

import android.util.Log;

import com.fluidtouch.noteshelf.templatepicker.common.modelclasses.ChildItem;
import com.fluidtouch.noteshelf.templatepicker.common.modelclasses.ColorRGB;
import com.fluidtouch.noteshelf.templatepicker.common.modelclasses.TemplatesLineInfo;
import com.fluidtouch.noteshelf2.R;

import java.util.ArrayList;
import java.util.List;

public class FTTemplateMoreDetailsInfo {

    public FTTemplateMoreDetailsInfo() {
    }

    public int hexToDecimal(String hexaClr) {
        return Integer.parseInt(hexaClr, 16);
    }

    public ColorRGB getRGBValue(String colorName) {
        String hexcolorNameCnvrted = null;

        if (colorName.contains("#")) {
            hexcolorNameCnvrted = colorName.split("#")[1].split("-")[0];
        }

        for (int i=1;i<=6;i++) {
            if (hexcolorNameCnvrted.length() < i) {
                hexcolorNameCnvrted = hexcolorNameCnvrted + "0";
            }
        }

        Log.d("TemplatePicker==>","getRGBValue hexcolorNameCnvrted::-"+hexcolorNameCnvrted);
        int bgcolorRed = hexToDecimal(hexcolorNameCnvrted.substring(0, 2));
        int bgcolorGreen = hexToDecimal(hexcolorNameCnvrted.substring(2, 4));
        int bgcolorBlue = hexToDecimal(hexcolorNameCnvrted.substring(4, 6));
        int bgcolorAlpha = Math.round(Float.parseFloat(colorName.split("-")[1]) * 255);

        ColorRGB mClrRGB = new ColorRGB(bgcolorRed,bgcolorGreen,bgcolorBlue,bgcolorAlpha);
        return mClrRGB;
    }

    public int getRedClrValue(String colorName) {
        String hexcolorNameCnvrted = null;

        if (colorName.contains("#")) {
            hexcolorNameCnvrted = colorName.split("#")[1].split("-")[0];
        }

        for (int i=1;i<=6;i++) {
            if (hexcolorNameCnvrted.length() < i) {
                hexcolorNameCnvrted = hexcolorNameCnvrted + "0";
            }
        }

        Log.d("TemplatePicker==>","getRedClrValue hexcolorNameCnvrted::-"+hexcolorNameCnvrted);
        int bgcolorRed = hexToDecimal(hexcolorNameCnvrted.substring(0, 2));
        return bgcolorRed;
    }

    public int getGreenClrValue(String colorName) {
        String hexcolorNameCnvrted = null;

        if (colorName.contains("#")) {
            hexcolorNameCnvrted = colorName.split("#")[1].split("-")[0];
        }

        for (int i=1;i<=6;i++) {
            if (hexcolorNameCnvrted.length() < i) {
                hexcolorNameCnvrted = hexcolorNameCnvrted + "0";
            }
        }

        Log.d("TemplatePicker==>","getGreenClrValue hexcolorNameCnvrted::-"+hexcolorNameCnvrted);

        int bgcolorGreen = hexToDecimal(hexcolorNameCnvrted.substring(2, 4));
        return bgcolorGreen;
    }

    public int getBlueClrValue(String colorName) {
        String hexcolorNameCnvrted = null;

        if (colorName.contains("#")) {
            hexcolorNameCnvrted = colorName.split("#")[1].split("-")[0];
        }

        for (int i=1;i<=6;i++) {
            if (hexcolorNameCnvrted.length() < i) {
                hexcolorNameCnvrted = hexcolorNameCnvrted + "0";
            }
        }

        Log.d("TemplatePicker==>","getBlueClrValue hexcolorNameCnvrted::-"+hexcolorNameCnvrted);

        int bgcolorBlue = hexToDecimal(hexcolorNameCnvrted.substring(4, 6));
        return bgcolorBlue;
    }

    public int getAlphaValue(String colorName) {
        String hexcolorNameCnvrted = null;

        if (colorName.contains("#")) {
            hexcolorNameCnvrted = colorName.split("#")[1].split("-")[0];
        }

        int bgcolorAlpha = Math.round(Float.parseFloat(colorName.split("-")[1]) * 255);
        return bgcolorAlpha;
    }
}
