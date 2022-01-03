package com.fluidtouch.noteshelf.store.model;

import android.net.Uri;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf2.BuildConfig;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class FTStorePackItem {

    private static final String rootUrl = (BuildConfig.FLAVOR.contains("china") || FTApp.getInstance().isChinaRegion()) ? FTConstants.HUAWEI_STORE_ENDPOINT + "store/v4/Theme Packs/" : FTConstants.STORE_ENDPOINT + "store/v4/Theme Packs/";
//    private static final String rootUrl = FTConstants.HUAWEI_STORE_ENDPOINT + "store/v3/Theme Packs/";

    @SerializedName("theme_display_name")
    String name;
    @SerializedName("theme_subtitle")
    String subtitle;
    @SerializedName("theme_filename")
    String fileName;
    @SerializedName("version_no")
    int version = 1;
    @SerializedName("supportsLocalizedMetadata")
    boolean supportsLocalizedMetadata;
    @SerializedName("banner")
    String bannerImageName;
    @SerializedName("theme_token")
    String themeToken;
    @SerializedName("banner_token")
    String bannerToken;
    @SerializedName("icon_large_token")
    String iconLargeToken;
    @SerializedName("icon_small_token")
    String iconSmallToken;
    @SerializedName("info_token")
    String infoToken;

    String searchkeys;
    String themePackInfo;
    String themeUpdatesInfo;
    String dialogDescription;
    String updateText;
    ArrayList<String> supported_devices = new ArrayList<>();
    ArrayList<String> previewTokens = new ArrayList<>();
    boolean isDownloaded = false;

    private String getRootURL() {
        String countryCode =  FTApp.getPref().get(SystemPref.COUNTRY_CODE,"en");
        if (countryCode.equalsIgnoreCase("cn")) {
            return URIBuilder();
        }
        return URIBuilder();
    }

    private Uri.Builder baseURIBuilder() {
        Uri.Builder builder = new Uri.Builder();
        if (FTApp.getInstance().isChinaRegion()) {
            builder.scheme("https")
                    .authority("ops-dra.agcstorage.link")
                    .appendPath("v0")
                    .appendPath("noteshelf-data-hdmvw")
                    .appendPath("store/v4/Theme Packs");
                    /*.appendPath("v4")
                    .appendPath("Theme Packs");*/
        } else {
            builder.scheme("https")
                    .authority("s3.amazonaws.com")
                    .appendPath("noteshelfv2-public")
                    .appendPath("store")
                    .appendPath("v4")
                    .appendPath("Theme Packs");
        }

        return builder;

    }
    private String URIBuilder() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("s3.amazonaws.com")
                .appendPath("store")
                .appendPath("v4")
                .appendPath("Theme Packs")
                .appendPath("fileName")
                .appendPath("icon_large.jpg");
        String myUrl = builder.build().toString();
        return myUrl;
    }

    public String getDialogDescription() {
        return dialogDescription;
    }

    public void setDialogDescription(String dialogDescription) {
        this.dialogDescription = dialogDescription;
    }

    public String getUpdateText() {
        return updateText;
    }

    public void setUpdateText(String updateText) {
        this.updateText = updateText;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getBannerImageName() {
        return bannerImageName;
    }

    public void setBannerImageName(String bannerImageName) {
        this.bannerImageName = bannerImageName;
    }

    public String getSearchkeys() {
        return searchkeys;
    }

    public void setSearchkeys(String searchkeys) {
        this.searchkeys = searchkeys;
    }

    public String getThemePackInfo() {
        return themePackInfo;
    }

    public void setThemePackInfo(String themePackInfo) {
        this.themePackInfo = themePackInfo;
    }

    public String getThemeUpdatesInfo() {
        return themeUpdatesInfo;
    }

    public void setThemeUpdatesInfo(String themeUpdatesInfo) {
        this.themeUpdatesInfo = themeUpdatesInfo;
    }

    public String getBannerImage() {
        /*return baseURIBuilder().appendPath(fileName)
                .appendPath("banner.jpg"+ (isForChinese() ? "" : "?token=" + bannerToken))
                .build().toString();*/
        //return "https://ops-dra.agcstorage.link/v0/noteshelf-data-hdmvw/store/v4/Theme%20Packs/Productivity%20Pack/banner.jpg%3Ftoken%3D690f74f0-a302-492c-88da-2948fe9e69e9";
        /*return baseURIBuilder().appendPath(fileName)
                .appendPath("banner.jpg"+ (!FTApp.getInstance().isChinaRegion() ? "" : "?token=" + bannerToken))
                .build().toString();*/

        //
        return rootUrl + fileName + "/banner.jpg" + (!FTApp.getInstance().isChinaRegion() ? "" : "?token=" + bannerToken);
    }

    public String getMediumImage() {
        /*return baseURIBuilder().appendPath(fileName)
                .appendPath("icon_large.jpg"+ (!FTApp.getInstance().isChinaRegion() ? "" : "?token=" + iconLargeToken))
                .build().toString();*/
        return rootUrl + fileName + "/icon_large.jpg" + (!FTApp.getInstance().isChinaRegion() ? "" : "?token=" + iconLargeToken);
    }

    public String getSmallImage() {
        /*return baseURIBuilder().appendPath(fileName)
                .appendPath("icon_small.jpg" + (!FTApp.getInstance().isChinaRegion() ? "" : "?token=" + iconSmallToken))
                .build().toString();*/
        return rootUrl + fileName + "/icon_small.jpg" + (!FTApp.getInstance().isChinaRegion() ? "" : "?token=" + iconSmallToken);
    }

    public String getMetaData(String languageCode) {
        /*return baseURIBuilder().appendPath(fileName)
                .appendPath("info/info_"+ languageCode + ".plist" + (!FTApp.getInstance().isChinaRegion() ? "" : "?token=" + infoToken))
                .build().toString();*/
        /*if (FTApp.getInstance().isChinaRegion()) {
            return rootUrl + fileName + "/info/info_" + languageCode + ".plist" + (!FTApp.getInstance().isChinaRegion() ? "" : "?token=" + infoToken);
        } else {

        }*/

        return rootUrl + fileName + "/info/info_" + languageCode + ".plist" + (!FTApp.getInstance().isChinaRegion() ? "" : "?token=" + infoToken);

    }

    public String getPreviewImage(int position) {
        /*return baseURIBuilder().appendPath(fileName)
                .appendPath("preview_"+ + position + ".jpg" + (previewTokens.isEmpty() || !FTApp.getInstance().isChinaRegion() ? "" : "?token=" + previewTokens.get(position - 1)))
                .build().toString();*/
        return rootUrl + fileName + "/preview_" + position + ".jpg" + (previewTokens.isEmpty() || !FTApp.getInstance().isChinaRegion() ? "" : "?token=" + previewTokens.get(position - 1));
    }

    public String getDownloadUrl() {
        /*return baseURIBuilder().appendPath(fileName)
                .appendPath("assets_"+ supported_devices.get(0) + ".nsthemes" + (!FTApp.getInstance().isChinaRegion() ? "" : "?token=" + themeToken))
                .build().toString();*/
        return rootUrl + fileName + "/assets_" + supported_devices.get(0) + ".nsthemes" + (!FTApp.getInstance().isChinaRegion() ? "" : "?token=" + themeToken);
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }

    public void setPreviewTokens(ArrayList<String> previewTokens) {
        this.previewTokens = previewTokens;
    }
}