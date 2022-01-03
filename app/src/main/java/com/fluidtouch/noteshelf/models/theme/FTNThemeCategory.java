package com.fluidtouch.noteshelf.models.theme;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListParser;
import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.utils.AssetsUtil;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.commons.utils.StringUtil;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf2.BuildConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class FTNThemeCategory {
    String categoryName;
    ArrayList<FTNTheme> themes = new ArrayList<>();
    private static Context mContext;

    public static String getThemesPlist() {
        String defaultPlistName = "themes_v8_en.plist";
        String lang = Locale.getDefault().getLanguage();
        if (Locale.getDefault().toLanguageTag().contains("zh-Hans")) {
            lang = "zh-Hans";
        } else if (Locale.getDefault().toLanguageTag().contains("zh-Hant")) {
            lang = "zh-Hant";
        }
        String plistName = "themes_v8_" + lang + ".plist";
        return AssetsUtil.isAssetFileExits(FTApp.getInstance().getApplicationContext(), plistName) ? plistName : defaultPlistName;
    }

    /*public static FTUrl getUrl(String fileName) {
        String path = "";
        String extension = fileName.split("\\.")[1];//StringUtil.getFileExtension(fileName);
        if (extension.equals("nsc")) {
            if (AssetsUtil.isAssetExists("stockCovers/" + fileName)) {
                path = "stockCovers/" + fileName;
            } else if (new File(FTConstants.DOWNLOADED_COVERS_PATH + fileName).exists()) {
                path = FTConstants.DOWNLOADED_COVERS_PATH + fileName;
            } else if (new File(FTConstants.CUSTOM_COVERS_PATH + fileName).exists()) {
                path = FTConstants.CUSTOM_COVERS_PATH + fileName;
            } else {
                return new FTUrl("");
            }
        } else if (extension.equals("nsp")) {
            if (AssetsUtil.isAssetExists("stockPapers/" + fileName) && (new File(FTConstants.DOWNLOADED_PAPERS_PATH2 + fileName).exists())) {
                path = FTConstants.DOWNLOADED_PAPERS_PATH2 + fileName;
                Log.d("TemplatePicker==>"," isAssetExists and DOWNLOADED_PAPERS_PATH2 Path::-"+path);
            } else if (AssetsUtil.isAssetExists("stockPapers/" + fileName)) {
                path = "stockPapers/" + fileName;
            } else if (new File(FTConstants.DOWNLOADED_PAPERS_PATH2 + fileName).exists()) {
                path = FTConstants.DOWNLOADED_PAPERS_PATH2 + fileName;
            } else if (new File(FTConstants.CUSTOM_PAPERS_PATH + fileName).exists()) {
                path = FTConstants.CUSTOM_PAPERS_PATH + fileName;
            } else {
                return new FTUrl("");
            }
        }

        return new FTUrl(path);
    }*/

    public static FTUrl getUrl(String fileName) {
        String path = "";
        String extension = fileName.split("\\.")[1];//StringUtil.getFileExtension(fileName);
        if (extension.equals("nsc")) {
            if (AssetsUtil.isAssetExists("stockCovers/" + fileName)) {
                path = "stockCovers/" + fileName;
            } else if (new File(FTConstants.DOWNLOADED_COVERS_PATH + fileName).exists()) {
                path = FTConstants.DOWNLOADED_COVERS_PATH + fileName;
            } else if (new File(FTConstants.CUSTOM_COVERS_PATH + fileName).exists()) {
                path = FTConstants.CUSTOM_COVERS_PATH + fileName;
            } else {
                return new FTUrl("");
            }
        } else if (extension.equals("nsp")) {
            if (AssetsUtil.isAssetExists("stockPapers/" + fileName)) {
                path = "stockPapers/" + fileName;
            } else if (new File(FTConstants.DOWNLOADED_PAPERS_PATH2 + fileName).exists()) {
                path = FTConstants.DOWNLOADED_PAPERS_PATH2 + fileName;
            } else if (new File(FTConstants.CUSTOM_PAPERS_PATH + fileName).exists()) {
                path = FTConstants.CUSTOM_PAPERS_PATH + fileName;
            } else {
                return new FTUrl("");
            }

            Log.d("TemplatePickerV2"," path:: "+path);
        }

        return new FTUrl(path);
    }

    public FTNThemeCategory(Context context, String name, FTThemeType type) {
        super();
        this.categoryName = name;
        this.mContext = context;
        this.copyMetadataIfNeeded();

        File plist = new File(FTConstants.DOCUMENTS_ROOT_PATH + "/Library/" + getThemesPlist());
        try {
            FileInputStream inputStream = new FileInputStream(plist);
            NSDictionary dictionary = (NSDictionary) PropertyListParser.parse(inputStream);
            NSDictionary packsDict = (NSDictionary) (dictionary.objectForKey(type == FTThemeType.COVER ? "covers" : "papers"));
            NSArray categories = (NSArray) packsDict.objectForKey("categories");
            for (int c = 0; c < categories.count(); c++) {
                NSDictionary category = (NSDictionary) categories.objectAtIndex(c);
                NSArray themesArray = (NSArray) category.objectForKey("themes");
                for (int i = 0; i < themesArray.count(); i++) {
                    String themeName = themesArray.objectAtIndex(i).toString();
                    FTUrl url = getUrl(themeName);
                    if (!url.getPath().equals("")) {
                        if (type == FTThemeType.COVER) {
                            FTNCoverTheme theme = (FTNCoverTheme) FTNTheme.theme(url);
                            this.themes.add(theme);
                        } else if (type == FTThemeType.PAPER) {
                            FTNTheme theme = FTNTheme.theme(url);
                            this.themes.add(theme);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Boolean isCustom() {
        return (categoryName.equals("Custom"));
    }

    Boolean isRecents() {
        return (categoryName.equals("Recents"));
    }

    public ArrayList<FTNTheme> getPortraitThemes() {
        //ToDo: filter portrait themes
        ArrayList<FTNTheme> filteredThemes = new ArrayList<>();
        for (int i = 0; i < this.themes.size(); i++) {
            if (!this.themes.get(i).isLandscape()) {
                filteredThemes.add(this.themes.get(i));
            }
        }
        return filteredThemes;
    }

    public ArrayList<FTNTheme> getLandscapeThemes() {
        //ToDo: filter landscape themes
        ArrayList<FTNTheme> filteredThemes = new ArrayList<>();
        for (int i = 0; i < this.themes.size(); i++) {
            if (this.themes.get(i).isLandscape()) {
                filteredThemes.add(this.themes.get(i));
            }
        }
        return filteredThemes;
    }

    public ArrayList<FTNTheme> getPortraitTransparentThemes() {
        //ToDo: filter portrait themes
        ArrayList<FTNTheme> filteredThemes = new ArrayList<>();
        for (int i = 0; i < this.themes.size(); i++) {
            FTNTheme theme = themes.get(i);
            if (!theme.isLandscape() && theme.packName.contains("Transparent")) {
                filteredThemes.add(this.themes.get(i));
            }
        }
        return filteredThemes;
    }

    private void copyMetadataIfNeeded() {
        File plistFile = new File(FTConstants.DOCUMENTS_ROOT_PATH + "/Library/" + getThemesPlist());
        if (plistFile.exists() && BuildConfig.VERSION_CODE == FTApp.getPref().get("themes_v5_appVersion", 0)) {
            return;
        }
        plistFile.getParentFile().mkdirs();

        AssetManager assetmanager = getContext().getAssets();
        try {
            InputStream bundleInputStrem = assetmanager.open("" + getThemesPlist());
            this.createFileFromInputStream(bundleInputStrem);
            FTApp.getPref().save("themes_v5_appVersion", BuildConfig.VERSION_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File createFileFromInputStream(InputStream inputStream) {

        try {
            File f = new File(FTConstants.DOCUMENTS_ROOT_PATH + "/Library/" + getThemesPlist());
            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length = 0;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return f;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public FTNCoverTheme getRandomCoverTheme() {
        ArrayList<FTNTheme> portraitThemes = this.getPortraitTransparentThemes();
        final int random = new Random().nextInt(portraitThemes.size() - 1);
        return (FTNCoverTheme) portraitThemes.get(random);
    }

    public FTNCoverTheme getCoverThemeForPackName(String packName) {
        FTNCoverTheme coverTheme = null;

        for (FTNTheme theme : this.themes) {
            if (theme.packName.equals(packName)) {
                coverTheme = (FTNCoverTheme) theme;
                break;
            }
        }

        return coverTheme;
    }

    public FTNPaperTheme getPaperThemeForPackName(String packName) {
        FTNPaperTheme paperTheme = null;

        for (FTNTheme theme : this.themes) {
            if (theme.packName.equals(packName)) {
                paperTheme = (FTNPaperTheme) theme;
                break;
            }
        }
        return paperTheme;
    }

    private Context getContext() {
        return mContext;
    }

    public enum FTThemeType implements Serializable {
        COVER, PAPER
    }
}