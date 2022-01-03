package com.fluidtouch.noteshelf.document.textedit.texttoolbar;

import android.graphics.Typeface;

import com.fluidtouch.noteshelf.document.textedit.FTFontProvider;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;

import java.util.ArrayList;
import java.util.List;

public class FTFontFamily {
    private String fontName = "";
    private List<String> fontStyles = new ArrayList<>();
    public boolean isDefault;

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public List<String> getFontStyles() {
        return fontStyles;
    }

    public void setFontStyles(List<String> fontStyles) {
        this.fontStyles = fontStyles;
    }

    public String getFontPathForStyle(String fontStyle) {
        if (isDefault) {
            if (!getFontStyles().isEmpty())
                return "fonts/" + fontName + "_" + fontStyle.toLowerCase() + ".ttf";
            else
                return "fonts/" + fontName + ".ttf";
        } else {
            if (!getFontStyles().isEmpty())
                return FTConstants.SYSTEM_FONTS_PATH + fontName + "-" + fontStyle + ".ttf";
            else
                return FTConstants.SYSTEM_FONTS_PATH + fontName + ".ttf";
        }
    }

    public int getDefaultStyle() {
        return !getFontStyles().isEmpty() ? getStyleForString(getFontStyles().get(0)) : -1;
    }

    public static int getStyleForString(String fontStyle) {
        fontStyle = fontStyle.toLowerCase();
        switch (fontStyle) {
            case "bolditalic":
                return Typeface.BOLD_ITALIC;
            case "bold":
                return Typeface.BOLD;
            case "italic":
                return Typeface.ITALIC;
            case "regular":
                return Typeface.NORMAL;
            default:
                return -1;
        }
    }

    public static String getStyleForInt(int fontStyle) {
        if (fontStyle == Typeface.BOLD_ITALIC) {
            return "BoldItalic";
        } else if (fontStyle == Typeface.BOLD) {
            return "Bold";
        } else if (fontStyle == Typeface.ITALIC) {
            return "Italic";
        } else if (fontStyle == Typeface.NORMAL) {
            return "Regular";
        }
        return "";
    }

    public static FTFontFamily getFontFamilyForName(String fontName) {
        FTFontFamily fontFamily = null;
        List<FTFontFamily> fontFamilies = FTFontProvider.getInstance().getAllFonts();
        for (FTFontFamily eachFontFamily : fontFamilies) {
            if (eachFontFamily.getFontName().equals(fontName)) {
                fontFamily = eachFontFamily;
                break;
            }
        }
        return fontFamily;
    }
}