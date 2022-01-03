package com.fluidtouch.noteshelf.documentframework;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.FileUriUtils;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.models.theme.FTNCoverTheme;
import com.fluidtouch.noteshelf.models.theme.FTNPaperTheme;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.store.data.FTStorePackData;
import com.fluidtouch.noteshelf.store.model.FTStoreCategories;
import com.fluidtouch.noteshelf.store.model.FTStoreCategoryItem;
import com.fluidtouch.noteshelf2.R;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

public class FTCoverPaperThemeProvider {

    private static final FTCoverPaperThemeProvider coverPaperThemeProvider = new FTCoverPaperThemeProvider();

    private final LinkedHashMap<FTNTheme, List<FTNTheme>> themesByCategory = new LinkedHashMap<>();

    public static FTCoverPaperThemeProvider getInstance() {
        return coverPaperThemeProvider;
    }

    public HashMap<FTNTheme, List<FTNTheme>> getThemesByCategory(Context context, FTNThemeCategory.FTThemeType themeType) {
        if (themesByCategory.isEmpty()) {
            HashMap<String, FTNTheme> allThemes = new HashMap<>();

            //Default themes
            try {
                String categoryFolderName = themeType == FTNThemeCategory.FTThemeType.PAPER ? "stockPapers" : "stockCovers";
                AssetManager assetManager = context.getAssets();

                for (String eachThemeDirName : assetManager.list(categoryFolderName)) {
                    FTNTheme theme = FTNTheme.theme(new FTUrl(categoryFolderName + "/" + eachThemeDirName));
                    theme.packName = eachThemeDirName;
                    allThemes.put(theme.packName, theme);
                }
            } catch (IOException e) {
                FTLog.debug(FTCoverPaperThemeProvider.class.getName(), e.getMessage());
            }

            //Downloaded themes
            //String downloadedThemesFolderPath = themeType == FTNThemeCategory.FTThemeType.PAPER ? FTConstants.DOWNLOADED_PAPERS_PATH : FTConstants.DOWNLOADED_COVERS_PATH;
            String downloadedThemesFolderPath = themeType == FTNThemeCategory.FTThemeType.PAPER ? FTConstants.DOWNLOADED_PAPERS_PATH2 : FTConstants.DOWNLOADED_COVERS_PATH;
            File downloadedThemesDir = new File(downloadedThemesFolderPath);
            if (downloadedThemesDir.exists() && downloadedThemesDir.isDirectory()) {
                for (File eachThemeDir : downloadedThemesDir.listFiles()) {
                    if (eachThemeDir.exists() && eachThemeDir.isDirectory()) {
                        FTNTheme theme = FTNTheme.theme(new FTUrl(eachThemeDir.getAbsolutePath()));
                        theme.packName = eachThemeDir.getName();
                        theme.isDownloadTheme = true;
                        allThemes.put(theme.packName, theme);
                    }
                }
            }


            //Theme filtering
            FTStorePackData storePackData = new FTStorePackData(context);
            HashMap<String, Object> mStorePackData = storePackData.getStorePackData();
            String categoryType = themeType == FTNThemeCategory.FTThemeType.PAPER ? "papers" : "covers";
            mStorePackData = (HashMap<String, Object>) mStorePackData.get(categoryType);
            Gson gson = new Gson();
            String storeData = gson.toJson(mStorePackData);
            FTStoreCategories storeCategories = gson.fromJson(storeData, FTStoreCategories.class);
            for (FTStoreCategoryItem storeCategoryItem : storeCategories.getCategories()) {
                String storeCategoryName = storeCategoryItem.getCategory_name();
                List<String> storeThemes = storeCategoryItem.getThemes();

                FTNTheme themeCategory = new FTNTheme();
                themeCategory.setCategoryName(storeCategoryName);

                List<FTNTheme> filteredThemes = new ArrayList<>();
                for (int j = 0; j < storeThemes.size(); j++) {
                    FTNTheme theme = allThemes.get(storeThemes.get(j));
                    if (theme != null) {
//                    theme.setCategoryName(storeCategoryName);
                        filteredThemes.add(theme);

                        themeCategory.isDownloadTheme = theme.isDownloadTheme;

                        allThemes.remove(storeThemes.get(j));
                    }
                }
                if (!filteredThemes.isEmpty()) {
                    customSort(filteredThemes);
                    themesByCategory.put(themeCategory, filteredThemes);
                }
            }

        /*Commented out by Sreenu. Rather doing this we need to upgrade the plist.
        
        List<String> categories = new ArrayList<>();
        for (FTNTheme theme : allThemes.values()) {
            FTNTheme themeCategory = new FTNTheme();
            if (!TextUtils.isEmpty(theme.getCategoryName()))
                themeCategory.setCategoryName(theme.getCategoryName());
            themeCategory.isDownloadTheme = theme.isDownloadTheme;
            if (theme.isDownloadTheme && !categories.contains(theme.categoryName)) {
                List<FTNTheme> themes = new ArrayList<>();
                for (FTNTheme eachTheme : allThemes.values()) {
                    if (!TextUtils.isEmpty(eachTheme.categoryName) && eachTheme.categoryName.equals(theme.categoryName) && eachTheme.isDownloadTheme) {
                        themes.add(eachTheme);
                    }
                }
                customSort(themes);
                themesByCategory.put(themeCategory, themes);
                categories.add(theme.categoryName);
            }
        }*/

            //Custom themes
            FTNTheme themeCategory = new FTNTheme();
            themeCategory.setCategoryName(context.getString(R.string.custom));

            List<FTNTheme> customThemes = new ArrayList<>();
            //String customThemesDirPath = themeType == FTNThemeCategory.FTThemeType.PAPER ? FTConstants.DOWNLOADED_PAPERS_PATH : FTConstants.DOWNLOADED_COVERS_PATH;
            String customThemesDirPath = themeType == FTNThemeCategory.FTThemeType.PAPER ? FTConstants.DOWNLOADED_PAPERS_PATH2 : FTConstants.DOWNLOADED_COVERS_PATH;
            FTNTheme custom = new FTNTheme();
            custom.themeName = "addCustomTheme";
            custom.ftThemeType = themeType;
            custom.setCategoryName(context.getString(R.string.custom));
            customThemes.add(custom);
            File customThemesDir = new File(customThemesDirPath.replace("download", "custom"));
            if (customThemesDir.exists() && customThemesDir.isDirectory()) {
                for (File eachThemeDir : customThemesDir.listFiles()) {
                    if (eachThemeDir.exists() && eachThemeDir.isDirectory()) {
                        FTNTheme theme = FTNTheme.theme(new FTUrl(eachThemeDir.getAbsolutePath()));
                        theme.themeName = FileUriUtils.getName((eachThemeDir.getName().replace(".nsc", "")).replace(".nsp", ""));
                        theme.setCategoryName(context.getString(R.string.custom));
                        customThemes.add(theme);
                        themeCategory.isCustomTheme = theme.isCustomTheme;
                    }
                }
            }
            customSort(customThemes);
            themesByCategory.put(themeCategory, customThemes);

            Iterator<FTNTheme> iterator = themesByCategory.keySet().iterator();
            while (iterator.hasNext()) {
                if (TextUtils.isEmpty(iterator.next().categoryName)) {
                    iterator.remove();
                }
            }
        }
        return themesByCategory;
    }

    public FTNTheme getRandomTheme(Context context, FTNThemeCategory.FTThemeType themeType) {
        List<List<FTNTheme>> allThemes = new ArrayList<>(getThemesByCategory(context, themeType).values());
        Random random = new Random();
        int randomIndex = random.nextInt(allThemes.size());
        List<FTNTheme> randomThemes = allThemes.get(randomIndex);
        Random random1 = new Random();
        randomIndex = random1.nextInt(randomThemes.size());

        FTNTheme randomTheme = randomThemes.get(randomIndex);

        if (themeType == FTNThemeCategory.FTThemeType.COVER) {
            String coverPackName = FTApp.getPref().get(SystemPref.RECENT_COVER_THEME_NAME, FTConstants.DEFAULT_COVER_THEME_NAME);
            if (coverPackName.equals(randomTheme.packName) || !(randomTheme instanceof FTNCoverTheme))
                randomTheme = getRandomTheme(context, themeType);
        } else {
            String paperPackName = FTApp.getPref().get(SystemPref.RECENT_PAPER_THEME_NAME, FTConstants.DEFAULT_PAPER_THEME_NAME);
            if (paperPackName.equals(randomTheme.packName))
                randomTheme = getRandomTheme(context, themeType);
        }
        return randomTheme;
    }

    private void customSort(List<FTNTheme> themes) {
        themes.sort(new Comparator<FTNTheme>() {
            public int compare(FTNTheme o1, FTNTheme o2) {
                return (int) (extractInt(o1.themeName) - extractInt(o2.themeName));
            }

            long extractInt(String s) {
                String num = s.replaceAll("\\D", "");
                return num.isEmpty() ? 0 : Long.parseLong(num);
            }
        });
    }
}