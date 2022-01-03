package com.fluidtouch.noteshelf.preferences;

import android.text.TextUtils;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.templatepicker.common.util.FTRecentlyDeletedTemplateInfo;
import com.fluidtouch.noteshelf2.BuildConfig;
import com.google.gson.Gson;
import com.fluidtouch.noteshelf.cloud.backup.webdav.FTWebDavCredentials;

import java.util.Date;

/**
 * Created by Sreenu on 23/08/18
 */
public class SystemPref extends FTBasePref {
    public static final String PREF_NAME = BuildConfig.APPLICATION_ID;
    public static final String SELECTED_THEME_STATUS_BAR_COLOR = "selectedThemeStatusBarColor";     //deafult: FTConstants.DEFAULT_THEME_STATUS_BAR_COLOR
    public static final String SELECTED_THEME_TOOLBAR_COLOR = "selectedThemeToolbarColor";          //deafult: FTConstants.DEFAULT_THEME_TOOLBAR_COLOR
    public static final String IS_SHOWING_DATE = "isShowingDate";                                   //Default: FTConstants.DEFAULT_IS_SHOWING_DATE
    public static final String RECENT_COVER_THEME_NAME = "recentCoverThemeName";                    //Default: FTConstants.DEFAULT_COVER_THEME_NAME
    public static final String RECENT_PAPER_THEME_NAME = "recentPaperThemeName";
    public static final String RECENT_COMMON_PAPER_THEME_NAME = "recentCommonPaperThemeName";    //Default: FTConstants.DEFAULT_PAPER_THEME_NAME
    public static final String QUICK_CREATE_PAPER_THEME_NAME = "quickCreatePaperThemeName";
    public static final String QUICK_CREATE_PAPER_THEME = "quickCreatePaperTheme"; //Default: FTConstants.DEFAULT_PAPER_THEME_NAME
    public static final String RECENT_INPUT_TEXT_COLOR = "recentInputTextColor";                    //Default: FTConstants.DEFAULT_INPUT_TEXT_COLOR
    public static final String CUSTOM_TEXT_COLORS = "customTextColors";                    //Default: FTConstants.DEFAULT_INPUT_TEXT_COLOR
    public static final String STYLUS_ENABLED = "stylusEnabled";                                    //Default: FTConstants.DEFAULT_INPUT_TEXT_COLOR
    public static final String STYLUS_PRESSURE_ENABLED = "stylusPressureEnabled";                   //Default: False
    public static final String WRITING_ANGLE = "writingAngle";
    public static final String DEFAULT_WRITING_STYLE = "right_bottom";
    public static final String EXPORT_FORMAT = "exportFormat";
    public static final String GROUP_DOCUMENT_URL = "groupDocumentURL";
    public static final String DOCUMENT_URL = "documentURL";
    public static final String FINDER_SHOWING_BOOKMARKED_PAGES = "isFinderShowingBookmarkedPages";
    public static final String FINDER_SHOWING_BOOKMARK_TITLES = "isFinderShowingBookmarkTitles";
    public static final String SELECTED_COVER_STYLE = "coverStyle";
    public static final String FAVORITE_PENS_CLICKED = "isFavoritePensClicked";
    public static final String FAVORITE_HIGHLIGHTER_CLICKED = "isFavoriteHighlighterClicked";
    public static final String HYPERLINKS_DISABLED = "isHyperlinksDisabled";
    public static final String BACKUPTHROUGHWIFI = "isBackUpThroughWifi";
    //Evernote
    public static final String EVERNOTE_NOTEBOOK_GUID = "enNotebookGUID";
    public static final String EVERNOTE_GLOBAL_ERROR = "enGlobalError";
    public static final String EVERNOTE_LAST_SYNC = "enLastSync";
    public static final String EVERNOTE_USERNAME = "enUsername";
    public static final String EVERNOTE_USER_UPLOAD_LIMIT = "enUserUploadLimit";
    public static final String EVERNOTE_USER_UPLOADED_SIZE = "enUserUploadedSize";
    //Recognition
    public static final String SELECTED_LANGUAGE = "currentLanguageCode";
    public static String UNSYNCED_DOCUMENT_UUID = "";
    public static String COUNTRY_CODE = "countryCode";
    public static String CONVERT_TO_TEXTBOX_FONT_TYPE = "CTTFontType";
    public static String CONVERT_TO_TEXT_LANGUAGE = "convertToTextLanguage";
    //eraser
    public static final String ERASE_ENTIRE_STROKE = "mPrefEraseEntireStrokeSwitch";                //Default: True
    //Privacy policy
    public static final String PRIVACY_POLICY_VERSION = "privacyPolicyVersion";
    //Samsung specific
    public static final String IS_FOR_SAMSUNG = "isForSamsung";
    public static final String SUPPORTS_FBR = "supportsFBR";  //True By default
    public static final String IS_FBR_SUPPORT_TESTING_DONE = "isFBRSupportTestingDone";
    //Cliparts
    public static final String IS_PIXABAY_SELECTED = "lastSelectedClipartProvider";
    public static final String LAST_SELECTED_PIXABAY_CATEGORY = "lastSelectedPixabayCategory";
    public static final String LAST_SELECTED_UNSPLASH_CATEGORY = "lastSelectedUnsplashCategory";
    //AddNew
    public static final String LAST_SELECTED_ADD_NEW_TAB = "lastSelectedAddNewTab";
    //Bookmarks
    public static final String LAST_SELECTED_BOOKMARK_COLOR = "lastSelectedBookmarkColor";
    public static final String LAST_BOOKMARK_TITLE_USED = "lastBookmarkTitleUsed";
    //Random cover
    public static final String RANDOM_COVER_DESIGN_ENABLED = "randomCoverDesign";

    public static final String IS_SPEN_AIR_ACTION_FEATURE_SHOWN = "randomCoverDesign";

    public static final String FIRST_TIME_INSTALLED_VERSION = "previousAppVersion";

    //  Template Picker
    public static final String SEARCH_QUERY = "searchQuery";
    public static final String THEME_TYPE = "themeType";
    public static final String LAST_SELECTED_PAPER = "lastSelectedPaper";
    public static final String SEARCH_ENABLED = "searchIconStatus";
    public static final String ENTERED_SEARCH_TEXT = "enteredSearchText";
    public static final String SELECTED_DEVICE_WIDTH = "selectedDeviceWidth";
    public static final String SELECTED_DEVICE_HEIGHT = "selectedDeviceHeight";
    public static final String LAST_SELECTED_TAB = "lastSelectedTab";
    public static final String LAST_SELECTED_TAB_POS = "lastSelectedTabPos";
    public static final String TEMPLATE_BG_MORE_CLR = "lastSelectedTemplateBgMoreClr";
    public static final String TEMPLATE_BG_CLR = "lastSelectedTemplateBgClr";
    public static final String TEMPLATE_IBFO_MASTERLIST = "TemplateInfoMasterList";
    public static final String TEMPLATE_RECENT_BASICS_BG_CLR = "recentBasicsBgClr";
    public static final String TEMPLATE_LINE_TYPE = "lastSelectedTemplateLineType";
    public static final String TEMPLATE_BG_CLR_MORE_VIEW = "lastSelectedTemplateBgClrMoreView";
    public static final String TEMPLATE_BG_CLR_MORE_POPUP_SELECTION_STATUS = "moreClrPopupSelectionStatus";
    public static final String TEMPLATE_LINE_TYPE_POSITION = "lineTypePosition";
    public static final String TEMPLATE_COLOUR_SELECTED_CLR_HEX = "colourSelectedPositionClrHex";
    public static final String TEMPLATE_COLOUR_SELECTED_CLR_HEX_MRE = "colourSelectedPositionClrHexMore";
    public static final String TEMPLATE_COLOUR_SELECTED_CLR_NAME = "colourSelectedPositionClrName";
    public static final String TEMPLATE_COLOUR_SELECTED_CLR_NAME_MRE = "colourSelectedPositionClrNameMore";
    public static final String TEMPLATE_LINES_HORIZONTAL_COLOUR_CLR_HEX = "horizontalLinesColourHexCode";
    public static final String TEMPLATE_LINES_HORIZONTAL_COLOUR_CLR_HEX_MRE = "horizontalLinesColourHexCodeMore";
    public static final String TEMPLATE_LINES_HORIZONTAL_LINE_SPACING = "horizontalLinesSpacing";
    public static final String TEMPLATE_LINES_VERTICAL_LINE_SPACING = "verticalLinesSpacing";
    public static final String TEMPLATE_LINES_VERTICAL_COLOUR_CLR_HEX = "verticalLinesColourHexCode";
    public static final String TEMPLATE_DEVICE_NAME = "templateDeviceName";
    public static final String TEMPLATE_MODEL_INFO = "templateModelInfo";
    public static final String TEMPLATE_LINES_VERTICAL_COLOUR_CLR_HEX_MRE = "verticalLinesColourHexCodeMore";
    public static final String TEMPLATE_LINE_TYPE_SELECTED = "templateLineTypeSelected";
    public static final String TEMPLATE_COLOR_SELECTED = "templateColorSelected";
    public static final String TYPE_OF_CLR_VIEW_SELECTED = "typeOfClrViewSelected";
    public static final String RECENTLY_SELECTED_PAPERS_LIST = "recentlySelectedPapersList";
    public static final String RECENTLY_SELECTED_COVERS_LIST = "recentlySelectedCoversList";
    public static final String ALL_TEMPLATES_INFO_LIST = "alltemplatesInfoList";
    public static final String RECENTLY_SELECTED_THUMBNAILS_LIST = "recentlySelectedThumbnailsList";
    public static final String RECENTLY_SELECTED_COVERS_THUMBNAILS_LIST = "recentlySelectedCoversThumbnailsList";
    public static final String RECENTLY_SELECTED_PAPERTHEME_NOTEBOOK_OPTIONS = "recentlySelectedPaperFromNoteBookOptions";
    public static final String RECENTLY_SELECTED_TEMP_PAPERTHEME_NOTEBOOK_OPTIONS = "recentlySelectedTempPaperFromNoteBookOptions";

    public static final String RECENTLY_SELECTED_COVERTHEME_NOTEBOOK_OPTIONS = "recentlySelectedCoverFromNoteBookOptions";
    public static final String RECENTLY_SELECTED_PAPERTHEME_QUICKCREATE_OPTIONS = "recentlySelectedPaperFromQuickCreateOptions";
    public static final String RECENT_BASIC_PAPER_THEMES = "recentBasicPaperThemes";
    public static final String RECENTLY_DELETED_PAPER_THEME = "recentlyDeletedPaperTheme";
    public static final String RECENTLY_DELETED_COVER_THEME = "recentlyDeletedCoverTheme";
    public static final String FILES_BEFORE_NEW_DOWNLOADS_PAPERS_LIST = "filesBeforeNewDownloadsPapersList";
    public static final String FILES_AFTER_NEW_DOWNLOADS_PAPERS_LIST = "filesAfterNewDownloadsPapersList";
    public static final String RECENT_COVER_THEME_URL = "recentCoverThemeURL";
    public static final String JOB_SCHEDULER_STARTED = "jobSchedulerStarted";
    public static final String RECENT_PAPER_THEME = "recentPaperTheme";
    public static final String RECENT_COVER_THEME = "recentCoverTheme";
    public static final String RECENT_PAPER_BITMAP = "recentPaperBitmap";
    public static final String CATEGORY_SELECTED_POSITION = "templateCategoryPosition";
    public static final String DIARY_CREATION_YEAR = "dairyCreationYear";
    public static final String LINE_TYPE_SELECTED = "lineTypeSelected";
    public static final String DID_PREVIOUSLY_SEARCHED = "didPreviouslySearched";
    public static final String DID_SHW_PREVIOUSLY_SEARCHED = "didSHWPreviouslySearched";
    public static final String IS_SHW_ENABLED = "isSHWEnabled";
    public static final String CURRENT_HW_REG = "currentHWReg";

    public static final String HAS_AGREED_PRIVACY_POLICY = "hasAgreedPrivacyPolicy";

    public static String PREDICTION_ENABLED = "Prediction_Enabled";

    public SystemPref init(String prefName) {
        setSharedPreferences(prefName);
        return this;
    }

    public int getLastGroupDocumentPosition() {
        return get(PrefKeys.LAST_GROUP_DOCUMENT_POSITION, -1);
    }

    public void saveLastGroupDocumentPosition(int position) {
        save(PrefKeys.LAST_GROUP_DOCUMENT_POSITION, position);
    }

    public int getLastDocumentPosition() {
        return get(PrefKeys.LAST_DOCUMENT_POSITION, -1);
    }

    public void saveLastDocumentPosition(int position) {
        save(PrefKeys.LAST_DOCUMENT_POSITION, position);
    }

    public String getGroupDocumentUrl() {
        return get(PrefKeys.GROUP_DOCUMENT_URL, "");
    }

    public void saveGroupDocumentUrl(String url) {
        save(PrefKeys.GROUP_DOCUMENT_URL, url);
    }

    public String getDocumentUrl() {
        return get(PrefKeys.DOCUMENT_URL, "");
    }

    public void saveDocumentUrl(String url) {
        save(PrefKeys.DOCUMENT_URL, url);
    }

    public int getThumbnailWidth(int defaultValue) {
        return get(PrefKeys.THUMBNAIL_WIDTH, defaultValue);
    }

    public void saveThumbnailWidth(int width) {
        save(PrefKeys.THUMBNAIL_WIDTH, width);
    }

    public String getDropBoxToken() {
        return get(PrefKeys.DROPBOX_ACCESS_TOKEN, "");
    }

    public void saveDropBoxToken(String accessToken) {
        save(PrefKeys.DROPBOX_ACCESS_TOKEN, accessToken);
    }

    public String getOneDriveToken() {
        return get(PrefKeys.ONE_DRIVE_ACCESS_TOKEN, "");
    }

    public void saveOneDriveToken(String accessToken) {
        save(PrefKeys.ONE_DRIVE_ACCESS_TOKEN, accessToken);
    }

    public int getBackUpType() {
        return get(PrefKeys.BACK_UP_TYPE, BackUpType.NONE.ordinal());
    }

    public void saveBackUpType(BackUpType backUpType) {
        save(PrefKeys.BACK_UP_TYPE, backUpType.ordinal());
    }

    public long getLastBackUpAt() {
        return get(PrefKeys.DRIVE_LAST_BACKUP + getBackUpType(), 0L);
    }

    public void saveLastBackUpAt(long timeInMillis) {
        save(PrefKeys.DRIVE_LAST_BACKUP + getBackUpType(), timeInMillis);
    }

    public String getBackupError() {
        return get(PrefKeys.DRIVE_BACKUP_ERROR + getBackUpType(), "");
    }

    public void saveBackupError(String errorMessage) {
        save(PrefKeys.DRIVE_BACKUP_ERROR + getBackUpType(), errorMessage);
    }

    public boolean isStylusEnabled() {
        return get(STYLUS_ENABLED, false);
    }

    public void saveStylusEnabled(boolean isStylusEnabled) {
        save(STYLUS_ENABLED, isStylusEnabled);
    }

    public String getRecentCollectionName() {
        return get(PrefKeys.RECENT_COLLECTION_NAME, FTConstants.DEFAULT_SHELF_NAME);
    }

    public void saveRecentCollectionName(String recentCollectionName) {
        save(PrefKeys.RECENT_COLLECTION_NAME, recentCollectionName);
    }

    public boolean isSortingWithDate() {
        return get(PrefKeys.IS_SORTING_WITH_DATE, FTConstants.DEFAULT_IS_SORTING_WITH_DATE);
    }

    public void saveSortingWithDate(boolean isSortingWithDate) {
        save(PrefKeys.IS_SORTING_WITH_DATE, isSortingWithDate);
    }

    public boolean isDefaultNotebookCreated() {
        return get(PrefKeys.IS_DEFAULT_NOTEBOOK_CREATED, false);
    }

    public void saveDefaultNotebookCreated(boolean isDefaultNotebookCreated) {
        save(PrefKeys.IS_DEFAULT_NOTEBOOK_CREATED, isDefaultNotebookCreated);
    }

    public Boolean isQuickAccessPanelEnabled() {
        return get(PrefKeys.IS_QUICK_ACCESS_PANEL_ENABLED, true);
    }

    public void saveQuickAccessPanelEnabled(boolean isQuickAccessPanelEnabled) {
        save(PrefKeys.IS_QUICK_ACCESS_PANEL_ENABLED, isQuickAccessPanelEnabled);
    }

    public Boolean isFinderEnabled() {
        return get(PrefKeys.IS_FINDER_ENABLED, true);
    }

    public void saveFinderEnabled(boolean isFinderEnabled) {
        save(PrefKeys.IS_FINDER_ENABLED, isFinderEnabled);
    }

    public enum BackUpType {
        NONE, DROPBOX, GOOGLE_DRIVE, ONE_DRIVE, WEBDAV;

        public String getBackUp() {
            switch (this) {
                case DROPBOX:
                    return "Dropbox";
                case GOOGLE_DRIVE:
                    return "Google Drive";
                default:
                    return "None";
            }
        }

        public static String getBackup(int ordinal) {
            switch (ordinal) {
                case 1:
                    return "Dropbox";
                case 2:
                    return "Google Drive";
                case 3:
                    return "One Drive";
                case 4:
                    return "WebDAV";
                default:
                    return "None";
            }
        }
    }

    public void saveDiaryRecentStartDate(Date startDate) {
        save(PrefKeys.DIARY_RECENT_START_DATE, startDate.getTime());
    }

    public Date getDiaryRecentStartDate() {
        return new Date(get(PrefKeys.DIARY_RECENT_START_DATE, new Date().getTime()));
    }

    public void saveDiaryRecentEndDate(Date endDate) {
        save(PrefKeys.DIARY_RECENT_END_DATE, endDate.getTime());
    }

    public Date getDiaryRecentEndDate() {
        return new Date(get(PrefKeys.DIARY_RECENT_END_DATE, new Date().getTime()));
    }

    public void spenAirActionFeatureShown(boolean shown) {
        save(IS_SPEN_AIR_ACTION_FEATURE_SHOWN, shown);
    }

    public boolean isSpenAirActionFeatureShown() {
        return get(IS_SPEN_AIR_ACTION_FEATURE_SHOWN, false);
    }

    public void saveWebDavCredentials(FTWebDavCredentials webDavCredentials) {
        if (webDavCredentials == null) {
            webDavCredentials = new FTWebDavCredentials();
        }
        save(PrefKeys.WEB_DAV_CREDENTIALS, new Gson().toJson(webDavCredentials));
    }

    public FTWebDavCredentials getWebDavCredentials() {
        String jsonString = get(PrefKeys.WEB_DAV_CREDENTIALS, null);
        FTWebDavCredentials webDavCredentials = TextUtils.isEmpty(jsonString) ? new FTWebDavCredentials() : new Gson().fromJson(jsonString, FTWebDavCredentials.class);
        return TextUtils.isEmpty(webDavCredentials.getServerAddress()) ? null : webDavCredentials;
    }

    public boolean hasAgreedPrivacyPolicy() {
        return FTApp.getPref().get(SystemPref.HAS_AGREED_PRIVACY_POLICY, false);
    }

    public boolean isPredictionEnabled() {
        return FTApp.getPref().get(SystemPref.PREDICTION_ENABLED, true);
    }

    public void savePredictionEnabled(boolean enabled) {
        FTApp.getPref().save(SystemPref.PREDICTION_ENABLED, enabled);
    }

    private static final class PrefKeys {
        private static final String LAST_GROUP_DOCUMENT_POSITION = "lastGroupDocumentPos";
        private static final String LAST_DOCUMENT_POSITION = "lastDocumentPos";
        private static final String GROUP_DOCUMENT_URL = "groupDocumentURL";
        private static final String DOCUMENT_URL = "documentURL";
        private static final String THUMBNAIL_WIDTH = "thumbnailWidth";
        private static final String DROPBOX_ACCESS_TOKEN = "dropboxAccessToken";
        private static final String BACK_UP_TYPE = "backUpType";
        private static final String DRIVE_LAST_BACKUP = "driveLastBackup";
        private static final String DRIVE_BACKUP_ERROR = "driveBackupError";
        private static final String ONE_DRIVE_ACCESS_TOKEN = "oneDriveBackupError";
        private static final String RECENT_COLLECTION_NAME = "recentCollectionName";                     //Default: FTConstants.DEFAULT_SHELF_NAME
        private static final String IS_SORTING_WITH_DATE = "isSortingWithDate";                          //Default: FTConstants.DEFAULT_IS_SORTING_WITH_DATE
        private static final String IS_DEFAULT_NOTEBOOK_CREATED = "DefaultNotebookCreated";
        private static final String IS_QUICK_ACCESS_PANEL_ENABLED = "isQuickAccessPanelEnabled";
        private static final String IS_FINDER_ENABLED = "isFinderEnabled";
        private static final String DIARY_RECENT_START_DATE = "diaryRecentStartDate";
        private static final String DIARY_RECENT_END_DATE = "diaryRecentEndDate";
        private static final String WEB_DAV_CREDENTIALS = "FTWebDavCredentials";
    }

//    public FTWritingStyle writingStyle() {
//        float writingAngle = get(SystemPref.WRITING_ANGLE, FTWritingStyle.rightBottom.writingAngle());
//        FTWritingStyle prevWritingStyle = FTWritingStyle.getWritingStyleForAngle(writingAngle);
//        return prevWritingStyle;
//    }
//
//    public void setWritingStyle(FTWritingStyle style) {
//        FTApp.getPref().save(SystemPref.WRITING_ANGLE, style.writingAngle());
//    }
}
