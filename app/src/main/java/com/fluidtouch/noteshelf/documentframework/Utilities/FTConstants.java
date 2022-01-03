package com.fluidtouch.noteshelf.documentframework.Utilities;

import android.graphics.Color;

import androidx.core.content.ContextCompat;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.document.enums.NSTextAlignment;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo;
import com.fluidtouch.noteshelf2.R;

public class FTConstants {
    //FTNoteshelfDocument
    public static final float APP_SUPPORTED_MAX_DOC_VERSION = 6.0f;
    public static final String CN_ENDPOINT = "http://noteshelf.net/NS2_Store_China/";
    public static final String METADATA_FOLDER_NAME = "Metadata";
    public static final String PROPERTIES_PLIST = "Properties.plist";
    public static final String ANNOTATIONS_FOLDER_NAME = "Annotations";
    public static final String TEMPLATES_FOLDER_NAME = "Templates";
    public static final String RESOURCES_FOLDER_NAME = "Resources";
    public static final String DOCUMENT_INFO_FILE_NAME = "Document.plist";
    public static final String COVER_SHELF_IMAGE_NAME = "cover-shelf-image.png";
    public static final String COVER_SHELF_OVERLAY_IMAGE_NAME = "cover-band-image.png";
    public static final String RECOGNITION_INFO_FILE_NAME = "RecognitionInfo";
    public static final String RECOGNITION_FILES_FOLDER_NAME = "RecognitionFiles";
    public static final String VISION_RECOGNITION_INFO_FILE_NAME = "VisionRecognitionInfo";
    public static final String IMAGE_RECOGNITION_INFO_FILE_NAME = "ImageRecognitionInfo";
    public static final String PDF_FILE_EXT = "ns_pdf";
    public static final String DOC_VERSION = "6.0";
    public static final String DOCUMENTS_KEY = "documents";
    public static final String DOCUMENT_ID_KEY = "document_ID";
    public static final String DOCUMENT_VERSION_KEY = "document_Ver";
    public static final String DEVICE_ID = "device_ID";
    public static final String APP_VERSION = "app_version";
    public static final String SHELF_TITLE_OPTION = "shelf_title";
    public static final String PNG_EXTENSION = ".png";
    public static final String PDF_EXTENSION = ".pdf";
    public static final String NSA_EXTENSION = ".nsa";
    public static final String iOS_NOTESHELF_EXTENSION = ".noteshelf";
    public static final String ZIP_EXTENSION = ".zip";
    public static final String PLIST_EXTENSION = ".plist";
    public static final String NS_EXTENSION = ".ns_a";
    public static final String SHELF_EXTENSION = ".shelf";
    public static final String GROUP_EXTENSION = ".group";
    public static final String DAT_EXTENSION = ".dat";
    public static final String DEFAULT_SHELF_NAME = FTApp.getInstance().getString(R.string.my_notes);
    public static final String DEFAULT_COVER_THEME_NAME = "Orange.nsc";
    public static final String DEFAULT_PAPER_THEME_NAME = "Plain.nsp";
    /*public static final String DEFAULT_PAPER_THEME_URL =
            "thumbnail_"+"Plain"+"_"+ ScreenUtil.getScreenWidth(FTApp.getInstance().getApplicationContext())+"_"+ScreenUtil.getScreenHeight(FTApp.getInstance().getApplicationContext())+"_"+"Default"+"_"+"Default"+"_land_.jpg";*/
    public static final String DEFAULT_PAPER_THEME_URL = "thumbnail_Plain_"+ FTSelectedDeviceInfo.selectedDeviceInfo().getPageWidth()+"_"
            +FTSelectedDeviceInfo.selectedDeviceInfo().getPageHeight()+"_"+FTSelectedDeviceInfo.selectedDeviceInfo().getThemeBgClrName()
            +"_"+FTSelectedDeviceInfo.selectedDeviceInfo().getLineType()+"_"+FTSelectedDeviceInfo.selectedDeviceInfo().getLayoutType()+".jpg";
    public static final String DEFAULT_COVER_THEME_URL = "stockCovers/Orange.nsc/thumbnail@2x.png";

    public static final String DEFAULT_THEME_TOOLBAR_COLOR = "#5ca7f7";
    public static final String DEFAULT_THEME_STATUS_BAR_COLOR = "#5ca7f7";
    public static final String DEFAULT_INPUT_TEXT_COLOR = "000000";
    public static final boolean DEFAULT_IS_SHOWING_DATE = true;
    public static final boolean DEFAULT_IS_SORTING_WITH_DATE = true;
    public static final int statusBarColor = Color.BLACK;
    public static final String DOCUMENTS_ROOT_PATH = ContextCompat.getDataDir(FTApp.getInstance().getApplicationContext()).getPath();
    public static final String DOWNLOADED_COVERS_PATH = FTConstants.DOCUMENTS_ROOT_PATH + "/Library/covers/download/";
    public static final String DOWNLOADED_PAPERS_PATH = FTConstants.DOCUMENTS_ROOT_PATH + "/Library/papers/download/";
    public static final String DOWNLOADED_PAPERS_PATH2 = FTConstants.DOCUMENTS_ROOT_PATH + "/Library/papers_v2/download/";
    public static final String CUSTOM_COVERS_PATH = FTConstants.DOCUMENTS_ROOT_PATH + "/Library/covers/custom/";
    public static final String CUSTOM_PAPERS_PATH = FTConstants.DOCUMENTS_ROOT_PATH + "/Library/papers/custom/";
    public static final String COVER_FOLDER_NAME = "stockCovers";
    public static final String PAPER_FOLDER_NAME = "stockPapers"; 
    public static final String HW_DOWNLOAD_PATH = FTConstants.DOCUMENTS_ROOT_PATH + "/HandWriting/";
    public static final String RESTORE_DOWNLOAD_PATH = FTConstants.DOCUMENTS_ROOT_PATH + "/Restoring/";
    public static final String TEMP_FOLDER_PATH = DOCUMENTS_ROOT_PATH + "/Temp/";
    public static final String SUPPORT_LOG_FILE_PATH = DOCUMENTS_ROOT_PATH + "/UserFlow.log";

    //InputTextDefaults
    public static final int TEXT_DEFAULT_PADDING = 10;
    public static final int TEXT_DEFAULT_SIZE = 12;
    public static final int CUSTOM_TEXT_MAX_SIZE = 12;
    public static final int TEXT_DEFAULT_STYLE = -1;
    public static final boolean TEXT_DEFAULT_UNDERLINE = false;
    public static final String TEXT_DEFAULT_FONT_FAMILY = "roboto";
    public static final int TEXT_DEFAULT_COLOR = Color.parseColor("#000000");
    public static final NSTextAlignment TEXT_DEFAULT_ALIGNMENT = NSTextAlignment.NSTextAlignmentLeft;
    public static final String SYSTEM_FONTS_PATH = "system/fonts/";
    //Recognition
    public static final boolean ENABLE_HW_RECOGNITION = true;
    public static final boolean ENABLE_VISION_RECOGNITION = false;

    //plist
    private static final String DEFAULT_COLORS_PLIST = "DefaultCustomColors.plist";
    public static final String DEFAULT_COLORS_PLIST_RELATIVE_PATH = "Library/" + DEFAULT_COLORS_PLIST;
    //https://noteshelfv2-public.s3.amazonaws.com
    public static String STORE_ENDPOINT = "https://s3.amazonaws.com/noteshelfv2-public/";
    public static float CURRENT_PAGE_SCALE = 1f;
    public static final String RECENT_PLIST = "Recent.plist";
    public static final String PINNED_PLIST = "Pinned.plist";

    //https://noteshelfv2-public.s3.amazonaws.com/store/v4/Themes%20Metadata/v8/themes_v8_en.plist
    public static String PLIST_ENDPOINT = "https://noteshelfv2-public.s3.amazonaws.com/store/v4/Themes%20Metadata/v8/themes_v8_en.plist";

    public static final int VIEWTYPE_GROUP = 0;
    public static final int VIEWTYPE_ROW_ITEM = 1;
    public static String HUAWEI_STORE_ENDPOINT = "https://ops-dra.agcstorage.link/v0/noteshelf-data-hdmvw/";


    //public static String HUAWEI_STORE_ENDPOINT = "https://ops-dra.agcstorage.link/v0/noteshelf-data-hdmvw/";

    public static final String KEY_OBJECT_THEME = "themeData";
    public static final String KEY_IS_NOT_SAVED_FUTURE = "isSavedFuture";
}