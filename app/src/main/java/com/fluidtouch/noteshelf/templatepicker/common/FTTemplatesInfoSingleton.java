package com.fluidtouch.noteshelf.templatepicker.common;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSNumber;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListParser;
import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.utils.AssetsUtil;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.commons.utils.StringUtil;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.models.theme.FTNCoverTheme;
import com.fluidtouch.noteshelf.models.theme.FTNPaperTheme;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTCategories;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTColorVariants;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTCustomizeOptions;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTLineTypes;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTTemplateColors;
import com.fluidtouch.noteshelf.templatepicker.common.supporteddevicesplistdatamodel.FTDevicesDetailedInfo;
import com.fluidtouch.noteshelf.templatepicker.common.supporteddevicesplistdatamodel.ItemModel;
import com.fluidtouch.noteshelf.templatepicker.common.util.FTTemplateUtil;
import com.fluidtouch.noteshelf.templatepicker.models.RecentsInfoModel;
import com.fluidtouch.noteshelf.templatepicker.models.TemplatesInfoModel;
import com.fluidtouch.noteshelf2.BuildConfig;
import com.fluidtouch.noteshelf2.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.ThinDownloadManager;
import com.zendesk.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class FTTemplatesInfoSingleton {
    private static FTTemplatesInfoSingleton instance;
    ArrayList<FTTemplateCategoryInfo> ftTemplateCategoryInfoArrayList = new ArrayList<>();
    ArrayList<FTNTheme> ftnThemeArrayListRecvd = new ArrayList<>();
    ArrayList<FTNTheme> ftnThemeArrayList;
    ArrayList<FTCategories> ftTemplateCategoryInfoArrayListTemp;


    public FTNTheme getRecentFTNTheme() {
        Log.d("TemplatePicker==>"," FTTemplatesSingleton recentFTNTheme FTTemplatesInfoSingleton getRecentFTNTheme recentFTNTheme::-"+recentFTNTheme);
        return recentFTNTheme;
    }

    public void setRecentFTNTheme(FTNTheme recentFTNTheme) {
        Log.d("TemplatePicker==>"," FTTemplatesSingleton recentFTNTheme FTTemplatesInfoSingleton setRecentFTNTheme recentFTNTheme::-"+recentFTNTheme);
        this.recentFTNTheme = recentFTNTheme;
    }

    FTNTheme recentFTNTheme;

    public ArrayList<FTNTheme> getRecentBasicThemes() {
        return recentBasicThemes;
    }

    public void setRecentBasicThemes(ArrayList<FTNTheme> recentBasicThemes) {
        this.recentBasicThemes = recentBasicThemes;
    }

    ArrayList<FTNTheme> recentBasicThemes = new ArrayList<>();
    ArrayList<String> prevDownloadedTemplList = null;
    private ThinDownloadManager downloadManager = new ThinDownloadManager();

    public ItemModel getRecentlySelctedDevice() {
        return recentlySelctedDevice;
    }

    public void setRecentlySelctedDevice(ItemModel recentlySelctedDevice) {
        this.recentlySelctedDevice = recentlySelctedDevice;
    }

    ItemModel recentlySelctedDevice;

    public ArrayList<FTColorVariants> getfTColorVariants() {
        return fTColorVariants;
    }

    public void setfTColorVariants(ArrayList<FTColorVariants> fTColorVariants) {
        this.fTColorVariants = fTColorVariants;
    }

    ArrayList<FTColorVariants> fTColorVariants = new ArrayList();


    public ArrayList<FTLineTypes> getmFTLineTypes() {
        return mFTLineTypes;
    }

    public void setmFTLineTypes(ArrayList<FTLineTypes> mFTLineTypes) {
        this.mFTLineTypes = mFTLineTypes;
    }

    ArrayList<FTLineTypes> mFTLineTypes = new ArrayList<>();

    public FTNTheme getPaperTheme() {
        return paperTheme;
    }

    public void setPaperTheme(FTNTheme paperTheme) {
        this.paperTheme = paperTheme;
    }

    FTNTheme paperTheme;

    public FTNTheme getPaperThemeFrmQuickCreate() {
        return paperThemeFrmQuickCreate;
    }

    public void setPaperThemeFrmQuickCreate(FTNTheme paperThemeFrmQuickCreate) {
        this.paperThemeFrmQuickCreate = paperThemeFrmQuickCreate;
    }

    FTNTheme paperThemeFrmQuickCreate;
    public static synchronized FTTemplatesInfoSingleton getInstance() {
        if (instance == null) {
            instance = new FTTemplatesInfoSingleton();
        }
        return instance;
    }

    public String getScreenResolutionPort() {
        WindowManager wm = (WindowManager) FTApp.getInstance().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        android.graphics.Point realSize = new android.graphics.Point();
        display.getRealSize(realSize);
        int realWidth = Math.min(realSize.x,realSize.y);
        int realHeight = Math.max(realSize.x,realSize.y);
        Log.d("##getScreenResolutionLand","X::-  "+realSize.x +" Y::_"+realSize.y);
        return realWidth + "_" +realHeight ;
    }

    public String getScreenResolutionLand() {
        WindowManager wm = (WindowManager) FTApp.getInstance().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        android.graphics.Point realSize = new android.graphics.Point();
        display.getRealSize(realSize);
        int realWidth = Math.max(realSize.x,realSize.y);
        int realHeight = Math.min(realSize.x,realSize.y);
        Log.d("##getScreenResolutionLand","X::-  "+realSize.x +" Y::_"+realSize.y);
        return realWidth + "_" +realHeight ;
    }

    public ArrayList<FTDevicesDetailedInfo> getSupportedDevicesInfo() {
        boolean matched = false;
        this.copyMetadataIfNeeded(getThemesPlist());

        String myDeviceResPort = getScreenResolutionPort();
        String myDeviceResLand = getScreenResolutionLand();

        ArrayList<FTDevicesDetailedInfo> mFTDevicesDetailedInfo = null;
        File plist = new File(FTConstants.DOCUMENTS_ROOT_PATH + "/Library/" + getThemesPlist());
        try {
            boolean isMobile = FTApp.getInstance().getApplicationContext().getResources().getConfiguration().smallestScreenWidthDp < 600;
            String tabSelected = FTApp.getPref().get(SystemPref.LAST_SELECTED_TAB, "portrait");

            Log.d("TemplatePicker==>"," FTTemplateTabsDialog isMobile::-"
                    +isMobile + "tabSelected::-"+tabSelected);

            FileInputStream inputStream = new FileInputStream(plist);
            NSDictionary dictionary = (NSDictionary) PropertyListParser.parse(inputStream);
            NSDictionary deviceMetaDataDict = (NSDictionary) dictionary.objectForKey("deviceMetaData");
            NSArray deviceMetaDataArray = (NSArray) deviceMetaDataDict.objectForKey("deviceData");
            mFTDevicesDetailedInfo = new ArrayList<>();
            for (int b = 0; b < deviceMetaDataArray.count(); b++) {
                FTDevicesDetailedInfo ftDeviceDataInfo = new FTDevicesDetailedInfo();
                NSDictionary deviceDataObjects = (NSDictionary) deviceMetaDataArray.objectAtIndex(b);

                String displayName = deviceDataObjects.objectForKey("displayName").toString().toLowerCase();
                if (!isMobile) {
                    Log.d("TemplatePicker==>"," FTTemplateTabsDialog Status::-"
                            +(!displayName.toLowerCase().equalsIgnoreCase("Mobile") &&
                            !tabSelected.toLowerCase().equalsIgnoreCase("landscape")));

                    if (tabSelected.toLowerCase().equalsIgnoreCase("portrait")) {
                        if (deviceDataObjects.objectForKey("displayName").toString().contains("This Device")) {
                            if (FTApp.getInstance().getApplicationContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                                ftDeviceDataInfo.setDimension(myDeviceResPort);
                                ftDeviceDataInfo.setDimension_port(myDeviceResPort);
                                ftDeviceDataInfo.setDimension_land(myDeviceResLand);
                            } else {
                                ftDeviceDataInfo.setDimension(myDeviceResPort);
                                ftDeviceDataInfo.setDimension_port(myDeviceResLand);
                                ftDeviceDataInfo.setDimension_land(myDeviceResPort);
                            }
                            ftDeviceDataInfo.setDisplayName("This Device");

                        } else {
                            ftDeviceDataInfo.setDimension(deviceDataObjects.objectForKey("dimension").toString());
                            ftDeviceDataInfo.setDimension_land(deviceDataObjects.objectForKey("dimension_land").toString());
                            ftDeviceDataInfo.setDimension_port(deviceDataObjects.objectForKey("dimension_port").toString());
                        }

                        ftDeviceDataInfo.setDisplayName(deviceDataObjects.objectForKey("displayName").toString());
                        ftDeviceDataInfo.setIdentifier(deviceDataObjects.objectForKey("identifier").toString());
                        mFTDevicesDetailedInfo.add(ftDeviceDataInfo);
                    } else {
                        if (!displayName.equalsIgnoreCase("Mobile")) {

                            if (deviceDataObjects.objectForKey("displayName").toString().contains("This Device")) {
                                if (FTApp.getInstance().getApplicationContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                    ftDeviceDataInfo.setDimension(myDeviceResPort);
                                    ftDeviceDataInfo.setDimension_port(myDeviceResLand);
                                    ftDeviceDataInfo.setDimension_land(myDeviceResPort);
                                }else {
                                    ftDeviceDataInfo.setDimension(myDeviceResPort);
                                    ftDeviceDataInfo.setDimension_port(myDeviceResPort);
                                    ftDeviceDataInfo.setDimension_land(myDeviceResLand);
                                }

                            } else {
                                ftDeviceDataInfo.setDimension(deviceDataObjects.objectForKey("dimension").toString());
                                ftDeviceDataInfo.setDimension_land(deviceDataObjects.objectForKey("dimension_land").toString());
                                ftDeviceDataInfo.setDimension_port(deviceDataObjects.objectForKey("dimension_port").toString());
                            }

                            ftDeviceDataInfo.setDisplayName(deviceDataObjects.objectForKey("displayName").toString());
                            ftDeviceDataInfo.setIdentifier(deviceDataObjects.objectForKey("identifier").toString());
                            mFTDevicesDetailedInfo.add(ftDeviceDataInfo);
                        }
                    }
                } else {
                    Log.d("TemplatePicker==>"," FTTemplateTabsDialog isMobile::-"
                            +isMobile + " tabSelected::-"+ tabSelected.toLowerCase().equalsIgnoreCase("portrait")
                            +displayName);
                    if (tabSelected.toLowerCase().equalsIgnoreCase("portrait")) {
                        if (displayName.toString().contains("This Device")) {
                            if (FTApp.getInstance().getApplicationContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                                ftDeviceDataInfo.setDimension(myDeviceResPort);
                                ftDeviceDataInfo.setDimension_port(myDeviceResPort);
                                ftDeviceDataInfo.setDimension_land(myDeviceResLand);
                            } else {
                                ftDeviceDataInfo.setDimension(myDeviceResPort);
                                ftDeviceDataInfo.setDimension_port(myDeviceResLand);
                                ftDeviceDataInfo.setDimension_land(myDeviceResPort);
                            }
                            ftDeviceDataInfo.setDisplayName("This Device");
                            /*ftDeviceDataInfo.setDimension(myDeviceResPort);
                            ftDeviceDataInfo.setDimension_land(myDeviceResLand);
                            ftDeviceDataInfo.setDimension_port(myDeviceResPort);*/
                        } else {
                            ftDeviceDataInfo.setDimension(deviceDataObjects.objectForKey("dimension").toString());
                            ftDeviceDataInfo.setDimension_land(deviceDataObjects.objectForKey("dimension_land").toString());
                            ftDeviceDataInfo.setDimension_port(deviceDataObjects.objectForKey("dimension_port").toString());
                        }

                        ftDeviceDataInfo.setDisplayName(deviceDataObjects.objectForKey("displayName").toString());
                        ftDeviceDataInfo.setIdentifier(deviceDataObjects.objectForKey("identifier").toString());
                        mFTDevicesDetailedInfo.add(ftDeviceDataInfo);
                    } else {
                        if (!displayName.equalsIgnoreCase("This Device")) {
                            Log.d("TemplatePicker==>"," FTTemplateTabsDialog displayName::-"
                                    +displayName);
                            if (displayName.toString().contains("This Device")) {
                                ftDeviceDataInfo.setDimension(myDeviceResPort);
                                ftDeviceDataInfo.setDimension_land(myDeviceResLand);
                                ftDeviceDataInfo.setDimension_port(myDeviceResPort);
                            } else {
                                ftDeviceDataInfo.setDimension(deviceDataObjects.objectForKey("dimension").toString());
                                ftDeviceDataInfo.setDimension_land(deviceDataObjects.objectForKey("dimension_land").toString());
                                ftDeviceDataInfo.setDimension_port(deviceDataObjects.objectForKey("dimension_port").toString());
                            }

                            ftDeviceDataInfo.setDisplayName(deviceDataObjects.objectForKey("displayName").toString());
                            ftDeviceDataInfo.setIdentifier(deviceDataObjects.objectForKey("identifier").toString());
                            mFTDevicesDetailedInfo.add(ftDeviceDataInfo);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return mFTDevicesDetailedInfo;
    }

    public ArrayList<FTTemplateCategoryInfo> getCategoriesList(FTNThemeCategory.FTThemeType ftThemeType) {

        ftTemplateCategoryInfoArrayList.clear();
        File plist = new File(FTConstants.DOCUMENTS_ROOT_PATH + "/Library/" + getThemesPlist());
        try {

            FileInputStream inputStream = new FileInputStream(plist);
            NSDictionary dictionary = (NSDictionary) PropertyListParser.parse(inputStream);
            NSDictionary packsDict = null;

            if (ftThemeType == FTNThemeCategory.FTThemeType.COVER) {
                packsDict  = (NSDictionary) dictionary.objectForKey("covers");
            } else if (ftThemeType == FTNThemeCategory.FTThemeType.PAPER) {
                packsDict  = (NSDictionary) dictionary.objectForKey("papers");
            }

            NSArray categories      = (NSArray) packsDict.objectForKey("categories");

            for (int c = 0; c < categories.count(); c++) {
                ArrayList<String> mThemesList = new ArrayList<>();
                NSDictionary category = (NSDictionary) categories.objectAtIndex(c);
                NSArray themes = (NSArray) category.objectForKey("themes");
                Log.d("TemplatePicker==>"," FTTemplatesSingleton getCategoriesList themes Count::-"
                        +themes.count());
                for (int j=0;j<themes.count();j++) {
                    Log.d("TemplatePicker==>"," FTTemplatesSingleton getCategoriesList themes.objectAtIndex::-"
                            +themes.objectAtIndex(j).toString());
                    String customThemesDirPath = FTConstants.DOWNLOADED_PAPERS_PATH2;
                    if (ftThemeType == FTNThemeCategory.FTThemeType.COVER) {
                        customThemesDirPath = FTConstants.DOWNLOADED_COVERS_PATH;
                    } else {
                        customThemesDirPath = FTConstants.DOWNLOADED_PAPERS_PATH2;
                    }
                    File customThemesDir = new File(customThemesDirPath);
                    if (customThemesDir.exists() && customThemesDir.isDirectory()) {
                        if (Arrays.asList(customThemesDir.list()).contains(themes.objectAtIndex(j).toString())) {
                            mThemesList.add(themes.objectAtIndex(j).toString());
                            ftTemplateCategoryInfoArrayList.add(new FTTemplateCategoryInfo(category.objectForKey("category_name").toString(),mThemesList));
                            break;
                        }
                    }

                    if (ftThemeType == FTNThemeCategory.FTThemeType.COVER) {
                        if (Arrays.asList(FTApp.getInstance().getApplicationContext().getResources().
                                getAssets().list("stockCovers")).contains(themes.objectAtIndex(j).toString())) {
                            mThemesList.add(themes.objectAtIndex(j).toString());
                            ftTemplateCategoryInfoArrayList.add(
                                    new FTTemplateCategoryInfo(category.objectForKey("category_name").toString(),mThemesList));
                            break;
                        }
                    } else if (ftThemeType == FTNThemeCategory.FTThemeType.PAPER) {
                        if (Arrays.asList(FTApp.getInstance().getApplicationContext().getResources().
                                getAssets().list("stockPapers")).contains(themes.objectAtIndex(j).toString())) {
                            mThemesList.add(themes.objectAtIndex(j).toString());
                            ftTemplateCategoryInfoArrayList.add(
                                    new FTTemplateCategoryInfo(category.objectForKey("category_name").toString(),mThemesList));
                            break;
                        }
                    }
                }
            }

            ftTemplateCategoryInfoArrayList.add(new FTTemplateCategoryInfo("Custom",new ArrayList<String>()));

            for (int i=0;i<ftTemplateCategoryInfoArrayList.size();i++) {
                Log.d("TemplatePicker==>"," FTTemplatesSingleton getCategoriesList getCategoryName::-"+
                        ftTemplateCategoryInfoArrayList.get(i).getCategoryName());
            }
            return ftTemplateCategoryInfoArrayList;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ftTemplateCategoryInfoArrayList;
    }

    public ArrayList<FTCategories> getFTNThemeCategory(String origin, FTNThemeCategory.FTThemeType type) {
        ArrayList<FTLineTypes> ftLineTypes                  = new ArrayList<>();
        ArrayList<FTCategories> ftCategoriesArrayList       = new ArrayList<>();
        ArrayList<FTColorVariants> ftColorVariantsArrayList = new ArrayList<>();

        /*
         * get List of categories downloaded in StockPapers folder
         *
         * */
        FTLineTypes mlineTypes=null;
        FTCategories mFTCategory=null;
        FTColorVariants mFTColorVariants = null;
        FTTemplateColors mFTTemplateColors = null;
        ArrayList<FTTemplateColors> ftTemplateColorsArrayList = null;

        File plist = new File(FTConstants.DOCUMENTS_ROOT_PATH + "/Library/" + getThemesPlist());

        try {
            //checkRecentThemes(ftCategoriesArrayList,origin,type,new ArrayList<FTNTheme>());
            Log.d("TemplatePicker==>"," FTTemplatesInfoSingleton getFTNThemeCategory FTThemeType::-"+type);
            FileInputStream inputStream = new FileInputStream(plist);
            NSDictionary dictionary = (NSDictionary) PropertyListParser.parse(inputStream);
            NSDictionary packsDict = null;
            if (type == FTNThemeCategory.FTThemeType.COVER) {
                packsDict = (NSDictionary) dictionary.objectForKey("covers");
                ftCategoriesArrayList = getPapersInfo(packsDict,mlineTypes,mFTCategory,mFTColorVariants,mFTTemplateColors,
                        ftTemplateColorsArrayList,ftLineTypes,ftColorVariantsArrayList,
                        ftCategoriesArrayList,origin,type);
            } else if (type == FTNThemeCategory.FTThemeType.PAPER) {
                packsDict = (NSDictionary) dictionary.objectForKey("papers");
                ftCategoriesArrayList = getPapersInfo(packsDict,mlineTypes,mFTCategory,mFTColorVariants,mFTTemplateColors,
                        ftTemplateColorsArrayList,ftLineTypes,ftColorVariantsArrayList,
                        ftCategoriesArrayList,origin,type);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        ftCategoriesArrayList.removeAll(Arrays.asList(null,""));
        return ftCategoriesArrayList;
    }

    private ArrayList<FTCategories> getPapersInfo(NSDictionary packsDict, FTLineTypes mlineTypes,
                                                  FTCategories mFTCategory, FTColorVariants mFTColorVariants,
                                                  FTTemplateColors mFTTemplateColors, ArrayList<FTTemplateColors> ftTemplateColorsArrayList,
                                                  ArrayList<FTLineTypes> ftLineTypes, ArrayList<FTColorVariants> ftColorVariantsArrayList,
                                                  ArrayList<FTCategories> ftCategoriesArrayList, String origin,
                                                  FTNThemeCategory.FTThemeType type) {
        FTSelectedDeviceInfo ftSelectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();
        NSArray categories = (NSArray) packsDict.objectForKey("categories");
        if (ftTemplateCategoryInfoArrayList != null && !
                ftTemplateCategoryInfoArrayList.isEmpty()) {
            for (int a=0;a<ftTemplateCategoryInfoArrayList.size();a++) {
                for (int b = 0; b < categories.count(); b++) {
                    NSDictionary categoryDict = (NSDictionary) categories.objectAtIndex(b);
                    String catName = categoryDict.objectForKey("category_name").toString();
                    if (catName.equalsIgnoreCase(ftTemplateCategoryInfoArrayList.get(a).categoryName)) {
                        ArrayList<FTNTheme> themes = new ArrayList<>();
                        NSDictionary filteredCatDictionary = (NSDictionary) categories.objectAtIndex(b);
                        mFTCategory = new FTCategories();
                        Log.d("TemplatePicker==>"," FTTemplatesSingleton getPapersInfo category_name::-"+filteredCatDictionary.objectForKey("category_name").toString());
                        mFTCategory.setCategory_name(filteredCatDictionary.objectForKey("category_name").toString());
                        mFTCategory.setFtThemeType(type);

                        if (filteredCatDictionary.containsKey("customize_options")) {
                            FTCustomizeOptions ftCustomizeOptions = new FTCustomizeOptions();
                            NSDictionary customize_options = (NSDictionary) filteredCatDictionary.objectForKey("customize_options");

                            NSArray color_variants = (NSArray) customize_options.objectForKey("color_variants");

                            for (int c = 0; c < color_variants.count(); c++) {
                                mFTCategory.setCustomizeOptions(true);
                                ftTemplateColorsArrayList = new ArrayList<>();
                                mFTColorVariants = new FTColorVariants();
                                NSDictionary color_variants_objects = (NSDictionary) color_variants.objectAtIndex(c);
                                NSString title = (NSString) color_variants_objects.objectForKey("title");
                                if (color_variants_objects.containsKey("colors")) {
                                    NSArray colors = (NSArray) color_variants_objects.objectForKey("colors");

                                    for (int j= 0 ; j<colors.count();j++) {
                                        mFTTemplateColors = new FTTemplateColors();
                                        NSDictionary color_objects = (NSDictionary) colors.objectAtIndex(j);
                                        mFTTemplateColors.setColorHex(color_objects.objectForKey("colorHex").toString());
                                        mFTTemplateColors.setColorName(color_objects.objectForKey("colorName").toString());
                                        mFTTemplateColors.setHorizontalLineColor(color_objects.objectForKey("horizontalLineColor").toString());
                                        mFTTemplateColors.setVerticalLineColor(color_objects.objectForKey("verticalLineColor").toString());
                                        ftTemplateColorsArrayList.add(mFTTemplateColors);
                                    }
                                }
                                mFTColorVariants.setmFTTemplateColors(ftTemplateColorsArrayList);
                                mFTColorVariants.setTitle(title.toString());
                                ftColorVariantsArrayList.add(mFTColorVariants);
                            }
                            ftCustomizeOptions.setmFTColorVariants(ftColorVariantsArrayList);
                            setfTColorVariants(ftColorVariantsArrayList);

                            //Line Type data
                            NSArray lineTypes = (NSArray) customize_options.objectForKey("lineTypes");

                            for (int d = 0; d < lineTypes.count(); d++) {
                                mlineTypes = new FTLineTypes();
                                NSDictionary lineTypes_objects = (NSDictionary) lineTypes.objectAtIndex(d);
                                NSNumber horizontalLineSpacing = (NSNumber) lineTypes_objects.objectForKey("horizontalLineSpacing");
                                NSNumber verticalLineSpacing   = (NSNumber) lineTypes_objects.objectForKey("verticalLineSpacing");

                                mlineTypes.setHorizontalLineSpacing(horizontalLineSpacing.intValue());
                                mlineTypes.setVerticalLineSpacing(verticalLineSpacing.intValue());
                                mlineTypes.setLineType(lineTypes_objects.objectForKey("lineType").toString());
                                ftLineTypes.add(mlineTypes);
                            }

                            ftCustomizeOptions.setmFTLineTypes(ftLineTypes);
                            setmFTLineTypes(ftLineTypes);

                            mFTCategory.setmFTCustomizeOptions(ftCustomizeOptions);
                        } else {
                            mFTCategory.setCustomizeOptions(false);
                        }

                        NSArray themesArray = (NSArray) filteredCatDictionary.objectForKey("themes");
                        for (int i = 0; i < themesArray.count(); i++) {
                                String themeName = themesArray.objectAtIndex(i).toString();
                            FTUrl url = getUrl(themeName, origin);
                            if (!url.getPath().equals("")) {
                                Log.d("TemplatePicker==>","FTTemplatesSingleton getFTNThemeCategory status::-"+(type == FTNThemeCategory.FTThemeType.COVER)+ " Type::-"+type+" COVER::-"+FTThemeType.COVER);
                                if (type == FTNThemeCategory.FTThemeType.COVER) {
                                    FTNCoverTheme coverTheme = (FTNCoverTheme) FTNTheme.theme(url);
                                    coverTheme.themeName     = themeName;
                                    coverTheme.categoryName  = catName;
                                    themes.add(coverTheme);
                                } else if (type == FTNThemeCategory.FTThemeType.PAPER) {
                                    FTNTheme paperTheme = FTNTheme.theme(url);
                                    paperTheme.themeName            = themeName;
                                    paperTheme.categoryName         = catName;
                                    paperTheme.width                = ftSelectedDeviceInfo.getPageWidth();
                                    paperTheme.height               = ftSelectedDeviceInfo.getPageHeight();

                                    if (paperTheme.dynamicId == 2) {
                                        paperTheme.themeBgClr           = ftSelectedDeviceInfo.getThemeBgClrHexCode();
                                        paperTheme.horizontalLineColor  = ftSelectedDeviceInfo.getHorizontalLineClr();
                                        paperTheme.verticalLineColor    = ftSelectedDeviceInfo.getVerticalLineClr();
                                        paperTheme.verticalSpacing      = ftSelectedDeviceInfo.getVerticalLineSpacing();
                                        paperTheme.horizontalSpacing    = ftSelectedDeviceInfo.getHorizontalLineSpacing();
                                    }

                                    themes.add(paperTheme);
                                    Log.d("TemplatePicker==>","FTTemplatesSingleton getFTNThemeCategory packName::-"
                                            +paperTheme.packName+" themeName::-"+paperTheme.themeName+
                                            " ftSelectedDeviceInfo.getThemeBgClrHexCode()::-"+ftSelectedDeviceInfo.getThemeBgClrHexCode()+
                                            " ftSelectedDeviceInfo.getHorizontalLineClr()::-"+ftSelectedDeviceInfo.getHorizontalLineClr()+
                                            " ftSelectedDeviceInfo.getVerticalLineClr()::-"+ftSelectedDeviceInfo.getVerticalLineClr()+
                                            " paperTheme.width::-"+paperTheme.width+
                                            " paperTheme.height::-"+paperTheme.height );
                                    if (paperTheme.packName.toLowerCase().contains("dairy")) {
                                        Log.d("TemplatePicker==>","FTTemplatesSingleton getFTNThemeCategory thumbnailURLPath::-"+paperTheme.thumbnailURLPath);
                                    }
                                }

                                for (int ij=0;ij<themes.size();ij++)  {
                                    Log.d("TemplatePicker==>","FTTemplatesSingleton getPapersInfo  " +
                                            "FTNPaperTheme instance::-"+(themes.get(ij) instanceof FTNPaperTheme) + " FTNCoverTheme instance::-"+(themes.get(ij) instanceof FTNCoverTheme));

                                }
                                mFTCategory.setFtThemes(themes);
                            }
                        }
                        ftCategoriesArrayList.add(mFTCategory);
                    }
                }
            }

            checkCustomThemes(ftCategoriesArrayList,type);
            return ftCategoriesArrayList;
        }
        return ftCategoriesArrayList;
    }

    public void savedDataInSharedPrefs(ArrayList<FTCategories> ftTemplateCategoryInfoArrayList, String origin) {

        for (int k=0;k<ftTemplateCategoryInfoArrayList.size();k++) {
            Log.d("TemplatePicker==>"," FTTemplateCategories FTTemplateDetailedInfoAdapter origin::- " +origin+
                    " savedDataInSharedPrefs getCategory_name mani:-"
                    +ftTemplateCategoryInfoArrayList.get(k).getCategory_name());
        }

        Gson gson = new Gson();
        String toJson = gson.toJson(ftTemplateCategoryInfoArrayList);
        FTApp.getPref().save(SystemPref.ALL_TEMPLATES_INFO_LIST, toJson);
    }

    public void savedData(ArrayList<FTCategories> ftTemplateCategoryInfoArrayList) {
        ftTemplateCategoryInfoArrayListTemp = ftTemplateCategoryInfoArrayList;
    }

    public ArrayList<FTCategories> getSavedData() {
        return ftTemplateCategoryInfoArrayListTemp;
    }

    /*public void checkRecentThemes(ArrayList<FTCategories> ftCategoriesArrayList, String origin,
                                  FTNThemeCategory.FTThemeType ftThemeType,ArrayList<FTNTheme> ftnThemeArrayList1) {
        Log.d("TemplatePicker==>","FTTemplatesSingleton checkRecentThemes ftThemeType::-"+ftThemeType);
        ArrayList<FTNTheme> ftnThemeArrayListNew = ftnThemeArrayList1;
        if (ftThemeType == FTNThemeCategory.FTThemeType.COVER) {
            ftnThemeArrayListNew.clear();
            Gson gson = new Gson();
            String json = FTApp.getPref().get(SystemPref.RECENTLY_SELECTED_COVERS_LIST, "defaultValue");
            Log.d("TemplatePicker==>","FTTemplatesSingleton checkRecentThemes json::-"+json);
            if (!json.equalsIgnoreCase("defaultValue")) {
                FTCategories mFTCustomCategory = new FTCategories();
                mFTCustomCategory.setCategory_name("Recent");
                Type type = new TypeToken<ArrayList<FTNTheme>>() {}.getType();
                ArrayList<FTNTheme> ftnThemeArrayList = gson.fromJson(json, type);

                for (int i=0;i<ftnThemeArrayList.size();i++) {

                    String themeName = ftnThemeArrayList.get(i).packName;
                    FTUrl url = getUrl(themeName,origin);

                    if (!url.getPath().equals("")) {
                        FTNTheme theme = FTNTheme.theme(url);
                        if (url.getPath().contains("download")) {
                            theme.isDownloadTheme = true;
                        } else if (url.getPath().contains("custom")) {
                            theme.isCustomTheme = true;
                        }

                        ftnThemeArrayList.get(i).categoryName = "Recent";
                        theme.categoryName              = "Recent";
                        theme.themeName                 = themeName;
                        theme.isDefaultTheme            = ftnThemeArrayList.get(i).isDefaultTheme;
                        theme.isBasicTheme              = ftnThemeArrayList.get(i).isBasicTheme;
                        theme.width                     = ftnThemeArrayList.get(i).width;
                        theme.height                    = ftnThemeArrayList.get(i).height;

                        theme.themeBgClr                = ftnThemeArrayList.get(i).themeBgClr;
                        theme.isLandscape               = ftnThemeArrayList.get(i).isLandscape;
                        theme.isBasicTheme              = ftnThemeArrayList.get(i).isBasicTheme;
                        theme.thumbnailURLPath          = ftnThemeArrayList.get(i).thumbnailURLPath;
                        theme.verticalLineColor         = ftnThemeArrayList.get(i).verticalLineColor;
                        theme.horizontalLineColor       = ftnThemeArrayList.get(i).horizontalLineColor;
                        theme.verticalSpacing           = ftnThemeArrayList.get(i).verticalSpacing;
                        theme.horizontalSpacing         = ftnThemeArrayList.get(i).horizontalSpacing;

                        ftnThemeArrayListNew.add(theme);

                        Log.d("TemplatePicker==>"," FTTemplatesSingleton checkRecentThemes FTTemplatesInfoSingleton COVERS checkRecentThemes::-"+
                                (theme instanceof FTNCoverTheme));
                    }
                }

                if (!origin.contains("download") ||
                        !origin.contains("custom")) {
                    Collections.reverse(ftnThemeArrayListNew);
                }
                ftnThemeArrayListNew.removeAll(Arrays.asList(null,""));
                for (int i=0;i<ftnThemeArrayListNew.size();i++) {
                    Log.d("TemplatePicker==>"," FTTemplatesSingleton RECENTLY_SELECTED_COVERS_LIST FTTemplatesInfoSingleton COVERS checkRecentThemes ftnThemeArrayListRecvd packName::-"+
                            ftnThemeArrayListNew.get(i).packName);
                }
                mFTCustomCategory.setFtThemes(ftnThemeArrayListNew);
                ftCategoriesArrayList.add(0,mFTCustomCategory);
            }
        } else {
            ftnThemeArrayListNew.clear();
            Gson gson = new Gson();
            String json = FTApp.getPref().get(SystemPref.RECENTLY_SELECTED_PAPERS_LIST, "defaultValue");
            if (!json.equalsIgnoreCase("defaultValue")) {
                FTCategories mFTCustomCategory = new FTCategories();
                mFTCustomCategory.setCategory_name("Recent");
                Type type = new TypeToken<ArrayList<FTNTheme>>() {}.getType();
                ArrayList<FTNTheme> ftnThemeArrayList = gson.fromJson(json, type);

                for (int i=0;i<ftnThemeArrayList.size();i++) {

                    String themeName = ftnThemeArrayList.get(i).packName;
                    FTUrl url = getUrl(themeName,origin);

                    if (!url.getPath().equals("")) {
                        FTNTheme theme = FTNTheme.theme(url);
                        if (url.getPath().contains("download")) {
                            theme.isDownloadTheme = true;
                        } else if (url.getPath().contains("custom")) {
                            theme.isCustomTheme = true;
                        }
                        theme.categoryName              = "Recent";
                        theme.themeName                 = themeName;
                        theme.width                     = ftnThemeArrayList.get(i).width;
                        theme.height                    = ftnThemeArrayList.get(i).height;

                        theme.themeBgClr                = ftnThemeArrayList.get(i).themeBgClr;
                        theme.isLandscape               = ftnThemeArrayList.get(i).isLandscape;
                        theme.isBasicTheme              = ftnThemeArrayList.get(i).isBasicTheme;
                        theme.thumbnailURLPath          = ftnThemeArrayList.get(i).thumbnailURLPath;
                        theme.verticalLineColor         = ftnThemeArrayList.get(i).verticalLineColor;
                        theme.horizontalLineColor       = ftnThemeArrayList.get(i).horizontalLineColor;
                        theme.bitmap                    = ftnThemeArrayList.get(i).bitmap;

                        ftnThemeArrayListNew.add(theme);
                    }
                }

                if (!origin.contains("download") ||
                        !origin.contains("custom")) {
                    Collections.reverse(ftnThemeArrayListNew);
                }
                ftnThemeArrayListNew.removeAll(Arrays.asList(null,""));
                for (int i=0;i<ftnThemeArrayListNew.size();i++) {
                    Log.d("TemplatePicker==>"," FTTemplatesSingleton RECENTLY_SELECTED_PAPERS_LIST FTTemplatesInfoSingleton checkRecentThemes ftnThemeArrayListNew packName::-"+
                            ftnThemeArrayListNew.get(i).packName+" thumbnailURLPath::-"+ftnThemeArrayListNew.get(i).thumbnailURLPath);
                }

                *//*
                 * Reordering logic of Recent Themes
                 *//*
            *//*if (ftnThemeArrayListNew != null &&
                    !ftnThemeArrayListNew.isEmpty()) {
                String recentPaperThemeURL = FTApp.getPref().get(SystemPref.RECENT_PAPER_THEME_URL,
                        FTConstants.DEFAULT_PAPER_THEME_URL);
                Log.d("TemplatePicker==>"," recentFTNTheme FTTemplatesInfoSingleton checkRecentThemes recentPaperThemeURL::-"
                        +recentPaperThemeURL+" recentFTNTheme.thumbnailURLPath::-"+recentFTNTheme.thumbnailURLPath);
                for (int i=0; i<ftnThemeArrayListNew.size();i++) {
                    if (recentPaperThemeURL.toLowerCase().equalsIgnoreCase(
                            ftnThemeArrayListNew.get(i).thumbnailURLPath.toLowerCase())) {
                        ftnThemeArrayListNew.remove(i);
                        ftnThemeArrayListNew.add(0,recentFTNTheme);
                    }
                }
                setRecentThemes(ftnThemeArrayListNew,"CheckRecentThemes");
            }*//*
                mFTCustomCategory.setFtThemes(ftnThemeArrayListNew);
                ftCategoriesArrayList.add(0,mFTCustomCategory);
            }
        }


    }*/

    private void checkCustomThemes(ArrayList<FTCategories> ftCategoriesArrayList, FTNThemeCategory.FTThemeType type) {
        String customThemesDirPath = FTConstants.CUSTOM_PAPERS_PATH;

        if (type == FTNThemeCategory.FTThemeType.COVER) {
            customThemesDirPath = FTConstants.CUSTOM_COVERS_PATH;
        } else {
            customThemesDirPath = FTConstants.CUSTOM_PAPERS_PATH;
        }
        File customThemesDir = new File(customThemesDirPath);
        if (customThemesDir.exists() && customThemesDir.isDirectory()) {
            customThemes(ftCategoriesArrayList,type);
        } else {

            FTCategories mFTCustomCategory = new FTCategories();
            mFTCustomCategory.setCategory_name("Custom");
            ArrayList<FTNTheme> ftnThemeArrayList = new ArrayList<>();
            FTNTheme ftnTheme = new FTNTheme();
            ftnTheme.themeName = FTApp.getInstance().
                    getApplicationContext().
                    getResources().
                    getString(R.string.template_custom_theme);
            if (type == FTNThemeCategory.FTThemeType.COVER) {
                ftnTheme.ftThemeType = FTNThemeCategory.FTThemeType.COVER;
            } else {
                ftnTheme.ftThemeType = FTNThemeCategory.FTThemeType.PAPER;
            }
            ftnTheme.categoryName = "Custom";
            ftnTheme.isCustomTheme = true;
            ftnTheme.dynamicId = 2;
            ftnTheme.isLandscape = false;
            ftnThemeArrayList.add(ftnTheme);
            mFTCustomCategory.setFtThemes(ftnThemeArrayList);
            ftCategoriesArrayList.add(mFTCustomCategory);
        }
    }

    private void customThemes(ArrayList<FTCategories> ftCategoriesArrayList, FTNThemeCategory.FTThemeType type) {
        String customThemesDirPath = null;
        FTCategories mFTCustomCategory = new FTCategories();
        ArrayList<FTNTheme> customThemesLocal = new ArrayList<>();
        if (type == FTNThemeCategory.FTThemeType.COVER) {
            customThemesDirPath = FTConstants.CUSTOM_COVERS_PATH;
        } else {
            customThemesDirPath = FTConstants.CUSTOM_PAPERS_PATH;
        }

        File customThemesDir = new File(customThemesDirPath);
        if (customThemesDir.exists() && customThemesDir.isDirectory()) {
            for (File eachThemeDir : customThemesDir.listFiles()) {
                if (eachThemeDir.exists() && eachThemeDir.isDirectory()) {
                    String themeName = eachThemeDir.getName();
                    FTUrl url = getUrl(themeName, "customThemes");
                    if (!url.getPath().equals("")) {
                        FTNTheme theme = FTNTheme.theme(url);
                        if (url.getPath().contains("custom")) {
                            theme.isCustomTheme = true;
                        }

                        theme.themeName     = themeName;
                        theme.categoryName  = "Custom";
                        theme.isLandscape   = false;
                        theme.bitmap = theme.themeThumbnail(FTApp.getInstance().getApplicationContext());
                        theme.setCategoryName(FTApp.getInstance().getApplicationContext().getString(R.string.custom));
                        customThemesLocal.add(theme);
                    }
                }
            }

            FTNTheme ftnTheme = new FTNTheme();
            ftnTheme.categoryName   = "Custom";
            ftnTheme.themeName      = FTApp.getInstance().getApplicationContext().getResources().getString(R.string.template_custom_theme);
            if (type == FTNThemeCategory.FTThemeType.COVER) {
                ftnTheme.ftThemeType    = FTNThemeCategory.FTThemeType.COVER;
            } else {
                ftnTheme.ftThemeType    = FTNThemeCategory.FTThemeType.PAPER;
            }

            ftnTheme.isCustomTheme  = true;
            customThemesLocal.add(ftnTheme);

            Collections.reverse(customThemesLocal);

            mFTCustomCategory.setCategory_name("Custom");
            mFTCustomCategory.setFtThemes(customThemesLocal);
            ftCategoriesArrayList.add(mFTCustomCategory);
        }

    }

    public FTUrl getUrl(String fileName, String origin) {

        String path = "";
        String extension = StringUtil.getFileExtension(fileName);
        Log.d("TemplatePicker==>"," FTTemplatesSingleton getUrl origin::-"+origin+" extension::-"+extension);
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
            if (AssetsUtil.isAssetExists("stockPapers/" + fileName) &&
                    (new File(FTConstants.DOWNLOADED_PAPERS_PATH2 + fileName).exists())) {
                path = FTConstants.DOWNLOADED_PAPERS_PATH2 + fileName;
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
    }

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

    private void copyMetadataIfNeeded(String fileName) {
        File plistFile = new File(FTConstants.DOCUMENTS_ROOT_PATH + "/Library/" + fileName);

        if (plistFile.exists() &&
                BuildConfig.VERSION_CODE == FTApp.getPref().get("themes_v5_appVersion", 0)) {
            return;
        } else {
            plistFile.getParentFile().mkdirs();

            AssetManager assetmanager = FTApp.getInstance().getApplicationContext().getAssets();
            try {
                InputStream bundleInputStrem = assetmanager.open("" + fileName);
                this.createFileFromInputStream(bundleInputStrem,fileName);
                FTApp.getPref().save("themes_v5_appVersion", BuildConfig.VERSION_CODE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private File createFileFromInputStream(InputStream inputStream, String fileName) {

        try {
            File f = new File(FTConstants.DOCUMENTS_ROOT_PATH + "/Library/" + fileName);
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

    private ArrayList<String> removeDuplicates(ArrayList<String> categoryNamesList) {
        // Create a new ArrayList
        ArrayList<String> newList = new ArrayList<>();
        // Traverse through the first list
        for (String element : categoryNamesList) {
            // If this element is not present in newList
            // then add it
            if (!newList.contains(element)) {
                newList.add(element);
            }
        }
        return newList;
    }

    public ArrayList<String> getPrevDownloadedTemplList() {
        return this.prevDownloadedTemplList;
    }

    public void setPrevDownloadedTemplList(ArrayList<String> prevDownloadedTemplList) {
        this.prevDownloadedTemplList = prevDownloadedTemplList;
    }

    public void validatingPlistVersions() {
        String existingPlistPath = getThemesPlist();
        int existingPlistVersion = getPlistExistingVersion(existingPlistPath);

        String pListfromServerPath = pListfromServer();
        int serverPlistVersion = getServerPlistVersion(pListfromServerPath);
    }

    public void downloadPlistFromServer() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                deleteExistingFile();
                DownloadRequest downloadRequest = new DownloadRequest(Uri.parse(FTConstants.PLIST_ENDPOINT))
                        .setDestinationURI(Uri.parse(pListfromServer()))
                        .setStatusListener(new DownloadStatusListenerV1() {
                            @Override
                            public void onDownloadComplete(com.thin.downloadmanager.DownloadRequest downloadRequest) {
                                FTApp.getPref().save(SystemPref.JOB_SCHEDULER_STARTED, true);
                                File sourceFile = new File(pListfromServer());
                                File destinationFile = new File(FTConstants.DOCUMENTS_ROOT_PATH + "/Library/" + FTTemplatesInfoSingleton.getThemesPlist());
                                if (destinationFile.exists()) {
                                    FTFileManagerUtil.moveFile(sourceFile,destinationFile);
                                }
                            }

                            @Override
                            public void onDownloadFailed(com.thin.downloadmanager.DownloadRequest downloadRequest, int errorCode, String errorMessage) {
                                FTApp.getPref().save(SystemPref.JOB_SCHEDULER_STARTED, false);
                            }

                            @Override
                            public void onProgress(com.thin.downloadmanager.DownloadRequest downloadRequest, long totalBytes, long downloadedBytes, int progress) {
                            }

                        });
                downloadManager.add(downloadRequest);
            }
        }).start();
    }

    protected void deleteExistingFile() {
        boolean deleted = false;
        File file = new File(pListfromServer());
        if (file.exists()) {
            deleted = file.delete();
        }
    }

    public static String pListfromServer() {
        String lang = Locale.getDefault().getLanguage();
        if (Locale.getDefault().toLanguageTag().contains("zh-Hans")) {
            lang = "zh-Hans";
        } else if (Locale.getDefault().toLanguageTag().contains("zh-Hant")) {
            lang = "zh-Hant";
        }
        String plistName = "themes_v8_" + lang + ".plist";
        return plistName;
    }

    public int getServerPlistVersion(String path) {
        int plistVersion = 0;

        File plist = new File(path);
        try {
            FileInputStream inputStream = new FileInputStream(plist);
            NSDictionary dictionary = (NSDictionary) PropertyListParser.parse(inputStream);
            NSNumber plistVersionNSNumber = (NSNumber) dictionary.objectForKey("version");
            return plistVersionNSNumber.intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return plistVersion;
    }

    public int getPlistExistingVersion(String path) {
        int plistVersion = 0;

        File plist = new File(path);
        try {
            AssetManager assetmanager = FTApp.getInstance().getApplicationContext().getAssets();
            InputStream bundleInputStrem = assetmanager.open("" +path);
            NSDictionary dictionary = (NSDictionary) PropertyListParser.parse(bundleInputStrem);
            NSNumber plistVersionNSNumber = (NSNumber) dictionary.objectForKey("version");

            return plistVersionNSNumber.intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return plistVersion;
    }

    public String getNSPFileNameWithoutExtn(FTNTheme ftnTheme) {
        String result;
        String separator = ".nsp";
        if (ftnTheme.ftThemeType == FTNThemeCategory.FTThemeType.COVER) {
            if (ftnTheme.themeName.contains(".nsc")) {
                result = ftnTheme.themeName;
            } else {
                result = ftnTheme.packName;
            }
            if (result==null || result.isEmpty()) {
                return null;
            }
            separator = ".nsc";
        } else {
            if (ftnTheme.themeName.contains(".nsp")) {
                result = ftnTheme.themeName;
            } else {
                result = ftnTheme.packName;
            }
            separator = ".nsp";
        }

        Log.d("TemplatePicker==>"," FTTemplatesSingleton getNSPFileNameWithoutExtn " +
                " result::-"+result +
                " themeName::-"+ftnTheme.themeName+
                " packName::-"+ftnTheme.packName+
                " ftThemeType::-"+ftnTheme.ftThemeType);
        int sepPos = result.lastIndexOf(separator);
        result = result.substring(0,sepPos);
        return result;
    }

    public enum FTThemeType implements Serializable {
        COVER, PAPER
    }

    public void checkRecentThemesDummy(FTNThemeCategory.FTThemeType ftThemeType,ArrayList<TemplatesInfoModel> templatesInfoModelsList) {
        Log.d("TemplatePicker==>","FTTemplatesSingleton checkRecentThemes ftThemeType::-"+ftThemeType);
        if (ftThemeType == FTNThemeCategory.FTThemeType.COVER) {

            ArrayList<RecentsInfoModel> recentsInfoList = FTTemplateUtil.getInstance().getRecentCoversDummy();

            boolean  isNullOrEmpty =   FTTemplateUtil.getInstance().isNullOrEmpty(recentsInfoList);
            Log.d("TemplatePickerV2"," checkRecentThemesDummy COVER recentsInfoList isNullOrEmpty:: "+isNullOrEmpty);

            if (!isNullOrEmpty) {
                TemplatesInfoModel recentTemplatesInfo = new TemplatesInfoModel();
                recentTemplatesInfo.set_categoryName("Recent");
                for (int i=0;i<recentsInfoList.size();i++) {
                    Log.d("TemplatePickerV2"," checkRecentThemesDummy COVER get_packName:: "+recentsInfoList.get(i).get_packName() +
                            " get_categoryName:: "+recentsInfoList.get(i).get_categoryName()+" get_thumbnailURLPath:: "+recentsInfoList.get(i).get_thumbnailURLPath());

                    FTNTheme theme  = FTNTheme.theme(FTNThemeCategory.getUrl(recentsInfoList.get(i).get_packName()));
                    theme.categoryName              = recentsInfoList.get(i).get_categoryName();
                    theme.thumbnailURLPath          = recentsInfoList.get(i).get_thumbnailURLPath();
                    theme.themeBgClrName            = recentsInfoList.get(i).getThemeBgClrName();
                    theme.themeBgClr                = recentsInfoList.get(i).getThemeBgClr();
                    theme.packName                  = recentsInfoList.get(i).get_packName();
                    theme.themeName                 = recentsInfoList.get(i).get_themeName();
                    /*theme.themeBgClrName          = recentsInfoList.get(i).get_dairyEndDate();
                    theme.themeBgClrName            = recentsInfoList.get(i).get_dairyStartDate();*/
                    theme.verticalLineColor         = recentsInfoList.get(i).getVerticalLineColor();
                    theme.horizontalLineColor       = recentsInfoList.get(i).getHorizontalLineColor();
                    theme.horizontalSpacing         = recentsInfoList.get(i).getHorizontalSpacing();
                    theme.verticalSpacing           = recentsInfoList.get(i).getVerticalSpacing();
                    theme.width                     = recentsInfoList.get(i).getWidth();
                    theme.height                    = recentsInfoList.get(i).getHeight();
                    theme.bitmap                    = FTTemplateUtil.getInstance().StringToBitMap(recentsInfoList.get(i).get_themeBitmapInStringFrmt());
                    recentTemplatesInfo.AddThemesToList(theme);
                }
                templatesInfoModelsList.add(0,recentTemplatesInfo);
            }
        } else {
            ArrayList<RecentsInfoModel> recentsInfoList = FTTemplateUtil.getInstance().getRecentPapersDummy();

            boolean  isNullOrEmpty =   FTTemplateUtil.getInstance().isNullOrEmpty(recentsInfoList);
            Log.d("TemplatePickerV2"," checkRecentThemesDummy recentsInfoList isNullOrEmpty:: "+isNullOrEmpty);

            if (!isNullOrEmpty) {
                TemplatesInfoModel recentTemplatesInfo = new TemplatesInfoModel();
                recentTemplatesInfo.set_categoryName("Recent");
                for (int i=0;i<recentsInfoList.size();i++) {
                    Log.d("TemplatePickerV2"," checkRecentThemesDummy get_packName:: "+recentsInfoList.get(i).get_packName() +
                            " get_categoryName:: "+recentsInfoList.get(i).get_categoryName()+" get_thumbnailURLPath:: "+recentsInfoList.get(i).get_thumbnailURLPath());

                    FTNTheme theme  = FTNTheme.theme(FTNThemeCategory.getUrl(recentsInfoList.get(i).get_packName()));
                    theme.categoryName              = recentsInfoList.get(i).get_categoryName();
                    theme.thumbnailURLPath          = recentsInfoList.get(i).get_thumbnailURLPath();
                    theme.themeBgClrName            = recentsInfoList.get(i).getThemeBgClrName();
                    theme.themeBgClr                = recentsInfoList.get(i).getThemeBgClr();
                    theme.packName                  = recentsInfoList.get(i).get_packName();
                    theme.themeName                 = recentsInfoList.get(i).get_themeName();
                    /*theme.themeBgClrName          = recentsInfoList.get(i).get_dairyEndDate();
                    theme.themeBgClrName            = recentsInfoList.get(i).get_dairyStartDate();*/
                    theme.verticalLineColor         = recentsInfoList.get(i).getVerticalLineColor();
                    theme.horizontalLineColor       = recentsInfoList.get(i).getHorizontalLineColor();
                    theme.horizontalSpacing         = recentsInfoList.get(i).getHorizontalSpacing();
                    theme.verticalSpacing           = recentsInfoList.get(i).getVerticalSpacing();
                    theme.width                     = recentsInfoList.get(i).getWidth();
                    theme.height                    = recentsInfoList.get(i).getHeight();
                    theme.bitmap                    = FTTemplateUtil.getInstance().StringToBitMap(recentsInfoList.get(i).get_themeBitmapInStringFrmt());
                    recentTemplatesInfo.AddThemesToList(theme);
                }
                templatesInfoModelsList.add(0,recentTemplatesInfo);
            }
        }
    }


    private ArrayList<String> templatesCategoryNamesFromDownloadsDummy(FTNThemeCategory.FTThemeType _coverOrPaperType)
    {
        ArrayList<String> _packNamesInDownloads = new ArrayList<>();
        File themeDir = new File(FTConstants.DOWNLOADED_PAPERS_PATH2);
        String[] downloadedTemplatesDirectory = null;
        if (_coverOrPaperType == FTNThemeCategory.FTThemeType.COVER) {
            themeDir = new File(FTConstants.DOWNLOADED_COVERS_PATH);
        } else {
            themeDir = new File(FTConstants.DOWNLOADED_PAPERS_PATH2);
        }
        downloadedTemplatesDirectory = themeDir.list();
        Log.d("TemplatePickerV2","Manikanth packName in Downloads _coverOrPaperType:: "+_coverOrPaperType+
                " themeDir.list():: "+themeDir.list());

        if (downloadedTemplatesDirectory != null) {
            // dir does not exist or is not a directory
            for (int i=0; i<downloadedTemplatesDirectory.length; i++) {
                // Get filename of file or directory
                String filename = downloadedTemplatesDirectory[i];
                Log.d("TemplatePickerV2","Manikanth packName in Downloads Folder:: "+filename);

                _packNamesInDownloads.add(filename);
               /* if (themeName.equalsIgnoreCase(filename))
                {
                    FTNTheme theme  = FTNTheme.theme(FTNThemeCategory.getUrl(themeName));
                    Log.d("TemplatePickerV2"," isDownloadTheme:: "+theme.isDownloadTheme);
                    return theme;
                }*/
            }

            return _packNamesInDownloads;
        }
        return null;
    }


    private ArrayList<String> templatesCategoryNamesFromAssetsDummy(FTNThemeCategory.FTThemeType _coverOrPaperType)
    {
        ArrayList<String> _packNamesInAssets = new ArrayList<>();
        AssetManager assetManager = FTApp.getInstance().getAssets();
        try {
            String[] stockTemplatesDirectory = assetManager.list("stockPapers");
            if (_coverOrPaperType == FTNThemeCategory.FTThemeType.COVER) {
                stockTemplatesDirectory = assetManager.list("stockCovers");
            } else {
                stockTemplatesDirectory = assetManager.list("stockPapers");
            }
            if (stockTemplatesDirectory != null) {
                // dir does not exist or is not a directory
                for (int i=0; i<stockTemplatesDirectory.length; i++) {
                    // Get filename of file or directory
                    String filename = stockTemplatesDirectory[i];
                    Log.d("TemplatePickerV2","Manikanth packName in Asstes Folder:: "+filename);
                    _packNamesInAssets.add(filename);
                }
            }

            return _packNamesInAssets;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private FTNTheme templatesCategoryNamesFromAssets(String themeName, FTNThemeCategory.FTThemeType _coverOrPaperType)
    {
        AssetManager assetManager = FTApp.getInstance().getAssets();
        try {
            String[] stockTemplatesDirectory = assetManager.list("stockPapers");
            if (_coverOrPaperType == FTNThemeCategory.FTThemeType.COVER) {
                stockTemplatesDirectory = assetManager.list("stockCovers");
            } else {
                stockTemplatesDirectory = assetManager.list("stockPapers");
            }
            if (stockTemplatesDirectory != null) {
                // dir does not exist or is not a directory
                for (int i=0; i<stockTemplatesDirectory.length; i++) {
                    // Get filename of file or directory
                    String filename = stockTemplatesDirectory[i];
                    if (themeName.equalsIgnoreCase(filename))
                    {
                        FTNTheme theme  = FTNTheme.theme(FTNThemeCategory.getUrl(themeName));
                        return theme;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void checkCustomThemesDummy(ArrayList<TemplatesInfoModel> templatesInfoModelsList, FTNThemeCategory.FTThemeType type) {
        String customThemesDirPath = FTConstants.CUSTOM_PAPERS_PATH;

        if (type == FTNThemeCategory.FTThemeType.COVER) {
            customThemesDirPath = FTConstants.CUSTOM_COVERS_PATH;
        } else {
            customThemesDirPath = FTConstants.CUSTOM_PAPERS_PATH;
        }
        File customThemesDir = new File(customThemesDirPath);
        if (customThemesDir.exists() && customThemesDir.isDirectory()) {
            customThemesDummy(templatesInfoModelsList,type);
        } else {

            TemplatesInfoModel templatesInfoModel = new TemplatesInfoModel();
            templatesInfoModel.set_categoryName("Custom");
            ArrayList<FTNTheme> ftnThemeArrayList = new ArrayList<>();
            FTNTheme ftnTheme = new FTNTheme();
            ftnTheme.themeName = FTApp.getInstance().
                    getApplicationContext().
                    getResources().
                    getString(R.string.template_custom_theme);
            if (type == FTNThemeCategory.FTThemeType.COVER) {
                ftnTheme.ftThemeType = FTNThemeCategory.FTThemeType.COVER;
            } else {
                ftnTheme.ftThemeType = FTNThemeCategory.FTThemeType.PAPER;
            }
            ftnTheme.categoryName = "Custom";
            ftnTheme.isCustomTheme = true;
            ftnTheme.dynamicId = 2;
            ftnTheme.isLandscape = false;
            ftnThemeArrayList.add(ftnTheme);
            templatesInfoModel.set_themeseList(ftnThemeArrayList);
            templatesInfoModelsList.add(templatesInfoModel);
        }
    }

    private void customThemesDummy(ArrayList<TemplatesInfoModel> templatesInfoModelsList, FTNThemeCategory.FTThemeType type) {
        String customThemesDirPath = null;
        TemplatesInfoModel templatesInfoModel = new TemplatesInfoModel();
        ArrayList<FTNTheme> customThemesLocal = new ArrayList<>();
        if (type == FTNThemeCategory.FTThemeType.COVER) {
            customThemesDirPath = FTConstants.CUSTOM_COVERS_PATH;
        } else {
            customThemesDirPath = FTConstants.CUSTOM_PAPERS_PATH;
        }

        File customThemesDir = new File(customThemesDirPath);
        if (customThemesDir.exists() && customThemesDir.isDirectory()) {
            for (File eachThemeDir : customThemesDir.listFiles()) {
                if (eachThemeDir.exists() && eachThemeDir.isDirectory()) {
                    String themeName = eachThemeDir.getName();
                    FTUrl url = getUrl(themeName, "customThemes");
                    if (!url.getPath().equals("")) {
                        FTNTheme theme = FTNTheme.theme(url);
                        if (url.getPath().contains("custom")) {
                            theme.isCustomTheme = true;
                        }

                        theme.themeName     = themeName;
                        theme.categoryName  = "Custom";
                        theme.isLandscape   = false;
                        theme.bitmap = theme.themeThumbnail(FTApp.getInstance().getApplicationContext());
                        theme.setCategoryName(FTApp.getInstance().getApplicationContext().getString(R.string.custom));
                        customThemesLocal.add(theme);
                    }
                }
            }

            FTNTheme ftnTheme = new FTNTheme();
            ftnTheme.categoryName   = "Custom";
            ftnTheme.themeName      = FTApp.getInstance().getApplicationContext().getResources().getString(R.string.template_custom_theme);
            if (type == FTNThemeCategory.FTThemeType.COVER) {
                ftnTheme.ftThemeType    = FTNThemeCategory.FTThemeType.COVER;
            } else {
                ftnTheme.ftThemeType    = FTNThemeCategory.FTThemeType.PAPER;
            }

            ftnTheme.isCustomTheme  = true;
            customThemesLocal.add(ftnTheme);

            Collections.reverse(customThemesLocal);

            templatesInfoModel.set_categoryName("Custom");
            templatesInfoModel.set_themeseList(customThemesLocal);
            templatesInfoModelsList.add(templatesInfoModel);
        }

    }

    public ArrayList<TemplatesInfoModel> getTemplatesInfo(FTNThemeCategory.FTThemeType _ftThemeType) {
        TemplatesInfoModel templatesModel;
        TemplatesInfoModel customTemplatesModel;
        ArrayList<TemplatesInfoModel> updatedTemplatesInfo = new ArrayList<>();

        ArrayList<String>     packNamesFromAssets              = new ArrayList<>();
        ArrayList<String>     packNamesFromDownloads           = new ArrayList<>();
        boolean packNamesFromAssetsIsNullOrEmpty;
        boolean packNamesFromDownloadsIsNullOrEmpty;

        checkRecentThemesDummy(_ftThemeType,updatedTemplatesInfo);

        File plist = new File(FTConstants.DOCUMENTS_ROOT_PATH + "/Library/" + getThemesPlist());
        try {
            FileInputStream inputStream = new FileInputStream(plist);
            NSDictionary dictionary = (NSDictionary) PropertyListParser.parse(inputStream);
            NSDictionary packsDict = null;

            if (_ftThemeType == FTNThemeCategory.FTThemeType.COVER) {
                packsDict  = (NSDictionary) dictionary.objectForKey("covers");
            } else if (_ftThemeType == FTNThemeCategory.FTThemeType.PAPER) {
                packsDict  = (NSDictionary) dictionary.objectForKey("papers");
            }

            NSArray categories      = (NSArray) packsDict.objectForKey("categories");

            ArrayList<FTColorVariants> ftColorVariantsArrayList = new ArrayList<>();
            ArrayList<FTLineTypes>     ftLineTypes              = new ArrayList<>();

            FTColorVariants mFTColorVariants;
            FTTemplateColors mFTTemplateColors;

            FTLineTypes mlineTypes;

            ArrayList<FTTemplateColors> ftTemplateColorsArrayList;

            ArrayList<String> _packsInsideAsstesFolder = templatesCategoryNamesFromAssetsDummy(_ftThemeType);
            ArrayList<String> _packsInsideDownloadsFolder = templatesCategoryNamesFromDownloadsDummy(_ftThemeType);

            packNamesFromAssetsIsNullOrEmpty = isNullOrEmpty(_packsInsideAsstesFolder);
            packNamesFromDownloadsIsNullOrEmpty= isNullOrEmpty(_packsInsideDownloadsFolder);

            if (!packNamesFromAssetsIsNullOrEmpty) {
                packNamesFromAssets.addAll(_packsInsideAsstesFolder);
            }

            if (!packNamesFromDownloadsIsNullOrEmpty) {
                packNamesFromDownloads.addAll(_packsInsideDownloadsFolder);
            }

            Log.d("TemplatePickerV2","Manikanth packName packNamesFromAssetsIsNullOrEmpty:: "
                    +packNamesFromAssetsIsNullOrEmpty +" packNamesFromDownloadsIsNullOrEmpty "
                    +packNamesFromDownloadsIsNullOrEmpty);

            for (int c = 0; c < categories.count(); c++)
            {
                templatesModel              = new TemplatesInfoModel();

                NSDictionary categoryDict   = (NSDictionary) categories.objectAtIndex(c);
                NSArray themes              = (NSArray)categoryDict.objectForKey("themes");
                String categoryName         = categoryDict.objectForKey("category_name").toString();

                if (categoryDict.containsKey("customize_options")) {
                    FTCustomizeOptions ftCustomizeOptions = new FTCustomizeOptions();
                    NSDictionary customize_options = (NSDictionary) categoryDict.objectForKey("customize_options");

                    NSArray color_variants = (NSArray) customize_options.objectForKey("color_variants");

                    for (int d = 0; d < color_variants.count(); d++) {
                        templatesModel.setCustomizeOptions(true);
                        ftTemplateColorsArrayList = new ArrayList<>();
                        mFTColorVariants = new FTColorVariants();
                        NSDictionary color_variants_objects = (NSDictionary) color_variants.objectAtIndex(d);
                        NSString title = (NSString) color_variants_objects.objectForKey("title");
                        if (color_variants_objects.containsKey("colors")) {
                            NSArray colors = (NSArray) color_variants_objects.objectForKey("colors");

                            for (int j= 0 ; j<colors.count();j++) {
                                mFTTemplateColors = new FTTemplateColors();
                                NSDictionary color_objects = (NSDictionary) colors.objectAtIndex(j);
                                mFTTemplateColors.setColorHex(color_objects.objectForKey("colorHex").toString());
                                mFTTemplateColors.setColorName(color_objects.objectForKey("colorName").toString());
                                mFTTemplateColors.setHorizontalLineColor(color_objects.objectForKey("horizontalLineColor").toString());
                                mFTTemplateColors.setVerticalLineColor(color_objects.objectForKey("verticalLineColor").toString());
                                ftTemplateColorsArrayList.add(mFTTemplateColors);
                            }
                        }
                        mFTColorVariants.setmFTTemplateColors(ftTemplateColorsArrayList);
                        mFTColorVariants.setTitle(title.toString());
                        ftColorVariantsArrayList.add(mFTColorVariants);
                    }
                    ftCustomizeOptions.setmFTColorVariants(ftColorVariantsArrayList);
                    setfTColorVariants(ftColorVariantsArrayList);

                    //Line Type data
                    NSArray lineTypes = (NSArray) customize_options.objectForKey("lineTypes");

                    for (int d = 0; d < lineTypes.count(); d++) {
                        mlineTypes = new FTLineTypes();
                        NSDictionary lineTypes_objects = (NSDictionary) lineTypes.objectAtIndex(d);
                        NSNumber horizontalLineSpacing = (NSNumber) lineTypes_objects.objectForKey("horizontalLineSpacing");
                        NSNumber verticalLineSpacing   = (NSNumber) lineTypes_objects.objectForKey("verticalLineSpacing");

                        mlineTypes.setHorizontalLineSpacing(horizontalLineSpacing.intValue());
                        mlineTypes.setVerticalLineSpacing(verticalLineSpacing.intValue());
                        mlineTypes.setLineType(lineTypes_objects.objectForKey("lineType").toString());
                        ftLineTypes.add(mlineTypes);
                    }

                    ftCustomizeOptions.setmFTLineTypes(ftLineTypes);
                    setmFTLineTypes(ftLineTypes);

                } else {
                    templatesModel.setCustomizeOptions(false);
                }

                for (int e = 0; e < themes.count(); e++)
                {
                    String packName                          = themes.objectAtIndex(e).toString();
                    Log.d("TemplatePickerV2","Manikanth packName in MasterPlist Folder:: "+packName +" status:: "+packNamesFromAssets.contains(packName));

                    if (!packNamesFromAssetsIsNullOrEmpty) {
                        if (packNamesFromAssets.contains(packName)) {
                            FTNTheme _matchedThemesFromAssetsInfo  = FTNTheme.theme(FTNThemeCategory.getUrl(packName));
                            if (_matchedThemesFromAssetsInfo != null) {
                                if (templatesModel != null) {
                                    _matchedThemesFromAssetsInfo.categoryName = categoryName;
                                    templatesModel.AddThemesToList(_matchedThemesFromAssetsInfo);
                                }
                            }
                        }
                    }

                    if (!packNamesFromDownloadsIsNullOrEmpty) {
                        if (packNamesFromDownloads.contains(packName)) {
                            FTNTheme _matchedThemesFromDownloadsInfo  = FTNTheme.theme(FTNThemeCategory.getUrl(packName));
                            _matchedThemesFromDownloadsInfo.isDownloadTheme = true;
                        if (_matchedThemesFromDownloadsInfo != null) {
                                if (templatesModel != null) {
                                    _matchedThemesFromDownloadsInfo.categoryName = categoryName;
                                    templatesModel.AddThemesToList(_matchedThemesFromDownloadsInfo);
                                }
                            }
                        }
                    }
                }

                boolean isNullOrEmpty = isNullOrEmpty(templatesModel.get_themeseList());
                if (!isNullOrEmpty)
                {
                    templatesModel.set_categoryName(categoryName);
                    updatedTemplatesInfo.add(templatesModel);
                }
            }

            checkCustomThemesDummy(updatedTemplatesInfo,_ftThemeType);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return updatedTemplatesInfo;
    }

    public static boolean isNullOrEmpty( final Collection< ? > c ) {
        return c == null || c.isEmpty();
    }

}
