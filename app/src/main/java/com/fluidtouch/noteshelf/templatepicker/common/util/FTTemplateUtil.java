package com.fluidtouch.noteshelf.templatepicker.common.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.utils.StringUtil;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.models.theme.FTNCoverTheme;
import com.fluidtouch.noteshelf.models.theme.FTNDynamicTemplateTheme;
import com.fluidtouch.noteshelf.models.theme.FTNPaperTheme;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.templatepicker.adapters.FTTemplateDetailedInfoAdapter;
import com.fluidtouch.noteshelf.templatepicker.common.FTTemplatesInfoSingleton;
import com.fluidtouch.noteshelf.templatepicker.common.modelclasses.FTUserSelectedTemplateInfo;
import com.fluidtouch.noteshelf.templatepicker.common.modelclasses.TemplateModelClass;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTCategories;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTLineTypes;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTTemplateColors;
import com.fluidtouch.noteshelf.templatepicker.models.RecentsInfoModel;
import com.fluidtouch.noteshelf.templatepicker.models.TemplatesInfoModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class FTTemplateUtil {

    private static FTTemplateUtil instance;
    FTTemplateColors ftTemplateColors;
    FTTemplateColors ftTemplateMoreColors;
    FTLineTypes ftLineTypes;
    FTSelectedDeviceInfo ftSelectedDeviceInfo = null;

    public ArrayList<FTCategories> getTemplatesInfoList() {
        return templatesInfoList;
    }

    public void setTemplatesInfoList(ArrayList<FTCategories> templatesInfoList) {
        this.templatesInfoList = templatesInfoList;
    }

    ArrayList<FTCategories> templatesInfoList;

    public FTUserSelectedTemplateInfo ftUserSelectedTemplateInfo = FTUserSelectedTemplateInfo.getInstance();;

    public static synchronized FTTemplateUtil getInstance() {
        if (instance == null) {
            instance = new FTTemplateUtil();
        }
        return instance;
    }

    public FTSelectedDeviceInfo getFtSelectedDeviceInfo() {
        return ftSelectedDeviceInfo;
    }

    public void fTTemplateColorsSerializedObject(String clrHexCode,String ClrName,
                                                 String verticalLineCle,String hrzntlLineClr) {

        ftTemplateColors = new FTTemplateColors();
        ftTemplateColors.setColorHex(clrHexCode);
        ftTemplateColors.setColorName(ClrName);
        ftTemplateColors.setVerticalLineColor(verticalLineCle);
        ftTemplateColors.setHorizontalLineColor(hrzntlLineClr);

        try {
            final Gson gson = new Gson();
            String serializedObject = gson.toJson(ftTemplateColors);
            ftUserSelectedTemplateInfo.setFtTemplateColors(ftTemplateColors);
            ftUserSelectedTemplateInfo.setFtLineTypes(getFtTemplateLineInfoObj());
            FTApp.getPref().save(SystemPref.TEMPLATE_BG_CLR, serializedObject);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void fTTemplateLineTypeSerializedObject(int verticalLineSpacing,String lineType,int horizontalSpacing) {
        ftLineTypes = new FTLineTypes();
        ftLineTypes.setVerticalLineSpacing(verticalLineSpacing);
        ftLineTypes.setLineType(lineType);
        ftLineTypes.setHorizontalLineSpacing(horizontalSpacing);

        final Gson gson = new Gson();
        try {
            String serializedObject = gson.toJson(ftLineTypes);
            FTApp.getPref().save(SystemPref.TEMPLATE_LINE_TYPE, serializedObject);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String selectedClrView = FTApp.getPref().get(SystemPref.TYPE_OF_CLR_VIEW_SELECTED,"");
        if (!selectedClrView.isEmpty()) {
            if (selectedClrView.toLowerCase().equalsIgnoreCase("view1") ||
                    selectedClrView.toLowerCase().equalsIgnoreCase("view2")) {
                ftUserSelectedTemplateInfo.setFtTemplateColors(getFtTemplateColorsObj());
            } else {
                if (getFtTemplateMoreColorsObj()!= null) {
                    ftUserSelectedTemplateInfo.setFtTemplateColors(getFtTemplateMoreColorsObj());
                } else {
                    ftUserSelectedTemplateInfo.setFtTemplateColors(getFtTemplateColorsObj());
                }

            }
        }
        ftUserSelectedTemplateInfo.setFtLineTypes(ftLineTypes);

    }

    public void fTTemplateMoreColorsSerializedObject(String clrHexCode,String ClrName,String verticalLineCle,String hrzntlLineClr) {
        ftTemplateMoreColors = new FTTemplateColors();
        ftTemplateMoreColors.setColorHex(clrHexCode);
        ftTemplateMoreColors.setColorName(ClrName);
        ftTemplateMoreColors.setVerticalLineColor(verticalLineCle);
        ftTemplateMoreColors.setHorizontalLineColor(hrzntlLineClr);

        try {
            final Gson gson = new Gson();
            String serializedObject = gson.toJson(ftTemplateMoreColors);
            FTApp.getPref().save(SystemPref.TEMPLATE_BG_CLR, serializedObject);
            FTApp.getPref().save(SystemPref.TEMPLATE_BG_MORE_CLR, serializedObject);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ftUserSelectedTemplateInfo.setFtTemplateColors(ftTemplateMoreColors);
        ftUserSelectedTemplateInfo.setFtLineTypes(getFtTemplateLineInfoObj());

    }

    public FTTemplateColors getFtTemplateColorsObj() {
        Gson gson = new Gson();
        return gson.fromJson(FTApp.getPref().get(SystemPref.TEMPLATE_BG_CLR, ""), FTTemplateColors.class);
    }

    public FTTemplateColors getFtTemplateMoreColorsObj() {
        Gson gson = new Gson();
        return gson.fromJson(FTApp.getPref().get(SystemPref.TEMPLATE_BG_MORE_CLR, ""), FTTemplateColors.class);
    }

    public FTLineTypes getFtTemplateLineInfoObj() {
        Gson gson = new Gson();
        return gson.fromJson(FTApp.getPref().get(SystemPref.TEMPLATE_LINE_TYPE, ""), FTLineTypes.class);
    }

    /*public void saveRecentPaperTheme(FTNTheme paperTheme,String origin) {

        Gson gsonPaperTheme = new Gson();
        try {
            String json = gsonPaperTheme.toJson(paperTheme);
            FTApp.getPref().save(SystemPref.RECENT_PAPER_THEME, json);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("TemplatePicker==>","FTTemplateUtil saveRecentPaperTheme packName::-"+
                paperTheme.packName+" paperTheme::-"+paperTheme.thumbnailURLPath+" origin::-"+origin);
    }*/

    public FTNTheme getRecentPaperTheme() {

        FTNTheme ftRecentPaperTheme = null;
        Gson recentGson = new Gson();
        String recentJson = FTApp.getPref().get(SystemPref.RECENT_PAPER_THEME, "defaultValue");

        if (!recentJson.contains("defaultValue")) {
            Type type = new TypeToken<FTNTheme>() {}.getType();
            ftRecentPaperTheme = recentGson.fromJson(recentJson, type);
        } else {
            ftRecentPaperTheme = new FTNThemeCategory(FTApp.getInstance().getApplicationContext(),
                    "Simple", FTNThemeCategory.FTThemeType.PAPER).getPaperThemeForPackName(FTConstants.DEFAULT_PAPER_THEME_NAME);
            ftRecentPaperTheme.categoryName     = "Basic";
            ftRecentPaperTheme.isDefaultTheme   = true;
            ftRecentPaperTheme.isBasicTheme     = true;
            ftRecentPaperTheme.thumbnailURLPath = FTConstants.TEMP_FOLDER_PATH+"TemplatesCache/thumbnail_Plain_595_842_ivory_default_portrait.jpg";
        }

        Log.d("TemplatePicker==>","FTTemplateUtil getRecentPaperTheme ftRecentPaperTheme::-"+
                ftRecentPaperTheme.packName);

        return ftRecentPaperTheme;
    }

    public static boolean isNullOrEmpty( final Collection< ? > c ) {
        return c == null || c.isEmpty();
    }

    public void saveRecentPapersDummy(FTNTheme paperTheme) {

        ArrayList<RecentsInfoModel> ftRecentThemeArrayList = getRecentPapersDummy();
        if (ftRecentThemeArrayList == null) {
            ftRecentThemeArrayList = new ArrayList<>();
        }

        RecentsInfoModel recentsInfoModel = new RecentsInfoModel();
        paperTheme.categoryName = "Recent";
        recentsInfoModel.set_categoryName("Recent");
        recentsInfoModel.set_packName(paperTheme.packName);
        recentsInfoModel.setThemeBgClr(paperTheme.themeBgClr);
        recentsInfoModel.setThemeBgClrName(paperTheme.themeBgClrName);
        recentsInfoModel.setHorizontalLineColor(paperTheme.horizontalLineColor);
        recentsInfoModel.setVerticalLineColor(paperTheme.verticalLineColor);
        recentsInfoModel.setVerticalSpacing(paperTheme.verticalSpacing);
        recentsInfoModel.setHorizontalSpacing(paperTheme.horizontalSpacing);
        recentsInfoModel.setWidth(paperTheme.width);
        recentsInfoModel.setHeight(paperTheme.height);
        recentsInfoModel.set_themeName(paperTheme.themeName);
        recentsInfoModel.setLandscape(paperTheme.isLandscape);
        recentsInfoModel.set_thumbnailURLPath(paperTheme.thumbnailURLPath);
        recentsInfoModel.set_themeBitmapInStringFrmt(BitMapToString(paperTheme.bitmap));

        ftRecentThemeArrayList.add(0,recentsInfoModel);

        Gson gson = new Gson();
        try {
            String jsonPrefs = gson.toJson(ftRecentThemeArrayList);
            FTApp.getPref().save(SystemPref.RECENTLY_SELECTED_PAPERS_LIST, jsonPrefs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("TemplatePickerV2:: "," themeFileURL.getPath():: "+paperTheme.themeFileURL.getPath());

        Log.d("TemplatePickerV2","checkRecentThemesDummy saveRecentPapersDummy get_packName:: "+recentsInfoModel.get_packName() +
                " get_categoryName:: "+recentsInfoModel.get_categoryName()+" get_thumbnailURLPath:: "+recentsInfoModel.get_thumbnailURLPath());

    }

    public void saveRecentCoversDummy(FTNTheme coverTheme) {

        ArrayList<RecentsInfoModel> ftRecentThemeArrayList = getRecentCoversDummy();
        if (ftRecentThemeArrayList == null) {
            ftRecentThemeArrayList = new ArrayList<>();
        }

        RecentsInfoModel recentsInfoModel = new RecentsInfoModel();
        coverTheme.categoryName = "Recent";
        recentsInfoModel.set_categoryName("Recent");
        recentsInfoModel.set_packName(coverTheme.packName);
        recentsInfoModel.setThemeBgClr(coverTheme.themeBgClr);
        recentsInfoModel.setThemeBgClrName(coverTheme.themeBgClrName);
        recentsInfoModel.setHorizontalLineColor(coverTheme.horizontalLineColor);
        recentsInfoModel.setVerticalLineColor(coverTheme.verticalLineColor);
        recentsInfoModel.setVerticalSpacing(coverTheme.verticalSpacing);
        recentsInfoModel.setHorizontalSpacing(coverTheme.horizontalSpacing);
        recentsInfoModel.setWidth(coverTheme.width);
        recentsInfoModel.setHeight(coverTheme.height);
        recentsInfoModel.set_themeName(coverTheme.themeName);
        recentsInfoModel.setLandscape(coverTheme.isLandscape);
        recentsInfoModel.set_thumbnailURLPath(coverTheme.thumbnailURLPath);
        recentsInfoModel.set_themeBitmapInStringFrmt(BitMapToString(coverTheme.bitmap));

        ftRecentThemeArrayList.add(recentsInfoModel);

        Gson gson = new Gson();
        try {
            String jsonPrefs = gson.toJson(ftRecentThemeArrayList);
            FTApp.getPref().save(SystemPref.RECENTLY_SELECTED_COVERS_LIST, jsonPrefs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("TemplatePickerV2:: "," themeFileURL.getPath():: "+coverTheme.themeFileURL.getPath());

        Log.d("TemplatePickerV2","checkRecentThemesDummy saveRecentCoversDummy get_packName:: "+recentsInfoModel.get_packName() +
                " get_categoryName:: "+recentsInfoModel.get_categoryName()+" get_thumbnailURLPath:: "+recentsInfoModel.get_thumbnailURLPath());

    }

    public void saveRecentCoverTheme(FTNTheme coverTheme) {

        RecentsInfoModel recentsInfoModel = getRecentCoverTheme();
        if (recentsInfoModel == null) {
            recentsInfoModel = new RecentsInfoModel();
        }

        coverTheme.categoryName = "Recent";
        recentsInfoModel.set_categoryName("Recent");
        recentsInfoModel.set_packName(coverTheme.packName);
        recentsInfoModel.setThemeBgClr(coverTheme.themeBgClr);
        recentsInfoModel.setThemeBgClrName(coverTheme.themeBgClrName);
        recentsInfoModel.setHorizontalLineColor(coverTheme.horizontalLineColor);
        recentsInfoModel.setVerticalLineColor(coverTheme.verticalLineColor);
        recentsInfoModel.setVerticalSpacing(coverTheme.verticalSpacing);
        recentsInfoModel.setHorizontalSpacing(coverTheme.horizontalSpacing);
        recentsInfoModel.setWidth(coverTheme.width);
        recentsInfoModel.setHeight(coverTheme.height);
        recentsInfoModel.set_themeName(coverTheme.themeName);
        recentsInfoModel.setLandscape(coverTheme.isLandscape());
        recentsInfoModel.set_thumbnailURLPath(coverTheme.thumbnailURLPath);
        recentsInfoModel.set_themeBitmapInStringFrmt(BitMapToString(coverTheme.bitmap));

        Gson gson = new Gson();
        try {
            String jsonPrefs = gson.toJson(recentsInfoModel);
            FTApp.getPref().save(SystemPref.RECENT_COVER_THEME, jsonPrefs);
            FTApp.getPref().save(SystemPref.RECENT_COVER_THEME_NAME, coverTheme.packName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("TemplatePickerV2","checkRecentThemesDummy saveRecentCoversDummy get_packName:: "+recentsInfoModel.get_packName() +
                " get_categoryName:: "+recentsInfoModel.get_categoryName()+" get_thumbnailURLPath:: "+recentsInfoModel.get_thumbnailURLPath());
    }

    public RecentsInfoModel getRecentCoverTheme() {

        RecentsInfoModel ftRecentThemeInfo = null;
        Gson recentGson = new Gson();
        String recentJson = FTApp.getPref().get(SystemPref.RECENT_COVER_THEME, "defaultValue");
        Log.d("TemplatePickerV2","checkRecentThemesDummy getRecentCoversDummy recentJson:: "+recentJson);

        if (!recentJson.equalsIgnoreCase("defaultValue")) {
            Type type = new TypeToken<RecentsInfoModel>() {}.getType();
            ftRecentThemeInfo = recentGson.fromJson(recentJson, type);
            Log.d("TemplatePickerV2","checkRecentThemesDummy getRecentCoverTheme Recents List Size:: ");

            return ftRecentThemeInfo;
        } else {
            Log.d("TemplatePickerV2","checkRecentThemesDummy getRecentCoverTheme Recents List is Empty:: ");
            return null;
        }
    }

    public ArrayList<RecentsInfoModel> getRecentCoversDummy() {
        ArrayList<RecentsInfoModel> ftRecentThemeArrayList = null;
        Gson recentGson = new Gson();
        String recentJson = FTApp.getPref().get(SystemPref.RECENTLY_SELECTED_COVERS_LIST, "defaultValue");
        Log.d("TemplatePickerV2","checkRecentThemesDummy getRecentCoversDummy recentJson:: "+recentJson);

        if (!recentJson.equalsIgnoreCase("defaultValue")) {
            Type type = new TypeToken<ArrayList<RecentsInfoModel>>() {}.getType();
            ftRecentThemeArrayList = recentGson.fromJson(recentJson, type);
            Log.d("TemplatePickerV2","checkRecentThemesDummy getRecentCoversDummy Recents List Size:: "+ftRecentThemeArrayList.size());
            return ftRecentThemeArrayList;
        } else {
            Log.d("TemplatePickerV2","checkRecentThemesDummy getRecentCoversDummy Recents List is Empty:: ");
            return null;
        }
    }

    public ArrayList<RecentsInfoModel> getRecentPapersDummy() {
        ArrayList<RecentsInfoModel> ftRecentThemeArrayList = null;
        Gson recentGson = new Gson();
        String recentJson = FTApp.getPref().get(SystemPref.RECENTLY_SELECTED_PAPERS_LIST, "defaultValue");
        Log.d("TemplatePickerV2","checkRecentThemesDummy getRecentPapersDummy recentJson:: "+recentJson);

        if (!recentJson.equalsIgnoreCase("defaultValue")) {
            Type type = new TypeToken<ArrayList<RecentsInfoModel>>() {}.getType();
            ftRecentThemeArrayList = recentGson.fromJson(recentJson, type);
            Log.d("TemplatePickerV2","checkRecentThemesDummy getRecentPapersDummy Recents List Size:: "+ftRecentThemeArrayList.size());

            return ftRecentThemeArrayList;
        } else {
            Log.d("TemplatePickerV2","checkRecentThemesDummy getRecentPapersDummy Recents List is Empty:: ");
            return null;
        }
    }

    public void saveRecentPaperThemeFromNewNotebookDialog(FTNTheme paperTheme) {

        if (paperTheme.ftThemeType == FTNThemeCategory.FTThemeType.PAPER) {
            RecentsInfoModel recentsInfoModel = new RecentsInfoModel();
            paperTheme.categoryName = "Recent";
            recentsInfoModel.set_categoryName("Recent");
            recentsInfoModel.set_packName(paperTheme.packName);
            recentsInfoModel.setThemeBgClr(paperTheme.themeBgClr);
            recentsInfoModel.setThemeBgClrName(paperTheme.themeBgClrName);
            recentsInfoModel.setHorizontalLineColor(paperTheme.horizontalLineColor);
            recentsInfoModel.setVerticalLineColor(paperTheme.verticalLineColor);
            recentsInfoModel.setVerticalSpacing(paperTheme.verticalSpacing);
            recentsInfoModel.setHorizontalSpacing(paperTheme.horizontalSpacing);
            recentsInfoModel.setWidth(paperTheme.width);
            recentsInfoModel.setHeight(paperTheme.height);
            recentsInfoModel.set_themeName(paperTheme.themeName);
            recentsInfoModel.setLandscape(paperTheme.isLandscape);
            recentsInfoModel.set_thumbnailURLPath(paperTheme.thumbnailURLPath);

            recentsInfoModel.set_themeBitmapInStringFrmt(BitMapToString(paperTheme.bitmap));

            Gson gson = new Gson();
            try {
                String jsonPrefs = gson.toJson(recentsInfoModel);
                FTApp.getPref().save(SystemPref.RECENTLY_SELECTED_PAPERTHEME_NOTEBOOK_OPTIONS, jsonPrefs);
                FTApp.getPref().save(SystemPref.RECENT_PAPER_THEME_NAME, paperTheme.packName);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.d("TemplatePickerV2","checkRecentThemesDummy saveRecentPaperThemeFromNewNotebookDialog" +
                    " get_packName:: "+recentsInfoModel.get_packName() +
                    " get_categoryName:: "+recentsInfoModel.get_categoryName()+
                    " get_thumbnailURLPath:: "+recentsInfoModel.get_thumbnailURLPath());
        }
    }

    public void saveRecentPaperThemeFromQuickCreateDialog(FTNTheme paperTheme) {

        if (paperTheme.ftThemeType == FTNThemeCategory.FTThemeType.PAPER) {
            RecentsInfoModel recentsInfoModel = new RecentsInfoModel();
            paperTheme.categoryName = "Recent";
            recentsInfoModel.set_categoryName("Recent");
            recentsInfoModel.set_packName(paperTheme.packName);
            recentsInfoModel.setThemeBgClr(paperTheme.themeBgClr);
            recentsInfoModel.setThemeBgClrName(paperTheme.themeBgClrName);
            recentsInfoModel.setHorizontalLineColor(paperTheme.horizontalLineColor);
            recentsInfoModel.setVerticalLineColor(paperTheme.verticalLineColor);
            recentsInfoModel.setVerticalSpacing(paperTheme.verticalSpacing);
            recentsInfoModel.setHorizontalSpacing(paperTheme.horizontalSpacing);
            recentsInfoModel.setWidth(paperTheme.width);
            recentsInfoModel.setHeight(paperTheme.height);
            recentsInfoModel.set_themeName(paperTheme.themeName);
            recentsInfoModel.setLandscape(paperTheme.isLandscape);
            recentsInfoModel.set_thumbnailURLPath(paperTheme.thumbnailURLPath);

            recentsInfoModel.set_themeBitmapInStringFrmt(BitMapToString(paperTheme.bitmap));

            Gson gson = new Gson();
            try {
                String jsonPrefs = gson.toJson(recentsInfoModel);
                FTApp.getPref().save(SystemPref.RECENTLY_SELECTED_PAPERTHEME_QUICKCREATE_OPTIONS, jsonPrefs);
                FTApp.getPref().save(SystemPref.QUICK_CREATE_PAPER_THEME_NAME, paperTheme.packName);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.d("TemplatePickerV2","checkRecentThemesDummy saveRecentPaperThemeFromQuickCreateDialog" +
                    " get_packName:: "+recentsInfoModel.get_packName() +
                    " get_categoryName:: "+recentsInfoModel.get_categoryName()+
                    " get_thumbnailURLPath:: "+recentsInfoModel.get_thumbnailURLPath());
        }
    }

    public String BitMapToString(Bitmap bitmap) {
        if (bitmap==null) {
            return "";
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0,
                    encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    public RecentsInfoModel getRecentPaperThemeFromNewNotebookDialog() {

        RecentsInfoModel ftRecentThemeFromNewNotebookDialog = null;
        Gson recentGson = new Gson();
        String recentJson = FTApp.getPref().get(SystemPref.RECENTLY_SELECTED_PAPERTHEME_NOTEBOOK_OPTIONS, "defaultValue");
        Log.d("TemplatePickerV2","checkRecentThemesDummy getRecentPaperThemeFromNewNotebookDialog recentJson:: "+recentJson);

        if (!recentJson.equalsIgnoreCase("defaultValue")) {
            Type type = new TypeToken<RecentsInfoModel>() {}.getType();
            ftRecentThemeFromNewNotebookDialog = recentGson.fromJson(recentJson, type);

            Log.d("TemplatePicker==>","FTTemplateUtil getRecentPaperThemeFromNewNotebookDialog get_packName " +ftRecentThemeFromNewNotebookDialog.get_packName()
                    +"get_thumbnailURLPath:: "+ftRecentThemeFromNewNotebookDialog.get_thumbnailURLPath());
            return ftRecentThemeFromNewNotebookDialog;
        } else {
            Log.d("TemplatePickerV2","checkRecentThemesDummy getRecentPaperThemeFromNewNotebookDialog Recents List is Empty:: ");
            return null;
        }

    }

    public RecentsInfoModel getRecentPaperThemeFromQuickCreateDialog() {

        RecentsInfoModel ftRecentThemeFromNewNotebookDialog = null;
        Gson recentGson = new Gson();
        String recentJson = FTApp.getPref().get(SystemPref.RECENTLY_SELECTED_PAPERTHEME_QUICKCREATE_OPTIONS, "defaultValue");
        Log.d("TemplatePickerV2","checkRecentThemesDummy getRecentPaperThemeFromQuickCreateDialog recentJson:: "+recentJson);

        if (!recentJson.equalsIgnoreCase("defaultValue")) {
            Type type = new TypeToken<RecentsInfoModel>() {}.getType();
            ftRecentThemeFromNewNotebookDialog = recentGson.fromJson(recentJson, type);

            Log.d("TemplatePicker==>","FTTemplateUtil getRecentPaperThemeFromQuickCreateDialog get_packName " +ftRecentThemeFromNewNotebookDialog.get_packName()
                    +"get_thumbnailURLPath:: "+ftRecentThemeFromNewNotebookDialog.get_thumbnailURLPath());
            return ftRecentThemeFromNewNotebookDialog;
        } else {
            Log.d("TemplatePickerV2","checkRecentThemesDummy getRecentPaperThemeFromQuickCreateDialog Recents List is Empty:: ");
            return null;
        }

    }

    public void updateRecentPapersThemesListDummy(ArrayList<RecentsInfoModel> ftRecentPapersThemesList) {

        Log.d("TemplatePickerV2","checkRecentThemesDummy updateRecentPapersThemesListDummy isNullOrEmpty:: "+isNullOrEmpty(ftRecentPapersThemesList));

        if (!isNullOrEmpty(ftRecentPapersThemesList)) {
            try {
                Gson gson = new Gson();
                String jsonPrefs = gson.toJson(ftRecentPapersThemesList);
                FTApp.getPref().save(SystemPref.RECENTLY_SELECTED_PAPERS_LIST, jsonPrefs);
            }catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            FTApp.getPref().save(SystemPref.RECENTLY_SELECTED_PAPERS_LIST, "defaultValue");
        }

    }

    public void updateRecentCoversThemesListDummy(ArrayList<RecentsInfoModel> ftRecentCoversThemesList) {

        Log.d("TemplatePickerV2","checkRecentThemesDummy updateRecentPapersThemesListDummy isNullOrEmpty:: "+isNullOrEmpty(ftRecentCoversThemesList));

        if (!isNullOrEmpty(ftRecentCoversThemesList)) {
            try {
                Gson gson = new Gson();
                String jsonPrefs = gson.toJson(ftRecentCoversThemesList);
                FTApp.getPref().save(SystemPref.RECENTLY_SELECTED_COVERS_LIST, jsonPrefs);
            }catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            FTApp.getPref().save(SystemPref.RECENTLY_SELECTED_COVERS_LIST, "defaultValue");
        }

    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    public void copyAssets(String filePath, String destPath,
                           String packName) {
        AssetManager assetManager = FTApp.getInstance().getApplicationContext().getResources().getAssets();
        Log.d("TemplatePicker==>","FTTemplateUtil filePath::-"+filePath);

        InputStream in   = null;
        OutputStream out = null;
        try {
            if (filePath.contains("_port@2x.png")) {
                Log.d("TemplatePicker==>","FTTemplateUtil fileName::-"+filePath);
                if (filePath.contains("stockPapers")) {
                    in = assetManager.open(filePath);
                } else {
                    in = new FileInputStream(filePath);
                }
            } else {
                in = new FileInputStream(filePath);
            }
            String themeName = getNSPFileNameWithoutExtn(packName);
            String srcFilePath = themeName+"_port@2x.png";
            File outFile = new File(destPath, themeName+"_port@2x.png");
            out = new FileOutputStream(outFile);
            Log.d("TemplatePicker==>","FTTemplateUtil  copyAssets mode Landscape IF filename::- "+packName );
            copyFile(in, out);
            renameFiles(srcFilePath,destPath,packName);
        } catch(IOException e) {
            Log.d("TemplatePicker==>", "FTTemplateUtil Failed to copy asset file: " + packName, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // NOOP
                    Log.d("TemplatePicker==>", "FTTemplateUtil finally in NOT null Failed to copy asset file: " + e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // NOOP
                    Log.d("TemplatePicker==>", "FTTemplateUtil finally OUT NOT null Failed to copy asset file: " + e);
                }
            }
        }

    }

    private void renameFiles(String srcFilePath, String destPath,String packName) {
        ftSelectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();
        Log.d("TemplatePicker==>","FTTemplateUtil srcFilePath path::-"+FTConstants.TEMP_FOLDER_PATH+"TemplatesCache/"+srcFilePath+
                " contains::-"+srcFilePath.contains("_port@2x.png")+"destPath::-"+destPath);
        File sourceFile = new File(FTConstants.TEMP_FOLDER_PATH+"TemplatesCache/"+srcFilePath);
        File destFile = null;

        if (srcFilePath.contains("_port@2x.png") ||
                srcFilePath.contains("_land@2x.png")) {
            String themeName = getNSPFileNameWithoutExtn(packName);
            String fileName = "thumbnail";
            fileName = "thumbnail"
                    +"_"+themeName
                    +"_"+ftSelectedDeviceInfo.getPageWidth()
                    +"_"+ftSelectedDeviceInfo.getPageHeight()
                    +"_"+ftSelectedDeviceInfo.getThemeBgClrName()
                    +"_"+ftSelectedDeviceInfo.getLineType()
                    +"_"+ftSelectedDeviceInfo.getLayoutType() +".jpg";

            destFile = new File(destPath+"/"+fileName);
            Log.d("TemplatePicker==>","FTTemplateUtil file path::-"+fileName+" destPath_fileName::-"+destPath+"/"+fileName);
            if (sourceFile.renameTo(destFile)) {
                Log.d("TemplatePicker==>","File renamed successfully");
            } else {
                Log.d("TemplatePicker==>","Failed to rename file");
            }
        }
    }

    public String getNSPFileNameWithoutExtn(String packName) {
        String separator = ".nsp";

        Log.d("TemplatePicker==>"," FTTemplatesSingleton getNSPFileNameWithoutExtn::-"+packName);

        int sepPos = packName.lastIndexOf(separator);
        packName = packName.substring(0,sepPos);
        return packName;
    }

    public String generateThumbnailURLPath(FTUrl url, FTNTheme ftnTheme) {
        Log.d("TemplatePickerV2", " generateThumbnailURLPath inside ");
        FTSelectedDeviceInfo ftSelectedDeviceInfo1 = FTSelectedDeviceInfo.selectedDeviceInfo();
        String pathExtension = StringUtil.getFileExtension(url.getPath());

        Log.d("TemplatePickerV2","FTNTheme generateThumbnailURLPath " +
                " PackName::-" +ftnTheme.packName+
                " ThemeName::-"+ftnTheme.themeName+
                " getPageWidth::-"+ftSelectedDeviceInfo1.getPageWidth()+
                " getPageHeight::-"+ftSelectedDeviceInfo1.getPageHeight()+
                " url.getPath::-"+url.getPath()+
                " getLayoutType::-"+ftSelectedDeviceInfo1.getLayoutType()+
                " condition::-"+(ftnTheme.themeName.toLowerCase().contains("diary")));
        if (FTNTheme.isTheThemeExists(url.getPath())) {
            if (pathExtension.equals("nsp")) {
                String dairyCreationYear = FTApp.getPref().get(SystemPref.DIARY_CREATION_YEAR,"2021_2022");
                if (url.getPath().contains("download")) {
                    ftnTheme.isDownloadTheme = true;
                    ftnTheme.thumbnailURLPath = FTConstants.TEMP_FOLDER_PATH+"TemplatesCache/"+"thumbnail"
                            +"_"+ftnTheme.themeName
                            +"_" +ftSelectedDeviceInfo1.getPageWidth()
                            +"_"+ftSelectedDeviceInfo1.getPageHeight()
                            +"_"+ ftSelectedDeviceInfo1.getLayoutType() +".jpg";

                } else if (url.getPath().contains("custom")) {
                    ftnTheme.isCustomTheme = true;
                    ftnTheme.thumbnailURLPath = FTConstants.CUSTOM_PAPERS_PATH  + ftnTheme.packName+ "/thumbnail@2x.png";
                } else if (url.getPath().contains("stockPapers")) {
                    Log.d("TemplatePickerV2","FTNTheme url.getPath()" +url.getPath());
                    if (url.getPath().toLowerCase().contains("plain") ||
                            url.getPath().toLowerCase().contains("ruled") ||
                            url.getPath().toLowerCase().contains("checked") ||
                            url.getPath().toLowerCase().contains("dotted") ||
                            url.getPath().toLowerCase().contains("legal")) {
                        String _layoutType;
                        if (ftnTheme.isLandscape) {
                            _layoutType = "landscape";
                        } else {
                            _layoutType = "portrait";
                        }
                        ftnTheme.thumbnailURLPath = FTConstants.TEMP_FOLDER_PATH+"TemplatesCache/"+"thumbnail"
                                +"_"+ftnTheme.themeName
                                +"_" +(int)ftnTheme.width
                                +"_"+(int)ftnTheme.height
                                +"_"+ftnTheme.themeBgClrName
                                +"_"+ftSelectedDeviceInfo1.getLineType()
                                +"_"+ ftSelectedDeviceInfo1.getLayoutType() +".jpg";

                        Log.d("TemplatePickerV2", " generateThumbnailURLPath IF thumbnailURLPath  "+ftnTheme.thumbnailURLPath);

                    }  else {

                        ftnTheme.thumbnailURLPath = FTConstants.TEMP_FOLDER_PATH+"TemplatesCache/"+"thumbnail"
                                +"_"+ftnTheme.themeName
                                +"_" +ftSelectedDeviceInfo1.getPageWidth()
                                +"_"+ftSelectedDeviceInfo1.getPageHeight()
                                +"_"+ ftSelectedDeviceInfo1.getLayoutType() +".jpg";

                        Log.d("TemplatePickerV2", " generateThumbnailURLPath else thumbnailURLPath  "+ftnTheme.thumbnailURLPath);

                    }
                } else {
                    ftnTheme.thumbnailURLPath        = FTConstants.TEMP_FOLDER_PATH+"TemplatesCache/"+FTConstants.DEFAULT_PAPER_THEME_URL;
                }

                if (ftnTheme.themeName.toLowerCase().contains("diary")) {
                    Log.d("TemplatePicker==>","FTNTheme getThumbnailURLPath Dairy True url.getPath::-"+url.getPath());
                    ftnTheme.thumbnailURLPath = FTConstants.TEMP_FOLDER_PATH+"TemplatesCache/"+"thumbnail"
                            +"_"+dairyCreationYear
                            +"_"+ftSelectedDeviceInfo1.getPageWidth()
                            +"_"+ftSelectedDeviceInfo1.getPageHeight()
                            +"_"+ftSelectedDeviceInfo1.getLayoutType()+".jpg";
                }

                Log.d("TemplatePicker==>","FTNTheme thumbnailURLPath::-" +ftnTheme.thumbnailURLPath);
            } else {
                if (url.getPath().contains("download")) {
                    ftnTheme.isDownloadTheme = true;
                    ftnTheme.thumbnailURLPath = FTConstants.DOWNLOADED_COVERS_PATH  + ftnTheme.packName+ "/thumbnail@2x.png";
                } else if (url.getPath().contains("custom")) {
                    ftnTheme.isCustomTheme = true;
                    ftnTheme.thumbnailURLPath = FTConstants.CUSTOM_COVERS_PATH  + ftnTheme.packName+ "/thumbnail@2x.png";
                } else if (url.getPath().contains("stockCovers")) {
                    ftnTheme.thumbnailURLPath = FTConstants.COVER_FOLDER_NAME + "/" + ftnTheme.packName+ "/thumbnail@2x.png";
                } else {
                    ftnTheme.thumbnailURLPath = FTConstants.TEMP_FOLDER_PATH+"TemplatesCache/"+FTConstants.DEFAULT_PAPER_THEME_URL;
                }
            }
        }
        return ftnTheme.thumbnailURLPath;
    }

    public static Bitmap getBitmapFromAsset(FTNThemeCategory.FTThemeType _coverOrPaperType) {

        String filePath = null;
        AssetManager assetManager = FTApp.getInstance().getAssets();
        InputStream istr;
        Bitmap bitmap = null;
        try {

            String[] stockTemplatesDirectory = assetManager.list("stockPapers");
            if (_coverOrPaperType == FTNThemeCategory.FTThemeType.COVER) {
                filePath = "stockCovers/Orange.nsc/thumbnail@2x.png";
            } else {
                filePath = "stockPapers/Plain.nsp/thumbnail_port@2x.png";
            }
            istr = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            // handle exception
            Log.d("TemplatePicker==>", "FTNewNotebookDialog getBitmapFromAsset exception ::-");
            e.printStackTrace();
        }
        Log.d("TemplatePicker==>", "FTNewNotebookDialog getBitmapFromAsset bitmap ::-"+bitmap);

        return bitmap;
    }

    public boolean isThemeDeleted(FTNThemeCategory.FTThemeType _ftThemeType,FTNTheme ftnTheme) {
        if (FTNTheme.isTheThemeExists(ftnTheme.themeFileURL.getPath())) {
            return  false;
        } else {
            return  true;
        }
    }
}
