package com.fluidtouch.noteshelf.models.theme;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListParser;
import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.utils.AssetsUtil;
import com.fluidtouch.noteshelf.commons.utils.BitmapUtil;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.commons.utils.StringUtil;
import com.fluidtouch.noteshelf.document.enums.FTPageFooterOption;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.generator.FTAutoTemplateGenerationCallback;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.templatepicker.adapters.FTTemplateDetailedInfoAdapter;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTLineTypes;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTTemplateColors;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Locale;

public class FTNTheme  implements Serializable {
    static final String DYNAMIC_ID = "dynamic_id";
    static final String DYNAMIC_TEMPLATE_INFO = "dynamic_template_info";

    public int dynamicId = 0;
    public int bottomMargin = 0;
    public String template_id = "Modern";
    public FTUrl themeFileURL = new FTUrl("");
    public String categoryName = "";
    public boolean isLandscape = false;
    public FTNThemeCategory.FTThemeType ftThemeType = FTNThemeCategory.FTThemeType.COVER;
    public boolean isDownloadTheme = false;
    public boolean isCustomTheme = false;
    public boolean isBasicTheme = false;

    public FTUrl thumbnailURL;
    public String themeName = "";
    public int overlayType = 0;
    public String diplayName = themeName;
    public String packName;
    public int lineHeight;

    public FTPageFooterOption themeFooterOption = FTPageFooterOption.SHOW;
    public FTPageFooterOption footerOption = this.themeFooterOption;

    public String bitmapInStringFrmt;

    public Bitmap bitmap;
    public boolean isDeleted            = false;
    public boolean isDefaultTheme       = false;

    public FTUrl overlayImageURL;
    public String thumbnailURLPath      = FTConstants.TEMP_FOLDER_PATH+"TemplatesCache/"+FTConstants.DEFAULT_PAPER_THEME_URL;;
    public float width                  = 595.0f;
    public float height                 = 842.0f;
    public String themeClassName;
    public boolean isTablet;

    public int redClrValue              = 255;
    public int greenClrValue            = 255;
    public int blueClrValue             = 255;
    public int alphaValue               = 255;

    public String themeBgClrName        = "White";
    public String themeBgClr            = "#F7F7F2-1.0";
    public String horizontalLineColor   = "#000000-0.5";
    public String verticalLineColor     = "#000000-0.5";
    public int horizontalSpacing        = 34;
    public int verticalSpacing          = 34;
    public boolean isSavedForFuture     = true;

    FTSelectedDeviceInfo ftSelectedDeviceInfo = null;

    public void updateSelectedDeviceInfo() {
        if (null == ftSelectedDeviceInfo) {
            ftSelectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();
            Log.d("TemplatePicker==>","FTDynamicTemplateFormat Size getPageWidth::-"+ftSelectedDeviceInfo.getPageWidth()+
                    " getPageHeight::-"+ftSelectedDeviceInfo.getPageHeight());
        }
    }

    public boolean isLandscape() {
        return isLandscape;
    }

    public static boolean isTheThemeExists(String path) {
        if (path.contains("stock")) {
            return AssetsUtil.isAssetExists(path);
        }
        return FTFileManagerUtil.isFileExits(path);
    }

    public static boolean getLandscapeStatus() {
        FTNTheme theme = new FTNTheme();
        FTSelectedDeviceInfo ftSelectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();
        if (ftSelectedDeviceInfo.getLayoutType().toLowerCase().
                contains("port")) {
            theme.isLandscape = false;
        } else {
            theme.isLandscape = true;
        }
        return theme.isLandscape;
    }

    public void template(Context mContext,FTAutoTemplateGenerationCallback callback) {
        try {
            throw new Exception("subclass should override");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static FTNTheme theme(FTUrl url) {
        FTNTheme themeToReturn = new FTNTheme();
        String pathExtension = StringUtil.getFileExtension(url.getPath());
        String packName = StringUtil.getFileName(url.getPath());
        if (isTheThemeExists(url.getPath())) {

            if (pathExtension.equals("nsp")) {
                themeToReturn = FTNPaperTheme.theme(url);
                themeToReturn.themeFileURL = url;
                themeToReturn.packName = packName;
                themeToReturn.ftThemeType = FTNThemeCategory.FTThemeType.PAPER;
                themeToReturn.isLandscape   = getLandscapeStatus();
                FTSelectedDeviceInfo selectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();
                themeToReturn.width                     = selectedDeviceInfo.getPageWidth();
                themeToReturn.height                    = selectedDeviceInfo.getPageHeight();

            } else if (pathExtension.equals("nsc")) {
                themeToReturn = new FTNCoverTheme();
                themeToReturn.themeFileURL = url;
                themeToReturn.packName = packName;
                themeToReturn.ftThemeType = FTNThemeCategory.FTThemeType.COVER;
            }

            String metadataUrl = url.withAppendedPath("metadata.plist").getPath();
            if (isTheThemeExists(metadataUrl)) {
                if (url.getPath().contains("/download/")) {
                    themeToReturn.isDownloadTheme = true;
                }
                try {
                    Log.d("TemplatePicker==>","themeClassName url::-"+url.getPath());
                    InputStream inputStream = null;
                    if (metadataUrl.startsWith("stock")) {
                        inputStream = FTApp.getInstance().getCurActCtx().getAssets().open(metadataUrl);
                    } else {
                        inputStream = new FileInputStream(metadataUrl);
                    }

                    NSDictionary metadataDict = (NSDictionary) PropertyListParser.parse(inputStream);
                    String currentLang = Locale.getDefault().getLanguage();
                    if (Locale.getDefault().toLanguageTag().contains("zh-Hans")) {
                        currentLang = "zh-Hans";
                    } else if (Locale.getDefault().toLanguageTag().contains("zh-Hant")) {
                        currentLang = "zh-Hant";
                    }

                    //For DisplayName
                    String defaultDisplayStringKey = "display_name";
                    NSObject displayNameObject = metadataDict.get(defaultDisplayStringKey + "_" + currentLang);
                        if (displayNameObject != null) {
                        themeToReturn.themeName = displayNameObject.toString();
                    } else if (metadataDict.containsKey(defaultDisplayStringKey)) {
                        themeToReturn.themeName = metadataDict.get(defaultDisplayStringKey).toString();
                    }
                    Log.d("TemplatePicker==>","themeClassName themeName::-"+themeToReturn.themeName);

                    //For bgColor
                    String defaultbgColorStringKey = "bgColor";
                    NSObject bgColorObject = metadataDict.get(defaultbgColorStringKey + "_" + currentLang);
                    if (bgColorObject != null) {
                        themeToReturn.themeBgClr = bgColorObject.toString();
                    } else if (metadataDict.containsKey(defaultbgColorStringKey)) {
                        themeToReturn.themeBgClr = metadataDict.get(defaultbgColorStringKey).toString();
                    }

                    //For CategoryName
                    String defaultCatNameKey = "category_name";
                    NSObject categoryNameObject = metadataDict.get(defaultCatNameKey + "_" + currentLang);
                    if (categoryNameObject != null) {
                        themeToReturn.categoryName = categoryNameObject.toString();
                    } else if (metadataDict.containsKey(defaultCatNameKey)) {
                        themeToReturn.categoryName = metadataDict.get(defaultCatNameKey).toString();
                    }

                    if (metadataDict.containsKey("line_height")) {
                        themeToReturn.lineHeight = Integer.parseInt(String.valueOf(Math.round(Float.parseFloat(String.valueOf(metadataDict.get("line_height"))))));
                    }

                    if (metadataDict.containsKey("footer_option")) {
                        themeToReturn.footerOption = FTPageFooterOption.valueOf(metadataDict.get("footer_option").toString());
                    }

//                    if (metadataDict.containsKey("template_type")) {
//                        themeToReturn.documentType = Integer.parseInt(metadataDict.get("template_type").toString());
//                    }

//                    if (metadataDict.containsKey("startYear")) {
//                        themeToReturn.diaryStartYear = Integer.parseInt(metadataDict.get("startYear").toString());
//                    }

                    if (metadataDict.containsKey("dynamic_id")) {
                        themeToReturn.dynamicId = Integer.parseInt(metadataDict.get("dynamic_id").toString());
                    }
                    Log.d("TemplatePicker==>"," url getPath::-"+url.getPath()+" isTheThemeExists::-"+isTheThemeExists(url.getPath()) + " themeClassName containsKey dynamic_id::-"+metadataDict.containsKey("dynamic_id")+" themeToReturn.dynamicId::-"+themeToReturn.dynamicId);

                    if (metadataDict.containsKey("template_id")) {
                        Log.d("TemplatePicker==>"," template_id FTNTheme If::-"+metadataDict.get("template_id").toString());
                        themeToReturn.template_id = metadataDict.get("template_id").toString();
                    } else {
                        Log.d("TemplatePicker==>"," template_id FTNTheme Else::-");
                    }
                    /*if (metadataDict.containsKey("width") && metadataDict.containsKey("height")) {
                        int width = Integer.parseInt(metadataDict.get("width").toString());
                        int height = Integer.parseInt(metadataDict.get("height").toString());
                        themeToReturn.isLandscape = width > height;
                    }*/

                    if (metadataDict.containsKey("overlay_type")) {
                        themeToReturn.overlayType = Integer.parseInt(metadataDict.get("overlay_type").toString());
                    }

                    Log.d("TemplatePicker==>","themeClassName pathExtension::-"+pathExtension.equals("nsp"));

                    if (pathExtension.equals("nsp")) {
                        if (metadataDict.containsKey("dynamic_template_info")) {
                            NSDictionary dynamicTemplateInfo = (NSDictionary) metadataDict.objectForKey("dynamic_template_info");
                            Log.d("TemplatePicker==>","themeClassName containsKey::-"+dynamicTemplateInfo.containsKey("themeClassName"));
                            if (dynamicTemplateInfo.containsKey("themeClassName")) {
                                Log.d("TemplatePicker==>","themeClassName themeClassName::-"+dynamicTemplateInfo.objectForKey("themeClassName"));
                                themeToReturn.themeClassName = dynamicTemplateInfo.get("themeClassName").toString();
                            }

                            if (dynamicTemplateInfo.containsKey("bottomMargin")) {
                                themeToReturn.bottomMargin = Integer.parseInt(dynamicTemplateInfo.get("bottomMargin").toString());
                            }

                            if (dynamicTemplateInfo.containsKey("horizontalSpacing")) {
                                themeToReturn.horizontalSpacing = Integer.parseInt(dynamicTemplateInfo.get("horizontalSpacing").toString());
                            }

                            if (dynamicTemplateInfo.containsKey("verticalSpacing")) {
                                themeToReturn.verticalSpacing = Integer.parseInt(dynamicTemplateInfo.get("verticalSpacing").toString());
                            }

                            if (dynamicTemplateInfo.containsKey("horizontalLineColor")) {
                                themeToReturn.horizontalLineColor = dynamicTemplateInfo.get("horizontalLineColor").toString();
                            }

                          /*  if (dynamicTemplateInfo.containsKey("horizontalLineColor")) {
                                themeToReturn.horizontalLineColor = "#CCCCCC-1.0";
                            }*/

                            if (dynamicTemplateInfo.containsKey("verticalLineColor")) {
                                themeToReturn.verticalLineColor = dynamicTemplateInfo.get("verticalLineColor").toString();
                            }

                            Log.d("TemplatePicker==>"," BottomMargin::-"+themeToReturn.bottomMargin);
                        }
                    }

                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                themeToReturn.isCustomTheme = true;
            }
        } else {
            Log.d("TemplatePicker==>","FTNTheme theme::-"+url.getPath());
        }

        if (themeToReturn.getClass().toString().contains("FTNTheme")) {
            Log.d("TemplatePicker==>","Sub FTNTheme theme TRUE::-"+url.getPath());
        }
        return themeToReturn;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        if (categoryName.toLowerCase().contains("transparent"))
            this.overlayType = 1;
        this.categoryName = categoryName;
    }

    public FTUrl themeThumbnailURL() {
        FTUrl thumbURL = null;
        float screenScale = 1;
        if (screenScale == 1) {
            thumbURL = FTUrl.withAppendedPath(this.themeFileURL, "thumbnail.png");
        } else {
            int screenIntValue = (int) screenScale;
            FTUrl thumbPath = FTUrl.withAppendedPath(this.themeFileURL, "thumbnail@" + screenIntValue + "x.png");

            while ((screenIntValue > 0) && (!new File(thumbPath.getPath()).exists())) {
                screenIntValue = screenIntValue - 1;
                thumbPath = FTUrl.withAppendedPath(this.themeFileURL, "thumbnail@" + screenIntValue + "x.png");
            }

            if (new File(thumbPath.getPath()).exists()) {
                thumbURL = thumbPath;
            }
        }

        if (thumbURL == null) {
            if (this instanceof FTNPaperTheme) {
                thumbURL = AssetsUtil.getUri("default_paper_image", "png");
            } else {
                thumbURL = AssetsUtil.getUri("default_cover_image", "png");
            }
        }
        return thumbURL;
    }

    FTUrl themeOverlayImageURL() {
        FTUrl overlayImageURL = null;

        float screenScale = 1;
        if (screenScale == 1) {
            overlayImageURL = FTUrl.withAppendedPath(this.themeFileURL, "overlay.png");
        } else {
            int screenIntValue = (int) screenScale;
            FTUrl overlayImagePath = FTUrl.withAppendedPath(this.themeFileURL, "overlay@" + screenIntValue + "x.png");

            while ((screenIntValue > 0) && (!new File(overlayImagePath.getPath()).exists())) {
                screenIntValue = screenIntValue - 1;
                overlayImagePath = FTUrl.withAppendedPath(this.themeFileURL, "overlay@" + screenIntValue + "x.png");
            }

            if (overlayImagePath != null && FTFileManagerUtil.isFileExits(Uri.parse(overlayImagePath.getPath()))) {
                overlayImageURL = overlayImagePath;
            }
        }
        return overlayImageURL;
    }

    public Bitmap themeThumbnail(Context context) {
        try {
            throw new Exception("subclass should override");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    Bitmap getOverlayImage() {
        FTUrl thumbURL = this.themeOverlayImageURL();
        if (thumbURL != null) {
            this.overlayImageURL = thumbURL;
            return BitmapUtil.getBitmap(Uri.parse(thumbURL.getPath()));
        }
        return null;
    }

    //MARK:- Equatable
    public FTUrl themeTemplateURL() {
        try {
            throw new Exception("subclass should override");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return FTUrl.parse("");
    }

    public void deleteTemplate() {
        String path = "";
        if (this instanceof FTNPaperTheme) {
            if (isDownloadTheme) {
                path = FTConstants.DOWNLOADED_PAPERS_PATH2;
            } else {
                path = FTConstants.CUSTOM_PAPERS_PATH;
            }
        } else if (this instanceof FTNCoverTheme) {
            if (isDownloadTheme) {
                path = FTConstants.DOWNLOADED_COVERS_PATH;
            } else {
                path = FTConstants.CUSTOM_COVERS_PATH;
            }
        }
        File packFolder = new File(path, packName);
        FTFileManagerUtil.deleteRecursive(packFolder);
        isDeleted = true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FTNTheme) {
            FTNTheme inputTheme = (FTNTheme) obj;
            if (inputTheme.themeTemplateURL().equals(this.themeTemplateURL())) {
                return true;
            }
        }

        return false;
    }

    public boolean isTemplate() {
        return !(isDownloadTheme || isCustomTheme);
    }

    public FTNPaperTheme basicTemplatePDFGenerated(FTUrl pdfFileURL, Context mContext) {
        try {
            throw new Exception("subclass should override");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void themeThumbnailOnCallBack(Context mContext, FTNTheme ftnTheme, FTTemplateDetailedInfoAdapter callBack,
                                         FTTemplateDetailedInfoAdapter.ThemeViewHolder childViewHolder/*,
                                         FTAutoTemplateGenerationCallback callback*/) {
        try {
            throw new Exception("subclass should override"+ftnTheme.getClass()+
                    " themeFileURL::-"+ftnTheme.themeFileURL.getPath()+
                    " thumbnailURLPath::-"+ftnTheme.thumbnailURLPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public Bitmap themeThumbnailOnCallBack(Context mContext,
                                           FTNTheme ftnTheme,
                                           FTLineTypes lineInfo,
                                           FTTemplateColors colorInfo,
                                           boolean isLandscape,
                                           FTTemplateDetailedInfoAdapter callBack,
                                           FTTemplateDetailedInfoAdapter.ThemeViewHolder childViewHolder) {
        try {
            throw new Exception("subclass should override"+ftnTheme.getClass()+
                    " themeFileURL::-"+ftnTheme.themeFileURL.getPath()+
                    " thumbnailURLPath::-"+ftnTheme.thumbnailURLPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public FTSelectedDeviceInfo selectedDeviceInfo() {
        if(null != ftSelectedDeviceInfo) {
         return ftSelectedDeviceInfo;
        }

        return FTSelectedDeviceInfo.selectedDeviceInfo();
    }

    public Bitmap addWhiteBorder(Bitmap bmp, int borderSize) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.parseColor("#1C000000"));
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }

    public Bitmap addBlueBorder(Bitmap bmp, int borderSize) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.parseColor("#5377F8"));
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }


}