package com.fluidtouch.noteshelf.document.textedit;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListParser;
import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.AssetsUtil;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.document.textedit.texttoolbar.FTFontFamily;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FTFontProvider {

    private static final FTFontProvider fontProvider = new FTFontProvider();

    public static FTFontProvider getInstance() {
        return fontProvider;
    }

    public List<FTFontFamily> getAllFonts() {
        List<FTFontFamily> defaultFonts = getDefaultFonts();
//        defaultFonts.addAll(defaultFonts.size(), getSystemFonts());
        return defaultFonts;
        //return getSystemFonts();
    }

    public List<FTFontFamily> getDefaultFonts() {
        List<FTFontFamily> defaultFonts = new ArrayList<>();
        try {
            NSDictionary nsDictionary = (NSDictionary) PropertyListParser.parse(AssetsUtil.getInputStream("fonts.plist"));
            for (Map.Entry<String, NSObject> entry : nsDictionary.entrySet()) {
                NSArray nsArray = (NSArray) entry.getValue();
                FTFontFamily fontFamily = new FTFontFamily();
                fontFamily.setFontName(entry.getKey());
                fontFamily.isDefault = true;

                List<String> fontStyles = new ArrayList<>();
                for (NSObject nsObject : nsArray.getArray()) {
                    String subFontName = nsObject.toJavaObject().toString();
                    String upperCaseString = subFontName.substring(0, 1).toUpperCase() + subFontName.substring(1);
                    if (FTFontFamily.getStyleForString(upperCaseString) != -1) {
                        fontStyles.add(subFontName);
                    }
                }
                fontFamily.setFontStyles(fontStyles);

                defaultFonts.add(fontFamily);
            }
        } catch (Exception e) {
            FTLog.error("font_provider", e.getMessage());
        }

        defaultFonts.sort((o1, o2) -> o1.getFontName().compareTo(o2.getFontName()));

        return defaultFonts;
    }


    private List<FTFontFamily> getSystemFonts() {
        List<FTFontFamily> systemFonts = new ArrayList<>();
        File[] fontFiles = new File(FTConstants.SYSTEM_FONTS_PATH).listFiles();
        for (File fontFile : fontFiles) {
            String fontName = FTDocumentUtils.getFileNameWithoutExtension(FTApp.getInstance().getApplicationContext(), FTUrl.parse(fontFile.getName()));
            if (fontName.contains("-")) {
                fontName = fontFile.getName().split("-")[0];
            }

            String finalFontName = fontName;
            if ((!systemFonts.isEmpty() && systemFonts.stream().anyMatch(fontFamily -> fontFamily.getFontName().equals(finalFontName))) || !fontFile.getName().contains(".ttf"))
                continue;

            FTFontFamily fontFamily = new FTFontFamily();
            fontFamily.setFontName(fontName);

            List<String> fontStyles = new ArrayList<>();
            for (File subFontFile : fontFiles) {
                if (subFontFile.getName().contains(fontName) && subFontFile.getName().contains("-")) {
                    String[] strings = subFontFile.getName().split("-");
                    if (strings.length > 1) {
                        String fontStyle = FTDocumentUtils.getFileNameWithoutExtension(FTApp.getInstance().getApplicationContext(), FTUrl.parse(strings[strings.length - 1]));
                        if (!fontStyles.contains(fontStyle) && FTFontFamily.getStyleForString(fontStyle) != -1)
                            fontStyles.add(fontStyle);
                    }
                }
            }
            fontFamily.setFontStyles(fontStyles);

            systemFonts.add(fontFamily);
        }

        //Remove invalid fonts
        boolean isValidFont = false;
        Iterator<FTFontFamily> iterator = systemFonts.iterator();
        while (iterator.hasNext()) {
            FTFontFamily fontFamily = iterator.next();
            if (fontFamily.getFontStyles().isEmpty()) {
                isValidFont = FTFileManagerUtil.isFileExits(FTConstants.SYSTEM_FONTS_PATH + fontFamily.getFontName() + ".ttf");
            } else {
                for (String fontStyle : fontFamily.getFontStyles()) {
                    isValidFont = FTFileManagerUtil.isFileExits(FTConstants.SYSTEM_FONTS_PATH + fontFamily.getFontName() + "-" + fontStyle + ".ttf");
                }
            }
            if (!isValidFont) iterator.remove();
        }

        systemFonts.sort((o1, o2) -> o1.getFontName().compareTo(o2.getFontName()));

        return systemFonts;
    }
}